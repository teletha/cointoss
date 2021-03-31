/*
 * Copyright (C) 2021 cointoss Development Team
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          https://opensource.org/licenses/MIT
 */
package cointoss.util.map;

import java.util.Collections;
import java.util.Comparator;
import java.util.NavigableMap;
import java.util.NavigableSet;
import java.util.NoSuchElementException;

import javax.annotation.processing.Generated;

import cointoss.util.map.LongMap.LongEntry;
import cointoss.util.set.NavigableLongSet;

@Generated("SpecializedCodeGenerator")
public interface NavigableLongMap<V> extends NavigableMap<Long, V> {

    /**
     * {@inheritDoc}
     */
    @Override
    default Entry<Long, V> lowerEntry(Long key) {
        return lowerEntry((long) key);
    }

    /**
     * Returns a key-value mapping associated with the greatest key strictly less than the given
     * key, or {@code null} if there is no such key.
     *
     * @param key the key
     * @return an entry with the greatest key less than {@code key}, or {@code null} if there is no
     *         such key
     * @throws ClassCastException if the specified key cannot be compared with the keys currently in
     *             the map
     * @throws NullPointerException if the specified key is null and this map does not permit null
     *             keys
     */
    LongEntry<V> lowerEntry(long key);

    /**
     * {@inheritDoc}
     */
    @Override
    default Long lowerKey(Long key) {
        return lowerKey((long) key);
    }

    /**
     * Returns the greatest key strictly less than the given key, or {@code null} if there is no
     * such key.
     *
     * @param key the key
     * @return the greatest key less than {@code key}, or {@code null} if there is no such key
     * @throws ClassCastException if the specified key cannot be compared with the keys currently in
     *             the map
     * @throws NullPointerException if the specified key is null and this map does not permit null
     *             keys
     */
    long lowerKey(long key);

    /**
     * {@inheritDoc}
     */
    @Override
    default Entry<Long, V> floorEntry(Long key) {
        return floorEntry((long) key);
    }

    /**
     * Returns a key-value mapping associated with the greatest key less than or equal to the given
     * key, or {@code null} if there is no such key.
     *
     * @param key the key
     * @return an entry with the greatest key less than or equal to {@code key}, or {@code null} if
     *         there is no such key
     * @throws ClassCastException if the specified key cannot be compared with the keys currently in
     *             the map
     * @throws NullPointerException if the specified key is null and this map does not permit null
     *             keys
     */
    LongEntry<V> floorEntry(long key);

    /**
     * {@inheritDoc}
     */
    @Override
    default Long floorKey(Long key) {
        return floorKey((long) key);
    }

    /**
     * Returns the greatest key less than or equal to the given key, or {@code null} if there is no
     * such key.
     *
     * @param key the key
     * @return the greatest key less than or equal to {@code key}, or {@code null} if there is no
     *         such key
     * @throws ClassCastException if the specified key cannot be compared with the keys currently in
     *             the map
     * @throws NullPointerException if the specified key is null and this map does not permit null
     *             keys
     */
    long floorKey(long key);

    /**
     * {@inheritDoc}
     */
    @Override
    default Entry<Long, V> ceilingEntry(Long key) {
        return ceilingEntry((long) key);
    }

    /**
     * Returns a key-value mapping associated with the least key greater than or equal to the given
     * key, or {@code null} if there is no such key.
     *
     * @param key the key
     * @return an entry with the least key greater than or equal to {@code key}, or {@code null} if
     *         there is no such key
     * @throws ClassCastException if the specified key cannot be compared with the keys currently in
     *             the map
     * @throws NullPointerException if the specified key is null and this map does not permit null
     *             keys
     */
    LongEntry<V> ceilingEntry(long key);

    /**
     * {@inheritDoc}
     */
    @Override
    default Long ceilingKey(Long key) {
        return ceilingKey((long) key);
    }

    /**
     * Returns the least key greater than or equal to the given key, or {@code null} if there is no
     * such key.
     *
     * @param key the key
     * @return the least key greater than or equal to {@code key}, or {@code null} if there is no
     *         such key
     * @throws ClassCastException if the specified key cannot be compared with the keys currently in
     *             the map
     * @throws NullPointerException if the specified key is null and this map does not permit null
     *             keys
     */
    long ceilingKey(long key);

