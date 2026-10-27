package connectivity;

import graph.Edge;
import graph.Graph;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Deque;
import java.util.List;


public class StronglyConnectedComponents {

    public static class SCCResult {
        private final String algorithmName;
        private final List<List<Integer>> components;
        private final int[] componentId;
        private final long executionTimeNanos;
        private final long vertexVisits;
        private final long edgeExaminations;
        private final long stackOperations;

        public SCCResult(
                String algorithmName,
                List<List<Integer>> components,
                int[] componentId,
                long executionTimeNanos,
                long vertexVisits,
                long edgeExaminations,
                long stackOperations) {
            this.algorithmName = algorithmName;
            this.components = Collections.unmodifiableList(components);
            this.componentId = componentId;
            this.executionTimeNanos = executionTimeNanos;
            this.vertexVisits = vertexVisits;
            this.edgeExaminations = edgeExaminations;
            this.stackOperations = stackOperations;
        }

        public String getAlgorithmName() {
            return algorithmName;
        }

        public List<List<Integer>> getComponents() {
            return components;
        }

        public int getNumComponents() {
            return components.size();
        }

        public int[] getComponentId() {
            return componentId;
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

        public long getStackOperations() {
            return stackOperations;
        }

        public long getTotalOperations() {
            return vertexVisits + edgeExaminations + stackOperations;
        }
    }


    public static SCCResult findSCCTarjan(Graph graph) {
        if (!graph.isDirected()) {
            throw new IllegalArgumentException("SCC requires a directed graph.");
        }

        int n = graph.getNumVertices();
        long startTime = System.nanoTime();

        int[] disc = new int[n];
        int[] low = new int[n];
        int[] componentId = new int[n];
        boolean[] onStack = new boolean[n];
        Arrays.fill(disc, -1);
        Arrays.fill(componentId, -1);

        Deque<Integer> stack = new ArrayDeque<>();
        List<List<Integer>> components = new ArrayList<>();

        long[] stats = new long[]{0, 0, 0, 0};

        for (int i = 0; i < n; i++) {
            if (disc[i] == -1) {
                tarjanDFS(i, graph, disc, low, onStack, stack, components, componentId, stats);
            }
        }

        long elapsedNanos = System.nanoTime() - startTime;
        return new SCCResult(
                "Tarjan SCC",
                components,
                componentId,
                elapsedNanos,
                stats[1],
                stats[2],
                stats[3]
        );
    }

    private static void tarjanDFS(
            int u,
            Graph graph,
            int[] disc,
            int[] low,
            boolean[] onStack,
            Deque<Integer> stack,
            List<List<Integer>> components,
            int[] componentId,
            long[] stats) {
        disc[u] = low[u] = (int) (stats[0]++);
        stats[1]++; 
        stack.push(u);
        onStack[u] = true;
        stats[3]++; 

        for (Edge e : graph.getNeighbors(u)) {
            stats[2]++; 
            int v = e.getDestination();
            if (disc[v] == -1) {
                tarjanDFS(v, graph, disc, low, onStack, stack, components, componentId, stats);
                low[u] = Math.min(low[u], low[v]);
            } else if (onStack[v]) {
                low[u] = Math.min(low[u], disc[v]);
            }
        }

        if (low[u] == disc[u]) {
            List<Integer> scc = new ArrayList<>();
            int compIdx = components.size();
            while (true) {
                int w = stack.pop();
                stats[3]++; // stack pop
                onStack[w] = false;
                componentId[w] = compIdx;
                scc.add(w);
                if (w == u) {
                    break;
                }
            }
            Collections.sort(scc);
            components.add(scc);
        }
    }

    public static SCCResult findSCCKosaraju(Graph graph) {
        if (!graph.isDirected()) {
            throw new IllegalArgumentException("SCC requires a directed graph.");
        }

        int n = graph.getNumVertices();
        long startTime = System.nanoTime();

        boolean[] visited = new boolean[n];
        Deque<Integer> finishStack = new ArrayDeque<>();
        long[] stats = new long[]{0, 0, 0};

        for (int i = 0; i < n; i++) {
            if (!visited[i]) {
                kosarajuPass1(i, graph, visited, finishStack, stats);
            }
        }

        Graph transposed = graph.getTranspose();
        stats[1] += graph.getNumEdges();

        Arrays.fill(visited, false);
        List<List<Integer>> components = new ArrayList<>();
        int[] componentId = new int[n];
        Arrays.fill(componentId, -1);

        while (!finishStack.isEmpty()) {
            int u = finishStack.pop();
            stats[2]++; // stack pop
            if (!visited[u]) {
                List<Integer> scc = new ArrayList<>();
                int compIdx = components.size();
                kosarajuPass2(u, transposed, visited, scc, componentId, compIdx, stats);
                Collections.sort(scc);
                components.add(scc);
            }
        }

        long elapsedNanos = System.nanoTime() - startTime;
        return new SCCResult(
                "Kosaraju SCC",
                components,
                componentId,
                elapsedNanos,
                stats[0],
                stats[1],
                stats[2]
        );
    }

    private static void kosarajuPass1(
            int u,
            Graph graph,
            boolean[] visited,
            Deque<Integer> finishStack,
            long[] stats) {
        visited[u] = true;
        stats[0]++;
        for (Edge e : graph.getNeighbors(u)) {
            stats[1]++;
            int v = e.getDestination();
            if (!visited[v]) {
                kosarajuPass1(v, graph, visited, finishStack, stats);
            }
        }
        finishStack.push(u);
        stats[2]++;
    }

    private static void kosarajuPass2(
            int u,
            Graph transposed,
            boolean[] visited,
            List<Integer> scc,
            int[] componentId,
            int compIdx,
            long[] stats) {
        visited[u] = true;
        stats[0]++;
        componentId[u] = compIdx;
        scc.add(u);
        for (Edge e : transposed.getNeighbors(u)) {
            stats[1]++;
            int v = e.getDestination();
            if (!visited[v]) {
                kosarajuPass2(v, transposed, visited, scc, componentId, compIdx, stats);
            }
        }
    }
}
