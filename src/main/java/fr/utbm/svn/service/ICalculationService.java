package fr.utbm.svn.service;

import com.telelogic.rhapsody.core.IRPDiagram;
import com.telelogic.rhapsody.core.IRPProject;

/**
 * Service contract for computing and persisting stakeholder importance scores.
 *
 * <p>Implementations are expected to extract the SVN elements from the supplied diagram,
 * run the appropriate calculation strategy, and write the results back into the
 * Rhapsody model.</p>
 */
public interface ICalculationService {

    /**
     * Computes stakeholder importance scores for all elements in the given diagram
     * and updates the Rhapsody model accordingly.
     *
     * @param project the active Rhapsody project (used to look up elements by GUID)
     * @param diagram the SVN diagram that contains the stakeholders, system, and value arcs
     *                to be analysed; may be {@code null}, in which case the call is a no-op
     */
    void calculateImportance(IRPProject project, IRPDiagram diagram);
}
