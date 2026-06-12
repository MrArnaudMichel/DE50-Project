package fr.utbm.svn.model;

import java.util.List;
import java.util.Set;

public class SearchState {
    final String current;
    final List<String> path;
    final List<Double> scores;
    final Set<String> visited;
    public SearchState(String c, List<String> p, List<Double> s, Set<String> v) {
        current = c; path = p; scores = s; visited = v;
    }

    public List<Double> getScores() {
        return scores;
    }

    public String getCurrent() {
        return current;
    }

    public List<String> getPath() {
        return path;
    }

    public Set<String> getVisited() {
        return visited;
    }


}
