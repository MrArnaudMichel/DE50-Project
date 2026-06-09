package fr.utbm.svn.model;

import java.util.List;
import java.util.Set;

public class SearchState {
    final String current;
    final List<String> path;
    final List<Double> scores;
    final Set<String> visited;
    SearchState(String c, List<String> p, List<Double> s, Set<String> v) {
        current = c; path = p; scores = s; visited = v;
    }
}
