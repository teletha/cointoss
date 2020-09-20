/*
 * Copyright (C) 2019 Nameless Production Committee
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          http://opensource.org/licenses/mit-license.php
 */
package cointoss.util.primitive.map;

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
import java.util.function.LongFunction;
import java.util.function.Predicate;

import cointoss.util.primitive.set.NavigableLongSet;
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
 * <p>
 * This class is a member of the
 * <a href="{@docRoot}/java.base/java/util/package-summary.html#CollectionsFramework"> Java
 * Collections Framework</a>.
 *
 * @author Doug Lea
 * @param <Long> the type of keys maintained by this map
 * @param <V> the type of mapped values
 */
@SuppressWarnings("serial")
class SkipListLongMap<V> extends AbstractMap<Long, V> implements ConcurrentNavigableLongMap<V> {

    /** The field updater. */
    private static final AtomicReferenceFieldUpdater<SkipListLongMap, Index> HEAD = AtomicReferenceFieldUpdater
            .newUpdater(SkipListLongMap.class, Index.class, "head");

    /** The field updater. */
    private static final AtomicReferenceFieldUpdater<SkipListLongMap, LongAdder> ADDER = AtomicReferenceFieldUpdater
            .newUpdater(SkipListLongMap.class, LongAdder.class, "adder");

    /** The field updater. */
    private static final AtomicReferenceFieldUpdater<Node, Node> NEXT = AtomicReferenceFieldUpdater
            .newUpdater(Node.class, Node.class, "next");

    /** The field updater. */
    private static final AtomicReferenceFieldUpdater<Node, Object> VALUE = AtomicReferenceFieldUpdater
            .newUpdater(Node.class, Object.class, "value");

    /** The field updater. */
    private static final AtomicReferenceFieldUpdater<Index, Index> RIGHT = AtomicReferenceFieldUpdater
            .newUpdater(Index.class, Index.class, "right");

    /* ---------------- Deletion -------------- */

    /* ---------------- Finding and removing first element -------------- */

    /* ---------------- Finding and removing last element -------------- */

    /* ---------------- Relational operations -------------- */

    // Control values OR'ed as arguments to findNear

    private static final int EQ = 1;

    private static final int LT = 2;

    private static final int GT = 0; // Actually checked as !LT

    /*
     * This class implements a tree-like two-dimensionally linked skip list in which the index
     * levels are represented in separate nodes from the base nodes holding data. There are two
     * reasons for taking this approach instead of the usual array-based structure: 1) Array based
     * implementations seem to encounter more complexity and overhead 2) We can use cheaper
     * algorithms for the heavily-traversed index lists than can be used for the base lists. Here's
     * a picture of some of the basics for a possible list with 2 levels of index: Head nodes Index
     * nodes +-+ right +-+ +-+ |2|---------------->| |--------------------->| |->null +-+ +-+ +-+ |
     * down | | v v v +-+ +-+ +-+ +-+ +-+ +-+ |1|----------->| |->| |------>| |----------->|
     * |------>| |->null +-+ +-+ +-+ +-+ +-+ +-+ v | | | | | Nodes next v v v v v +-+ +-+ +-+ +-+
     * +-+ +-+ +-+ +-+ +-+ +-+ +-+ +-+ |
     * |->|A|->|B|->|C|->|D|->|E|->|F|->|G|->|H|->|I|->|J|->|K|->null +-+ +-+ +-+ +-+ +-+ +-+ +-+
     * +-+ +-+ +-+ +-+ +-+ The base lists use a variant of the HM linked ordered set algorithm. See
     * Tim Harris, "A pragmatic implementation of non-blocking linked lists"
     * http://www.cl.cam.ac.uk/~tlh20/publications.html and Maged Michael "High Performance Dynamic
     * Lock-Free Hash Tables and List-Based Sets"
     * http://www.research.ibm.com/people/m/michael/pubs.htm. The basic idea in these lists is to
     * mark the "next" pointers of deleted nodes when deleting to avoid conflicts with concurrent
     * insertions, and when traversing to keep track of triples (predecessor, node, successor) in
     * order to detect when and how to unlink these deleted nodes. Rather than using mark-bits to
     * mark list deletions (which can be slow and space-intensive using AtomicMarkedReference),
     * nodes use direct CAS'able next pointers. On deletion, instead of marking a pointer, they
     * splice in another node that can be thought of as standing for a marked pointer (see method
     * unlinkNode). Using plain nodes acts roughly like "boxed" implementations of marked pointers,
     * but uses new nodes only when nodes are deleted, not for every link. This requires less space
     * and supports faster traversal. Even if marked references were better supported by JVMs,
     * traversal using this technique might still be faster because any search need only read ahead
     * one more node than otherwise required (to check for trailing marker) rather than unmasking
     * mark bits or whatever on each read. This approach maintains the essential property needed in
     * the HM algorithm of changing the next-pointer of a deleted node so that any other CAS of it
     * will fail, but implements the idea by changing the pointer to point to a different node (with
     * otherwise illegal null fields), not by marking it. While it would be possible to further
     * squeeze space by defining marker nodes not to have key/value fields, it isn't worth the extra
     * type-testing overhead. The deletion markers are rarely encountered during traversal, are
     * easily detected via null checks that are needed anyway, and are normally quickly garbage
     * collected. (Note that this technique would not work well in systems without garbage
     * collection.) In addition to using deletion markers, the lists also use nullness of value
     * fields to indicate deletion, in a style similar to typical lazy-deletion schemes. If a node's
     * value is null, then it is considered logically deleted and ignored even though it is still
     * reachable. Here's the sequence of events for a deletion of node n with predecessor b and
     * successor f, initially: +------+ +------+ +------+ ... | b |------>| n |----->| f | ...
     * +------+ +------+ +------+ 1. CAS n's value field from non-null to null. Traversals
     * encountering a node with null value ignore it. However, ongoing insertions and deletions
     * might still modify n's next pointer. 2. CAS n's next pointer to point to a new marker node.
     * From this point on, no other nodes can be appended to n. which avoids deletion errors in
     * CAS-based linked lists. +------+ +------+ +------+ +------+ ... | b |------>| n
     * |----->|marker|------>| f | ... +------+ +------+ +------+ +------+ 3. CAS b's next pointer
     * over both n and its marker. From this point on, no new traversals will encounter n, and it
     * can eventually be GCed. +------+ +------+ ... | b |----------------------------------->| f |
     * ... +------+ +------+ A failure at step 1 leads to simple retry due to a lost race with
     * another operation. Steps 2-3 can fail because some other thread noticed during a traversal a
     * node with null value and helped out by marking and/or unlinking. This helping-out ensures
     * that no thread can become stuck waiting for progress of the deleting thread. Skip lists add
     * indexing to this scheme, so that the base-level traversals start close to the locations being
     * found, inserted or deleted -- usually base level traversals only traverse a few nodes. This
     * doesn't change the basic algorithm except for the need to make sure base traversals start at
     * predecessors (here, b) that are not (structurally) deleted, otherwise retrying after
     * processing the deletion. Index levels are maintained using CAS to link and unlink successors
     * ("right" fields). Races are allowed in index-list operations that can (rarely) fail to link
     * in a new index node. (We can't do this of course for data nodes.) However, even when this
     * happens, the index lists correctly guide search. This can impact performance, but since skip
     * lists are probabilistic anyway, the net result is that under contention, the effective "p"
     * value may be lower than its nominal value. Index insertion and deletion sometimes require a
     * separate traversal pass occurring after the base-level action, to add or remove index nodes.
     * This adds to single-threaded overhead, but improves contended multithreaded performance by
     * narrowing interference windows, and allows deletion to ensure that all index nodes will be
     * made unreachable upon return from a public remove operation, thus avoiding unwanted garbage
     * retention. Indexing uses skip list parameters that maintain good search performance while
     * using sparser-than-usual indices: The hardwired parameters k=1, p=0.5 (see method doPut) mean
     * that about one-quarter of the nodes have indices. Of those that do, half have one level, a
     * quarter have two, and so on (see Pugh's Skip List Cookbook, sec 3.4), up to a maximum of 62
     * levels (appropriate for up to 2^63 elements). The expected total space requirement for a map
     * is slightly less than for the current implementation of java.util.TreeMap. Changing the level
     * of the index (i.e, the height of the tree-like structure) also uses CAS. Creation of an index
     * with height greater than the current level adds a level to the head index by CAS'ing on a new
     * top-most head. To maintain good performance after a lot of removals, deletion methods
     * heuristically try to reduce the height if the topmost levels appear to be empty. This may
     * encounter races in which it is possible (but rare) to reduce and "lose" a level just as it is
     * about to contain an index (that will then never be encountered). This does no structural
     * harm, and in practice appears to be a better option than allowing unrestrained growth of
     * levels. This class provides concurrent-reader-style memory consistency, ensuring that
     * read-only methods report status and/or values no staler than those holding at method entry.
     * This is done by performing all publication and structural updates using (volatile) CAS,
     * placing an acquireFence in a few access methods, and ensuring that linked objects are
     * transitively acquired via dependent reads (normally once) unless performing a volatile-mode
     * CAS operation (that also acts as an acquire and release). This form of fence-hoisting is
     * similar to RCU and related techniques (see McKenney's online book
     * https://www.kernel.org/pub/linux/kernel/people/paulmck/perfbook/perfbook.html) It minimizes
     * overhead that may otherwise occur when using so many volatile-mode reads. Using explicit
     * acquireFences is logistically easier than targeting particular fields to be read in acquire
     * mode: fences are just hoisted up as far as possible, to the entry points or loop headers of a
     * few methods. A potential disadvantage is that these few remaining fences are not easily
     * optimized away by compilers under exclusively single-thread use. It requires some care to
     * avoid volatile mode reads of other fields. (Note that the memory semantics of a reference
     * dependently read in plain mode exactly once are equivalent to those for atomic opaque mode.)
     * Iterators and other traversals encounter each node and value exactly once. Other operations
     * locate an element (or position to insert an element) via a sequence of dereferences. This
     * search is broken into two parts. Method findPredecessor (and its specialized embeddings)
     * searches index nodes only, returning a base-level predecessor of the key. Callers carry out
     * the base-level search, restarting if encountering a marker preventing link modification. In
     * some cases, it is possible to encounter a node multiple times while descending levels. For
     * mutative operations, the reported value is validated using CAS (else retrying), preserving
     * linearizability with respect to each other. Others may return any (non-null) value holding in
     * the course of the method call. (Search-based methods also include some useless-looking
     * explicit null checks designed to allow more fields to be nulled out upon removal, to reduce
     * floating garbage, but which is not currently done, pending discovery of a way to do this with
     * less impact on other operations.) To produce random values without interference across
     * threads, we use within-JDK thread local random support (via the "secondary seed", to avoid
     * interference with user-level ThreadLocalRandom.) For explanation of algorithms sharing at
     * least a couple of features with this one, see Mikhail Fomitchev's thesis
     * (http://www.cs.yorku.ca/~mikhail/), Keir Fraser's thesis
     * (http://www.cl.cam.ac.uk/users/kaf24/), and Hakan Sundell's thesis
     * (http://www.cs.chalmers.se/~phs/). Notation guide for local variables Node: b, n, f, p for
     * predecessor, node, successor, aux Index: q, r, d for index node, right, down. Head: h Keys:
     * k, key Values: v, value Comparisons: c
     */

