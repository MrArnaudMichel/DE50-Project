package fr.utbm.RhapsodySVN.model;

public class ArcEdge {
    final String target;
    final double score;
    public ArcEdge(String t, double s) { target = t; score = s; }

    public double getScore() {
        return score;
    }

    public String getTarget() {
        return target;
    }
}