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

/**
 * Default implementation of {@link ICalculationService}.
 *
 * <p>Orchestrates the importance calculation by:
 * <ol>
 *   <li>Extracting the SVN elements (system, stakeholders, arcs) from the diagram.</li>
 *   <li>Running the {@link ValueLoopStrategy} as the primary strategy.</li>
 *   <li>Falling back to {@link ArcSumStrategy} when no value loops are found.</li>
 *   <li>Writing the results back into the Rhapsody model via
 *       {@link RhapsodyElementUpdater}.</li>
 * </ol>
 */
public class CalculationService implements ICalculationService {

    private final Logger logger = Logger.getInstance();

    /**
     * {@inheritDoc}
     *
     * <p>If {@code diagram} is {@code null} the method logs an error and returns
     * immediately. If any mandatory SVN element (system, stakeholders, arcs) is missing,
     * the method skips the score calculation but still attempts to update the system tags.</p>
     *
     * @param project the active Rhapsody project
     * @param diagram the SVN diagram to analyse
     */
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
