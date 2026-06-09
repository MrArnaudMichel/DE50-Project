package fr.utbm.svn.model;

import com.telelogic.rhapsody.core.*;

public class Stackholder implements IRPActor {
    double score;

    public Stackholder() {}

    @Override
    public IRPEventReception addEventReceptionWithEvent(String s, IRPEvent irpEvent) {
        return null;
    }

    @Override
    public int getIsBehaviorOverriden() {
        return 0;
    }

    @Override
    public void setIsBehaviorOverriden(int i) {

    }

    @Override
    public int updateContainedDiagramsOnServer(int i) {
        return 0;
    }

    @Override
    public IRPFlowchart addActivityDiagram() {
        return null;
    }

    @Override
    public IRPAttribute addAttribute(String s) {
        return null;
    }

    @Override
    public IRPFlowItem addFlowItems(String s) {
        return null;
    }

    @Override
    public IRPFlow addFlows(String s) {
        return null;
    }

    @Override
    public void addGeneralization(IRPClassifier irpClassifier) {

    }

    @Override
    public IRPOperation addOperation(String s) {
        return null;
    }

    @Override
    public IRPRelation addRelation(String s, String s1, String s2, String s3, String s4, String s5, String s6, String s7, String s8) {
        return null;
    }

    @Override
    public IRPRelation addRelationTo(IRPClassifier irpClassifier, String s, String s1, String s2, String s3, String s4, String s5, String s6) {
        return null;
    }

    @Override
    public IRPStatechart addStatechart() {
        return null;
    }

    @Override
    public IRPRelation addUnidirectionalRelation(String s, String s1, String s2, String s3, String s4, String s5) {
        return null;
    }

    @Override
    public IRPRelation addUnidirectionalRelationTo(IRPClassifier irpClassifier, String s, String s1, String s2, String s3) {
        return null;
    }

    @Override
    public void deleteAttribute(IRPAttribute irpAttribute) {

    }

    @Override
    public void deleteFlowItems(IRPFlowItem irpFlowItem) {

    }

    @Override
    public void deleteFlows(IRPFlow irpFlow) {

    }

    @Override
    public void deleteGeneralization(IRPClassifier irpClassifier) {

    }

    @Override
    public void deleteOperation(IRPOperation irpOperation) {

    }

    @Override
    public void deleteRelation(IRPRelation irpRelation) {

    }

    @Override
    public IRPAttribute findAttribute(String s) {
        return null;
    }

    @Override
    public IRPClassifier findBaseClassifier(String s) {
        return null;
    }

    @Override
    public IRPClassifier findDerivedClassifier(String s) {
        return null;
    }

    @Override
    public IRPGeneralization findGeneralization(String s) {
        return null;
    }

    @Override
    public IRPInterfaceItem findInterfaceItem(String s) {
        return null;
    }

    @Override
    public IRPClassifier findNestedClassifier(String s) {
        return null;
    }

    @Override
    public IRPModelElement findNestedClassifierRecursive(String s) {
        return null;
    }

    @Override
    public IRPRelation findRelation(String s) {
        return null;
    }

    @Override
    public IRPInterfaceItem findTrigger(String s) {
        return null;
    }

    @Override
    public IRPFlowchart getActivityDiagram() {
        return null;
    }

    @Override
    public IRPCollection getAttributes() {
        return null;
    }

    @Override
    public IRPCollection getAttributesIncludingBases() {
        return null;
    }

    @Override
    public IRPCollection getBaseClassifiers() {
        return null;
    }

    @Override
    public IRPCollection getBehavioralDiagrams() {
        return null;
    }

    @Override
    public IRPCollection getDerivedClassifiers() {
        return null;
    }

    @Override
    public IRPCollection getFlowItems() {
        return null;
    }

    @Override
    public IRPCollection getFlows() {
        return null;
    }

    @Override
    public IRPCollection getGeneralizations() {
        return null;
    }

    @Override
    public IRPCollection getInterfaceItems() {
        return null;
    }

    @Override
    public IRPCollection getInterfaceItemsIncludingBases() {
        return null;
    }

    @Override
    public IRPCollection getLinks() {
        return null;
    }

    @Override
    public IRPCollection getNestedClassifiers() {
        return null;
    }

