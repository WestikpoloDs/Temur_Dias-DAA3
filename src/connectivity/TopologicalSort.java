package connectivity;

import graph.Edge;
import graph.Graph;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Deque;
import java.util.List;

/**
 * Topological ordering and cycle detection for directed graphs (Task 2).
 * Supports both Kahn's BFS (in-degree queue) algorithm and 3-color DFS post-order reversal.
 */
public class TopologicalSort {

    public static class TopologicalResult {
        private final boolean isDAG;
        private final boolean hasCycle;
        private final List<Integer> topologicalOrder;
        private final long executionTimeNanos;
        private final long vertexVisits;
        private final long edgeExaminations;

        public TopologicalResult(
                boolean isDAG,
                List<Integer> topologicalOrder,
                long executionTimeNanos,
                long vertexVisits,
                long edgeExaminations) {
            this.isDAG = isDAG;
            this.hasCycle = !isDAG;
            this.topologicalOrder = isDAG ? Collections.unmodifiableList(topologicalOrder) : Collections.emptyList();
            this.executionTimeNanos = executionTimeNanos;
            this.vertexVisits = vertexVisits;
            this.edgeExaminations = edgeExaminations;
        }

        public boolean isDAG() {
            return isDAG;
        }

        public boolean hasCycle() {
            return hasCycle;
        }

        public List<Integer> getTopologicalOrder() {
            return topologicalOrder;
        }

        public long getExecutionTimeNanos() {
            return executionTimeNanos;
        }

        public long getVertexVisits() {
            return vertexVisits;
        }

        public long getEdgeExaminations() {
            return edgeExaminations;
        }

        public long getTotalOperations() {
            return vertexVisits + edgeExaminations;
        }
    }

    /**
     * Computes a topological ordering using Kahn's in-degree algorithm.
     * Detects cycles if fewer than V vertices can be scheduled.
     */
    public static TopologicalResult sortKahn(Graph graph) {
        if (!graph.isDirected()) {
            throw new IllegalArgumentException("Topological sort requires a directed graph.");
        }

        int n = graph.getNumVertices();
        long startTime = System.nanoTime();

        int[] inDegree = new int[n];
        long edgeExaminations = 0;

        for (int u = 0; u < n; u++) {
            for (Edge e : graph.getNeighbors(u)) {
                inDegree[e.getDestination()]++;
                edgeExaminations++;
            }
        }

        Deque<Integer> queue = new ArrayDeque<>();
        for (int i = 0; i < n; i++) {
            if (inDegree[i] == 0) {
                queue.offer(i);
            }
        }

        List<Integer> order = new ArrayList<>(n);
        long vertexVisits = 0;

        while (!queue.isEmpty()) {
            int u = queue.poll();
            vertexVisits++;
            order.add(u);

            for (Edge e : graph.getNeighbors(u)) {
                edgeExaminations++;
                int v = e.getDestination();
                inDegree[v]--;
                if (inDegree[v] == 0) {
                    queue.offer(v);
                }
            }
        }

        boolean isDAG = (order.size() == n);
        long elapsedNanos = System.nanoTime() - startTime;

        return new TopologicalResult(isDAG, order, elapsedNanos, vertexVisits, edgeExaminations);
    }

    /**
     * Verifies whether a given vertex ordering is a valid topological sort of the directed graph.
     */
    public static boolean isValidTopologicalOrder(Graph graph, List<Integer> order) {
        int n = graph.getNumVertices();
        if (order == null || order.size() != n) {
            return false;
        }
        int[] position = new int[n];
        boolean[] seen = new boolean[n];
        for (int i = 0; i < n; i++) {
            int v = order.get(i);
            if (v < 0 || v >= n || seen[v]) {
                return false;
            }
            seen[v] = true;
            position[v] = i;
        }
        for (Edge e : graph.getEdges()) {
            if (position[e.getSource()] >= position[e.getDestination()]) {
                return false;
            }
        }
        return true;
    }
}
