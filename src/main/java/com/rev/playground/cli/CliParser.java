package com.rev.playground.cli;

import com.rev.playground.config.BenchmarksOptions;
import com.rev.playground.config.PlaygroundConfig;
import com.rev.playground.config.PlaygroundConfigBuilder;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;

import java.io.PrintWriter;
import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

import static com.rev.playground.config.BenchmarksOptions.ALL;

public final class CliParser {
    private static final CommandLineParser PARSER = new DefaultParser();

    private CliParser() {
    }

    public static PlaygroundConfig parse(final String[] args) {
        Options options = CliOptions.getOptions();
        try {
            CommandLine cl = PARSER.parse(options, args, true);


            if (cl.hasOption(CliOptions.LIST)) {
                listBenchmarks();
                return null;
            }

            if (cl.hasOption(CliOptions.HELP) || !cl.hasOption(CliOptions.BENCHMARKS)) {
                printHelp(options);
                return null;
            }

            final boolean load = cl.hasOption(CliOptions.LOAD);
            String[] benchmarksOptionValues = cl.getOptionValues(CliOptions.BENCHMARKS);
            final Set<BenchmarksOptions> benchmarksToRun = getBenchmarksToRun(benchmarksOptionValues);

            return new PlaygroundConfigBuilder()
                    .setBenchmarks(benchmarksToRun)
                    .setLoad(load)
                    .build();
        } catch (Exception e) {
            System.out.println(e.getMessage());
//            Get the actual arg rather than using cli, in case it was the cli that failed!
            if (Arrays.stream(args).anyMatch(s -> "-d".equals(s) || "--debug".equals(s))) {
                PrintWriter pw = new PrintWriter(System.out);
                e.printStackTrace(pw);
                pw.flush();
            }
            printHelp(options);
            return null;
        }
    }

    private static Set<BenchmarksOptions> getBenchmarksToRun(String[] rawCliOptions) {
        final Set<BenchmarksOptions> benchmarksToRun =
                Arrays.stream(rawCliOptions).map(BenchmarksOptions::valueOf).collect(Collectors.toSet());

        if (benchmarksToRun.contains(ALL)) {
            return Arrays.stream(BenchmarksOptions.values()).filter(o -> !o.equals(ALL)).collect(Collectors.toSet());
        }

        return benchmarksToRun;
    }

    private static void listBenchmarks() {
        System.out.println("Available Benchmarks:\n\n");
        final BenchmarksOptions[] benchmarks = BenchmarksOptions.values();
        Arrays.stream(benchmarks).forEach(engine -> System.out.println(" - " + engine));
    }

    private static void printHelp(final Options options) {
        final HelpFormatter helpFormatter = new HelpFormatter();
        helpFormatter.printHelp("data-structures-playground", options);
    }
}

