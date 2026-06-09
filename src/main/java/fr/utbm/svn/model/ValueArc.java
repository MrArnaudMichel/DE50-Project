package fr.utbm.svn.model;

import com.telelogic.rhapsody.core.*;
import fr.utbm.RhapsodySVN.constants.SVNConstants;
import fr.utbm.RhapsodySVN.rhapsody.RhapsodyWrapper;


public class ValueArc implements IRPDependency {
    final String target;
    final double score;

    public ValueArc(String t, double s) { target = t; score = s; }

    public double getScore() {
        return score;
    }

    public String getTarget() {
        return target;
    }

    public void initDefaultTags() {
        initTagIfAbsent(SVNConstants.TAG_BENEFIT_RANKING,
                SVNConstants.LITERALS_BENEFIT[0]); // MIGHT_BE
        initTagIfAbsent(SVNConstants.TAG_SUPPLY_IMPORTANCE,
                SVNConstants.LITERALS_SUPPLY[0]);  // LOW
    }

    public void initTagIfAbsent(String tagName, String defaultValue) {
        try {
            IRPTag tag = this.getTag(tagName);
            if (tag == null || tag.getValue() == null || tag.getValue().isEmpty()) {
                if (tag == null) {
                    tag = (IRPTag) this.addNewAggr("Tag", tagName);
                }
                if (tag != null) tag.setValue(defaultValue);
            }
        } catch (Exception e) {
            System.err.println("[SVN] initTagIfAbsent " + tagName + " : " + e.getMessage());
        }
    }

    public static boolean isValueArc(IRPModelElement element) {
        return element instanceof IRPDependency
                && RhapsodyWrapper.hasStereotype(element, SVNConstants.STEREOTYPE_VALUE_ARC);
    }

    public String getTagValue(String tagName, String defaultVal) {
        try {
            IRPTag tag = this.getTag(tagName);
            if (tag == null) return defaultVal;
            String val = tag.getValue();
            return (val == null || val.isEmpty()) ? defaultVal : val;
        } catch (Exception e) { return defaultVal; }
    }

    @Override
    public IRPModelElement getDependent() {
        return null;
    }

    @Override
    public IRPModelElement getDependsOn() {
        return null;
    }

    @Override
    public int isNeedToMigrate() {
        return 0;
    }

    @Override
    public void setDependent(IRPModelElement irpModelElement) {

    }

    @Override
    public void setDependsOn(IRPModelElement irpModelElement) {

    }

    @Override
    public void setLinkType(String s) {

    }

    @Override
    public void setOwnerWithoutChangingDependent(IRPModelElement irpModelElement) {

    }

    @Override
    public IRPAssociationClass addAssociation(IRPRelation irpRelation, IRPRelation irpRelation1, String s) {
        return null;
    }

    @Override
    public IRPDependency addDependency(String s, String s1) {
        return null;
    }

    @Override
    public IRPDependency addDependencyBetween(IRPModelElement irpModelElement, IRPModelElement irpModelElement1) {
        return null;
    }

    @Override
    public IRPDependency addDependencyTo(IRPModelElement irpModelElement) {
        return null;
    }

    @Override
    public IRPLink addLinkToElement(IRPModelElement irpModelElement, IRPRelation irpRelation, IRPModelElement irpModelElement1, IRPModelElement irpModelElement2) {
        return null;
    }

    @Override
    public IRPModelElement addNewAggr(String s, String s1) {
        return null;
    }

    @Override
    public void addProperty(String s, String s1, String s2) {

    }

    @Override
    public void addRedefines(IRPModelElement irpModelElement) {

    }

    @Override
    public IRPDependency addRemoteDependencyTo(IRPModelElement irpModelElement, String s) {
        return null;
    }

    @Override
    public void addSpecificStereotype(IRPStereotype irpStereotype) {

    }

    @Override
    public IRPStereotype addStereotype(String s, String s1) {
        return null;
    }

    @Override
    public void becomeTemplateInstantiationOf(IRPModelElement irpModelElement) {

    }

