package com.rev.playground.datastructures.tm;

import com.rev.playground.datastructures.ll.MemoryManager;

import java.util.function.Consumer;

public class ArrayBackedBiLongToLongRedBlackTreeMapV2 implements BiLongToLongMap {

    private static final boolean BLACK = true;
    private static final boolean RED = false;

    private static final int NIL = -1;

    final MemoryManager memoryManager;

    // long             long            long       long         long
    // parent,color     left,right      key0       key1         value
    private static final int PARENT_OR_COLOR_INDEX = 0;
    private static final int LEFT_OR_RIGHT_INDEX = 1;
    private static final int KEY0_INDEX = 2;
    private static final int KEY1_INDEX = 3;
    private static final int VALUE_INDEX = 4;
    private final long[] packedArray;

    //Pointers
    private int root = NIL;

    //The stored values

    private transient int size = 0;
    private transient int modCount = 0;

    private ArrayBackedBiLongToLongRedBlackTreeMapV2(final MemoryManager memoryManager, long[] packedArray) {
        this.memoryManager = memoryManager;
        this.packedArray = packedArray;

    }

    public static ArrayBackedBiLongToLongRedBlackTreeMapV2 create(int size) {

        final MemoryManager memoryManager = MemoryManager.create(size);

        return new ArrayBackedBiLongToLongRedBlackTreeMapV2(
                memoryManager,
                new long[5 * size]
        );
    }

    @Override
    public final long put(
            final long key0,
            final long key1,
            final long value) {
        int t = root;

        if (t == NIL) {
            root = newEntry(NIL, NIL, NIL, BLACK, key0, key1, value);
            return value;
        }

        int cmp;
        int parent;
        do {
            parent = t;
            cmp = cmp(key0, key1, getKey0(t), getKey1(t));
            if (cmp == -1) {
                t = getLeft(t);
            } else if (cmp == 1) {
                t = getRight(t);
            } else {
                long oldValue = getValue(t);
                setValue(t, value);
                return oldValue;
            }
        } while (t != NIL);
        addEntry(key0, key1, value, parent, cmp < 0);
        return Long.MIN_VALUE;
    }

    private int newEntry(final int parent, final int left, final int right, final boolean color,
                         final long key0, final long key1, final long value) {
        final int address = memoryManager.allocate();
        setParent(address, parent);
        setLeft(address, left);
        setRight(address, right);
        setKey0(address, key0);
        setKey1(address, key1);
        setValue(address, value);
        setColorBlah(address, color);

        return address;
    }

    @Override
    public long get(final long key0, final long key1) {
        int t = getEntry(key0, key1);
        return (t == NIL ? Long.MIN_VALUE : getValue(t));
    }

    final int getEntry(final long key0, final long key1) {
        int p = root;
        while (p != NIL) {
            int cmp = cmp(key0, key1, getKey0(p), getKey1(p));
            if (cmp < 0) {
                p = getLeft(p);
            } else if (cmp > 0) {
                p = getRight(p);
            } else {
                return p;
            }
        }
        return NIL;
    }

    @Override
    public void forEach(final Consumer<Long> consumer) {
        consume(root, consumer);
    }

    @Override
    public void forEach(final BiLongMapConsumer<Long> consumer) {
        consume(root, consumer);
    }

    private void consume(int e, Consumer<Long> consumer) {
        if (e == NIL) {
            return;
        }
        consume(getLeft(e), consumer);
        consumer.accept(getValue(e));
        consume(getRight(e), consumer);
    }

    private void consume(int e, BiLongMapConsumer<Long> consumer) {
        if (e == NIL) {
            return;
        }
        consume(getLeft(e), consumer);
        consumer.accept(getKey0(e), getKey1(e), getValue(e));
        consume(getRight(e), consumer);
    }

    @Override
    public long remove(final long key0, final long key1) {
        int t = getEntry(key0, key1);
        if (t == NIL) {
            return Long.MIN_VALUE;
        }

        long oldValue = getValue(t);
        deleteEntry(t);
        return oldValue;
    }

    @Override
    public boolean containsKey(final long key0, final long key1) {
        return getEntry(key0, key1) != NIL;
    }

