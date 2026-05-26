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
        System.out.println("[SVN] Configuration du profil : " + PROFILE_NAME + (cleanFirst ? " (Mode Nettoyage)" : ""));
        
        if (cleanFirst) {
            deleteProfile();
        }

        IRPProfile profile = findOrCreateProfile();
        if (profile == null) {
            System.err.println("[SVN] Impossible de créer ou trouver le profil.");
            return;
        }

        IRPType benefitType = RhapsodyWrapper.getOrCreateEnumType(profile, TYPE_BENEFIT_RANKING, LITERALS_BENEFIT);
        IRPType supplyType = RhapsodyWrapper.getOrCreateEnumType(profile, TYPE_SUPPLY_IMPORTANCE, LITERALS_SUPPLY);
        
        if (benefitType == null || supplyType == null) {
            System.out.println("[SVN] Tentative de création des types dans un package interne 'SVNData'...");
            IRPPackage dataPkg = (IRPPackage) RhapsodyWrapper.findInCollection(profile.getPackages(), "SVNData");
            if (dataPkg == null) dataPkg = profile.addNestedPackage("SVNData");
            
            benefitType = RhapsodyWrapper.getOrCreateEnumType(dataPkg, TYPE_BENEFIT_RANKING, LITERALS_BENEFIT);
            supplyType = RhapsodyWrapper.getOrCreateEnumType(dataPkg, TYPE_SUPPLY_IMPORTANCE, LITERALS_SUPPLY);
        }

        IRPStereotype stakeholder = RhapsodyWrapper.getOrCreateStereotype(profile, STEREOTYPE_STAKEHOLDER, METACLASS_CLASS, true);
        if (stakeholder != null) {
            stakeholder.setPropertyValue("Model.Stereotype.BrowserIcon",       "stakeholder.ico");
            stakeholder.setPropertyValue("Model.Stereotype.BrowserGroupIcon",  "stakeholder_group.ico");
            stakeholder.setPropertyValue("Model.Stereotype.DrawingToolIcon",   "stakeholder.ico");
            stakeholder.setPropertyValue("Model.Stereotype.PluralName",        "Stakeholders");
            RhapsodyWrapper.getOrCreateTag(stakeholder, TAG_IMPORTANCE_SCORE, null);
        }

        IRPStereotype svnSystem = RhapsodyWrapper.getOrCreateStereotype(profile, STEREOTYPE_SYSTEM, METACLASS_CLASS, true);
        if (svnSystem != null) {
            svnSystem.setPropertyValue("Model.Stereotype.BrowserIcon", "SVNSystem.ico");
            svnSystem.setPropertyValue("Model.Stereotype.BrowserGroupIcon", "SVNSystem_group.ico");
            svnSystem.setPropertyValue("Model.Stereotype.DrawingToolIcon", "SVNSystem.ico");
            svnSystem.setPropertyValue("Model.Stereotype.PluralName", "SVNSystems");
        }

        IRPStereotype valueArc = RhapsodyWrapper.getOrCreateStereotype(profile, STEREOTYPE_VALUE_ARC, METACLASS_ASSOCIATION, true);
        if (valueArc != null) {
            valueArc.setPropertyValue("Model.Stereotype.BrowserIcon", "valueArc.ico");
            valueArc.setPropertyValue("Model.Stereotype.BrowserGroupIcon", "valueArc_group.ico");
            valueArc.setPropertyValue("Model.Stereotype.DrawingToolIcon", "valueArc.ico");
            valueArc.setPropertyValue("Model.Stereotype.PluralName", "ValueArcs");
            RhapsodyWrapper.getOrCreateTag(valueArc, TAG_BENEFIT_RANKING, benefitType);
            RhapsodyWrapper.getOrCreateTag(valueArc, TAG_SUPPLY_IMPORTANCE, supplyType);
        }

        IRPStereotype svnDiagram = RhapsodyWrapper.getOrCreateStereotype(
                profile, STEREOTYPE_DIAGRAM, METACLASS_OBJECT_MODEL_DIAGRAM, true);
        if (svnDiagram != null) {
            svnDiagram.setPropertyValue(
                    "Model.Stereotype.DrawingToolbar",
                    "stakeholder,svnSystem,valueArc"
            );
        }

        project.save();
        System.out.println("[SVN] Profil '" + PROFILE_NAME + "' mis à jour avec succès.");
    }

    public void deleteProfile() {
        IRPProfile profile = (IRPProfile) RhapsodyWrapper.findInCollection(project.getProfiles(), PROFILE_NAME);
        if (profile != null) {
            System.out.println("[SVN] Suppression du profil existant...");
            RhapsodyWrapper.deleteElement(profile);
            project.save();
        }
    }

    private IRPProfile findOrCreateProfile() {
        IRPProfile profile = (IRPProfile) RhapsodyWrapper.findInCollection(project.getProfiles(), PROFILE_NAME);
        if (profile == null) {
            try {
                profile = project.addProfile(PROFILE_NAME);
                project.save(); // Sauvegarde immédiate pour stabiliser l'objet
            } catch (Exception e) {
                System.err.println("[SVN] Erreur lors de la création du profil : " + e.getMessage());
            }
        }
        return profile;
    }
}
