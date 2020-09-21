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

import java.lang.reflect.Array;

import javax.annotation.processing.Generated;

@Generated("SpecializedCodeGenerator")
public class IntRingBuffer {

    /** The fixed buffer size. */
    private final int size;

    /** The actual buffer. */
    private final int[] buffer;

    /** The current index. */
    private int index;

    /**
     * @param size
     */
    public IntRingBuffer(int size) {
        this.size = size;
        this.buffer = (int[]) Array.newInstance(Object.class, size);
    }

    /**
     * Add an item at tail.
     * 
     * @param item
     * @return Removed value.
     */
    public int add(int item) {
        int prev = buffer[index];
        buffer[index] = item;
        index = (index + 1) % size;
        return prev;
    }
}