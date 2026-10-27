package dsu;

/**
 * Disjoint Set Union (Union-Find) data structure.
 * Supports both optimized mode (path compression + union by rank & size) and naive mode (for benchmarking comparison).
 * Tracks operation counts for experimental analysis.
 */
public class DisjointSetUnion {
    private final int[] parent;
    private final int[] rank;
    private final int[] size;
    private int numComponents;
    private final boolean useOptimizations;

    // Instrumentation counters
    private long findCalls;
    private long parentTraversals;
    private long unionCalls;
    private long successfulUnions;

    public DisjointSetUnion(int n) {
        this(n, true);
    }

    public DisjointSetUnion(int n, boolean useOptimizations) {
        if (n <= 0) {
            throw new IllegalArgumentException("DSU size must be positive.");
        }
        this.parent = new int[n];
        this.rank = new int[n];
        this.size = new int[n];
        this.numComponents = n;
        this.useOptimizations = useOptimizations;

        for (int i = 0; i < n; i++) {
            parent[i] = i;
            rank[i] = 0;
            size[i] = 1;
        }
    }

    /**
     * Finds the representative of the set containing x.
     * Uses path compression when optimizations are enabled.
     */
    public int find(int x) {
        validate(x);
        findCalls++;
        if (useOptimizations) {
            return findWithCompression(x);
        } else {
            return findNaive(x);
        }
    }

    private int findWithCompression(int x) {
        parentTraversals++;
        if (parent[x] != x) {
            parent[x] = findWithCompression(parent[x]);
        }
        return parent[x];
    }

    private int findNaive(int x) {
        int curr = x;
        while (parent[curr] != curr) {
            parentTraversals++;
            curr = parent[curr];
        }
        parentTraversals++;
        return curr;
    }

    /**
     * Unites the sets containing u and v.
     * Uses union by rank (and tracks component size) when optimizations are enabled.
     *
     * @return true if u and v were in different sets and were merged; false if already in the same set.
     */
    public boolean union(int u, int v) {
        unionCalls++;
        int rootU = find(u);
        int rootV = find(v);

        if (rootU == rootV) {
            return false;
        }

        if (useOptimizations) {
            if (rank[rootU] < rank[rootV]) {
                parent[rootU] = rootV;
                size[rootV] += size[rootU];
            } else if (rank[rootU] > rank[rootV]) {
                parent[rootV] = rootU;
                size[rootU] += size[rootV];
            } else {
                parent[rootV] = rootU;
                size[rootU] += size[rootV];
                rank[rootU]++;
            }
        } else {
            parent[rootV] = rootU;
            size[rootU] += size[rootV];
        }

        numComponents--;
        successfulUnions++;
        return true;
    }

    public boolean isConnected(int u, int v) {
        return find(u) == find(v);
    }

    public int getComponentSize(int x) {
        return size[find(x)];
    }

    public int getNumComponents() {
        return numComponents;
    }

    public long getFindCalls() {
        return findCalls;
    }

    public long getParentTraversals() {
        return parentTraversals;
    }

    public long getUnionCalls() {
        return unionCalls;
    }

    public long getSuccessfulUnions() {
        return successfulUnions;
    }

    public long getTotalOperations() {
        return parentTraversals + unionCalls;
    }

    private void validate(int x) {
        if (x < 0 || x >= parent.length) {
            throw new IndexOutOfBoundsException("Element " + x + " out of bounds [0, " + (parent.length - 1) + "].");
        }
    }
}
