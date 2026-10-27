import connectivity.StronglyConnectedComponents;
import connectivity.StronglyConnectedComponents.SCCResult;
import connectivity.TopologicalSort;
import connectivity.TopologicalSort.TopologicalResult;
import graph.Graph;
import graph.GraphGenerator;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Locale;
import mst.KruskalMST;
import mst.MSTResult;
import mst.PrimMST;
import shortestpath.DijkstraShortestPath;
import shortestpath.DijkstraShortestPath.ShortestPathResult;


public class Main {

    private static final long FIXED_SEED = 202603L;
    private static final int REPETITIONS = 5;
    private static final int WARMUP_RUNS = 3;

    private static final String[] CONFIG_NAMES = {"Small", "Medium", "Large"};
    private static final int[] VERTEX_COUNTS = {10, 100, 1000};
    private static final int[] EDGE_COUNTS = {30, 500, 5000};

    public static void main(String[] args) throws IOException {
        Locale.setDefault(Locale.US);

        boolean testsPassed = SmartCityTestSuite.runAllTests();
        if (!testsPassed) {
            System.err.println("Verification tests failed! Aborting benchmark.");
            System.exit(1);
        }

        File tablesDir = new File("results/tables");
        if (!tablesDir.exists()) {
            tablesDir.mkdirs();
        }

        System.out.println("======================================================================");
        System.out.println("  SMART CITY INFRASTRUCTURE — EXPERIMENTAL BENCHMARK (5 REPETITIONS)");
        System.out.println("  Fixed Random Seed: " + FIXED_SEED);
        System.out.println("======================================================================\n");

        runMSTAndDSUBenchmarks();
        runConnectivityBenchmarks();
        runDijkstraBenchmarks();
        runScalingSweepForPlots();

        System.out.println("\nAll benchmark tables saved to results/tables/.");
    }


    private static void runMSTAndDSUBenchmarks() throws IOException {
        System.out.println("--- Task 1: Minimum Spanning Tree (Kruskal vs. Prim & DSU Optimization) ---");
        System.out.printf("%-8s %-6s %-6s | %-14s %-14s %-12s %-12s | %-14s %-12s %-12s%n",
                "Graph", "V", "E", "Kruskal(us)", "Prim(us)", "KruskalOps", "PrimOps",
                "NaiveDSU(us)", "OptDSUOps", "NaiveDSUOps");

        try (PrintWriter csv = new PrintWriter(new FileWriter("results/tables/mst_comparison.csv"));
             PrintWriter md = new PrintWriter(new FileWriter("results/tables/mst_comparison.md"))) {

            csv.println("Graph,V,E,MST_Weight,Kruskal_Time_us,Kruskal_EdgeExams,Kruskal_Comparisons,Kruskal_DSUOps,Kruskal_TotalOps,Prim_Time_us,Prim_EdgeExams,Prim_Comparisons,Prim_HeapOps,Prim_TotalOps,KruskalNaive_Time_us,KruskalNaive_DSUOps");

            md.println("| Graph | V | E | MST Weight | Kruskal Time (µs) | Kruskal Edge Exams | Kruskal Comparisons | Kruskal DSU Ops | Prim Time (µs) | Prim Edge Exams | Prim Comparisons | Prim Heap Ops | Naive DSU Time (µs) | Naive DSU Ops |");
            md.println("| :--- | ---: | ---: | ---: | ---: | ---: | ---: | ---: | ---: | ---: | ---: | ---: | ---: | ---: |");

            for (int i = 0; i < CONFIG_NAMES.length; i++) {
                String name = CONFIG_NAMES[i];
                int v = VERTEX_COUNTS[i];
                int e = EDGE_COUNTS[i];
                Graph graph = GraphGenerator.generateConnectedUndirectedGraph(v, e, FIXED_SEED + i);

                for (int w = 0; w < WARMUP_RUNS; w++) {
                    KruskalMST.findMST(graph, true);
                    KruskalMST.findMST(graph, false);
                    PrimMST.findMST(graph);
                }

                long kruskalTimeSum = 0;
                long primTimeSum = 0;
                long naiveTimeSum = 0;

                MSTResult lastKruskal = null;
                MSTResult lastPrim = null;
                MSTResult lastNaive = null;

                for (int r = 0; r < REPETITIONS; r++) {
                    lastKruskal = KruskalMST.findMST(graph, true);
                    lastPrim = PrimMST.findMST(graph);
                    lastNaive = KruskalMST.findMST(graph, false);

                    kruskalTimeSum += lastKruskal.getExecutionTimeNanos();
                    primTimeSum += lastPrim.getExecutionTimeNanos();
                    naiveTimeSum += lastNaive.getExecutionTimeNanos();
                }

                double avgKruskalUs = (kruskalTimeSum / (double) REPETITIONS) / 1_000.0;
                double avgPrimUs = (primTimeSum / (double) REPETITIONS) / 1_000.0;
                double avgNaiveUs = (naiveTimeSum / (double) REPETITIONS) / 1_000.0;

                System.out.printf("%-8s %-6d %-6d | %-14.2f %-14.2f %-12d %-12d | %-14.2f %-12d %-12d%n",
                        name, v, e, avgKruskalUs, avgPrimUs,
                        lastKruskal.getTotalOperations(), lastPrim.getTotalOperations(),
                        avgNaiveUs, lastKruskal.getDsuOrHeapOperations(), lastNaive.getDsuOrHeapOperations());

                csv.printf("%s,%d,%d,%.2f,%.2f,%d,%d,%d,%d,%.2f,%d,%d,%d,%d,%.2f,%d%n",
                        name, v, e, lastKruskal.getTotalWeight(),
                        avgKruskalUs, lastKruskal.getEdgeExaminations(), lastKruskal.getComparisons(),
                        lastKruskal.getDsuOrHeapOperations(), lastKruskal.getTotalOperations(),
                        avgPrimUs, lastPrim.getEdgeExaminations(), lastPrim.getComparisons(),
                        lastPrim.getDsuOrHeapOperations(), lastPrim.getTotalOperations(),
                        avgNaiveUs, lastNaive.getDsuOrHeapOperations());

                md.printf("| %s | %d | %d | %.2f | %.2f | %d | %d | %d | %.2f | %d | %d | %d | %.2f | %d |%n",
                        name, v, e, lastKruskal.getTotalWeight(),
                        avgKruskalUs, lastKruskal.getEdgeExaminations(), lastKruskal.getComparisons(),
                        lastKruskal.getDsuOrHeapOperations(),
                        avgPrimUs, lastPrim.getEdgeExaminations(), lastPrim.getComparisons(),
                        lastPrim.getDsuOrHeapOperations(),
                        avgNaiveUs, lastNaive.getDsuOrHeapOperations());
            }
        }
        System.out.println();
    }


