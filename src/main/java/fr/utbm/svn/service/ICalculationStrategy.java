package fr.utbm.svn.service;

import fr.utbm.svn.model.SVNSystem;
import fr.utbm.svn.model.Stakeholder;
import fr.utbm.svn.model.ValueArc;

import java.util.List;
import java.util.Map;

public interface ICalculationStrategy {
    Map<Stakeholder, Double> computeScores(List<Stakeholder> stakeholders, List<ValueArc> valueArcs, SVNSystem svnSystem);
}