    private void deleteEntry(int t) {
        modCount++;
        size--;

        // If strictly internal, copy successor's element to p and then make p
        // point to successor.
        if (getLeft(t) != NIL && getRight(t) != NIL) {
            int s = successor(t);
            setKey0(t, getKey0(s));
            setKey1(t, getKey1(s));
            setValue(t, getValue(s));
            t = s;
        } // p has 2 children

        // Start fixup at replacement node, if it exists.
        int replacement = (getLeft(t) != NIL ? getLeft(t) : getRight(t));

        if (replacement != NIL) {
            // Link replacement to parent
            setParent(replacement, getParent(t));
            if (getParent(t) == NIL) {
                root = replacement;
            } else if (t == getLeft(getParent(t))) {
                setLeft(getParent(t), replacement);
            } else {
                setRight(getParent(t), replacement);
            }

            // Null out links so they are OK to use by fixAfterDeletion.
            setLeft(t, NIL);
            setRight(t, NIL);
            setParent(t, NIL);

            // Fix replacement
            if (getColor(t) == BLACK) {
                fixAfterDeletion(replacement);
            }
        } else if (getParent(t) == NIL) { // return if we are the only node.
            root = NIL;
        } else { //  No children. Use self as phantom replacement and unlink.
            if (getColor(t) == BLACK) {
                fixAfterDeletion(t);
            }

            if (getParent(t) != NIL) {
                if (t == getLeft(getParent(t))) {
                    setLeft(getParent(t), NIL);
                } else if (t == getRight(getParent(t))) {
                    setRight(getParent(t), NIL);
                }
                setParent(t, NIL);
            }
        }

        memoryManager.release(t);
    }

    private void fixAfterDeletion(int x) {
        while (x != NIL && colorOf(x) == BLACK) {
            if (x == leftOf(parentOf(x))) {
                int sib = rightOf(parentOf(x));

                if (colorOf(sib) == RED) {
                    setColor(sib, BLACK);
                    setColor(parentOf(x), RED);
                    rotateLeft(parentOf(x));
                    sib = rightOf(parentOf(x));
                }

                if (colorOf(leftOf(sib)) == BLACK &&
                        colorOf(rightOf(sib)) == BLACK) {
                    setColor(sib, RED);
                    x = parentOf(x);
                } else {
                    if (colorOf(rightOf(sib)) == BLACK) {
                        setColor(leftOf(sib), BLACK);
                        setColor(sib, RED);
                        rotateRight(sib);
                        sib = rightOf(parentOf(x));
                    }
                    setColor(sib, colorOf(parentOf(x)));
                    setColor(parentOf(x), BLACK);
                    setColor(rightOf(sib), BLACK);
                    rotateLeft(parentOf(x));
                    x = root;
                }
            } else { // symmetric
                int sib = leftOf(parentOf(x));

                if (colorOf(sib) == RED) {
                    setColor(sib, BLACK);
                    setColor(parentOf(x), RED);
                    rotateRight(parentOf(x));
                    sib = leftOf(parentOf(x));
                }

                if (colorOf(rightOf(sib)) == BLACK &&
                        colorOf(leftOf(sib)) == BLACK) {
                    setColor(sib, RED);
                    x = parentOf(x);
                } else {
                    if (colorOf(leftOf(sib)) == BLACK) {
                        setColor(rightOf(sib), BLACK);
                        setColor(sib, RED);
                        rotateLeft(sib);
                        sib = leftOf(parentOf(x));
                    }
                    setColor(sib, colorOf(parentOf(x)));
                    setColor(parentOf(x), BLACK);
                    setColor(leftOf(sib), BLACK);
                    rotateRight(parentOf(x));
                    x = root;
                }
            }
        }

        setColor(x, BLACK);
    }

    private int successor(int t) {
        if (t == NIL) {
            return NIL;
        } else if (getRight(t) != NIL) {
            int p = getRight(t);
            while (getLeft(p) != NIL)
                p = getLeft(p);
            return p;
        } else {
            int p = getParent(t);
            int ch = t;
            while (p != NIL && ch == getRight(p)) {
                ch = p;
                p = getParent(p);
            }
            return p;
        }
    }

    private void addEntry(
            final long key0,
            final long key1,
            final long value,
            final int p,
            final boolean leftSide) {
        int e = newEntry(p, NIL, NIL, BLACK, key0, key1, value);

        if (leftSide) {
            setLeft(p, e);
        } else {
            setRight(p, e);
        }
        fixAfterInsertion(e);
        size++;
        modCount++;
    }

    private void fixAfterInsertion(final int input) {
        int x = input;
        setColor(x, RED);

        while (x != NIL && x != root && getColor(getParent(x)) == RED) {
            if (parentOf(x) == leftOf(parentOf(parentOf(x)))) {
                int y = rightOf(parentOf(parentOf(x)));
                if (colorOf(y) == RED) {
                    setColor(parentOf(x), BLACK);
                    setColor(y, BLACK);
                    setColor(parentOf(parentOf(x)), RED);
                    x = parentOf(parentOf(x));
                } else {
                    if (x == rightOf(parentOf(x))) {
                        x = parentOf(x);
                        rotateLeft(x);
                    }
                    setColor(parentOf(x), BLACK);
                    setColor(parentOf(parentOf(x)), RED);
                    rotateRight(parentOf(parentOf(x)));
                }
            } else {
                int y = leftOf(parentOf(parentOf(x)));
                if (colorOf(y) == RED) {
                    setColor(parentOf(x), BLACK);
                    setColor(y, BLACK);
                    setColor(parentOf(parentOf(x)), RED);
                    x = parentOf(parentOf(x));
                } else {
                    if (x == leftOf(parentOf(x))) {
                        x = parentOf(x);
                        rotateRight(x);
                    }
                    setColor(parentOf(x), BLACK);
                    setColor(parentOf(parentOf(x)), RED);
                    rotateLeft(parentOf(parentOf(x)));
                }
            }
        }
        setColor(root, BLACK);
    }

