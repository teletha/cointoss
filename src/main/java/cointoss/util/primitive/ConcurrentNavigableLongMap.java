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
     * Ensure key type as {@link Long}.
     * 
     * @param key
     * @return
     */
    private long ensureLong(Object key) {
        if (key instanceof Long == false) {
            throw new IllegalArgumentException("Key type must be Long.");
        }
        return ((Long) key).longValue();
    }

    /**
     * Ensure key type as {@link Long}.
     * 
     * @param key
     * @return
     */
    private long ensureLong(Long key) {
        if (key == null) {
            throw new IllegalArgumentException("Key must be Long, this is null.");
        }
        return key.longValue();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    default boolean containsKey(Object key) {
        return containsKey(ensureLong(key));
    }

    /**
     * Returns {@code true} if this map contains a mapping for the specified key. More formally,
     * returns {@code true} if and only if this map contains a mapping for a key {@code k} such that
     * {@code Objects.equals(key, k)}. (There can be at most one such mapping.)
     *
     * @param key key whose presence in this map is to be tested
     * @return {@code true} if this map contains a mapping for the specified key
     * @throws ClassCastException if the key is of an inappropriate type for this map (<a href=
     *             "{@docRoot}/java.base/java/util/Collection.html#optional-restrictions">optional</a>)
     * @throws NullPointerException if the specified key is null and this map does not permit null
     *             keys (<a href=
     *             "{@docRoot}/java.base/java/util/Collection.html#optional-restrictions">optional</a>)
     */
    boolean containsKey(long key);

    /**
     * {@inheritDoc}
     */
    @Override
    default V get(Object key) {
        return get(ensureLong(key));
    }

    /**
     * Returns the value to which the specified key is mapped, or {@code null} if this map contains
     * no mapping for the key.
     *
     * <p>
     * More formally, if this map contains a mapping from a key {@code k} to a value {@code v} such
     * that {@code Objects.equals(key, k)}, then this method returns {@code v}; otherwise it returns
     * {@code null}. (There can be at most one such mapping.)
     *
     * <p>
     * If this map permits null values, then a return value of {@code null} does not
     * <i>necessarily</i> indicate that the map contains no mapping for the key; it's also possible
     * that the map explicitly maps the key to {@code null}. The {@link #containsKey containsKey}
     * operation may be used to distinguish these two cases.
     *
     * @param key the key whose associated value is to be returned
     * @return the value to which the specified key is mapped, or {@code null} if this map contains
     *         no mapping for the key
     * @throws ClassCastException if the key is of an inappropriate type for this map (<a href=
     *             "{@docRoot}/java.base/java/util/Collection.html#optional-restrictions">optional</a>)
     * @throws NullPointerException if the specified key is null and this map does not permit null
     *             keys (<a href=
     *             "{@docRoot}/java.base/java/util/Collection.html#optional-restrictions">optional</a>)
     */
    V get(long key);

    /**
     * {@inheritDoc}
     */
    @Override
    default V getOrDefault(Object key, V defaultValue) {
        return getOrDefault(ensureLong(key), defaultValue);
    }

    /**
     * Returns the value to which the specified key is mapped, or {@code defaultValue} if this map
     * contains no mapping for the key.
     *
     * @implSpec The default implementation makes no guarantees about synchronization or atomicity
     *           properties of this method. Any implementation providing atomicity guarantees must
     *           override this method and document its concurrency properties.
     *
     * @param key the key whose associated value is to be returned
     * @param defaultValue the default mapping of the key
     * @return the value to which the specified key is mapped, or {@code defaultValue} if this map
     *         contains no mapping for the key
     * @throws ClassCastException if the key is of an inappropriate type for this map (<a href=
     *             "{@docRoot}/java.base/java/util/Collection.html#optional-restrictions">optional</a>)
     * @throws NullPointerException if the specified key is null and this map does not permit null
     *             keys (<a href=
     *             "{@docRoot}/java.base/java/util/Collection.html#optional-restrictions">optional</a>)
     */
    default V getOrDefault(long key, V defaultValue) {
        V value = get(key);
        return value != null ? value : defaultValue;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    default V put(Long key, V value) {
        return put(ensureLong(key), value);
    }

    /**
     * Associates the specified value with the specified key in this map (optional operation). If
     * the map previously contained a mapping for the key, the old value is replaced by the
     * specified value. (A map {@code m} is said to contain a mapping for a key {@code k} if and
     * only if {@link #containsKey(Object) m.containsKey(k)} would return {@code true}.)
     *
     * @param key key with which the specified value is to be associated
     * @param value value to be associated with the specified key
     * @return the previous value associated with {@code key}, or {@code null} if there was no
     *         mapping for {@code key}. (A {@code null} return can also indicate that the map
     *         previously associated {@code null} with {@code key}, if the implementation supports
     *         {@code null} values.)
     * @throws UnsupportedOperationException if the {@code put} operation is not supported by this
     *             map
     * @throws ClassCastException if the class of the specified key or value prevents it from being
     *             stored in this map
     * @throws NullPointerException if the specified key or value is null and this map does not
     *             permit null keys or values
     * @throws IllegalArgumentException if some property of the specified key or value prevents it
     *             from being stored in this map
     */
    V put(long key, V value);

    /**
     * {@inheritDoc}
     */
    @Override
    default V putIfAbsent(Long key, V value) {
        return putIfAbsent(ensureLong(key), value);
    }

    /**
     * If the specified key is not already associated with a value, associates it with the given
     * value. This is equivalent to, for this {@code map}: <pre> {@code
     * if (!map.containsKey(key))
     *   return map.put(key, value);
     * else
     *   return map.get(key);}</pre>
     *
     * except that the action is performed atomically.
     *
     * @implNote This implementation intentionally re-abstracts the inappropriate default provided
     *           in {@code Map}.
     *
     * @param key key with which the specified value is to be associated
     * @param value value to be associated with the specified key
     * @return the previous value associated with the specified key, or {@code null} if there was no
     *         mapping for the key. (A {@code null} return can also indicate that the map previously
     *         associated {@code null} with the key, if the implementation supports null values.)
     * @throws UnsupportedOperationException if the {@code put} operation is not supported by this
     *             map
     * @throws ClassCastException if the class of the specified key or value prevents it from being
     *             stored in this map
     * @throws NullPointerException if the specified key or value is null, and this map does not
     *             permit null keys or values
     * @throws IllegalArgumentException if some property of the specified key or value prevents it
     *             from being stored in this map
     */
    V putIfAbsent(long key, V value);

    /**
     * {@inheritDoc}
     */
    @Override
    default V remove(Object key) {
        return remove(ensureLong(key));
    }

    /**
     * Removes the mapping for a key from this map if it is present (optional operation). More
     * formally, if this map contains a mapping from key {@code k} to value {@code v} such that
     * {@code Objects.equals(key, k)}, that mapping is removed. (The map can contain at most one
     * such mapping.)
     *
     * <p>
     * Returns the value to which this map previously associated the key, or {@code null} if the map
     * contained no mapping for the key.
     *
     * <p>
     * If this map permits null values, then a return value of {@code null} does not
     * <i>necessarily</i> indicate that the map contained no mapping for the key; it's also possible
     * that the map explicitly mapped the key to {@code null}.
     *
     * <p>
     * The map will not contain a mapping for the specified key once the call returns.
     *
     * @param key key whose mapping is to be removed from the map
     * @return the previous value associated with {@code key}, or {@code null} if there was no
     *         mapping for {@code key}.
     * @throws UnsupportedOperationException if the {@code remove} operation is not supported by
     *             this map
     * @throws ClassCastException if the key is of an inappropriate type for this map (<a href=
     *             "{@docRoot}/java.base/java/util/Collection.html#optional-restrictions">optional</a>)
     * @throws NullPointerException if the specified key is null and this map does not permit null
     *             keys (<a href=
     *             "{@docRoot}/java.base/java/util/Collection.html#optional-restrictions">optional</a>)
     */
    V remove(long key);

    /**
     * {@inheritDoc}
     */
    @Override
    default boolean remove(Object key, Object value) {
        return remove(ensureLong(key), value);
    }

    /**
     * Removes the entry for a key only if currently mapped to a given value. This is equivalent to,
     * for this {@code map}: <pre> {@code
     * if (map.containsKey(key)
     *     && Objects.equals(map.get(key), value)) {
     *   map.remove(key);
     *   return true;
     * } else {
     *   return false;
     * }}</pre>
     *
     * except that the action is performed atomically.
     *
     * @implNote This implementation intentionally re-abstracts the inappropriate default provided
     *           in {@code Map}.
     *
     * @param key key with which the specified value is associated
     * @param value value expected to be associated with the specified key
     * @return {@code true} if the value was removed
     * @throws UnsupportedOperationException if the {@code remove} operation is not supported by
     *             this map
     * @throws ClassCastException if the key or value is of an inappropriate type for this map
     *             (<a href=
     *             "{@docRoot}/java.base/java/util/Collection.html#optional-restrictions">optional</a>)
     * @throws NullPointerException if the specified key or value is null, and this map does not
     *             permit null keys or values (<a href=
     *             "{@docRoot}/java.base/java/util/Collection.html#optional-restrictions">optional</a>)
     */
    boolean remove(long key, Object value);

    /**
     * {@inheritDoc}
     */
    @Override
    default boolean replace(Long key, V oldValue, V newValue) {
        return replace(ensureLong(key), oldValue, newValue);
    }

    /**
     * Replaces the entry for a key only if currently mapped to a given value. This is equivalent
     * to, for this {@code map}: <pre> {@code
     * if (map.containsKey(key)
     *     && Objects.equals(map.get(key), oldValue)) {
     *   map.put(key, newValue);
     *   return true;
     * } else {
     *   return false;
     * }}</pre>
     *
     * except that the action is performed atomically.
     *
     * @implNote This implementation intentionally re-abstracts the inappropriate default provided
     *           in {@code Map}.
     *
     * @param key key with which the specified value is associated
     * @param oldValue value expected to be associated with the specified key
     * @param newValue value to be associated with the specified key
     * @return {@code true} if the value was replaced
     * @throws UnsupportedOperationException if the {@code put} operation is not supported by this
     *             map
     * @throws ClassCastException if the class of a specified key or value prevents it from being
     *             stored in this map
     * @throws NullPointerException if a specified key or value is null, and this map does not
     *             permit null keys or values
     * @throws IllegalArgumentException if some property of a specified key or value prevents it
     *             from being stored in this map
     */
    boolean replace(long key, V oldValue, V newValue);

    /**
     * {@inheritDoc}
     */
    @Override
    default V replace(Long key, V value) {
        return replace(ensureLong(key), value);
    }

    /**
     * Replaces the entry for a key only if currently mapped to some value. This is equivalent to,
     * for this {@code map}: <pre> {@code
     * if (map.containsKey(key))
     *   return map.put(key, value);
     * else
     *   return null;}</pre>
     *
     * except that the action is performed atomically.
     *
     * @implNote This implementation intentionally re-abstracts the inappropriate default provided
     *           in {@code Map}.
     *
     * @param key key with which the specified value is associated
     * @param value value to be associated with the specified key
     * @return the previous value associated with the specified key, or {@code null} if there was no
     *         mapping for the key. (A {@code null} return can also indicate that the map previously
     *         associated {@code null} with the key, if the implementation supports null values.)
     * @throws UnsupportedOperationException if the {@code put} operation is not supported by this
     *             map
     * @throws ClassCastException if the class of the specified key or value prevents it from being
     *             stored in this map
     * @throws NullPointerException if the specified key or value is null, and this map does not
     *             permit null keys or values
     * @throws IllegalArgumentException if some property of the specified key or value prevents it
     *             from being stored in this map
     */
    V replace(long key, V value);

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
