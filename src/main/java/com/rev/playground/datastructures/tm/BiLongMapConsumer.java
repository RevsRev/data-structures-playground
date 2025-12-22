package com.rev.playground.datastructures.tm;

public interface BiLongMapConsumer<T> {
    void accept(long key0, long key1, T t);
}
