package com.rev.playground.datastructures.tm;

import java.util.Comparator;
import java.util.TreeMap;
import java.util.function.Consumer;

public class BiLongJavaTreeMap implements BiLongMap<Long> {

    final TreeMap<Key, Long> delegate = new TreeMap<>();

    @Override
    public Long put(long key0, long key1, Long value) {
        return delegate.put(new Key(key0, key1), value);
    }

    @Override
    public Long get(long key0, long key1) {
        return delegate.get(new Key(key0, key1));
    }

    @Override
    public void forEach(Consumer<Long> consumer) {
        delegate.forEach((key, value) -> consumer.accept(value));
    }

    @Override
    public void forEach(BiLongMapConsumer<Long> consumer) {
        delegate.forEach((key, value) -> consumer.accept(key.key0, key.key1, value));
    }

    @Override
    public Long remove(long key0, long key1) {
        return delegate.remove(new Key(key0, key1));
    }

    @Override
    public boolean containsKey(long key0, long key1) {
        return delegate.containsKey(new Key(key0, key1));
    }

    private record Key(long key0, long key1) implements Comparable<Key>{

        public static final Comparator<Key> KEY_COMPARATOR =
                Comparator.comparingLong(Key::key0).thenComparing(Key::key1);

        @Override
        public int compareTo(final Key o) {
            return KEY_COMPARATOR.compare(this, o);
        }
    }
}
