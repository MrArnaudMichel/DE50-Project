package fr.utbm.svn.service.strategy;

import com.telelogic.rhapsody.core.IRPModelElement;
import com.telelogic.rhapsody.core.IRPPort;
import fr.utbm.svn.Logger;
import fr.utbm.svn.model.*;
import fr.utbm.svn.service.ICalculationStrategy;

import java.util.*;

/**
 * Primary calculation strategy that computes stakeholder importance using value loops.
 *
 * <p>A <em>value loop</em> is a directed cycle in the SVN graph that starts and ends at
 * the system element. The score of a loop is the product of the scores of its arcs, and
 * the importance of a stakeholder is the proportion of the total loop score contributed by
 * loops that contain that stakeholder.</p>
 *
 * <p>Algorithm overview:
 * <ol>
 *   <li>Build a directed adjacency map ({@link #buildGraph}) from the value arcs.</li>
 *   <li>Run a depth-first search ({@link #findValueLoops}) from the system node to detect
 *       all cycles that return to the system.</li>
 *   <li>Compute each loop's score as the product of its arc scores.</li>
 *   <li>Assign each stakeholder an importance proportional to the sum of scores of loops
 *       it participates in.</li>
 * </ol>
 *
 * <p>If no loops are found, an empty map is returned so that the caller can fall back to
 * {@link ArcSumStrategy}.</p>
 */
public class ValueLoopStrategy implements ICalculationStrategy {

    private final SVNSystem system;
    private final Logger logger = Logger.getInstance();

    /**
     * Constructs a new strategy for the given system element.
     *
     * @param system the SVN system that acts as the start/end node for all value loops
     */
    public ValueLoopStrategy(SVNSystem system) {
        this.system = system;
    }

    /**
     * Computes normalised importance scores using the value-loop algorithm.
     *
     * <p>Returns an empty map when no loops are detected so the caller can apply a
     * fallback strategy.</p>
     *
     * @param stakeholders the stakeholders to score
     * @param valueArcs    all value arcs in the diagram
     * @return a map from each stakeholder to its normalised importance score, or an empty
     *         map if no value loops were found
     */
    @Override
    public Map<Stakeholder, Double> computeScores(List<Stakeholder> stakeholders, List<ValueArc> valueArcs) {
        Map<String, List<ValueArc>> graph = this.buildGraph(valueArcs);

        List<ValueLoop> loops = findValueLoops(system, graph);
        logger.log("Value loops found : " + loops.size());

        if (loops.isEmpty()) {
            logger.log("No loop — calculation failed.");
            return Collections.emptyMap();
        }

        double totalLoopScore = 0;
        for (ValueLoop loop : loops) {
            loop.setScore(1);
            for (double s : loop.getArcScores()) loop.setScore(loop.getScore() * s);
            totalLoopScore += loop.getScore();
            logger.log("Loop " + loop.getNodes().values() + " score=" + loop.getScore());
        }
        logger.log("Total Loop Score : " + String.format("%.4f", totalLoopScore));

        for (Stakeholder sh : stakeholders) {
            double sumLoopsContaining = 0;
            for (ValueLoop loop : loops) {
                if (loop.getNodes().containsKey(sh.getGUID())) {
                    sumLoopsContaining += loop.getScore();
                }
            }
            double importance = (totalLoopScore > 0) ? sumLoopsContaining / totalLoopScore : 0;
            sh.setScore(importance);
            logger.log("Importance " + sh.getGUID()
                    + " = " + String.format("%.4f", importance));
        }

        Map<Stakeholder, Double> scores = new HashMap<>();

        for (Stakeholder sh : stakeholders) {
            scores.put(sh, sh.getScore());
        }

        system.setTotalLoopScore(totalLoopScore);
        system.setLoops(loops);
        logger.log("Calcul finished. " + stakeholders.size() + " actors updated.");

        return scores;
    }

    /**
     * Builds a directed adjacency map from the list of value arcs.
     *
     * <p>The map associates each node GUID (the {@code dependent} side of an arc) with
     * the list of arcs departing from it. Port elements are resolved to their owning
     * element before being inserted into the graph.</p>
     *
     * @param arcs the value arcs to process
     * @return adjacency map: node GUID to list of outgoing {@link ValueArc}s
     */
    private Map<String, List<ValueArc>> buildGraph(List<ValueArc> arcs) {
        Map<String, List<ValueArc>> graph = new HashMap<>();
        for (ValueArc arc : arcs) {
            try {
                IRPModelElement dependent = arc.getDependent();
                IRPModelElement dependsOn = arc.getDependsOn();
                dependsOn = dependsOn instanceof IRPPort ? (dependsOn).getOwner() : dependsOn;
                dependent = dependent instanceof IRPPort ? (dependent).getOwner() : dependent;
                if (dependsOn == null || dependent == null) continue;

                graph.computeIfAbsent(dependent.getGUID(), k -> new ArrayList<>())
                        .add(arc);

            } catch (Exception ignored) {}
        }
        return graph;
    }

