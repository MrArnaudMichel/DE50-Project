package fr.utbm.svn.model;

import com.telelogic.rhapsody.core.*;
import fr.utbm.svn.rhapsody.RhapsodyWrapper;
import fr.utbm.svn.constants.SVNConstants;

import java.util.ArrayList;
import java.util.List;

public class SVNSystem {

    private final IRPClass system;
    private double totalLoopScore;
    private List<ValueLoop> loops;

    public SVNSystem(IRPClass system) {
        this.system = system;
        this.totalLoopScore = 0;
        this.loops = new ArrayList<>();
        RhapsodyWrapper.setOrCreateTag(system, SVNConstants.TAG_TOTAL_LOOP_SCORE, "?");
    }

    public IRPClass getSystem() {
        return system;
    }
    public double getTotalLoopScore() {
        return totalLoopScore;
    }
    public List<ValueLoop> getLoops() {
        return loops;
    }
    public void setTotalLoopScore(double totalLoopScore) {
        this.totalLoopScore = totalLoopScore;
    }
    public void setLoops(List<ValueLoop> loops) {
        this.loops = loops;
    }

    // FROM IRPClass

    public String getName() { return system.getName(); }
    public String getGUID() { return system.getGUID(); }
}