    /**
     * {@inheritDoc}
     */
    @Override
    default Entry<Long, V> higherEntry(Long key) {
        return higherEntry((long) key);
    }

    /**
     * Returns a key-value mapping associated with the least key strictly greater than the given
     * key, or {@code null} if there is no such key.
     *
     * @param key the key
     * @return an entry with the least key greater than {@code key}, or {@code null} if there is no
     *         such key
     * @throws ClassCastException if the specified key cannot be compared with the keys currently in
     *             the map
     * @throws NullPointerException if the specified key is null and this map does not permit null
     *             keys
     */
    LongEntry<V> higherEntry(long key);

    /**
     * {@inheritDoc}
     */
    @Override
    default Long higherKey(Long key) {
        return higherKey((long) key);
    }

    /**
     * Returns the least key strictly greater than the given key, or {@code null} if there is no
     * such key.
     *
     * @param key the key
     * @return the least key greater than {@code key}, or {@code null} if there is no such key
     * @throws ClassCastException if the specified key cannot be compared with the keys currently in
     *             the map
     * @throws NullPointerException if the specified key is null and this map does not permit null
     *             keys
     */
    long higherKey(long key);

    /**
     * {@inheritDoc}
     */
    @Override
    default Long firstKey() {
        return firstLongKey();
    }

    /**
     * Returns the first (lowest) key currently in this map.
     *
     * @return the first (lowest) key currently in this map
     * @throws NoSuchElementException if this map is empty
     */
    long firstLongKey();

    /**
     * Returns a value mapping associated with the least key in this map, or {@code null} if the map
     * is empty.
     *
     * @return A value with the least key, or {@code null} if this map is empty
     */
    default V firstValue() {
        LongEntry<V> entry = firstEntry();
        return entry == null ? null : entry.getValue();
    }

    /**
     * Returns a key-value mapping associated with the least key in this map, or {@code null} if the
     * map is empty.
     *
     * @return an entry with the least key, or {@code null} if this map is empty
     */
    @Override
    LongEntry<V> firstEntry();

    /**
     * {@inheritDoc}
     */
    @Override
    default Long lastKey() {
        return lastLongKey();
    }

    /**
     * Returns the last (highest) key currently in this map.
     *
     * @return the last (highest) key currently in this map
     * @throws NoSuchElementException if this map is empty
     */
    long lastLongKey();

    /**
     * Returns a value mapping associated with the greatest key in this map, or {@code null} if the
     * map is empty.
     *
     * @return A value with the greatest key, or {@code null} if this map is empty
     */
    default V lastValue() {
        LongEntry<V> entry = lastEntry();
        return entry == null ? null : entry.getValue();
    }

    /**
     * Returns a key-value mapping associated with the greatest key in this map, or {@code null} if
     * the map is empty.
     *
     * @return an entry with the greatest key, or {@code null} if this map is empty
     */
    @Override
    LongEntry<V> lastEntry();

    /**
     * Removes and returns a key-value mapping associated with the least key in this map, or
     * {@code null} if the map is empty.
     *
     * @return the removed first entry of this map, or {@code null} if this map is empty
     */
    @Override
    LongEntry<V> pollFirstEntry();

    /**
     * Removes and returns a key-value mapping associated with the greatest key in this map, or
     * {@code null} if the map is empty.
     *
     * @return the removed last entry of this map, or {@code null} if this map is empty
     */
    @Override
    LongEntry<V> pollLastEntry();

    /**
     * Returns a reverse order view of the mappings contained in this map. The descending map is
     * backed by this map, so changes to the map are reflected in the descending map, and
     * vice-versa. If either map is modified while an iteration over a collection view of either map
     * is in progress (except through the iterator's own {@code remove} operation), the results of
     * the iteration are undefined.
     * <p>
     * The returned map has an ordering equivalent to {@link Collections#reverseOrder(Comparator)
     * Collections.reverseOrder}{@code (comparator())}. The expression
     * {@code m.descendingMap().descendingMap()} returns a view of {@code m} essentially equivalent
     * to {@code m}.
     *
     * @return a reverse order view of this map
     */
    @Override
    NavigableLongMap<V> descendingMap();

