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
 * 1. Build a directed graph (dependent -> dependsOn) with scores keyed by GUID
 * 2. Find all cycles (value loops) passing through the "System" node
 * 3. Score of each loop = product of its arc scores
 * 4. Importance of a stakeholder = sum of scores of loops containing it / totalLoopScore
 *
 * Note: nodes in a loop are stored as Map<String, String> (GUID -> Name),
 * matching the actual ValueLoop class in fr.utbm.svn.model.
 */
public class ImportanceCalculationTest {

    private static final double DELTA = 0.001;

    // --- Simplified data structures ---

    static class ArcEdge {
        final String targetGuid;  // GUID of the  node
        final String targetName;  // display name of the node
        final double score;
        ArcEdge(String guid, String name, double s) { targetGuid = guid; targetName = name; score = s; }
    }

    static class LocalValueLoop {
        final Map<String, String> nodes;  // GUID -> Name
        final List<Double> arcScores;
        double score;
        LocalValueLoop(Map<String, String> n, List<Double> s) { nodes = n; arcScores = s; }
    }

    /**
     * Finds all loops starting and ending at systemGuid.
     * The graph maps a node GUID to its outgoing ArcEdge list.
     */
    private static List<LocalValueLoop> findValueLoops(String systemGuid, String systemName,
                                                       Map<String, List<ArcEdge>> graph) {
        List<LocalValueLoop> result = new ArrayList<>();
        if (!graph.containsKey(systemGuid)) return result;

        for (ArcEdge startArc : graph.get(systemGuid)) {
            Map<String, String> pathNodes = new LinkedHashMap<>();
            pathNodes.put(systemGuid, systemName);
            List<Double> pathScores = new ArrayList<>();
            pathScores.add(startArc.score);
            Set<String> visited = new HashSet<>();
            visited.add(systemGuid);

            dfs(startArc.targetGuid, startArc.targetName, systemGuid, systemName,
                graph, pathNodes, pathScores, visited, result);
        }
        return result;
    }

    private static void dfs(String currentGuid, String currentName,
                             String targetGuid, String targetName,
                             Map<String, List<ArcEdge>> graph,
                             Map<String, String> pathNodes,
                             List<Double> pathScores,
                             Set<String> visited,
                             List<LocalValueLoop> result) {
        if (currentGuid.equals(targetGuid)) {
            // Loop closed: record it
            Map<String, String> loopNodes = new LinkedHashMap<>(pathNodes);
            loopNodes.put(targetGuid, targetName);
            result.add(new LocalValueLoop(loopNodes, new ArrayList<>(pathScores)));
            return;
        }

        if (visited.contains(currentGuid)) return;

        visited.add(currentGuid);
        pathNodes.put(currentGuid, currentName);

        for (ArcEdge arc : graph.getOrDefault(currentGuid, Collections.emptyList())) {
            pathScores.add(arc.score);
            dfs(arc.targetGuid, arc.targetName, targetGuid, targetName,
                graph, pathNodes, pathScores, visited, result);
            pathScores.remove(pathScores.size() - 1);  // backtrack
        }

        pathNodes.remove(currentGuid);
        visited.remove(currentGuid);
    }

    private Map<String, Double> computeImportances(List<String> stakeholderGuids,
                                                    List<LocalValueLoop> loops) {
        double totalLoopScore = 0;
        for (LocalValueLoop loop : loops) {
            loop.score = 1.0;
            for (double s : loop.arcScores) loop.score *= s;
            totalLoopScore += loop.score;
        }

        Map<String, Double> result = new LinkedHashMap<>();
        for (String guid : stakeholderGuids) {
            double sum = 0;
            for (LocalValueLoop loop : loops) {
                if (loop.nodes.containsKey(guid)) {
                    sum += loop.score;
                }
            }
            result.put(guid, (totalLoopScore > 0) ? sum / totalLoopScore : 0);
        }
        return result;
    }

    /**
     * Simple case: a single loop System -> A -> System
     * Arcs: System-> A (0.5), A ->System (0.8)
     * Loop score = 0.5 * 0.8 = 0.4
     * Importance of A = 0.4/0.4 = 1.0
     */
    @Test
    public void singleLoop_oneStakeholder() {
        Map<String, List<ArcEdge>> graph = new HashMap<>();
        graph.put("guid-S", Arrays.asList(new ArcEdge("guid-A", "A", 0.5)));
        graph.put("guid-A", Arrays.asList(new ArcEdge("guid-S", "System", 0.8)));

        List<LocalValueLoop> loops = findValueLoops("guid-S", "System", graph);
        assertEquals(1, loops.size());

        Map<String, Double> importances = computeImportances(
                Arrays.asList("guid-A"), loops);

        assertEquals(1.0, importances.get("guid-A"), DELTA);
    }

