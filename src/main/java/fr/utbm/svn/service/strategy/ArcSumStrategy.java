package fr.utbm.svn.service.strategy;

import com.telelogic.rhapsody.core.IRPModelElement;
import com.telelogic.rhapsody.core.IRPPort;
import fr.utbm.svn.Logger;
import fr.utbm.svn.model.Stakeholder;
import fr.utbm.svn.model.ValueArc;
import fr.utbm.svn.service.ICalculationStrategy;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Fallback calculation strategy that computes stakeholder importance as the normalised
 * sum of the scores of all arcs directly connected to each stakeholder.
 *
 * <p>This strategy is used when no value loops can be detected in the SVN diagram.
 * Each arc score is added to both endpoints (dependent and depended-on sides), then
 * every stakeholder's raw score is divided by the grand total to obtain a value in
 * [0.0, 1.0].</p>
 *
 * <p>Ports are transparently resolved to their owning element so that arcs connected
 * via interface ports are attributed to the correct stakeholder.</p>
 */
public class ArcSumStrategy implements ICalculationStrategy {

    private final Logger logger = Logger.getInstance();

    /**
     * Computes normalised importance scores using the arc-sum algorithm.
     *
     * <p>For each arc, its score is credited to any stakeholder that appears at either
     * the {@code dependent} or the {@code dependsOn} end. The raw scores are then
     * normalised so they sum to {@code 1.0}.</p>
     *
     * @param stakeholders the list of stakeholders to score
     * @param valueArcs    the list of value arcs in the diagram
     * @return a map from each stakeholder to its normalised importance score;
     *         all values are {@code 0.0} when there are no arcs (total is zero)
     */
    @Override
    public Map<Stakeholder, Double> computeScores(List<Stakeholder> stakeholders, List<ValueArc> valueArcs) {
        double total = 0;

        for (Stakeholder sh : stakeholders) {
            double score = 0;
            for (ValueArc arc : valueArcs) {
                try {
                    IRPModelElement dependent = arc.getDependent();
                    IRPModelElement dependsOn = arc.getDependsOn();
                    IRPModelElement out = dependsOn instanceof IRPPort ? (dependsOn).getOwner() : dependsOn;
                    IRPModelElement in = dependent instanceof IRPPort ? (dependent).getOwner() : dependent;
                    if ((in != null && sh.getGUID().equals(in.getGUID())) || (out != null && sh.getGUID().equals(out.getGUID()))) {
                        score += arc.getScore();
                    }
                } catch (Exception ignored) {}
            }
            sh.setScore(score);
            total += score;
        }

        Map<Stakeholder, Double> scores = new HashMap<>();

        for (Stakeholder sh : stakeholders) {
            double importance = (total > 0) ? sh.getScore() / total : 0;
            scores.put(sh, importance);
            logger.log("Importance " + sh.getName()
                    + " = " + String.format("%.4f", importance));
        }

        return scores;
    }
}
