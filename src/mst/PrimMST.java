package mst;

import graph.Edge;
import graph.Graph;
import java.util.ArrayList;
import java.util.List;
import java.util.PriorityQueue;

/**
 * Implementation of Prim's Minimum Spanning Tree algorithm using a binary heap PriorityQueue.
 */
public class PrimMST {

    public static MSTResult findMST(Graph graph) {
        return findMST(graph, 0);
    }

    public static MSTResult findMST(Graph graph, int startVertex) {
        if (graph.isDirected()) {
            throw new IllegalArgumentException("Prim's MST requires an undirected graph.");
        }

        int n = graph.getNumVertices();
        long startTime = System.nanoTime();

        boolean[] visited = new boolean[n];
        List<Edge> mstEdges = new ArrayList<>();
        double totalWeight = 0.0;

        long[] comparisons = new long[]{0};
        long edgeExaminations = 0;
        long heapOperations = 0;

        PriorityQueue<Edge> pq = new PriorityQueue<>(
                Math.max(1, graph.getNumEdges()),
                (e1, e2) -> {
                    comparisons[0]++;
                    return e1.compareTo(e2);
                }
        );

        visited[startVertex] = true;
        int visitedCount = 1;

        for (Edge edge : graph.getNeighbors(startVertex)) {
            edgeExaminations++;
            if (!visited[edge.getDestination()]) {
                pq.offer(edge);
                heapOperations++;
            }
        }

        while (!pq.isEmpty() && visitedCount < n) {
            Edge minEdge = pq.poll();
            heapOperations++;

            int nextVertex = minEdge.getDestination();
            if (visited[nextVertex]) {
                continue;
            }

            visited[nextVertex] = true;
            visitedCount++;
            mstEdges.add(minEdge);
            totalWeight += minEdge.getWeight();

            for (Edge neighborEdge : graph.getNeighbors(nextVertex)) {
                edgeExaminations++;
                if (!visited[neighborEdge.getDestination()]) {
                    pq.offer(neighborEdge);
                    heapOperations++;
                }
            }
        }

        boolean connected = (visitedCount == n);
        long elapsedNanos = System.nanoTime() - startTime;

        return new MSTResult(
                "Prim (PriorityQueue)",
                connected,
                connected ? totalWeight : Double.POSITIVE_INFINITY,
                mstEdges,
                elapsedNanos,
                edgeExaminations,
                comparisons[0],
                heapOperations
        );
    }
}
