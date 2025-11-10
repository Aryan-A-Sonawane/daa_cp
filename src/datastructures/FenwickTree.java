package datastructures;

import java.util.Arrays;
import java.util.List;

public class FenwickTree {
    private final int n;
    private final double[] tree; // 1-indexed array

    public FenwickTree(int n) {
        this.n = n;
        this.tree = new double[n + 1];
    }

    /**
     * O(n) construction using the optimized method.
     */
    public static FenwickTree fromArray(List<Double> arr) {
        int n = arr.size();
        FenwickTree fenwick = new FenwickTree(n);

        // Copy array values to tree (1-indexed)
        for (int i = 0; i < n; i++) {
            fenwick.tree[i + 1] = arr.get(i);
        }

        // Build tree in O(n) by cascading values upward
        for (int i = 1; i <= n; i++) {
            int parentIdx = i + (i & -i);
            if (parentIdx <= n) {
                fenwick.tree[parentIdx] += fenwick.tree[i];
            }
        }
        return fenwick;
    }

    /**
     * Naive O(n log n) construction for comparison/simplicity.
     */
    public static FenwickTree fromArrayNaive(List<Double> arr) {
        FenwickTree fenwick = new FenwickTree(arr.size());
        for (int i = 0; i < arr.size(); i++) {
            fenwick.update(i, arr.get(i)); // This update adds the value, not sets it
        }
        return fenwick;
    }

    /**
     * Add delta to element at index idx (0-based).
     */
    public void update(int idx, double delta) {
        idx++; // Convert to 1-based indexing
        while (idx <= n) {
            tree[idx] += delta;
            idx += idx & (-idx);
        }
    }

    /**
     * Get sum of elements from index 0 to idx inclusive (0-based).
     */
    public double prefixSum(int idx) {
        if (idx < 0) return 0;
        idx = Math.min(idx, n - 1); // Bound check
        idx++; // Convert to 1-based indexing
        double result = 0;
        while (idx > 0) {
            result += tree[idx];
            idx -= idx & (-idx);
        }
        return result;
    }

    /**
     * Get sum of elements in range [l, r] (0-based).
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
        update(idx, delta);
    }

    public long getMemoryUsage() {
        // Return size of the tree array (doubles * 8 bytes)
        return (long) (n + 1) * 8;
    }
}