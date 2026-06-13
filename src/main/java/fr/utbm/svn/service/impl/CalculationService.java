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

import static fr.utbm.svn.rhapsody.RhapsodyWrapper.*;

public class CalculationService implements ICalculationService {
    private final Logger logger = Logger.getInstance();


    @Override
    public void calculateImportance(IRPProject project, IRPDiagram diagram) {
        ICalculationStrategy fallBackStrategy = new ArcSumStrategy();

        if (diagram == null) {
            logger.error("calculateImportance: getDiagramOfSelectedElement() returned null - " +
                    "no element selected, or selection lost. Calculation skipped.");
            return;
        }
        logger.log("Starting importance calculation.");

        SVNSystem system = findSystem(diagram);
        List<Stakeholder> stakeholders = findStakeholders(diagram);
        List<ValueArc> valueArcs = findValueArcs(diagram);

        if (system != null && !stakeholders.isEmpty() && !valueArcs.isEmpty()) {
            logger.log("SVNSystem : " + system.getName());
            Map<Stakeholder, Double> scores = new ValueLoopStrategy(system).computeScores(stakeholders, valueArcs);

            if (scores.isEmpty()) {
                scores = fallBackStrategy.computeScores(stakeholders, valueArcs);
            }
            scores.forEach(RhapsodyElementUpdater::updateStakeholderImportance);
        }

        if (system != null)
            RhapsodyElementUpdater.updateSystemTags(system, system.getLoops(), system.getTotalLoopScore(), valueArcs);
    }


}