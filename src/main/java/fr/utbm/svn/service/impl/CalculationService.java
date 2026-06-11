package fr.utbm.svn.service.impl;

import com.telelogic.rhapsody.core.*;

import fr.utbm.svn.rhapsody.RhapsodyWrapper;
import fr.utbm.svn.Logger;
import fr.utbm.svn.constants.SVNConstants;
import fr.utbm.svn.model.*;
import fr.utbm.svn.rhapsody.RhapsodyElementUpdater;
import fr.utbm.svn.service.ICalculationService;
import fr.utbm.svn.service.ICalculationStrategy;
import fr.utbm.svn.service.strategy.ArcSumStrategy;
import fr.utbm.svn.service.strategy.ValueLoopStrategy;

import java.util.*;

public class CalculationService implements ICalculationService {
    private final Logger logger = Logger.getInstance();


    @Override
    public void calculateImportance(IRPDiagram diagram) {
        ICalculationStrategy fallBackStrategy = new ArcSumStrategy();

        logger.log("Starting importance calculation.");
        List<Stakeholder> stakeholders = findStakeholders(diagram);
        if (stakeholders.isEmpty()) {
            logger.log("No stakeholders found.");
            return;
        }

        List<ValueArc> valueArcs = findValueArcs(diagram);
        if (valueArcs.isEmpty()) { logger.log("No arcs, can't calculate."); return; }

        SVNSystem system = findSystem(diagram);

        Map<Stakeholder, Double> scores = Collections.emptyMap();

        if (system != null) {
            logger.log("SVNSystem : " + system.getName());
            scores = new ValueLoopStrategy(system).computeScores(stakeholders, valueArcs);
        }

        if (scores.isEmpty()) {
            scores = fallBackStrategy.computeScores(stakeholders, valueArcs);
        }

        scores.forEach(RhapsodyElementUpdater::updateStakeholderImportance);
        if (system != null) RhapsodyElementUpdater.updateSystemTags(system, system.getTotalLoopScore());
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
                result.add(new Stakeholder((IRPActor) el));
                logger.log("Stakeholder found : " + el.getName());
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
                result.add(new ValueArc((IRPDependency) el));
            }
        }
        return result;
    }

    private SVNSystem findSystem(IRPDiagram diagram) {;
        IRPCollection descendants = diagram.getElementsInDiagram();
        for (int i = 1; i <= descendants.getCount(); i++) {
            IRPModelElement el = (IRPModelElement) descendants.getItem(i);
            if (el instanceof IRPClass
                    && RhapsodyWrapper.hasStereotype(el, SVNConstants.STEREOTYPE_SYSTEM)) {
                return new SVNSystem((IRPClass) el);
            }
        }
        return null;
    }
}