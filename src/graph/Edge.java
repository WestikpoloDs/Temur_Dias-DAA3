package graph;

import java.util.Objects;


public class Edge implements Comparable<Edge> {
    private final int source;
    private final int destination;
    private final double weight;

    public Edge(int source, int destination, double weight) {
        if (source < 0 || destination < 0) {
            throw new IllegalArgumentException("Vertex indices must be non-negative.");
        }
        this.source = source;
        this.destination = destination;
        this.weight = weight;
    }

    public int getSource() {
        return source;
    }

    public int getDestination() {
        return destination;
    }

    public double getWeight() {
        return weight;
    }

    @Override
    public int compareTo(Edge other) {
        int weightComparison = Double.compare(this.weight, other.weight);
        if (weightComparison != 0) {
            return weightComparison;
        }
        int srcComparison = Integer.compare(this.source, other.source);
        if (srcComparison != 0) {
            return srcComparison;
        }
        return Integer.compare(this.destination, other.destination);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Edge)) return false;
        Edge edge = (Edge) o;
        return source == edge.source &&
               destination == edge.destination &&
               Double.compare(edge.weight, weight) == 0;
    }

    @Override
    public int hashCode() {
        return Objects.hash(source, destination, weight);
    }

    @Override
    public String toString() {
        return String.format("(%d -> %d, w=%.2f)", source, destination, weight);
    }
}
