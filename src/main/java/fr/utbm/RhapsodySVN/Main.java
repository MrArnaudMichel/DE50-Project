package fr.utbm.RhapsodySVN;

import com.telelogic.rhapsody.core.*;
import fr.utbm.RhapsodySVN.service.CalculationService;
import fr.utbm.RhapsodySVN.service.DiagramService;
import fr.utbm.RhapsodySVN.service.ProfileService;

public class Main {

    public static void main(String[] args) {
        IRPApplication app = RhapsodyAppServer.getActiveRhapsodyApplication();
        IRPProject project = app.activeProject();

        if (project == null) {
            System.err.println("[SVN] Aucun projet actif.");
            return;
        }

        // --- Analyse des arguments CLI ---
        boolean clean      = false;
        boolean calculate  = false;
        boolean labelArcs  = false;
        boolean colorize   = false;

        for (String arg : args) {
            switch (arg.toLowerCase()) {
                case "-clean":     clean     = true; break;
                case "-calculate": calculate = true; break;
                case "-labels":    labelArcs = true; break;
                case "-colorize":  colorize  = true; break;
                // -debug conservé comme alias de -clean (comportement d'origine)
                case "-debug":     clean     = true; break;
            }
        }

        // 1. Configuration du profil (toujours exécutée)
        ProfileService profileService = new ProfileService(project);
        profileService.configureProfile(clean);

        // 2. Calcul d'importance (si demandé ou si -colorize implique un calcul préalable)
        IRPModelElement root = app.getSelectedElement();
        if (root == null) root = project; // fallback sur le projet entier

        DiagramService diagramService = new DiagramService();

        if (calculate || colorize) {
            System.out.println("[SVN] Calcul d'importance...");
            new CalculationService().calculateImportance(root);
        }

        // 3. Mise à jour des labels sur les arcs
        if (labelArcs) {
            System.out.println("[SVN] Mise à jour des labels d'arcs...");
            diagramService.updateArcLabels(root);
        }

        // 4. Colorisation des stakeholders
        if (colorize) {
            System.out.println("[SVN] Colorisation des stakeholders...");
            diagramService.colorizeStakeholdersByRank(root);
        }
    }
}