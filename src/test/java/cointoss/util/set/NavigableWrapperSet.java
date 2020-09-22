/*
 * Copyright (C) 2019 Nameless Production Committee
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          http://opensource.org/licenses/mit-license.php
 */
package cointoss.util.set;

import java.util.NavigableSet;

import javax.annotation.processing.Generated;

import cointoss.util.SpecializedCodeGenerator.Primitive;
import cointoss.util.SpecializedCodeGenerator.Wrapper;

/**
 * Sepcialized {@link NavigableSet} interface for Primitive value.
 */
@Generated("SpecializedCodeGenerator")
public interface NavigableWrapperSet extends NavigableSet<Wrapper>, SortedWrapperSet {

    /**
     * {@inheritDoc}
     */
    @Override
    default Wrapper lower(Wrapper e) {
        return lower((Primitive) e);
    }

    /**
     * Returns the greatest element in this set strictly less than the given element, or
     * {@code null} if there is no such element.
     *
     * @param e the value to match
     * @return the greatest element less than {@code e}, or {@code null} if there is no such element
     * @throws ClassCastException if the specified element cannot be compared with the elements
     *             currently in the set
     * @throws NullPointerException if the specified element is null and this set does not permit
     *             null elements
     */
    Primitive lower(Primitive e);

    /**
     * {@inheritDoc}
     */
    @Override
    default Wrapper floor(Wrapper e) {
        return floor((Primitive) e);
    }

    /**
     * Returns the greatest element in this set less than or equal to the given element, or
     * {@code null} if there is no such element.
     *
     * @param e the value to match
     * @return the greatest element less than or equal to {@code e}, or {@code null} if there is no
     *         such element
     * @throws ClassCastException if the specified element cannot be compared with the elements
     *             currently in the set
     * @throws NullPointerException if the specified element is null and this set does not permit
     *             null elements
     */
    Primitive floor(Primitive e);

    /**
     * {@inheritDoc}
     */
    @Override
    default Wrapper ceiling(Wrapper e) {
        return ceiling((Primitive) e);
    }

    /**
     * Returns the least element in this set greater than or equal to the given element, or
     * {@code null} if there is no such element.
     *
     * @param e the value to match
     * @return the least element greater than or equal to {@code e}, or {@code null} if there is no
     *         such element
     * @throws ClassCastException if the specified element cannot be compared with the elements
     *             currently in the set
     * @throws NullPointerException if the specified element is null and this set does not permit
     *             null elements
     */
    Primitive ceiling(Primitive e);

    /**
     * {@inheritDoc}
     */
    @Override
    default Wrapper higher(Wrapper e) {
        return higher((Primitive) e);
    }

    /**
     * Returns the least element in this set strictly greater than the given element, or
     * {@code null} if there is no such element.
     *
     * @param e the value to match
     * @return the least element greater than {@code e}, or {@code null} if there is no such element
     * @throws ClassCastException if the specified element cannot be compared with the elements
     *             currently in the set
     * @throws NullPointerException if the specified element is null and this set does not permit
     *             null elements
     */
    Primitive higher(Primitive e);

    /**
     * {@inheritDoc}
     */
    @Override
    default Wrapper pollFirst() {
        return pollFirstWrapper();
    }

    /**
     * Retrieves and removes the first (lowest) element, or returns {@code null} if this set is
     * empty.
     *
     * @return the first element, or {@code null} if this set is empty
     */
    Primitive pollFirstWrapper();

    /**
     * {@inheritDoc}
     */
    @Override
    default Wrapper pollLast() {
        return pollLastWrapper();
    }

    /**
     * Retrieves and removes the last (highest) element, or returns {@code null} if this set is
     * empty.
     *
     * @return the last element, or {@code null} if this set is empty
     */
    Primitive pollLastWrapper();

    /**
     * {@inheritDoc}
     */
    @Override
    NavigableWrapperSet descendingSet();

    /**
     * {@inheritDoc}
     */
    @Override
    default NavigableWrapperSet subSet(Wrapper fromElement, boolean fromInclusive, Wrapper toElement, boolean toInclusive) {
        return subSet((Primitive) fromElement, fromInclusive, (Primitive) toElement, toInclusive);
    }

    /**
     * Returns a view of the portion of this set whose elements range from {@code fromElement} to
     * {@code toElement}. If {@code fromElement} and {@code toElement} are equal, the returned set
     * is empty unless {@code
     * fromInclusive} and {@code toInclusive} are both true. The returned set is backed by this set,
     * so changes in the returned set are reflected in this set, and vice-versa. The returned set
     * supports all optional set operations that this set supports.
     * <p>
     * The returned set will throw an {@code IllegalArgumentException} on an attempt to insert an
     * element outside its range.
     *
     * @param fromElement low endpoint of the returned set
     * @param fromInclusive {@code true} if the low endpoint is to be included in the returned view
     * @param toElement high endpoint of the returned set
     * @param toInclusive {@code true} if the high endpoint is to be included in the returned view
     * @return a view of the portion of this set whose elements range from {@code fromElement},
     *         inclusive, to {@code toElement}, exclusive
     * @throws ClassCastException if {@code fromElement} and {@code toElement} cannot be compared to
     *             one another using this set's comparator (or, if the set has no comparator, using
     *             natural ordering). Implementations may, but are not required to, throw this
     *             exception if {@code fromElement} or {@code toElement} cannot be compared to
     *             elements currently in the set.
     * @throws NullPointerException if {@code fromElement} or {@code toElement} is null and this set
     *             does not permit null elements
     * @throws IllegalArgumentException if {@code fromElement} is greater than {@code toElement}; or
     *             if this set itself has a restricted range, and {@code fromElement} or
     *             {@code toElement} lies outside the bounds of the range.
     */
    NavigableWrapperSet subSet(Primitive fromElement, boolean fromInclusive, Primitive toElement, boolean toInclusive);

