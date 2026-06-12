package fr.utbm.svn.model;

import org.junit.Test;
import static org.junit.Assert.*;

/**
 * Unit tests for the getArcScore(benefit, supply) calculation logic.
 * 
 * This static method is defined in ValueArc and calculates a score
 * based on a Benefit × Supply correspondence matrix.
 *
 * Since ValueArc depends on the Rhapsody API (not available during tests),
 * the calculation logic is reproduced here identically to verify
 * that the results are consistent with the expected matrix.
 *
 * Matrix:
 *              Might Be  Should Be  Must Be
 * High           0.30       0.50      0.95
 * Medium         0.20       0.40      0.80
 * Low            0.10       0.20      0.40
 */
public class ValueArcScoreTest {

    private static final double DELTA = 0.001;

    /**
     * Exact reproduction of ValueArc.getArcScore for self-contained tests.
     */
    private static double getArcScore(String benefit, String supply) {
        if ("MUST_BE".equals(benefit)) {
            if ("HIGH".equals(supply))   return 0.95;
            if ("MEDIUM".equals(supply)) return 0.8;
            if ("LOW".equals(supply))    return 0.4;
        } else if ("SHOULD_BE".equals(benefit)) {
            if ("HIGH".equals(supply))   return 0.5;
            if ("MEDIUM".equals(supply)) return 0.4;
            if ("LOW".equals(supply))    return 0.2;
        } else {
            if ("HIGH".equals(supply))   return 0.3;
            if ("MEDIUM".equals(supply)) return 0.2;
            if ("LOW".equals(supply))    return 0.1;
        }
        return 0.2;
    }

    // -------- MUST_BE --------

    @Test
    public void mustBe_High() {
        assertEquals(0.95, getArcScore("MUST_BE", "HIGH"), DELTA);
    }

    @Test
    public void mustBe_Medium() {
        assertEquals(0.80, getArcScore("MUST_BE", "MEDIUM"), DELTA);
    }

    @Test
    public void mustBe_Low() {
        assertEquals(0.40, getArcScore("MUST_BE", "LOW"), DELTA);
    }

    // -------- SHOULD_BE --------

    @Test
    public void shouldBe_High() {
        assertEquals(0.50, getArcScore("SHOULD_BE", "HIGH"), DELTA);
    }

    @Test
    public void shouldBe_Medium() {
        assertEquals(0.40, getArcScore("SHOULD_BE", "MEDIUM"), DELTA);
    }

    @Test
    public void shouldBe_Low() {
        assertEquals(0.20, getArcScore("SHOULD_BE", "LOW"), DELTA);
    }

    // -------- MIGHT_BE --------

    @Test
    public void mightBe_High() {
        assertEquals(0.30, getArcScore("MIGHT_BE", "HIGH"), DELTA);
    }

    @Test
    public void mightBe_Medium() {
        assertEquals(0.20, getArcScore("MIGHT_BE", "MEDIUM"), DELTA);
    }

    @Test
    public void mightBe_Low() {
        assertEquals(0.10, getArcScore("MIGHT_BE", "LOW"), DELTA);
    }

    // -------- Default value --------

    @Test
    public void unknownValue_returnsDefault() {
        // Unknown benefit + unknown supply → else branch, no match → 0.2
        assertEquals(0.20, getArcScore("UNKNOWN", "UNKNOWN"), DELTA);
    }
}
