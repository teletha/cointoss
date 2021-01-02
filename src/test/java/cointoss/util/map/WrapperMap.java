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

import cointoss.util.SpecializedCodeGenerator.Primitive;
import cointoss.util.SpecializedCodeGenerator.Wrapper;

/**
 * Specialized {@link Map} interface for Primitive key.
 */
@Generated("SpecializedCodeGenerator")
public interface WrapperMap<V> extends Map<Wrapper, V> {

    /**
     * {@inheritDoc}
     */
    @Override
    default boolean containsKey(Object key) {
        return containsKey((Primitive) key);
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
    boolean containsKey(Primitive key);

    /**
     * {@inheritDoc}
     */
    @Override
    default V get(Object key) {
        return get((Primitive) key);
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
    V get(Primitive key);

    /**
     * {@inheritDoc}
     */
    @Override
    default V put(Wrapper key, V value) {
        return put((Primitive) key, value);
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
    V put(Primitive key, V value);

    /**
     * {@inheritDoc}
     */
    @Override
    default V remove(Object key) {
        return remove((Primitive) key);
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
    V remove(Primitive key);

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
    Set<WrapperEntry<V>> PrimitiveEntrySet();

    /**
     * Specialized {@link Entry} for primitive Primitive.
     */
    interface WrapperEntry<V> extends Map.Entry<Wrapper, V>, Comparable<WrapperEntry<V>> {

        /**
         * {@inheritDoc}
         */
        @Override
        default Wrapper getKey() {
            return getWrapperKey();
        }

        /**
         * Returns the key corresponding to this entry.
         *
         * @return the key corresponding to this entry
         * @throws IllegalStateException implementations may, but are not required to, throw this
         *             exception if the entry has been removed from the backing map.
         */
        Primitive getWrapperKey();

        /**
         * {@inheritDoc}
         */
        @Override
        default int compareTo(WrapperEntry<V> o) {
            return Wrapper.compare(getWrapperKey(), o.getWrapperKey());
        }

        /**
         * Build immutable entry.
         * 
         * @param <V>
         * @param key
         * @param value
         * @return
         */
        static <V> WrapperEntry<V> immutable(Primitive key, V value) {
            return new WrapperEntry() {

                /**
                 * {@inheritDoc}
                 */
                @Override
                public Primitive getWrapperKey() {
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
                    if (obj instanceof WrapperEntry) {
                        WrapperEntry other = (WrapperEntry) obj;
                        if (key == other.getWrapperKey() && Objects.equal(value, other.getValue())) {
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
    interface WrapperComparator extends Comparator<Wrapper> {

        /**
         * Compare values.
         * 
         * @param one A value to compare.
         * @param other A value to compare.
         * @return
         */
        int compare(Primitive one, Primitive other);

        /**
         * {@inheritDoc}
         */
        @Override
        default int compare(Wrapper one, Wrapper other) {
            return compare((Primitive) one, (Primitive) other);
        }
    }

    /**
     * Create the concurrent-safe sorted map for primitive Primitive with natual order.
     *
     * @param <V> A value type.
     * @return A new created map.
     */
    public static <V> ConcurrentNavigableWrapperMap<V> createSortedMap() {
        return new SkipListWrapperMap(null);
    }

    /**
     * Create the concurrent-safe sorted map for primitive Primitive with your order.
     *
     * @param <V> A value type.
     * @return A new created map.
     */
    public static <V> ConcurrentNavigableWrapperMap<V> createSortedMap(WrapperComparator comparator) {
        return new SkipListWrapperMap(comparator);
    }
}