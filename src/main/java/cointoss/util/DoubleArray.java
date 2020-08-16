/*
 * Copyright (C) 2020 cointoss Development Team
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          https://opensource.org/licenses/MIT
 */
package cointoss.util;

public class DoubleArray {

    private volatile int size = 0;

    private double[] array;

    /**
     * Create empty array.
     */
    public DoubleArray() {
        this(16);
    }

    /**
     * Create empty array with the specified array size.
     */
    public DoubleArray(int initialRawArraySize) {
        array = new double[initialRawArraySize];
    }

    /**
     * Add new value at last.
     * 
     * @param value A value to append.
     * @return
     */
    public DoubleArray add(double value) {
        if (array.length <= size) {
            widen(size + 1);
        }

        array[size++] = value;
        return this;
    }

    /**
     * Widen the base array's capacity.
     */
    private synchronized void widen(int require) {
        int length = array.length;
        if (length <= require) {
            double[] large = new double[Math.max(require, length + Math.min(length, 1024))];
            System.arraycopy(array, 0, large, 0, length);
            array = large;
        }
    }

    /**
     * Get the first element. If this array has no element, 0 will be returned.
     * 
     * @return
     */
    public double first() {
        return size == 0 ? 0 : array[0];
    }

    /**
     * Get the last element. If this array has no element, 0 will be returned.
     * 
     * @return
     */
    public double last() {
        return size == 0 ? 0 : array[size - 1];
    }

    /**
     * Get the value at the specified index. If out of bounded index is specified, 0 will be
     * returned.
     * 
     * @param index
     * @return
     */
    public double get(int index) {
        return 0 <= index && index < size ? array[index] : 0;
    }

    public DoubleArray set(int index, double value) {
        if (size <= index) {
            if (array.length <= index) {
                widen(index);
            }
            size = index + 1;
            array[index] = value;
        } else if (0 <= index) {
            array[index] = value;
        }
        return this;
    }

    public void modify(int index, double value) {
        array[index] = array[index] + value;
    }

    /**
     * Get the number of elemenets.
     * 
     * @return
     */
    public int size() {
        return size;
    }

    /**
     * Check whether this array is empty or not.
     * 
     * @return
     */
    public boolean isEmpty() {
        return size == 0;
    }

    /**
     * Check whether this array is empty or not.
     * 
     * @return
     */
    public boolean isNotEmpty() {
        return size != 0;
    }

    /**
     * Clear all elements.
     * 
     * @return
     */
    public DoubleArray clear() {
        size = 0;
        return this;
    }

    /**
     * Expose low-level array.
     * 
     * @return
     */
    public double[] asArray() {
        return array;
    }
}