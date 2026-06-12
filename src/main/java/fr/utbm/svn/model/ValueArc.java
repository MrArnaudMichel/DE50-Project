package fr.utbm.svn.model;

import com.telelogic.rhapsody.core.*;
import fr.utbm.svn.constants.SVNConstants;

import java.util.List;

import static fr.utbm.svn.rhapsody.RhapsodyWrapper.initTagIfAbsent;


public class ValueArc {

    private final IRPDependency dependency;

    public ValueArc(IRPDependency dependency) {
        this.dependency = dependency;
        this.initDefaultTags();
    }

    /**
     * Score = f(BenefitRanking, SupplyImportance)
     *
     *              Might Be  Should Be  Must Be
     * High           0.3       0.5       0.95
     * Medium         0.2       0.4       0.8
     * Low            0.1       0.2       0.4
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

    public IRPDependency getDependency() {
        return dependency;
    }

    // Methods

    public double getScore() {
        String benefit = this.getTagValue(SVNConstants.TAG_BENEFIT_RANKING, "MIGHT_BE");
        String supply  = this.getTagValue(SVNConstants.TAG_SUPPLY_IMPORTANCE, "LOW");
        return getArcScore(benefit, supply);
    }

    public void initDefaultTags() {
        initTagIfAbsent(dependency, SVNConstants.TAG_BENEFIT_RANKING, SVNConstants.LITERALS_BENEFIT[0]); // MIGHT_BE
        initTagIfAbsent(dependency, SVNConstants.TAG_SUPPLY_IMPORTANCE, SVNConstants.LITERALS_SUPPLY[0]);  // LOW
    }

    public static boolean isValueArc(IRPModelElement element) {
        List stereotypes = element.getStereotypes().toList();
        if (stereotypes.isEmpty()) return false;
        RPStereotype stereo = (RPStereotype) stereotypes.get(0);
        boolean hasStereotype = stereo.getName().equals(SVNConstants.STEREOTYPE_VALUE_ARC);
        return element instanceof IRPDependency && hasStereotype;
    }

    public String getTagValue(String tagName, String defaultVal) {
        try {
            IRPTag tag = dependency.getTag(tagName);
            if (tag == null) return defaultVal;
            String val = tag.getValue();
            return (val == null || val.isEmpty()) ? defaultVal : val;
        } catch (Exception e) { return defaultVal; }
    }

    // FROM IRPDependency

    public String getName()     { return dependency.getName(); }
    public String getGUID()     { return dependency.getGUID(); }
    public IRPModelElement getDependent()  { return dependency.getDependent(); }
    public IRPModelElement getDependsOn()  { return dependency.getDependsOn(); }
    public void setDisplayName(String displayName) {
        dependency.setDisplayName(displayName);
    }
    public void setIsShowDisplayName(int isShowDisplayName) {
        dependency.setIsShowDisplayName(isShowDisplayName);
    }
}