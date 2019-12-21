/*
 * Copyright (C) 2019 Nameless Production Committee
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          http://opensource.org/licenses/mit-license.php
 */
package cointoss.util.ring;

public class LongRingBuffer {

    /** The fixed buffer size. */
    private final int size;

    /** The actual buffer. */
    private final long[] buffer;

    /** The current index. */
    private int index;

    /**
     * @param size
     */
    public LongRingBuffer(int size) {
        this.size = size;
        this.buffer = new long[size];
    }

    /**
     * Add an item at tail.
     * 
     * @param item
     * @return Removed value.
     */
    public long add(long item) {
        long prev = buffer[index];
        buffer[index] = item;
        index = (index + 1) % size;
        return prev;
    }
}
