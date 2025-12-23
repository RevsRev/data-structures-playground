package com.rev.playground.datastructures.ll;

import org.apache.commons.math3.exception.OutOfRangeException;

public class MemoryManager {

    private static final int NIL = -1;

    private final int size;

    //Linked list for free memory
    private int free = 0;
    private final int[] prev;
    private final int[] next;

    private MemoryManager(int size, int[] prev, int[] next) {
        this.size = size;
        this.prev = prev;
        this.next = next;
    }

    public static MemoryManager create(int size) {
        final int[] prev = new int[size];
        final int[] next = new int[size];

        for (int i = 0; i < size; i++) {
            prev[i] = i != 0 ? i - 1 : NIL;
            next[i] = i != size - 1 ? i + 1 : NIL;
        }

        return new MemoryManager(size, prev, next);
    }

    public int allocate() {
        if (free == NIL) {
            throw new OutOfMemoryError("Out of memory");
        }
        int f = free;
        free = next[free];

        delete(f);
        return f;
    }

    public void release(int addr) {
        checkBounds(addr);

        if (free == NIL) {
            free = addr;
            next[free] = NIL;
            return;
        }

        insertBefore(free, addr);
        free = addr;
    }

    private void delete(final int addr) {
        checkBounds(addr);

        int p = prev[addr];
        int n = next[addr];

        if (p != NIL) {
            next[p] = n;
        }

        if (n != NIL) {
            prev[n] = p;
        }
    }

    private void insertBefore(final int insertBefore, final int value) {
        next[value] = insertBefore;
        prev[value] = prev[insertBefore];
        prev[insertBefore] = value;
    }

    private void checkBounds(final int addr) {
        if (addr < 0 || addr >= size) {
            throw new OutOfRangeException(addr, 0, this.prev.length);
        }
    }
}
