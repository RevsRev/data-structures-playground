package com.rev.playground.datastructures.tm;

import com.rev.playground.datastructures.ll.MemoryManager;

import java.util.Arrays;
import java.util.function.Consumer;

public class ArrayBackedBiLongRedBlackTreeMap<T> implements BiLongMap<T> {

    private static final boolean BLACK = true;
    private static final boolean RED = false;

    private static final int NIL = -1;

    final MemoryManager memoryManager;

    //Pointers
    private int root = NIL;
    private final int[] parent;
    private final int[] left;
    private final int[] right;

    //The stored values
    private final long[] key0;
    private final long[] key1;
    private final boolean[] color;
    private final T[] value;

    private transient int size = 0;
    private transient int modCount = 0;

    private ArrayBackedBiLongRedBlackTreeMap(final MemoryManager memoryManager,
                                             final int[] parent,
                                             final int[] left,
                                             final int[] right,
                                             final long[] key0,
                                             final long[] key1,
                                             final boolean[] color,
                                             final T[] value) {
        this.memoryManager = memoryManager;
        this.parent = parent;
        this.left = left;
        this.right = right;

        this.key0 = key0;
        this.key1 = key1;
        this.value = value;
        this.color = color;
    }

    public static <T> ArrayBackedBiLongRedBlackTreeMap<T> create(int size, final T[] copyMe) {

        final MemoryManager memoryManager = MemoryManager.create(size);
        final int[] parent = new int[size];
        final int[] left = new int[size];
        final int[] right = new int[size];

        return new ArrayBackedBiLongRedBlackTreeMap<>(
                memoryManager,
                parent,
                left,
                right,
                new long[size],
                new long[size],
                new boolean[size],
                Arrays.copyOf(Arrays.copyOf(copyMe, 0), size));
    }

    @Override
    public final T put(
            final long key0,
            final long key1,
            final T value) {
        int t = root;

        if (t == NIL) {
            root = newEntry(NIL, NIL, NIL, BLACK, key0, key1, value);
            return value;
        }

        int cmp;
        int parent;
        do {
            parent = t;
            cmp = cmp(key0, key1, this.key0[t], this.key1[t]);
            if (cmp == -1) {
                t = left[t];
            } else if (cmp == 1) {
                t = right[t];
            } else {
                T oldValue = this.value[t];
                this.value[t] = value;
                return oldValue;
            }
        } while (t != NIL);
        addEntry(key0, key1, value, parent, cmp < 0);
        return null;
    }

    private int newEntry(final int parent, final int left, final int right, final boolean color,
                         final long key0, final long key1, final T value) {
        final int address = memoryManager.allocate();
        this.parent[address] = parent;
        this.left[address] = left;
        this.right[address] = right;
        this.key0[address] = key0;
        this.key1[address] = key1;
        this.value[address] = value;
        this.color[address] = color;

        return address;
    }

    @Override
    public T get(final long key0, final long key1) {
        int t = getEntry(key0, key1);
        return (t == NIL ? null : value[t]);
    }

    final int getEntry(final long key0, final long key1) {
        int p = root;
        while (p != NIL) {
            int cmp = cmp(key0, key1, this.key0[p], this.key1[p]);
            if (cmp < 0) {
                p = left[p];
            } else if (cmp > 0) {
                p = right[p];
            } else {
                return p;
            }
        }
        return NIL;
    }

    @Override
    public void forEach(final Consumer<T> consumer) {
        consume(root, consumer);
    }

    @Override
    public void forEach(final BiLongMapConsumer<T> consumer) {
        consume(root, consumer);
    }

    private void consume(int e, Consumer<T> consumer) {
        if (e == NIL) {
            return;
        }
        consume(left[e], consumer);
        consumer.accept(value[e]);
        consume(right[e], consumer);
    }

    private void consume(int e, BiLongMapConsumer<T> consumer) {
        if (e == NIL) {
            return;
        }
        consume(left[e], consumer);
        consumer.accept(key0[e], key1[e], value[e]);
        consume(right[e], consumer);
    }

