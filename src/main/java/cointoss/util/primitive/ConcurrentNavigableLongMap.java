/*
 * Copyright (C) 2019 Nameless Production Committee
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          http://opensource.org/licenses/mit-license.php
 */
package cointoss.util.primitive;

import java.util.NavigableSet;
import java.util.Objects;
import java.util.concurrent.ConcurrentNavigableMap;

public interface ConcurrentNavigableLongMap<V> extends ConcurrentNavigableMap<Long, V> {

    /**
     * {@inheritDoc}
     */
    @Override
    default ConcurrentNavigableMap<Long, V> subMap(Long fromKey, boolean fromInclusive, Long toKey, boolean toInclusive) {
        Objects.requireNonNull(fromKey);
        Objects.requireNonNull(toKey);

        return subMap(fromKey.longValue(), fromInclusive, toKey.longValue(), toInclusive);
    }

    /**
     * Returns a view of the portion of this map whose keys range from {@code fromKey} to
     * {@code toKey}. If {@code fromKey} and {@code toKey} are equal, the returned map is empty
     * unless {@code fromInclusive} and {@code toInclusive} are both true. The returned map is
     * backed by this map, so changes in the returned map are reflected in this map, and vice-versa.
     * The returned map supports all optional map operations that this map supports.
     *
     * <p>
     * The returned map will throw an {@code IllegalArgumentException} on an attempt to insert a key
     * outside of its range, or to construct a submap either of whose endpoints lie outside its
     * range.
     *
     * @param fromKey low endpoint of the keys in the returned map
     * @param fromInclusive {@code true} if the low endpoint is to be included in the returned view
     * @param toKey high endpoint of the keys in the returned map
     * @param toInclusive {@code true} if the high endpoint is to be included in the returned view
     * @return a view of the portion of this map whose keys range from {@code fromKey} to
     *         {@code toKey}
     * @throws ClassCastException if {@code fromKey} and {@code toKey} cannot be compared to one
     *             another using this map's comparator (or, if the map has no comparator, using
     *             natural ordering). Implementations may, but are not required to, throw this
     *             exception if {@code fromKey} or {@code toKey} cannot be compared to keys
     *             currently in the map.
     * @throws NullPointerException if {@code fromKey} or {@code toKey} is null and this map does
     *             not permit null keys
     * @throws IllegalArgumentException if {@code fromKey} is greater than {@code toKey}; or if this
     *             map itself has a restricted range, and {@code fromKey} or {@code toKey} lies
     *             outside the bounds of the range
     */
    ConcurrentNavigableLongMap<V> subMap(long fromKey, boolean fromInclusive, long toKey, boolean toInclusive);

    /**
     * {@inheritDoc}
     */
    @Override
    default ConcurrentNavigableMap<Long, V> headMap(Long toKey, boolean inclusive) {
        Objects.requireNonNull(toKey);

        return headMap(toKey.longValue(), inclusive);
    }

    /**
     * Returns a view of the portion of this map whose keys are less than (or equal to, if
     * {@code inclusive} is true) {@code toKey}. The returned map is backed by this map, so changes
     * in the returned map are reflected in this map, and vice-versa. The returned map supports all
     * optional map operations that this map supports.
     *
     * <p>
     * The returned map will throw an {@code IllegalArgumentException} on an attempt to insert a key
     * outside its range.
     *
     * @param toKey high endpoint of the keys in the returned map
     * @param inclusive {@code true} if the high endpoint is to be included in the returned view
     * @return a view of the portion of this map whose keys are less than (or equal to, if
     *         {@code inclusive} is true) {@code toKey}
     * @throws ClassCastException if {@code toKey} is not compatible with this map's comparator (or,
     *             if the map has no comparator, if {@code toKey} does not implement
     *             {@link Comparable}). Implementations may, but are not required to, throw this
     *             exception if {@code toKey} cannot be compared to keys currently in the map.
     * @throws NullPointerException if {@code toKey} is null and this map does not permit null keys
     * @throws IllegalArgumentException if this map itself has a restricted range, and {@code toKey}
     *             lies outside the bounds of the range
     */
    ConcurrentNavigableLongMap<V> headMap(long toKey, boolean inclusive);

    /**
     * {@inheritDoc}
     */
    @Override
    default ConcurrentNavigableMap<Long, V> tailMap(Long fromKey, boolean inclusive) {
        Objects.requireNonNull(fromKey);

        return tailMap(fromKey.longValue(), inclusive);
    }

    /**
     * Returns a view of the portion of this map whose keys are greater than (or equal to, if
     * {@code inclusive} is true) {@code fromKey}. The returned map is backed by this map, so
     * changes in the returned map are reflected in this map, and vice-versa. The returned map
     * supports all optional map operations that this map supports.
     *
     * <p>
     * The returned map will throw an {@code IllegalArgumentException} on an attempt to insert a key
     * outside its range.
     *
     * @param fromKey low endpoint of the keys in the returned map
     * @param inclusive {@code true} if the low endpoint is to be included in the returned view
     * @return a view of the portion of this map whose keys are greater than (or equal to, if
     *         {@code inclusive} is true) {@code fromKey}
     * @throws ClassCastException if {@code fromKey} is not compatible with this map's comparator
     *             (or, if the map has no comparator, if {@code fromKey} does not implement
     *             {@link Comparable}). Implementations may, but are not required to, throw this
     *             exception if {@code fromKey} cannot be compared to keys currently in the map.
     * @throws NullPointerException if {@code fromKey} is null and this map does not permit null
     *             keys
     * @throws IllegalArgumentException if this map itself has a restricted range, and
     *             {@code fromKey} lies outside the bounds of the range
     */
    ConcurrentNavigableLongMap<V> tailMap(long fromKey, boolean inclusive);

