package com.rev.playground.config;

import java.util.Set;

public record PlaygroundConfig(Set<BenchmarksOptions> benchmarks, boolean load) {
}
