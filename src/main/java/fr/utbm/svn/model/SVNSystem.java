package fr.utbm.svn.model;

import com.telelogic.rhapsody.core.*;
import fr.utbm.svn.rhapsody.RhapsodyWrapper;
import fr.utbm.svn.constants.SVNConstants;

import java.util.ArrayList;
import java.util.List;

/**
 * Wraps a Rhapsody {@link IRPClass} that carries the {@code system} stereotype.
 *
 * <p>Represents the central system element in a Stakeholder Value Network (SVN) diagram.
 * After each calculation pass the system stores the total loop score and the list of
 * detected value loops, which are then reflected back into the Rhapsody model as tags.</p>
 */
public class SVNSystem {

    private final IRPClass system;

    /** Aggregated score computed as the sum of all individual value-loop scores. */
    private double totalLoopScore;

    /** All value loops detected in the last calculation pass. */
    private List<ValueLoop> loops;

    /**
     * Constructs a new SVN system wrapper and initialises the {@code totalLoopScore} tag
     * on the underlying Rhapsody class with a placeholder value ({@code "?"}).
     *
     * @param system the Rhapsody class with the {@code system} stereotype
     */
    public SVNSystem(IRPClass system) {
        this.system = system;
        this.totalLoopScore = 0;
        this.loops = new ArrayList<>();
        RhapsodyWrapper.setOrCreateTag(system, SVNConstants.TAG_TOTAL_LOOP_SCORE, "?");
    }

    /**
     * Returns the underlying Rhapsody class.
     *
     * @return the wrapped {@link IRPClass}
     */
    public IRPClass getSystem() {
        return system;
    }

    /**
     * Returns the total loop score computed in the last calculation pass.
     *
     * @return sum of all value-loop scores
     */
    public double getTotalLoopScore() {
        return totalLoopScore;
    }

    /**
     * Returns the list of value loops detected in the last calculation pass.
     *
     * @return list of {@link ValueLoop} objects
     */
    public List<ValueLoop> getLoops() {
        return loops;
    }

    /**
     * Updates the total loop score.
     *
     * @param totalLoopScore the new total loop score
     */
    public void setTotalLoopScore(double totalLoopScore) {
        this.totalLoopScore = totalLoopScore;
    }

    /**
     * Replaces the list of detected value loops.
     *
     * @param loops the new list of {@link ValueLoop} objects
     */
    public void setLoops(List<ValueLoop> loops) {
        this.loops = loops;
    }

    // -------------------------------------------------------------------------
    // Delegated methods from IRPClass
    // -------------------------------------------------------------------------

    /**
     * Returns the name of this system element as defined in the Rhapsody model.
     *
     * @return system name
     */
    public String getName() { return system.getName(); }

    /**
     * Returns the globally unique identifier of this system's class.
     *
     * @return GUID string
     */
    public String getGUID() { return system.getGUID(); }
}
