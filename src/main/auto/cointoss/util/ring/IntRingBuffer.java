/*
 * Copyright (C) 2020 Nameless Production Committee
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          http://opensource.org/licenses/mit-license.php
 */
package cointoss.util.ring;

import java.util.function.IntBinaryOperator;
import java.util.function.IntConsumer;

import javax.annotation.processing.Generated;

@Generated("SpecializedCodeGenerator")
public class IntRingBuffer {

    /** The fixed buffer size. */
    private final int size;

    /** The actual buffer. */
    private final int[] buffer;

    /** The current index. */
    private int index = -1;

    /**
     * Create new buffer.
     * 
     * @param size A fixed buffer size.
     */
    public IntRingBuffer(int size) {
        this.size = size;
        this.buffer = new int[size];
    }

    /**
     * Add an item at tail.
     * 
     * @param item An item to add.
     * @return Removed item.
     */
    public int add(int item) {
        int nextIndex = (index + 1) % size;
        int prev = buffer[nextIndex];
        buffer[nextIndex] = item;
        index = nextIndex;
        return prev;
    }

    /**
     * Get the latest item.
     * 
     * @return A latest item.
     */
    public int latest() {
        return buffer[index];
    }

    /**
     * Take all items.
     * 
     * @param consumer
     */
    public void forEach(IntConsumer consumer) {
        int start = index + 1;
        for (int i = 0; i < size; i++) {
            consumer.accept(buffer[(start + i) % size]);
        }
    }

    /**
     * Reduce items.
     * 
     * @param operator The calculation.
     * @return The reduced result.
     */
    public int reduce(IntBinaryOperator operator) {
        int start = index;
        int result = buffer[start];
        for (int i = 1; i < size; i++) {
            int item = buffer[(start + i) % size];
            if (item != 0) result = operator.applyAsInt(result, item);
        }
        return result;
    }
}