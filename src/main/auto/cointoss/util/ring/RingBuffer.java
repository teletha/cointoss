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

import javax.annotation.processing.Generated;

import java.lang.reflect.Array;



@Generated("SpecializedCodeGenerator")
public class RingBuffer<E> {

    /** The fixed buffer size. */
    private final int size;

    /** The actual buffer. */
    private final E[] buffer;

    /** The current index. */
    private int index;

    /**
     * Create new buffer.
     * 
     * @param size A fixed buffer size.
     */
    public RingBuffer(int size) {
        this.size = size;
        this.buffer = (E[]) Array.newInstance(Object.class, size);
    }

    /**
     * Add an item at tail.
     * 
     * @param item An item to add.
     * @return Removed item.
     */
    public E add(E item) {
        E prev = buffer[index];
        buffer[index] = item;
        index = (index + 1) % size;
        return prev;
    }
}