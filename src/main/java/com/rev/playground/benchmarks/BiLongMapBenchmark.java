package com.rev.playground.benchmarks;

import com.rev.playground.datastructures.tm.BiLongJavaTreeMap;
import com.rev.playground.datastructures.tm.BiLongMap;
import com.rev.playground.datastructures.tm.BiLongRedBlackTreeMap;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.Level;
import org.openjdk.jmh.annotations.Measurement;
import org.openjdk.jmh.annotations.Param;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.annotations.Warmup;
import org.openjdk.jmh.infra.Blackhole;

import java.util.TreeMap;

@State(Scope.Thread)
@Warmup(iterations = 3, time = 1)
@Measurement(iterations = 3, time = 1)
public class BiLongMapBenchmark {

    @Param({"1", "10", "100", "1000"})
    int firstKeySize;

//    @Param({"1", "10", "100", "1000"})
    int secondKeySize = 4;

    @Param({"BI_LONG_RED_BLACK_TREE", "BI_LONG_JAVA_TREE"})
    MapType mapType;

    BiLongMap<Long> map;

    @Setup(Level.Invocation)
    public void setup() {
        map = switch (mapType) {
            case BI_LONG_RED_BLACK_TREE -> new BiLongRedBlackTreeMap<>();
            case BI_LONG_JAVA_TREE -> new BiLongJavaTreeMap();
        };

        for (long i = 0; i < firstKeySize; i++) {
            for (long j = 0; j < secondKeySize; j++) {
                map.put(i, j, i * firstKeySize + j * secondKeySize);
            }
        }

    }

    @Benchmark
    public void benchmarkIterate(final Blackhole bh) {
        map.forEach(bh::consume);
    }

    @Benchmark
    public void benchmarkPut(final Blackhole bh) {
        bh.consume(map.put(firstKeySize, secondKeySize, (long) firstKeySize));
    }

    @Benchmark
    public void benchmarkRemove(final Blackhole bh) {
        bh.consume(map.remove(firstKeySize / 2, secondKeySize / 2));
    }

    @Benchmark
    public void benchmarkContainsKey(final Blackhole bh) {
        bh.consume(map.containsKey(firstKeySize / 2, secondKeySize / 2));
    }

    public enum MapType {
        BI_LONG_RED_BLACK_TREE,
        BI_LONG_JAVA_TREE
    }
}
