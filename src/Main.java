import datastructures.FenwickTree;
import datastructures.RangeOptimizedBIT;
import datastructures.SegmentTree;
import utils.DatasetGenerator;
import utils.PerformanceTester;
import utils.PerformanceMetrics;

import java.util.List;
import java.util.Map;
import java.util.ArrayList;

public class Main {

    public static void main(String[] args) {
        System.out.println("================================================================================");
        System.out.println("DYNAMIC RANGE QUERY DATA STRUCTURES - PERFORMANCE ANALYSIS (Java)");
        System.out.println("================================================================================");

        DatasetGenerator generator = new DatasetGenerator();
        PerformanceTester tester = new PerformanceTester();

        int arraySize = 10000;
        int numQueries = 1000;

        // 1. Generate main dataset
        System.out.printf("\n1. Generating main dataset (N = %,d)...\n", arraySize);
        List<Double> mainData = generator.generateUniformRandom(arraySize);
        List<Map<String, Object>> mainQueries = generator.generateTestQueries(arraySize, numQueries, "mixed");
        System.out.println("   Dataset generated successfully.");

        // 2. Run main performance comparison
        System.out.println("\n2. Running performance comparison on main dataset...");
        System.out.println("   (This may take a moment...)");

        Map<String, PerformanceMetrics> comparisonResults = tester.compareDataStructures(
                mainData,
                mainQueries
        );

        // 3. Print performance table
        System.out.println("\n3. Performance Comparison Table (N = " + arraySize + ")");
        System.out.println("-------------------------------------------------------------------------------------------------------------------------");
        System.out.printf("%-20s | %-15s | %-18s | %-15s | %-15s | %-15s\n",
                "Data Structure", "Build Time (ms)", "Memory (bytes)", "Point Update (ms)", "Range Query (ms)", "Range Update (ms)");
        System.out.println("-------------------------------------------------------------------------------------------------------------------------");

        String[] structures = {"SegmentTree", "FenwickTree", "RangeOptimizedBIT"};
        for (String name : structures) {
            PerformanceMetrics metrics = comparisonResults.get(name);
            if (metrics != null) {
                double buildTime = metrics.buildTimeMs;
                long memory = metrics.memoryUsageBytes;
                double ptUpdate = metrics.operationTimesMs.getOrDefault("point_update", Double.NaN);
                double rgQuery = metrics.operationTimesMs.getOrDefault("range_query", Double.NaN);
                double rgUpdate = metrics.operationTimesMs.getOrDefault("range_update", Double.NaN);

                System.out.printf("%-20s | %-15.2f | %-18d | %-15.5f | %-15.5f | %-15s\n",
                        name, buildTime, memory, ptUpdate, rgQuery,
                        Double.isNaN(rgUpdate) ? "N/A" : String.format("%.5f", rgUpdate));
            }
        }
        System.out.println("-------------------------------------------------------------------------------------------------------------------------");

        // 4. Summary report
        System.out.println("\n" + "================================================================================");
        System.out.println("EXPERIMENT SUMMARY (Conceptual)");
        System.out.println("================================================================================");

        System.out.println("\nKey Findings (Theoretical/Complexity):");
        System.out.println("1. Segment Tree:");
        System.out.println("   - Build Time: O(N)");
        System.out.println("   - Operations (Query/Update): O(log N)");
        System.out.println("   - Memory: ~4N elements (due to tree structure)");
        System.out.println("   - Features: Supports both point and range updates efficiently using Lazy Propagation.");

        System.out.println("\n2. Fenwick Tree:");
        System.out.println("   - Build Time: O(N) with optimized construction");
        System.out.println("   - Operations (Query/Update): O(log N)");
        System.out.println("   - Memory: N+1 elements (most memory efficient)");
        System.out.println("   - Features: Best for simple point updates and prefix sum (range sum is a difference of two prefix sums). Cannot do range updates directly.");

        System.out.println("\n3. Range-Optimized BIT:");
        System.out.println("   - Build Time: O(N)");
        System.out.println("   - Operations (Query/Update): O(log N)");
        System.out.println("   - Memory: 2N+2 elements (two Fenwick Trees)");
        System.out.println("   - Features: Supports both range updates and range queries, offering a middle ground in memory and complexity between the others.");

        System.out.println("\n================================================================================");
    }
}