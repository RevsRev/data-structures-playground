package com.rev.playground.datastructures.ll;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class MemoryManagerTest {

    @Test
    void shouldInsertAndThenRunOutOfMemory() {
        MemoryManager memoryManager = MemoryManager.create(10);

        for (int i = 0; i < 10; i++) {
            assertEquals(i, memoryManager.allocate());
        }
        assertThrows(OutOfMemoryError.class, memoryManager::allocate);
    }

    @Test
    void shouldFree() {
        MemoryManager memoryManager = MemoryManager.create(10);

        for (int i = 0; i < 5; i++) {
            assertEquals(i, memoryManager.allocate());
        }

        memoryManager.release(3);
        memoryManager.release(1);
        memoryManager.release(4);

        assertEquals(4, memoryManager.allocate());
        assertEquals(1, memoryManager.allocate());
        assertEquals(3, memoryManager.allocate());

        for (int i = 5; i < 10; i++) {
            assertEquals(i, memoryManager.allocate());
        }
    }

    @Test
    void shouldFreeWhenAtMaxCapacity() {
        MemoryManager memoryManager = MemoryManager.create(10);

        for (int i = 0; i < 10; i++) {
            assertEquals(i, memoryManager.allocate());
        }

        memoryManager.release(6);
        memoryManager.release(2);
        memoryManager.release(9);

        assertEquals(9, memoryManager.allocate());
        assertEquals(2, memoryManager.allocate());
        assertEquals(6, memoryManager.allocate());
    }

    @Test
    void shouldFreeEverything() {
        MemoryManager memoryManager = MemoryManager.create(10);

        for (int i = 0; i < 10; i++) {
            assertEquals(i, memoryManager.allocate());
        }

        for (int i = 0; i < 10; i++) {
            memoryManager.release(i);
        }

        for (int i = 9; i >= 0; i--) {
            assertEquals(i, memoryManager.allocate());
        }
    }

    @Test
    void shouldFreeEverythingOppositeOrder() {
        MemoryManager memoryManager = MemoryManager.create(10);

        for (int i = 0; i < 10; i++) {
            assertEquals(i, memoryManager.allocate());
        }

        for (int i = 9; i >= 0; i--) {
            memoryManager.release(i);
        }

        for (int i = 0; i < 10; i++) {
            assertEquals(i, memoryManager.allocate());
        }
    }
}