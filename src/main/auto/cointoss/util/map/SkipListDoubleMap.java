/*
 * Copyright (C) 2019 Nameless Production Committee
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          http://opensource.org/licenses/mit-license.php
 */
package cointoss.util.map;

import java.lang.invoke.VarHandle;
import java.util.AbstractCollection;
import java.util.AbstractMap;
import java.util.AbstractSet;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.Map;
import java.util.NavigableSet;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.Set;
import java.util.SortedMap;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.concurrent.ConcurrentNavigableMap;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.atomic.AtomicReferenceFieldUpdater;
import java.util.concurrent.atomic.LongAdder;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Predicate;

import java.util.function.DoubleFunction;
import cointoss.util.set.NavigableDoubleSet;
import kiss.I;

/**
 * A scalable concurrent {@link ConcurrentNavigableMap} implementation. The map is sorted according
 * to the {@linkplain Comparable natural ordering} of its keys, or by a {@link Comparator} provided
 * at map creation time, depending on which constructor is used.
 * <p>
 * This class implements a concurrent variant of
 * <a href="http://en.wikipedia.org/wiki/Skip_list" target="_top">SkipLists</a> providing expected
 * average <i>log(n)</i> time cost for the {@code containsKey}, {@code get}, {@code put} and
 * {@code remove} operations and their variants. Insertion, removal, update, and access operations
 * safely execute concurrently by multiple threads.
 * <p>
 * Iterators and spliterators are <a href="package-summary.html#Weakly"><i>weakly
 * consistent</i></a>.
 * <p>
 * Ascending key ordered views and their iterators are faster than descending ones.
 * <p>
 * All {@code Map.Entry} pairs returned by methods in this class and its views represent snapshots
 * of mappings at the time they were produced. They do <em>not</em> support the
 * {@code Entry.setValue} method. (Note however that it is possible to change mappings in the
 * associated map using {@code put}, {@code putIfAbsent}, or {@code replace}, depending on exactly
 * which effect you need.)
 * <p>
 * Beware that bulk operations {@code putAll}, {@code equals}, {@code toArray},
 * {@code containsValue}, and {@code clear} are <em>not</em> guaranteed to be performed atomically.
 * For example, an iterator operating concurrently with a {@code putAll} operation might view only
 * some of the added elements.
 * <p>
 * This class and its views and iterators implement all of the <em>optional</em> methods of the
 * {@link Map} and {@link Iterator} interfaces. Like most other concurrent collections, this class
 * does <em>not</em> permit the use of {@code null} keys or values because some null return values
 * cannot be reliably distinguished from the absence of elements.
 *
 * @param <V> the type of mapped values
 */
public class SkipListDoubleMap<V> extends AbstractMap<Double, V> implements ConcurrentNavigableDoubleMap<V> {

    /** The field updater. */
    private static final AtomicReferenceFieldUpdater<SkipListDoubleMap, Index> HEAD = AtomicReferenceFieldUpdater
            .newUpdater(SkipListDoubleMap.class, Index.class, "head");

    /** The field updater. */
    private static final AtomicReferenceFieldUpdater<SkipListDoubleMap, LongAdder> ADDER = AtomicReferenceFieldUpdater
            .newUpdater(SkipListDoubleMap.class, LongAdder.class, "adder");

    /** The field updater. */
    private static final AtomicReferenceFieldUpdater<Node, Node> NEXT = AtomicReferenceFieldUpdater
            .newUpdater(Node.class, Node.class, "next");

    /** The field updater. */
    private static final AtomicReferenceFieldUpdater<Node, Object> VALUE = AtomicReferenceFieldUpdater
            .newUpdater(Node.class, Object.class, "value");

    /** The field updater. */
    private static final AtomicReferenceFieldUpdater<Index, Index> RIGHT = AtomicReferenceFieldUpdater
            .newUpdater(Index.class, Index.class, "right");

    // Control values OR'ed as arguments to findNear
    private static final int EQ = 1;

    private static final int LT = 2;

    private static final int GT = 0; // Actually checked as !LT

    private static final double EMPTY = Double.MIN_VALUE;

    /** Lazily initialized topmost index of the skiplist. */
    private transient volatile Index<V> head;

    /** Lazily initialized element count */
    private transient volatile LongAdder adder;

    /** Lazily initialized key set */
    private transient volatile NavigableDoubleSet keySet;

    /** Lazily initialized values collection */
    private transient volatile Values<V> values;

    /** Lazily initialized entry set */
    private transient Entries<V> entrySet;

    /** Lazily initialized descending map */
    private transient SubMap<V> descendingMap;

    /**
     * The comparator used to maintain order in this map. (Non-private to simplify access in nested
     * classes.)
     */
    private final DoubleComparator comparator;

    /**
     * Returns the header for base node list, or null if uninitialized
     */
    private final Node<V> baseHead() {
        Index<V> h;
        VarHandle.acquireFence();
        return ((h = head) == null) ? null : h.node;
    }

    /**
     * Tries to unlink deleted node n from predecessor b (if both exist), by first splicing in a
     * marker if not already present. Upon return, node n is sure to be unlinked from b, possibly
     * via the actions of some other thread.
     *
     * @param b if nonnull, predecessor
     * @param n if nonnull, node known to be deleted
     */
    private static <V> void unlinkNode(Node<V> b, Node<V> n) {
        if (b != null && n != null) {
            Node<V> f, p;
            for (;;) {
                if ((f = n.next) != null && f.key == EMPTY) {
                    p = f.next; // already marked
                    break;
                } else if (NEXT.compareAndSet(n, f, new Node<V>(EMPTY, null, f))) {
                    p = f; // add marker
                    break;
                }
            }
            NEXT.compareAndSet(b, n, p);
        }
    }

    /**
     * Adds to element count, initializing adder if necessary
     *
     * @param c count to add
     */
    private void addCount(long c) {
        LongAdder a;
        do {
        } while ((a = adder) == null && !ADDER.compareAndSet(this, null, a = new LongAdder()));
        a.add(c);
    }

    /**
     * Returns element count, initializing adder if necessary.
     */
    private final long getAdderCount() {
        LongAdder a;
        long c;
        do {
        } while ((a = adder) == null && !ADDER.compareAndSet(this, null, a = new LongAdder()));
        return ((c = a.sum()) <= 0L) ? 0L : c; // ignore transient negatives
    }

    /**
     * Returns an index node with key strictly less than given key. Also unlinks indexes to deleted
     * nodes found adouble the way. Callers rely on this side-effect of clearing indices to
     * deleted nodes.
     *
     * @param key if nonnull the key
     * @return a predecessor node of key, or null if uninitialized or null key
     */
    private Node<V> findPredecessor(double key, DoubleComparator cmp) {
        Index<V> q;
        VarHandle.acquireFence();
        if ((q = head) == null || key == EMPTY)
            return null;
        else {
            for (Index<V> r, d;;) {
                while ((r = q.right) != null) {
                    Node<V> p;
                    double k;
                    if ((p = r.node) == null || (k = p.key) == EMPTY || p.value == null) // unlink
                                                                                         // index to
                                                                                         // deleted
                                                                                         // node
                        RIGHT.compareAndSet(q, r, r.right);
                    else if (cmp.compare(key, k) > 0)
                        q = r;
                    else
                        break;
                }
                if ((d = q.down) != null)
                    q = d;
                else
                    return q.node;
            }
        }
    }

    /**
     * Returns node holding key or null if no such, clearing out any deleted nodes seen adouble
     * the way. Repeatedly traverses at base-level looking for key starting at predecessor returned
     * from findPredecessor, processing base-level deletions as encountered. Restarts occur, at
     * traversal step encountering node n, if n's key field is null, indicating it is a marker, so
     * its predecessor is deleted before continuing, which we help do by re-finding a valid
     * predecessor. The traversal loops in doPut, doRemove, and findNear all include the same
     * checks.
     *
     * @param key the key
     * @return node holding key, or null if no such
     */
    private Node<V> findNode(double key) {
        DoubleComparator cmp = comparator;
        Node<V> b;
        outer: while ((b = findPredecessor(key, cmp)) != null) {
            for (;;) {
                Node<V> n;
                double k;
                int c;
                if ((n = b.next) == null) {
                    break outer; // empty
                } else if ((k = n.key) == EMPTY) {
                    break; // b is deleted
                } else if (n.value == null) {
                    unlinkNode(b, n); // n is deleted
                } else if ((c = cmp.compare(key, k)) > 0) {
                    b = n;
                } else if (c == 0) {
                    return n;
                } else {
                    break outer;
                }
            }
        }
        return null;
    }

