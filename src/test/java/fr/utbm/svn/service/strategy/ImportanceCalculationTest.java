package fr.utbm.svn.service.strategy;

import org.junit.Test;

import java.util.*;

import static org.junit.Assert.*;

/**
 * Unit tests for the importance calculation algorithm using value loops.
 *
 * <p>Reproduces the pure logic of {@code ValueLoopStrategy.computeScores} without any
 * Rhapsody dependency, using simple local data structures. The tested algorithm is:
 * <ol>
 *   <li>Build a directed graph (dependent → dependsOn) with arc scores keyed by GUID.</li>
 *   <li>Find all cycles (value loops) passing through the "System" node.</li>
 *   <li>Score each loop = product of its arc scores.</li>
 *   <li>Importance of a stakeholder = sum of scores of loops containing it / totalLoopScore.</li>
 * </ol>
 *
 * <p>Nodes in a loop are stored as {@code Map<String, String>} (GUID → name), matching
 * the actual {@code ValueLoop} class in {@code fr.utbm.svn.model}.</p>
 */
public class ImportanceCalculationTest {

    private static final double DELTA = 0.001;

    // -------------------------------------------------------------------------
    // Simplified local data structures
    // -------------------------------------------------------------------------

    /** Represents a directed arc with a target node and a numeric score. */
    static class ArcEdge {
        /** GUID of the target node. */
        final String targetGuid;
        /** Display name of the target node. */
        final String targetName;
        /** Arc score. */
        final double score;

        /**
         * Constructs an arc edge.
         *
         * @param guid  GUID of the target node
         * @param name  display name of the target node
         * @param s     arc score
         */
        ArcEdge(String guid, String name, double s) { targetGuid = guid; targetName = name; score = s; }
    }

    /** Represents a value loop with an ordered set of nodes and accumulated arc scores. */
    static class LocalValueLoop {
        /** Ordered map of GUID to name for every node in the loop. */
        final Map<String, String> nodes;
        /** Arc scores along the loop in traversal order. */
        final List<Double> arcScores;
        /** Computed loop score (product of arc scores). */
        double score;

        /**
         * Constructs a local value loop.
         *
         * @param n ordered map of GUID to name
         * @param s arc scores in traversal order
         */
        LocalValueLoop(Map<String, String> n, List<Double> s) { nodes = n; arcScores = s; }
    }

    // -------------------------------------------------------------------------
    // Algorithm helpers
    // -------------------------------------------------------------------------

    /**
     * Finds all loops starting and ending at the system node.
     *
     * <p>The graph maps a node GUID to its outgoing {@link ArcEdge} list.</p>
     *
     * @param systemGuid GUID of the system node (start/end of every loop)
     * @param systemName display name of the system node
     * @param graph      directed adjacency map: node GUID to list of outgoing arcs
     * @return list of all detected value loops; empty if none found
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

    /**
     * Recursive depth-first search used by {@link #findValueLoops}.
     *
     * <p>Records a loop when the current node equals the target (system). Uses standard
     * DFS backtracking to restore path state after each recursive call.</p>
     *
     * @param currentGuid GUID of the node currently being visited
     * @param currentName display name of the current node
     * @param targetGuid  GUID of the system node (loop closes when current == target)
     * @param targetName  display name of the system node
     * @param graph       directed adjacency map
     * @param pathNodes   mutable map of GUID to name for nodes on the current path
     * @param pathScores  mutable list of arc scores accumulated along the current path
     * @param visited     mutable set of GUIDs already on the current DFS branch
     * @param result      accumulator for completed loops
     */
    private static void dfs(String currentGuid, String currentName,
                             String targetGuid, String targetName,
                             Map<String, List<ArcEdge>> graph,
                             Map<String, String> pathNodes,
                             List<Double> pathScores,
                             Set<String> visited,
                             List<LocalValueLoop> result) {
        if (currentGuid.equals(targetGuid)) {
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

    /**
     * Computes normalised importance scores for a list of stakeholders given a set of loops.
     *
     * <p>Each loop score is computed as the product of its arc scores. The importance of a
     * stakeholder is the sum of scores of loops it belongs to, divided by the total loop
     * score.</p>
     *
     * @param stakeholderGuids ordered list of stakeholder GUIDs to score
     * @param loops            value loops detected by {@link #findValueLoops}
     * @return map from stakeholder GUID to normalised importance score in [0.0, 1.0]
     */
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

    // -------------------------------------------------------------------------
    // Tests
    // -------------------------------------------------------------------------

    /**
     * Single loop: System → A → System with arc scores 0.5 and 0.8.
     * Loop score = 0.5 * 0.8 = 0.40.
     * Stakeholder A is in the only loop, so importance = 1.0.
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
     * Loop 1: System → A → System (arcs 0.5, 0.8) → score = 0.40
     * Loop 2: System → B → System (arcs 0.3, 0.4) → score = 0.12
     * Total = 0.52; importance A ≈ 0.7692, importance B ≈ 0.2308.
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
     * No loop: the graph does not return to the system (System → A, no back-arc).
     * All importance scores should be 0.
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
     * Loop with 3 nodes: System → A → B → System with arc scores 0.5, 0.8, 0.4.
     * Loop score = 0.16. Both A and B participate → importance = 1.0 each.
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
     * Stakeholder C is not part of any loop while A is; importance of C should be 0.
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
     * Empty graph: no arcs at all, so no loops and all importances are 0.
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
