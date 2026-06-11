package fr.utbm.svn.model;

import com.telelogic.rhapsody.core.*;

public class Stakeholder {
    private final IRPActor actor;
    double score;

    public Stakeholder(IRPActor actor) {
        this.actor = actor;
        this.score = 0.0;
    }

    // GETTERS

    public double getScore() {
        return score;
    }

    public IRPActor getActor() {
        return actor;
    }

    // SETTERS

    public void setScore(double score) {
        this.score = score;
    }

    // FROM IRPActor

    public String getName() { return actor.getName(); }
    public String getGUID() { return actor.getGUID(); }
    public IRPTag getTag(String s) { return actor.getTag(s); }
    public void setDisplayName(String displayName) {
        actor.setDisplayName(displayName);
    }
    public IRPModelElement addNewAggr(String s, String s1) {
        return this.actor.addNewAggr(s, s1);
    }

}
