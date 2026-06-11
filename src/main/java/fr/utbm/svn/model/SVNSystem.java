package fr.utbm.svn.model;

import com.telelogic.rhapsody.core.*;
import fr.utbm.svn.constants.SVNConstants;

public class SVNSystem {

    private final IRPClass system;
    private double totalLoopScore;

    public SVNSystem(IRPClass system) {
        this.system = system;
        SVNConstants.setOrCreateTag(system, SVNConstants.TAG_TOTAL_LOOP_SCORE, "?");
    }

    public IRPClass getSystem() {
        return system;
    }
    public double getTotalLoopScore() {
        return totalLoopScore;
    }
    public void setTotalLoopScore(double totalLoopScore) {
        this.totalLoopScore = totalLoopScore;
    }

    // FROM IRPObjectModelDiagram

    public String getName() { return system.getName(); }
}
