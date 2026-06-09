package fr.utbm.svn.service.impl;

import com.telelogic.rhapsody.core.*;

import fr.utbm.RhapsodySVN.constants.SVNConstants;
import fr.utbm.RhapsodySVN.rhapsody.RhapsodyWrapper;
import fr.utbm.svn.model.SearchState;
import fr.utbm.svn.model.ValueArc;
import fr.utbm.svn.model.ValueLoop;
import fr.utbm.svn.service.ICalculationService;
import fr.utbm.svn.service.IUpdateElementService;
import fr.utbm.svn.service.ICalculationStrategy;

import java.util.*;


public class CalculationService implements ICalculationService {
    private ICalculationStrategy strategy;
    private IUpdateElementService updateElementService;

    public void setStrategy(ICalculationStrategy strategy) {

        this.strategy = strategy;
    }


    @Override
    public void calculateImportance(IRPModelElement root, IRPDiagram diagram) {
        System.out.println("[SVN] Début du calcul d'importance pour : " + root.getName());

        List<IRPActor> stakeholders = findStakeholders(diagram);
        if (stakeholders.isEmpty()) {
            System.out.println("[SVN] Aucun stakeholder trouvé.");
            return;
        }

        List<IRPDependency> allArcs = findValueArcs(diagram);
        System.out.println("[SVN] ValueArcs trouvés : " + allArcs.size());
        if (allArcs.isEmpty()) {
            System.out.println("[SVN] Aucun arc — calcul impossible.");
            return;
        }

        this.strategy.computeScores(stakeholders, valueArcs, )

        IRPModelElement system = findSystem(diagram);
        if (system == null) {
            System.out.println("[SVN] Aucun nœud «system» trouvé — "
                    + "calcul simplifié par somme des arcs.");
            calculateByArcSum(stakeholders, allArcs);
            return;
        }
        System.out.println("[SVN] Système central : " + system.getName());

        // Calcul par value loops (Équations 1 & 2)
        calculateByValueLoops(stakeholders, allArcs, system);
    }

    // -------------------------------------------------------------------------
    // Helpers
    // -------------------------------------------------------------------------

    private List<IRPActor> findStakeholders(IRPDiagram diagram) {
        List<IRPActor> result = new ArrayList<>();
        IRPCollection descendants = diagram.getElementsInDiagram();
        for (int i = 1; i <= descendants.getCount(); i++) {
            IRPModelElement el = (IRPModelElement) descendants.getItem(i);
            if (el instanceof IRPActor
                    && RhapsodyWrapper.hasStereotype(el, SVNConstants.STEREOTYPE_STAKEHOLDER)) {
                result.add((IRPActor) el);
                System.out.println("[SVN] Stakeholder trouvé : " + el.getName());
            }
        }
        return result;
    }
    
    private List<IRPDependency> findValueArcs(IRPDiagram diagram) {
        List<IRPDependency> result = new ArrayList<>();
        IRPCollection descendants = diagram.getElementsInDiagram();
        for (int i = 1; i <= descendants.getCount(); i++) {
            IRPModelElement el = (IRPModelElement) descendants.getItem(i);
            if (el instanceof IRPDependency
                    && RhapsodyWrapper.hasStereotype(el, SVNConstants.STEREOTYPE_VALUE_ARC)) {
                result.add((IRPDependency) el);
            }
        }
        return result;
    }

    private IRPModelElement findSystem(IRPDiagram diagram) {;
        IRPCollection descendants = diagram.getElementsInDiagram();
        for (int i = 1; i <= descendants.getCount(); i++) {
            IRPModelElement el = (IRPModelElement) descendants.getItem(i);
            System.out.println("[SVN][SystemDebug] Project element : " + el.getName());
            if (RhapsodyWrapper.hasStereotype(el, SVNConstants.STEREOTYPE_SYSTEM)) {
                return el;
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