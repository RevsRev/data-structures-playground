package com.rev.playground.datastructures.tm;

import java.util.function.Consumer;

public class BiLongRedBlackTreeMap<T> implements BiLongMap<T> {

    private static final Entry NIL = new Entry<>(-1, -1, null, null);

    private static final boolean BLACK = true;
    private static final boolean RED = false;

    private Entry<T> root = NIL;

    private transient int size = 0;
    private transient int modCount = 0;

    @Override
    public final T put(
            final long key0,
            final long key1,
            final T value) {
        Entry<T> t = root;

        if (t == NIL) {
            root = new Entry<>(key0, key1, value, NIL);
            return value;
        }

        int cmp;
        Entry<T> parent;
        do {
            parent = t;
            cmp = cmp(key0, key1, t);
            if (cmp == -1) {
                t = t.left;
            } else if (cmp == 1) {
                t = t.right;
            } else {
                T oldValue = t.value;
                t.value = value;
                return oldValue;
            }
        } while (t != NIL);
        addEntry(key0, key1, value, parent, cmp < 0);
        return null;
    }

    @Override
    public T get(final long key0, final long key1) {
        Entry<T> p = getEntry(key0, key1);
        return (p == null ? null : p.value);
    }

    final Entry<T> getEntry(final long key0, final long key1) {
        Entry<T> p = root;
        while (p != NIL) {
            int cmp = cmp(key0, key1, p);
            if (cmp < 0) {
                p = p.left;
            } else if (cmp > 0) {
                p = p.right;
            } else {
                return p;
            }
        }
        return null;
    }

    @Override
    public void forEach(final Consumer<T> consumer) {
        consume(root, consumer);
    }

    @Override
    public void forEach(final BiLongMapConsumer<T> consumer) {
        consume(root, consumer);
    }

    private void consume(Entry<T> e, Consumer<T> consumer) {
        if (e == NIL) {
            return;
        }
        consume(e.left, consumer);
        consumer.accept(e.value);
        consume(e.right, consumer);
    }

    private void consume(Entry<T> e, BiLongMapConsumer<T> consumer) {
        if (e == NIL) {
            return;
        }
        consume(e.left, consumer);
        consumer.accept(e.key0, e.key1, e.value);
        consume(e.right, consumer);
    }

    @Override
    public T remove(final long key0, final long key1) {
        Entry<T> p = getEntry(key0, key1);
        if (p == NIL) {
            return null;
        }

        T oldValue = p.value;
        deleteEntry(p);
        return oldValue;
    }

    private void deleteEntry(Entry<T> p) {
        modCount++;
        size--;

        // If strictly internal, copy successor's element to p and then make p
        // point to successor.
        if (p.left != NIL && p.right != NIL) {
            Entry<T> s = successor(p);
            p.key0 = s.key0;
            p.key1 = s.key1;
            p.value = s.value;
            p = s;
        } // p has 2 children

        // Start fixup at replacement node, if it exists.
        Entry<T> replacement = (p.left != NIL ? p.left : p.right);

        if (replacement != NIL) {
            // Link replacement to parent
            replacement.parent = p.parent;
            if (p.parent == NIL) {
                root = replacement;
            } else if (p == p.parent.left) {
                p.parent.left = replacement;
            } else {
                p.parent.right = replacement;
            }

            // Null out links so they are OK to use by fixAfterDeletion.
            p.left = p.right = p.parent = NIL;

            // Fix replacement
            if (p.color == BLACK) {
                fixAfterDeletion(replacement);
            }
        } else if (p.parent == NIL) { // return if we are the only node.
            root = NIL;
        } else { //  No children. Use self as phantom replacement and unlink.
            if (p.color == BLACK) {
                fixAfterDeletion(p);
            }

            if (p.parent != NIL) {
                if (p == p.parent.left) {
                    p.parent.left = NIL;
                } else if (p == p.parent.right) {
                    p.parent.right = NIL;
                }
                p.parent = NIL;
            }
        }
    }

