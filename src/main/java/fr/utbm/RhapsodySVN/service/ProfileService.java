package fr.utbm.RhapsodySVN.service;

import com.telelogic.rhapsody.core.*;
import fr.utbm.RhapsodySVN.rhapsody.RhapsodyWrapper;

import static fr.utbm.RhapsodySVN.constants.SVNConstants.*;

public class ProfileService {

    private final IRPProject project;

    public ProfileService(IRPProject project) {
        this.project = project;
    }

    public void configureProfile(boolean cleanFirst) {
        System.out.println("[SVN] Configuration du profil : " + PROFILE_NAME
                + (cleanFirst ? " (Mode Nettoyage)" : ""));

        if (cleanFirst) {
            cleanDefaultPackage();
            deleteProfile();
        }

        IRPProfile profile = findOrCreateProfile();
        if (profile == null) {
            System.err.println("[SVN] Impossible de créer ou trouver le profil.");
            return;
        }

        IRPType benefitType = RhapsodyWrapper.getOrCreateEnumType(profile, TYPE_BENEFIT_RANKING, LITERALS_BENEFIT);
        IRPType supplyType  = RhapsodyWrapper.getOrCreateEnumType(profile, TYPE_SUPPLY_IMPORTANCE, LITERALS_SUPPLY);

        if (benefitType == null || supplyType == null) {
            System.out.println("[SVN] Tentative dans un package interne 'SVNData'...");
            IRPPackage dataPkg = (IRPPackage) RhapsodyWrapper.findInCollection(profile.getPackages(), "SVNData");
            if (dataPkg == null) dataPkg = profile.addNestedPackage("SVNData");
            benefitType = RhapsodyWrapper.getOrCreateEnumType(dataPkg, TYPE_BENEFIT_RANKING, LITERALS_BENEFIT);
            supplyType  = RhapsodyWrapper.getOrCreateEnumType(dataPkg, TYPE_SUPPLY_IMPORTANCE, LITERALS_SUPPLY);
        }

        IRPStereotype stakeholder = RhapsodyWrapper.getOrCreateStereotype(
                profile, STEREOTYPE_STAKEHOLDER, METACLASS_ACTOR, true);
        if (stakeholder != null) {
            stakeholder.setPropertyValue("Model.Stereotype.BrowserIcon",      STEREOTYPE_STAKEHOLDER + ".ico");
            stakeholder.setPropertyValue("Model.Stereotype.BrowserGroupIcon", STEREOTYPE_STAKEHOLDER + "_group.ico");
            stakeholder.setPropertyValue("Model.Stereotype.DrawingToolIcon",  STEREOTYPE_STAKEHOLDER + ".ico");
            stakeholder.setPropertyValue("Model.Stereotype.PluralName",       "Stakeholders");
            RhapsodyWrapper.getOrCreateTag(stakeholder, TAG_IMPORTANCE_SCORE, null);
        }

        IRPStereotype svnSystem = RhapsodyWrapper.getOrCreateStereotype(
                profile, STEREOTYPE_SYSTEM, METACLASS_CLASS, true);
        if (svnSystem != null) {
            svnSystem.setPropertyValue("Model.Stereotype.BrowserIcon",      STEREOTYPE_SYSTEM + ".ico");
            svnSystem.setPropertyValue("Model.Stereotype.BrowserGroupIcon", STEREOTYPE_SYSTEM + "_group.ico");
            svnSystem.setPropertyValue("Model.Stereotype.DrawingToolIcon",  STEREOTYPE_SYSTEM + ".ico");
            svnSystem.setPropertyValue("Model.Stereotype.PluralName",       "SVNSystems");
        }

        IRPStereotype valueArc = RhapsodyWrapper.getOrCreateStereotype(
                profile, STEREOTYPE_VALUE_ARC, METACLASS_ASSOCIATION, true);
        if (valueArc != null) {
            valueArc.setPropertyValue("Model.Stereotype.BrowserIcon",      STEREOTYPE_VALUE_ARC + ".ico");
            valueArc.setPropertyValue("Model.Stereotype.BrowserGroupIcon", STEREOTYPE_VALUE_ARC + "_group.ico");
            valueArc.setPropertyValue("Model.Stereotype.DrawingToolIcon",  STEREOTYPE_VALUE_ARC + ".ico");
            valueArc.setPropertyValue("Model.Stereotype.PluralName",       "ValueArcs");
            RhapsodyWrapper.getOrCreateTag(valueArc, TAG_BENEFIT_RANKING,   benefitType);
            RhapsodyWrapper.getOrCreateTag(valueArc, TAG_SUPPLY_IMPORTANCE, supplyType);
        }

        IRPStereotype svnDiagram = RhapsodyWrapper.getOrCreateStereotype(
                profile, STEREOTYPE_DIAGRAM, METACLASS_OBJECT_MODEL_DIAGRAM, true);
        if (svnDiagram != null) {
            String toolbar = STEREOTYPE_STAKEHOLDER + "," + STEREOTYPE_SYSTEM + "," + STEREOTYPE_VALUE_ARC;
            setOrAddProperty(svnDiagram, "Model.Stereotype.DrawingToolbar", toolbar);
            System.out.println("[SVN] DrawingToolbar = ["
                    + svnDiagram.getPropertyValue("Model.Stereotype.DrawingToolbar") + "]");
        }

        project.save();
        System.out.println("[SVN] Profil '" + PROFILE_NAME + "' mis à jour avec succès.");
    }