    @Override
    public IRPCollection getOperations() {
        return null;
    }

    @Override
    public IRPCollection getPorts() {
        return null;
    }

    @Override
    public IRPCollection getRelations() {
        return null;
    }

    @Override
    public IRPCollection getRelationsIncludingBases() {
        return null;
    }

    @Override
    public IRPCollection getSequenceDiagrams() {
        return null;
    }

    @Override
    public IRPCollection getSourceArtifacts() {
        return null;
    }

    @Override
    public IRPStatechart getStatechart() {
        return null;
    }

    @Override
    public IRPUnit copyToAnotherProject(IRPModelElement irpModelElement) {
        return null;
    }

    @Override
    public int getAddToModelMode() {
        return 0;
    }

    @Override
    public String getCMHeader() {
        return "";
    }

    @Override
    public int getCMState() {
        return 0;
    }

    @Override
    public String getCurrentDirectory() {
        return "";
    }

    @Override
    public String getFilename() {
        return "";
    }

    @Override
    public int getIncludeInNextLoad() {
        return 0;
    }

    @Override
    public int getIsStub() {
        return 0;
    }

    @Override
    public String getLanguage() {
        return "";
    }

    @Override
    public String getLastModifiedTime() {
        return "";
    }

    @Override
    public IRPCollection getNestedSaveUnits() {
        return null;
    }

    @Override
    public int getNestedSaveUnitsCount() {
        return 0;
    }

    @Override
    public IRPCollection getStructureDiagrams() {
        return null;
    }

    @Override
    public int isReadOnly() {
        return 0;
    }

    @Override
    public int isReferenceUnit() {
        return 0;
    }

    @Override
    public int isSeparateSaveUnit() {
        return 0;
    }

    @Override
    public IRPUnit load(int i) {
        return null;
    }

    @Override
    public IRPUnit moveToAnotherProjectLeaveAReference(IRPModelElement irpModelElement) {
        return null;
    }

    @Override
    public IRPUnit referenceToAnotherProject(IRPModelElement irpModelElement) {
        return null;
    }

    @Override
    public void save(int i) {

    }

    @Override
    public void setCMHeader(String s) {

    }

    @Override
    public void setFilename(String s) {

    }

    @Override
    public void setIncludeInNextLoad(int i) {

    }

    @Override
    public void setLanguage(String s, int i) {

    }

    @Override
    public void setReadOnly(int i) {

    }

    @Override
    public void setSeparateSaveUnit(int i) {

    }

    @Override
    public void setUnitPath(String s) {

    }

    @Override
    public void unload() {

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
    public void highLightElement() {

    }

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
    public void setDescriptionHTML(String s) {

    }

    @Override
    public void setDescriptionRTF(String s) {

    }

    @Override
    public void setDisplayName(String s) {

    }

    @Override
    public void setDisplayNameRTF(String s) {

    }

    @Override
    public void setGUID(String s) {

    }

    @Override
    public void setIsShowDisplayName(int i) {

    }

    @Override
    public void setMainDiagram(IRPDiagram irpDiagram) {

    }

    @Override
    public void setName(String s) {

    }

    @Override
    public void setOfTemplate(IRPModelElement irpModelElement) {

    }

    @Override
    public void setOwner(IRPModelElement irpModelElement) {

    }

    @Override
    public void setPropertyValue(String s, String s1) {

    }

    @Override
    public void setRequirementTraceabilityHandle(int i) {

    }

    @Override
    public void setStereotype(IRPStereotype irpStereotype) {

    }

    @Override
    public IRPTag setTagContextValue(IRPTag irpTag, IRPCollection irpCollection, IRPCollection irpCollection1) {
        return null;
    }

    @Override
    public IRPTag setTagElementValue(IRPTag irpTag, IRPModelElement irpModelElement) {
        return null;
    }

    @Override
    public IRPTag setTagValue(IRPTag irpTag, String s) {
        return null;
    }

    @Override
    public void setTi(IRPTemplateInstantiation irpTemplateInstantiation) {

    }

    @Override
    public void synchronizeTemplateInstantiation() {

    }

    @Override
    public void unlockOnDesignManager() {

    }
}
