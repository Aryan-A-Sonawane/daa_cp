package datastructures;

import java.util.List;
import java.util.Arrays;
import java.lang.Math;

public class SegmentTree {
    private final int n;
    private final double[] tree;
    private final double[] lazy;
    private final int treeSize;

    public SegmentTree(List<Double> arr) {
        this.n = arr != null ? arr.size() : 0;

        if (this.n == 0) {
            this.tree = new double[0];
            this.lazy = new double[0];
            this.treeSize = 0;
            return;
        }

        // Calculate tree size (roughly 4*N, or next power of 2 * 2 - 1)
        int height = (int) Math.ceil(Math.log(this.n) / Math.log(2));
        this.treeSize = 2 * (1 << height) - 1;

        this.tree = new double[this.treeSize];
        this.lazy = new double[this.treeSize];

        build(arr, 0, 0, this.n - 1);
    }

    private void build(List<Double> arr, int node, int start, int end) {
        if (node >= treeSize) return;

        if (start == end) {
            tree[node] = arr.get(start);
        } else {
            int mid = (start + end) / 2;
            int leftChild = 2 * node + 1;
            int rightChild = 2 * node + 2;

            build(arr, leftChild, start, mid);
            build(arr, rightChild, mid + 1, end);

            tree[node] = tree[leftChild] + tree[rightChild];
        }
    }

    private void pushDown(int node, int start, int end) {
        if (lazy[node] != 0 && node < treeSize) {
            // Apply lazy update to tree node
            tree[node] += lazy[node] * (end - start + 1);

            if (start != end) {
                // Pass lazy value to children
                int leftChild = 2 * node + 1;
                int rightChild = 2 * node + 2;

                if(leftChild < treeSize) lazy[leftChild] += lazy[node];
                if(rightChild < treeSize) lazy[rightChild] += lazy[node];
            }

            // Clear current node's lazy value
            lazy[node] = 0;
        }
    }

    // --- Range Update ---

    public void rangeUpdate(int l, int r, double value) {
        if (l < 0) l = 0;
        if (r >= n) r = n - 1;
        if (l > r) return;
        _rangeUpdateHelper(0, 0, n - 1, l, r, value);
    }

    private void _rangeUpdateHelper(int node, int start, int end, int l, int r, double value) {
        if (node >= treeSize) return;

        pushDown(node, start, end); // Apply pending updates

        // No overlap
        if (start > r || end < l) {
            return;
        }

        // Complete overlap
        if (start >= l && end <= r) {
            // Apply update directly
            tree[node] += value * (end - start + 1);

            // Pass lazy value to children
            if (start != end) {
                int leftChild = 2 * node + 1;
                int rightChild = 2 * node + 2;
                if(leftChild < treeSize) lazy[leftChild] += value;
                if(rightChild < treeSize) lazy[rightChild] += value;
            }
            return;
        }

        // Partial overlap
        int mid = (start + end) / 2;
        int leftChild = 2 * node + 1;
        int rightChild = 2 * node + 2;

        _rangeUpdateHelper(leftChild, start, mid, l, r, value);
        _rangeUpdateHelper(rightChild, mid + 1, end, l, r, value);

        // Update current node after children are updated
        double leftVal = leftChild < treeSize ? tree[leftChild] : 0;
        double rightVal = rightChild < treeSize ? tree[rightChild] : 0;
        tree[node] = leftVal + rightVal;
    }

    // --- Point Update (sets a new value) ---

    public void pointUpdate(int idx, double newValue) {
        // Find current value and calculate delta
        double currentValue = rangeQuery(idx, idx);
        double delta = newValue - currentValue;
        rangeUpdate(idx, idx, delta); // A point update is a range update of size 1
    }

    // --- Range Query ---

    public double rangeQuery(int l, int r) {
        if (l < 0) l = 0;
        if (r >= n) r = n - 1;
        if (l > r) return 0;
        return _rangeQueryHelper(0, 0, n - 1, l, r);
    }

    private double _rangeQueryHelper(int node, int start, int end, int l, int r) {
        if (node >= treeSize) return 0;

        pushDown(node, start, end); // Apply pending updates

        // No overlap
        if (start > r || end < l) {
            return 0;
        }

        // Complete overlap
        if (start >= l && end <= r) {
            return tree[node];
        }

        // Partial overlap
        int mid = (start + end) / 2;
        int leftChild = 2 * node + 1;
        int rightChild = 2 * node + 2;

        double leftSum = _rangeQueryHelper(leftChild, start, mid, l, r);
        double rightSum = _rangeQueryHelper(rightChild, mid + 1, end, l, r);

        return leftSum + rightSum;
    }

    public long getMemoryUsage() {
        // tree array + lazy array (doubles * 8 bytes)
        return (long) treeSize * 8 * 2;
    }
}