package fr.utbm.svn.constants;

/**
 * Central repository of string constants used throughout the SVN plugin.
 *
 * <p>Groups stereotype names, tag names, and enumeration literals that are shared
 * between Rhapsody model elements and the plugin's business logic. Keeping them in
 * one place avoids magic strings and makes renaming easier.</p>
 */
public class SVNConstants {

    // -------------------------------------------------------------------------
    // Stereotypes
    // -------------------------------------------------------------------------

    /** Stereotype applied to Rhapsody actors that represent stakeholders. */
    public static final String STEREOTYPE_STAKEHOLDER = "stakeholder";

    /** Stereotype applied to Rhapsody classes that represent the system under analysis. */
    public static final String STEREOTYPE_SYSTEM = "system";

    /** Stereotype applied to Rhapsody dependencies that represent value arcs. */
    public static final String STEREOTYPE_VALUE_ARC = "valuearc";

    /** Stereotype applied to Rhapsody diagrams that contain an SVN model. */
    public static final String STEREOTYPE_DIAGRAM = "SVNDiagram";

    // -------------------------------------------------------------------------
    // Tags
    // -------------------------------------------------------------------------

    /** Tag name storing the computed importance score on a stakeholder actor. */
    public static final String TAG_IMPORTANCE_SCORE = "importanceScore";

    /** Tag name storing the benefit ranking on a value arc (e.g. {@code MUST_BE}). */
    public static final String TAG_BENEFIT_RANKING = "benefitRanking";

    /** Tag name storing the supply importance on a value arc (e.g. {@code HIGH}). */
    public static final String TAG_SUPPLY_IMPORTANCE = "supplyImportance";

    /** Tag name storing the aggregated loop score on the system element. */
    public static final String TAG_TOTAL_LOOP_SCORE = "totalLoopScore";

    // -------------------------------------------------------------------------
    // Enumeration literals
    // -------------------------------------------------------------------------

    /**
     * Ordered literals for the benefit ranking tag.
     * Index 0 ({@code "MIGHT_BE"}) is the default value used when creating a new arc.
     */
    public static final String[] LITERALS_BENEFIT = {"MIGHT_BE", "SHOULD_BE", "MUST_BE"};

    /**
     * Ordered literals for the supply importance tag.
     * Index 0 ({@code "LOW"}) is the default value used when creating a new arc.
     */
    public static final String[] LITERALS_SUPPLY = {"LOW", "MEDIUM", "HIGH"};
}
