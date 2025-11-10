package utils;

import datastructures.FenwickTree;
import datastructures.RangeOptimizedBIT;
import datastructures.SegmentTree;

import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;

public class PerformanceTester {

    private final Runtime runtime = Runtime.getRuntime();

    /**
     * Measures time and memory for a single operation.
     */
    private double measureOperationTime(Runnable operation) {
        System.gc();

        long startTime = System.nanoTime();
        operation.run();
        long endTime = System.nanoTime();

        // Convert to milliseconds
        return (endTime - startTime) / 1_000_000.0;
    }

    /**
     * Benchmarks build time and memory usage for a structure.
     */
    private <T> Map<String, Object> benchmarkBuild(List<Double> data, java.util.function.Function<List<Double>, T> builder) {
        System.gc(); // Clean up memory before build

        // Measure memory BEFORE build
        long memoryBefore = runtime.totalMemory() - runtime.freeMemory();

        // Measure time
        long startTime = System.nanoTime();
        T ds = builder.apply(data);
        long endTime = System.nanoTime();

        // Measure memory AFTER build
        long memoryAfter = runtime.totalMemory() - runtime.freeMemory();

        Map<String, Object> results = new HashMap<>();
        results.put("instance", ds);
        results.put("time", (endTime - startTime) / 1_000_000.0); // ms

        // Use the structure's built-in memory method for a more precise/consistent value (based on array sizes)
        try {
            Method getMemoryUsageMethod = ds.getClass().getMethod("getMemoryUsage");
            results.put("memory_usage", (Long) getMemoryUsageMethod.invoke(ds));
        } catch (Exception e) {
            results.put("memory_usage", memoryAfter - memoryBefore); // Fallback to rough heap diff
        }

        return results;
    }

    /**
     * Benchmarks performance of point/range operations on a data structure.
     */
    private Map<String, Map<String, Double>> benchmarkOperations(Object dataStructure, List<Map<String, Object>> queries) {
        Map<String, List<Double>> operationTimes = new HashMap<>();

        for (Map<String, Object> query : queries) {
            String op = (String) query.get("operation");
            List<Integer> params = (List<Integer>) query.get("parameters");

            Runnable operation;

            if ("range_query".equals(op)) {
                operation = () -> {
                    try {
                        Method method = dataStructure.getClass().getMethod("rangeSum", int.class, int.class);
                        method.invoke(dataStructure, params.get(0), params.get(1));
                    } catch (Exception e) {
                        try { // Segment Tree uses rangeQuery
                            Method method = dataStructure.getClass().getMethod("rangeQuery", int.class, int.class);
                            method.invoke(dataStructure, params.get(0), params.get(1));
                        } catch (Exception ee) {
                            // System.err.println("Query method not found: " + e.getMessage());
                        }
                    }
                };
            } else if ("point_update".equals(op)) {
                double value = (double) query.get("value");
                operation = () -> {
                    try {
                        Method method = dataStructure.getClass().getMethod("pointUpdate", int.class, double.class);
                        method.invoke(dataStructure, params.get(0), value);
                    } catch (Exception e) {
                        // System.err.println("Point Update method not found: " + e.getMessage());
                    }
                };
            } else if ("range_update".equals(op)) {
                double value = (double) query.get("value");
                operation = () -> {
                    try {
                        Method method = dataStructure.getClass().getMethod("rangeUpdate", int.class, int.class, double.class);
                        method.invoke(dataStructure, params.get(0), params.get(1), value);
                    } catch (Exception e) {
                        // System.err.println("Range Update method not found: " + e.getMessage());
                    }
                };
            } else {
                continue;
            }

            double time = measureOperationTime(operation);
            operationTimes.computeIfAbsent(op, k -> new ArrayList<>()).add(time);
        }

        Map<String, Map<String, Double>> avgResults = new HashMap<>();
        for (Map.Entry<String, List<Double>> entry : operationTimes.entrySet()) {
            double sum = entry.getValue().stream().mapToDouble(Double::doubleValue).sum();
            double avgTime = sum / entry.getValue().size();
            Map<String, Double> opMap = new HashMap<>();
            opMap.put("avg_time", avgTime);
            avgResults.put(entry.getKey(), opMap);
        }

        return avgResults;
    }

    /**
     * Compares all three data structures.
     */
    public Map<String, PerformanceMetrics> compareDataStructures(List<Double> data, List<Map<String, Object>> queries) {
        Map<String, PerformanceMetrics> allResults = new HashMap<>();

        // 1. Segment Tree
        Map<String, Object> stBuild = benchmarkBuild(data, SegmentTree::new);
        SegmentTree st = (SegmentTree) stBuild.get("instance");
        Map<String, Map<String, Double>> stOps = benchmarkOperations(st, queries);

        allResults.put("SegmentTree", new PerformanceMetrics(
                (double) stBuild.get("time"),
                (long) stBuild.get("memory_usage"),
                extractAvgTimes(stOps)
        ));

        // 2. Fenwick Tree (Only Point Update and Range Query)
        Map<String, Object> ftBuild = benchmarkBuild(data, FenwickTree::fromArray);
        FenwickTree ft = (FenwickTree) ftBuild.get("instance");
        Map<String, Map<String, Double>> ftOps = benchmarkOperations(ft, queries);

        allResults.put("FenwickTree", new PerformanceMetrics(
                (double) ftBuild.get("time"),
                (long) ftBuild.get("memory_usage"),
                extractAvgTimes(ftOps)
        ));

        // 3. Range-Optimized BIT
        Map<String, Object> robitBuild = benchmarkBuild(data, RangeOptimizedBIT::fromArray);
        RangeOptimizedBIT robit = (RangeOptimizedBIT) robitBuild.get("instance");
        Map<String, Map<String, Double>> robitOps = benchmarkOperations(robit, queries);

        allResults.put("RangeOptimizedBIT", new PerformanceMetrics(
                (double) robitBuild.get("time"),
                (long) robitBuild.get("memory_usage"),
                extractAvgTimes(robitOps)
        ));

        return allResults;
    }

    private Map<String, Double> extractAvgTimes(Map<String, Map<String, Double>> ops) {
        Map<String, Double> avgTimes = new HashMap<>();
        for(Map.Entry<String, Map<String, Double>> entry : ops.entrySet()) {
            avgTimes.put(entry.getKey(), entry.getValue().get("avg_time"));
        }
        return avgTimes;
    }
}