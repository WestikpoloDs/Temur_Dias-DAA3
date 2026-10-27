package graph;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;


public class GraphGenerator {

    private static final double MIN_WEIGHT = 1.0;
    private static final double MAX_WEIGHT = 100.0;


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

        for (int i = 1; i < numVertices; i++) {
            int u = vertices.get(i);
            int v = vertices.get(rng.nextInt(i));
            double weight = roundWeight(MIN_WEIGHT + rng.nextDouble() * (MAX_WEIGHT - MIN_WEIGHT));
            graph.addEdge(u, v, weight);
            existingEdges.add(encodeUndirectedPair(u, v));
        }

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

    public static Graph generateCyclicDirectedGraph(int numVertices, int numEdges, long seed) {
        int maxPossibleEdges = numVertices * (numVertices - 1);
        if (numEdges < 0 || numEdges > maxPossibleEdges) {
            throw new IllegalArgumentException("Invalid number of directed edges: " + numEdges);
        }

        Random rng = new Random(seed);
        Graph graph = new Graph(numVertices, true);
        Set<Long> existingEdges = new HashSet<>();

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
