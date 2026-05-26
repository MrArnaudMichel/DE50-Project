package fr.utbm.RhapsodySVN.constants;

public class SVNConstants {
    public static final String PROFILE_NAME = "SVNProfile";
    
    // Stereotypes
    public static final String STEREOTYPE_STAKEHOLDER = "stakeholder";
    public static final String STEREOTYPE_SYSTEM = "svnSystem";
    public static final String STEREOTYPE_VALUE_ARC = "valuearc";
    public static final String STEREOTYPE_DIAGRAM = "SVNDiagram";
    
    // Metaclasses
    public static final String METACLASS_CLASS = "Class";
    public static final String METACLASS_ASSOCIATION = "Association";
    public static final String METACLASS_STRUCTURE_DIAGRAM = "StructureDiagram";
    public static final String METACLASS_OBJECT_MODEL_DIAGRAM = "ObjectModelDiagram";
    public static final String METACLASS_ACTOR = "Actor";
    
    // Tags
    public static final String TAG_IMPORTANCE_SCORE = "importanceScore";
    public static final String TAG_BENEFIT_RANKING = "benefitRanking";
    public static final String TAG_SUPPLY_IMPORTANCE = "supplyImportance";
    
    // Types
    public static final String TYPE_BENEFIT_RANKING = "BenefitRanking";
    public static final String TYPE_SUPPLY_IMPORTANCE = "SupplyImportance";
    
    // Literals
    public static final String[] LITERALS_BENEFIT = {"MIGHT_BE", "SHOULD_BE", "MUST_BE"};
    public static final String[] LITERALS_SUPPLY = {"LOW", "MEDIUM", "HIGH"};
}
