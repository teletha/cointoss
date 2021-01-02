/*
 * Copyright (C) 2021 cointoss Development Team
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          https://opensource.org/licenses/MIT
 */
package cointoss.util.set;

import java.util.NoSuchElementException;
import java.util.SortedSet;

import javax.annotation.processing.Generated;


@Generated("SpecializedCodeGenerator")
public interface SortedDoubleSet extends SortedSet<Double>, DoubleSet {

    /**
     * {@inheritDoc}
     */
    @Override
    default SortedSet<Double> subSet(Double fromElement, Double toElement) {
        return subSet((double) fromElement, (double) toElement);
    }

    /**
     * Returns a view of the portion of this set whose elements range from {@code fromElement},
     * inclusive, to {@code toElement}, exclusive. (If {@code fromElement} and {@code toElement} are
     * equal, the returned set is empty.) The returned set is backed by this set, so changes in the
     * returned set are reflected in this set, and vice-versa. The returned set supports all
     * optional set operations that this set supports.
     * <p>
     * The returned set will throw an {@code IllegalArgumentException} on an attempt to insert an
     * element outside its range.
     *
     * @param fromElement low endpoint (inclusive) of the returned set
     * @param toElement high endpoint (exclusive) of the returned set
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
     *             {@code toElement} lies outside the bounds of the range
     */
    SortedDoubleSet subSet(double fromElement, double toElement);

    /**
     * {@inheritDoc}
     */
    @Override
    default SortedSet<Double> headSet(Double toElement) {
        return headSet((double) toElement);
    }

    /**
     * Returns a view of the portion of this set whose elements are strictly less than
     * {@code toElement}. The returned set is backed by this set, so changes in the returned set are
     * reflected in this set, and vice-versa. The returned set supports all optional set operations
     * that this set supports.
     * <p>
     * The returned set will throw an {@code IllegalArgumentException} on an attempt to insert an
     * element outside its range.
     *
     * @param toElement high endpoint (exclusive) of the returned set
     * @return a view of the portion of this set whose elements are strictly less than
     *         {@code toElement}
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
    SortedDoubleSet headSet(double toElement);

    /**
     * {@inheritDoc}
     */
    @Override
    default SortedSet<Double> tailSet(Double fromElement) {
        return tailSet((double) fromElement);
    }

    /**
     * Returns a view of the portion of this set whose elements are greater than or equal to
     * {@code fromElement}. The returned set is backed by this set, so changes in the returned set
     * are reflected in this set, and vice-versa. The returned set supports all optional set
     * operations that this set supports.
     * <p>
     * The returned set will throw an {@code IllegalArgumentException} on an attempt to insert an
     * element outside its range.
     *
     * @param fromElement low endpoint (inclusive) of the returned set
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
    SortedDoubleSet tailSet(double fromElement);

    /**
     * {@inheritDoc}
     */
    @Override
    default Double first() {
        return firstDouble();
    }

    /**
     * Returns the first (lowest) element currently in this set.
     *
     * @return the first (lowest) element currently in this set
     * @throws NoSuchElementException if this set is empty
     */
    double firstDouble();

    /**
     * {@inheritDoc}
     */
    @Override
    default Double last() {
        return lastDouble();
    }

    /**
     * Returns the last (highest) element currently in this set.
     *
     * @return the last (highest) element currently in this set
     * @throws NoSuchElementException if this set is empty
     */
    double lastDouble();
}