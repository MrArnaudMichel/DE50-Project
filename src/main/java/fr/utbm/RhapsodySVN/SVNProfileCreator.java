package fr.utbm.RhapsodySVN;

import com.telelogic.rhapsody.core.*;

/**
 * Créateur de profil pour le plugin Rhapsody SVN (Stakeholder Value Network).
 * Ce script configure le profil SVN, les stéréotypes et les types nécessaires.
 */
public class SVNProfileCreator {

    private static final String PROFILE_NAME = "SVNProfile";

    public static void main(String[] args) {
        IRPApplication app = RhapsodyAppServer.getActiveRhapsodyApplication();
        run(app);
    }

    public static void run(IRPApplication app) {
        IRPProject project = app.activeProject();
        if (project == null) {
            System.err.println("[SVN] Aucun projet actif dans Rhapsody.");
            return;
        }

        System.out.println("[SVN] Configuration du profil dans le projet : " + project.getName());

        IRPProfile profile = findOrCreateProfile(project);
        if (profile == null) {
            System.err.println("[SVN] Erreur lors de l'accès au profil.");
            return;
        }

        System.out.println("[SVN] Profil utilisé : " + profile.getName() + " (Metaclasse: " + profile.getMetaClass() + ", Chemin: " + profile.getFullPathName() + ")");

        // Sauvegarde intermédiaire pour s'assurer que le profil est bien "présent" dans le modèle
        project.save();

        // 1. Création des types énumérés pour les Tag Values
        IRPType benefitRankingType = createEnumType(profile, "BenefitRanking", 
                new String[]{"MIGHT_BE", "SHOULD_BE", "MUST_BE"});
        IRPType supplyImportanceType = createEnumType(profile, "SupplyImportance", 
                new String[]{"LOW", "MEDIUM", "HIGH"});

        // 2. Création des stéréotypes pour les éléments (New Terms)
        
        // Stakeholder (applicable aux Blocs SysML, donc Métaclasse Class)
        IRPStereotype stakeholder = createStereotype(profile, "stakeholder", "Class", 
                "Représente un acteur participant au réseau de valeur SVN.", true);
        
        if (stakeholder != null) {
            // Tag pour stocker le résultat du calcul
            createTag(stakeholder, "importanceScore", null); // null type means String by default or manual
        }
        
        // Système central (svnSystem)
        createStereotype(profile, "svnSystem", "Class", 
                "Identifie le système central faisant l'objet de l'analyse SVN.", true);
        
        // Arc de valeur (valueArc) sur les Associations
        IRPStereotype valueArc = createStereotype(profile, "valueArc", "Association", 
                "Modélise un flux de valeur entre deux acteurs ou entre un acteur et le système.", true);
        
        if (valueArc != null) {
            createTag(valueArc, "benefitRanking", benefitRankingType);
            createTag(valueArc, "supplyImportance", supplyImportanceType);
        }

        // 3. Création du type de diagramme SVN
        // Le diagramme SVN est une extension du BDD (StructureDiagram)
        IRPStereotype svnDiagram = createStereotype(profile, "SVNDiagram", "StructureDiagram", 
                "Diagramme spécifique pour la modélisation des Stakeholder Value Networks.", true);
        
        if (svnDiagram != null) {
            // Optionnel : on pourrait forcer des propriétés spécifiques ici via le profil
            // par exemple pour changer l'icône ou le comportement par défaut.
        }

        // 4. Sauvegarde
        project.save();
        System.out.println("[SVN] ✓ Profil SVN '" + PROFILE_NAME + "' configuré avec succès.");
        System.out.println("[SVN] Éléments créés : stakeholder, svnSystem, valueArc (avec tags), SVNDiagram.");
    }

    private static IRPProfile findOrCreateProfile(IRPProject project) {
        IRPCollection profiles = project.getProfiles();
        for (int i = 1; i <= profiles.getCount(); i++) {
            IRPModelElement p = (IRPModelElement) profiles.getItem(i);
            if (PROFILE_NAME.equals(p.getName())) {
                if (p instanceof IRPProfile) {
                    return (IRPProfile) p;
                } else {
                    System.err.println("[SVN] Conflit : l'élément nommé " + PROFILE_NAME + " n'est pas un profil mais un(e) " + p.getMetaClass());
                }
            }
        }
        try {
            System.out.println("[SVN] Création d'un nouveau profil : " + PROFILE_NAME);
            return project.addProfile(PROFILE_NAME);
        } catch (Exception e) {
            System.err.println("[SVN] Erreur addProfile : " + e.getMessage());
            return null;
        }
    }

    private static IRPStereotype findStereotype(IRPProfile profile, String name) {
        IRPCollection stereotypes = profile.getStereotypes();
        for (int i = 1; i <= stereotypes.getCount(); i++) {
            IRPStereotype st = (IRPStereotype) stereotypes.getItem(i);
            if (name.equals(st.getName())) return st;
        }
        return null;
    }

    private static IRPStereotype createStereotype(IRPProfile profile, String name, String metaclass, String description, boolean isNewTerm) {
        IRPStereotype st = findStereotype(profile, name);
        if (st == null) {
            try {
                // On utilise addNewAggr("Stereotype") car addStereotype(name, metaclass) 
                // semble être mal interprété (comme une application de stéréotype au lieu d'une création)
                st = (IRPStereotype) profile.addNewAggr("Stereotype", name);
            } catch (Exception e) {
                System.err.println("[SVN] Erreur addNewAggr Stereotype " + name + " : " + e.getMessage());
                return null;
            }
        }
        if (st != null) {
            st.setDescription(description);
            try {
                // Définit à quoi s'applique le stéréotype
                st.addMetaClass(metaclass);
            } catch (Exception e) {
                System.err.println("[SVN] Erreur addMetaClass " + metaclass + " sur " + name + " : " + e.getMessage());
            }
            if (isNewTerm) {
                st.setIsNewTerm(1);
            }
        }
        return st;
    }

    private static IRPType createEnumType(IRPProfile profile, String name, String[] literals) {
        IRPCollection types = profile.getTypes();
        IRPType type = null;
        for (int i = 1; i <= types.getCount(); i++) {
            IRPType t = (IRPType) types.getItem(i);
            if (name.equals(t.getName())) {
                type = t;
                break;
            }
        }
        
        if (type == null) {
            try {
                // Utilisation de addType (méthode de IRPPackage) au lieu de addNewAggr("Type")
                type = profile.addType(name);
                type.setKind("Enumeration");
                for (String lit : literals) {
                    type.addEnumerationLiteral(lit);
                }
            } catch (Exception e) {
                System.err.println("[SVN] Erreur addType " + name + " : " + e.getMessage());
            }
        }
        return type;
    }

    private static IRPTag createTag(IRPStereotype st, String name, IRPType type) {
        IRPTag tag = st.getTag(name);
        if (tag == null) {
            try {
                tag = (IRPTag) st.addNewAggr("Tag", name);
            } catch (Exception e) {
                System.err.println("[SVN] Erreur addTag " + name + " : " + e.getMessage());
            }
        }
        if (tag != null && type != null) {
            tag.setType(type);
        }
        return tag;
    }
}