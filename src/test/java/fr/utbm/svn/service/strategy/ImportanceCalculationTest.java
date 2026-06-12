package fr.utbm.svn.service.strategy;

import org.junit.Test;

import java.util.*;

import static org.junit.Assert.*;

/**
 * Unit tests for the importance calculation algorithm using Value Loops.
 *
 * We reproduce the pure logic of ValueLoopStrategy.computeScores here
 * without any Rhapsody dependency, using simple structures.
 *
 * Tested algorithm:
 * 1. Build a directed graph (dependent → dependsOn) with scores
 * 2. Find all cycles (value loops) passing through the "System" node
 * 3. Score of each loop = product of its arc scores
 * 4. Importance of a stakeholder = sum of the scores of loops containing it / totalLoopScore
 */
public class ImportanceCalculationTest {

    private static final double DELTA = 0.001;

    // --- Simplified data structures ---

    public static class ArcEdge {
        final String target;
        final double score;
        ArcEdge(String t, double s) { target = t; score = s; }
    }

    public static class LocalValueLoop {
        final List<String> nodes;
        final List<Double> arcScores;
        double score;
        LocalValueLoop(List<String> n, List<Double> s) { nodes = n; arcScores = s; }
    }

    // --- Reproduction of the loop detection logic (DFS) ---

    private static List<LocalValueLoop> findValueLoops(String systemName,
                                                   Map<String, List<ArcEdge>> graph) {
        List<LocalValueLoop> result = new ArrayList<LocalValueLoop>();
        if (!graph.containsKey(systemName)) return result;

        Deque<Object[]> stack = new ArrayDeque<Object[]>();
        stack.push(new Object[]{systemName, new ArrayList<String>(),
                new ArrayList<Double>(), new HashSet<String>()});

        while (!stack.isEmpty()) {
            Object[] state = stack.pop();
            String current = (String) state[0];
            @SuppressWarnings("unchecked") List<String> path = (List<String>) state[1];
            @SuppressWarnings("unchecked") List<Double> scores = (List<Double>) state[2];
            @SuppressWarnings("unchecked") Set<String> visited = (Set<String>) state[3];

            List<ArcEdge> neighbors = graph.getOrDefault(current, Collections.emptyList());
            for (ArcEdge edge : neighbors) {
                String next = edge.target;

                if (next.equals(systemName) && !path.isEmpty()) {
                    List<String> loopNodes = new ArrayList<>(path);
                    loopNodes.add(systemName);
                    List<Double> loopScores = new ArrayList<>(scores);
                    loopScores.add(edge.score);
                    LocalValueLoop valLoop = new LocalValueLoop(loopNodes, loopScores);
                    result.add(valLoop);
                    continue;
                }
                if (visited.contains(next)) continue;

                Set<String> newVisited = new HashSet<>(visited);
                newVisited.add(next);
                List<String> newPath = new ArrayList<>(path);
                newPath.add(next);
                List<Double> newScores = new ArrayList<>(scores);
                newScores.add(edge.score);
                stack.push(new Object[]{next, newPath, newScores, newVisited});
            }
        }
        return result;
    }

    // --- Calculation of importance scores ---

    private Map<String, Double> computeImportances(List<String> stakeholderNames,
                                                    List<LocalValueLoop> loops) {
        double totalLoopScore = 0;
        for (LocalValueLoop loop : loops) {
            loop.score = 1.0;
            for (double s : loop.arcScores) loop.score *= s;
            totalLoopScore += loop.score;
        }

        Map<String, Double> result = new LinkedHashMap<>();
        for (String sh : stakeholderNames) {
            double sumLoopsContaining = 0;
            for (LocalValueLoop loop : loops) {
                if (loop.nodes.contains(sh)) {
                    sumLoopsContaining += loop.score;
                }
            }
            double importance = (totalLoopScore > 0) ? sumLoopsContaining / totalLoopScore : 0;
            result.put(sh, importance);
        }
        return result;
    }

    // ==========================================================================
    // Tests
    // ==========================================================================

