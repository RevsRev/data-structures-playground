package com.rev.playground.config;

import java.util.Set;

public class PlaygroundConfigBuilder {
    private Set<BenchmarksOptions> benchmarks;
    private boolean load;

    public PlaygroundConfigBuilder setBenchmarks(final Set<BenchmarksOptions> benchmarks) {
        this.benchmarks = benchmarks;
        return this;
    }

    public PlaygroundConfigBuilder setLoad(final boolean load) {
        this.load = load;
        return this;
    }

    public PlaygroundConfig build() {
        return new PlaygroundConfig(benchmarks, load);
    }
}