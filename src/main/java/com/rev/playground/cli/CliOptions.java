package com.rev.playground.cli;

import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;

public final class CliOptions {
    public static final Option HELP = helpOption();
    public static final Option LIST = listOption();
    public static final Option BENCHMARKS = benchmarksOption();
    public static final Option LOAD = loadOption();

    private CliOptions() {
    }

    public static Options getOptions() {
        Options options = new Options();
        options.addOption(HELP);
        options.addOption(LIST);
        options.addOption(LOAD);
        options.addOption(BENCHMARKS);
        return options;
    }

    private static Option helpOption() {
        return Option.builder()
                .option("h")
                .longOpt("help")
                .desc("Print this message")
                .build();
    }

    private static Option benchmarksOption() {
        return Option.builder()
                .option("b")
                .longOpt("benchmarks")
                .desc("Which benchmarks to use (\"ALL\" to run all)")
                .hasArgs()
                .type(String.class)
                .build();
    }

    private static Option listOption() {
        return Option.builder()
                .option("lb")
                .longOpt("list-benchmarks")
                .desc("List available benchmarks")
                .build();
    }

    private static Option loadOption() {
        return Option.builder()
                .option("l")
                .longOpt("load")
                .desc("Load benchmark results from file, rather than running again")
                .build();
    }
}

