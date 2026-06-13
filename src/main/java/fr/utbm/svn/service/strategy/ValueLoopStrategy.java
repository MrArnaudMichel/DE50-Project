package fr.utbm.svn.service.strategy;

import com.telelogic.rhapsody.core.IRPModelElement;
import com.telelogic.rhapsody.core.IRPPort;
import fr.utbm.svn.Logger;
import fr.utbm.svn.model.*;
import fr.utbm.svn.service.ICalculationStrategy;

import java.util.*;

public class ValueLoopStrategy implements ICalculationStrategy {

    private final SVNSystem system;

    private final Logger logger = Logger.getInstance();

    public ValueLoopStrategy(SVNSystem system) {
        this.system = system;
    }

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

        // Visiter le nœud
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
