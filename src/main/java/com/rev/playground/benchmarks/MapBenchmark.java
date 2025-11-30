package com.rev.playground.benchmarks;

import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.Level;
import org.openjdk.jmh.annotations.Measurement;
import org.openjdk.jmh.annotations.Param;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.annotations.Warmup;
import org.openjdk.jmh.infra.Blackhole;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

@State(Scope.Thread)
@Warmup(iterations = 3, time = 1)
@Measurement(iterations = 3, time = 1)
public class MapBenchmark {

    @Param({"1", "10", "100", "1000", "10000", "100000", "1000000"})
    int size;

    @Param({"HASH_MAP", "TREE_MAP"})
    ListType mapType;

    Map<Long, Long> map;

    @Setup(Level.Invocation)
    public void setup()
    {
        final Map<Long, Long> seed = new HashMap<>(size);
        for (long l = 0; l < size; l++) {
            seed.put(l, l);
        }

        map = switch (mapType) {
            case HASH_MAP -> new HashMap<>(seed);
            case TREE_MAP -> new TreeMap<>(seed);
        };
    }

    @Benchmark
    public void benchmarkIterate(final Blackhole bh)
    {
        for (final Map.Entry<Long, Long> entry : map.entrySet()) {
            bh.consume(entry);
        }
    }

    @Benchmark
    public void benchmarkPut(final Blackhole bh)
    {
        bh.consume(map.put((long) size, (long) size));
    }

    @Benchmark
    public void benchmarkRemove(final Blackhole bh)
    {
        bh.consume(map.remove((long) size / 2));
    }

    @Benchmark
    public void benchmarkContainsKey(final Blackhole bh) {
        bh.consume(map.containsKey((long)size / 2));
    }

    public enum ListType {
        HASH_MAP, TREE_MAP
    }
}