    private void rotateLeft(int p) {
        if (p != NIL) {
            int r = getRight(p);
            setRight(p, getLeft(r));
            if (getLeft(r) != NIL) {
                setParent(getLeft(r), p);
            }
            setParent(r, getParent(p));
            if (getParent(p) == NIL) {
                root = r;
            } else if (getLeft(getParent(p)) == p) {
                setLeft(getParent(p), r);
            } else {
                setRight(getParent(p), r);
            }
            setLeft(r, p);
            setParent(p, r);
        }
    }

    /**
     * From CLR
     */
    private void rotateRight(int p) {
        if (p != NIL) {
            int l = getLeft(p);
            setLeft(p, getRight(l));
            if (getRight(l) != NIL) {
                setParent(getRight(l), p);
            }
            setParent(l, getParent(p));
            if (getParent(p) == NIL) {
                root = l;
            } else if (getRight(getParent(p)) == p) {
                setRight(getParent(p), l);
            } else {
                setLeft(getParent(p), l);
            }
            setRight(l, p);
            setParent(p, l);
        }
    }

    private boolean colorOf(int t) {
        return (t == NIL ? BLACK : getColor(t));
    }

    private int parentOf(int t) {
        return (t == NIL ? NIL : getParent(t));
    }

    private void setColor(
            int addr,
            boolean c) {
        if (addr != NIL) {
            setColorBlah(addr, c);
        }
    }

    private int leftOf(int t) {
        return (t == NIL) ? NIL : getLeft(t);
    }

    private int rightOf(int t) {
        return (t == NIL) ? NIL : getRight(t);
    }

    private static int cmp(
            final long key0,
            final long key1,
            final long otherKey0,
            final long otherKey1) {
        if (key0 < otherKey0 || (key0 == otherKey0 && key1 < otherKey1)) {
            return -1;
        } else if (key0 > otherKey0 || key1 > otherKey1) {
            return 1;
        }
        return 0;
    }

    private boolean getColor(int addr) {
        return (packedArray[5 * addr] & 0x1L) == 0x1L;
    }

    private void setColorBlah(
            int addr,
            boolean c) {
        if (addr != NIL) {
            long old = packedArray[5 * addr + PARENT_OR_COLOR_INDEX];
            long n = (old & ~0x1L) | (c ? 0x1L : 0x0L);
            packedArray[5 * addr + PARENT_OR_COLOR_INDEX] = n;
        }
    }

    private int getParent(int addr) {
        return (int) (packedArray[5 * addr] >> 32);
    }

    private void setParent(int addr, int parent) {
        long old = packedArray[5 * addr + PARENT_OR_COLOR_INDEX];
        packedArray[5 * addr + PARENT_OR_COLOR_INDEX] = (old & 0x00000000FFFFFFFFL) | ((long)parent) << 32;
    }

    private long getValue(int addr) {
        return packedArray[5 * addr + VALUE_INDEX];
    }

    private void setValue(int addr, long value) {
        packedArray[5 * addr + VALUE_INDEX] = value;
    }

    private int getRight(int addr) {
        return (int) (packedArray[5 * addr + LEFT_OR_RIGHT_INDEX]);
    }

    private void setRight(int addr, int right) {
        long old = packedArray[5 * addr + LEFT_OR_RIGHT_INDEX];
        packedArray[5 * addr + LEFT_OR_RIGHT_INDEX] = (old & 0xFFFFFFFF00000000L) | ((long)right & 0xFFFFFFFFL);
    }

    private int getLeft(int addr) {
        return (int) (packedArray[5 * addr + LEFT_OR_RIGHT_INDEX] >> 32);
    }

    private void setLeft(int addr, int left) {
        long old = packedArray[5 * addr + LEFT_OR_RIGHT_INDEX];
        packedArray[5 * addr + LEFT_OR_RIGHT_INDEX] = (old & 0x00000000FFFFFFFFL) | ((long)left) << 32;
    }

    private long getKey0(int addr) {
        return packedArray[5 * addr + KEY0_INDEX];
    }

    private void setKey0(int addr, long key0) {
        packedArray[5 * addr + KEY0_INDEX] = key0;
    }

    private long getKey1(int addr) {
        return packedArray[5 * addr + KEY1_INDEX];
    }

    private void setKey1(int addr, long key1) {
        packedArray[5 * addr + KEY1_INDEX] = key1;
    }
}
