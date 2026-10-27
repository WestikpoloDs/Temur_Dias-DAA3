import connectivity.StronglyConnectedComponents;
import connectivity.StronglyConnectedComponents.SCCResult;
import connectivity.TopologicalSort;
import connectivity.TopologicalSort.TopologicalResult;
import dsu.DisjointSetUnion;
import graph.Edge;
import graph.Graph;
import graph.GraphGenerator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import mst.KruskalMST;
import mst.MSTResult;
import mst.PrimMST;
import shortestpath.DijkstraShortestPath;
import shortestpath.DijkstraShortestPath.ShortestPathResult;

/**
 * Comprehensive verification suite covering all 7 required graph scenarios
 * and all 5 correctness assertions specified in Section 6 of Assignment 3.
 */
public class SmartCityTestSuite {

    private static int passedTests = 0;
    private static int failedTests = 0;

    public static boolean runAllTests() {
        passedTests = 0;
        failedTests = 0;
        System.out.println("======================================================================");
        System.out.println("  SMART CITY INFRASTRUCTURE — AUTOMATED VERIFICATION TEST SUITE");
        System.out.println("======================================================================");

        test1_SmallGraphs();
        test2_DisconnectedGraphs();
        test3_GraphsContainingCycles();
        test4_MultipleValidMSTs();
        test5_UnreachableVertices();
        test6_DuplicateEdgeWeights();
        test7_SingleVertexGraphs();
        test8_RandomizedCrossValidation();

        System.out.println("----------------------------------------------------------------------");
        System.out.printf("  Test Summary: %d PASSED, %d FAILED%n", passedTests, failedTests);
        System.out.println("======================================================================\n");
        return failedTests == 0;
    }

    private static void check(String testName, boolean condition) {
        if (condition) {
            passedTests++;
            System.out.printf("  [PASS] %s%n", testName);
        } else {
            failedTests++;
            System.out.printf("  [FAIL] %s%n", testName);
        }
    }

    /**
     * Test 1: Small graphs (undirected MST, DAG topological sort, SCC, and Dijkstra).
     */
    private static void test1_SmallGraphs() {
        System.out.println("\n[Test Group 1] Small Graphs (5 districts)");
        Graph roadGraph = new Graph(5, false);
        roadGraph.addEdge(0, 1, 4.0);
        roadGraph.addEdge(0, 2, 2.0);
        roadGraph.addEdge(1, 2, 1.0);
        roadGraph.addEdge(1, 3, 5.0);
        roadGraph.addEdge(2, 3, 8.0);
        roadGraph.addEdge(2, 4, 10.0);
        roadGraph.addEdge(3, 4, 2.0);

        MSTResult kruskal = KruskalMST.findMST(roadGraph);
        MSTResult prim = PrimMST.findMST(roadGraph);

        // MST edges: (1,2,1), (0,2,2), (3,4,2), (1,3,5) -> weight = 10.0
        check("Small graph: Kruskal detects connected graph", kruskal.isConnected());
        check("Small graph: Prim detects connected graph", prim.isConnected());
        check("Small graph: Kruskal and Prim return exact same MST weight (10.0)",
                Math.abs(kruskal.getTotalWeight() - 10.0) < 1e-6 &&
                Math.abs(kruskal.getTotalWeight() - prim.getTotalWeight()) < 1e-6);

        ShortestPathResult sp = DijkstraShortestPath.computeShortestPaths(roadGraph, 0);
        // 0 -> 2 (2.0) -> 1 (3.0) -> 3 (8.0) -> 4 (10.0)
        check("Small graph: Dijkstra shortest distance 0 -> 4 is 10.0",
                Math.abs(sp.getDistanceTo(4) - 10.0) < 1e-6);
        check("Small graph: Dijkstra shortest path 0 -> 4 is [0, 2, 1, 3, 4]",
                sp.getPathTo(4).equals(List.of(0, 2, 1, 3, 4)));
    }

    /**
     * Test 2: Disconnected graphs (MST connectivity detection and DSU component tracking).
     */
    private static void test2_DisconnectedGraphs() {
        System.out.println("\n[Test Group 2] Disconnected Graphs");
        Graph disconnected = new Graph(6, false);
        // Component A: {0, 1, 2}
        disconnected.addEdge(0, 1, 3.0);
        disconnected.addEdge(1, 2, 4.0);
        // Component B: {3, 4, 5}
        disconnected.addEdge(3, 4, 2.0);
        disconnected.addEdge(4, 5, 6.0);

        MSTResult kruskal = KruskalMST.findMST(disconnected);
        MSTResult prim = PrimMST.findMST(disconnected);

        check("Disconnected graph: Kruskal correctly reports isConnected() == false", !kruskal.isConnected());
        check("Disconnected graph: Prim correctly reports isConnected() == false", !prim.isConnected());

        DisjointSetUnion dsu = new DisjointSetUnion(6);
        for (Edge e : disconnected.getEdges()) {
            dsu.union(e.getSource(), e.getDestination());
        }
        check("Disconnected graph: DSU identifies exactly 2 connected components of size 3",
                dsu.getNumComponents() == 2 && dsu.getComponentSize(0) == 3 && dsu.getComponentSize(3) == 3);
    }

