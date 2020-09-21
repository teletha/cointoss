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

import cointoss.util.SpecializedCodeGenerator;

@Generated("SpecializedCodeGenerator")
public class PrefixRingBuffer<Specializable> {

    /** The fixed buffer size. */
    private final int size;

    /** The actual buffer. */
    private final Specializable[] buffer;

    /** The current index. */
    private int index;

    /**
     * Create new buffer.
     * 
     * @param size A fixed buffer size.
     */
    public PrefixRingBuffer(int size) {
        this.size = size;
        this.buffer = SpecializedCodeGenerator.newArray(size);
    }

    /**
     * Add an item at tail.
     * 
     * @param item An item to add.
     * @return Removed item.
     */
    public Specializable add(Specializable item) {
        Specializable prev = buffer[index];
        buffer[index] = item;
        index = (index + 1) % size;
        return prev;
    }
}