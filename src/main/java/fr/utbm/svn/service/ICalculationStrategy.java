package fr.utbm.svn.service;

import com.telelogic.rhapsody.core.IRPModelElement;
import fr.utbm.svn.model.SVNSystem;
import fr.utbm.svn.model.Stakeholder;
import fr.utbm.svn.model.ValueArc;

import java.util.List;

public interface ICalculationStrategy {
    void computeScores(List<Stakeholder> stackholders, List<ValueArc> valueArcs, IRPModelElement root, SVNSystem svnSystem);
}
