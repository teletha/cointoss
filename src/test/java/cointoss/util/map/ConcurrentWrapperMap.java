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

import java.util.Objects;
import java.util.concurrent.ConcurrentMap;
import java.util.function.BiFunction;

import javax.annotation.processing.Generated;

import cointoss.util.SpecializedCodeGenerator.Primitive;
import cointoss.util.SpecializedCodeGenerator.Wrapper;
import cointoss.util.SpecializedCodeGenerator.WrapperFunction;

@Generated("SpecializedCodeGenerator")
public interface ConcurrentWrapperMap<V> extends ConcurrentMap<Wrapper, V>, WrapperMap<V> {

    /**
     * {@inheritDoc}
     */
    @Override
    default V getOrDefault(Object key, V defaultValue) {
        return getOrDefault(key, defaultValue);
    }

    /**
     * Returns the value to which the specified key is mapped, or {@code defaultValue} if this map
     * contains no mapping for the key.
     *
     * @implSpec The default implementation makes no guarantees about synchronization or atomicity
     *           properties of this method. Any implementation providing atomicity guarantees must
     *           override this method and document its concurrency properties.
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
    default V getOrDefault(Primitive key, V defaultValue) {
        V value = get(key);
        return value != null ? value : defaultValue;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    default V putIfAbsent(Wrapper key, V value) {
        return putIfAbsent((Primitive) key, value);
    }

    /**
     * If the specified key is not already associated with a value, associates it with the given
     * value. This is equivalent to, for this {@code map}: <pre> {@code
     * if (!map.containsKey(key))
     *   return map.put(key, value);
     * else
     *   return map.get(key);}</pre> except that the action is performed atomically.
     *
     * @implNote This implementation intentionally re-abstracts the inappropriate default provided
     *           in {@code Map}.
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
    V putIfAbsent(Primitive key, V value);

    /**
     * {@inheritDoc}
     */
    @Override
    default boolean remove(Object key, Object value) {
        return remove(key, value);
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
     * }}</pre> except that the action is performed atomically.
     *
     * @implNote This implementation intentionally re-abstracts the inappropriate default provided
     *           in {@code Map}.
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
    boolean remove(Primitive key, Object value);

    /**
     * {@inheritDoc}
     */
    @Override
    default boolean replace(Wrapper key, V oldValue, V newValue) {
        return replace((Primitive) key, oldValue, newValue);
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
     * }}</pre> except that the action is performed atomically.
     *
     * @implNote This implementation intentionally re-abstracts the inappropriate default provided
     *           in {@code Map}.
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
    boolean replace(Primitive key, V oldValue, V newValue);

    /**
     * {@inheritDoc}
     */
    @Override
    default V replace(Wrapper key, V value) {
        return replace((Primitive) key, value);
    }

    /**
     * Replaces the entry for a key only if currently mapped to some value. This is equivalent to,
     * for this {@code map}: <pre> {@code
     * if (map.containsKey(key))
     *   return map.put(key, value);
     * else
     *   return null;}</pre> except that the action is performed atomically.
     *
     * @implNote This implementation intentionally re-abstracts the inappropriate default provided
     *           in {@code Map}.
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
    V replace(Primitive key, V value);

    /**
     * {@inheritDoc}
     *
     * @implSpec The default implementation is equivalent to the following steps for this
     *           {@code map}: <pre> {@code
     * V oldValue, newValue;
     * return ((oldValue = map.get(key)) == null
     *         && (newValue = mappingFunction.apply(key)) != null
     *         && (oldValue = map.putIfAbsent(key, newValue)) == null)
     *   ? newValue
     *   : oldValue;}</pre>
     *           <p>
     *           This implementation assumes that the ConcurrentMap cannot contain null values and
     *           {@code get()} returning null unambiguously means the key is absent. Implementations
     *           which support null values <strong>must</strong> override this default
     *           implementation.
     * @throws UnsupportedOperationException {@inheritDoc}
     * @throws ClassCastException {@inheritDoc}
     * @throws NullPointerException {@inheritDoc}
     * @throws IllegalArgumentException {@inheritDoc}
     */
    default V computeIfAbsent(Primitive key, WrapperFunction<? extends V> mappingFunction) {
        Objects.requireNonNull(mappingFunction);
        V oldValue, newValue;
        return ((oldValue = get(key)) == null && (newValue = mappingFunction
                .apply(key)) != null && (oldValue = putIfAbsent(key, newValue)) == null) ? newValue : oldValue;
    }

    /**
     * {@inheritDoc}
     *
     * @implSpec The default implementation is equivalent to performing the following steps for this
     *           {@code map}: <pre> {@code
     * for (;;) {
     *   V oldValue = map.get(key);
     *   if (oldValue != null) {
     *     V newValue = remappingFunction.apply(oldValue, value);
     *     if (newValue != null) {
     *       if (map.replace(key, oldValue, newValue))
     *         return newValue;
     *     } else if (map.remove(key, oldValue)) {
     *       return null;
     *     }
     *   } else if (map.putIfAbsent(key, value) == null) {
     *     return value;
     *   }
     * }}</pre> When multiple threads attempt updates, map operations and the remapping function
     *           may be called multiple times.
     *           <p>
     *           This implementation assumes that the ConcurrentMap cannot contain null values and
     *           {@code get()} returning null unambiguously means the key is absent. Implementations
     *           which support null values <strong>must</strong> override this default
     *           implementation.
     * @throws UnsupportedOperationException {@inheritDoc}
     * @throws ClassCastException {@inheritDoc}
     * @throws NullPointerException {@inheritDoc}
     * @throws IllegalArgumentException {@inheritDoc}
     */
    default V merge(Primitive key, V value, BiFunction<? super V, ? super V, ? extends V> remappingFunction) {
        Objects.requireNonNull(remappingFunction);
        Objects.requireNonNull(value);
        retry: for (;;) {
            V oldValue = get(key);
            // if putIfAbsent fails, opportunistically use its return value
            haveOldValue: for (;;) {
                if (oldValue != null) {
                    V newValue = remappingFunction.apply(oldValue, value);
                    if (newValue != null) {
                        if (replace(key, oldValue, newValue)) return newValue;
                    } else if (remove(key, oldValue)) {
                        return null;
                    }
                    continue retry;
                } else {
                    if ((oldValue = putIfAbsent(key, value)) == null) return value;
                    continue haveOldValue;
                }
            }
        }
    }
}