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

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

@State(Scope.Thread)
@Warmup(iterations = 3, time = 1)
@Measurement(iterations = 3, time = 1)
public class ListBenchmark {

    @Param({"1", "10", "100", "1000", "10000", "100000", "1000000"})
    int size;

    @Param({"ARRAY_LIST", "LINKED_LIST"})
    ListType listType;

    List<Long> list;

    @Setup(Level.Invocation)
    public void setup()
    {
        final List<Long> seed = new ArrayList<>(size);
        for (long l = 0; l < size; l++) {
            seed.add(l);
        }

        list = switch (listType) {
            case ARRAY_LIST -> new ArrayList<>(seed);
            case LINKED_LIST -> new LinkedList<>(seed);
        };
    }

    @Benchmark
    public void benchmarkIteration(final Blackhole bh)
    {
        for (final Long l : list) {
            bh.consume(l);
        }
    }

    @Benchmark
    public void benchmarkIterateAndRemove(final Blackhole bh)
    {
        int i = 0;
        final Iterator<Long> iterator = list.iterator();
        while (iterator.hasNext()) {
            bh.consume(iterator.next());
            if (i % 10 == 0) {
                iterator.remove();
            }
            i++;
        }
    }

    @Benchmark
    //One of Ed's favourite phone screening questions
    public void benchmarkRemoveHalfway(final Blackhole bh)
    {
        bh.consume(list.remove(size / 2));
    }

    @Benchmark
    public void benchmarkAddHalfway(final Blackhole bh)
    {
        list.add(size/2, 1234L);
    }

    @Benchmark
    public void benchmarkSearch(final Blackhole bh) {
        bh.consume(Collections.binarySearch(list, (long)size - 1));
    }

    public enum ListType {
        ARRAY_LIST,
        LINKED_LIST
    }
}
