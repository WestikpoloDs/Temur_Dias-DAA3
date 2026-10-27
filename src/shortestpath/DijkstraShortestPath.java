package shortestpath;

import graph.Edge;
import graph.Graph;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.PriorityQueue;


public class DijkstraShortestPath {

    public static class ShortestPathResult {
        private final int sourceVertex;
        private final double[] distances;
        private final int[] predecessors;
        private final long executionTimeNanos;
        private final long edgeExaminations;
        private final long relaxations;
        private final long heapOperations;
        private final long comparisons;

        public ShortestPathResult(
                int sourceVertex,
                double[] distances,
                int[] predecessors,
                long executionTimeNanos,
                long edgeExaminations,
                long relaxations,
                long heapOperations,
                long comparisons) {
            this.sourceVertex = sourceVertex;
            this.distances = distances;
            this.predecessors = predecessors;
            this.executionTimeNanos = executionTimeNanos;
            this.edgeExaminations = edgeExaminations;
            this.relaxations = relaxations;
            this.heapOperations = heapOperations;
            this.comparisons = comparisons;
        }

        public int getSourceVertex() {
            return sourceVertex;
        }

        public double getDistanceTo(int target) {
            return distances[target];
        }

        public double[] getDistances() {
            return Arrays.copyOf(distances, distances.length);
        }

        public boolean isReachable(int target) {
            return Double.isFinite(distances[target]);
        }


        public List<Integer> getPathTo(int target) {
            if (!isReachable(target)) {
                return Collections.emptyList();
            }
            List<Integer> path = new ArrayList<>();
            for (int curr = target; curr != -1; curr = predecessors[curr]) {
                path.add(curr);
            }
            Collections.reverse(path);
            return path;
        }

        public long getExecutionTimeNanos() {
            return executionTimeNanos;
        }

        public long getEdgeExaminations() {
            return edgeExaminations;
        }

        public long getRelaxations() {
            return relaxations;
        }

        public long getHeapOperations() {
            return heapOperations;
        }

        public long getComparisons() {
            return comparisons;
        }

        public long getTotalOperations() {
            return edgeExaminations + relaxations + heapOperations + comparisons;
        }
    }

    private static class NodeDistance {
        final int vertex;
        final double distance;

        NodeDistance(int vertex, double distance) {
            this.vertex = vertex;
            this.distance = distance;
        }
    }


    public static ShortestPathResult computeShortestPaths(Graph graph, int sourceVertex) {
        int n = graph.getNumVertices();
        if (sourceVertex < 0 || sourceVertex >= n) {
            throw new IndexOutOfBoundsException("Invalid source vertex: " + sourceVertex);
        }

        for (Edge e : graph.getEdges()) {
            if (e.getWeight() < 0.0) {
                throw new IllegalArgumentException(
                        "Dijkstra's algorithm requires non-negative edge weights. Found negative edge: " + e);
            }
        }

        long startTime = System.nanoTime();

        double[] dist = new double[n];
        int[] pred = new int[n];
        boolean[] settled = new boolean[n];
        Arrays.fill(dist, Double.POSITIVE_INFINITY);
        Arrays.fill(pred, -1);

        long[] comparisons = new long[]{0};
        long edgeExaminations = 0;
        long relaxations = 0;
        long heapOperations = 0;

        PriorityQueue<NodeDistance> pq = new PriorityQueue<>(
                Math.max(1, n),
                (a, b) -> {
                    comparisons[0]++;
                    return Double.compare(a.distance, b.distance);
                }
        );

        dist[sourceVertex] = 0.0;
        pq.offer(new NodeDistance(sourceVertex, 0.0));
        heapOperations++;

        while (!pq.isEmpty()) {
            NodeDistance current = pq.poll();
            heapOperations++;
            int u = current.vertex;

            if (settled[u]) {
                continue;
            }
            settled[u] = true;

            for (Edge edge : graph.getNeighbors(u)) {
                edgeExaminations++;
                int v = edge.getDestination();
                if (settled[v]) {
                    continue;
                }
                double newDist = dist[u] + edge.getWeight();
                comparisons[0]++;
                if (newDist < dist[v]) {
                    dist[v] = newDist;
                    pred[v] = u;
                    relaxations++;
                    pq.offer(new NodeDistance(v, newDist));
                    heapOperations++;
                }
            }
        }

        long elapsedNanos = System.nanoTime() - startTime;
        return new ShortestPathResult(
                sourceVertex,
                dist,
                pred,
                elapsedNanos,
                edgeExaminations,
                relaxations,
                heapOperations,
                comparisons[0]
        );
    }
}
