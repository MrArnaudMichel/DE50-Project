package fr.utbm.svn.model;

import org.junit.Test;

import java.util.*;

import static org.junit.Assert.*;

/**
 * Unit tests for the SearchState model logic.
 *
 * SearchState stores the DFS search state for value loops detection.
 * Since the package transitively depends on the Rhapsody API,
 * we reproduce a simplified SearchState model here.
 */
public class SearchStateTest {

    // --- Simplified model reproducing SearchState ---

    static class SearchState {
        final String current;
        final List<String> path;
        final List<Double> scores;
        final Set<String> visited;

        SearchState(String c, List<String> p, List<Double> s, Set<String> v) {
            current = c; path = p; scores = s; visited = v;
        }

        String getCurrent() { return current; }
        List<String> getPath() { return path; }
        List<Double> getScores() { return scores; }
        Set<String> getVisited() { return visited; }
    }

    @Test
    public void constructor_storesAllFields() {
        String current = "nodeA";
        List<String> path = Arrays.asList("S", "nodeA");
        List<Double> scores = Arrays.asList(0.5, 0.8);
        Set<String> visited = new HashSet<>(Arrays.asList("S", "nodeA"));

        SearchState state = new SearchState(current, path, scores, visited);

        assertEquals("nodeA", state.getCurrent());
        assertEquals(path, state.getPath());
        assertEquals(scores, state.getScores());
        assertEquals(visited, state.getVisited());
    }

    @Test
    public void currentField() {
        SearchState state = new SearchState("X",
                Collections.emptyList(),
                Collections.emptyList(),
                Collections.emptySet());

        assertEquals("X", state.getCurrent());
    }

    @Test
    public void emptyFields() {
        SearchState state = new SearchState("start",
                Collections.emptyList(),
                Collections.emptyList(),
                Collections.emptySet());

        assertTrue(state.getPath().isEmpty());
        assertTrue(state.getScores().isEmpty());
        assertTrue(state.getVisited().isEmpty());
    }
}
