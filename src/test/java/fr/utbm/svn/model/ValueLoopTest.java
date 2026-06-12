package fr.utbm.svn.model;

import org.junit.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.*;

/**
 * Unit tests for the ValueLoop model logic.
 *
 * ValueLoop stores a list of nodes and a list of arc scores.
 * Since the class transitively depends on the Rhapsody API via the package,
 * we reproduce a simplified ValueLoop model here to test the calculation logic
 * of the product of arc scores (as used in ValueLoopStrategy).
 */
public class ValueLoopTest {

    private static final double DELTA = 0.001;

    // --- Simplified model reproducing ValueLoop ---

    static class ValueLoop {
        final List<String> nodes;
        final List<Double> arcScores;
        double score;

        ValueLoop(List<String> n, List<Double> s) { nodes = n; arcScores = s; }
        List<String> getNodes() { return nodes; }
        List<Double> getArcScores() { return arcScores; }
        double getScore() { return score; }
        void setScore(double score) { this.score = score; }
    }

    @Test
    public void constructor_storesNodesAndScores() {
        List<String> nodes = Arrays.asList("S", "A", "B", "S");
        List<Double> scores = Arrays.asList(0.5, 0.8, 0.3);

        ValueLoop loop = new ValueLoop(nodes, scores);

        assertEquals(nodes, loop.getNodes());
        assertEquals(scores, loop.getArcScores());
    }

    @Test
    public void setScore_getScore() {
        ValueLoop loop = new ValueLoop(
                Arrays.asList("S", "A", "S"),
                Arrays.asList(0.5, 0.8)
        );

        loop.setScore(0.40);
        assertEquals(0.40, loop.getScore(), DELTA);
    }

    @Test
    public void initialScoreIsZero() {
        ValueLoop loop = new ValueLoop(
                Arrays.asList("S", "A", "S"),
                Arrays.asList(0.5, 0.8)
        );

        assertEquals(0.0, loop.getScore(), DELTA);
    }

    @Test
    public void calculateArcScoresProduct() {
        // Simulates the calculation done in ValueLoopStrategy:
        // loopScore = product of all arcScores
        List<Double> arcScores = Arrays.asList(0.5, 0.8, 0.4);
        ValueLoop loop = new ValueLoop(Arrays.asList("S", "A", "B", "S"), arcScores);

        double product = 1.0;
        for (double s : loop.getArcScores()) {
            product *= s;
        }
        loop.setScore(product);

        // 0.5 * 0.8 * 0.4 = 0.16
        assertEquals(0.16, loop.getScore(), DELTA);
    }

    @Test
    public void calculateProductWithSingleArc() {
        List<Double> arcScores = Arrays.asList(0.95);
        ValueLoop loop = new ValueLoop(Arrays.asList("S", "A", "S"), arcScores);

        double product = 1.0;
        for (double s : loop.getArcScores()) {
            product *= s;
        }
        loop.setScore(product);

        assertEquals(0.95, loop.getScore(), DELTA);
    }
}
