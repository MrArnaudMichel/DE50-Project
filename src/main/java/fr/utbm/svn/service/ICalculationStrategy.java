package fr.utbm.svn.service;

import com.telelogic.rhapsody.core.IRPActor;
import fr.utbm.svn.model.SVNSystem;
import fr.utbm.svn.model.Stakeholder;
import fr.utbm.svn.model.ValueArc;

import java.util.List;
import java.util.Map;

public interface ICalculationStrategy {
    Map<IRPActor, Double> computeScores(List<Stakeholder> stackholders, List<ValueArc> valueArcs, SVNSystem svnSystem);
}