    private static final long EMPTY = Long.MIN_VALUE;

    /** Lazily initialized topmost index of the skiplist. */
    private transient volatile Index<V> head;

    /** Lazily initialized element count */
    private transient volatile LongAdder adder;

    /** Lazily initialized key set */
    private transient volatile NavigableLongSet keySet;

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
    private final LongComparator comparator;

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

    /* ---------------- Traversal -------------- */

    /**
     * Returns an index node with key strictly less than given key. Also unlinks indexes to deleted
     * nodes found along the way. Callers rely on this side-effect of clearing indices to deleted
     * nodes.
     *
     * @param key if nonnull the key
     * @return a predecessor node of key, or null if uninitialized or null key
     */
    private Node<V> findPredecessor(long key, LongComparator cmp) {
        Index<V> q;
        VarHandle.acquireFence();
        if ((q = head) == null || key == EMPTY)
            return null;
        else {
            for (Index<V> r, d;;) {
                while ((r = q.right) != null) {
                    Node<V> p;
                    long k;
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
     * Returns node holding key or null if no such, clearing out any deleted nodes seen along the
     * way. Repeatedly traverses at base-level looking for key starting at predecessor returned from
     * findPredecessor, processing base-level deletions as encountered. Restarts occur, at traversal
     * step encountering node n, if n's key field is null, indicating it is a marker, so its
     * predecessor is deleted before continuing, which we help do by re-finding a valid predecessor.
     * The traversal loops in doPut, doRemove, and findNear all include the same checks.
     *
     * @param key the key
     * @return node holding key, or null if no such
     */
    private Node<V> findNode(long key) {
        LongComparator cmp = comparator;
        Node<V> b;
        outer: while ((b = findPredecessor(key, cmp)) != null) {
            for (;;) {
                Node<V> n;
                long k;
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

    /* ---------------- Insertion -------------- */

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
    private static <V> boolean addIndices(Index<V> q, int skips, Index<V> x, LongComparator cmp) {
        Node<V> z;
        long key;
        if (x != null && (z = x.node) != null && (key = z.key) != EMPTY && q != null) { // hoist
                                                                                        // checks
            boolean retrying = false;
            for (;;) { // find splice point
                Index<V> r, d;
                int c;
                if ((r = q.right) != null) {
                    Node<V> p;
                    long k;
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

    /* ---------------- Deletion -------------- */

    /* ---------------- Finding and removing first element -------------- */

    /* ---------------- Finding and removing last element -------------- */

    /* ---------------- Relational operations -------------- */

    // Control values OR'ed as arguments to findNear

    /*
     * This class implements a tree-like two-dimensionally linked skip list in which the index
     * levels are represented in separate nodes from the base nodes holding data. There are two
     * reasons for taking this approach instead of the usual array-based structure: 1) Array based
     * implementations seem to encounter more complexity and overhead 2) We can use cheaper
     * algorithms for the heavily-traversed index lists than can be used for the base lists. Here's
     * a picture of some of the basics for a possible list with 2 levels of index: Head nodes Index
     * nodes +-+ right +-+ +-+ |2|---------------->| |--------------------->| |->null +-+ +-+ +-+ |
     * down | | v v v +-+ +-+ +-+ +-+ +-+ +-+ |1|----------->| |->| |------>| |----------->|
     * |------>| |->null +-+ +-+ +-+ +-+ +-+ +-+ v | | | | | Nodes next v v v v v +-+ +-+ +-+ +-+
     * +-+ +-+ +-+ +-+ +-+ +-+ +-+ +-+ |
     * |->|A|->|B|->|C|->|D|->|E|->|F|->|G|->|H|->|I|->|J|->|K|->null +-+ +-+ +-+ +-+ +-+ +-+ +-+
     * +-+ +-+ +-+ +-+ +-+ The base lists use a variant of the HM linked ordered set algorithm. See
     * Tim Harris, "A pragmatic implementation of non-blocking linked lists"
     * http://www.cl.cam.ac.uk/~tlh20/publications.html and Maged Michael "High Performance Dynamic
     * Lock-Free Hash Tables and List-Based Sets"
     * http://www.research.ibm.com/people/m/michael/pubs.htm. The basic idea in these lists is to
     * mark the "next" pointers of deleted nodes when deleting to avoid conflicts with concurrent
     * insertions, and when traversing to keep track of triples (predecessor, node, successor) in
     * order to detect when and how to unlink these deleted nodes. Rather than using mark-bits to
     * mark list deletions (which can be slow and space-intensive using AtomicMarkedReference),
     * nodes use direct CAS'able next pointers. On deletion, instead of marking a pointer, they
     * splice in another node that can be thought of as standing for a marked pointer (see method
     * unlinkNode). Using plain nodes acts roughly like "boxed" implementations of marked pointers,
     * but uses new nodes only when nodes are deleted, not for every link. This requires less space
     * and supports faster traversal. Even if marked references were better supported by JVMs,
     * traversal using this technique might still be faster because any search need only read ahead
     * one more node than otherwise required (to check for trailing marker) rather than unmasking
     * mark bits or whatever on each read. This approach maintains the essential property needed in
     * the HM algorithm of changing the next-pointer of a deleted node so that any other CAS of it
     * will fail, but implements the idea by changing the pointer to point to a different node (with
     * otherwise illegal null fields), not by marking it. While it would be possible to further
     * squeeze space by defining marker nodes not to have key/value fields, it isn't worth the extra
     * type-testing overhead. The deletion markers are rarely encountered during traversal, are
     * easily detected via null checks that are needed anyway, and are normally quickly garbage
     * collected. (Note that this technique would not work well in systems without garbage
     * collection.) In addition to using deletion markers, the lists also use nullness of value
     * fields to indicate deletion, in a style similar to typical lazy-deletion schemes. If a node's
     * value is null, then it is considered logically deleted and ignored even though it is still
     * reachable. Here's the sequence of events for a deletion of node n with predecessor b and
     * successor f, initially: +------+ +------+ +------+ ... | b |------>| n |----->| f | ...
     * +------+ +------+ +------+ 1. CAS n's value field from non-null to null. Traversals
     * encountering a node with null value ignore it. However, ongoing insertions and deletions
     * might still modify n's next pointer. 2. CAS n's next pointer to point to a new marker node.
     * From this point on, no other nodes can be appended to n. which avoids deletion errors in
     * CAS-based linked lists. +------+ +------+ +------+ +------+ ... | b |------>| n
     * |----->|marker|------>| f | ... +------+ +------+ +------+ +------+ 3. CAS b's next pointer
     * over both n and its marker. From this point on, no new traversals will encounter n, and it
     * can eventually be GCed. +------+ +------+ ... | b |----------------------------------->| f |
     * ... +------+ +------+ A failure at step 1 leads to simple retry due to a lost race with
     * another operation. Steps 2-3 can fail because some other thread noticed during a traversal a
     * node with null value and helped out by marking and/or unlinking. This helping-out ensures
     * that no thread can become stuck waiting for progress of the deleting thread. Skip lists add
     * indexing to this scheme, so that the base-level traversals start close to the locations being
     * found, inserted or deleted -- usually base level traversals only traverse a few nodes. This
     * doesn't change the basic algorithm except for the need to make sure base traversals start at
     * predecessors (here, b) that are not (structurally) deleted, otherwise retrying after
     * processing the deletion. Index levels are maintained using CAS to link and unlink successors
     * ("right" fields). Races are allowed in index-list operations that can (rarely) fail to link
     * in a new index node. (We can't do this of course for data nodes.) However, even when this
     * happens, the index lists correctly guide search. This can impact performance, but since skip
     * lists are probabilistic anyway, the net result is that under contention, the effective "p"
     * value may be lower than its nominal value. Index insertion and deletion sometimes require a
     * separate traversal pass occurring after the base-level action, to add or remove index nodes.
     * This adds to single-threaded overhead, but improves contended multithreaded performance by
     * narrowing interference windows, and allows deletion to ensure that all index nodes will be
     * made unreachable upon return from a public remove operation, thus avoiding unwanted garbage
     * retention. Indexing uses skip list parameters that maintain good search performance while
     * using sparser-than-usual indices: The hardwired parameters k=1, p=0.5 (see method doPut) mean
     * that about one-quarter of the nodes have indices. Of those that do, half have one level, a
     * quarter have two, and so on (see Pugh's Skip List Cookbook, sec 3.4), up to a maximum of 62
     * levels (appropriate for up to 2^63 elements). The expected total space requirement for a map
     * is slightly less than for the current implementation of java.util.TreeMap. Changing the level
     * of the index (i.e, the height of the tree-like structure) also uses CAS. Creation of an index
     * with height greater than the current level adds a level to the head index by CAS'ing on a new
     * top-most head. To maintain good performance after a lot of removals, deletion methods
     * heuristically try to reduce the height if the topmost levels appear to be empty. This may
     * encounter races in which it is possible (but rare) to reduce and "lose" a level just as it is
     * about to contain an index (that will then never be encountered). This does no structural
     * harm, and in practice appears to be a better option than allowing unrestrained growth of
     * levels. This class provides concurrent-reader-style memory consistency, ensuring that
     * read-only methods report status and/or values no staler than those holding at method entry.
     * This is done by performing all publication and structural updates using (volatile) CAS,
     * placing an acquireFence in a few access methods, and ensuring that linked objects are
     * transitively acquired via dependent reads (normally once) unless performing a volatile-mode
     * CAS operation (that also acts as an acquire and release). This form of fence-hoisting is
     * similar to RCU and related techniques (see McKenney's online book
     * https://www.kernel.org/pub/linux/kernel/people/paulmck/perfbook/perfbook.html) It minimizes
     * overhead that may otherwise occur when using so many volatile-mode reads. Using explicit
     * acquireFences is logistically easier than targeting particular fields to be read in acquire
     * mode: fences are just hoisted up as far as possible, to the entry points or loop headers of a
     * few methods. A potential disadvantage is that these few remaining fences are not easily
     * optimized away by compilers under exclusively single-thread use. It requires some care to
     * avoid volatile mode reads of other fields. (Note that the memory semantics of a reference
     * dependently read in plain mode exactly once are equivalent to those for atomic opaque mode.)
     * Iterators and other traversals encounter each node and value exactly once. Other operations
     * locate an element (or position to insert an element) via a sequence of dereferences. This
     * search is broken into two parts. Method findPredecessor (and its specialized embeddings)
     * searches index nodes only, returning a base-level predecessor of the key. Callers carry out
     * the base-level search, restarting if encountering a marker preventing link modification. In
     * some cases, it is possible to encounter a node multiple times while descending levels. For
     * mutative operations, the reported value is validated using CAS (else retrying), preserving
     * linearizability with respect to each other. Others may return any (non-null) value holding in
     * the course of the method call. (Search-based methods also include some useless-looking
     * explicit null checks designed to allow more fields to be nulled out upon removal, to reduce
     * floating garbage, but which is not currently done, pending discovery of a way to do this with
     * less impact on other operations.) To produce random values without interference across
     * threads, we use within-JDK thread local random support (via the "secondary seed", to avoid
     * interference with user-level ThreadLocalRandom.) For explanation of algorithms sharing at
     * least a couple of features with this one, see Mikhail Fomitchev's thesis
     * (http://www.cs.yorku.ca/~mikhail/), Keir Fraser's thesis
     * (http://www.cl.cam.ac.uk/users/kaf24/), and Hakan Sundell's thesis
     * (http://www.cs.chalmers.se/~phs/). Notation guide for local variables Node: b, n, f, p for
     * predecessor, node, successor, aux Index: q, r, d for index node, right, down. Head: h Keys:
     * k, key Values: v, value Comparisons: c
     */

    /**
     * Constructs a new, empty map, sorted according to the specified comparator.
     *
     * @param comparator the comparator that will be used to order this map. If {@code null}, the
     *            {@linkplain Comparable natural ordering} of the keys will be used.
     */
    SkipListLongMap(LongComparator comparator) {
        this.comparator = comparator == null ? Long::compare : comparator;
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

    /* ---------------- Deletion -------------- */

    /* ---------------- Finding and removing first element -------------- */

    /* ---------------- Finding and removing last element -------------- */

    /* ---------------- Relational operations -------------- */

    // Control values OR'ed as arguments to findNear

    /*
     * This class implements a tree-like two-dimensionally linked skip list in which the index
     * levels are represented in separate nodes from the base nodes holding data. There are two
     * reasons for taking this approach instead of the usual array-based structure: 1) Array based
     * implementations seem to encounter more complexity and overhead 2) We can use cheaper
     * algorithms for the heavily-traversed index lists than can be used for the base lists. Here's
     * a picture of some of the basics for a possible list with 2 levels of index: Head nodes Index
     * nodes +-+ right +-+ +-+ |2|---------------->| |--------------------->| |->null +-+ +-+ +-+ |
     * down | | v v v +-+ +-+ +-+ +-+ +-+ +-+ |1|----------->| |->| |------>| |----------->|
     * |------>| |->null +-+ +-+ +-+ +-+ +-+ +-+ v | | | | | Nodes next v v v v v +-+ +-+ +-+ +-+
     * +-+ +-+ +-+ +-+ +-+ +-+ +-+ +-+ |
     * |->|A|->|B|->|C|->|D|->|E|->|F|->|G|->|H|->|I|->|J|->|K|->null +-+ +-+ +-+ +-+ +-+ +-+ +-+
     * +-+ +-+ +-+ +-+ +-+ The base lists use a variant of the HM linked ordered set algorithm. See
     * Tim Harris, "A pragmatic implementation of non-blocking linked lists"
     * http://www.cl.cam.ac.uk/~tlh20/publications.html and Maged Michael "High Performance Dynamic
     * Lock-Free Hash Tables and List-Based Sets"
     * http://www.research.ibm.com/people/m/michael/pubs.htm. The basic idea in these lists is to
     * mark the "next" pointers of deleted nodes when deleting to avoid conflicts with concurrent
     * insertions, and when traversing to keep track of triples (predecessor, node, successor) in
     * order to detect when and how to unlink these deleted nodes. Rather than using mark-bits to
     * mark list deletions (which can be slow and space-intensive using AtomicMarkedReference),
     * nodes use direct CAS'able next pointers. On deletion, instead of marking a pointer, they
     * splice in another node that can be thought of as standing for a marked pointer (see method
     * unlinkNode). Using plain nodes acts roughly like "boxed" implementations of marked pointers,
     * but uses new nodes only when nodes are deleted, not for every link. This requires less space
     * and supports faster traversal. Even if marked references were better supported by JVMs,
     * traversal using this technique might still be faster because any search need only read ahead
     * one more node than otherwise required (to check for trailing marker) rather than unmasking
     * mark bits or whatever on each read. This approach maintains the essential property needed in
     * the HM algorithm of changing the next-pointer of a deleted node so that any other CAS of it
     * will fail, but implements the idea by changing the pointer to point to a different node (with
     * otherwise illegal null fields), not by marking it. While it would be possible to further
     * squeeze space by defining marker nodes not to have key/value fields, it isn't worth the extra
     * type-testing overhead. The deletion markers are rarely encountered during traversal, are
     * easily detected via null checks that are needed anyway, and are normally quickly garbage
     * collected. (Note that this technique would not work well in systems without garbage
     * collection.) In addition to using deletion markers, the lists also use nullness of value
     * fields to indicate deletion, in a style similar to typical lazy-deletion schemes. If a node's
     * value is null, then it is considered logically deleted and ignored even though it is still
     * reachable. Here's the sequence of events for a deletion of node n with predecessor b and
     * successor f, initially: +------+ +------+ +------+ ... | b |------>| n |----->| f | ...
     * +------+ +------+ +------+ 1. CAS n's value field from non-null to null. Traversals
     * encountering a node with null value ignore it. However, ongoing insertions and deletions
     * might still modify n's next pointer. 2. CAS n's next pointer to point to a new marker node.
     * From this point on, no other nodes can be appended to n. which avoids deletion errors in
     * CAS-based linked lists. +------+ +------+ +------+ +------+ ... | b |------>| n
     * |----->|marker|------>| f | ... +------+ +------+ +------+ +------+ 3. CAS b's next pointer
     * over both n and its marker. From this point on, no new traversals will encounter n, and it
     * can eventually be GCed. +------+ +------+ ... | b |----------------------------------->| f |
     * ... +------+ +------+ A failure at step 1 leads to simple retry due to a lost race with
     * another operation. Steps 2-3 can fail because some other thread noticed during a traversal a
     * node with null value and helped out by marking and/or unlinking. This helping-out ensures
     * that no thread can become stuck waiting for progress of the deleting thread. Skip lists add
     * indexing to this scheme, so that the base-level traversals start close to the locations being
     * found, inserted or deleted -- usually base level traversals only traverse a few nodes. This
     * doesn't change the basic algorithm except for the need to make sure base traversals start at
     * predecessors (here, b) that are not (structurally) deleted, otherwise retrying after
     * processing the deletion. Index levels are maintained using CAS to link and unlink successors
     * ("right" fields). Races are allowed in index-list operations that can (rarely) fail to link
     * in a new index node. (We can't do this of course for data nodes.) However, even when this
     * happens, the index lists correctly guide search. This can impact performance, but since skip
     * lists are probabilistic anyway, the net result is that under contention, the effective "p"
     * value may be lower than its nominal value. Index insertion and deletion sometimes require a
     * separate traversal pass occurring after the base-level action, to add or remove index nodes.
     * This adds to single-threaded overhead, but improves contended multithreaded performance by
     * narrowing interference windows, and allows deletion to ensure that all index nodes will be
     * made unreachable upon return from a public remove operation, thus avoiding unwanted garbage
     * retention. Indexing uses skip list parameters that maintain good search performance while
     * using sparser-than-usual indices: The hardwired parameters k=1, p=0.5 (see method doPut) mean
     * that about one-quarter of the nodes have indices. Of those that do, half have one level, a
     * quarter have two, and so on (see Pugh's Skip List Cookbook, sec 3.4), up to a maximum of 62
     * levels (appropriate for up to 2^63 elements). The expected total space requirement for a map
     * is slightly less than for the current implementation of java.util.TreeMap. Changing the level
     * of the index (i.e, the height of the tree-like structure) also uses CAS. Creation of an index
     * with height greater than the current level adds a level to the head index by CAS'ing on a new
     * top-most head. To maintain good performance after a lot of removals, deletion methods
     * heuristically try to reduce the height if the topmost levels appear to be empty. This may
     * encounter races in which it is possible (but rare) to reduce and "lose" a level just as it is
     * about to contain an index (that will then never be encountered). This does no structural
     * harm, and in practice appears to be a better option than allowing unrestrained growth of
     * levels. This class provides concurrent-reader-style memory consistency, ensuring that
     * read-only methods report status and/or values no staler than those holding at method entry.
     * This is done by performing all publication and structural updates using (volatile) CAS,
     * placing an acquireFence in a few access methods, and ensuring that linked objects are
     * transitively acquired via dependent reads (normally once) unless performing a volatile-mode
     * CAS operation (that also acts as an acquire and release). This form of fence-hoisting is
     * similar to RCU and related techniques (see McKenney's online book
     * https://www.kernel.org/pub/linux/kernel/people/paulmck/perfbook/perfbook.html) It minimizes
     * overhead that may otherwise occur when using so many volatile-mode reads. Using explicit
     * acquireFences is logistically easier than targeting particular fields to be read in acquire
     * mode: fences are just hoisted up as far as possible, to the entry points or loop headers of a
     * few methods. A potential disadvantage is that these few remaining fences are not easily
     * optimized away by compilers under exclusively single-thread use. It requires some care to
     * avoid volatile mode reads of other fields. (Note that the memory semantics of a reference
     * dependently read in plain mode exactly once are equivalent to those for atomic opaque mode.)
     * Iterators and other traversals encounter each node and value exactly once. Other operations
     * locate an element (or position to insert an element) via a sequence of dereferences. This
     * search is broken into two parts. Method findPredecessor (and its specialized embeddings)
     * searches index nodes only, returning a base-level predecessor of the key. Callers carry out
     * the base-level search, restarting if encountering a marker preventing link modification. In
     * some cases, it is possible to encounter a node multiple times while descending levels. For
     * mutative operations, the reported value is validated using CAS (else retrying), preserving
     * linearizability with respect to each other. Others may return any (non-null) value holding in
     * the course of the method call. (Search-based methods also include some useless-looking
     * explicit null checks designed to allow more fields to be nulled out upon removal, to reduce
     * floating garbage, but which is not currently done, pending discovery of a way to do this with
     * less impact on other operations.) To produce random values without interference across
     * threads, we use within-JDK thread local random support (via the "secondary seed", to avoid
     * interference with user-level ThreadLocalRandom.) For explanation of algorithms sharing at
     * least a couple of features with this one, see Mikhail Fomitchev's thesis
     * (http://www.cs.yorku.ca/~mikhail/), Keir Fraser's thesis
     * (http://www.cl.cam.ac.uk/users/kaf24/), and Hakan Sundell's thesis
     * (http://www.cs.chalmers.se/~phs/). Notation guide for local variables Node: b, n, f, p for
     * predecessor, node, successor, aux Index: q, r, d for index node, right, down. Head: h Keys:
     * k, key Values: v, value Comparisons: c
     */

    /**
     * Utility for ceiling, floor, lower, higher methods.
     * 
     * @param key the key
     * @param rel the relation -- OR'ed combination of EQ, LT, GT
     * @return nearest node fitting relation, or null if no such
     */
    final Node<V> findNear(long key, int rel, LongComparator cmp) {
        Node<V> result;
        outer: for (Node<V> b;;) {
            if ((b = findPredecessor(key, cmp)) == null) {
                result = null;
                break; // empty
            }
            for (;;) {
                Node<V> n;
                long k;
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
    final LongEntry<V> findNearEntry(long key, int rel, LongComparator cmp) {
        for (;;) {
            Node<V> n;
            V v;
            if ((n = findNear(key, rel, cmp)) == null) return null;
            if ((v = n.value) != null) return LongEntry.immutable(n.key, v);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean containsKey(long key) {
        return doGet(key) != null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public V get(long key) {
        return doGet(key);
    }

    /**
     * Gets value for key. Same idea as findNode, except skips over deletions and markers, and
     * returns first encountered value to avoid possibly inconsistent rereads.
     *
     * @param key the key
     * @return the value, or null if absent
     */
    private V doGet(long key) {
        Index<V> q;
        VarHandle.acquireFence();
        LongComparator cmp = comparator;
        V result = null;
        if ((q = head) != null) {
            outer: for (Index<V> r, d;;) {
                while ((r = q.right) != null) {
                    Node<V> p;
                    long k;
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
                            long k = n.key;
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
    public V put(long key, V value) {
        if (value == null) {
            throw new NullPointerException();
        }
        return doPut(key, value, false);
    }

    /* ---------------- Insertion -------------- */

    /**
     * Main insertion method. Adds element if not present, or replaces value if present and
     * onlyIfAbsent is false.
     *
     * @param key the key
     * @param value the value that must be associated with key
     * @param onlyIfAbsent if should not insert if already present
     * @return the old value, or null if newly inserted
     */
    private V doPut(long key, V value, boolean onlyIfAbsent) {
        LongComparator cmp = comparator;
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
                        long k;
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
                    long k;
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
    public V remove(long key) {
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
    final V doRemove(long key, Object value) {
        LongComparator cmp = comparator;
        V result = null;
        Node<V> b;
        outer: while ((b = findPredecessor(key, cmp)) != null && result == null) {
            for (;;) {
                Node<V> n;
                long k;
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
    public V computeIfAbsent(long key, LongFunction<? extends V> mappingFunction) {
        if (key == EMPTY || mappingFunction == null) throw new NullPointerException();
        V v, p, r;
        if ((v = doGet(key)) == null && (r = mappingFunction.apply(key)) != null) v = (p = doPut(key, r, true)) == null ? r : p;
        return v;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public V merge(long key, V value, BiFunction<? super V, ? super V, ? extends V> remappingFunction) {
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
    public NavigableSet<Long> keySet() {
        NavigableLongSet ks;
        if ((ks = keySet) != null) return ks;
        return keySet = new Keys<>(this);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public NavigableLongSet navigableKeySet() {
        NavigableLongSet ks;
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
    public Set<Entry<Long, V>> entrySet() {
        return (Set) longEntrySet();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Set<LongEntry<V>> longEntrySet() {
        Entries<V> es;
        if ((es = entrySet) != null) return es;
        return entrySet = new Entries<V>(this);
    }

    @Override
    public ConcurrentNavigableLongMap<V> descendingMap() {
        ConcurrentNavigableLongMap<V> dm;
        if ((dm = descendingMap) != null) return dm;
        return descendingMap = new SubMap<V>(this, EMPTY, false, EMPTY, false, true);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public NavigableLongSet descendingKeySet() {
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
            LongComparator cmp = comparator;
            // See JDK-8223553 for Iterator type wildcard rationale
            Iterator<? extends Map.Entry<?, ?>> it = m.entrySet().iterator();
            if (m instanceof SortedMap && ((SortedMap<?, ?>) m).comparator() == cmp) {
                Node<V> b, n;
                if ((b = baseHead()) != null) {
                    while ((n = b.next) != null) {
                        long k;
                        V v;
                        if ((v = n.value) != null && (k = n.key) != EMPTY) {
                            if (!it.hasNext()) return false;
                            Map.Entry<?, ?> e = it.next();
                            long mk = (Long) e.getKey();
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
                    long k;
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
    public V putIfAbsent(long key, V value) {
        return doPut(key, value, true);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean remove(long key, Object value) {
        return value != null && doRemove(key, value) != null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean replace(long key, V oldValue, V newValue) {
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
    public V replace(long key, V value) {
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
    public LongComparator comparator() {
        return comparator;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public long firstLongKey() {
        Node<V> n = findFirst();
        if (n == null) {
            throw new NoSuchElementException();
        }
        return n.key;
    }

    /* ---------------- Finding and removing first element -------------- */

    /**
     * {@inheritDoc}
     */
    @Override
    public long lastLongKey() {
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
    public ConcurrentNavigableLongMap<V> subMap(long fromKey, boolean fromInclusive, long toKey, boolean toInclusive) {
        return new SubMap<V>(this, fromKey, fromInclusive, toKey, toInclusive, false);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ConcurrentNavigableLongMap<V> headMap(long toKey, boolean inclusive) {
        return new SubMap<V>(this, EMPTY, false, toKey, inclusive, false);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ConcurrentNavigableLongMap<V> tailMap(long fromKey, boolean inclusive) {
        return new SubMap<V>(this, fromKey, inclusive, EMPTY, false, false);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ConcurrentNavigableLongMap<V> subMap(long fromKey, long toKey) {
        return subMap(fromKey, true, toKey, false);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ConcurrentNavigableLongMap<V> headMap(long toKey) {
        return headMap(toKey, false);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ConcurrentNavigableLongMap<V> tailMap(long fromKey) {
        return tailMap(fromKey, true);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public LongEntry<V> lowerEntry(long key) {
        return findNearEntry(key, LT, comparator);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public long lowerKey(long key) {
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
    public LongEntry<V> floorEntry(long key) {
        return findNearEntry(key, LT | EQ, comparator);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public long floorKey(long key) {
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
    public LongEntry<V> ceilingEntry(long key) {
        return findNearEntry(key, GT | EQ, comparator);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public long ceilingKey(long key) {
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
    public LongEntry<V> higherEntry(long key) {
        return findNearEntry(key, GT, comparator);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public long higherKey(long key) {
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
    public LongEntry firstEntry() {
        Node<V> node = findFirst();
        return node == null || node.value == null ? null : LongEntry.immutable(node.key, node.value);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public LongEntry<V> lastEntry() {
        Node<V> node = findLast();
        return node == null || node.value == null ? null : LongEntry.immutable(node.key, node.value);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public LongEntry<V> pollFirstEntry() {
        Node<V> b, n;
        V v;
        if ((b = baseHead()) != null) {
            while ((n = b.next) != null) {
                if ((v = n.value) == null || VALUE.compareAndSet(n, v, null)) {
                    long k = n.key;
                    unlinkNode(b, n);
                    if (v != null) {
                        tryReduceLevel();
                        findPredecessor(k, comparator); // clean index
                        addCount(-1L);
                        return LongEntry.immutable(k, v);
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
    public LongEntry<V> pollLastEntry() {
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
                    long k;
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
                        return LongEntry.immutable(k, v);
                    }
                }
            }
        }
        return null;
    }

    // default Map method overrides

    @Override
    public void forEach(BiConsumer<? super Long, ? super V> action) {
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
    public void replaceAll(BiFunction<? super Long, ? super V, ? extends V> function) {
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
    private boolean removeEntryIf(Predicate<? super LongEntry<V>> function) {
        if (function == null) throw new NullPointerException();
        boolean removed = false;
        Node<V> b, n;
        V v;
        if ((b = baseHead()) != null) {
            while ((n = b.next) != null) {
                if ((v = n.value) != null) {
                    long k = n.key;
                    LongEntry<V> e = LongEntry.immutable(k, v);
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
     * Submaps returned by {@link SkipListLongMap} submap operations represent a subrange of
     * mappings of their underlying maps. Instances of this class support all methods of their
     * underlying maps, differing in that mappings outside their range are ignored, and attempts to
     * add mappings outside their ranges result in {@link IllegalArgumentException}. Instances of
     * this class are constructed only using the {@code subMap}, {@code headMap}, and
     * {@code tailMap} methods of their underlying maps.
     */
    private static class SubMap<V> extends AbstractMap<Long, V> implements ConcurrentNavigableLongMap<V> {

        /** Underlying map */
        private final SkipListLongMap<V> m;

        /** lower bound key, or null if from start */
        private final long lo;

        /** upper bound key, or null if to end */
        private final long hi;

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
        SubMap(SkipListLongMap<V> map, long fromKey, boolean fromInclusive, long toKey, boolean toInclusive, boolean isDescending) {
            LongComparator cmp = map.comparator;
            if (fromKey != EMPTY && toKey != EMPTY && cmp.compare(fromKey, toKey) > 0)
                throw new IllegalArgumentException("inconsistent range");
            this.m = map;
            this.lo = fromKey;
            this.hi = toKey;
            this.loInclusive = fromInclusive;
            this.hiInclusive = toInclusive;
            this.isDescending = isDescending;
        }

        boolean tooLow(long key, LongComparator cmp) {
            int c;
            return (lo != EMPTY && ((c = cmp.compare(key, lo)) < 0 || (c == 0 && !loInclusive)));
        }

        boolean tooHigh(long key, LongComparator cmp) {
            int c;
            return (hi != EMPTY && ((c = cmp.compare(key, hi)) > 0 || (c == 0 && !hiInclusive)));
        }

        boolean inBounds(long key, LongComparator cmp) {
            return !tooLow(key, cmp) && !tooHigh(key, cmp);
        }

        void checkKeyBounds(long key, LongComparator cmp) {
            if (!inBounds(key, cmp)) throw new IllegalArgumentException("key out of range");
        }

        /**
         * Returns true if node key is less than upper bound of range.
         */
        boolean isBeforeEnd(SkipListLongMap.Node<V> n, LongComparator cmp) {
            if (n == null) return false;
            if (hi == EMPTY) return true;
            long k = n.key;
            if (k == EMPTY) // pass by markers and headers
                return true;
            int c = cmp.compare(k, hi);
            return c < 0 || (c == 0 && hiInclusive);
        }

        /**
         * Returns lowest node. This node might not be in range, so most usages need to check
         * bounds.
         */
        SkipListLongMap.Node<V> loNode(LongComparator cmp) {
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
        SkipListLongMap.Node<V> hiNode(LongComparator cmp) {
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
        long lowestKey() {
            LongComparator cmp = m.comparator;
            SkipListLongMap.Node<V> n = loNode(cmp);
            if (isBeforeEnd(n, cmp))
                return n.key;
            else
                throw new NoSuchElementException();
        }

        /**
         * Returns highest absolute key (ignoring directionality).
         */
        long highestKey() {
            LongComparator cmp = m.comparator;
            SkipListLongMap.Node<V> n = hiNode(cmp);
            if (n != null) {
                long last = n.key;
                if (inBounds(last, cmp)) return last;
            }
            throw new NoSuchElementException();
        }

        LongEntry<V> lowestEntry() {
            LongComparator cmp = m.comparator;
            for (;;) {
                SkipListLongMap.Node<V> n;
                V v;
                if ((n = loNode(cmp)) == null || !isBeforeEnd(n, cmp))
                    return null;
                else if ((v = n.value) != null) return LongEntry.immutable(n.key, v);
            }
        }

        LongEntry<V> highestEntry() {
            LongComparator cmp = m.comparator;
            for (;;) {
                SkipListLongMap.Node<V> n;
                V v;
                if ((n = hiNode(cmp)) == null || !inBounds(n.key, cmp))
                    return null;
                else if ((v = n.value) != null) return LongEntry.immutable(n.key, v);
            }
        }

        LongEntry<V> removeLowest() {
            LongComparator cmp = m.comparator;
            for (;;) {
                SkipListLongMap.Node<V> n;
                long k;
                V v;
                if ((n = loNode(cmp)) == null)
                    return null;
                else if (!inBounds((k = n.key), cmp))
                    return null;
                else if ((v = m.doRemove(k, null)) != null) return LongEntry.immutable(k, v);
            }
        }

        LongEntry<V> removeHighest() {
            LongComparator cmp = m.comparator;
            for (;;) {
                SkipListLongMap.Node<V> n;
                long k;
                V v;
                if ((n = hiNode(cmp)) == null)
                    return null;
                else if (!inBounds((k = n.key), cmp))
                    return null;
                else if ((v = m.doRemove(k, null)) != null) return LongEntry.immutable(k, v);
            }
        }

        /**
         * Submap version of ConcurrentSkipListLongMap.findNearEntry.
         */
        LongEntry<V> getNearEntry(long key, int rel) {
            LongComparator cmp = m.comparator;
            if (isDescending) { // adjust relation for direction
                if ((rel & LT) == 0)
                    rel |= LT;
                else
                    rel &= ~LT;
            }
            if (tooLow(key, cmp)) return ((rel & LT) != 0) ? null : lowestEntry();
            if (tooHigh(key, cmp)) return ((rel & LT) != 0) ? highestEntry() : null;
            LongEntry<V> e = m.findNearEntry(key, rel, cmp);
            if (e == null || !inBounds(e.getLongKey(), cmp))
                return null;
            else
                return e;
        }

        // Almost the same as getNearEntry, except for keys
        long getNearKey(long key, int rel) {
            LongComparator cmp = m.comparator;
            if (isDescending) { // adjust relation for direction
                if ((rel & LT) == 0)
                    rel |= LT;
                else
                    rel &= ~LT;
            }
            if (tooLow(key, cmp)) {
                if ((rel & LT) == 0) {
                    SkipListLongMap.Node<V> n = loNode(cmp);
                    if (isBeforeEnd(n, cmp)) return n.key;
                }
                return EMPTY;
            }
            if (tooHigh(key, cmp)) {
                if ((rel & LT) != 0) {
                    SkipListLongMap.Node<V> n = hiNode(cmp);
                    if (n != null) {
                        long last = n.key;
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
        public boolean containsKey(long key) {
            return inBounds(key, m.comparator) && m.containsKey(key);
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public V get(long key) {
            return (!inBounds(key, m.comparator)) ? null : m.get(key);
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public V put(long key, V value) {
            checkKeyBounds(key, m.comparator);
            return m.put(key, value);
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public V remove(long key) {
            return (!inBounds(key, m.comparator)) ? null : m.remove(key);
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public int size() {
            LongComparator cmp = m.comparator;
            long count = 0;
            for (SkipListLongMap.Node<V> n = loNode(cmp); isBeforeEnd(n, cmp); n = n.next) {
                if (n.value != null) ++count;
            }
            return count >= Integer.MAX_VALUE ? Integer.MAX_VALUE : (int) count;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public boolean isEmpty() {
            LongComparator cmp = m.comparator;
            return !isBeforeEnd(loNode(cmp), cmp);
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public boolean containsValue(Object value) {
            if (value == null) throw new NullPointerException();
            LongComparator cmp = m.comparator;
            for (SkipListLongMap.Node<V> n = loNode(cmp); isBeforeEnd(n, cmp); n = n.next) {
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
            LongComparator cmp = m.comparator;
            for (SkipListLongMap.Node<V> n = loNode(cmp); isBeforeEnd(n, cmp); n = n.next) {
                if (n.value != null) m.remove(n.key);
            }
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public V putIfAbsent(long key, V value) {
            checkKeyBounds(key, m.comparator);
            return m.putIfAbsent(key, value);
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public boolean remove(long key, Object value) {
            return inBounds(key, m.comparator) && m.remove(key, value);
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public boolean replace(long key, V oldValue, V newValue) {
            checkKeyBounds(key, m.comparator);
            return m.replace(key, oldValue, newValue);
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public V replace(long key, V value) {
            checkKeyBounds(key, m.comparator);
            return m.replace(key, value);
        }

        @Override
        public Comparator<Long> comparator() {
            LongComparator cmp = m.comparator();
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
        SubMap<V> newSubMap(long fromKey, boolean fromInclusive, long toKey, boolean toInclusive) {
            LongComparator cmp = m.comparator;
            if (isDescending) { // flip senses
                long tk = fromKey;
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
        public ConcurrentNavigableLongMap<V> subMap(long fromKey, boolean fromInclusive, long toKey, boolean toInclusive) {
            return newSubMap(fromKey, fromInclusive, toKey, toInclusive);
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public ConcurrentNavigableLongMap<V> headMap(long toKey, boolean inclusive) {
            return newSubMap(EMPTY, false, toKey, inclusive);
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public ConcurrentNavigableLongMap<V> tailMap(long fromKey, boolean inclusive) {
            return newSubMap(fromKey, inclusive, EMPTY, false);
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public ConcurrentNavigableLongMap<V> subMap(long fromKey, long toKey) {
            return subMap(fromKey, true, toKey, false);
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public ConcurrentNavigableLongMap<V> headMap(long toKey) {
            return headMap(toKey, false);
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public ConcurrentNavigableLongMap<V> tailMap(long fromKey) {
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
        public LongEntry<V> lowerEntry(long key) {
            return getNearEntry(key, LT);
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public long lowerKey(long key) {
            return getNearKey(key, LT);
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public LongEntry<V> floorEntry(long key) {
            return getNearEntry(key, LT | EQ);
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public long floorKey(long key) {
            return getNearKey(key, LT | EQ);
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public LongEntry<V> ceilingEntry(long key) {
            return getNearEntry(key, GT | EQ);
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public long ceilingKey(long key) {
            return getNearKey(key, GT | EQ);
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public LongEntry<V> higherEntry(long key) {
            return getNearEntry(key, GT);
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public long higherKey(long key) {
            return getNearKey(key, GT);
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public Entry<Long, V> lowerEntry(Long key) {
            return null;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public Long lowerKey(Long key) {
            return null;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public Entry<Long, V> floorEntry(Long key) {
            return null;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public Long floorKey(Long key) {
            return null;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public Entry<Long, V> ceilingEntry(Long key) {
            return null;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public Long ceilingKey(Long key) {
            return null;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public Entry<Long, V> higherEntry(Long key) {
            return null;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public Long higherKey(Long key) {
            return null;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public long firstLongKey() {
            return isDescending ? highestKey() : lowestKey();
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public LongEntry<V> firstEntry() {
            return isDescending ? highestEntry() : lowestEntry();
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public long lastLongKey() {
            return isDescending ? lowestKey() : highestKey();
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public LongEntry<V> lastEntry() {
            return isDescending ? lowestEntry() : highestEntry();
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public LongEntry<V> pollFirstEntry() {
            return isDescending ? removeHighest() : removeLowest();
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public LongEntry<V> pollLastEntry() {
            return isDescending ? removeLowest() : removeHighest();
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public NavigableLongSet keySet() {
            NavigableLongSet ks;
            if ((ks = keySetView) != null) return ks;
            return keySetView = new Keys<>(this);
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public NavigableLongSet navigableKeySet() {
            NavigableLongSet ks;
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
        public Set<Entry<Long, V>> entrySet() {
            return (Set) longEntrySet();
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public Set<LongEntry<V>> longEntrySet() {
            if (entrySetView == null) {
                entrySetView = new Entries(this);
            }
            return entrySetView;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public NavigableLongSet descendingKeySet() {
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
                LongComparator cmp = m.comparator;
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
                LongComparator cmp = m.comparator;
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
                LongComparator cmp = m.comparator;
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
    private static final class Keys<V> extends AbstractSet<Long> implements NavigableLongSet {

        /** The original map. */
        private final ConcurrentNavigableLongMap<V> m;

        /**
         * Build key-set view.
         * 
         * @param map
         */
        private Keys(ConcurrentNavigableLongMap<V> map) {
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
        public boolean contains(Object o) {
            return m.containsKey(o);
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public boolean remove(Object o) {
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
        public long lower(long e) {
            return m.lowerKey(e);
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public long floor(long e) {
            return m.floorKey(e);
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public long ceiling(long e) {
            return m.ceilingKey(e);
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public long higher(long e) {
            return m.higherKey(e);
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public Comparator<? super Long> comparator() {
            return m.comparator();
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public long firstLong() {
            return m.firstLongKey();
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public long lastLong() {
            return m.lastLongKey();
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public long pollFirstLong() {
            LongEntry<V> entry = m.pollFirstEntry();
            if (entry == null) {
                throw new NoSuchElementException();
            }
            return entry.getLongKey();
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public long pollLastLong() {
            LongEntry<V> entry = m.pollLastEntry();
            if (entry == null) {
                throw new NoSuchElementException();
            }
            return entry.getLongKey();
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

        /**
         * {@inheritDoc}
         */
        @Override
        public Iterator<Long> descendingIterator() {
            return descendingSet().iterator();
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public NavigableLongSet subSet(long fromElement, boolean fromInclusive, long toElement, boolean toInclusive) {
            return new Keys(m.subMap(fromElement, fromInclusive, toElement, toInclusive));
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public NavigableLongSet headSet(long toElement, boolean inclusive) {
            return new Keys<>(m.headMap(toElement, inclusive));
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public NavigableLongSet tailSet(long fromElement, boolean inclusive) {
            return new Keys<>(m.tailMap(fromElement, inclusive));
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public NavigableLongSet subSet(long fromElement, long toElement) {
            return subSet(fromElement, true, toElement, false);
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public NavigableLongSet headSet(long toElement) {
            return headSet(toElement, false);
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public NavigableLongSet tailSet(long fromElement) {
            return tailSet(fromElement, true);
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public NavigableLongSet descendingSet() {
            return new Keys<>(m.descendingMap());
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public Iterator<Long> iterator() {
            if (m instanceof SkipListLongMap) {
                return ((SkipListLongMap) m).createIteratorFor(Type.Key);
            } else {
                return ((SubMap) m).new SubMapGenericIterator(Type.Key);
            }
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public Spliterator<Long> spliterator() {
            return (m instanceof SkipListLongMap) ? ((SkipListLongMap<V>) m).createSpliteratorFor(Type.Key)
                    : ((SubMap<V>) m).new SubMapGenericIterator(Type.Key);
        }
    }

    /**
     * 
     */
    private static class Values<V> extends AbstractCollection<V> {

        /** The original view. */
        private final ConcurrentNavigableLongMap<V> m;

        /**
         * Build view.
         * 
         * @param map
         */
        private Values(ConcurrentNavigableLongMap<V> map) {
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
            if (m instanceof SkipListLongMap) {
                return ((SkipListLongMap) m).createIteratorFor(Type.Value);
            } else {
                return ((SubMap) m).new SubMapGenericIterator(Type.Value);
            }
        }

        @Override
        public Spliterator<V> spliterator() {
            return (m instanceof SkipListLongMap) ? ((SkipListLongMap<V>) m).createSpliteratorFor(Type.Value)
                    : ((SubMap<V>) m).new SubMapGenericIterator(Type.Value);
        }

        @Override
        public boolean removeIf(Predicate<? super V> filter) {
            if (filter == null) throw new NullPointerException();
            if (m instanceof SkipListLongMap) return ((SkipListLongMap<V>) m).removeValueIf(filter);
            // else use iterator
            Iterator<LongEntry<V>> it = ((SubMap<V>) m).new SubMapGenericIterator(Type.Entry);
            boolean removed = false;
            while (it.hasNext()) {
                LongEntry<V> e = it.next();
                V v = e.getValue();
                if (filter.test(v) && m.remove(e.getLongKey(), v)) removed = true;
            }
            return removed;
        }
    }

    /**
     * 
     */
    private static class Entries<V> extends AbstractSet<LongEntry<V>> {

        /** The original view. */
        private final ConcurrentNavigableLongMap<V> m;

        /**
         * Build view.
         * 
         * @param map
         */
        private Entries(ConcurrentNavigableLongMap<V> map) {
            m = map;
        }

        @Override
        public Iterator<LongEntry<V>> iterator() {
            if (m instanceof SkipListLongMap) {
                return ((SkipListLongMap) m).createIteratorFor(Type.Entry);
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
        public Spliterator<LongEntry<V>> spliterator() {
            return (m instanceof SkipListLongMap) ? ((SkipListLongMap<V>) m).createSpliteratorFor(Type.Entry)
                    : ((SubMap<V>) m).new SubMapGenericIterator(Type.Entry);
        }

        @Override
        public boolean removeIf(Predicate<? super LongEntry<V>> filter) {
            if (filter == null) throw new NullPointerException();
            if (m instanceof SkipListLongMap) return ((SkipListLongMap<V>) m).removeEntryIf(filter);
            // else use iterator
            Iterator<LongEntry<V>> it = ((SubMap<V>) m).new SubMapGenericIterator(Type.Entry);
            boolean removed = false;
            while (it.hasNext()) {
                LongEntry<V> e = it.next();
                if (filter.test(e) && m.remove(e.getLongKey(), e.getValue())) removed = true;
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
        private final long key;

        /** The entry value. */
        private volatile V value;

        /** The next entry. */
        private volatile Node<V> next;

        private Node(long key, V value, Node<V> next) {
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
            public Object create(long key, Object value) {
                return key;
            }

            /**
             * {@inheritDoc}
             */
            @Override
            public Comparator create(LongComparator comparator) {
                return comparator;
            }
        },

        Value(Spliterator.ORDERED | Spliterator.CONCURRENT | Spliterator.NONNULL) {

            /**
             * {@inheritDoc}
             */
            @Override
            public Object create(long key, Object value) {
                return value;
            }

            /**
             * {@inheritDoc}
             */
            @Override
            public Comparator create(LongComparator comparator) {
                return null;
            }
        },

        Entry(Spliterator.DISTINCT | Spliterator.SORTED | Spliterator.ORDERED | Spliterator.CONCURRENT | Spliterator.NONNULL) {

            /**
             * {@inheritDoc}
             */
            @Override
            public Object create(long key, Object value) {
                return LongEntry.immutable(key, value);
            }

            /**
             * {@inheritDoc}
             */
            @Override
            public Comparator<LongEntry> create(LongComparator comparator) {
                return (one, other) -> comparator.compare(one.getLongKey(), other.getLongKey());
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
        public abstract Object create(long key, Object value);

        /**
         * Create specila {@link Comparator} for {@link Spliterator}.
         * 
         * @param comprator
         * @return
         */
        public abstract Comparator create(LongComparator comparator);
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
            long key = node.key;
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
            long key;
            if ((node = lastReturned) == null || (key = node.key) == EMPTY) {
                throw new IllegalStateException();
            }
            // It would not be worth all of the overhead to directly
            // unlink from here. Using remove is fast enough.
            SkipListLongMap.this.remove(key);
            lastReturned = null;
        }
    }
}
