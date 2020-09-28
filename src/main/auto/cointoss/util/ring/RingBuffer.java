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

import java.util.function.Consumer;

@Generated("SpecializedCodeGenerator")
public class RingBuffer<E> {

    /** The fixed buffer size. */
    private final int size;

    /** The actual buffer. */
    private final E[] buffer;

    /** The current index. */
    private int index = -1;

    /**
     * Create new buffer.
     * 
     * @param size A fixed buffer size.
     */
    public RingBuffer(int size) {
        this.size = size;
        this.buffer = (E[]) java.lang.reflect.Array.newInstance(Object.class, size);
    }

    /**
     * Add an item at tail.
     * 
     * @param item An item to add.
     * @return Removed item.
     */
    public E add(E item) {
        int nextIndex = (index + 1) % size;
        E prev = buffer[nextIndex];
        buffer[nextIndex] = item;
        index = nextIndex;
        return prev;
    }

    /**
     * Get the latest item.
     * 
     * @return A latest item.
     */
    public E latest() {
        return buffer[index];
    }

    /**
     * Take all items.
     * 
     * @param consumer
     */
    public void forEach(Consumer<E> consumer) {
        int start = index;
        for (int i = 0; i < size; i++) {
            consumer.accept(buffer[(start + i) % size]);
        }
    }
}