    public void cleanDefaultPackage() {
        IRPPackage defaultPkg = findDefaultPackage();
        if (defaultPkg == null) {
            System.out.println("[SVN] Package Default non trouvé, nettoyage ignoré.");
            return;
        }
        System.out.println("[SVN] Nettoyage du package : " + defaultPkg.getName());

        // Collecte avant suppression (évite les ConcurrentModification)
        java.util.List<IRPActor>              actors   = new java.util.ArrayList<>();
        java.util.List<IRPClass>              classes  = new java.util.ArrayList<>();
        java.util.List<IRPFlow>               flows    = new java.util.ArrayList<>();
        java.util.List<IRPObjectModelDiagram> diagrams = new java.util.ArrayList<>();

        IRPCollection elements = defaultPkg.getNestedElementsRecursive();
        for (int i = 1; i <= elements.getCount(); i++) {
            IRPModelElement el = (IRPModelElement) elements.getItem(i);

            if (el instanceof IRPActor
                    && RhapsodyWrapper.hasStereotype(el, STEREOTYPE_STAKEHOLDER)) {
                actors.add((IRPActor) el);

            } else if (el instanceof IRPClass
                    && RhapsodyWrapper.hasStereotype(el, STEREOTYPE_SYSTEM)) {
                classes.add((IRPClass) el);

            } else if (el instanceof IRPFlow
                    && RhapsodyWrapper.hasStereotype(el, STEREOTYPE_VALUE_ARC)) {
                flows.add((IRPFlow) el);

            } else if (el instanceof IRPObjectModelDiagram
                    && RhapsodyWrapper.hasStereotype(el, STEREOTYPE_DIAGRAM)) {
                diagrams.add((IRPObjectModelDiagram) el);
            }
        }

        // Suppression dans le bon ordre : arcs d'abord, puis noeuds, puis diagrammes
        for (IRPFlow f : flows) {
            try {
                System.out.println("[SVN]   Suppression Flow : " + f.getName());
                defaultPkg.deleteFlows(f);
            } catch (Exception e) {
                System.err.println("[SVN]   deleteFlows échoué : " + e.getMessage());
                try { f.deleteFromProject(); } catch (Exception ignored) {}
            }
        }

        for (IRPActor a : actors) {
            try {
                System.out.println("[SVN]   Suppression Actor : " + a.getName());
                defaultPkg.deleteActor(a);
            } catch (Exception e) {
                System.err.println("[SVN]   deleteActor échoué : " + e.getMessage());
                try { a.deleteFromProject(); } catch (Exception ignored) {}
            }
        }

        for (IRPClass c : classes) {
            try {
                System.out.println("[SVN]   Suppression Class : " + c.getName());
                defaultPkg.deleteClass(c);
            } catch (Exception e) {
                System.err.println("[SVN]   deleteClass échoué : " + e.getMessage());
                try { c.deleteFromProject(); } catch (Exception ignored) {}
            }
        }

        for (IRPObjectModelDiagram d : diagrams) {
            try {
                System.out.println("[SVN]   Suppression Diagram (Default) : " + d.getName());
                defaultPkg.deleteObjectModelDiagram(d.getName());
            } catch (Exception e) {
                System.err.println("[SVN]   deleteObjectModelDiagram échoué : " + e.getMessage());
                try { d.deleteFromProject(); } catch (Exception ignored) {}
            }
        }

        int extraDiagrams = 0;
        try {
            IRPCollection allDiags = project.getNestedElementsByMetaClass("ObjectModelDiagram", 1);
            for (int i = 1; i <= allDiags.getCount(); i++) {
                IRPModelElement el = (IRPModelElement) allDiags.getItem(i);
                if (el instanceof IRPObjectModelDiagram
                        && RhapsodyWrapper.hasStereotype(el, STEREOTYPE_DIAGRAM)) {
                    try {
                        System.out.println("[SVN]   Suppression Diagram (projet) : " + el.getName());
                        el.deleteFromProject();
                        extraDiagrams++;
                    } catch (Exception e) {
                        System.err.println("[SVN]   Suppression diagram échouée : " + e.getMessage());
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("[SVN]   Recherche diagrammes projet : " + e.getMessage());
        }

        int total = actors.size() + classes.size() + flows.size() + diagrams.size() + extraDiagrams;
        System.out.println("[SVN] Nettoyage terminé : " + total + " élément(s) supprimé(s).");
    }


    private IRPPackage findDefaultPackage() {
        IRPCollection packages = project.getPackages();
        for (int i = 1; i <= packages.getCount(); i++) {
            IRPModelElement el = (IRPModelElement) packages.getItem(i);
            if ("Default".equals(el.getName()) && el instanceof IRPPackage) {
                return (IRPPackage) el;
            }
        }
        // Fallback : prend le premier package non-profil
        if (packages.getCount() > 0) {
            return (IRPPackage) packages.getItem(1);
        }
        return null;
    }

    // -------------------------------------------------------------------------
    // Helpers
    // -------------------------------------------------------------------------

    public void deleteProfile() {
        IRPProfile profile = (IRPProfile) RhapsodyWrapper.findInCollection(
                project.getProfiles(), PROFILE_NAME);
        if (profile != null) {
            System.out.println("[SVN] Suppression du profil existant...");
            RhapsodyWrapper.deleteElement(profile);
            project.save();
        }
    }

    private IRPProfile findOrCreateProfile() {
        IRPProfile profile = (IRPProfile) RhapsodyWrapper.findInCollection(
                project.getProfiles(), PROFILE_NAME);
        if (profile == null) {
            try {
                profile = project.addProfile(PROFILE_NAME);
                project.save();
            } catch (Exception e) {
                System.err.println("[SVN] Erreur création profil : " + e.getMessage());
            }
        }
        return profile;
    }

    private void setOrAddProperty(IRPModelElement element, String key, String value) {
        try {
            element.setPropertyValue(key, value);
        } catch (Exception e) {
            try {
                element.addProperty(key, "String", value);
            } catch (Exception e2) {
                System.err.println("[SVN] Impossible d'écrire " + key + " : " + e2.getMessage());
            }
        }
    }
}