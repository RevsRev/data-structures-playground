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
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.TreeSet;

@State(Scope.Thread)
@Warmup(iterations = 3, time = 1)
@Measurement(iterations = 3, time = 1)
public class CollectionBenchmark {

    @Param({"1", "10", "100", "1000", "10000", "100000", "1000000"})
    int size;

    @Param({"ARRAY_LIST", "LINKED_LIST", "TREE_SET", "HASH_SET"})
    CollectionType collectionType;

    Collection<Long> collection;

    @Setup(Level.Invocation)
    public void setup()
    {
        final List<Long> seed = new ArrayList<>(size);
        for (long l = 0; l < size; l++) {
            seed.add(l);
        }

        collection = switch (collectionType) {
            case ARRAY_LIST -> new ArrayList<>(seed);
            case LINKED_LIST -> new LinkedList<>(seed);
            case HASH_SET -> new HashSet<>(seed);
            case TREE_SET -> new TreeSet<>(seed);
        };
    }

    @Benchmark
    public void benchmarkIteration(final Blackhole bh)
    {
        for (final Long l : collection) {
            bh.consume(l);
        }
    }

    @Benchmark
    public void benchmarkIterateAndRemove(final Blackhole bh)
    {
        int i = 0;
        final Iterator<Long> iterator = collection.iterator();
        while (iterator.hasNext()) {
            bh.consume(iterator.next());
            if (i % 10 == 0) {
                iterator.remove();
            }
            i++;
        }
    }

    @Benchmark
    public void benchmarkAddElement(final Blackhole bh)
    {
        bh.consume(collection.add(8766L));
    }

    @Benchmark
    public void benchmarkRemoveElementInMiddle(final Blackhole bh)
    {
        bh.consume(collection.remove((long) size / 2));
    }

    @Benchmark
    public void benchmarkAddElementMaybeInMiddle(final Blackhole bh)
    {
        collection.add((long) size /2);
    }

    @Benchmark
    public void benchmarkContains(final Blackhole bh) {
        bh.consume(collection.contains((long) size / 2));
    }

    public enum CollectionType {
        ARRAY_LIST,
        LINKED_LIST,
        HASH_SET,
        TREE_SET
    }
}