    @Override
    public T remove(final long key0, final long key1) {
        int t = getEntry(key0, key1);
        if (t == NIL) {
            return null;
        }

        T oldValue = value[t];
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
        if (left[t] != NIL && right[t] != NIL) {
            int s = successor(t);
            key0[t] = key0[s];
            key1[t] = key1[s];
            value[t] = value[s];
            t = s;
        } // p has 2 children

        // Start fixup at replacement node, if it exists.
        int replacement = (left[t] != NIL ? left[t] : right[t]);

        if (replacement != NIL) {
            // Link replacement to parent
            parent[replacement] = parent[t];
            if (parent[t] == NIL) {
                root = replacement;
            } else if (t == left[parent[t]]) {
                left[parent[t]] = replacement;
            } else {
                right[parent[t]] = replacement;
            }

            // Null out links so they are OK to use by fixAfterDeletion.
            left[t] = right[t] = parent[t] = NIL;

            // Fix replacement
            if (color[t] == BLACK) {
                fixAfterDeletion(replacement);
            }
        } else if (parent[t] == NIL) { // return if we are the only node.
            root = NIL;
        } else { //  No children. Use self as phantom replacement and unlink.
            if (color[t] == BLACK) {
                fixAfterDeletion(t);
            }

            if (parent[t] != NIL) {
                if (t == left[parent[t]]) {
                    left[parent[t]] = NIL;
                } else if (t == right[parent[t]]) {
                    right[parent[t]] = NIL;
                }
                parent[t] = NIL;
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
        } else if (right[t] != NIL) {
            int p = right[t];
            while (left[p] != NIL)
                p = left[p];
            return p;
        } else {
            int p = parent[t];
            int ch = t;
            while (p != NIL && ch == right[p]) {
                ch = p;
                p = parent[p];
            }
            return p;
        }
    }

    private void addEntry(
            final long key0,
            final long key1,
            final T value,
            final int p,
            final boolean leftSide) {
        int e = newEntry(p, NIL, NIL, BLACK, key0, key1, value);

        if (leftSide) {
            left[p] = e;
        } else {
            right[p] = e;
        }
        fixAfterInsertion(e);
        size++;
        modCount++;
    }

    private void fixAfterInsertion(final int input) {
        int x = input;
        color[x] = RED;

        while (x != NIL && x != root && color[parent[x]] == RED) {
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
        color[root] = BLACK;
    }

    private void rotateLeft(int p) {
        if (p != NIL) {
            int r = right[p];
            right[p] = left[r];
            if (left[r] != NIL) {
                parent[left[r]] = p;
            }
            parent[r] = parent[p];
            if (parent[p] == NIL) {
                root = r;
            } else if (left[parent[p]] == p) {
                left[parent[p]] = r;
            } else {
                right[parent[p]] = r;
            }
            left[r] = p;
            parent[p] = r;
        }
    }

    /**
     * From CLR
     */
    private void rotateRight(int p) {
        if (p != NIL) {
            int l = left[p];
            left[p] = right[l];
            if (right[l] != NIL) {
                parent[right[l]] = p;
            }
            parent[l] = parent[p];
            if (parent[p] == NIL) {
                root = l;
            } else if (right[parent[p]] == p) {
                right[parent[p]] = l;
            } else {
                left[parent[p]] = l;
            }
            right[l] = p;
            parent[p] = l;
        }
    }

    private boolean colorOf(int t) {
        return (t == NIL ? BLACK : color[t]);
    }

    private int parentOf(int t) {
        return (t == NIL ? NIL : parent[t]);
    }

    private void setColor(
            int t,
            boolean c) {
        if (t != NIL) {
            color[t] = c;
        }
    }

    private int leftOf(int t) {
        return (t == NIL) ? NIL : left[t];
    }

    private int rightOf(int t) {
        return (t == NIL) ? NIL : right[t];
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
}
