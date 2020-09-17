/*
 * Copyright (C) 2019 Nameless Production Committee
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          http://opensource.org/licenses/mit-license.php
 */
package cointoss.util.primitive.maps;

import static cointoss.util.primitive.Primitives.ensureLong;

import java.util.Collection;
import java.util.Comparator;
import java.util.Iterator;
import java.util.NavigableMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ConcurrentNavigableMap;

/**
 * Sepcialized {@link ConcurrentMap} and {@link NavigableMap} interface for primitive long.
 */
public interface ConcurrentNavigableLongMap<V> extends ConcurrentNavigableMap<Long, V>, ConcurrentLongMap<V>, NavigableLongMap<V> {

    /**
     * {@inheritDoc}
     */
    @Override
    default ConcurrentNavigableLongMap<V> subMap(Long fromKey, boolean fromInclusive, Long toKey, boolean toInclusive) {
        return subMap(ensureLong(fromKey), fromInclusive, ensureLong(toKey), toInclusive);
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
    @Override
    ConcurrentNavigableLongMap<V> subMap(long fromKey, boolean fromInclusive, long toKey, boolean toInclusive);

    /**
     * {@inheritDoc}
     */
    @Override
    default ConcurrentNavigableLongMap<V> headMap(Long toKey, boolean inclusive) {
        return headMap(ensureLong(toKey), inclusive);
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
    @Override
    ConcurrentNavigableLongMap<V> headMap(long toKey, boolean inclusive);

    /**
     * {@inheritDoc}
     */
    @Override
    default ConcurrentNavigableLongMap<V> tailMap(Long fromKey, boolean inclusive) {
        return tailMap(ensureLong(fromKey), inclusive);
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
    @Override
    ConcurrentNavigableLongMap<V> tailMap(long fromKey, boolean inclusive);

    /**
     * {@inheritDoc}
     */
    @Override
    default ConcurrentNavigableLongMap<V> subMap(Long fromKey, Long toKey) {
        return subMap(ensureLong(fromKey), ensureLong(toKey));
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
    @Override
    ConcurrentNavigableLongMap<V> subMap(long fromKey, long toKey);

    /**
     * {@inheritDoc}
     */
    @Override
    default ConcurrentNavigableLongMap<V> headMap(Long toKey) {
        return headMap(ensureLong(toKey));
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
    @Override
    ConcurrentNavigableLongMap<V> headMap(long toKey);

    /**
     * {@inheritDoc}
     */
    @Override
    default ConcurrentNavigableLongMap<V> tailMap(Long fromKey) {
        return tailMap(ensureLong(fromKey));
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
    @Override
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
     * Create the key-set view.
     * 
     * @param <V>
     * @param map
     * @return
     */
    static NavigableLongSet viewKeys(ConcurrentNavigableLongMap<?> map) {
        return new NavigableLongSet() {

            /**
             * {@inheritDoc}
             */
            @Override
            public Iterator<Long> iterator() {
                return null;
            }

            /**
             * {@inheritDoc}
             */
            @Override
            public Iterator<Long> descendingIterator() {
                return null;
            }

            /**
             * {@inheritDoc}
             */
            @Override
            public Comparator<? super Long> comparator() {
                return null;
            }

            /**
             * {@inheritDoc}
             */
            @Override
            public Long first() {
                return null;
            }

            /**
             * {@inheritDoc}
             */
            @Override
            public Long last() {
                return null;
            }

            /**
             * {@inheritDoc}
             */
            @Override
            public int size() {
                return 0;
            }

            /**
             * {@inheritDoc}
             */
            @Override
            public boolean isEmpty() {
                return false;
            }

            /**
             * {@inheritDoc}
             */
            @Override
            public boolean contains(Object o) {
                return false;
            }

            /**
             * {@inheritDoc}
             */
            @Override
            public Object[] toArray() {
                return null;
            }

            /**
             * {@inheritDoc}
             */
            @Override
            public <T> T[] toArray(T[] a) {
                return null;
            }

            /**
             * {@inheritDoc}
             */
            @Override
            public boolean add(Long e) {
                return false;
            }

            /**
             * {@inheritDoc}
             */
            @Override
            public boolean remove(Object o) {
                return false;
            }

            /**
             * {@inheritDoc}
             */
            @Override
            public boolean containsAll(Collection<?> c) {
                return false;
            }

            /**
             * {@inheritDoc}
             */
            @Override
            public boolean addAll(Collection<? extends Long> c) {
                return false;
            }

            /**
             * {@inheritDoc}
             */
            @Override
            public boolean retainAll(Collection<?> c) {
                return false;
            }

            /**
             * {@inheritDoc}
             */
            @Override
            public boolean removeAll(Collection<?> c) {
                return false;
            }

            /**
             * {@inheritDoc}
             */
            @Override
            public void clear() {
            }

            /**
             * {@inheritDoc}
             */
            @Override
            public long lower(long e) {
                return 0;
            }

            /**
             * {@inheritDoc}
             */
            @Override
            public long floor(long e) {
                return 0;
            }

            /**
             * {@inheritDoc}
             */
            @Override
            public long ceiling(long e) {
                return 0;
            }

            /**
             * {@inheritDoc}
             */
            @Override
            public long higher(long e) {
                return 0;
            }

            /**
             * {@inheritDoc}
             */
            @Override
            public long pollFirstLong() {
                return 0;
            }

            /**
             * {@inheritDoc}
             */
            @Override
            public long pollLastLong() {
                return 0;
            }

            /**
             * {@inheritDoc}
             */
            @Override
            public NavigableLongSet descendingSet() {
                return null;
            }

            /**
             * {@inheritDoc}
             */
            @Override
            public NavigableLongSet subSet(long fromElement, boolean fromInclusive, long toElement, boolean toInclusive) {
                return null;
            }

            /**
             * {@inheritDoc}
             */
            @Override
            public NavigableLongSet headSet(long toElement, boolean inclusive) {
                return null;
            }

            /**
             * {@inheritDoc}
             */
            @Override
            public NavigableLongSet tailSet(long fromElement, boolean inclusive) {
                return null;
            }

            /**
             * {@inheritDoc}
             */
            @Override
            public NavigableLongSet subSet(long fromElement, long toElement) {
                return null;
            }

            /**
             * {@inheritDoc}
             */
            @Override
            public NavigableLongSet headSet(long toElement) {
                return null;
            }

            /**
             * {@inheritDoc}
             */
            @Override
            public NavigableLongSet tailSet(long fromElement) {
                return null;
            }
        };
    }
}
