package fr.utbm.svn.model;

import java.util.List;
import java.util.Map;

/**
 * Represents a value loop detected in a Stakeholder Value Network (SVN) diagram.
 *
 * <p>A value loop is a directed cycle in the SVN graph that starts and ends at the system
 * element and passes through one or more stakeholders. Its score is the product of the
 * scores of all arcs it contains, reflecting the combined value delivered through the loop.</p>
 */
public class ValueLoop {

    /** Ordered map of GUID to name for every node (system and stakeholders) in the loop. */
    final Map<String, String> nodes;

    /** Score of each arc in the loop, in traversal order. */
    final List<Double> arcScores;

    /** The arcs that form this loop, in traversal order. */
    final List<ValueArc> arcs;

    /** Product of all arc scores; computed and stored by the calculation strategy. */
    double score;

    /**
     * Constructs a new value loop.
     *
     * @param n ordered map of GUID to name for every node in the loop
     * @param s arc scores along the loop, in traversal order
     * @param a arc objects along the loop, in traversal order
     */
    public ValueLoop(Map<String, String> n, List<Double> s, List<ValueArc> a) {
        nodes = n;
        arcScores = s;
        arcs = a;
    }

    /**
     * Returns the ordered map of node GUIDs to node names for this loop.
     *
     * @return GUID to name map
     */
    public Map<String, String> getNodes() {
        return nodes;
    }

    /**
     * Returns the arc scores along this loop in traversal order.
     *
     * @return list of arc scores
     */
    public List<Double> getArcScores() {
        return arcScores;
    }

    /**
     * Returns the arc objects that form this loop in traversal order.
     *
     * @return list of {@link ValueArc} objects
     */
    public List<ValueArc> getArcs() {
        return arcs;
    }

    /**
     * Returns the computed score of this loop (product of all arc scores).
     *
     * @return loop score
     */
    public double getScore() {
        return score;
    }

    /**
     * Sets the computed score for this loop.
     *
     * @param score the product of all arc scores along the loop
     */
    public void setScore(double score) {
        this.score = score;
    }
}
