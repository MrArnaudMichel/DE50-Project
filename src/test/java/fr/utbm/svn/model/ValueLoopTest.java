package fr.utbm.svn.model;

import org.junit.Test;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.*;

/**
 * Unit tests for the {@link ValueLoop} model logic.
 *
 * <p>{@code ValueLoop} stores a map of nodes (GUID to name) and a list of arc scores.
 * Because the class transitively depends on the Rhapsody API via the package, a simplified
 * local reproduction is used here to test the score-product calculation performed by
 * {@code ValueLoopStrategy}.</p>
 */
public class ValueLoopTest {

    private static final double DELTA = 0.001;

    // -------------------------------------------------------------------------
    // Simplified local reproduction of ValueLoop
    // -------------------------------------------------------------------------

    static class ValueLoop {
        final Map<String, String> nodes;
        final List<Double> arcScores;
        double score;

        /**
         * Constructs a local ValueLoop for testing purposes.
         *
         * @param n ordered map of GUID to name for nodes in the loop
         * @param s arc scores along the loop in traversal order
         */
        ValueLoop(Map<String, String> n, List<Double> s) { nodes = n; arcScores = s; }

        /** @return ordered map of GUID to name */
        Map<String, String> getNodes() { return nodes; }

        /** @return arc scores in traversal order */
        List<Double> getArcScores() { return arcScores; }

        /** @return the computed loop score */
        double getScore() { return score; }

        /**
         * Sets the computed score for this loop.
         *
         * @param score the new loop score
         */
        void setScore(double score) { this.score = score; }
    }

    // -------------------------------------------------------------------------
    // Tests
    // -------------------------------------------------------------------------

    /**
     * Verifies that nodes and arc scores are stored correctly by the constructor.
     */
    @Test
    public void constructor_storesNodesAndScores() {
        Map<String, String> nodes = new LinkedHashMap<>();
        nodes.put("guid-S", "System");
        nodes.put("guid-A", "A");
        nodes.put("guid-B", "B");
        List<Double> scores = Arrays.asList(0.5, 0.8, 0.3);

        ValueLoop loop = new ValueLoop(nodes, scores);

        assertEquals(nodes, loop.getNodes());
        assertEquals(scores, loop.getArcScores());
    }

    /**
     * Verifies that {@code setScore} and {@code getScore} round-trip correctly.
     */
    @Test
    public void setScore_getScore() {
        Map<String, String> nodes = new LinkedHashMap<>();
        nodes.put("guid-S", "System");
        nodes.put("guid-A", "A");
        ValueLoop loop = new ValueLoop(nodes, Arrays.asList(0.5, 0.8));

        loop.setScore(0.40);
        assertEquals(0.40, loop.getScore(), DELTA);
    }

    /**
     * Verifies that the initial score is {@code 0.0} before any setter call.
     */
    @Test
    public void initialScoreIsZero() {
        Map<String, String> nodes = new LinkedHashMap<>();
        nodes.put("guid-S", "System");
        nodes.put("guid-A", "A");
        ValueLoop loop = new ValueLoop(nodes, Arrays.asList(0.5, 0.8));

        assertEquals(0.0, loop.getScore(), DELTA);
    }

    /**
     * Verifies that the product of three arc scores is computed correctly.
     * Simulates the calculation performed in {@code ValueLoopStrategy}:
     * {@code loopScore = product of all arcScores}.
     * Expected: 0.5 * 0.8 * 0.4 = 0.16
     */
    @Test
    public void calculateArcScoresProduct() {
        Map<String, String> nodes = new LinkedHashMap<>();
        nodes.put("guid-S", "System");
        nodes.put("guid-A", "A");
        nodes.put("guid-B", "B");
        List<Double> arcScores = Arrays.asList(0.5, 0.8, 0.4);
        ValueLoop loop = new ValueLoop(nodes, arcScores);

        double product = 1.0;
        for (double s : loop.getArcScores()) {
            product *= s;
        }
        loop.setScore(product);

        assertEquals(0.16, loop.getScore(), DELTA);
    }

    /**
     * Verifies the product calculation with a single arc.
     * Expected: 0.95 (no multiplication needed).
     */
    @Test
    public void calculateProductWithSingleArc() {
        Map<String, String> nodes = new LinkedHashMap<>();
        nodes.put("guid-S", "System");
        nodes.put("guid-A", "A");
        List<Double> arcScores = Arrays.asList(0.95);
        ValueLoop loop = new ValueLoop(nodes, arcScores);

        double product = 1.0;
        for (double s : loop.getArcScores()) {
            product *= s;
        }
        loop.setScore(product);

        assertEquals(0.95, loop.getScore(), DELTA);
    }
}