    /**
     * {@inheritDoc}
     */
    @Override
    default NavigableWrapperSet headSet(Wrapper toElement, boolean inclusive) {
        return headSet((Primitive) toElement, inclusive);
    }

    /**
     * Returns a view of the portion of this set whose elements are less than (or equal to, if
     * {@code inclusive} is true) {@code toElement}. The returned set is backed by this set, so
     * changes in the returned set are reflected in this set, and vice-versa. The returned set
     * supports all optional set operations that this set supports.
     * <p>
     * The returned set will throw an {@code IllegalArgumentException} on an attempt to insert an
     * element outside its range.
     *
     * @param toElement high endpoint of the returned set
     * @param inclusive {@code true} if the high endpoint is to be included in the returned view
     * @return a view of the portion of this set whose elements are less than (or equal to, if
     *         {@code inclusive} is true) {@code toElement}
     * @throws ClassCastException if {@code toElement} is not compatible with this set's comparator
     *             (or, if the set has no comparator, if {@code toElement} does not implement
     *             {@link Comparable}). Implementations may, but are not required to, throw this
     *             exception if {@code toElement} cannot be compared to elements currently in the
     *             set.
     * @throws NullPointerException if {@code toElement} is null and this set does not permit null
     *             elements
     * @throws IllegalArgumentException if this set itself has a restricted range, and
     *             {@code toElement} lies outside the bounds of the range
     */
    NavigableWrapperSet headSet(Primitive toElement, boolean inclusive);

    /**
     * {@inheritDoc}
     */
    @Override
    default NavigableWrapperSet tailSet(Wrapper fromElement, boolean inclusive) {
        return tailSet((Primitive) fromElement, inclusive);
    }

    /**
     * Returns a view of the portion of this set whose elements are greater than (or equal to, if
     * {@code inclusive} is true) {@code fromElement}. The returned set is backed by this set, so
     * changes in the returned set are reflected in this set, and vice-versa. The returned set
     * supports all optional set operations that this set supports.
     * <p>
     * The returned set will throw an {@code IllegalArgumentException} on an attempt to insert an
     * element outside its range.
     *
     * @param fromElement low endpoint of the returned set
     * @param inclusive {@code true} if the low endpoint is to be included in the returned view
     * @return a view of the portion of this set whose elements are greater than or equal to
     *         {@code fromElement}
     * @throws ClassCastException if {@code fromElement} is not compatible with this set's
     *             comparator (or, if the set has no comparator, if {@code fromElement} does not
     *             implement {@link Comparable}). Implementations may, but are not required to,
     *             throw this exception if {@code fromElement} cannot be compared to elements
     *             currently in the set.
     * @throws NullPointerException if {@code fromElement} is null and this set does not permit null
     *             elements
     * @throws IllegalArgumentException if this set itself has a restricted range, and
     *             {@code fromElement} lies outside the bounds of the range
     */
    NavigableWrapperSet tailSet(Primitive fromElement, boolean inclusive);

    /**
     * {@inheritDoc}
     */
    @Override
    default NavigableWrapperSet subSet(Wrapper fromElement, Wrapper toElement) {
        return subSet((Primitive) fromElement, (Primitive) toElement);
    }

    /**
     * {@inheritDoc}
     * <p>
     * Equivalent to {@code subSet(fromElement, true, toElement, false)}.
     *
     * @throws ClassCastException {@inheritDoc}
     * @throws NullPointerException {@inheritDoc}
     * @throws IllegalArgumentException {@inheritDoc}
     */
    @Override
    NavigableWrapperSet subSet(Primitive fromElement, Primitive toElement);

    /**
     * {@inheritDoc}
     */
    @Override
    default NavigableWrapperSet headSet(Wrapper toElement) {
        return headSet((Primitive) toElement);
    }

    /**
     * {@inheritDoc}
     * <p>
     * Equivalent to {@code headSet(toElement, false)}.
     *
     * @throws ClassCastException {@inheritDoc}
     * @throws NullPointerException {@inheritDoc}
     * @throws IllegalArgumentException {@inheritDoc}
     */
    @Override
    NavigableWrapperSet headSet(Primitive toElement);

    /**
     * {@inheritDoc}
     */
    @Override
    default NavigableWrapperSet tailSet(Wrapper fromElement) {
        return tailSet((Primitive) fromElement);
    }

    /**
     * {@inheritDoc}
     * <p>
     * Equivalent to {@code tailSet(fromElement, true)}.
     *
     * @throws ClassCastException {@inheritDoc}
     * @throws NullPointerException {@inheritDoc}
     * @throws IllegalArgumentException {@inheritDoc}
     */
    @Override
    NavigableWrapperSet tailSet(Primitive fromElement);
}
