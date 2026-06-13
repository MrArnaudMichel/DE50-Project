package fr.utbm.svn.model;

import java.util.List;
import java.util.Set;

/**
 * Immutable snapshot of a single step in the depth-first search (DFS) used to detect
 * value loops in the stakeholder value network.
 *
 * <p>Each {@code SearchState} captures:
 * <ul>
 *   <li>The GUID of the node currently being visited.</li>
 *   <li>The ordered list of GUIDs forming the path from the start node to the current one.</li>
 *   <li>The arc scores accumulated along that path.</li>
 *   <li>The set of GUIDs already visited to prevent re-visiting nodes.</li>
 * </ul>
 */
public class SearchState {

    /** GUID of the node currently being visited. */
    final String current;

    /** Ordered GUIDs forming the path from the start node to {@code current}. */
    final List<String> path;

    /** Arc scores accumulated along {@code path}, in the same order. */
    final List<Double> scores;

    /** GUIDs of nodes already visited on the current DFS branch. */
    final Set<String> visited;

    /**
     * Constructs a new search state.
     *
     * @param c the GUID of the current node
     * @param p the ordered list of GUIDs on the path from start to {@code c}
     * @param s the arc scores accumulated along {@code p}
     * @param v the set of GUIDs already visited on this branch
     */
    public SearchState(String c, List<String> p, List<Double> s, Set<String> v) {
        current = c; path = p; scores = s; visited = v;
    }

    /**
     * Returns the arc scores accumulated along the current path.
     *
     * @return list of arc scores, in traversal order
     */
    public List<Double> getScores() {
        return scores;
    }

    /**
     * Returns the GUID of the node currently being visited.
     *
     * @return current node GUID
     */
    public String getCurrent() {
        return current;
    }

    /**
     * Returns the ordered list of GUIDs forming the path from the start node to the current node.
     *
     * @return ordered GUID path
     */
    public List<String> getPath() {
        return path;
    }

    /**
     * Returns the set of GUIDs already visited on the current DFS branch.
     *
     * @return visited GUID set
     */
    public Set<String> getVisited() {
        return visited;
    }
}
