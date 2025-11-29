package com.rev.playground;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.rev.playground.results.ListBenchmarkResults;
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

        final ListBenchmarkResults listBenchmarkResults = loadResultsFromFile ? loadResultsFromFile() : runBenchmark();

        final Map<String, Map<String, ListBenchmarkResults.LogBenchmarkResultSeries>> resultsForGraph =
                listBenchmarkResults.getResults();

        final List<XYChart> charts = new ArrayList<>();

        for (final Map.Entry<String, Map<String, ListBenchmarkResults.LogBenchmarkResultSeries>> resultForChart :
                resultsForGraph.entrySet()) {

            final String benchmarkName = resultForChart.getKey();
            final Map<String, ListBenchmarkResults.LogBenchmarkResultSeries> resultsByListType = resultForChart.getValue();
            final Iterator<Map.Entry<String, ListBenchmarkResults.LogBenchmarkResultSeries>> typeAndResultIterator =
                    resultsByListType.entrySet().iterator();

            Map.Entry<String, ListBenchmarkResults.LogBenchmarkResultSeries> typeAndResult = typeAndResultIterator.next();

            final XYChart chart = QuickChart.getChart(benchmarkName, "Size", "Ops/s", typeAndResult.getKey(),
                    typeAndResult.getValue().getSizes(), typeAndResult.getValue().getScores());

            while (typeAndResultIterator.hasNext()) {
                final Map.Entry<String, ListBenchmarkResults.LogBenchmarkResultSeries> next = typeAndResultIterator.next();
                chart.addSeries(next.getKey(), next.getValue().getSizes(), next.getValue().getScores());
            }

            charts.add(chart);
        }

        new SwingWrapper<>(charts).displayChartMatrix();
    }

    private static ListBenchmarkResults loadResultsFromFile() throws IOException {
        final File file = getFile();
        final ObjectMapper om = new ObjectMapper();
        return om.readValue(file, ListBenchmarkResults.class);
    }

    private static File getFile() throws FileNotFoundException {
        final Optional<String> benchmarkFile = Optional.ofNullable(System.getProperty("benchmark.file"));
        final String fileName = benchmarkFile.orElseThrow(FileNotFoundException::new);
        final String filePath = "%s/%s".formatted(System.getProperty("user.dir"), fileName);
        return new File(filePath);
    }

    private static ListBenchmarkResults runBenchmark() throws RunnerException {
        final Options options = new OptionsBuilder().include("com.rev.playground.benchmarks*").forks(1).build();

        final Runner runner = new Runner(options);
        final Collection<RunResult> results = runner.run();

        final ListBenchmarkResults listBenchmarkResults = new ListBenchmarkResults();

        for (final RunResult result : results) {

            final String[] tokens = result.getParams().getBenchmark().split("\\.");
            final String methodName = tokens[tokens.length - 1];
            final String className = tokens[tokens.length - 2];

            switch (className) {
                case "ListBenchmark" -> {
                    final String size = result.getParams().getParam("size");
                    final String implementation = result.getParams().getParam("listType");

                    listBenchmarkResults.addResult(methodName, implementation, size, result.getPrimaryResult().getScore());
                }
            }
        }

        final Optional<String> saveFileLocation = Optional.ofNullable(System.getProperty("benchmark.file"));

        saveFileLocation.ifPresent(
                fPath -> {
                    final ObjectMapper om = new ObjectMapper();
                    try {
                        final File f = getFile();
                        om.writer().writeValue(f, listBenchmarkResults);
                    } catch (Exception e) {
                        LOGGER.error("Could not save benchmark results to file", e);
                    }
                }
        );
        return listBenchmarkResults;
    }
}