    private static void runConnectivityBenchmarks() throws IOException {
        System.out.println("--- Task 2: Connectivity, SCC (Tarjan & Kosaraju) & Topological Sort ---");
        System.out.printf("%-8s %-6s %-6s | %-12s %-12s %-10s | %-12s %-12s %-12s%n",
                "Graph", "V", "E", "Tarjan(us)", "Kosaraju(us)", "SCC_Count",
                "TopoDAG(us)", "TopoCyc(us)", "DAG_Valid");

        try (PrintWriter csv = new PrintWriter(new FileWriter("results/tables/connectivity_comparison.csv"));
             PrintWriter md = new PrintWriter(new FileWriter("results/tables/connectivity_comparison.md"))) {

            csv.println("Graph,V,E,SCC_Count,Tarjan_Time_us,Tarjan_VertexVisits,Tarjan_EdgeExams,Tarjan_StackOps,Tarjan_TotalOps,Kosaraju_Time_us,Kosaraju_VertexVisits,Kosaraju_EdgeExams,Kosaraju_StackOps,Kosaraju_TotalOps,TopoDAG_Time_us,TopoDAG_Ops,TopoDAG_IsDAG,TopoCyclic_Time_us,TopoCyclic_Ops,TopoCyclic_HasCycle");

            md.println("| Graph | V | E | SCC Count | Tarjan Time (µs) | Tarjan Total Ops | Kosaraju Time (µs) | Kosaraju Total Ops | Topo Sort DAG Time (µs) | Topo Sort DAG Ops | Topo Sort Cyclic Time (µs) | Cycle Detected |");
            md.println("| :--- | ---: | ---: | ---: | ---: | ---: | ---: | ---: | ---: | ---: | ---: | :---: |");

            for (int i = 0; i < CONFIG_NAMES.length; i++) {
                String name = CONFIG_NAMES[i];
                int v = VERTEX_COUNTS[i];
                int e = EDGE_COUNTS[i];

                Graph cyclicGraph = GraphGenerator.generateCyclicDirectedGraph(v, e, FIXED_SEED + 100 + i);
                Graph dagGraph = GraphGenerator.generateDAG(v, e, FIXED_SEED + 200 + i);

                for (int w = 0; w < WARMUP_RUNS; w++) {
                    StronglyConnectedComponents.findSCCTarjan(cyclicGraph);
                    StronglyConnectedComponents.findSCCKosaraju(cyclicGraph);
                    TopologicalSort.sortKahn(dagGraph);
                    TopologicalSort.sortKahn(cyclicGraph);
                }

                long tarjanTimeSum = 0;
                long kosarajuTimeSum = 0;
                long topoDagTimeSum = 0;
                long topoCyclicTimeSum = 0;

                SCCResult lastTarjan = null;
                SCCResult lastKosaraju = null;
                TopologicalResult lastTopoDag = null;
                TopologicalResult lastTopoCyclic = null;

                for (int r = 0; r < REPETITIONS; r++) {
                    lastTarjan = StronglyConnectedComponents.findSCCTarjan(cyclicGraph);
                    lastKosaraju = StronglyConnectedComponents.findSCCKosaraju(cyclicGraph);
                    lastTopoDag = TopologicalSort.sortKahn(dagGraph);
                    lastTopoCyclic = TopologicalSort.sortKahn(cyclicGraph);

                    tarjanTimeSum += lastTarjan.getExecutionTimeNanos();
                    kosarajuTimeSum += lastKosaraju.getExecutionTimeNanos();
                    topoDagTimeSum += lastTopoDag.getExecutionTimeNanos();
                    topoCyclicTimeSum += lastTopoCyclic.getExecutionTimeNanos();
                }

                double avgTarjanUs = (tarjanTimeSum / (double) REPETITIONS) / 1_000.0;
                double avgKosarajuUs = (kosarajuTimeSum / (double) REPETITIONS) / 1_000.0;
                double avgTopoDagUs = (topoDagTimeSum / (double) REPETITIONS) / 1_000.0;
                double avgTopoCyclicUs = (topoCyclicTimeSum / (double) REPETITIONS) / 1_000.0;

                System.out.printf("%-8s %-6d %-6d | %-12.2f %-12.2f %-10d | %-12.2f %-12.2f %-12b%n",
                        name, v, e, avgTarjanUs, avgKosarajuUs, lastTarjan.getNumComponents(),
                        avgTopoDagUs, avgTopoCyclicUs, lastTopoDag.isDAG());

                csv.printf("%s,%d,%d,%d,%.2f,%d,%d,%d,%d,%.2f,%d,%d,%d,%d,%.2f,%d,%b,%.2f,%d,%b%n",
                        name, v, e, lastTarjan.getNumComponents(),
                        avgTarjanUs, lastTarjan.getVertexVisits(), lastTarjan.getEdgeExaminations(),
                        lastTarjan.getStackOperations(), lastTarjan.getTotalOperations(),
                        avgKosarajuUs, lastKosaraju.getVertexVisits(), lastKosaraju.getEdgeExaminations(),
                        lastKosaraju.getStackOperations(), lastKosaraju.getTotalOperations(),
                        avgTopoDagUs, lastTopoDag.getTotalOperations(), lastTopoDag.isDAG(),
                        avgTopoCyclicUs, lastTopoCyclic.getTotalOperations(), lastTopoCyclic.hasCycle());

                md.printf("| %s | %d | %d | %d | %.2f | %d | %.2f | %d | %.2f | %d | %.2f | %b |%n",
                        name, v, e, lastTarjan.getNumComponents(),
                        avgTarjanUs, lastTarjan.getTotalOperations(),
                        avgKosarajuUs, lastKosaraju.getTotalOperations(),
                        avgTopoDagUs, lastTopoDag.getTotalOperations(),
                        avgTopoCyclicUs, lastTopoCyclic.hasCycle());
            }
        }
        System.out.println();
    }