    /**
     * Add indices after an insertion. Descends iteratively to the highest level of insertion, then
     * recursively, to chain index nodes to lower ones. Returns null on (staleness) failure,
     * disabling higher-level insertions. Recursion depths are exponentially less probable.
     *
     * @param q starting index for current level
     * @param skips levels to skip before inserting
     * @param x index for this insertion
     * @param cmp comparator
     */
    private static <V> boolean addIndices(Index<V> q, int skips, Index<V> x, DoubleComparator cmp) {
        Node<V> z;
        double key;
        if (x != null && (z = x.node) != null && (key = z.key) != EMPTY && q != null) { // hoist
                                                                                        // checks
            boolean retrying = false;
            for (;;) { // find splice point
                Index<V> r, d;
                int c;
                if ((r = q.right) != null) {
                    Node<V> p;
                    double k;
                    if ((p = r.node) == null || (k = p.key) == EMPTY || p.value == null) {
                        RIGHT.compareAndSet(q, r, r.right);
                        c = 0;
                    } else if ((c = cmp.compare(key, k)) > 0)
                        q = r;
                    else if (c == 0) break; // stale
                } else
                    c = -1;

                if (c < 0) {
                    if ((d = q.down) != null && skips > 0) {
                        --skips;
                        q = d;
                    } else if (d != null && !retrying && !addIndices(d, 0, x.down, cmp))
                        break;
                    else {
                        x.right = r;
                        if (RIGHT.compareAndSet(q, r, x))
                            return true;
                        else
                            retrying = true; // re-find splice point
                    }
                }
            }
        }
        return false;
    }

    /**
     * Constructs a new, empty map, sorted according to the specified comparator.
     *
     * @param comparator the comparator that will be used to order this map. If {@code null}, the
     *            {@linkplain Comparable natural ordering} of the keys will be used.
     */
    SkipListDoubleMap(DoubleComparator comparator) {
        this.comparator = comparator == null ? Double::compare : comparator;
    }

    /**
     * Possibly reduce head level if it has no nodes. This method can (rarely) make mistakes, in
     * which case levels can disappear even though they are about to contain index nodes. This
     * impacts performance, not correctness. To minimize mistakes as well as to reduce hysteresis,
     * the level is reduced by one only if the topmost three levels look empty. Also, if the removed
     * level looks non-empty after CAS, we try to change it back quick before anyone notices our
     * mistake! (This trick works pretty well because this method will practically never make
     * mistakes unless current thread stalls immediately before first CAS, in which case it is very
     * unlikely to stall again immediately afterwards, so will recover.) We put up with all this
     * rather than just let levels grow because otherwise, even a small map that has undergone a
     * large number of insertions and removals will have a lot of levels, slowing down access more
     * than would an occasional unwanted reduction.
     */
    private void tryReduceLevel() {
        Index<V> h, d, e;
        if ((h = head) != null && h.right == null && (d = h.down) != null && d.right == null && (e = d.down) != null && e.right == null && HEAD
                .compareAndSet(this, h, d) && h.right != null) // recheck
            HEAD.compareAndSet(this, d, h); // try to backout
    }

    /**
     * Gets first valid node, unlinking deleted nodes if encountered.
     * 
     * @return first node or null if empty
     */
    private Node<V> findFirst() {
        Node<V> b, n;
        if ((b = baseHead()) != null) {
            while ((n = b.next) != null) {
                if (n.value == null)
                    unlinkNode(b, n);
                else
                    return n;
            }
        }
        return null;
    }

    /**
     * Specialized version of find to get last valid node.
     * 
     * @return last node or null if empty
     */
    private Node<V> findLast() {
        outer: for (;;) {
            Index<V> q;
            Node<V> b;
            VarHandle.acquireFence();
            if ((q = head) == null) break;
            for (Index<V> r, d;;) {
                while ((r = q.right) != null) {
                    Node<V> p;
                    if ((p = r.node) == null || p.value == null)
                        RIGHT.compareAndSet(q, r, r.right);
                    else
                        q = r;
                }
                if ((d = q.down) != null)
                    q = d;
                else {
                    b = q.node;
                    break;
                }
            }
            if (b != null) {
                for (;;) {
                    Node<V> n;
                    if ((n = b.next) == null) {
                        if (b.key == EMPTY) // empty
                            break outer;
                        else
                            return b;
                    } else if (n.key == EMPTY)
                        break;
                    else if (n.value == null)
                        unlinkNode(b, n);
                    else
                        b = n;
                }
            }
        }
        return null;
    }

    /**
     * Utility for ceiling, floor, lower, higher methods.
     * 
     * @param key the key
     * @param rel the relation -- OR'ed combination of EQ, LT, GT
     * @return nearest node fitting relation, or null if no such
     */
    private Node<V> findNear(double key, int rel, DoubleComparator cmp) {
        Node<V> result;
        outer: for (Node<V> b;;) {
            if ((b = findPredecessor(key, cmp)) == null) {
                result = null;
                break; // empty
            }
            for (;;) {
                Node<V> n;
                double k;
                int c;
                if ((n = b.next) == null) {
                    result = ((rel & LT) != 0 && b.key != EMPTY) ? b : null;
                    break outer;
                } else if ((k = n.key) == EMPTY)
                    break;
                else if (n.value == null)
                    unlinkNode(b, n);
                else if (((c = cmp.compare(key, k)) == 0 && (rel & EQ) != 0) || (c < 0 && (rel & LT) == 0)) {
                    result = n;
                    break outer;
                } else if (c <= 0 && (rel & LT) != 0) {
                    result = (b.key != EMPTY) ? b : null;
                    break outer;
                } else
                    b = n;
            }
        }
        return result;
    }

