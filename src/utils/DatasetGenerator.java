package utils;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Map;
import java.util.HashMap;

public class DatasetGenerator {
    private final Random random;

    public DatasetGenerator() {
        this.random = new Random(42); // Seed for reproducibility
    }

    public List<Double> generateUniformRandom(int size, double minVal, double maxVal) {
        List<Double> data = new ArrayList<>(size);
        for (int i = 0; i < size; i++) {
            data.add(minVal + (maxVal - minVal) * random.nextDouble());
        }
        return data;
    }

    public List<Double> generateUniformRandom(int size) {
        return generateUniformRandom(size, 0, 1000);
    }

    /**
     * Generates a list of queries as a Map: {operation: String, params: List<Double>}
     */
    public List<Map<String, Object>> generateTestQueries(int arraySize, int numQueries, String mixType) {
        List<Map<String, Object>> queries = new ArrayList<>(numQueries);
        String[] operations;

        if ("point_only".equals(mixType)) {
            operations = new String[]{"point_update", "range_query"};
        } else if ("range_only".equals(mixType)) {
            operations = new String[]{"range_update", "range_query"};
        } else { // "mixed"
            operations = new String[]{"point_update", "range_update", "range_query"};
        }

        for (int i = 0; i < numQueries; i++) {
            String op = operations[random.nextInt(operations.length)];
            Map<String, Object> query = new HashMap<>();
            query.put("operation", op);

            List<Integer> params = new ArrayList<>();
            switch (op) {
                case "range_query": {
                    // Pick two random indices and normalize to [l, r]
                    int a = random.nextInt(arraySize);
                    int b = random.nextInt(arraySize);
                    int lq = Math.min(a, b);
                    int rq = Math.max(a, b);
                    params.add(lq);
                    params.add(rq);
                    query.put("parameters", params);
                    break;
                }
                case "point_update": {
                    int idx = random.nextInt(arraySize);
                    double newVal = random.nextDouble() * 1000;
                    params.add(idx);
                    query.put("parameters", params);
                    query.put("value", newVal);
                    break;
                }
                case "range_update": {
                    int a = random.nextInt(arraySize);
                    int b = random.nextInt(arraySize);
                    int lu = Math.min(a, b);
                    int ru = Math.max(a, b);
                    double value = random.nextDouble() * 200 - 100; // Value between -100 and 100
                    params.add(lu);
                    params.add(ru);
                    query.put("parameters", params);
                    query.put("value", value);
                    break;
                }
            }
            queries.add(query);
        }
        return queries;
    }

    // Omitted save/load methods as Java memory/file handling is different and less crucial for core logic
}