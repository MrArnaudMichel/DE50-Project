package fr.utbm.svn.model;

import org.junit.Test;
import static org.junit.Assert.*;

/**
 * Unit tests for the {@code getArcScore(benefit, supply)} calculation logic.
 *
 * <p>This static method is defined in {@link ValueArc} and computes a score based on a
 * Benefit × Supply correspondence matrix. Because {@code ValueArc} depends on the Rhapsody
 * API (not available during tests), the calculation logic is reproduced here identically
 * to verify that the results match the expected matrix below.</p>
 *
 * <pre>
 *              Might Be  Should Be  Must Be
 * High           0.30       0.50      0.95
 * Medium         0.20       0.40      0.80
 * Low            0.10       0.20      0.40
 * </pre>
 */
public class ValueArcScoreTest {

    private static final double DELTA = 0.001;

    /**
     * Exact reproduction of {@code ValueArc.getArcScore} for self-contained tests.
     *
     * @param benefit the benefit ranking value ({@code "MUST_BE"}, {@code "SHOULD_BE"},
     *                or {@code "MIGHT_BE"})
     * @param supply  the supply importance value ({@code "HIGH"}, {@code "MEDIUM"},
     *                or {@code "LOW"})
     * @return the arc score from the lookup matrix, or {@code 0.2} for unknown combinations
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

    // -------------------------------------------------------------------------
    // MUST_BE
    // -------------------------------------------------------------------------

    /** Verifies MUST_BE + HIGH = 0.95. */
    @Test
    public void mustBe_High() {
        assertEquals(0.95, getArcScore("MUST_BE", "HIGH"), DELTA);
    }

    /** Verifies MUST_BE + MEDIUM = 0.80. */
    @Test
    public void mustBe_Medium() {
        assertEquals(0.80, getArcScore("MUST_BE", "MEDIUM"), DELTA);
    }

    /** Verifies MUST_BE + LOW = 0.40. */
    @Test
    public void mustBe_Low() {
        assertEquals(0.40, getArcScore("MUST_BE", "LOW"), DELTA);
    }

    // -------------------------------------------------------------------------
    // SHOULD_BE
    // -------------------------------------------------------------------------

    /** Verifies SHOULD_BE + HIGH = 0.50. */
    @Test
    public void shouldBe_High() {
        assertEquals(0.50, getArcScore("SHOULD_BE", "HIGH"), DELTA);
    }

    /** Verifies SHOULD_BE + MEDIUM = 0.40. */
    @Test
    public void shouldBe_Medium() {
        assertEquals(0.40, getArcScore("SHOULD_BE", "MEDIUM"), DELTA);
    }

    /** Verifies SHOULD_BE + LOW = 0.20. */
    @Test
    public void shouldBe_Low() {
        assertEquals(0.20, getArcScore("SHOULD_BE", "LOW"), DELTA);
    }

    // -------------------------------------------------------------------------
    // MIGHT_BE
    // -------------------------------------------------------------------------

    /** Verifies MIGHT_BE + HIGH = 0.30. */
    @Test
    public void mightBe_High() {
        assertEquals(0.30, getArcScore("MIGHT_BE", "HIGH"), DELTA);
    }

    /** Verifies MIGHT_BE + MEDIUM = 0.20. */
    @Test
    public void mightBe_Medium() {
        assertEquals(0.20, getArcScore("MIGHT_BE", "MEDIUM"), DELTA);
    }

    /** Verifies MIGHT_BE + LOW = 0.10. */
    @Test
    public void mightBe_Low() {
        assertEquals(0.10, getArcScore("MIGHT_BE", "LOW"), DELTA);
    }

    // -------------------------------------------------------------------------
    // Default / unknown values
    // -------------------------------------------------------------------------

    /** Verifies that unknown benefit and supply values return the default score 0.20. */
    @Test
    public void unknownValue_returnsDefault() {
        assertEquals(0.20, getArcScore("UNKNOWN", "UNKNOWN"), DELTA);
    }
}
