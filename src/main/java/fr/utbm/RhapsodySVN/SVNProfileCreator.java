package fr.utbm.RhapsodySVN;

import com.telelogic.rhapsody.core.*;

public class SVNProfileCreator {

    private static final String PROFILE_NAME = "SVN";

    private static final String[][] STEREOTYPES = {
            { "Stakeholder",        "Actor",        "Acteur participant au réseau de valeur SVN"    },
            { "CentralizedSystem",  "Class",        "Système central faisant l'objet du SVN"       },
            { "ValueObject",        "Class",        "Objet de valeur échangé dans le réseau"       },
            { "ValueExchange",      "Association",  "Échange de valeur entre deux acteurs"         },
            { "ValueOffering",      "Association",  "Offre groupée de valeurs d'un acteur à autre" },
            { "StakeholderNeed",    "Class",        "Besoin exprimé par un stakeholder"            },
            { "SystemGoal",         "Class",        "Objectif du système centralisé"               },
            { "ValuePath",          "Dependency",   "Chemin de valeur dans le réseau"              },
    };

    public static void main(String[] args) {
        IRPApplication app = RhapsodyAppServer.getActiveRhapsodyApplication();
        run(app);
    }

    public static void run(IRPApplication app) {
        IRPProject project = app.activeProject();
        if (project == null) {
            System.err.println("[SVN] Aucun projet actif.");
            return;
        }
        System.out.println("[SVN] Projet : " + project.getName());

        IRPProfile profile = findOrCreateProfile(project);
        if (profile == null) {
            System.err.println("[SVN] Impossible de créer le profil.");
            return;
        }

        for (String[] def : STEREOTYPES) {
            IRPStereotype st = findOrCreateStereotype(profile, def[0]);
            if (st == null) continue;

            st.setIsNewTerm(1);
            st.setDescription(def[2]);

            System.out.printf("[SVN] ✓ %-22s  New Term ON  (Applicable to: %s — manuel)%n",
                    def[0], def[1]);
        }

        project.save();
        System.out.println("[SVN] Sauvegardé.");
    }

    private static IRPProfile findOrCreateProfile(IRPProject project) {
        IRPCollection profiles = project.getProfiles();
        for (int i = 1; i <= profiles.getCount(); i++) {
            IRPProfile p = (IRPProfile) profiles.getItem(i);
            if (PROFILE_NAME.equals(p.getName())) {
                System.out.println("[SVN] Profil existant trouvé : " + PROFILE_NAME);
                return p;
            }
        }
        try {
            IRPProfile p = project.addProfile(PROFILE_NAME);
            System.out.println("[SVN] Profil créé : " + PROFILE_NAME);
            return p;
        } catch (Exception e) {
            System.err.println("[SVN] addProfile() échoué : " + e.getMessage());
            return null;
        }
    }

    private static IRPStereotype findOrCreateStereotype(IRPProfile profile, String name) {
        IRPCollection existing = profile.getStereotypes();
        for (int i = 1; i <= existing.getCount(); i++) {
            IRPStereotype st = (IRPStereotype) existing.getItem(i);
            if (name.equals(st.getName())) return st;
        }
        try {
            return profile.addStereotype(name, "Profile");
        } catch (Exception e) {
            System.err.println("[SVN] addStereotype() échoué pour " + name + " : " + e.getMessage());
            return null;
        }
    }
}