    @Override
    public IRPModelElement changeTo(String s) {
        return null;
    }

    @Override
    public IRPModelElement clone(String s, IRPModelElement irpModelElement) {
        return null;
    }

    @Override
    public void createOSLCLink(String s, String s1) {

    }

    @Override
    public void deleteDependency(IRPDependency irpDependency) {

    }

    @Override
    public void deleteFromProject() {

    }

    @Override
    public void deleteOSLCLink(String s, String s1) {

    }

    @Override
    public String errorMessage() {
        return "";
    }

    @Override
    public IRPModelElement findElementsByFullName(String s, String s1) {
        return null;
    }

    @Override
    public IRPModelElement findNestedElement(String s, String s1) {
        return null;
    }

    @Override
    public IRPModelElement findNestedElementRecursive(String s, String s1) {
        return null;
    }

    @Override
    public IRPCollection getAllTags() {
        return null;
    }

    @Override
    public IRPCollection getAnnotations() {
        return null;
    }

    @Override
    public IRPCollection getAssociationClasses() {
        return null;
    }

    @Override
    public byte[] getBinaryID() {
        return new byte[0];
    }

    @Override
    public IRPCollection getConstraints() {
        return null;
    }

    @Override
    public IRPCollection getConstraintsByHim() {
        return null;
    }

    @Override
    public IRPCollection getControlledFiles() {
        return null;
    }

    @Override
    public String getDecorationStyle() {
        return "";
    }

    @Override
    public IRPCollection getDependencies() {
        return null;
    }

    @Override
    public String getDescription() {
        return "";
    }

    @Override
    public String getDescriptionHTML() {
        return "";
    }

    @Override
    public String getDescriptionPlainText() {
        return "";
    }

    @Override
    public String getDescriptionRTF() {
        return "";
    }

    @Override
    public String getDisplayName() {
        return "";
    }

    @Override
    public String getDisplayNameRTF() {
        return "";
    }

    @Override
    public String getErrorMessage() {
        return "";
    }

    @Override
    public String getFullPathName() {
        return "";
    }

    @Override
    public String getFullPathNameIn() {
        return "";
    }

    @Override
    public String getGUID() {
        return "";
    }

    @Override
    public IRPCollection getHyperLinks() {
        return null;
    }

    @Override
    public String getIconFileName() {
        return "";
    }

    @Override
    public String getInterfaceName() {
        return "";
    }

    @Override
    public int getIsExternal() {
        return 0;
    }

    @Override
    public int getIsOfMetaClass(String s) {
        return 0;
    }

    @Override
    public int getIsShowDisplayName() {
        return 0;
    }

    @Override
    public int getIsUnresolved() {
        return 0;
    }

    @Override
    public IRPCollection getLocalTags() {
        return null;
    }

    @Override
    public IRPDiagram getMainDiagram() {
        return null;
    }

    @Override
    public String getMetaClass() {
        return "";
    }

    @Override
    public String getName() {
        return "";
    }

    @Override
    public IRPCollection getNestedElements() {
        return null;
    }

    @Override
    public IRPCollection getNestedElementsByMetaClass(String s, int i) {
        return null;
    }

    @Override
    public IRPCollection getNestedElementsRecursive() {
        return null;
    }

    @Override
    public IRPStereotype getNewTermStereotype() {
        return null;
    }

    @Override
    public IRPCollection getOSLCLinks() {
        return null;
    }

    @Override
    public IRPModelElement getOfTemplate() {
        return null;
    }

    @Override
    public String getOverlayIconFileName() {
        return "";
    }

    @Override
    public IRPCollection getOverriddenProperties(int i) {
        return null;
    }

    @Override
    public IRPCollection getOverriddenPropertiesByPattern(String s, int i, int i1) {
        return null;
    }

    @Override
    public IRPCollection getOwnedDependencies() {
        return null;
    }

    @Override
    public IRPModelElement getOwner() {
        return null;
    }

    @Override
    public IRPProject getProject() {
        return null;
    }

    @Override
    public String getPropertyValue(String s) {
        return "";
    }