    private static void runDijkstraBenchmarks() throws IOException {
        System.out.println("--- Task 3: Dijkstra's Shortest Path Algorithm ---");
        System.out.printf("%-8s %-6s %-6s | %-14s %-12s %-12s %-12s %-12s%n",
                "Graph", "V", "E", "Dijkstra(us)", "Reachable", "EdgeExams", "Relaxations", "HeapOps");

        try (PrintWriter csv = new PrintWriter(new FileWriter("results/tables/dijkstra_comparison.csv"));
             PrintWriter md = new PrintWriter(new FileWriter("results/tables/dijkstra_comparison.md"))) {

            csv.println("Graph,V,E,Reachable_Vertices,Dijkstra_Time_us,Edge_Examinations,Relaxations,Heap_Operations,Comparisons,Total_Operations");

            md.println("| Graph | V | E | Reachable Vertices | Execution Time (µs) | Edge Examinations | Edge Relaxations | Heap Operations | Comparisons | Total Operations |");
            md.println("| :--- | ---: | ---: | ---: | ---: | ---: | ---: | ---: | ---: | ---: |");

            for (int i = 0; i < CONFIG_NAMES.length; i++) {
                String name = CONFIG_NAMES[i];
                int v = VERTEX_COUNTS[i];
                int e = EDGE_COUNTS[i];


                Graph roadNetwork = GraphGenerator.generateConnectedUndirectedGraph(v, e, FIXED_SEED + 300 + i);

                for (int w = 0; w < WARMUP_RUNS; w++) {
                    DijkstraShortestPath.computeShortestPaths(roadNetwork, 0);
                }

                long timeSum = 0;
                ShortestPathResult lastResult = null;

                for (int r = 0; r < REPETITIONS; r++) {
                    lastResult = DijkstraShortestPath.computeShortestPaths(roadNetwork, 0);
                    timeSum += lastResult.getExecutionTimeNanos();
                }

                int reachable = 0;
                for (int node = 0; node < v; node++) {
                    if (lastResult.isReachable(node)) {
                        reachable++;
                    }
                }

                double avgTimeUs = (timeSum / (double) REPETITIONS) / 1_000.0;

                System.out.printf("%-8s %-6d %-6d | %-14.2f %-12d %-12d %-12d %-12d%n",
                        name, v, e, avgTimeUs, reachable,
                        lastResult.getEdgeExaminations(), lastResult.getRelaxations(),
                        lastResult.getHeapOperations());

                csv.printf("%s,%d,%d,%d,%.2f,%d,%d,%d,%d,%d%n",
                        name, v, e, reachable, avgTimeUs,
                        lastResult.getEdgeExaminations(), lastResult.getRelaxations(),
                        lastResult.getHeapOperations(), lastResult.getComparisons(),
                        lastResult.getTotalOperations());

                md.printf("| %s | %d | %d | %d | %.2f | %d | %d | %d | %d | %d |%n",
                        name, v, e, reachable, avgTimeUs,
                        lastResult.getEdgeExaminations(), lastResult.getRelaxations(),
                        lastResult.getHeapOperations(), lastResult.getComparisons(),
                        lastResult.getTotalOperations());
            }
        }
    }