    /**
     * Returns a {@link NavigableSet} view of the keys contained in this map. The set's iterator
     * returns the keys in ascending order. The set is backed by the map, so changes to the map are
     * reflected in the set, and vice-versa. If the map is modified while an iteration over the set
     * is in progress (except through the iterator's own {@code
     * remove} operation), the results of the iteration are undefined. The set supports element
     * removal, which removes the corresponding mapping from the map, via the
     * {@code Iterator.remove}, {@code Set.remove}, {@code removeAll}, {@code retainAll}, and
     * {@code clear} operations. It does not support the {@code add} or {@code addAll} operations.
     *
     * @return a navigable set view of the keys in this map
     */
    @Override
    NavigableLongSet navigableKeySet();

    /**
     * Returns a reverse order {@link NavigableSet} view of the keys contained in this map. The
     * set's iterator returns the keys in descending order. The set is backed by the map, so changes
     * to the map are reflected in the set, and vice-versa. If the map is modified while an
     * iteration over the set is in progress (except through the iterator's own {@code
     * remove} operation), the results of the iteration are undefined. The set supports element
     * removal, which removes the corresponding mapping from the map, via the
     * {@code Iterator.remove}, {@code Set.remove}, {@code removeAll}, {@code retainAll}, and
     * {@code clear} operations. It does not support the {@code add} or {@code addAll} operations.
     *
     * @return a reverse order navigable set view of the keys in this map
     */
    @Override
    NavigableLongSet descendingKeySet();

    /**
     * {@inheritDoc}
     */
    @Override
    default NavigableMap<Long, V> subMap(Long fromKey, boolean fromInclusive, Long toKey, boolean toInclusive) {
        return subMap((long) fromKey, fromInclusive, (long) toKey, toInclusive);
    }

    /**
     * Returns a view of the portion of this map whose keys range from {@code fromKey} to
     * {@code toKey}. If {@code fromKey} and {@code toKey} are equal, the returned map is empty
     * unless {@code fromInclusive} and {@code toInclusive} are both true. The returned map is
     * backed by this map, so changes in the returned map are reflected in this map, and vice-versa.
     * The returned map supports all optional map operations that this map supports.
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
    NavigableLongMap<V> subMap(long fromKey, boolean fromInclusive, long toKey, boolean toInclusive);

    /**
     * {@inheritDoc}
     */
    @Override
    default NavigableLongMap<V> headMap(Long toKey, boolean inclusive) {
        return headMap((long) toKey, inclusive);
    }

    /**
     * Returns a view of the portion of this map whose keys are less than (or equal to, if
     * {@code inclusive} is true) {@code toKey}. The returned map is backed by this map, so changes
     * in the returned map are reflected in this map, and vice-versa. The returned map supports all
     * optional map operations that this map supports.
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
    NavigableLongMap<V> headMap(long toKey, boolean inclusive);

    /**
     * {@inheritDoc}
     */
    @Override
    default NavigableLongMap<V> tailMap(Long fromKey, boolean inclusive) {
        return tailMap((long) fromKey, inclusive);
    }

    /**
     * Returns a view of the portion of this map whose keys are greater than (or equal to, if
     * {@code inclusive} is true) {@code fromKey}. The returned map is backed by this map, so
     * changes in the returned map are reflected in this map, and vice-versa. The returned map
     * supports all optional map operations that this map supports.
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
    NavigableLongMap<V> tailMap(long fromKey, boolean inclusive);

    /**
     * {@inheritDoc}
     */
    @Override
    default NavigableLongMap<V> subMap(Long fromKey, Long toKey) {
        return subMap((long) fromKey, (long) toKey);
    }

    /**
     * Equivalent to {@code subMap(fromKey, true, toKey, false)}.
     */
    NavigableLongMap<V> subMap(long fromKey, long toKey);

    /**
     * {@inheritDoc}
     */
    @Override
    default NavigableLongMap<V> headMap(Long toKey) {
        return headMap((long) toKey);
    }

    /**
     * Equivalent to {@code headMap(toKey, false)}.
     */
    NavigableLongMap<V> headMap(long toKey);

    /**
     * {@inheritDoc}
     */
    @Override
    default NavigableLongMap<V> tailMap(Long fromKey) {
        return tailMap((long) fromKey);
    }

    /**
     * Equivalent to {@code tailMap(fromKey, true)}.
     */
    NavigableLongMap<V> tailMap(long fromKey);
}