    @Override
    public String getPropertyValueConditional(String s, IRPCollection irpCollection, IRPCollection irpCollection1) {
        return "";
    }

    @Override
    public String getPropertyValueConditionalExplicit(String s, IRPCollection irpCollection, IRPCollection irpCollection1) {
        return "";
    }

    @Override
    public String getPropertyValueExplicit(String s) {
        return "";
    }

    @Override
    public IRPCollection getRedefines() {
        return null;
    }

    @Override
    public IRPCollection getReferences() {
        return null;
    }

    @Override
    public IRPCollection getRemoteDependencies() {
        return null;
    }

    @Override
    public String getRemoteURI() {
        return "";
    }

    @Override
    public int getRequirementTraceabilityHandle() {
        return 0;
    }

    @Override
    public String getRmmUrl() {
        return "";
    }

    @Override
    public IRPUnit getSaveUnit() {
        return null;
    }

    @Override
    public IRPStereotype getStereotype() {
        return null;
    }

    @Override
    public IRPCollection getStereotypes() {
        return null;
    }

    @Override
    public IRPTag getTag(String s) {
        return null;
    }

    @Override
    public IRPCollection getTemplateParameters() {
        return null;
    }

    @Override
    public IRPTemplateInstantiation getTi() {
        return null;
    }

    @Override
    public String getToolTipHTML() {
        return "";
    }

    @Override
    public String getUserDefinedMetaClass() {
        return "";
    }

    @Override
    public int hasNestedElements() {
        return 0;
    }

    @Override
    public int hasPanelWidget() {
        return 0;
    }

    @Override
    public void highLightElement() { }

    @Override
    public int isATemplate() {
        return 0;
    }

    @Override
    public int isDescriptionRTF() {
        return 0;
    }

    @Override
    public int isDisplayNameRTF() {
        return 0;
    }

    @Override
    public int isModified() {
        return 0;
    }

    @Override
    public int isRemote() {
        return 0;
    }

    @Override
    public int locateInBrowser() {
        return 0;
    }

    @Override
    public void lockOnDesignManager() {

    }

    @Override
    public void openFeaturesDialog(int i) {

    }

    @Override
    public void removeProperty(String s) {

    }

    @Override
    public void removeRedefines(IRPModelElement irpModelElement) {

    }

    @Override
    public void removeStereotype(IRPStereotype irpStereotype) {

    }

    @Override
    public void setDecorationStyle(String s) {

    }

    @Override
    public void setDescription(String s) {

    }

    @Override
    public void setDescriptionAndHyperlinks(String s, IRPCollection irpCollection) {

    }

    @Override
    public void setDescriptionHTML(String s) { }

    @Override
    public void setDescriptionRTF(String s) { }

    @Override
    public void setDisplayName(String s) { }

    @Override
    public void setDisplayNameRTF(String s) { }

    @Override
    public void setGUID(String s) { }

    @Override
    public void setIsShowDisplayName(int i) { }

    @Override
    public void setMainDiagram(IRPDiagram irpDiagram) { }

    @Override
    public void setName(String s) { }

    @Override
    public void setOfTemplate(IRPModelElement irpModelElement) { }

    @Override
    public void setOwner(IRPModelElement irpModelElement) { }

    @Override
    public void setPropertyValue(String s, String s1) { }

    @Override
    public void setRequirementTraceabilityHandle(int i) { }

    @Override
    public void setStereotype(IRPStereotype irpStereotype) { }
    @Override
    public IRPTag setTagContextValue(IRPTag irpTag, IRPCollection irpCollection, IRPCollection irpCollection1) { return null;}

    @Override
    public IRPTag setTagElementValue(IRPTag irpTag, IRPModelElement irpModelElement) {
        return null;
    }

    @Override
    public IRPTag setTagValue(IRPTag irpTag, String s) {
        return null;
    }

    @Override
    public void setTi(IRPTemplateInstantiation irpTemplateInstantiation) {}

    @Override
    public void synchronizeTemplateInstantiation() {}

    @Override
    public void unlockOnDesignManager() {}

}