package com.rev.playground.benchmarks;

import com.rev.playground.datastructures.tm.*;
import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.infra.Blackhole;

@State(Scope.Thread)
@Warmup(iterations = 3, time = 1)
@Measurement(iterations = 3, time = 1)
public class BiLongToLongMapBenchmark {

    @Param({"1", "10", "100", "1000", "10000"})
    int firstKeySize;

//    @Param({"1", "10", "100", "1000"})
    int secondKeySize = 10;

    @Param({"ARRAY_BACKED_BI_LONG_TO_LONG_RED_BLACK_TREE", "ARRAY_BACKED_BI_LONG_TO_LONG_RED_BLACK_TREE_V2"})
    MapType mapType;

    BiLongToLongMap map;

    @Setup(Level.Invocation)
    public void setup() {
        map = switch (mapType) {
            case ARRAY_BACKED_BI_LONG_TO_LONG_RED_BLACK_TREE -> ArrayBackedBiLongToLongRedBlackTreeMap.create(firstKeySize * secondKeySize + 1);
            case ARRAY_BACKED_BI_LONG_TO_LONG_RED_BLACK_TREE_V2 -> ArrayBackedBiLongToLongRedBlackTreeMapV2.create(firstKeySize * secondKeySize + 1);
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
        bh.consume(map.put(firstKeySize, secondKeySize, firstKeySize));
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
        ARRAY_BACKED_BI_LONG_TO_LONG_RED_BLACK_TREE,
        ARRAY_BACKED_BI_LONG_TO_LONG_RED_BLACK_TREE_V2
    }
}
