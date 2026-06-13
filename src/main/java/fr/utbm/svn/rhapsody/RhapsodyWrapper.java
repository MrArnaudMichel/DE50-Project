package fr.utbm.svn.rhapsody;

import com.telelogic.rhapsody.core.*;
import fr.utbm.svn.Logger;
import fr.utbm.svn.constants.SVNConstants;
import fr.utbm.svn.model.SVNSystem;
import fr.utbm.svn.model.Stakeholder;
import fr.utbm.svn.model.ValueArc;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

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
            // Inherited from a stereotype (e.g. a read-only referenced profile)?
            // Its owner won't be this element — create a local override so it's writable.
            boolean inherited = (tag != null)
                    && !el.getGUID().equals(tag.getOwner().getGUID());
            if (tag == null || inherited) {
                tag = (IRPTag) el.addNewAggr("Tag", tagName);   // local override on the instance
            }
            if (tag != null) tag.setValue(value);
        } catch (Exception e) {
            Logger.getInstance().error("setOrCreateTag " + tagName + " : " + e.getMessage());
        }
    }

    public static List<Stakeholder> findStakeholders(IRPDiagram diagram) {
        final Logger logger = Logger.getInstance();
        List<Stakeholder> result = new ArrayList<>();
        IRPCollection descendants = diagram.getElementsInDiagram();
        for (int i = 1; i <= descendants.getCount(); i++) {
            IRPModelElement el = (IRPModelElement) descendants.getItem(i);
            if (el instanceof IRPActor
                    && RhapsodyWrapper.hasStereotype(el, SVNConstants.STEREOTYPE_STAKEHOLDER)) {
                result.add(new Stakeholder((IRPActor) el));
                logger.log("Stakeholder found : " + el.getName());
            }
        }
        return result;
    }

    public static List<ValueArc> findValueArcs(IRPDiagram diagram) {
        final Logger logger = Logger.getInstance();
        List<ValueArc> result = new ArrayList<>();
        IRPCollection descendants = diagram.getElementsInDiagram();
        for (int i = 1; i <= descendants.getCount(); i++) {
            IRPModelElement el = (IRPModelElement) descendants.getItem(i);
            if (el instanceof IRPDependency
                    && RhapsodyWrapper.hasStereotype(el, SVNConstants.STEREOTYPE_VALUE_ARC)) {
                result.add(new ValueArc((IRPDependency) el));
                logger.log("ValueArc found : " + el.getName());
            }
        }
        return result;
    }

    public static SVNSystem findSystem(IRPDiagram diagram) {;
        IRPCollection descendants = diagram.getElementsInDiagram();
        for (int i = 1; i <= Objects.requireNonNull(descendants).getCount(); i++) {
            IRPModelElement el = (IRPModelElement) descendants.getItem(i);
            if (el instanceof IRPClass
                    && RhapsodyWrapper.hasStereotype(el, SVNConstants.STEREOTYPE_SYSTEM)) {
                return new SVNSystem((IRPClass) el);
            }
        }
        return null;
    }
}