package fr.utbm.svn.constants;

import com.telelogic.rhapsody.core.IRPModelElement;
import com.telelogic.rhapsody.core.IRPTag;
import fr.utbm.svn.Logger;

public class SVNConstants {
    public static final String PROFILE_NAME = "SVNProfile";
    
    // Stereotypes
    public static final String STEREOTYPE_STAKEHOLDER = "stakeholder";
    public static final String STEREOTYPE_SYSTEM = "system";
    public static final String STEREOTYPE_VALUE_ARC = "valuearc";
    public static final String STEREOTYPE_DIAGRAM = "SVNDiagram";
    
    // Metaclasses
    public static final String METACLASS_CLASS = "Class";
    public static final String METACLASS_ASSOCIATION = "Flow";
    public static final String METACLASS_STRUCTURE_DIAGRAM = "StructureDiagram";
    public static final String METACLASS_OBJECT_MODEL_DIAGRAM = "ObjectModelDiagram";
    public static final String METACLASS_ACTOR = "Actor";
    
    // Tags
    public static final String TAG_IMPORTANCE_SCORE = "importanceScore";
    public static final String TAG_BENEFIT_RANKING = "benefitRanking";
    public static final String TAG_SUPPLY_IMPORTANCE = "supplyImportance";
    public static final String TAG_TOTAL_LOOP_SCORE = "totalLoopScore";
    
    // Types
    public static final String TYPE_BENEFIT_RANKING = "BenefitRanking";
    public static final String TYPE_SUPPLY_IMPORTANCE = "SupplyImportance";
    
    // Literals
    public static final String[] LITERALS_BENEFIT = {"MIGHT_BE", "SHOULD_BE", "MUST_BE"};
    public static final String[] LITERALS_SUPPLY = {"LOW", "MEDIUM", "HIGH"};


    // STATIC METHODS TO SET OR CREATE TAGS

    public static void initTagIfAbsent(IRPModelElement el, String tagName, String defaultValue) {
        try {
            IRPTag tag = el.getTag(tagName);
            if (tag == null) {
                tag = (IRPTag) el.addNewAggr("Tag", tagName);
                tag.setValue(defaultValue);

            }
        } catch (Exception e) {
            Logger logger = Logger.getInstance();
            logger.error("initTagIfAbsent " + tagName + " : " + e.getMessage());
        }
    }

    public static void initTagIfAbsent(IRPModelElement el, String tagName) {
        try {
            IRPTag tag = el.getTag(tagName);
            if (tag == null) {
                el.addNewAggr("Tag", tagName);
            }
        } catch (Exception e) {
            Logger logger = Logger.getInstance();
            logger.error("initTagIfAbsent " + tagName + " : " + e.getMessage());
        }
    }

    public static void setOrCreateTag(IRPModelElement el, String tagName, String value) {
        try {
            IRPTag tag = el.getTag(tagName);
            if (tag == null) {
                tag = (IRPTag) el.addNewAggr("Tag", tagName);
            }
            if (tag != null) tag.setValue(value);
        } catch (Exception e) {
            Logger logger = Logger.getInstance();
            logger.error("setOrCreateTag " + tagName + " : " + e.getMessage());
        }
    }
}
