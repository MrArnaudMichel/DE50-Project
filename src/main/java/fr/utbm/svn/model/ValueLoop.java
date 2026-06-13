package fr.utbm.svn.model;

import java.util.List;
import java.util.Map;

public class ValueLoop {
    final Map<String, String> nodes;
    final List<Double> arcScores;
    final List<ValueArc> arcs;
    double score;

    public ValueLoop(Map<String, String> n, List<Double> s, List<ValueArc> a) { 
        nodes = n; 
        arcScores = s; 
        arcs = a; 
    }

    public Map<String, String> getNodes() {
        return nodes;
    }

    public List<Double> getArcScores() {
        return arcScores;
    }

    public List<ValueArc> getArcs() {
        return arcs;
    }

    public double getScore() {
        return score;
    }

    public void setScore(double score) {
        this.score = score;
    }
}