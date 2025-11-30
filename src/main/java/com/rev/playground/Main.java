package com.rev.playground;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.rev.playground.results.BenchmarkResults;
import com.rev.playground.results.BenchmarkResultsElement;
import org.knowm.xchart.QuickChart;
import org.knowm.xchart.SwingWrapper;
import org.knowm.xchart.XYChart;
import org.openjdk.jmh.results.RunResult;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class Main {

    private static final Logger LOGGER = LoggerFactory.getLogger(Main.class);

    public static void main(String[] args) throws RunnerException, IOException {

        System.setProperty("benchmark.file", "results.json");

        boolean loadResultsFromFile = args.length > 0 && "load".equals(args[0]);

        final BenchmarkResults benchmarkResults = loadResultsFromFile ? loadResultsFromFile() : runBenchmark();

        final List<XYChart> collectionCharts = getCharts(benchmarkResults.getCollectionResults());
        final List<XYChart> listCharts = getCharts(benchmarkResults.getListResults());
        final List<XYChart> mapCharts = getCharts(benchmarkResults.getMapResults());

        new SwingWrapper<>(collectionCharts).displayChartMatrix();
        new SwingWrapper<>(listCharts).displayChartMatrix();
        new SwingWrapper<>(mapCharts).displayChartMatrix();
    }

    private static List<XYChart> getCharts(final BenchmarkResultsElement benchmarkResultsElement) {
        final Map<String, Map<String, BenchmarkResultsElement.LogBenchmarkResultSeries>> resultsForGraph =
                benchmarkResultsElement.getResults();

        final List<XYChart> charts = new ArrayList<>();

        for (final Map.Entry<String, Map<String, BenchmarkResultsElement.LogBenchmarkResultSeries>> resultForChart :
                resultsForGraph.entrySet()) {

            final String benchmarkName = resultForChart.getKey();
            final Map<String, BenchmarkResultsElement.LogBenchmarkResultSeries> resultsByListType = resultForChart.getValue();
            final Iterator<Map.Entry<String, BenchmarkResultsElement.LogBenchmarkResultSeries>> typeAndResultIterator =
                    resultsByListType.entrySet().iterator();

            Map.Entry<String, BenchmarkResultsElement.LogBenchmarkResultSeries> typeAndResult = typeAndResultIterator.next();

            final XYChart chart = QuickChart.getChart(benchmarkName, "Size", "Ops/s", typeAndResult.getKey(),
                    typeAndResult.getValue().getSizes(), typeAndResult.getValue().getScores());

            while (typeAndResultIterator.hasNext()) {
                final Map.Entry<String, BenchmarkResultsElement.LogBenchmarkResultSeries> next = typeAndResultIterator.next();
                chart.addSeries(next.getKey(), next.getValue().getSizes(), next.getValue().getScores());
            }

            charts.add(chart);
        }
        return charts;
    }

    private static BenchmarkResults loadResultsFromFile() throws IOException {
        final File file = getFile();
        final ObjectMapper om = new ObjectMapper();
        return om.readValue(file, BenchmarkResults.class);
    }

    private static File getFile() throws FileNotFoundException {
        final Optional<String> benchmarkFile = Optional.ofNullable(System.getProperty("benchmark.file"));
        final String fileName = benchmarkFile.orElseThrow(FileNotFoundException::new);
        final String filePath = "%s/%s".formatted(System.getProperty("user.dir"), fileName);
        return new File(filePath);
    }

    private static BenchmarkResults runBenchmark() throws RunnerException {
        final Options options = new OptionsBuilder().include("com.rev.playground.benchmarks*").forks(1).build();

        final Runner runner = new Runner(options);
        final Collection<RunResult> results = runner.run();

        final BenchmarkResults benchmarkResultsElement = new BenchmarkResults();

        for (final RunResult result : results) {

            final String[] tokens = result.getParams().getBenchmark().split("\\.");
            final String methodName = tokens[tokens.length - 1];
            final String className = tokens[tokens.length - 2];

            switch (className) {
                case "ListBenchmark" -> {
                    final String size = result.getParams().getParam("size");
                    final String implementation = result.getParams().getParam("listType");

                    benchmarkResultsElement.getListResults().addResult(methodName, implementation, size, result.getPrimaryResult().getScore());
                }
                case "CollectionBenchmark" -> {
                    final String size = result.getParams().getParam("size");
                    final String implementation = result.getParams().getParam("collectionType");

                    benchmarkResultsElement.getCollectionResults().addResult(methodName, implementation, size, result.getPrimaryResult().getScore());
                }
                case "MapBenchmark" -> {
                    final String size = result.getParams().getParam("size");
                    final String implementation = result.getParams().getParam("mapType");

                    benchmarkResultsElement.getMapResults().addResult(methodName, implementation, size, result.getPrimaryResult().getScore());
                }
            }
        }

        final Optional<String> saveFileLocation = Optional.ofNullable(System.getProperty("benchmark.file"));

        saveFileLocation.ifPresent(
                fPath -> {
                    final ObjectMapper om = new ObjectMapper();
                    try {
                        final File f = getFile();
                        om.writer().writeValue(f, benchmarkResultsElement);
                    } catch (Exception e) {
                        LOGGER.error("Could not save benchmark results to file", e);
                    }
                }
        );
        return benchmarkResultsElement;
    }
}