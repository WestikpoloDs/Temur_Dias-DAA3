package mst;

import graph.Edge;
import java.util.Collections;
import java.util.List;


public class MSTResult {
    private final String algorithmName;
    private final boolean connected;
    private final double totalWeight;
    private final List<Edge> mstEdges;
    private final long executionTimeNanos;
    private final long edgeExaminations;
    private final long comparisons;
    private final long dsuOrHeapOperations;

    public MSTResult(
            String algorithmName,
            boolean connected,
            double totalWeight,
            List<Edge> mstEdges,
            long executionTimeNanos,
            long edgeExaminations,
            long comparisons,
            long dsuOrHeapOperations) {
        this.algorithmName = algorithmName;
        this.connected = connected;
        this.totalWeight = totalWeight;
        this.mstEdges = Collections.unmodifiableList(mstEdges);
        this.executionTimeNanos = executionTimeNanos;
        this.edgeExaminations = edgeExaminations;
        this.comparisons = comparisons;
        this.dsuOrHeapOperations = dsuOrHeapOperations;
    }

    public String getAlgorithmName() {
        return algorithmName;
    }

    public boolean isConnected() {
        return connected;
    }

    public double getTotalWeight() {
        return totalWeight;
    }

    public List<Edge> getMstEdges() {
        return mstEdges;
    }

    public long getExecutionTimeNanos() {
        return executionTimeNanos;
    }

    public long getEdgeExaminations() {
        return edgeExaminations;
    }

    public long getComparisons() {
        return comparisons;
    }

    public long getDsuOrHeapOperations() {
        return dsuOrHeapOperations;
    }

    public long getTotalOperations() {
        return edgeExaminations + comparisons + dsuOrHeapOperations;
    }

    @Override
    public String toString() {
        return String.format("%s [connected=%b, weight=%.2f, edges=%d, time=%.3f ms, totalOps=%d]",
                algorithmName, connected, totalWeight, mstEdges.size(),
                executionTimeNanos / 1_000_000.0, getTotalOperations());
    }
}
