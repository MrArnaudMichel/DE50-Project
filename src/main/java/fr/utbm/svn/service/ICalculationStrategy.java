package fr.utbm.svn.service;

import fr.utbm.svn.model.Stakeholder;
import fr.utbm.svn.model.ValueArc;

import java.util.List;
import java.util.Map;

/**
 * Strategy contract for computing importance scores from a set of stakeholders and arcs.
 *
 * <p>Implementations encapsulate a specific algorithm (e.g., arc-sum, value-loop product)
 * and return a map of normalised importance scores in the range [0.0, 1.0], one per
 * stakeholder. An empty map signals that the strategy could not produce a result and a
 * fallback strategy should be tried.</p>
 */
public interface ICalculationStrategy {

    /**
     * Computes a normalised importance score for each stakeholder.
     *
     * @param stakeholders the stakeholders whose importance scores should be computed
     * @param valueArcs    all value arcs present in the diagram
     * @return a map from each {@link Stakeholder} to its normalised importance score;
     *         an empty map indicates that this strategy was unable to produce a result
     */
    Map<Stakeholder, Double> computeScores(List<Stakeholder> stakeholders, List<ValueArc> valueArcs);
}
