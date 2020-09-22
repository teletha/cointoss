/*
 * Copyright (C) 2020 Nameless Production Committee
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          http://opensource.org/licenses/mit-license.php
 */
package cointoss.util.set;

import java.util.Collection;
import java.util.Set;

import kiss.I;

public interface IntSet extends Set<Integer> {

    /**
     * {@inheritDoc}
     */
    @Override
    default boolean add(Integer e) {
        return add((int) e);
    }

    /**
     * Adds the specified element to this set if it is not already present (optional operation).
     * More formally, adds the specified element {@code e} to this set if the set contains no
     * element {@code e2} such that {@code Objects.equals(e, e2)}. If this set already contains the
     * element, the call leaves the set unchanged and returns {@code false}. In combination with the
     * restriction on constructors, this ensures that sets never contain duplicate elements.
     * <p>
     * The stipulation above does not imply that sets must accept all elements; sets may refuse to
     * add any particular element, including {@code null}, and throw an exception, as described in
     * the specification for {@link Collection#add Collection.add}. Individual set implementations
     * should clearly document any restrictions on the elements that they may contain.
     *
     * @param value element to be added to this set
     * @return {@code true} if this set did not already contain the specified element
     * @throws UnsupportedOperationException if the {@code add} operation is not supported by this
     *             set
     * @throws ClassCastException if the class of the specified element prevents it from being added
     *             to this set
     * @throws NullPointerException if the specified element is null and this set does not permit
     *             null elements
     * @throws IllegalArgumentException if some property of the specified element prevents it from
     *             being added to this set
     */
    boolean add(int value);

    /**
     * {@inheritDoc}
     */
    @Override
    default boolean remove(Object o) {
        return remove((int) o);
    }

    /**
     * Removes the specified element from this set if it is present (optional operation). More
     * formally, removes an element {@code e} such that {@code Objects.equals(o, e)}, if this set
     * contains such an element. Returns {@code true} if this set contained the element (or
     * equivalently, if this set changed as a result of the call). (This set will not contain the
     * element once the call returns.)
     *
     * @param value object to be removed from this set, if present
     * @return {@code true} if this set contained the specified element
     * @throws ClassCastException if the type of the specified element is incompatible with this set
     *             (<a href="Collection.html#optional-restrictions">optional</a>)
     * @throws NullPointerException if the specified element is null and this set does not permit
     *             null elements (<a href="Collection.html#optional-restrictions">optional</a>)
     * @throws UnsupportedOperationException if the {@code remove} operation is not supported by
     *             this set
     */
    boolean remove(int value);

    /**
     * {@inheritDoc}
     */
    @Override
    default boolean contains(Object o) {
        return contains((int) o);
    }

    /**
     * Returns {@code true} if this set contains the specified element. More formally, returns
     * {@code true} if and only if this set contains an element {@code e} such that
     * {@code Objects.equals(o, e)}.
     *
     * @param o element whose presence in this set is to be tested
     * @return {@code true} if this set contains the specified element
     * @throws ClassCastException if the type of the specified element is incompatible with this set
     *             (<a href="Collection.html#optional-restrictions">optional</a>)
     * @throws NullPointerException if the specified element is null and this set does not permit
     *             null elements (<a href="Collection.html#optional-restrictions">optional</a>)
     */
    boolean contains(int value);

    /**
     * {@inheritDoc}
     */
    @Override
    default Object[] toArray() {
        return I.signal(this).toList().toArray();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    default <T> T[] toArray(T[] a) {
        return I.signal(this).toList().toArray(a);
    }
}