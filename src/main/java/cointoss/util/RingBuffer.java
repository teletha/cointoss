/*
 * Copyright (C) 2017 Nameless Production Committee
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          http://opensource.org/licenses/mit-license.php
 */
package cointoss.util;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReferenceArray;

/**
 * @version 2017/09/10 9:10:16
 */
public class RingBuffer<T> {

    /** The current position. */
    private final AtomicInteger index = new AtomicInteger(-1);

    /** The item list. */
    private final AtomicReferenceArray<T> buffer;

    /** The max size. */
    private final int size;

    private int first;

    private int last;

    /**
     * @param size
     */
    public RingBuffer(int size) {
        this.size = size;
        this.buffer = new AtomicReferenceArray<>(size);
    }

    /**
     * Add new item.
     * 
     * @param item
     */
    public void add(T item) {
        buffer.set(index.updateAndGet(this::increment), item);
    }

    /**
     * Increment index position.
     * 
     * @param index
     * @return
     */
    private int increment(int index) {
        return index < size - 1 ? index + 1 : 0;
    }

    /**
     * Return latest item.
     * 
     * @return
     */
    public T latest() {
        return latest(0);
    }

    /**
     * Return latest item.
     * 
     * @return
     */
    public T latest(int offset) {
        int i = index.get() - offset % size;

        if (i < 0) {
            i += size;
        }
        return buffer.get(i);
    }
}
