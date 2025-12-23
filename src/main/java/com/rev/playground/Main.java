package com.rev.playground;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.rev.playground.cli.CliParser;
import com.rev.playground.config.BenchmarksOptions;
import com.rev.playground.config.PlaygroundConfig;
import com.rev.playground.results.BenchmarkResults;
import com.rev.playground.results.BenchmarkResultsElement;
import org.knowm.xchart.QuickChart;
import org.knowm.xchart.SwingWrapper;
import org.knowm.xchart.XYChart;
import org.openjdk.jmh.results.RunResult;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.ChainedOptionsBuilder;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

public class Main {

    private static final Logger LOGGER = LoggerFactory.getLogger(Main.class);

    public static void main(String[] args) throws RunnerException, IOException {

        System.setProperty("benchmark.file", "results.json");

        final PlaygroundConfig config = CliParser.parse(args);

        if (config == null) {
            return;
        }

        final BenchmarkResults benchmarkResults =
                config.load() ? loadResultsFromFile(config.benchmarks()) : runBenchmark(config.benchmarks());

        final List<List<XYChart>> charts = config.benchmarks().stream()
                .map(b -> getResultsForBenchmark(b, benchmarkResults))
                .map(Main::getCharts)
                .toList();

        for (List<XYChart> chart : charts) {
            new SwingWrapper<>(chart).displayChartMatrix();
        }
    }

    private static List<XYChart> getCharts(final BenchmarkResultsElement benchmarkResultsElement) {
        final Map<String, Map<String, BenchmarkResultsElement.LogBenchmarkResultSeries>> resultsForGraph =
                benchmarkResultsElement.getResults();

        final List<XYChart> charts = new ArrayList<>();

        for (final Map.Entry<String, Map<String, BenchmarkResultsElement.LogBenchmarkResultSeries>> resultForChart :
                resultsForGraph.entrySet()) {

            final String benchmarkName = resultForChart.getKey();
            final Map<String, BenchmarkResultsElement.LogBenchmarkResultSeries> resultsByListType =
                    resultForChart.getValue();
            final Iterator<Map.Entry<String, BenchmarkResultsElement.LogBenchmarkResultSeries>> typeAndResultIterator =
                    resultsByListType.entrySet().iterator();

            Map.Entry<String, BenchmarkResultsElement.LogBenchmarkResultSeries> typeAndResult =
                    typeAndResultIterator.next();

            final XYChart chart = QuickChart.getChart(benchmarkName, "log10(Size)", "log10(Ops/s)", typeAndResult.getKey(),
                    typeAndResult.getValue().getSizes(), typeAndResult.getValue().getScores());

            while (typeAndResultIterator.hasNext()) {
                final Map.Entry<String, BenchmarkResultsElement.LogBenchmarkResultSeries> next =
                        typeAndResultIterator.next();
                chart.addSeries(next.getKey(), next.getValue().getSizes(), next.getValue().getScores());
            }

            charts.add(chart);
        }
        return charts;
    }

    private static BenchmarkResults loadResultsFromFile(Set<BenchmarksOptions> benchmarks) throws IOException {
        final File file = getFile();
        final ObjectMapper om = new ObjectMapper();
        final BenchmarkResults benchmarkResults = om.readValue(file, BenchmarkResults.class);

        final BenchmarkResults filteredResults = new BenchmarkResults();
        for (BenchmarksOptions benchmark : benchmarks) {
            final BenchmarkResultsElement source = getResultsForBenchmark(benchmark, benchmarkResults);
            final BenchmarkResultsElement target = getResultsForBenchmark(benchmark, filteredResults);

            source.getResults().forEach((methodName, resultMap) -> resultMap.forEach(
                    (type, result) -> target.addResult(methodName, type, result)));
        }

        return filteredResults;
    }

    private static BenchmarkResultsElement getResultsForBenchmark(BenchmarksOptions benchmark,
                                                                  BenchmarkResults results) {
        return switch (benchmark) {
            case COLLECTION -> results.getCollectionResults();
            case MAP -> results.getMapResults();
            case LIST -> results.getListResults();
            case BI_LONG_MAP -> results.getBiLongMapResults();
            case ALL -> throw new RuntimeException("This should have been filtered out in parsing!");
        };
    }

    private static File getFile() throws FileNotFoundException {
        final Optional<String> benchmarkFile = Optional.ofNullable(System.getProperty("benchmark.file"));
        final String fileName = benchmarkFile.orElseThrow(FileNotFoundException::new);
        final String filePath = "%s/%s".formatted(System.getProperty("user.dir"), fileName);
        return new File(filePath);
    }

    private static BenchmarkResults runBenchmark(Set<BenchmarksOptions> benchmarks) throws RunnerException {

        ChainedOptionsBuilder builder = new OptionsBuilder()
                .include("com.rev.playground.benchmarks*")
                .forks(1);

        Arrays.stream(BenchmarksOptions.values())
                .filter(b -> !benchmarks.contains(b) && b != BenchmarksOptions.ALL)
                .map(Main::getExcludeRegex)
                .forEach(builder::exclude);

        final Options options = builder.build();

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

                    benchmarkResultsElement.getListResults()
                            .addResult(methodName, implementation, size, result.getPrimaryResult().getScore());
                }
                case "CollectionBenchmark" -> {
                    final String size = result.getParams().getParam("size");
                    final String implementation = result.getParams().getParam("collectionType");

                    benchmarkResultsElement.getCollectionResults()
                            .addResult(methodName, implementation, size, result.getPrimaryResult().getScore());
                }
                case "MapBenchmark" -> {
                    final String size = result.getParams().getParam("size");
                    final String implementation = result.getParams().getParam("mapType");

                    benchmarkResultsElement.getMapResults()
                            .addResult(methodName, implementation, size, result.getPrimaryResult().getScore());
                }
                case "BiLongMapBenchmark" -> {
                    final String size = result.getParams().getParam("firstKeySize");
                    final String implementation = result.getParams().getParam("mapType");

                    benchmarkResultsElement.getBiLongMapResults()
                            .addResult(methodName, implementation, size, result.getPrimaryResult().getScore());
                }
            }
        }

        final Optional<String> saveFileLocation = Optional.ofNullable(System.getProperty("benchmark.file"));

        saveFileLocation.ifPresent(fPath -> {
            final ObjectMapper om = new ObjectMapper();
            try {
                final File f = getFile();
                om.writer().writeValue(f, benchmarkResultsElement);
            } catch (Exception e) {
                LOGGER.error("Could not save benchmark results to file", e);
            }
        });
        return benchmarkResultsElement;
    }

    private static String getExcludeRegex(final BenchmarksOptions b) {
        return switch (b) {
            case COLLECTION -> "com.rev.playground.benchmarks.CollectionBenchmark";
            case MAP -> "com.rev.playground.benchmarks.MapBenchmark";
            case LIST -> "com.rev.playground.benchmarks.ListBenchmark";
            case BI_LONG_MAP -> "com.rev.playground.benchmarks.BiLongMapBenchmark";
            case ALL -> throw new RuntimeException("This should have been filtered out during parsing!");
        };
    }
}