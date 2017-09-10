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
import java.util.function.IntFunction;

/**
 * @version 2017/09/10 11:55:00
 */
public class RingBuffer<T> {

    /** The logical index. */
    private final AtomicInteger logical = new AtomicInteger();

    /** The physical index. */
    private final AtomicInteger physical = new AtomicInteger();

    /** The item list. */
    private final AtomicReferenceArray<T> buffer;

    /** The max size. */
    private final int size;

    /**
     * @param size
     */
    public RingBuffer(int size) {
        this.size = size;
        this.buffer = new AtomicReferenceArray<>(size);
    }

    /**
     * @param size
     */
    public RingBuffer(RingBuffer buffer) {
        this.size = buffer.size;
        this.logical.set(buffer.logical.intValue());
        this.physical.set(buffer.physical.intValue());
        this.buffer = new AtomicReferenceArray<>(size);
    }

    /**
     * Add new item.
     * 
     * @param item
     */
    public void add(T item) {
        buffer.set(physical.intValue(), item);

        // increment index
        logical.incrementAndGet();
        if (physical.incrementAndGet() == size) {
            physical.set(0);
        }
    }

    /**
     * <p>
     * Set new value.
     * </p>
     * 
     * @param index
     * @param value
     */
    public void set(int index, T value) {
        for (int i = logical.intValue(); i < index; i++) {
            add(null);
        }
        add(value);
    }

    /**
     * Get the indexed value.
     * 
     * @param index
     * @return
     */
    public T get(int index) {
        return get(index, v -> null);
    }

    /**
     * Get the indexed value.
     * 
     * @param index
     * @return
     */
    public T get(int index, IntFunction<T> calculator) {
        if (index < 0 || index < logical.intValue() - size || logical.intValue() <= index) {
            return null;
        } else {
            return buffer.updateAndGet(index % size, v -> v != null ? v : calculator.apply(index));
        }
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
        return latest(offset, v -> null);
    }

    /**
     * Return latest item.
     * 
     * @return
     */
    public T latest(int offset, IntFunction<T> calculator) {
        System.out.println(logical.intValue() + "     " + offset + "   " + size);
        return get(logical.intValue() - offset - 1, calculator);
    }

    /**
     * Return the available start index.
     * 
     * @return
     */
    public int start() {
        int start = logical.intValue() - size;

        return start < 0 ? 0 : start;
    }

    /**
     * Return the available end index.
     * 
     * @return
     */
    public int end() {
        return logical.intValue();
    }

    /**
     * Return buffer size.
     * 
     * @return
     */
    public int size() {
        return logical.intValue();
    }
}
