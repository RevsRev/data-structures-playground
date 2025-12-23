package com.rev.playground.datastructures.tm;


import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedClass;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.ArrayList;
import java.util.List;
import java.util.TreeMap;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.Supplier;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

@ParameterizedClass(name = "{0}")
@MethodSource("getMapInstance")
public class BiLongMapTest {

    private final Supplier<BiLongMap<Long>> mapSupplier;

    public BiLongMapTest(final String name, final Supplier<BiLongMap<Long>> mapSupplier) {
        this.mapSupplier = mapSupplier;
    }

    public static Stream<Arguments> getMapInstance() {
        final Supplier<BiLongMap<Long>> biLongRedBlackTreeMapFactory = BiLongRedBlackTreeMap::new;
        final Supplier<BiLongMap<Long>> biLongJavaTreeMapFactory = BiLongJavaTreeMap::new;
        return Stream.of(
                Arguments.of("BiLongRedBlackTreeMap", biLongRedBlackTreeMapFactory),
                Arguments.of("BiLongJavaTreeMap", biLongJavaTreeMapFactory)
        );
    }

    @Test
    public void forEachTest() {

        final BiLongMap<Long> x = mapSupplier.get();
        x.put(0, 1, 12L);
        x.put(1, 1, 13L);
        x.put(0, 1, 14L);

        final List<Long> actual = new ArrayList<>();
        x.forEach(actual::add);

        assertEquals(List.of(14L, 13L), actual);
    }

    @Test
    public void deleteTest() {
        final BiLongMap<Long> x = mapSupplier.get();
        x.put(0, 1, 12L);
        x.put(1, 1, 13L);
        x.put(0, 1, 14L);

        assertEquals((Long) 13L, x.get(1, 1));

        x.remove(1, 1);

        assertNull(x.get(1, 1));
    }

    @Test
    public void getTest() {
        final BiLongMap<Long> x = mapSupplier.get();
        x.put(0, 1, 12L);
        assertEquals((Long) 12L, x.get(0, 1));

        x.put(1, 1, 13L);
        assertEquals((Long) 13L, x.get(1, 1));

        x.put(0, 1, 14L);
        assertEquals((Long) 14L, x.get(0, 1));
    }

    @Test
    public void massiveFuzzTest() {
        final BiLongMap<Long> x = mapSupplier.get();
        final TreeMap<BiLong, Long> y = new TreeMap<>();
        final ThreadLocalRandom tlr = ThreadLocalRandom.current();

        for (int i = 0; i < 100_000; i++) {
            final long f = tlr.nextLong();
            final long s = tlr.nextLong();
            final long p = tlr.nextLong();

            x.put(f, s, p);
            y.put(new BiLong(f, s), p);

            if (tlr.nextInt(0, 100) > 75) {
                // replace sometimes
                final long u = tlr.nextLong();
                x.put(f, s, u);
                y.put(new BiLong(f, s), u);
            }

            if (tlr.nextInt(0, 100) > 85) {
                // delete sometimes
                x.remove(f, s);
                y.remove(new BiLong(f, s));
            }
        }

        y.forEach((bl, p) -> assertEquals(p, x.get(bl.f, bl.s)));
        x.forEach((f, s, p) -> assertEquals(p, y.get(new BiLong(f, s))));
    }

    private record BiLong(long f, long s) implements Comparable<BiLong> {
        @Override
        public int compareTo(final BiLong other) {
            final int cmp = Long.compare(this.f, other.f);
            if (cmp != 0) {
                return cmp;
            }
            return Long.compare(this.s, other.s);
        }
    }
}