    /**
     * Variant of findNear returning SimpleImmutableEntry
     * 
     * @param key the key
     * @param rel the relation -- OR'ed combination of EQ, LT, GT
     * @return Entry fitting relation, or null if no such
     */
    private DoubleEntry<V> findNearEntry(double key, int rel, DoubleComparator cmp) {
        for (;;) {
            Node<V> n;
            V v;
            if ((n = findNear(key, rel, cmp)) == null) return null;
            if ((v = n.value) != null) return DoubleEntry.immutable(n.key, v);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean containsKey(double key) {
        return doGet(key) != null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public V get(double key) {
        return doGet(key);
    }

    /**
     * Gets value for key. Same idea as findNode, except skips over deletions and markers, and
     * returns first encountered value to avoid possibly inconsistent rereads.
     *
     * @param key the key
     * @return the value, or null if absent
     */
    private V doGet(double key) {
        Index<V> q;
        VarHandle.acquireFence();
        DoubleComparator cmp = comparator;
        V result = null;
        if ((q = head) != null) {
            outer: for (Index<V> r, d;;) {
                while ((r = q.right) != null) {
                    Node<V> p;
                    double k;
                    V v;
                    int c;
                    if ((p = r.node) == null || (k = p.key) == EMPTY || (v = p.value) == null)
                        RIGHT.compareAndSet(q, r, r.right);
                    else if ((c = cmp.compare(key, k)) > 0)
                        q = r;
                    else if (c == 0) {
                        result = v;
                        break outer;
                    } else
                        break;
                }
                if ((d = q.down) != null)
                    q = d;
                else {
                    Node<V> b, n;
                    if ((b = q.node) != null) {
                        while ((n = b.next) != null) {
                            V v;
                            int c;
                            double k = n.key;
                            if ((v = n.value) == null || k == EMPTY || (c = cmp.compare(key, k)) > 0)
                                b = n;
                            else {
                                if (c == 0) result = v;
                                break;
                            }
                        }
                    }
                    break;
                }
            }
        }
        return result;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public V put(double key, V value) {
        if (value == null) {
            throw new NullPointerException();
        }
        return doPut(key, value, false);
    }

    /**
     * Main insertion method. Adds element if not present, or replaces value if present and
     * onlyIfAbsent is false.
     *
     * @param key the key
     * @param value the value that must be associated with key
     * @param onlyIfAbsent if should not insert if already present
     * @return the old value, or null if newly inserted
     */
    private V doPut(double key, V value, boolean onlyIfAbsent) {
        DoubleComparator cmp = comparator;
        for (;;) {
            Index<V> h;
            Node<V> b;
            VarHandle.acquireFence();
            int levels = 0; // number of levels descended
            if ((h = head) == null) { // try to initialize
                Node<V> base = new Node<V>(EMPTY, null, null);
                h = new Index<V>(base, null, null);
                b = (HEAD.compareAndSet(this, null, h)) ? base : null;
            } else {
                for (Index<V> q = h, r, d;;) { // count while descending
                    while ((r = q.right) != null) {
                        Node<V> p;
                        double k;
                        if ((p = r.node) == null || (k = p.key) == EMPTY || p.value == null)
                            RIGHT.compareAndSet(q, r, r.right);
                        else if (cmp.compare(key, k) > 0)
                            q = r;
                        else
                            break;
                    }
                    if ((d = q.down) != null) {
                        ++levels;
                        q = d;
                    } else {
                        b = q.node;
                        break;
                    }
                }
            }
            if (b != null) {
                Node<V> z = null; // new node, if inserted
                for (;;) { // find insertion point
                    Node<V> n, p;
                    double k;
                    V v;
                    int c;
                    if ((n = b.next) == null) {
                        if (b.key == EMPTY) // if empty, type check key now
                            cmp.compare(key, key);
                        c = -1;
                    } else if ((k = n.key) == EMPTY)
                        break; // can't append; restart
                    else if ((v = n.value) == null) {
                        unlinkNode(b, n);
                        c = 1;
                    } else if ((c = cmp.compare(key, k)) > 0)
                        b = n;
                    else if (c == 0 && (onlyIfAbsent || VALUE.compareAndSet(n, v, value))) return v;

                    if (c < 0 && NEXT.compareAndSet(b, n, p = new Node<V>(key, value, n))) {
                        z = p;
                        break;
                    }
                }

                if (z != null) {
                    int lr = ThreadLocalRandom.current().nextInt();
                    if ((lr & 0x3) == 0) { // add indices with 1/4 prob
                        int hr = ThreadLocalRandom.current().nextInt();
                        long rnd = ((long) hr << 32) | (lr & 0xffffffffL);
                        int skips = levels; // levels to descend before add
                        Index<V> x = null;
                        for (;;) { // create at most 62 indices
                            x = new Index<V>(z, x, null);
                            if (rnd >= 0L || --skips < 0)
                                break;
                            else
                                rnd <<= 1;
                        }
                        if (addIndices(h, skips, x, cmp) && skips < 0 && head == h) { // try to add
                                                                                      // new level
                            Index<V> hx = new Index<V>(z, x, null);
                            Index<V> nh = new Index<V>(h.node, h, hx);
                            HEAD.compareAndSet(this, h, nh);
                        }
                        if (z.value == null) // deleted while adding indices
                            findPredecessor(key, cmp); // clean
                    }
                    addCount(1L);
                    return null;
                }
            }
        }
    }

    /**
     * Removes the mapping for the specified key from this map if present.
     *
     * @param key key for which mapping should be removed
     * @return the previous value associated with the specified key, or {@code null} if there was no
     *         mapping for the key
     * @throws ClassCastException if the specified key cannot be compared with the keys currently in
     *             the map
     * @throws NullPointerException if the specified key is null
     */
    @Override
    public V remove(double key) {
        return doRemove(key, null);
    }

    /**
     * Main deletion method. Locates node, nulls value, appends a deletion marker, unlinks
     * predecessor, removes associated index nodes, and possibly reduces head index level.
     *
     * @param key the key
     * @param value if non-null, the value that must be associated with key
     * @return the node, or null if not found
     */
    private V doRemove(double key, Object value) {
        DoubleComparator cmp = comparator;
        V result = null;
        Node<V> b;
        outer: while ((b = findPredecessor(key, cmp)) != null && result == null) {
            for (;;) {
                Node<V> n;
                double k;
                V v;
                int c;
                if ((n = b.next) == null)
                    break outer;
                else if ((k = n.key) == EMPTY)
                    break;
                else if ((v = n.value) == null)
                    unlinkNode(b, n);
                else if ((c = cmp.compare(key, k)) > 0)
                    b = n;
                else if (c < 0)
                    break outer;
                else if (value != null && !value.equals(v))
                    break outer;
                else if (VALUE.compareAndSet(n, v, null)) {
                    result = v;
                    unlinkNode(b, n);
                    break; // loop to clean up
                }
            }
        }
        if (result != null) {
            tryReduceLevel();
            addCount(-1L);
        }
        return result;
    }

    /**
     * Returns {@code true} if this map maps one or more keys to the specified value. This operation
     * requires time linear in the map size. Additionally, it is possible for the map to change
     * during execution of this method, in which case the returned result may be inaccurate.
     *
     * @param value value whose presence in this map is to be tested
     * @return {@code true} if a mapping to {@code value} exists; {@code false} otherwise
     * @throws NullPointerException if the specified value is null
     */
    @Override
    public boolean containsValue(Object value) {
        if (value == null) throw new NullPointerException();
        Node<V> b, n;
        V v;
        if ((b = baseHead()) != null) {
            while ((n = b.next) != null) {
                if ((v = n.value) != null && value.equals(v))
                    return true;
                else
                    b = n;
            }
        }
        return false;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int size() {
        long c;
        return ((baseHead() == null) ? 0 : ((c = getAdderCount()) >= Integer.MAX_VALUE) ? Integer.MAX_VALUE : (int) c);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isEmpty() {
        return findFirst() == null;
    }

    /**
     * Removes all of the mappings from this map.
     */
    @Override
    public void clear() {
        Index<V> h, r, d;
        Node<V> b;
        VarHandle.acquireFence();
        while ((h = head) != null) {
            if ((r = h.right) != null) // remove indices
                RIGHT.compareAndSet(h, r, null);
            else if ((d = h.down) != null) // remove levels
                HEAD.compareAndSet(this, h, d);
            else {
                long count = 0L;
                if ((b = h.node) != null) { // remove nodes
                    Node<V> n;
                    V v;
                    while ((n = b.next) != null) {
                        if ((v = n.value) != null && VALUE.compareAndSet(n, v, null)) {
                            --count;
                            v = null;
                        }
                        if (v == null) unlinkNode(b, n);
                    }
                }
                if (count != 0L)
                    addCount(count);
                else
                    break;
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public V computeIfAbsent(double key, DoubleFunction<? extends V> mappingFunction) {
        if (key == EMPTY || mappingFunction == null) throw new NullPointerException();
        V v, p, r;
        if ((v = doGet(key)) == null && (r = mappingFunction.apply(key)) != null) v = (p = doPut(key, r, true)) == null ? r : p;
        return v;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public V merge(double key, V value, BiFunction<? super V, ? super V, ? extends V> remappingFunction) {
        Objects.requireNonNull(value);
        Objects.requireNonNull(remappingFunction);

        for (;;) {
            Node<V> n;
            V v;
            V r;
            if ((n = findNode(key)) == null) {
                if (doPut(key, value, true) == null) return value;
            } else if ((v = n.value) != null) {
                if ((r = remappingFunction.apply(v, value)) != null) {
                    if (VALUE.compareAndSet(n, v, r)) return r;
                } else if (doRemove(key, v) != null) return null;
            }
        }
    }

    /**
     * Returns a {@link NavigableSet} view of the keys contained in this map.
     * <p>
     * The set's iterator returns the keys in ascending order. The set's spliterator additionally
     * reports {@link Spliterator#CONCURRENT}, {@link Spliterator#NONNULL},
     * {@link Spliterator#SORTED} and {@link Spliterator#ORDERED}, with an encounter order that is
     * ascending key order.
     * <p>
     * The {@linkplain Spliterator#getComparator() spliterator's comparator} is {@code null} if the
     * {@linkplain #comparator() map's comparator} is {@code null}. Otherwise, the spliterator's
     * comparator is the same as or imposes the same total ordering as the map's comparator.
     * <p>
     * The set is backed by the map, so changes to the map are reflected in the set, and vice-versa.
     * The set supports element removal, which removes the corresponding mapping from the map, via
     * the {@code Iterator.remove}, {@code Set.remove}, {@code removeAll}, {@code retainAll}, and
     * {@code clear} operations. It does not support the {@code add} or {@code addAll} operations.
     * <p>
     * The view's iterators and spliterators are <a href="package-summary.html#Weakly"><i>weakly
     * consistent</i></a>.
     * <p>
     * This method is equivalent to method {@code navigableKeySet}.
     *
     * @return a navigable set view of the keys in this map
     */
    @Override
    public NavigableSet<Double> keySet() {
        NavigableDoubleSet ks;
        if ((ks = keySet) != null) return ks;
        return keySet = new Keys<>(this);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public NavigableDoubleSet navigableKeySet() {
        NavigableDoubleSet ks;
        if ((ks = keySet) != null) return ks;
        return keySet = new Keys<>(this);
    }

    /**
     * Returns a {@link Collection} view of the values contained in this map.
     * <p>
     * The collection's iterator returns the values in ascending order of the corresponding keys.
     * The collections's spliterator additionally reports {@link Spliterator#CONCURRENT},
     * {@link Spliterator#NONNULL} and {@link Spliterator#ORDERED}, with an encounter order that is
     * ascending order of the corresponding keys.
     * <p>
     * The collection is backed by the map, so changes to the map are reflected in the collection,
     * and vice-versa. The collection supports element removal, which removes the corresponding
     * mapping from the map, via the {@code Iterator.remove}, {@code Collection.remove},
     * {@code removeAll}, {@code retainAll} and {@code clear} operations. It does not support the
     * {@code add} or {@code addAll} operations.
     * <p>
     * The view's iterators and spliterators are <a href="package-summary.html#Weakly"><i>weakly
     * consistent</i></a>.
     */
    @Override
    public Collection<V> values() {
        Values<V> vs;
        if ((vs = values) != null) return vs;
        return values = new Values<>(this);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Set<Entry<Double, V>> entrySet() {
        return (Set) doubleEntrySet();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Set<DoubleEntry<V>> doubleEntrySet() {
        Entries<V> es;
        if ((es = entrySet) != null) return es;
        return entrySet = new Entries<V>(this);
    }

    @Override
    public ConcurrentNavigableDoubleMap<V> descendingMap() {
        ConcurrentNavigableDoubleMap<V> dm;
        if ((dm = descendingMap) != null) return dm;
        return descendingMap = new SubMap<V>(this, EMPTY, false, EMPTY, false, true);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public NavigableDoubleSet descendingKeySet() {
        return descendingMap().navigableKeySet();
    }

    /**
     * Compares the specified object with this map for equality. Returns {@code true} if the given
     * object is also a map and the two maps represent the same mappings. More formally, two maps
     * {@code m1} and {@code m2} represent the same mappings if
     * {@code m1.entrySet().equals(m2.entrySet())}. This operation may return misleading results if
     * either map is concurrently modified during execution of this method.
     *
     * @param o object to be compared for equality with this map
     * @return {@code true} if the specified object is equal to this map
     */
    @Override
    public boolean equals(Object o) {
        if (o == this) return true;
        if (!(o instanceof Map)) return false;
        Map<?, ?> m = (Map<?, ?>) o;
        try {
            DoubleComparator cmp = comparator;
            // See JDK-8223553 for Iterator type wildcard rationale
            Iterator<? extends Map.Entry<?, ?>> it = m.entrySet().iterator();
            if (m instanceof SortedMap && ((SortedMap<?, ?>) m).comparator() == cmp) {
                Node<V> b, n;
                if ((b = baseHead()) != null) {
                    while ((n = b.next) != null) {
                        double k;
                        V v;
                        if ((v = n.value) != null && (k = n.key) != EMPTY) {
                            if (!it.hasNext()) return false;
                            Map.Entry<?, ?> e = it.next();
                            double mk = (double) e.getKey();
                            Object mv = e.getValue();
                            if (mk == EMPTY || mv == null) return false;
                            try {
                                if (cmp.compare(k, mk) != 0) return false;
                            } catch (ClassCastException cce) {
                                return false;
                            }
                            if (!mv.equals(v)) return false;
                        }
                        b = n;
                    }
                }
                return !it.hasNext();
            } else {
                while (it.hasNext()) {
                    V v;
                    Map.Entry<?, ?> e = it.next();
                    Object mk = e.getKey();
                    Object mv = e.getValue();
                    if (mk == null || mv == null || (v = get(mk)) == null || !v.equals(mv)) return false;
                }
                Node<V> b, n;
                if ((b = baseHead()) != null) {
                    double k;
                    V v;
                    Object mv;
                    while ((n = b.next) != null) {
                        if ((v = n.value) != null && (k = n.key) != EMPTY && ((mv = m.get(k)) == null || !mv.equals(v))) return false;
                        b = n;
                    }
                }
                return true;
            }
        } catch (ClassCastException | NullPointerException unused) {
            return false;
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public V putIfAbsent(double key, V value) {
        return doPut(key, value, true);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean remove(double key, Object value) {
        return value != null && doRemove(key, value) != null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean replace(double key, V oldValue, V newValue) {
        if (key == EMPTY || oldValue == null || newValue == null) throw new NullPointerException();
        for (;;) {
            Node<V> n;
            V v;
            if ((n = findNode(key)) == null) return false;
            if ((v = n.value) != null) {
                if (!oldValue.equals(v)) return false;
                if (VALUE.compareAndSet(n, v, newValue)) return true;
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public V replace(double key, V value) {
        if (key == EMPTY || value == null) throw new NullPointerException();
        for (;;) {
            Node<V> n;
            V v;
            if ((n = findNode(key)) == null) return null;
            if ((v = n.value) != null && VALUE.compareAndSet(n, v, value)) return v;
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public DoubleComparator comparator() {
        return comparator;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public double firstDoubleKey() {
        Node<V> n = findFirst();
        if (n == null) {
            throw new NoSuchElementException();
        }
        return n.key;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public double lastDoubleKey() {
        Node<V> n = findLast();
        if (n == null) {
            throw new NoSuchElementException();
        }
        return n.key;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ConcurrentNavigableDoubleMap<V> subMap(double fromKey, boolean fromInclusive, double toKey, boolean toInclusive) {
        return new SubMap<V>(this, fromKey, fromInclusive, toKey, toInclusive, false);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ConcurrentNavigableDoubleMap<V> headMap(double toKey, boolean inclusive) {
        return new SubMap<V>(this, EMPTY, false, toKey, inclusive, false);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ConcurrentNavigableDoubleMap<V> tailMap(double fromKey, boolean inclusive) {
        return new SubMap<V>(this, fromKey, inclusive, EMPTY, false, false);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ConcurrentNavigableDoubleMap<V> subMap(double fromKey, double toKey) {
        return subMap(fromKey, true, toKey, false);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ConcurrentNavigableDoubleMap<V> headMap(double toKey) {
        return headMap(toKey, false);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ConcurrentNavigableDoubleMap<V> tailMap(double fromKey) {
        return tailMap(fromKey, true);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public DoubleEntry<V> lowerEntry(double key) {
        return findNearEntry(key, LT, comparator);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public double lowerKey(double key) {
        Node<V> node = findNear(key, LT, comparator);
        if (node == null) {
            throw new NoSuchElementException();
        }
        return node.key;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public DoubleEntry<V> floorEntry(double key) {
        return findNearEntry(key, LT | EQ, comparator);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public double floorKey(double key) {
        Node<V> node = findNear(key, LT | EQ, comparator);
        if (node == null) {
            throw new NoSuchElementException();
        }
        return node.key;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public DoubleEntry<V> ceilingEntry(double key) {
        return findNearEntry(key, GT | EQ, comparator);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public double ceilingKey(double key) {
        Node<V> node = findNear(key, GT | EQ, comparator);
        if (node == null) {
            throw new NoSuchElementException();
        }
        return node.key;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public DoubleEntry<V> higherEntry(double key) {
        return findNearEntry(key, GT, comparator);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public double higherKey(double key) {
        Node<V> node = findNear(key, GT, comparator);
        if (node == null) {
            throw new NoSuchElementException();
        }
        return node.key;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public DoubleEntry firstEntry() {
        Node<V> node = findFirst();
        return node == null || node.value == null ? null : DoubleEntry.immutable(node.key, node.value);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public DoubleEntry<V> lastEntry() {
        Node<V> node = findLast();
        return node == null || node.value == null ? null : DoubleEntry.immutable(node.key, node.value);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public DoubleEntry<V> pollFirstEntry() {
        Node<V> b, n;
        V v;
        if ((b = baseHead()) != null) {
            while ((n = b.next) != null) {
                if ((v = n.value) == null || VALUE.compareAndSet(n, v, null)) {
                    double k = n.key;
                    unlinkNode(b, n);
                    if (v != null) {
                        tryReduceLevel();
                        findPredecessor(k, comparator); // clean index
                        addCount(-1L);
                        return DoubleEntry.immutable(k, v);
                    }
                }
            }
        }
        return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public DoubleEntry<V> pollLastEntry() {
        outer: for (;;) {
            Index<V> q;
            Node<V> b;
            VarHandle.acquireFence();
            if ((q = head) == null) break;
            for (;;) {
                Index<V> d, r;
                Node<V> p;
                while ((r = q.right) != null) {
                    if ((p = r.node) == null || p.value == null)
                        RIGHT.compareAndSet(q, r, r.right);
                    else if (p.next != null)
                        q = r; // continue only if a successor
                    else
                        break;
                }
                if ((d = q.down) != null)
                    q = d;
                else {
                    b = q.node;
                    break;
                }
            }
            if (b != null) {
                for (;;) {
                    Node<V> n;
                    double k;
                    V v;
                    if ((n = b.next) == null) {
                        if (b.key == EMPTY) // empty
                            break outer;
                        else
                            break; // retry
                    } else if ((k = n.key) == EMPTY)
                        break;
                    else if ((v = n.value) == null)
                        unlinkNode(b, n);
                    else if (n.next != null)
                        b = n;
                    else if (VALUE.compareAndSet(n, v, null)) {
                        unlinkNode(b, n);
                        tryReduceLevel();
                        findPredecessor(k, comparator); // clean index
                        addCount(-1L);
                        return DoubleEntry.immutable(k, v);
                    }
                }
            }
        }
        return null;
    }

    // default Map method overrides

    @Override
    public void forEach(BiConsumer<? super Double, ? super V> action) {
        if (action == null) throw new NullPointerException();
        Node<V> b, n;
        V v;
        if ((b = baseHead()) != null) {
            while ((n = b.next) != null) {
                if ((v = n.value) != null) action.accept(n.key, v);
                b = n;
            }
        }
    }

    @Override
    public void replaceAll(BiFunction<? super Double, ? super V, ? extends V> function) {
        if (function == null) throw new NullPointerException();
        Node<V> b, n;
        V v;
        if ((b = baseHead()) != null) {
            while ((n = b.next) != null) {
                while ((v = n.value) != null) {
                    V r = function.apply(n.key, v);
                    if (r == null) throw new NullPointerException();
                    if (VALUE.compareAndSet(n, v, r)) break;
                }
                b = n;
            }
        }
    }

    /**
     * Helper method for EntrySet.removeIf.
     */
    private boolean removeEntryIf(Predicate<? super DoubleEntry<V>> function) {
        if (function == null) throw new NullPointerException();
        boolean removed = false;
        Node<V> b, n;
        V v;
        if ((b = baseHead()) != null) {
            while ((n = b.next) != null) {
                if ((v = n.value) != null) {
                    double k = n.key;
                    DoubleEntry<V> e = DoubleEntry.immutable(k, v);
                    if (function.test(e) && remove(k, v)) removed = true;
                }
                b = n;
            }
        }
        return removed;
    }

    /**
     * Helper method for Values.removeIf.
     */
    private boolean removeValueIf(Predicate<? super V> function) {
        if (function == null) throw new NullPointerException();
        boolean removed = false;
        Node<V> b, n;
        V v;
        if ((b = baseHead()) != null) {
            while ((n = b.next) != null) {
                if ((v = n.value) != null && function.test(v) && remove(n.key, v)) removed = true;
                b = n;
            }
        }
        return removed;
    }

    /**
     * Submaps returned by {@link SkipListDoubleMap} submap operations represent a subrange of
     * mappings of their underlying maps. Instances of this class support all methods of their
     * underlying maps, differing in that mappings outside their range are ignored, and attempts to
     * add mappings outside their ranges result in {@link IllegalArgumentException}. Instances of
     * this class are constructed only using the {@code subMap}, {@code headMap}, and
     * {@code tailMap} methods of their underlying maps.
     */
    private static class SubMap<V> extends AbstractMap<Double, V> implements ConcurrentNavigableDoubleMap<V> {

        /** Underlying map */
        private final SkipListDoubleMap<V> m;

        /** lower bound key, or null if from start */
        private final double lo;

        /** upper bound key, or null if to end */
        private final double hi;

        /** inclusion flag for lo */
        private final boolean loInclusive;

        /** inclusion flag for hi */
        private final boolean hiInclusive;

        /** direction */
        private final boolean isDescending;

        // Lazily initialized view holders
        private transient Keys<V> keySetView;

        private transient Values<V> valuesView;

        private transient Entries<V> entrySetView;

        /**
         * Creates a new submap, initializing all fields.
         */
        SubMap(SkipListDoubleMap<V> map, double fromKey, boolean fromInclusive, double toKey, boolean toInclusive, boolean isDescending) {
            DoubleComparator cmp = map.comparator;
            if (fromKey != EMPTY && toKey != EMPTY && cmp.compare(fromKey, toKey) > 0)
                throw new IllegalArgumentException("inconsistent range");
            this.m = map;
            this.lo = fromKey;
            this.hi = toKey;
            this.loInclusive = fromInclusive;
            this.hiInclusive = toInclusive;
            this.isDescending = isDescending;
        }

        boolean tooLow(double key, DoubleComparator cmp) {
            int c;
            return (lo != EMPTY && ((c = cmp.compare(key, lo)) < 0 || (c == 0 && !loInclusive)));
        }

        boolean tooHigh(double key, DoubleComparator cmp) {
            int c;
            return (hi != EMPTY && ((c = cmp.compare(key, hi)) > 0 || (c == 0 && !hiInclusive)));
        }

        boolean inBounds(double key, DoubleComparator cmp) {
            return !tooLow(key, cmp) && !tooHigh(key, cmp);
        }

        void checkKeyBounds(double key, DoubleComparator cmp) {
            if (!inBounds(key, cmp)) throw new IllegalArgumentException("key out of range");
        }

        /**
         * Returns true if node key is less than upper bound of range.
         */
        boolean isBeforeEnd(SkipListDoubleMap.Node<V> n, DoubleComparator cmp) {
            if (n == null) return false;
            if (hi == EMPTY) return true;
            double k = n.key;
            if (k == EMPTY) // pass by markers and headers
                return true;
            int c = cmp.compare(k, hi);
            return c < 0 || (c == 0 && hiInclusive);
        }

        /**
         * Returns lowest node. This node might not be in range, so most usages need to check
         * bounds.
         */
        SkipListDoubleMap.Node<V> loNode(DoubleComparator cmp) {
            if (lo == EMPTY)
                return m.findFirst();
            else if (loInclusive)
                return m.findNear(lo, GT | EQ, cmp);
            else
                return m.findNear(lo, GT, cmp);
        }

        /**
         * Returns highest node. This node might not be in range, so most usages need to check
         * bounds.
         */
        SkipListDoubleMap.Node<V> hiNode(DoubleComparator cmp) {
            if (hi == EMPTY)
                return m.findLast();
            else if (hiInclusive)
                return m.findNear(hi, LT | EQ, cmp);
            else
                return m.findNear(hi, LT, cmp);
        }

        /**
         * Returns lowest absolute key (ignoring directionality).
         */
        double lowestKey() {
            DoubleComparator cmp = m.comparator;
            SkipListDoubleMap.Node<V> n = loNode(cmp);
            if (isBeforeEnd(n, cmp))
                return n.key;
            else
                throw new NoSuchElementException();
        }

        /**
         * Returns highest absolute key (ignoring directionality).
         */
        double highestKey() {
            DoubleComparator cmp = m.comparator;
            SkipListDoubleMap.Node<V> n = hiNode(cmp);
            if (n != null) {
                double last = n.key;
                if (inBounds(last, cmp)) return last;
            }
            throw new NoSuchElementException();
        }

        DoubleEntry<V> lowestEntry() {
            DoubleComparator cmp = m.comparator;
            for (;;) {
                SkipListDoubleMap.Node<V> n;
                V v;
                if ((n = loNode(cmp)) == null || !isBeforeEnd(n, cmp))
                    return null;
                else if ((v = n.value) != null) return DoubleEntry.immutable(n.key, v);
            }
        }

        DoubleEntry<V> highestEntry() {
            DoubleComparator cmp = m.comparator;
            for (;;) {
                SkipListDoubleMap.Node<V> n;
                V v;
                if ((n = hiNode(cmp)) == null || !inBounds(n.key, cmp))
                    return null;
                else if ((v = n.value) != null) return DoubleEntry.immutable(n.key, v);
            }
        }

        DoubleEntry<V> removeLowest() {
            DoubleComparator cmp = m.comparator;
            for (;;) {
                SkipListDoubleMap.Node<V> n;
                double k;
                V v;
                if ((n = loNode(cmp)) == null)
                    return null;
                else if (!inBounds((k = n.key), cmp))
                    return null;
                else if ((v = m.doRemove(k, null)) != null) return DoubleEntry.immutable(k, v);
            }
        }

        DoubleEntry<V> removeHighest() {
            DoubleComparator cmp = m.comparator;
            for (;;) {
                SkipListDoubleMap.Node<V> n;
                double k;
                V v;
                if ((n = hiNode(cmp)) == null)
                    return null;
                else if (!inBounds((k = n.key), cmp))
                    return null;
                else if ((v = m.doRemove(k, null)) != null) return DoubleEntry.immutable(k, v);
            }
        }

        /**
         * Submap version of ConcurrentSkipListDoubleMap.findNearEntry.
         */
        DoubleEntry<V> getNearEntry(double key, int rel) {
            DoubleComparator cmp = m.comparator;
            if (isDescending) { // adjust relation for direction
                if ((rel & LT) == 0)
                    rel |= LT;
                else
                    rel &= ~LT;
            }
            if (tooLow(key, cmp)) return ((rel & LT) != 0) ? null : lowestEntry();
            if (tooHigh(key, cmp)) return ((rel & LT) != 0) ? highestEntry() : null;
            DoubleEntry<V> e = m.findNearEntry(key, rel, cmp);
            if (e == null || !inBounds(e.getDoubleKey(), cmp))
                return null;
            else
                return e;
        }

        // Almost the same as getNearEntry, except for keys
        double getNearKey(double key, int rel) {
            DoubleComparator cmp = m.comparator;
            if (isDescending) { // adjust relation for direction
                if ((rel & LT) == 0)
                    rel |= LT;
                else
                    rel &= ~LT;
            }
            if (tooLow(key, cmp)) {
                if ((rel & LT) == 0) {
                    SkipListDoubleMap.Node<V> n = loNode(cmp);
                    if (isBeforeEnd(n, cmp)) return n.key;
                }
                return EMPTY;
            }
            if (tooHigh(key, cmp)) {
                if ((rel & LT) != 0) {
                    SkipListDoubleMap.Node<V> n = hiNode(cmp);
                    if (n != null) {
                        double last = n.key;
                        if (inBounds(last, cmp)) return last;
                    }
                }
                return EMPTY;
            }
            for (;;) {
                Node<V> n = m.findNear(key, rel, cmp);
                if (n == null || !inBounds(n.key, cmp)) return EMPTY;
                if (n.value != null) return n.key;
            }
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public boolean containsKey(double key) {
            return inBounds(key, m.comparator) && m.containsKey(key);
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public V get(double key) {
            return (!inBounds(key, m.comparator)) ? null : m.get(key);
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public V put(double key, V value) {
            checkKeyBounds(key, m.comparator);
            return m.put(key, value);
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public V remove(double key) {
            return (!inBounds(key, m.comparator)) ? null : m.remove(key);
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public int size() {
            DoubleComparator cmp = m.comparator;
            long count = 0;
            for (SkipListDoubleMap.Node<V> n = loNode(cmp); isBeforeEnd(n, cmp); n = n.next) {
                if (n.value != null) ++count;
            }
            return count >= Integer.MAX_VALUE ? Integer.MAX_VALUE : (int) count;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public boolean isEmpty() {
            DoubleComparator cmp = m.comparator;
            return !isBeforeEnd(loNode(cmp), cmp);
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public boolean containsValue(Object value) {
            if (value == null) throw new NullPointerException();
            DoubleComparator cmp = m.comparator;
            for (SkipListDoubleMap.Node<V> n = loNode(cmp); isBeforeEnd(n, cmp); n = n.next) {
                V v = n.value;
                if (v != null && value.equals(v)) return true;
            }
            return false;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public void clear() {
            DoubleComparator cmp = m.comparator;
            for (SkipListDoubleMap.Node<V> n = loNode(cmp); isBeforeEnd(n, cmp); n = n.next) {
                if (n.value != null) m.remove(n.key);
            }
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public V putIfAbsent(double key, V value) {
            checkKeyBounds(key, m.comparator);
            return m.putIfAbsent(key, value);
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public boolean remove(double key, Object value) {
            return inBounds(key, m.comparator) && m.remove(key, value);
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public boolean replace(double key, V oldValue, V newValue) {
            checkKeyBounds(key, m.comparator);
            return m.replace(key, oldValue, newValue);
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public V replace(double key, V value) {
            checkKeyBounds(key, m.comparator);
            return m.replace(key, value);
        }

        @Override
        public Comparator<Double> comparator() {
            DoubleComparator cmp = m.comparator();
            if (isDescending) {
                return Collections.reverseOrder(cmp);
            } else {
                return cmp;
            }
        }

        /**
         * Utility to create submaps, where given bounds override unbounded(null) ones and/or are
         * checked against bounded ones.
         */
        SubMap<V> newSubMap(double fromKey, boolean fromInclusive, double toKey, boolean toInclusive) {
            DoubleComparator cmp = m.comparator;
            if (isDescending) { // flip senses
                double tk = fromKey;
                fromKey = toKey;
                toKey = tk;
                boolean ti = fromInclusive;
                fromInclusive = toInclusive;
                toInclusive = ti;
            }
            if (lo != EMPTY) {
                if (fromKey == EMPTY) {
                    fromKey = lo;
                    fromInclusive = loInclusive;
                } else {
                    int c = cmp.compare(fromKey, lo);
                    if (c < 0 || (c == 0 && !loInclusive && fromInclusive)) throw new IllegalArgumentException("key out of range");
                }
            }
            if (hi != EMPTY) {
                if (toKey == EMPTY) {
                    toKey = hi;
                    toInclusive = hiInclusive;
                } else {
                    int c = cmp.compare(toKey, hi);
                    if (c > 0 || (c == 0 && !hiInclusive && toInclusive)) throw new IllegalArgumentException("key out of range");
                }
            }
            return new SubMap<V>(m, fromKey, fromInclusive, toKey, toInclusive, isDescending);
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public ConcurrentNavigableDoubleMap<V> subMap(double fromKey, boolean fromInclusive, double toKey, boolean toInclusive) {
            return newSubMap(fromKey, fromInclusive, toKey, toInclusive);
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public ConcurrentNavigableDoubleMap<V> headMap(double toKey, boolean inclusive) {
            return newSubMap(EMPTY, false, toKey, inclusive);
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public ConcurrentNavigableDoubleMap<V> tailMap(double fromKey, boolean inclusive) {
            return newSubMap(fromKey, inclusive, EMPTY, false);
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public ConcurrentNavigableDoubleMap<V> subMap(double fromKey, double toKey) {
            return subMap(fromKey, true, toKey, false);
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public ConcurrentNavigableDoubleMap<V> headMap(double toKey) {
            return headMap(toKey, false);
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public ConcurrentNavigableDoubleMap<V> tailMap(double fromKey) {
            return tailMap(fromKey, true);
        }

        @Override
        public SubMap<V> descendingMap() {
            return new SubMap<V>(m, lo, loInclusive, hi, hiInclusive, !isDescending);
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public DoubleEntry<V> lowerEntry(double key) {
            return getNearEntry(key, LT);
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public double lowerKey(double key) {
            return getNearKey(key, LT);
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public DoubleEntry<V> floorEntry(double key) {
            return getNearEntry(key, LT | EQ);
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public double floorKey(double key) {
            return getNearKey(key, LT | EQ);
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public DoubleEntry<V> ceilingEntry(double key) {
            return getNearEntry(key, GT | EQ);
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public double ceilingKey(double key) {
            return getNearKey(key, GT | EQ);
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public DoubleEntry<V> higherEntry(double key) {
            return getNearEntry(key, GT);
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public double higherKey(double key) {
            return getNearKey(key, GT);
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public Entry<Double, V> lowerEntry(Double key) {
            return null;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public Double lowerKey(Double key) {
            return null;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public Entry<Double, V> floorEntry(Double key) {
            return null;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public Double floorKey(Double key) {
            return null;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public Entry<Double, V> ceilingEntry(Double key) {
            return null;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public Double ceilingKey(Double key) {
            return null;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public Entry<Double, V> higherEntry(Double key) {
            return null;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public Double higherKey(Double key) {
            return null;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public double firstDoubleKey() {
            return isDescending ? highestKey() : lowestKey();
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public DoubleEntry<V> firstEntry() {
            return isDescending ? highestEntry() : lowestEntry();
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public double lastDoubleKey() {
            return isDescending ? lowestKey() : highestKey();
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public DoubleEntry<V> lastEntry() {
            return isDescending ? lowestEntry() : highestEntry();
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public DoubleEntry<V> pollFirstEntry() {
            return isDescending ? removeHighest() : removeLowest();
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public DoubleEntry<V> pollLastEntry() {
            return isDescending ? removeLowest() : removeHighest();
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public NavigableDoubleSet keySet() {
            NavigableDoubleSet ks;
            if ((ks = keySetView) != null) return ks;
            return keySetView = new Keys<>(this);
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public NavigableDoubleSet navigableKeySet() {
            NavigableDoubleSet ks;
            if ((ks = keySetView) != null) return ks;
            return keySetView = new Keys<>(this);
        }

        @Override
        public Collection<V> values() {
            Values<V> vs;
            if ((vs = valuesView) != null) return vs;
            return valuesView = new Values<>(this);
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public Set<Entry<Double, V>> entrySet() {
            return (Set) doubleEntrySet();
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public Set<DoubleEntry<V>> doubleEntrySet() {
            if (entrySetView == null) {
                entrySetView = new Entries(this);
            }
            return entrySetView;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public NavigableDoubleSet descendingKeySet() {
            return descendingMap().navigableKeySet();
        }

        /**
         * Variant of main Iter class to traverse through submaps. Also serves as back-up
         * Spliterator for views.
         */
        private class SubMapGenericIterator<T> implements Iterator<T>, Spliterator<T> {

            /** The node access type. */
            private final Type type;

            /** the last node returned by next() */
            private Node<V> lastReturned;

            /** the next node to return from next(); */
            private Node<V> next;

            /** Cache of next value field to maintain weak consistency */
            private V nextValue;

            /**
             * @param type
             */
            private SubMapGenericIterator(Type type) {
                this.type = type;

                VarHandle.acquireFence();
                DoubleComparator cmp = m.comparator;
                for (;;) {
                    next = isDescending ? hiNode(cmp) : loNode(cmp);
                    if (next == null) break;
                    V x = next.value;
                    if (x != null) {
                        if (!inBounds(next.key, cmp))
                            next = null;
                        else
                            nextValue = x;
                        break;
                    }
                }
            }

            /**
             * {@inheritDoc}
             */
            @Override
            public final int characteristics() {
                return type.characteristics;
            }

            /**
             * {@inheritDoc}
             */
            @Override
            public final T next() {
                Node<V> node = next;
                V value = nextValue;
                advance();
                return (T) type.create(node.key, value);
            }

            /**
             * {@inheritDoc}
             */
            @Override
            public final boolean hasNext() {
                return next != null;
            }

            private void advance() {
                if (next == null) throw new NoSuchElementException();
                lastReturned = next;
                if (isDescending)
                    descend();
                else
                    ascend();
            }

            private void ascend() {
                DoubleComparator cmp = m.comparator;
                for (;;) {
                    next = next.next;
                    if (next == null) break;
                    V x = next.value;
                    if (x != null) {
                        if (tooHigh(next.key, cmp))
                            next = null;
                        else
                            nextValue = x;
                        break;
                    }
                }
            }

            private void descend() {
                DoubleComparator cmp = m.comparator;
                for (;;) {
                    next = m.findNear(lastReturned.key, LT, cmp);
                    if (next == null) break;
                    V x = next.value;
                    if (x != null) {
                        if (tooLow(next.key, cmp))
                            next = null;
                        else
                            nextValue = x;
                        break;
                    }
                }
            }

            /**
             * {@inheritDoc}
             */
            @Override
            public void remove() {
                Node<V> l = lastReturned;
                if (l == null) throw new IllegalStateException();
                m.remove(l.key);
                lastReturned = null;
            }

            /**
             * {@inheritDoc}
             */
            @Override
            public Spliterator<T> trySplit() {
                return null;
            }

            /**
             * {@inheritDoc}
             */
            @Override
            public boolean tryAdvance(Consumer<? super T> action) {
                if (hasNext()) {
                    action.accept(next());
                    return true;
                }
                return false;
            }

            /**
             * {@inheritDoc}
             */
            @Override
            public void forEachRemaining(Consumer<? super T> action) {
                while (hasNext())
                    action.accept(next());
            }

            /**
             * {@inheritDoc}
             */
            @Override
            public long estimateSize() {
                return Long.MAX_VALUE;
            }

            /**
             * {@inheritDoc}
             */
            @Override
            public final Comparator<? super T> getComparator() {
                if (type == Type.Key) {
                    return (Comparator<? super T>) SubMap.this.comparator();
                } else {
                    return null;
                }
            }
        }
    }

    /**
     * 
     */
    private static final class Keys<V> extends AbstractSet<Double> implements NavigableDoubleSet {

        /** The original map. */
        private final ConcurrentNavigableDoubleMap<V> m;

        /**
         * Build key-set view.
         * 
         * @param map
         */
        private Keys(ConcurrentNavigableDoubleMap<V> map) {
            m = map;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public int size() {
            return m.size();
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public boolean isEmpty() {
            return m.isEmpty();
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public boolean add(double e) {
            throw new UnsupportedOperationException();
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public boolean contains(double o) {
            return m.containsKey(o);
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public boolean remove(double o) {
            return m.remove(o) != null;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public void clear() {
            m.clear();
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public double lower(double e) {
            return m.lowerKey(e);
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public double floor(double e) {
            return m.floorKey(e);
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public double ceiling(double e) {
            return m.ceilingKey(e);
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public double higher(double e) {
            return m.higherKey(e);
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public Comparator<? super Double> comparator() {
            return m.comparator();
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public double firstDouble() {
            return m.firstDoubleKey();
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public double lastDouble() {
            return m.lastDoubleKey();
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public double pollFirstDouble() {
            DoubleEntry<V> entry = m.pollFirstEntry();
            if (entry == null) {
                throw new NoSuchElementException();
            }
            return entry.getDoubleKey();
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public double pollLastDouble() {
            DoubleEntry<V> entry = m.pollLastEntry();
            if (entry == null) {
                throw new NoSuchElementException();
            }
            return entry.getDoubleKey();
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public boolean equals(Object o) {
            if (o == this) return true;
            if (!(o instanceof Set)) return false;
            Collection<?> c = (Collection<?>) o;
            try {
                return containsAll(c) && c.containsAll(this);
            } catch (ClassCastException | NullPointerException unused) {
                return false;
            }
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public Iterator<Double> descendingIterator() {
            return descendingSet().iterator();
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public NavigableDoubleSet subSet(double fromElement, boolean fromInclusive, double toElement, boolean toInclusive) {
            return new Keys(m.subMap(fromElement, fromInclusive, toElement, toInclusive));
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public NavigableDoubleSet headSet(double toElement, boolean inclusive) {
            return new Keys<>(m.headMap(toElement, inclusive));
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public NavigableDoubleSet tailSet(double fromElement, boolean inclusive) {
            return new Keys<>(m.tailMap(fromElement, inclusive));
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public NavigableDoubleSet subSet(double fromElement, double toElement) {
            return subSet(fromElement, true, toElement, false);
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public NavigableDoubleSet headSet(double toElement) {
            return headSet(toElement, false);
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public NavigableDoubleSet tailSet(double fromElement) {
            return tailSet(fromElement, true);
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public NavigableDoubleSet descendingSet() {
            return new Keys<>(m.descendingMap());
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public Iterator<Double> iterator() {
            if (m instanceof SkipListDoubleMap) {
                return ((SkipListDoubleMap) m).createIteratorFor(Type.Key);
            } else {
                return ((SubMap) m).new SubMapGenericIterator(Type.Key);
            }
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public Spliterator<Double> spliterator() {
            if (m instanceof SkipListDoubleMap) {
                return ((SkipListDoubleMap) m).createSpliteratorFor(Type.Key);
            } else {
                return ((SubMap) m).new SubMapGenericIterator(Type.Key);
            }
        }
    }

    /**
     * 
     */
    private static class Values<V> extends AbstractCollection<V> {

        /** The original view. */
        private final ConcurrentNavigableDoubleMap<V> m;

        /**
         * Build view.
         * 
         * @param map
         */
        private Values(ConcurrentNavigableDoubleMap<V> map) {
            m = map;
        }

        @Override
        public int size() {
            return m.size();
        }

        @Override
        public boolean isEmpty() {
            return m.isEmpty();
        }

        @Override
        public boolean contains(Object o) {
            return m.containsValue(o);
        }

        @Override
        public void clear() {
            m.clear();
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public Object[] toArray() {
            return I.signal(this).toList().toArray();
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public <T> T[] toArray(T[] a) {
            return I.signal(this).toList().toArray(a);
        }

        @Override
        public Iterator<V> iterator() {
            if (m instanceof SkipListDoubleMap) {
                return ((SkipListDoubleMap) m).createIteratorFor(Type.Value);
            } else {
                return ((SubMap) m).new SubMapGenericIterator(Type.Value);
            }
        }

        @Override
        public Spliterator<V> spliterator() {
            return (m instanceof SkipListDoubleMap) ? ((SkipListDoubleMap<V>) m).createSpliteratorFor(Type.Value)
                    : ((SubMap<V>) m).new SubMapGenericIterator(Type.Value);
        }

        @Override
        public boolean removeIf(Predicate<? super V> filter) {
            if (filter == null) throw new NullPointerException();
            if (m instanceof SkipListDoubleMap) return ((SkipListDoubleMap<V>) m).removeValueIf(filter);
            // else use iterator
            Iterator<DoubleEntry<V>> it = ((SubMap<V>) m).new SubMapGenericIterator(Type.Entry);
            boolean removed = false;
            while (it.hasNext()) {
                DoubleEntry<V> e = it.next();
                V v = e.getValue();
                if (filter.test(v) && m.remove(e.getDoubleKey(), v)) removed = true;
            }
            return removed;
        }
    }

    /**
     * 
     */
    private static class Entries<V> extends AbstractSet<DoubleEntry<V>> {

        /** The original view. */
        private final ConcurrentNavigableDoubleMap<V> m;

        /**
         * Build view.
         * 
         * @param map
         */
        private Entries(ConcurrentNavigableDoubleMap<V> map) {
            m = map;
        }

        @Override
        public Iterator<DoubleEntry<V>> iterator() {
            if (m instanceof SkipListDoubleMap) {
                return ((SkipListDoubleMap) m).createIteratorFor(Type.Entry);
            } else {
                return ((SubMap) m).new SubMapGenericIterator(Type.Entry);
            }
        }

        @Override
        public boolean contains(Object o) {
            if (!(o instanceof Map.Entry)) return false;
            Map.Entry<?, ?> e = (Map.Entry<?, ?>) o;
            V v = m.get(e.getKey());
            return v != null && v.equals(e.getValue());
        }

        @Override
        public boolean remove(Object o) {
            if (!(o instanceof Map.Entry)) return false;
            Map.Entry<?, ?> e = (Map.Entry<?, ?>) o;
            return m.remove(e.getKey(), e.getValue());
        }

        @Override
        public boolean isEmpty() {
            return m.isEmpty();
        }

        @Override
        public int size() {
            return m.size();
        }

        @Override
        public void clear() {
            m.clear();
        }

        @Override
        public boolean equals(Object o) {
            if (o == this) return true;
            if (!(o instanceof Set)) return false;
            Collection<?> c = (Collection<?>) o;
            try {
                return containsAll(c) && c.containsAll(this);
            } catch (ClassCastException | NullPointerException unused) {
                return false;
            }
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public Object[] toArray() {
            return I.signal(this).toList().toArray();
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public <T> T[] toArray(T[] a) {
            return I.signal(this).toList().toArray(a);
        }

        @Override
        public Spliterator<DoubleEntry<V>> spliterator() {
            return (m instanceof SkipListDoubleMap) ? ((SkipListDoubleMap<V>) m).createSpliteratorFor(Type.Entry)
                    : ((SubMap<V>) m).new SubMapGenericIterator(Type.Entry);
        }

        @Override
        public boolean removeIf(Predicate<? super DoubleEntry<V>> filter) {
            if (filter == null) throw new NullPointerException();
            if (m instanceof SkipListDoubleMap) return ((SkipListDoubleMap<V>) m).removeEntryIf(filter);
            // else use iterator
            Iterator<DoubleEntry<V>> it = ((SubMap<V>) m).new SubMapGenericIterator(Type.Entry);
            boolean removed = false;
            while (it.hasNext()) {
                DoubleEntry<V> e = it.next();
                if (filter.test(e) && m.remove(e.getDoubleKey(), e.getValue())) removed = true;
            }
            return removed;
        }
    }

    // default Map method overrides

    /**
     * Nodes hold keys and values, and are singly linked in sorted order, possibly with some
     * intervening marker nodes. The list is headed by a header node accessible as head.node.
     * Headers and marker nodes have null keys. The val field (but currently not the key field) is
     * nulled out upon deletion.
     */
    private static final class Node<V> {

        /** The entry key. */
        private final double key;

        /** The entry value. */
        private volatile V value;

        /** The next entry. */
        private volatile Node<V> next;

        private Node(double key, V value, Node<V> next) {
            this.key = key;
            this.value = value;
            this.next = next;
        }
    }

    /**
     * Index nodes represent the levels of the skip list.
     */
    private static final class Index<V> {

        private final Node<V> node;

        private final Index<V> down;

        private volatile Index<V> right;

        private Index(Node<V> node, Index<V> down, Index<V> right) {
            this.node = node;
            this.down = down;
            this.right = right;
        }
    }

    /**
     * Node access type.
     */
    private static enum Type {

        Key(Spliterator.DISTINCT | Spliterator.SORTED | Spliterator.ORDERED | Spliterator.CONCURRENT | Spliterator.NONNULL) {

            /**
             * {@inheritDoc}
             */
            @Override
            public Object create(double key, Object value) {
                return key;
            }

            /**
             * {@inheritDoc}
             */
            @Override
            public Comparator create(DoubleComparator comparator) {
                return comparator;
            }
        },

        Value(Spliterator.ORDERED | Spliterator.CONCURRENT | Spliterator.NONNULL) {

            /**
             * {@inheritDoc}
             */
            @Override
            public Object create(double key, Object value) {
                return value;
            }

            /**
             * {@inheritDoc}
             */
            @Override
            public Comparator create(DoubleComparator comparator) {
                return null;
            }
        },

        Entry(Spliterator.DISTINCT | Spliterator.SORTED | Spliterator.ORDERED | Spliterator.CONCURRENT | Spliterator.NONNULL) {

            /**
             * {@inheritDoc}
             */
            @Override
            public Object create(double key, Object value) {
                return DoubleEntry.immutable(key, value);
            }

            /**
             * {@inheritDoc}
             */
            @Override
            public Comparator<DoubleEntry> create(DoubleComparator comparator) {
                return (one, other) -> comparator.compare(one.getDoubleKey(), other.getDoubleKey());
            }
        };

        /** The characteristics of {@link Spliterator}. */
        private final int characteristics;

        /**
         * Build.
         * 
         * @param characteristics
         */
        private Type(int characteristics) {
            this.characteristics = characteristics;
        }

        /**
         * Create value from {@link Node}.
         * 
         * @param key
         * @param value
         * @return
         */
        public abstract Object create(double key, Object value);

        /**
         * Create specila {@link Comparator} for {@link Spliterator}.
         * 
         * @param comprator
         * @return
         */
        public abstract Comparator create(DoubleComparator comparator);
    }

    private Iterator createIteratorFor(Type type) {
        return new GenericIterator(type);
    }

    private Spliterator createSpliteratorFor(Type type) {
        return Spliterators.spliteratorUnknownSize(createIteratorFor(type), type.characteristics);
    }

    /**
     * Generic iterator.
     */
    private class GenericIterator<R> implements Iterator<R> {

        /** The node access type. */
        private final Type type;

        /** the last node returned by next() */
        private Node<V> lastReturned;

        /** the next node to return from next(); */
        private Node<V> next;

        /** Cache of next value field to maintain weak consistency */
        private V nextValue;

        /** Initializes ascending iterator for entire range. */
        private GenericIterator(Type type) {
            this.type = type;
            advance(baseHead());
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public final boolean hasNext() {
            return next != null;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public R next() {
            Node<V> node;
            if ((node = next) == null) {
                throw new NoSuchElementException();
            }
            double key = node.key;
            V value = nextValue;
            advance(node);
            return (R) type.create(key, value);
        }

        /**
         * Advances next to higher entry.
         * 
         * @param base
         */
        private void advance(Node<V> base) {
            Node<V> node = null;
            V value = null;
            if ((lastReturned = base) != null) {
                while ((node = base.next) != null && (value = node.value) == null) {
                    base = node;
                }
            }
            nextValue = value;
            next = node;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public final void remove() {
            Node<V> node;
            double key;
            if ((node = lastReturned) == null || (key = node.key) == EMPTY) {
                throw new IllegalStateException();
            }
            // It would not be worth all of the overhead to directly
            // unlink from here. Using remove is fast enough.
            SkipListDoubleMap.this.remove(key);
            lastReturned = null;
        }
    }
}