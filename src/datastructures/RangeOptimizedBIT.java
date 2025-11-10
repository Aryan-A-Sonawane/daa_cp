package datastructures;

import java.util.List;

public class RangeOptimizedBIT {
    private final int n;
    private FenwickTree tree1; // For range updates (difference array)
    private FenwickTree tree2; // For position correction

    public RangeOptimizedBIT(int n) {
        this.n = n;
        this.tree1 = new FenwickTree(n);
        this.tree2 = new FenwickTree(n);
    }

    /**
     * OPTIMIZED: Create Range-Optimized BIT from array in O(n).
     */
    public static RangeOptimizedBIT fromArray(List<Double> arr) {
        int n = arr.size();
        RangeOptimizedBIT robit = new RangeOptimizedBIT(n);

        // Build difference array for tree1
        double[] diff = new double[n];
        if (n > 0) {
            diff[0] = arr.get(0);
        }
        for (int i = 1; i < n; i++) {
            diff[i] = arr.get(i) - arr.get(i - 1);
        }

        // Build tree1 from difference array using the FenwickTree O(N) builder.
        List<Double> diffList = new java.util.ArrayList<>(n);
        for (int i = 0; i < n; i++) diffList.add(diff[i]);
        robit.tree1 = FenwickTree.fromArray(diffList);

        // Tree2 remains initialized to zero.

        return robit;
    }

    // Naive O(N log N) construction (N range updates)
    public static RangeOptimizedBIT fromArrayNaive(List<Double> arr) {
        int n = arr.size();
        RangeOptimizedBIT robit = new RangeOptimizedBIT(n);

        // Apply point-updates to simulate range updates from [i, i]
        for (int i = 0; i < n; i++) {
            robit.pointUpdate(i, arr.get(i));
        }
        return robit;
    }


    /**
     * Add 'value' to all elements in range [l, r] (0-based).
     */
    public void rangeUpdate(int l, int r, double value) {
        if (l < 0) l = 0;
        if (r >= n) r = n - 1;
        if (l > r) return;

        // Update tree1 for range effect
        tree1.update(l, value);
        if (r + 1 < n) {
            tree1.update(r + 1, -value);
        }

        // Update tree2 for position correction
        tree2.update(l, value * l);
        if (r + 1 < n) {
            tree2.update(r + 1, -value * (r + 1));
        }
    }

    /**
     * Get sum of elements from index 0 to idx inclusive.
     */
    public double prefixSum(int idx) {
        if (idx < 0) return 0;
        if (idx >= n) idx = n - 1;
        // Formula: Sum[0..idx] = tree1.prefixSum(idx) * (idx + 1) - tree2.prefixSum(idx)
        return tree1.prefixSum(idx) * (idx + 1) - tree2.prefixSum(idx);
    }

    /**
     * Get sum of elements in range [l, r].
     */
    public double rangeSum(int l, int r) {
        if (l > r || r < 0 || l >= n) return 0;
        if (l <= 0) return prefixSum(r);
        return prefixSum(r) - prefixSum(l - 1);
    }

    /**
     * Set the element at idx to a new value (0-based).
     */
    public void pointUpdate(int idx, double newValue) {
        // Find current value at idx
        double currentValue = rangeSum(idx, idx);
        double delta = newValue - currentValue;
        rangeUpdate(idx, idx, delta); // A point update is a range update of size 1
    }

    public long getMemoryUsage() {
        // Two Fenwick Trees
        return tree1.getMemoryUsage() + tree2.getMemoryUsage();
    }
}