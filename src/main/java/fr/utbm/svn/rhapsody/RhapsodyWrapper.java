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

/**
 * Utility class providing thin, null-safe wrappers around common Rhapsody API operations.
 *
 * <p>All methods are stateless and {@code static}; this class is not meant to be
 * instantiated. It centralises stereotype and tag management, and provides finders for
 * the domain elements ({@link Stakeholder}, {@link ValueArc}, {@link SVNSystem}) within
 * a given diagram.</p>
 */
public class RhapsodyWrapper {

    /**
     * Returns {@code true} if the given model element has a stereotype with the specified name.
     *
     * @param el             the model element to check; returns {@code false} if {@code null}
     * @param stereotypeName the stereotype name to look for
     * @return {@code true} if {@code el} has the stereotype, {@code false} otherwise
     */
    public static boolean hasStereotype(IRPModelElement el, String stereotypeName) {
        if (el == null) return false;
        IRPCollection stereotypes = el.getStereotypes();
        for (int i = 1; i <= stereotypes.getCount(); i++) {
            IRPModelElement st = (IRPModelElement) stereotypes.getItem(i);
            if (st.getName().equals(stereotypeName)) return true;
        }
        return false;
    }

    /**
     * Adds a stereotype to a model element if it is not already present.
     *
     * @param el             the element to stereotype
     * @param stereotypeName the name of the stereotype to add
     */
    public static void addStereotype(IRPModelElement el, String stereotypeName) {
        try {
            if (!hasStereotype(el, stereotypeName)) {
                el.addStereotype(stereotypeName, el.getMetaClass());
            }
        } catch (Exception e) {
            Logger.getInstance().error("addStereotype " + stereotypeName + " : " + e.getMessage());
        }
    }

    /**
     * Removes a stereotype from a model element if it is present.
     *
     * @param el             the element from which the stereotype should be removed
     * @param stereotypeName the name of the stereotype to remove
     */
    public static void removeStereotype(IRPModelElement el, String stereotypeName) {
        try {
            IRPCollection stereotypes = el.getStereotypes();
            for (int i = 1; i <= stereotypes.getCount(); i++) {
                IRPModelElement st = (IRPModelElement) stereotypes.getItem(i);
                if (st.getName().equals(stereotypeName)) {
                    el.removeStereotype((IRPStereotype) st);
                    break;
                }
            }
        } catch (Exception e) {
            Logger.getInstance().error("removeStereotype " + stereotypeName + " : " + e.getMessage());
        }
    }

    /**
     * Creates a tag on an element only if that tag does not already exist, setting an
     * initial value.
     *
     * @param el           the element on which the tag should be initialised
     * @param tagName      the name of the tag
     * @param defaultValue the value to assign to the newly created tag
     */
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

    /**
     * Creates a tag on an element only if that tag does not already exist, without
     * setting an initial value.
     *
     * @param el      the element on which the tag should be initialised
     * @param tagName the name of the tag
     */
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

    /**
     * Sets a tag value on a model element, creating a local instance if the tag does not
     * exist or is inherited from a stereotype profile (and therefore read-only).
     *
     * <p>A tag is considered inherited when its owner GUID differs from the element's
     * own GUID. In that case a new local tag with the same name is created to shadow
     * the inherited one.</p>
     *
     * @param el      the element on which to set the tag
     * @param tagName the name of the tag
     * @param value   the value to assign
     */
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

    /**
     * Scans a diagram and returns all elements that are actors with the
     * {@code stakeholder} stereotype.
     *
     * @param diagram the SVN diagram to scan
     * @return list of {@link Stakeholder} wrappers found in the diagram; never {@code null}
     */
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

    /**
     * Scans a diagram and returns all elements that are dependencies with the
     * {@code valuearc} stereotype.
     *
     * @param diagram the SVN diagram to scan
     * @return list of {@link ValueArc} wrappers found in the diagram; never {@code null}
     */
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

    /**
     * Scans a diagram and returns the first element that is a class with the
     * {@code system} stereotype.
     *
     * @param diagram the SVN diagram to scan
     * @return the {@link SVNSystem} wrapper for the system element, or {@code null} if not found
     */
    public static SVNSystem findSystem(IRPDiagram diagram) {
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
