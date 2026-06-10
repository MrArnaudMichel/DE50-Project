package fr.utbm.svn.service.impl;

import com.telelogic.rhapsody.core.*;

import fr.utbm.RhapsodySVN.constants.SVNConstants;
import fr.utbm.RhapsodySVN.rhapsody.RhapsodyWrapper;
import fr.utbm.svn.Logger;
import fr.utbm.svn.model.*;
import fr.utbm.svn.service.ICalculationService;
import fr.utbm.svn.service.IUpdateElementService;
import fr.utbm.svn.service.ICalculationStrategy;

import java.util.*;


public class CalculationService implements ICalculationService {
    private ICalculationStrategy strategy;
    private IUpdateElementService updateElementService;
    private final Logger logger = Logger.getInstance();

    public void setStrategy(ICalculationStrategy strategy) {

        this.strategy = strategy;
    }


    @Override
    public void calculateImportance(IRPModelElement root, IRPDiagram diagram) {
        // TODO: Note a t'on besoin de garder root en paramètre si c'est seulmeent pour le logging
        logger.log("Début du calcul d'importance pour : " + root.getName());

        List<Stakeholder> stakeholders = findStakeholders(diagram);
        if (stakeholders.isEmpty()) {
            logger.log("Aucun stakeholder trouvé.");
            return;
        }

        List<ValueArc> valueArcs = findValueArcs(diagram);
        logger.log("ValueArcs trouvés : " + valueArcs.size());
        if (valueArcs.isEmpty()) {
            logger.log("Aucun arc — calcul impossible.");
            return;
        }


        SVNSystem system = findSystem(diagram);
        if (system == null) {
            logger.log("Aucun nœud «system» trouvé — "
                    + "calcul simplifié par somme des arcs.");
            calculateByArcSum(stakeholders, valueArcs);
            return;
        }

        this.strategy.computeScores(stakeholders, valueArcs, system);

        logger.log("Système central : " + system.getName());

        // Calcul par value loops (Équations 1 & 2)
        calculateByValueLoops(stakeholders, valueArcs, system);
    }

    // -------------------------------------------------------------------------
    // Helpers
    // -------------------------------------------------------------------------

    private List<Stakeholder> findStakeholders(IRPDiagram diagram) {
        List<Stakeholder> result = new ArrayList<>();
        IRPCollection descendants = diagram.getElementsInDiagram();
        for (int i = 1; i <= descendants.getCount(); i++) {
            IRPModelElement el = (IRPModelElement) descendants.getItem(i);
            if (el instanceof IRPActor
                    && RhapsodyWrapper.hasStereotype(el, SVNConstants.STEREOTYPE_STAKEHOLDER)) {
                result.add((Stakeholder) el);
                System.out.println("[SVN] Stakeholder trouvé : " + el.getName());
            }
        }
        return result;
    }
    
    private List<ValueArc> findValueArcs(IRPDiagram diagram) {
        List<ValueArc> result = new ArrayList<>();
        IRPCollection descendants = diagram.getElementsInDiagram();
        for (int i = 1; i <= descendants.getCount(); i++) {
            IRPModelElement el = (IRPModelElement) descendants.getItem(i);
            if (el instanceof IRPDependency
                    && RhapsodyWrapper.hasStereotype(el, SVNConstants.STEREOTYPE_VALUE_ARC)) {
                result.add((ValueArc) el);
            }
        }
        return result;
    }

    private SVNSystem findSystem(IRPDiagram diagram) {;
        IRPCollection descendants = diagram.getElementsInDiagram();
        for (int i = 1; i <= descendants.getCount(); i++) {
            IRPModelElement el = (IRPModelElement) descendants.getItem(i);
            if (el instanceof IRPActor
                    && RhapsodyWrapper.hasStereotype(el, SVNConstants.STEREOTYPE_SYSTEM)) {
                return (SVNSystem) el;
            }
        }
        return null;
    }

    private void updateImportanceTag(IRPModelElement el, double score) {
        IRPTag tag = el.getTag(SVNConstants.TAG_IMPORTANCE_SCORE);
        if (tag == null) {
            try { tag = (IRPTag) el.addNewAggr("Tag", SVNConstants.TAG_IMPORTANCE_SCORE); }
            catch (Exception ignored) {}
        }

        if (tag != null) {
            // 1. Mise à jour de la valeur du Tag (c'est le plus important)
            tag.setValue(String.format("%.4f", score));

            // 2. Correction du nom pour éviter l'accumulation
            String currentName = el.getName();
            String baseName = currentName;

            // On nettoie le nom de tout ancien score existant (avec " : " ou votre ancien " _ ")
            if (currentName.contains(" : ")) {
                baseName = currentName.split(" : ")[0].trim();
            } else if (currentName.contains(" _ ")) {
                baseName = currentName.split(" _ ")[0].trim();
            }

            // On applique le DisplayName proprement sur le nom de base
            el.setDisplayName(baseName + " : " + String.format("%.4f", score));
        }
    }
}