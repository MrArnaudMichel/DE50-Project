package fr.utbm.svn.model;

import java.util.List;

public class ValueLoop {
    final List<String> nodes;
    final List<Double> arcScores;
    double score;

    public ValueLoop(List<String> n, List<Double> s) { nodes = n; arcScores = s; }

    public List<String> getNodes() {
        return nodes;
    }

    public List<Double> getArcScores() {
        return arcScores;
    }

    public double getScore() {
        return score;
    }

    public void setScore(double score) {
        this.score = score;
    }
}