    private void fixAfterDeletion(Entry<T> x) {
        while (x != root && colorOf(x) == BLACK) {
            if (x == leftOf(parentOf(x))) {
                Entry<T> sib = rightOf(parentOf(x));

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
                Entry<T> sib = leftOf(parentOf(x));

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

    static <T> Entry<T> successor(Entry<T> t) {
        if (t == NIL) {
            return NIL;
        } else if (t.right != NIL) {
            Entry<T> p = t.right;
            while (p.left != NIL)
                p = p.left;
            return p;
        } else {
            Entry<T> p = t.parent;
            Entry<T> ch = t;
            while (p != NIL && ch == p.right) {
                ch = p;
                p = p.parent;
            }
            return p;
        }
    }

    private void addEntry(
            final long key0,
            final long key1,
            final T value,
            final Entry<T> p,
            final boolean leftSide) {
        final Entry<T> e = new Entry<>(key0, key1, value, p);

        if (leftSide) {
            p.left = e;
        } else {
            p.right = e;
        }
        fixAfterInsertion(e);
        size++;
        modCount++;
    }

    private void fixAfterInsertion(final Entry<T> input) {
        Entry<T> x = input;
        x.color = RED;

        while (x != NIL && x != root && x.parent.color == RED) {
            if (parentOf(x) == leftOf(parentOf(parentOf(x)))) {
                Entry<T> y = rightOf(parentOf(parentOf(x)));
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
                Entry<T> y = leftOf(parentOf(parentOf(x)));
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
        root.color = BLACK;
    }

    private void rotateLeft(Entry<T> p) {
        if (p != NIL) {
            Entry<T> r = p.right;
            p.right = r.left;
            if (r.left != NIL) {
                r.left.parent = p;
            }
            r.parent = p.parent;
            if (p.parent == NIL) {
                root = r;
            } else if (p.parent.left == p) {
                p.parent.left = r;
            } else {
                p.parent.right = r;
            }
            r.left = p;
            p.parent = r;
        }
    }

    /**
     * From CLR
     */
    private void rotateRight(Entry<T> p) {
        if (p != NIL) {
            Entry<T> l = p.left;
            p.left = l.right;
            if (l.right != NIL) {
                l.right.parent = p;
            }
            l.parent = p.parent;
            if (p.parent == NIL) {
                root = l;
            } else if (p.parent.right == p) {
                p.parent.right = l;
            } else {
                p.parent.left = l;
            }
            l.right = p;
            p.parent = l;
        }
    }

    private static <T> boolean colorOf(Entry<T> p) {
        return (p == NIL ? BLACK : p.color);
    }

    private static <T> Entry<T> parentOf(Entry<T> p) {
        return (p == NIL ? NIL : p.parent);
    }

    private static <T> void setColor(
            Entry<T> p,
            boolean c) {
        if (p != NIL) {
            p.color = c;
        }
    }

    private static <T> Entry<T> leftOf(Entry<T> p) {
        return (p == NIL) ? NIL : p.left;
    }

    private static <T> Entry<T> rightOf(Entry<T> p) {
        return (p == NIL) ? NIL : p.right;
    }

    private static <T> int cmp(
            final long key0,
            final long key1,
            final Entry<T> t) {
        if (key0 < t.key0 || (key0 == t.key0 && key1 < t.key1)) {
            return -1;
        } else if (key0 > t.key0 || key1 > t.key1) {
            return 1;
        }
        return 0;
    }

    private static final class Entry<T> {
        private long key0;
        private long key1;
        private T value;

        private Entry<T> parent;
        private Entry<T> left = NIL;
        private Entry<T> right = NIL;
        private boolean color = BLACK;

        public Entry(
                final long key0,
                final long key1,
                final T value,
                final Entry<T> p) {
            this.key0 = key0;
            this.key1 = key1;
            this.value = value;
            this.parent = p;
        }
    }
}
