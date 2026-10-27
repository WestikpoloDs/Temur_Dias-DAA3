package graph;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

/**
 * Reproducible random graph generator using a fixed seed.
 * Generates connected undirected graphs, general directed graphs (with cycles), and Directed Acyclic Graphs (DAGs).
 */
public class GraphGenerator {

    private static final double MIN_WEIGHT = 1.0;
    private static final double MAX_WEIGHT = 100.0;

    /**
     * Generates a reproducible, connected, undirected weighted graph with V vertices and E edges.
     * Step 1: Creates a random spanning tree over shuffled vertices (V - 1 edges) to guarantee connectivity.
     * Step 2: Uniformly adds the remaining E - (V - 1) unique undirected edges.
     */
    public static Graph generateConnectedUndirectedGraph(int numVertices, int numEdges, long seed) {
        int maxPossibleEdges = numVertices * (numVertices - 1) / 2;
        if (numEdges < numVertices - 1 || numEdges > maxPossibleEdges) {
            throw new IllegalArgumentException(
                String.format("Invalid edge count %d for connected undirected graph with %d vertices (must be in [%d, %d]).",
                    numEdges, numVertices, numVertices - 1, maxPossibleEdges));
        }

        Random rng = new Random(seed);
        Graph graph = new Graph(numVertices, false);
        Set<Long> existingEdges = new HashSet<>();

        List<Integer> vertices = new ArrayList<>(numVertices);
        for (int i = 0; i < numVertices; i++) {
            vertices.add(i);
        }
        Collections.shuffle(vertices, rng);

        // Step 1: Build random spanning tree (V - 1 edges)
        for (int i = 1; i < numVertices; i++) {
            int u = vertices.get(i);
            int v = vertices.get(rng.nextInt(i));
            double weight = roundWeight(MIN_WEIGHT + rng.nextDouble() * (MAX_WEIGHT - MIN_WEIGHT));
            graph.addEdge(u, v, weight);
            existingEdges.add(encodeUndirectedPair(u, v));
        }

        // Step 2: Add remaining random unique edges
        while (graph.getNumEdges() < numEdges) {
            int u = rng.nextInt(numVertices);
            int v = rng.nextInt(numVertices);
            if (u != v) {
                long key = encodeUndirectedPair(u, v);
                if (!existingEdges.contains(key)) {
                    double weight = roundWeight(MIN_WEIGHT + rng.nextDouble() * (MAX_WEIGHT - MIN_WEIGHT));
                    graph.addEdge(u, v, weight);
                    existingEdges.add(key);
                }
            }
        }

        return graph;
    }

    /**
     * Generates a reproducible directed graph with guaranteed cycles (for SCC and cycle detection testing).
     * Plants structured strongly connected clusters and fills remaining edges randomly.
     */
    public static Graph generateCyclicDirectedGraph(int numVertices, int numEdges, long seed) {
        int maxPossibleEdges = numVertices * (numVertices - 1);
        if (numEdges < 0 || numEdges > maxPossibleEdges) {
            throw new IllegalArgumentException("Invalid number of directed edges: " + numEdges);
        }

        Random rng = new Random(seed);
        Graph graph = new Graph(numVertices, true);
        Set<Long> existingEdges = new HashSet<>();

        // Plant a cycle among the first min(numVertices, 5) vertices to guarantee at least one cycle
        int cycleLen = Math.min(numVertices, 5);
        if (cycleLen >= 2 && numEdges >= cycleLen) {
            for (int i = 0; i < cycleLen; i++) {
                int u = i;
                int v = (i + 1) % cycleLen;
                double weight = roundWeight(MIN_WEIGHT + rng.nextDouble() * (MAX_WEIGHT - MIN_WEIGHT));
                graph.addEdge(u, v, weight);
                existingEdges.add(encodeDirectedPair(u, v));
            }
        }

        // Fill remaining edges uniformly across all pairs (u != v)
        while (graph.getNumEdges() < numEdges) {
            int u = rng.nextInt(numVertices);
            int v = rng.nextInt(numVertices);
            if (u != v) {
                long key = encodeDirectedPair(u, v);
                if (!existingEdges.contains(key)) {
                    double weight = roundWeight(MIN_WEIGHT + rng.nextDouble() * (MAX_WEIGHT - MIN_WEIGHT));
                    graph.addEdge(u, v, weight);
                    existingEdges.add(key);
                }
            }
        }

        return graph;
    }

    /**
     * Generates a reproducible Directed Acyclic Graph (DAG) with V vertices and E edges.
     * Generates a random topological permutation of vertices and only adds edges from earlier to later positions.
     */
    public static Graph generateDAG(int numVertices, int numEdges, long seed) {
        int maxPossibleEdges = numVertices * (numVertices - 1) / 2;
        if (numEdges < 0 || numEdges > maxPossibleEdges) {
            throw new IllegalArgumentException(
                String.format("Invalid edge count %d for DAG with %d vertices (max %d).",
                    numEdges, numVertices, maxPossibleEdges));
        }

        Random rng = new Random(seed);
        Graph graph = new Graph(numVertices, true);
        Set<Long> existingEdges = new HashSet<>();

        List<Integer> topoOrder = new ArrayList<>(numVertices);
        for (int i = 0; i < numVertices; i++) {
            topoOrder.add(i);
        }
        Collections.shuffle(topoOrder, rng);

        // Ensure backbone path if enough edges so most vertices are reachable from topoOrder.get(0)
        int backboneCount = Math.min(numVertices - 1, numEdges / 2);
        for (int i = 0; i < backboneCount; i++) {
            int u = topoOrder.get(i);
            int v = topoOrder.get(i + 1);
            double weight = roundWeight(MIN_WEIGHT + rng.nextDouble() * (MAX_WEIGHT - MIN_WEIGHT));
            graph.addEdge(u, v, weight);
            existingEdges.add(encodeDirectedPair(u, v));
        }

        while (graph.getNumEdges() < numEdges) {
            int idx1 = rng.nextInt(numVertices);
            int idx2 = rng.nextInt(numVertices);
            if (idx1 != idx2) {
                int fromIdx = Math.min(idx1, idx2);
                int toIdx = Math.max(idx1, idx2);
                int u = topoOrder.get(fromIdx);
                int v = topoOrder.get(toIdx);
                long key = encodeDirectedPair(u, v);
                if (!existingEdges.contains(key)) {
                    double weight = roundWeight(MIN_WEIGHT + rng.nextDouble() * (MAX_WEIGHT - MIN_WEIGHT));
                    graph.addEdge(u, v, weight);
                    existingEdges.add(key);
                }
            }
        }

        return graph;
    }

    private static long encodeUndirectedPair(int u, int v) {
        int a = Math.min(u, v);
        int b = Math.max(u, v);
        return (((long) a) << 32) | (b & 0xFFFFFFFFL);
    }

    private static long encodeDirectedPair(int u, int v) {
        return (((long) u) << 32) | (v & 0xFFFFFFFFL);
    }

    private static double roundWeight(double value) {
        return Math.round(value * 100.0) / 100.0;
    }
}
