package fr.utbm.svn.model;

import com.telelogic.rhapsody.core.*;
import fr.utbm.svn.constants.SVNConstants;

import java.util.List;

import static fr.utbm.svn.rhapsody.RhapsodyWrapper.initTagIfAbsent;
import static fr.utbm.svn.service.strategy.ArcScoreStrategy.getArcScore;


public class ValueArc {

    private final IRPDependency dependency;

    public ValueArc(IRPDependency dependency) {
        this.dependency = dependency;
        this.initDefaultTags();
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