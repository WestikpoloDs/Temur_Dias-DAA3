package mst;

import dsu.DisjointSetUnion;
import graph.Edge;
import graph.Graph;
import java.util.ArrayList;
import java.util.List;


public class KruskalMST {

    public static MSTResult findMST(Graph graph) {
        return findMST(graph, true);
    }

    public static MSTResult findMST(Graph graph, boolean useOptimizedDSU) {
        if (graph.isDirected()) {
            throw new IllegalArgumentException("Kruskal's MST requires an undirected graph.");
        }

        int n = graph.getNumVertices();
        long startTime = System.nanoTime();

        List<Edge> sortedEdges = new ArrayList<>(graph.getEdges());
        long[] comparisonCounter = new long[]{0};

        sortedEdges.sort((e1, e2) -> {
            comparisonCounter[0]++;
            return e1.compareTo(e2);
        });

        DisjointSetUnion dsu = new DisjointSetUnion(n, useOptimizedDSU);
        List<Edge> mstEdges = new ArrayList<>();
        double totalWeight = 0.0;
        long edgeExaminations = 0;

        for (Edge edge : sortedEdges) {
            if (mstEdges.size() == n - 1) {
                break;
            }
            edgeExaminations++;
            int u = edge.getSource();
            int v = edge.getDestination();
            if (dsu.union(u, v)) {
                mstEdges.add(edge);
                totalWeight += edge.getWeight();
            }
        }

        boolean connected = (n == 1) || (mstEdges.size() == n - 1 && dsu.getNumComponents() == 1);
        long elapsedNanos = System.nanoTime() - startTime;

        return new MSTResult(
                useOptimizedDSU ? "Kruskal (Optimized DSU)" : "Kruskal (Naive DSU)",
                connected,
                connected ? totalWeight : Double.POSITIVE_INFINITY,
                mstEdges,
                elapsedNanos,
                edgeExaminations,
                comparisonCounter[0],
                dsu.getTotalOperations()
        );
    }
}
