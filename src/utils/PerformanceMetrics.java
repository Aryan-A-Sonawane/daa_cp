package utils;

import java.util.Map;

public class PerformanceMetrics {
    public double buildTimeMs;
    public long memoryUsageBytes;
    public Map<String, Double> operationTimesMs; // e.g., "point_update" -> avg_time

    public PerformanceMetrics(double buildTimeMs, long memoryUsageBytes, Map<String, Double> operationTimesMs) {
        this.buildTimeMs = buildTimeMs;
        this.memoryUsageBytes = memoryUsageBytes;
        this.operationTimesMs = operationTimesMs;
    }

    @Override
    public String toString() {
        return String.format("Build Time: %.2f ms, Memory: %d bytes, Ops: %s",
                buildTimeMs, memoryUsageBytes, operationTimesMs);
    }
}
