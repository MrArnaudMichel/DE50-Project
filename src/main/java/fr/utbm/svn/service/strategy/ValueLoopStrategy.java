package fr.utbm.svn.service.strategy;

import com.telelogic.rhapsody.core.IRPModelElement;
import fr.utbm.svn.model.SVNSystem;
import fr.utbm.svn.model.Stakeholder;
import fr.utbm.svn.model.ValueArc;
import fr.utbm.svn.service.ICalculationStrategy;

import java.util.Collections;
import java.util.List;

public class ValueLoopStrategy implements ICalculationStrategy {
    @Override
    public void computeScores(List<Stakeholder> stackholders, List<ValueArc> valueArcs, IRPModelElement root, SVNSystem svnSystem) {

    }
}