    /**
     * {@inheritDoc}
     */
    @Override
    default ConcurrentNavigableMap<Long, V> subMap(Long fromKey, Long toKey) {
        Objects.requireNonNull(fromKey);
        Objects.requireNonNull(toKey);

        return subMap(fromKey.longValue(), toKey.longValue());
    }

    /**
     * {@inheritDoc}
     *
     * <p>
     * Equivalent to {@code subMap(fromKey, true, toKey, false)}.
     *
     * @throws ClassCastException {@inheritDoc}
     * @throws NullPointerException {@inheritDoc}
     * @throws IllegalArgumentException {@inheritDoc}
     */
    ConcurrentNavigableLongMap<V> subMap(long fromKey, long toKey);

    /**
     * {@inheritDoc}
     */
    @Override
    default ConcurrentNavigableMap<Long, V> headMap(Long toKey) {
        Objects.requireNonNull(toKey);

        return headMap(toKey.longValue());
    }

    /**
     * {@inheritDoc}
     *
     * <p>
     * Equivalent to {@code headMap(toKey, false)}.
     *
     * @throws ClassCastException {@inheritDoc}
     * @throws NullPointerException {@inheritDoc}
     * @throws IllegalArgumentException {@inheritDoc}
     */
    ConcurrentNavigableLongMap<V> headMap(long toKey);

    /**
     * {@inheritDoc}
     */
    @Override
    default ConcurrentNavigableMap<Long, V> tailMap(Long fromKey) {
        Objects.requireNonNull(fromKey);

        return tailMap(fromKey.longValue());
    }

    /**
     * {@inheritDoc}
     *
     * <p>
     * Equivalent to {@code tailMap(fromKey, true)}.
     *
     * @throws ClassCastException {@inheritDoc}
     * @throws NullPointerException {@inheritDoc}
     * @throws IllegalArgumentException {@inheritDoc}
     */
    ConcurrentNavigableLongMap<V> tailMap(long fromKey);

    /**
     * Returns a reverse order view of the mappings contained in this map. The descending map is
     * backed by this map, so changes to the map are reflected in the descending map, and
     * vice-versa.
     *
     * <p>
     * The returned map has an ordering equivalent to
     * {@link java.util.Collections#reverseOrder(Comparator)
     * Collections.reverseOrder}{@code (comparator())}. The expression
     * {@code m.descendingMap().descendingMap()} returns a view of {@code m} essentially equivalent
     * to {@code m}.
     *
     * @return a reverse order view of this map
     */
    @Override
    ConcurrentNavigableLongMap<V> descendingMap();

    /**
     * Returns a {@link NavigableSet} view of the keys contained in this map. The set's iterator
     * returns the keys in ascending order. The set is backed by the map, so changes to the map are
     * reflected in the set, and vice-versa. The set supports element removal, which removes the
     * corresponding mapping from the map, via the {@code Iterator.remove}, {@code Set.remove},
     * {@code removeAll}, {@code retainAll}, and {@code clear} operations. It does not support the
     * {@code add} or {@code addAll} operations.
     *
     * <p>
     * The view's iterators and spliterators are <a href="package-summary.html#Weakly"><i>weakly
     * consistent</i></a>.
     *
     * @return a navigable set view of the keys in this map
     */
    @Override
    NavigableSet<Long> navigableKeySet();

    /**
     * Returns a {@link NavigableSet} view of the keys contained in this map. The set's iterator
     * returns the keys in ascending order. The set is backed by the map, so changes to the map are
     * reflected in the set, and vice-versa. The set supports element removal, which removes the
     * corresponding mapping from the map, via the {@code Iterator.remove}, {@code Set.remove},
     * {@code removeAll}, {@code retainAll}, and {@code clear} operations. It does not support the
     * {@code add} or {@code addAll} operations.
     *
     * <p>
     * The view's iterators and spliterators are <a href="package-summary.html#Weakly"><i>weakly
     * consistent</i></a>.
     *
     * <p>
     * This method is equivalent to method {@code navigableKeySet}.
     *
     * @return a navigable set view of the keys in this map
     */
    @Override
    NavigableSet<Long> keySet();

    /**
     * Returns a reverse order {@link NavigableSet} view of the keys contained in this map. The
     * set's iterator returns the keys in descending order. The set is backed by the map, so changes
     * to the map are reflected in the set, and vice-versa. The set supports element removal, which
     * removes the corresponding mapping from the map, via the {@code Iterator.remove},
     * {@code Set.remove}, {@code removeAll}, {@code retainAll}, and {@code clear} operations. It
     * does not support the {@code add} or {@code addAll} operations.
     *
     * <p>
     * The view's iterators and spliterators are <a href="package-summary.html#Weakly"><i>weakly
     * consistent</i></a>.
     *
     * @return a reverse order navigable set view of the keys in this map
     */
    @Override
    NavigableSet<Long> descendingKeySet();
}