    /**
     * Finds all directed cycles in the graph that start and end at the system node.
     *
     * <p>For each arc departing from the system, a DFS explores the graph looking for
     * a path back to the system. Every such path constitutes a value loop.</p>
     *
     * @param system the SVN system (start/end node)
     * @param graph  the adjacency map built by {@link #buildGraph}
     * @return list of all detected {@link ValueLoop}s; empty if none exist
     */
    private List<ValueLoop> findValueLoops(SVNSystem system, Map<String, List<ValueArc>> graph) {
        List<ValueLoop> result = new ArrayList<>();
        String systemGUID = system.getGUID();

        if (!graph.containsKey(systemGUID)) return result;

        List<ValueArc> startingArcs = graph.getOrDefault(systemGUID, Collections.emptyList());
        for (ValueArc startArc : startingArcs) {
            Map<String, String> pathNodes = new HashMap<>();
            pathNodes.put(systemGUID, system.getName()); // start node

            List<Double> pathScores = new ArrayList<>();
            pathScores.add(startArc.getScore());

            List<ValueArc> pathArcs = new ArrayList<>();
            pathArcs.add(startArc);

            Set<String> visited = new HashSet<>();
            visited.add(systemGUID);

            IRPModelElement nextActor = startArc.getDependsOn();
            nextActor = nextActor instanceof IRPPort ? nextActor.getOwner() : nextActor;

            if (nextActor != null) {
                dfs(nextActor, system.getSystem(), graph, pathNodes, pathScores, pathArcs, visited, result);
            }
        }
        return result;
    }

    /**
     * Recursive depth-first search that builds value loops by exploring the adjacency graph.
     *
     * <p>When {@code current} equals {@code target} (i.e. the system is reached again),
     * the current path is recorded as a new {@link ValueLoop}. The method uses standard
     * DFS backtracking: path state is restored before returning from each recursive call.</p>
     *
     * @param current    the model element currently being visited
     * @param target     the system element (loop closes when {@code current == target})
     * @param graph      the adjacency map
     * @param pathNodes  mutable map of GUID to name for nodes on the current path
     * @param pathScores mutable list of arc scores accumulated along the current path
     * @param pathArcs   mutable list of arcs traversed along the current path
     * @param visited    mutable set of GUIDs already on the current DFS branch
     * @param result     accumulator for completed loops
     */
    private void dfs(IRPModelElement current, IRPModelElement target, Map<String, List<ValueArc>> graph,
                     Map<String, String> pathNodes, List<Double> pathScores, List<ValueArc> pathArcs,
                     Set<String> visited, List<ValueLoop> result) {
        String currentGUID = current.getGUID();
        String currentName = current.getName();
        String targetGUID = target.getGUID();

        if (currentGUID.equals(targetGUID)) {
            Map<String, String> loopNodes = new LinkedHashMap<>(pathNodes); // LinkedHashMap to preserve order if needed
            loopNodes.put(targetGUID, target.getName());
            List<Double> loopScores = new ArrayList<>(pathScores);
            List<ValueArc> loopArcs = new ArrayList<>(pathArcs);
            result.add(new ValueLoop(loopNodes, loopScores, loopArcs));
            return;
        }

        if (visited.contains(currentGUID)) {
            return;
        }

        visited.add(currentGUID);
        pathNodes.put(currentGUID, currentName);

        List<ValueArc> outgoingArcs = graph.getOrDefault(currentGUID, Collections.emptyList());
        for (ValueArc arc : outgoingArcs) {
            IRPModelElement nextActor = arc.getDependsOn();
            nextActor = nextActor instanceof IRPPort ? nextActor.getOwner() : nextActor;

            if (nextActor != null) {
                pathScores.add(arc.getScore());
                pathArcs.add(arc);

                dfs(nextActor, target, graph, pathNodes, pathScores, pathArcs, visited, result);

                pathScores.remove(pathScores.size() - 1); // Backtrack
                pathArcs.remove(pathArcs.size() - 1);
            }
        }

        // Backtrack
        pathNodes.remove(currentGUID);
        visited.remove(currentGUID);
    }
}
