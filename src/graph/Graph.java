package graph;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Adjacency-list and edge-list representation of a smart city's infrastructure network.
 * Supports both undirected graphs (road construction MST) and directed graphs (one-way routes / service dependencies).
 */
public class Graph {
    private final int numVertices;
    private final boolean directed;
    private final List<List<Edge>> adjacencyList;
    private final List<Edge> edges;
    private final String[] districtNames;

    public Graph(int numVertices, boolean directed) {
        if (numVertices <= 0) {
            throw new IllegalArgumentException("Number of vertices must be positive.");
        }
        this.numVertices = numVertices;
        this.directed = directed;
        this.adjacencyList = new ArrayList<>(numVertices);
        this.districtNames = new String[numVertices];
        for (int i = 0; i < numVertices; i++) {
            this.adjacencyList.add(new ArrayList<>());
            this.districtNames[i] = "District-" + i;
        }
        this.edges = new ArrayList<>();
    }

    public void setDistrictName(int vertex, String name) {
        validateVertex(vertex);
        this.districtNames[vertex] = name;
    }

    public String getDistrictName(int vertex) {
        validateVertex(vertex);
        return this.districtNames[vertex];
    }

    public void addEdge(int u, int v, double weight) {
        validateVertex(u);
        validateVertex(v);
        Edge forward = new Edge(u, v, weight);
        adjacencyList.get(u).add(forward);
        edges.add(forward);
        if (!directed && u != v) {
            Edge backward = new Edge(v, u, weight);
            adjacencyList.get(v).add(backward);
        }
    }

    public int getNumVertices() {
        return numVertices;
    }

    public int getNumEdges() {
        return edges.size();
    }

    public boolean isDirected() {
        return directed;
    }

    public List<Edge> getNeighbors(int vertex) {
        validateVertex(vertex);
        return Collections.unmodifiableList(adjacencyList.get(vertex));
    }

    public List<Edge> getEdges() {
        return Collections.unmodifiableList(edges);
    }

    /**
     * Creates the transpose (reversed edges) of a directed graph, used in Kosaraju's algorithm.
     */
    public Graph getTranspose() {
        if (!directed) {
            throw new IllegalStateException("Transpose is only applicable to directed graphs.");
        }
        Graph transposed = new Graph(numVertices, true);
        for (int i = 0; i < numVertices; i++) {
            transposed.setDistrictName(i, this.districtNames[i]);
        }
        for (Edge e : edges) {
            transposed.addEdge(e.getDestination(), e.getSource(), e.getWeight());
        }
        return transposed;
    }

    private void validateVertex(int v) {
        if (v < 0 || v >= numVertices) {
            throw new IndexOutOfBoundsException("Vertex " + v + " is out of bounds [0, " + (numVertices - 1) + "].");
        }
    }
}
