package fr.utbm.RhapsodySVN;

import com.telelogic.rhapsody.core.*;
import fr.utbm.RhapsodySVN.constants.SVNConstants;
import fr.utbm.RhapsodySVN.rhapsody.RhapsodyWrapper;
import fr.utbm.RhapsodySVN.service.CalculationService;

import static fr.utbm.RhapsodySVN.rhapsody.RhapsodyWrapper.getTagValue;
import static fr.utbm.RhapsodySVN.service.CalculationService.getArcScore;

public class SVNPlugin extends RPUserPlugin {

    private IRPApplication app;
    private IRPProject project;
    private SVNModelListener listener;
    private CalculationService calculationService;

    private boolean isCalculating = false;

    @Override
    public void RhpPluginInit(IRPApplication rpyApplication) {
        this.app = rpyApplication;
        this.project = app.activeProject();
        this.calculationService = new CalculationService();

        // Activation des notifications de changement d'éléments
        if (project != null) {
            project.setNotifyPluginOnElementsChanged(1);
        }

        // Connexion du listener via l'API correcte
        listener = new SVNModelListener();
        listener.connect(app);

        System.out.println("[SVN] Plugin initialisé avec listener automatique.");
    }

    // -------------------------------------------------------------------------
    // Listener d'événements Rhapsody
    // -------------------------------------------------------------------------

    private class SVNModelListener extends RPApplicationListener {

        @Override
        public String getId() {
            return "SVNModelListener";
        }

        @Override
        public boolean onDiagramOpen(IRPDiagram irpDiagram) {
            return false;
        }

        @Override
        public boolean onDoubleClick(IRPModelElement irpModelElement) {
            return false;
        }

        @Override
        public boolean onFeaturesOpen(IRPModelElement irpModelElement) {
            return false;
        }

        /**
         * Appelé après l'ajout d'un élément au modèle.
         * On détecte la création d'un nouvel arc «valuearc».
         */
        @Override
        public boolean afterAddElement(IRPModelElement element) {
            if (!isValueArc(element)) return false;

            IRPDependency arc = (IRPDependency) element;
            System.out.println("[SVN] Nouvel arc détecté : " + arc.getName());

            try {
                // Stop notifications to avoid initializations of onElementsChanged
                project.setNotifyPluginOnElementsChanged(0);

                initDefaultTags(arc);
                updateArcLabel(arc);
                calculationService.calculateImportance(project);

            } catch (Exception e) {
                System.err.println("[SVN] Erreur lors de l'afterAddElement : " + e.getMessage());
            } finally {
                project.setNotifyPluginOnElementsChanged(1);
            }

            return false;
        }

        @Override
        public boolean afterProjectClose(String s) {
            return false;
        }

        @Override
        public boolean beforeProjectClose(IRPProject irpProject) {
            return false;
        }

        /**
         * Appelé quand des éléments sont modifiés, via leurs GUIDs.
         * On filtre sur les arcs «valuearc» dont un tag SVN a changé.
         */
        @Override
        public boolean onElementsChanged(String GUIDs) {
            if (isCalculating) {
                return false;
            }

            if (GUIDs == null || GUIDs.trim().isEmpty()) return false;

            String[] guidArray = GUIDs.split(",");
            boolean elementHasBeenDeleted = false;

            try {
                isCalculating = true;

                for (String guid : guidArray) {
                    guid = guid.trim();
                    if (guid.isEmpty()) continue;

                    IRPModelElement element = project.findElementByGUID(guid);

                    if (element == null) {
                        elementHasBeenDeleted = true;
                        continue;
                    }

                    // Cas 1 : le tag lui-même a été modifié → on remonte à l'owner
                    if (element instanceof IRPTag) {
                        String tagName = element.getName();
                        if (!SVNConstants.TAG_BENEFIT_RANKING.equals(tagName)
                                && !SVNConstants.TAG_SUPPLY_IMPORTANCE.equals(tagName)) {
                            continue;
                        }
                        IRPModelElement owner = element.getOwner();
                        if (isValueArc(owner)) {
                            System.out.println("[SVN] Tag '" + tagName
                                    + "' modifié sur arc : " + owner.getName());
                            try {
                                // Silence notifications from calculations modifications
                                project.setNotifyPluginOnElementsChanged(0);

                                updateArcLabel((IRPDependency) owner);
                                calculationService.calculateImportance(project);

                            } finally {
                                project.setNotifyPluginOnElementsChanged(1);
                            }

                            // An element justify a change so it was not a deletion notification
                            elementHasBeenDeleted = false;
                            break;
                        }
                    }
                }

                if (elementHasBeenDeleted) {
                    try {
                        System.out.println("[SVN] Notification de suppression potentielle, lancement du recalcul global.");
                        project.setNotifyPluginOnElementsChanged(0);

                        calculationService.calculateImportance(project);

                    } finally {
                        project.setNotifyPluginOnElementsChanged(1);
                    }
                }

            } catch (Exception e) {
                System.err.println("[SVN] Erreur lors du traitement onElementsChanged : " + e.getMessage());
            } finally {
                isCalculating = false;
            }

            return false;
        }

