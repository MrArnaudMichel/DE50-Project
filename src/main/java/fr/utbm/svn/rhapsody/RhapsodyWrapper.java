package fr.utbm.svn.rhapsody;

import com.telelogic.rhapsody.core.*;
import fr.utbm.svn.Logger;

public class RhapsodyWrapper {


    public static boolean hasStereotype(IRPModelElement el, String stereotypeName) {
        if (el == null) return false;
        IRPCollection stereotypes = el.getStereotypes();
        for (int i = 1; i <= stereotypes.getCount(); i++) {
            IRPModelElement st = (IRPModelElement) stereotypes.getItem(i);
            if (st.getName().equals(stereotypeName)) return true;
        }
        return false;
    }

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
