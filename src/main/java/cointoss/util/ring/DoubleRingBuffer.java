/*
 * Copyright (C) 2020 cointoss Development Team
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          https://opensource.org/licenses/MIT
 */
package cointoss.util.ring;

public class DoubleRingBuffer {

    /** The fixed buffer size. */
    private final int size;

    /** The actual buffer. */
    private final double[] buffer;

    /** The current index. */
    private int index;

    /**
     * @param size
     */
    public DoubleRingBuffer(int size) {
        this.size = size;
        this.buffer = new double[size];
    }

    /**
     * Add an item at tail.
     * 
     * @param item
     * @return Removed value.
     */
    public double add(double item) {
        double prev = buffer[index];
        buffer[index] = item;
        index = (index + 1) % size;
        return prev;
    }
}