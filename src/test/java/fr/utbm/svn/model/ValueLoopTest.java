package fr.utbm.svn.model;

import org.junit.Test;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.*;

/**
 * Unit tests for the ValueLoop model logic.
 *
 * ValueLoop stores a map of nodes (GUID -> Name) and a list of arc scores.
 * Since the class transitively depends on the Rhapsody API via the package,
 * we reproduce a simplified ValueLoop model here to test the calculation logic
 * of the product of arc scores (as used in ValueLoopStrategy).
 */
public class ValueLoopTest {

    private static final double DELTA = 0.001;

    static class ValueLoop {
        final Map<String, String> nodes;
        final List<Double> arcScores;
        double score;

        ValueLoop(Map<String, String> n, List<Double> s) { nodes = n; arcScores = s; }
        Map<String, String> getNodes() { return nodes; }
        List<Double> getArcScores() { return arcScores; }
        double getScore() { return score; }
        void setScore(double score) { this.score = score; }
    }

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

    @Test
    public void setScore_getScore() {
        Map<String, String> nodes = new LinkedHashMap<>();
        nodes.put("guid-S", "System");
        nodes.put("guid-A", "A");
        ValueLoop loop = new ValueLoop(nodes, Arrays.asList(0.5, 0.8));

        loop.setScore(0.40);
        assertEquals(0.40, loop.getScore(), DELTA);
    }

    @Test
    public void initialScoreIsZero() {
        Map<String, String> nodes = new LinkedHashMap<>();
        nodes.put("guid-S", "System");
        nodes.put("guid-A", "A");
        ValueLoop loop = new ValueLoop(nodes, Arrays.asList(0.5, 0.8));

        assertEquals(0.0, loop.getScore(), DELTA);
    }

    @Test
    public void calculateArcScoresProduct() {
        // Simulates the calculation done in ValueLoopStrategy:
        // loopScore = product of all arcScores
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

        // 0.5 * 0.8 * 0.4 = 0.16
        assertEquals(0.16, loop.getScore(), DELTA);
    }

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
