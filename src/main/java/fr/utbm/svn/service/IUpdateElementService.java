package fr.utbm.svn.service;

import com.telelogic.rhapsody.core.IRPProject;
import fr.utbm.svn.model.SVNSystem;
import fr.utbm.svn.model.Stakeholder;
import fr.utbm.svn.model.ValueArc;
import fr.utbm.svn.model.ValueLoop;

import java.util.List;

public interface IUpdateElementService {
    public void updateArcLabel(ValueArc arc, IRPProject project);
    public void updateSystemTags(SVNSystem system, List<ValueLoop> loops, double totalLoopScore);
    public void updateStakeholderImportance(Stakeholder sh, double score);
}
