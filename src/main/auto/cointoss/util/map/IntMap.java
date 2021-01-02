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

import java.util.Comparator;
import java.util.Map;
import java.util.Set;

import javax.annotation.processing.Generated;

import com.google.common.base.Objects;


/**
 * Specialized {@link Map} interface for int key.
 */
@Generated("SpecializedCodeGenerator")
public interface IntMap<V> extends Map<Integer, V> {

    /**
     * {@inheritDoc}
     */
    @Override
    default boolean containsKey(Object key) {
        return containsKey((int) key);
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
    boolean containsKey(int key);

    /**
     * {@inheritDoc}
     */
    @Override
    default V get(Object key) {
        return get((int) key);
    }

    /**
     * Returns the value to which the specified key is mapped, or {@code null} if this map contains
     * no mapping for the key.
     * <p>
     * More formally, if this map contains a mapping from a key {@code k} to a value {@code v} such
     * that {@code Objects.equals(key, k)}, then this method returns {@code v}; otherwise it returns
     * {@code null}. (There can be at most one such mapping.)
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
    V get(int key);

    /**
     * {@inheritDoc}
     */
    @Override
    default V put(Integer key, V value) {
        return put((int) key, value);
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
    V put(int key, V value);

    /**
     * {@inheritDoc}
     */
    @Override
    default V remove(Object key) {
        return remove((int) key);
    }

    /**
     * Removes the mapping for a key from this map if it is present (optional operation). More
     * formally, if this map contains a mapping from key {@code k} to value {@code v} such that
     * {@code Objects.equals(key, k)}, that mapping is removed. (The map can contain at most one
     * such mapping.)
     * <p>
     * Returns the value to which this map previously associated the key, or {@code null} if the map
     * contained no mapping for the key.
     * <p>
     * If this map permits null values, then a return value of {@code null} does not
     * <i>necessarily</i> indicate that the map contained no mapping for the key; it's also possible
     * that the map explicitly mapped the key to {@code null}.
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
    V remove(int key);

    /**
     * Returns a {@link Set} view of the mappings contained in this map. The set is backed by the
     * map, so changes to the map are reflected in the set, and vice-versa. If the map is modified
     * while an iteration over the set is in progress (except through the iterator's own
     * {@code remove} operation, or through the {@code setValue} operation on a map entry returned
     * by the iterator) the results of the iteration are undefined. The set supports element
     * removal, which removes the corresponding mapping from the map, via the
     * {@code Iterator.remove}, {@code Set.remove}, {@code removeAll}, {@code retainAll} and
     * {@code clear} operations. It does not support the {@code add} or {@code addAll} operations.
     *
     * @return a set view of the mappings contained in this map
     */
    Set<IntEntry<V>> intEntrySet();

    /**
     * Specialized {@link Entry} for primitive int.
     */
    interface IntEntry<V> extends Map.Entry<Integer, V>, Comparable<IntEntry<V>> {

        /**
         * {@inheritDoc}
         */
        @Override
        default Integer getKey() {
            return getIntKey();
        }

        /**
         * Returns the key corresponding to this entry.
         *
         * @return the key corresponding to this entry
         * @throws IllegalStateException implementations may, but are not required to, throw this
         *             exception if the entry has been removed from the backing map.
         */
        int getIntKey();

        /**
         * {@inheritDoc}
         */
        @Override
        default int compareTo(IntEntry<V> o) {
            return Integer.compare(getIntKey(), o.getIntKey());
        }

        /**
         * Build immutable entry.
         * 
         * @param <V>
         * @param key
         * @param value
         * @return
         */
        static <V> IntEntry<V> immutable(int key, V value) {
            return new IntEntry() {

                /**
                 * {@inheritDoc}
                 */
                @Override
                public int getIntKey() {
                    return key;
                }

                /**
                 * {@inheritDoc}
                 */
                @Override
                public Object getValue() {
                    return value;
                }

                /**
                 * {@inheritDoc}
                 */
                @Override
                public Object setValue(Object value) {
                    throw new UnsupportedOperationException("This is immutable entry.");
                }

                /**
                 * {@inheritDoc}
                 */
                @Override
                public int hashCode() {
                    return Objects.hashCode(key, value);
                }

                /**
                 * {@inheritDoc}
                 */
                @Override
                public boolean equals(Object obj) {
                    if (obj instanceof IntEntry) {
                        IntEntry other = (IntEntry) obj;
                        if (key == other.getIntKey() && Objects.equal(value, other.getValue())) {
                            return true;
                        }
                    }
                    return false;
                }
            };
        }
    }

    /**
     * Specialized {@link Comparator} for primitive value.
     */
    interface IntComparator extends Comparator<Integer> {

        /**
         * Compare values.
         * 
         * @param one A value to compare.
         * @param other A value to compare.
         * @return
         */
        int compare(int one, int other);

        /**
         * {@inheritDoc}
         */
        @Override
        default int compare(Integer one, Integer other) {
            return compare((int) one, (int) other);
        }
    }

    /**
     * Create the concurrent-safe sorted map for primitive int with natual order.
     *
     * @param <V> A value type.
     * @return A new created map.
     */
    public static <V> ConcurrentNavigableIntMap<V> createSortedMap() {
        return new SkipListIntMap(null);
    }

    /**
     * Create the concurrent-safe sorted map for primitive int with your order.
     *
     * @param <V> A value type.
     * @return A new created map.
     */
    public static <V> ConcurrentNavigableIntMap<V> createSortedMap(IntComparator comparator) {
        return new SkipListIntMap(comparator);
    }
}