    private static void runScalingSweepForPlots() throws IOException {
        try (PrintWriter csv = new PrintWriter(new FileWriter("results/tables/scaling_sweep.csv"))) {
            csv.println("SweepType,V,E,Kruskal_us,Prim_us,Tarjan_us,Kosaraju_us,TopoDAG_us,Dijkstra_us");

      
            int[][] points = {
                {10, 30},
                {50, 200},
                {100, 500},
                {250, 1250},
                {500, 2500},
                {1000, 5000}
            };

            for (int[] pt : points) {
                int v = pt[0];
                int e = pt[1];
                Graph ug = GraphGenerator.generateConnectedUndirectedGraph(v, e, FIXED_SEED + v);
                Graph dg = GraphGenerator.generateCyclicDirectedGraph(v, e, FIXED_SEED + v + 1000);
                Graph dag = GraphGenerator.generateDAG(v, e, FIXED_SEED + v + 2000);

                for (int w = 0; w < WARMUP_RUNS; w++) {
                    KruskalMST.findMST(ug);
                    PrimMST.findMST(ug);
                    StronglyConnectedComponents.findSCCTarjan(dg);
                    StronglyConnectedComponents.findSCCKosaraju(dg);
                    TopologicalSort.sortKahn(dag);
                    DijkstraShortestPath.computeShortestPaths(ug, 0);
                }

                long kSum = 0, pSum = 0, tSum = 0, kosSum = 0, topoSum = 0, dSum = 0;
                for (int r = 0; r < REPETITIONS; r++) {
                    kSum += KruskalMST.findMST(ug).getExecutionTimeNanos();
                    pSum += PrimMST.findMST(ug).getExecutionTimeNanos();
                    tSum += StronglyConnectedComponents.findSCCTarjan(dg).getExecutionTimeNanos();
                    kosSum += StronglyConnectedComponents.findSCCKosaraju(dg).getExecutionTimeNanos();
                    topoSum += TopologicalSort.sortKahn(dag).getExecutionTimeNanos();
                    dSum += DijkstraShortestPath.computeShortestPaths(ug, 0).getExecutionTimeNanos();
                }

                csv.printf("Combined,%d,%d,%.2f,%.2f,%.2f,%.2f,%.2f,%.2f%n",
                        v, e,
                        (kSum / (double) REPETITIONS) / 1000.0,
                        (pSum / (double) REPETITIONS) / 1000.0,
                        (tSum / (double) REPETITIONS) / 1000.0,
                        (kosSum / (double) REPETITIONS) / 1000.0,
                        (topoSum / (double) REPETITIONS) / 1000.0,
                        (dSum / (double) REPETITIONS) / 1000.0);
            }
        }
    }
}
