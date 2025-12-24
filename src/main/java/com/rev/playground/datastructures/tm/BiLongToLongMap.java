package com.rev.playground.datastructures.tm;

import java.util.function.Consumer;

public interface BiLongToLongMap {
    long put(
            long key0,
            long key1,
            long value);

    long get(long key0, long key1);

    void forEach(Consumer<Long> consumer);

    void forEach(BiLongMapConsumer<Long> consumer);

    long remove(long key0, long key1);

    boolean containsKey(long key0, long key1);
}
