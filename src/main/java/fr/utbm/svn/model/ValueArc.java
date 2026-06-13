package fr.utbm.svn.model;

import com.telelogic.rhapsody.core.*;
import fr.utbm.svn.constants.SVNConstants;

import java.util.List;

import static fr.utbm.svn.rhapsody.RhapsodyWrapper.initTagIfAbsent;

/**
 * Wraps a Rhapsody {@link IRPDependency} that carries the {@code valuearc} stereotype.
 *
 * <p>A value arc connects two elements (typically a stakeholder and the system, or vice
 * versa) and carries two enumerated tags — {@code benefitRanking} and
 * {@code supplyImportance} — whose combination determines a numeric arc score according
 * to the matrix below:</p>
 *
 * <pre>
 *              Might Be  Should Be  Must Be
 * High           0.30       0.50      0.95
 * Medium         0.20       0.40      0.80
 * Low            0.10       0.20      0.40
 * </pre>
 */
public class ValueArc {

    private final IRPDependency dependency;

    /**
     * Constructs a new value arc wrapper and ensures the required tags are present
     * on the underlying dependency.
     *
     * @param dependency the Rhapsody dependency with the {@code valuearc} stereotype
     */
    public ValueArc(IRPDependency dependency) {
        this.dependency = dependency;
        this.initDefaultTags();
    }

    /**
     * Computes the numeric score for a given combination of benefit ranking and
     * supply importance using the predefined lookup matrix.
     *
     * <p>Unknown combinations fall back to {@code 0.2} (the MIGHT_BE / MEDIUM cell).</p>
     *
     * @param benefit the benefit ranking value ({@code "MUST_BE"}, {@code "SHOULD_BE"},
     *                or {@code "MIGHT_BE"})
     * @param supply  the supply importance value ({@code "HIGH"}, {@code "MEDIUM"},
     *                or {@code "LOW"})
     * @return the arc score in the range [0.10, 0.95]
     */
    public static double getArcScore(String benefit, String supply) {
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

    /**
     * Returns the underlying Rhapsody dependency.
     *
     * @return the wrapped {@link IRPDependency}
     */
    public IRPDependency getDependency() {
        return dependency;
    }

    /**
     * Computes and returns the score of this arc by reading its current tag values.
     *
     * @return arc score derived from {@code benefitRanking} and {@code supplyImportance} tags
     */
    public double getScore() {
        String benefit = this.getTagValue(SVNConstants.TAG_BENEFIT_RANKING, "MIGHT_BE");
        String supply  = this.getTagValue(SVNConstants.TAG_SUPPLY_IMPORTANCE, "LOW");
        return getArcScore(benefit, supply);
    }

    /**
     * Ensures that the {@code benefitRanking} and {@code supplyImportance} tags exist
     * on the underlying dependency, creating them with their default values if absent.
     */
    public void initDefaultTags() {
        initTagIfAbsent(dependency, SVNConstants.TAG_BENEFIT_RANKING, SVNConstants.LITERALS_BENEFIT[0]); // MIGHT_BE
        initTagIfAbsent(dependency, SVNConstants.TAG_SUPPLY_IMPORTANCE, SVNConstants.LITERALS_SUPPLY[0]);  // LOW
    }

    /**
     * Returns {@code true} if the given model element is a value arc, i.e. it is an
     * {@link IRPDependency} with the {@code valuearc} stereotype.
     *
     * @param element the model element to test
     * @return {@code true} if {@code element} is a value arc, {@code false} otherwise
     */
    public static boolean isValueArc(IRPModelElement element) {
        List stereotypes = element.getStereotypes().toList();
        if (stereotypes.isEmpty()) return false;
        RPStereotype stereo = (RPStereotype) stereotypes.get(0);
        boolean hasStereotype = stereo.getName().equals(SVNConstants.STEREOTYPE_VALUE_ARC);
        return element instanceof IRPDependency && hasStereotype;
    }

    /**
     * Reads the value of a tag from the underlying dependency, returning a default
     * value when the tag is absent or its value is empty.
     *
     * @param tagName    the name of the tag to read
     * @param defaultVal the value to return if the tag is missing or empty
     * @return the tag value, or {@code defaultVal}
     */
    public String getTagValue(String tagName, String defaultVal) {
        try {
            IRPTag tag = dependency.getTag(tagName);
            if (tag == null) return defaultVal;
            String val = tag.getValue();
            return (val == null || val.isEmpty()) ? defaultVal : val;
        } catch (Exception e) { return defaultVal; }
    }

    // -------------------------------------------------------------------------
    // Delegated methods from IRPDependency
    // -------------------------------------------------------------------------

    /**
     * Returns the name of this arc as defined in the Rhapsody model.
     *
     * @return arc name
     */
    public String getName()     { return dependency.getName(); }

    /**
     * Returns the globally unique identifier of this arc's dependency.
     *
     * @return GUID string
     */
    public String getGUID()     { return dependency.getGUID(); }

    /**
     * Returns the dependent (target) end of the dependency.
     *
     * @return the {@link IRPModelElement} at the dependent end
     */
    public IRPModelElement getDependent()  { return dependency.getDependent(); }

    /**
     * Returns the depended-on (source) end of the dependency.
     *
     * @return the {@link IRPModelElement} at the depended-on end
     */
    public IRPModelElement getDependsOn()  { return dependency.getDependsOn(); }

    /**
     * Sets the display name shown on this arc in Rhapsody diagrams.
     *
     * @param displayName the new display name
     */
    public void setDisplayName(String displayName) {
        dependency.setDisplayName(displayName);
    }

    /**
     * Controls whether the display name is shown on this arc in diagrams.
     *
     * @param isShowDisplayName {@code 1} to show the display name, {@code 0} to hide it
     */
    public void setIsShowDisplayName(int isShowDisplayName) {
        dependency.setIsShowDisplayName(isShowDisplayName);
    }
}
