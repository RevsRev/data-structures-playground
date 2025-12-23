package com.rev.playground.results;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BenchmarkResultsElement {

    final Map<String, Map<String, LogBenchmarkResultSeries>> results = new HashMap<>();

    public final void addResult(
            final String methodName,
            final String type,
            final LogBenchmarkResultSeries series) {
        results.computeIfAbsent(methodName, k -> new HashMap<>()).put(type, series);
    }

    public final void addResult(final String methodName,
                                final String type,
                                final String size,
                                final double score) {
        final Map<String, LogBenchmarkResultSeries> methodResults =
                results.computeIfAbsent(methodName, k -> new HashMap<>());

        final LogBenchmarkResultSeries logBenchmarkResultSeries =
                methodResults.computeIfAbsent(type, k -> new LogBenchmarkResultSeries());

        logBenchmarkResultSeries.addResult(Integer.parseInt(size), score);
    }

    public final Map<String, Map<String, LogBenchmarkResultSeries>> getResults() {
        return results;
    }

    public static final class LogBenchmarkResultSeries {

        final List<Double> sizes = new ArrayList<>();
        final List<Double> scores = new ArrayList<>();

        private void addResult(final double size, final double score) {
            sizes.add(Math.log10(size));
            scores.add(Math.log10(score));
        }

        public double[] getSizes() {
            return sizes.stream().mapToDouble(Double::doubleValue).toArray();
        }

        public double[] getScores() {
            return scores.stream().mapToDouble(Double::doubleValue).toArray();
        }

    }

}
