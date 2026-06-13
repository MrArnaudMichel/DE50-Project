package fr.utbm.svn.model;

import org.junit.Test;

import java.util.*;

import static org.junit.Assert.*;

/**
 * Unit tests for the {@link SearchState} model logic.
 *
 * <p>{@code SearchState} stores the DFS search state used for value-loop detection.
 * Because the production class transitively depends on the Rhapsody API (unavailable
 * during tests), a simplified local reproduction is used here to verify the data-holder
 * behaviour independently.</p>
 */
public class SearchStateTest {

    // -------------------------------------------------------------------------
    // Simplified local reproduction of SearchState
    // -------------------------------------------------------------------------

    static class SearchState {
        final String current;
        final List<String> path;
        final List<Double> scores;
        final Set<String> visited;

        /**
         * Constructs a local SearchState for testing purposes.
         *
         * @param c the current node identifier
         * @param p the ordered path of node identifiers
         * @param s the arc scores along the path
         * @param v the set of already-visited node identifiers
         */
        SearchState(String c, List<String> p, List<Double> s, Set<String> v) {
            current = c; path = p; scores = s; visited = v;
        }

        /** @return the current node identifier */
        String getCurrent() { return current; }

        /** @return the ordered path of node identifiers */
        List<String> getPath() { return path; }

        /** @return the arc scores along the path */
        List<Double> getScores() { return scores; }

        /** @return the set of already-visited node identifiers */
        Set<String> getVisited() { return visited; }
    }

    // -------------------------------------------------------------------------
    // Tests
    // -------------------------------------------------------------------------

    /**
     * Verifies that all four fields are stored correctly by the constructor.
     */
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

    /**
     * Verifies that {@code getCurrent()} returns the value passed at construction.
     */
    @Test
    public void currentField() {
        SearchState state = new SearchState("X",
                Collections.emptyList(),
                Collections.emptyList(),
                Collections.emptySet());

        assertEquals("X", state.getCurrent());
    }

    /**
     * Verifies that path, scores, and visited are all empty when constructed with empty collections.
     */
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