    /**
     * Test 3: Graphs containing cycles (Topological sort cycle detection & SCC partition verification).
     */
    private static void test3_GraphsContainingCycles() {
        System.out.println("\n[Test Group 3] Graphs Containing Cycles (Topological Sort & SCC)");
        Graph cyclic = new Graph(6, true);
        // SCC 1: 0 -> 1 -> 2 -> 0
        cyclic.addEdge(0, 1, 1.0);
        cyclic.addEdge(1, 2, 1.0);
        cyclic.addEdge(2, 0, 1.0);
        // Bridge: 2 -> 3
        cyclic.addEdge(2, 3, 1.0);
        // SCC 2: 3 -> 4 -> 3
        cyclic.addEdge(3, 4, 1.0);
        cyclic.addEdge(4, 3, 1.0);
        // SCC 3: 4 -> 5 (singleton 5)
        cyclic.addEdge(4, 5, 1.0);

        TopologicalResult topo = TopologicalSort.sortKahn(cyclic);
        check("Cyclic graph: Topological ordering detects cycle (isDAG == false, hasCycle == true)",
                !topo.isDAG() && topo.hasCycle() && topo.getTopologicalOrder().isEmpty());

        SCCResult tarjan = StronglyConnectedComponents.findSCCTarjan(cyclic);
        SCCResult kosaraju = StronglyConnectedComponents.findSCCKosaraju(cyclic);

        check("Cyclic graph: Tarjan and Kosaraju both find 3 SCCs",
                tarjan.getNumComponents() == 3 && kosaraju.getNumComponents() == 3);
        check("Cyclic graph: Every vertex belongs to exactly one SCC (Tarjan & Kosaraju)",
                verifyExactSCCPartition(cyclic.getNumVertices(), tarjan) &&
                verifyExactSCCPartition(cyclic.getNumVertices(), kosaraju));
    }

    /**
     * Test 4: Graphs with multiple valid MSTs (e.g., 4-cycle with identical edge weights).
     */
    private static void test4_MultipleValidMSTs() {
        System.out.println("\n[Test Group 4] Graphs with Multiple Valid MSTs");
        Graph square = new Graph(4, false);
        square.addEdge(0, 1, 5.0);
        square.addEdge(1, 2, 5.0);
        square.addEdge(2, 3, 5.0);
        square.addEdge(3, 0, 5.0);
        square.addEdge(0, 2, 5.0); // diagonal also weight 5.0

        MSTResult kruskal = KruskalMST.findMST(square);
        MSTResult prim = PrimMST.findMST(square);

        check("Multiple valid MSTs: Both Kruskal and Prim select V-1 = 3 edges",
                kruskal.getMstEdges().size() == 3 && prim.getMstEdges().size() == 3);
        check("Multiple valid MSTs: Kruskal and Prim return identical total weight (15.0)",
                Math.abs(kruskal.getTotalWeight() - 15.0) < 1e-6 &&
                Math.abs(prim.getTotalWeight() - 15.0) < 1e-6);
    }

    /**
     * Test 5: Graphs with unreachable vertices (Dijkstra shortest paths).
     */
    private static void test5_UnreachableVertices() {
        System.out.println("\n[Test Group 5] Graphs with Unreachable Vertices");
        Graph directed = new Graph(5, true);
        directed.addEdge(0, 1, 3.0);
        directed.addEdge(1, 2, 4.0);
        // Vertex 3 and 4 only have edges between themselves or pointing back to 0
        directed.addEdge(3, 4, 2.0);
        directed.addEdge(4, 0, 1.0);

        ShortestPathResult sp = DijkstraShortestPath.computeShortestPaths(directed, 0);
        check("Unreachable vertices: Reachable vertex 2 has distance 7.0",
                sp.isReachable(2) && Math.abs(sp.getDistanceTo(2) - 7.0) < 1e-6);
        check("Unreachable vertices: Unreachable vertices 3 and 4 have distance +Infinity and empty path",
                !sp.isReachable(3) && Double.isInfinite(sp.getDistanceTo(3)) && sp.getPathTo(3).isEmpty() &&
                !sp.isReachable(4) && Double.isInfinite(sp.getDistanceTo(4)) && sp.getPathTo(4).isEmpty());
    }

    /**
     * Test 6: Graphs containing duplicate edge weights.
     */
    private static void test6_DuplicateEdgeWeights() {
        System.out.println("\n[Test Group 6] Graphs Containing Duplicate Edge Weights");
        Graph dupGraph = new Graph(6, false);
        dupGraph.addEdge(0, 1, 2.5);
        dupGraph.addEdge(1, 2, 2.5);
        dupGraph.addEdge(2, 3, 2.5);
        dupGraph.addEdge(3, 4, 7.0);
        dupGraph.addEdge(4, 5, 7.0);
        dupGraph.addEdge(0, 5, 7.0);
        dupGraph.addEdge(1, 4, 2.5);

        MSTResult kruskal = KruskalMST.findMST(dupGraph);
        MSTResult prim = PrimMST.findMST(dupGraph);

        // 4 edges of weight 2.5 + 1 edge of weight 7.0 = 17.0
        check("Duplicate edge weights: Kruskal and Prim both return MST weight 17.0",
                Math.abs(kruskal.getTotalWeight() - 17.0) < 1e-6 &&
                Math.abs(prim.getTotalWeight() - 17.0) < 1e-6);

        ShortestPathResult sp = DijkstraShortestPath.computeShortestPaths(dupGraph, 0);
        // 0 -> 1 (2.5) -> 4 (5.0) -> 5 (12.0), whereas direct 0 -> 5 is 7.0
        check("Duplicate edge weights: Dijkstra finds optimal shortest path 0 -> 5 of weight 7.0",
                Math.abs(sp.getDistanceTo(5) - 7.0) < 1e-6);
    }