    /**
     * Simple case: a single loop System → A → System
     * Arcs: System→A (0.5), A→System (0.8)
     * Loop score = 0.5 * 0.8 = 0.4
     * Importance of A = 0.4 / 0.4 = 1.0
     */
    @Test
    public void singleLoop_oneStakeholder() {
        Map<String, List<ArcEdge>> graph = new HashMap<>();
        graph.put("System", Arrays.asList(new ArcEdge("A", 0.5)));
        graph.put("A", Arrays.asList(new ArcEdge("System", 0.8)));

        List<LocalValueLoop> loops = findValueLoops("System", graph);
        assertEquals(1, loops.size());

        Map<String, Double> importances = computeImportances(
                Arrays.asList("A"), loops);

        assertEquals(1.0, importances.get("A"), DELTA);
    }

    /**
     * Two loops with two stakeholders:
     * Loop 1: System → A → System (arcs 0.5, 0.8) → score = 0.40
     * Loop 2: System → B → System (arcs 0.3, 0.4) → score = 0.12
     * Total = 0.52
     * Importance of A = 0.40 / 0.52 ≈ 0.7692
     * Importance of B = 0.12 / 0.52 ≈ 0.2308
     */
    @Test
    public void twoLoops_twoStakeholders() {
        Map<String, List<ArcEdge>> graph = new HashMap<>();
        graph.put("System", Arrays.asList(
                new ArcEdge("A", 0.5),
                new ArcEdge("B", 0.3)));
        graph.put("A", Arrays.asList(new ArcEdge("System", 0.8)));
        graph.put("B", Arrays.asList(new ArcEdge("System", 0.4)));

        List<LocalValueLoop> loops = findValueLoops("System", graph);
        assertEquals(2, loops.size());

        Map<String, Double> importances = computeImportances(
                Arrays.asList("A", "B"), loops);

        assertEquals(0.7692, importances.get("A"), DELTA);
        assertEquals(0.2308, importances.get("B"), DELTA);
    }

    /**
     * No loop: the graph does not return to the system.
     * System → A (no return)
     * Importance of A = 0
     */
    @Test
    public void noLoop_zeroImportance() {
        Map<String, List<ArcEdge>> graph = new HashMap<>();
        graph.put("System", Arrays.asList(new ArcEdge("A", 0.5)));
        // A has no outgoing arc → no loop

        List<LocalValueLoop> loops = findValueLoops("System", graph);
        assertTrue(loops.isEmpty());

        Map<String, Double> importances = computeImportances(
                Arrays.asList("A"), loops);

        assertEquals(0.0, importances.get("A"), DELTA);
    }

    /**
     * Loop with 3 nodes: System → A → B → System
     * Arcs: 0.5, 0.8, 0.4
     * Score = 0.5 * 0.8 * 0.4 = 0.16
     * Both A and B are in the loop → importance = 1.0 each
     */
    @Test
    public void loopWithThreeNodes() {
        Map<String, List<ArcEdge>> graph = new HashMap<>();
        graph.put("System", Arrays.asList(new ArcEdge("A", 0.5)));
        graph.put("A", Arrays.asList(new ArcEdge("B", 0.8)));
        graph.put("B", Arrays.asList(new ArcEdge("System", 0.4)));

        List<LocalValueLoop> loops = findValueLoops("System", graph);
        assertEquals(1, loops.size());

        Map<String, Double> importances = computeImportances(
                Arrays.asList("A", "B"), loops);

        assertEquals(1.0, importances.get("A"), DELTA);
        assertEquals(1.0, importances.get("B"), DELTA);
    }

    /**
     * Stakeholder not in any loop → importance = 0
     */
    @Test
    public void stakeholderNotInLoop() {
        Map<String, List<ArcEdge>> graph = new HashMap<>();
        graph.put("System", Arrays.asList(new ArcEdge("A", 0.5)));
        graph.put("A", Arrays.asList(new ArcEdge("System", 0.8)));

        List<LocalValueLoop> loops = findValueLoops("System", graph);

        Map<String, Double> importances = computeImportances(
                Arrays.asList("A", "C"), loops);

        assertEquals(1.0, importances.get("A"), DELTA);
        assertEquals(0.0, importances.get("C"), DELTA);
    }

    /**
     * Empty graph → no loop → importance = 0
     */
    @Test
    public void emptyGraph() {
        Map<String, List<ArcEdge>> graph = new HashMap<>();

        List<LocalValueLoop> loops = findValueLoops("System", graph);
        assertTrue(loops.isEmpty());

        Map<String, Double> importances = computeImportances(
                Arrays.asList("A"), loops);

        assertEquals(0.0, importances.get("A"), DELTA);
    }
}
