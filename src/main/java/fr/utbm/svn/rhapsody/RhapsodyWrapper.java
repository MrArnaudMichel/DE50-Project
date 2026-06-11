package fr.utbm.RhapsodySVN.rhapsody;

import com.telelogic.rhapsody.core.*;

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

    public static String getTagValue(IRPDependency arc, String tagName, String defaultVal) {
        try {
            IRPTag tag = arc.getTag(tagName);
            if (tag == null) return defaultVal;
            String val = tag.getValue();
            return (val == null || val.isEmpty()) ? defaultVal : val;
        } catch (Exception e) { return defaultVal; }
    }

}