    /**
     * Test 7: Graphs with a single vertex (edge case V = 1, E = 0).
     */
    private static void test7_SingleVertexGraphs() {
        System.out.println("\n[Test Group 7] Graphs with a Single Vertex (V = 1, E = 0)");
        Graph singleUndirected = new Graph(1, false);
        MSTResult kruskal = KruskalMST.findMST(singleUndirected);
        MSTResult prim = PrimMST.findMST(singleUndirected);

        check("Single vertex: Kruskal and Prim report connected with 0.0 weight and 0 edges",
                kruskal.isConnected() && prim.isConnected() &&
                Math.abs(kruskal.getTotalWeight()) < 1e-6 &&
                Math.abs(prim.getTotalWeight()) < 1e-6 &&
                kruskal.getMstEdges().isEmpty() && prim.getMstEdges().isEmpty());

        Graph singleDirected = new Graph(1, true);
        TopologicalResult topo = TopologicalSort.sortKahn(singleDirected);
        SCCResult scc = StronglyConnectedComponents.findSCCTarjan(singleDirected);
        ShortestPathResult sp = DijkstraShortestPath.computeShortestPaths(singleDirected, 0);

        check("Single vertex: Topological sort returns [0] and isDAG == true",
                topo.isDAG() && topo.getTopologicalOrder().equals(List.of(0)));
        check("Single vertex: SCC returns 1 component [[0]]",
                scc.getNumComponents() == 1 && verifyExactSCCPartition(1, scc));
        check("Single vertex: Dijkstra distance to self is 0.0 with path [0]",
                Math.abs(sp.getDistanceTo(0)) < 1e-6 && sp.getPathTo(0).equals(List.of(0)));
    }

    /**
     * Test 8: Randomized cross-validation across Small, Medium, and Large graphs.
     */
    private static void test8_RandomizedCrossValidation() {
        System.out.println("\n[Test Group 8] Cross-Validation on Generated Small/Medium/Large Graphs");
        int[][] configs = {{10, 30}, {100, 500}, {1000, 5000}};
        boolean allMSTMatch = true;
        boolean allDAGValid = true;
        boolean allSCCValid = true;

        for (int[] cfg : configs) {
            int v = cfg[0];
            int e = cfg[1];
            Graph ug = GraphGenerator.generateConnectedUndirectedGraph(v, e, 42L + v);
            MSTResult k = KruskalMST.findMST(ug);
            MSTResult p = PrimMST.findMST(ug);
            if (!k.isConnected() || !p.isConnected() || Math.abs(k.getTotalWeight() - p.getTotalWeight()) > 1e-5) {
                allMSTMatch = false;
            }

            Graph dag = GraphGenerator.generateDAG(v, e, 42L + v);
            TopologicalResult topo = TopologicalSort.sortKahn(dag);
            if (!topo.isDAG() || !TopologicalSort.isValidTopologicalOrder(dag, topo.getTopologicalOrder())) {
                allDAGValid = false;
            }

            Graph dg = GraphGenerator.generateCyclicDirectedGraph(v, e, 42L + v);
            SCCResult tarjan = StronglyConnectedComponents.findSCCTarjan(dg);
            SCCResult kosaraju = StronglyConnectedComponents.findSCCKosaraju(dg);
            if (!verifyExactSCCPartition(v, tarjan) ||
                !verifyExactSCCPartition(v, kosaraju) ||
                tarjan.getNumComponents() != kosaraju.getNumComponents()) {
                allSCCValid = false;
            }
        }

        check("All generated connected graphs (10, 100, 1000 V): Kruskal and Prim MST weights match", allMSTMatch);
        check("All generated DAGs (10, 100, 1000 V): Topological ordering is valid for every edge", allDAGValid);
        check("All generated directed graphs (10, 100, 1000 V): Every vertex belongs to exactly one SCC", allSCCValid);
    }

    private static boolean verifyExactSCCPartition(int numVertices, SCCResult sccResult) {
        Set<Integer> seen = new HashSet<>();
        int totalCount = 0;
        for (List<Integer> comp : sccResult.getComponents()) {
            if (comp.isEmpty()) {
                return false;
            }
            for (int v : comp) {
                if (v < 0 || v >= numVertices || !seen.add(v)) {
                    return false;
                }
                totalCount++;
            }
        }
        return totalCount == numVertices && seen.size() == numVertices;
    }
}
