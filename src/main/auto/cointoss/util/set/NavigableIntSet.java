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




/**
 * Sepcialized {@link NavigableSet} interface for int value.
 */
public interface NavigableIntSet extends NavigableSet<Integer>, SortedIntSet {

    /**
     * {@inheritDoc}
     */
    @Override
    default Integer lower(Integer e) {
        return lower((int) e);
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
    int lower(int e);

    /**
     * {@inheritDoc}
     */
    @Override
    default Integer floor(Integer e) {
        return floor((int) e);
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
    int floor(int e);

    /**
     * {@inheritDoc}
     */
    @Override
    default Integer ceiling(Integer e) {
        return ceiling((int) e);
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
    int ceiling(int e);

    /**
     * {@inheritDoc}
     */
    @Override
    default Integer higher(Integer e) {
        return higher((int) e);
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
    int higher(int e);

    /**
     * {@inheritDoc}
     */
    @Override
    default Integer pollFirst() {
        return (Integer) pollFirstInteger();
    }

    /**
     * Retrieves and removes the first (lowest) element, or returns {@code null} if this set is
     * empty.
     *
     * @return the first element, or {@code null} if this set is empty
     */
    int pollFirstInteger();

    /**
     * {@inheritDoc}
     */
    @Override
    default Integer pollLast() {
        return (Integer) pollLastInteger();
    }

    /**
     * Retrieves and removes the last (highest) element, or returns {@code null} if this set is
     * empty.
     *
     * @return the last element, or {@code null} if this set is empty
     */
    int pollLastInteger();

    /**
     * {@inheritDoc}
     */
    @Override
    NavigableIntSet descendingSet();

    /**
     * {@inheritDoc}
     */
    @Override
    default NavigableIntSet subSet(Integer fromElement, boolean fromInclusive, Integer toElement, boolean toInclusive) {
        return subSet((int) fromElement, fromInclusive, (int) toElement, toInclusive);
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
    NavigableIntSet subSet(int fromElement, boolean fromInclusive, int toElement, boolean toInclusive);

    /**
     * {@inheritDoc}
     */
    @Override
    default NavigableIntSet headSet(Integer toElement, boolean inclusive) {
        return headSet((int) toElement, inclusive);
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
    NavigableIntSet headSet(int toElement, boolean inclusive);

    /**
     * {@inheritDoc}
     */
    @Override
    default NavigableIntSet tailSet(Integer fromElement, boolean inclusive) {
        return tailSet((int) fromElement, inclusive);
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
    NavigableIntSet tailSet(int fromElement, boolean inclusive);

    /**
     * {@inheritDoc}
     */
    @Override
    default NavigableIntSet subSet(Integer fromElement, Integer toElement) {
        return subSet((int) fromElement, (int) toElement);
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
    NavigableIntSet subSet(int fromElement, int toElement);

    /**
     * {@inheritDoc}
     */
    @Override
    default NavigableIntSet headSet(Integer toElement) {
        return headSet((int) toElement);
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
    NavigableIntSet headSet(int toElement);

    /**
     * {@inheritDoc}
     */
    @Override
    default NavigableIntSet tailSet(Integer fromElement) {
        return tailSet((int) fromElement);
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
    NavigableIntSet tailSet(int fromElement);
}