        // ---------------------------------------------------------------------
        // Helpers
        // ---------------------------------------------------------------------

        private boolean isValueArc(IRPModelElement element) {
            return element instanceof IRPDependency
                    && RhapsodyWrapper.hasStereotype(element, SVNConstants.STEREOTYPE_VALUE_ARC);
        }

        private void initDefaultTags(IRPDependency arc) {
            initTagIfAbsent(arc, SVNConstants.TAG_BENEFIT_RANKING,
                    SVNConstants.LITERALS_BENEFIT[0]); // MIGHT_BE
            initTagIfAbsent(arc, SVNConstants.TAG_SUPPLY_IMPORTANCE,
                    SVNConstants.LITERALS_SUPPLY[0]);  // LOW
        }

        private void initTagIfAbsent(IRPDependency arc, String tagName, String defaultValue) {
            try {
                IRPTag tag = arc.getTag(tagName);
                if (tag == null || tag.getValue() == null || tag.getValue().isEmpty()) {
                    if (tag == null) {
                        tag = (IRPTag) arc.addNewAggr("Tag", tagName);
                    }
                    if (tag != null) tag.setValue(defaultValue);
                }
            } catch (Exception e) {
                System.err.println("[SVN] initTagIfAbsent " + tagName + " : " + e.getMessage());
            }
        }

        private void updateArcLabel(IRPDependency arc) {
            String benefit = getTagValue(arc, SVNConstants.TAG_BENEFIT_RANKING, "MIGHT_BE");
            String supply  = getTagValue(arc, SVNConstants.TAG_SUPPLY_IMPORTANCE, "LOW");
            double score   = getArcScore(benefit, supply);
            String label   = String.format("%.2f", score);

            // Tentative 1 : setDisplayName sur l'élément modèle
            try {
                arc.setDisplayName(label);
                arc.setIsShowDisplayName(1);
                System.out.println("[SVN] Label arc '" + arc.getName() + "' → " + label);
                return;
            } catch (Exception e) {
                System.err.println("[SVN] setDisplayName échoué : " + e.getMessage());
            }

            // Tentative 2 : via les GraphElements du diagramme
            try {
                IRPCollection allDiags = project.getNestedElementsByMetaClass(
                        "ObjectModelDiagram", 1);
                for (int i = 1; i <= allDiags.getCount(); i++) {
                    Object d = allDiags.getItem(i);
                    if (!(d instanceof IRPObjectModelDiagram)) continue;
                    IRPCollection graphElems =
                            ((IRPObjectModelDiagram) d).getGraphicalElements();
                    for (int j = 1; j <= graphElems.getCount(); j++) {
                        Object ge = graphElems.getItem(j);
                        if (!(ge instanceof IRPGraphElement)) continue;
                        IRPGraphElement graphElem = (IRPGraphElement) ge;
                        if (arc.equals(graphElem.getModelObject())) {
                            graphElem.setGraphicalPropertyOfText("Keyword", "Text", label);
                            System.out.println("[SVN] Label arc via GraphElement → " + label);
                        }
                    }
                }
            } catch (Exception e) {
                System.err.println("[SVN] Mise à jour graphique échouée : " + e.getMessage());
            }
        }
    }

    @Override
    public void OnMenuItemSelect(String menuItem) {
//        System.out.println("[SVN] Commande : " + menuItem);
//        switch (menuItem) {
//            case "SVN Configure":             SVNConfigureCommand.run(app);            break;
//            case "SVN Calculate":             SVNCalculateCommand.run(app);            break;
//            case "SVN Update Arc Labels":     SVNLabelArcCommand.run(project);         break;
//            case "SVN Set Arc Color":         SVNArcColorCommand.run(app);             break;
//            case "SVN Colorize Stakeholders": SVNColorizeStakeholdersCommand.run(app); break;
//            case "SVN Create Arc":            SVNCreateArcCommand.run(app);            break;
//            case "SVN Clean":                 SVNCleanCommand.run(app);                break;
//            default: System.err.println("[SVN] Commande inconnue : " + menuItem);
//        }
    }

    @Override
    public void RhpPluginInvokeItem() { SVNConfigureCommand.run(app); }

    @Override public void OnTrigger(String trigger) {}
    @Override public boolean RhpPluginCleanup() {
        // Déconnecte proprement le listener à la fermeture
        if (listener != null) {
            try { listener.disconnect(); } catch (Exception ignored) {}
        }
        return true;
    }
    @Override public void RhpPluginFinalCleanup() {
        System.out.println("[SVN] Plugin déchargé.");
    }
}