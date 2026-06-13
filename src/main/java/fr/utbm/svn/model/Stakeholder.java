package fr.utbm.svn.model;

import com.telelogic.rhapsody.core.*;

/**
 * Wraps a Rhapsody {@link IRPActor} that carries the {@code stakeholder} stereotype.
 *
 * <p>Provides a domain-level view of a stakeholder, adding a computed importance
 * {@link #score} while delegating identity and model operations to the underlying actor.</p>
 */
public class Stakeholder {

    private final IRPActor actor;

    /** Computed importance score, set by the calculation service. Defaults to {@code 0.0}. */
    double score;

    /**
     * Constructs a new stakeholder wrapper around the given Rhapsody actor.
     *
     * @param actor the Rhapsody actor with the {@code stakeholder} stereotype
     */
    public Stakeholder(IRPActor actor) {
        this.actor = actor;
        this.score = 0.0;
    }

    // -------------------------------------------------------------------------
    // Getters
    // -------------------------------------------------------------------------

    /**
     * Returns the current importance score of this stakeholder.
     *
     * @return importance score, typically in the range [0.0, 1.0]
     */
    public double getScore() {
        return score;
    }

    /**
     * Returns the underlying Rhapsody actor.
     *
     * @return the wrapped {@link IRPActor}
     */
    public IRPActor getActor() {
        return actor;
    }

    // -------------------------------------------------------------------------
    // Setters
    // -------------------------------------------------------------------------

    /**
     * Sets the importance score for this stakeholder.
     *
     * @param score the new importance score
     */
    public void setScore(double score) {
        this.score = score;
    }

    // -------------------------------------------------------------------------
    // Delegated methods from IRPActor
    // -------------------------------------------------------------------------

    /**
     * Returns the name of this stakeholder as defined in the Rhapsody model.
     *
     * @return stakeholder name
     */
    public String getName() { return actor.getName(); }

    /**
     * Returns the globally unique identifier of this stakeholder's actor.
     *
     * @return GUID string
     */
    public String getGUID() { return actor.getGUID(); }

    /**
     * Returns the Rhapsody tag with the specified name from this actor.
     *
     * @param s the tag name to look up
     * @return the {@link IRPTag}, or {@code null} if not found
     */
    public IRPTag getTag(String s) { return actor.getTag(s); }

    /**
     * Sets the display name shown in Rhapsody diagrams for this actor.
     *
     * @param displayName the new display name
     */
    public void setDisplayName(String displayName) {
        actor.setDisplayName(displayName);
    }

    /**
     * Adds a new aggregate child element to this actor.
     *
     * @param s  the meta-class of the element to add (e.g. {@code "Tag"})
     * @param s1 the name to assign to the new element
     * @return the newly created model element
     */
    public IRPModelElement addNewAggr(String s, String s1) {
        return this.actor.addNewAggr(s, s1);
    }
}
