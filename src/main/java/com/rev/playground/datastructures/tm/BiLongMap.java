package com.rev.playground.datastructures.tm;

import java.util.function.Consumer;

public interface BiLongMap<T> {
    T put(
            long key0,
            long key1,
            T value);

    T get(long key0, long key1);

    void forEach(Consumer<T> consumer);

    void forEach(BiLongMapConsumer<T> consumer);

    T remove(long key0, long key1);
}