    /**
     * Two loops with two stakeholders:
     * Loop 1: System -> A -> System (arcs 0.5, 0.8) -> score = 0.40
     * Loop 2: System -> B -> System (arcs 0.3, 0.4) -> score = 0.12
     * Total = 0.52
     * Importance of A = 0.40 / 0.52 ~ 0.7692
     * Importance of B = 0.12 / 0.52 ~ 0.2308
     */
    @Test
    public void twoLoops_twoStakeholders() {
        Map<String, List<ArcEdge>> graph = new HashMap<>();
        graph.put("guid-S", Arrays.asList(
                new ArcEdge("guid-A", "A", 0.5),
                new ArcEdge("guid-B", "B", 0.3)));
        graph.put("guid-A", Arrays.asList(new ArcEdge("guid-S", "System", 0.8)));
        graph.put("guid-B", Arrays.asList(new ArcEdge("guid-S", "System", 0.4)));

        List<LocalValueLoop> loops = findValueLoops("guid-S", "System", graph);
        assertEquals(2, loops.size());

        Map<String, Double> importances = computeImportances(
                Arrays.asList("guid-A", "guid-B"), loops);

        assertEquals(0.7692, importances.get("guid-A"), DELTA);
        assertEquals(0.2308, importances.get("guid-B"), DELTA);
    }

    /**
     * No loop: the graph does not return to the system.
     * System -> A (no return)
     * Importance of A = 0
     */
    @Test
    public void noLoop_zeroImportance() {
        Map<String, List<ArcEdge>> graph = new HashMap<>();
        graph.put("guid-S", Arrays.asList(new ArcEdge("guid-A", "A", 0.5)));
        // A has no outgoing arc -> no loop

        List<LocalValueLoop> loops = findValueLoops("guid-S", "System", graph);
        assertTrue(loops.isEmpty());

        Map<String, Double> importances = computeImportances(
                Arrays.asList("guid-A"), loops);

        assertEquals(0.0, importances.get("guid-A"), DELTA);
    }

    /**
     * Loop with 3 nodes: System -> A -> B -> System
     * Arcs: 0.5, 0.8, 0.4
     * Score = 0.5 * 0.8 * 0.4 = 0.16
     * Both A and B are in the loop -> importance = 1.0 each
     */
    @Test
    public void loopWithThreeNodes() {
        Map<String, List<ArcEdge>> graph = new HashMap<>();
        graph.put("guid-S", Arrays.asList(new ArcEdge("guid-A", "A", 0.5)));
        graph.put("guid-A", Arrays.asList(new ArcEdge("guid-B", "B", 0.8)));
        graph.put("guid-B", Arrays.asList(new ArcEdge("guid-S", "System", 0.4)));

        List<LocalValueLoop> loops = findValueLoops("guid-S", "System", graph);
        assertEquals(1, loops.size());

        Map<String, Double> importances = computeImportances(
                Arrays.asList("guid-A", "guid-B"), loops);

        assertEquals(1.0, importances.get("guid-A"), DELTA);
        assertEquals(1.0, importances.get("guid-B"), DELTA);
    }

    /**
     * Stakeholder not in any loop -> importance = 0
     */
    @Test
    public void stakeholderNotInLoop() {
        Map<String, List<ArcEdge>> graph = new HashMap<>();
        graph.put("guid-S", Arrays.asList(new ArcEdge("guid-A", "A", 0.5)));
        graph.put("guid-A", Arrays.asList(new ArcEdge("guid-S", "System", 0.8)));

        List<LocalValueLoop> loops = findValueLoops("guid-S", "System", graph);

        Map<String, Double> importances = computeImportances(
                Arrays.asList("guid-A", "guid-C"), loops);

        assertEquals(1.0, importances.get("guid-A"), DELTA);
        assertEquals(0.0, importances.get("guid-C"), DELTA);
    }

    /**
     * Empty graph -> no loop -> importance = 0
     */
    @Test
    public void emptyGraph() {
        Map<String, List<ArcEdge>> graph = new HashMap<>();

        List<LocalValueLoop> loops = findValueLoops("guid-S", "System", graph);
        assertTrue(loops.isEmpty());

        Map<String, Double> importances = computeImportances(
                Arrays.asList("guid-A"), loops);

        assertEquals(0.0, importances.get("guid-A"), DELTA);
    }
}
