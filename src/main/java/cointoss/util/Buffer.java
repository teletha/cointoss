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

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReferenceArray;
import java.util.function.IntFunction;

/**
 * @version 2017/09/10 11:55:00
 */
public class Buffer<T> implements Iterable<T> {

    /** The name */
    private final String name;

    /** The logical index. */
    private final AtomicInteger logicalStart = new AtomicInteger();

    /** The logical index. */
    private final AtomicInteger logicalEnd = new AtomicInteger();

    /** The physical index. */
    private final AtomicInteger physicalStart = new AtomicInteger();

    /** The physical index. */
    private final AtomicInteger physicalEnd = new AtomicInteger();

    /** The item list. */
    private final ArrayList<T> buffer;

    /** The max size. */
    private final int max;

    /** The max size. */
    private final int block;

    /**
     * @param max
     */
    public Buffer(int max, int block, String name) {
        this.name = name;
        this.max = max;
        this.block = block;
        this.buffer = new ArrayList<>(max);
    }

    /**
     * @param max
     */
    public Buffer(Buffer buffer, String name) {
        this.name = name;
        this.max = buffer.max;
        this.block = buffer.block;
        this.logicalEnd.set(buffer.logicalEnd.intValue());
        this.physicalEnd.set(buffer.physicalEnd.intValue());
        this.buffer = new ArrayList<>(max);
    }

    /**
     * Add new item.
     * 
     * @param item
     */
    public void add(T item) {
        buffer.set(physicalEnd.intValue(), item);

        // increment index
        logicalEnd.incrementAndGet();
        if (physicalEnd.incrementAndGet() == max) {
            physicalEnd.set(0);
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
        for (int i = logicalEnd.intValue(); i < index; i++) {
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
        if (index < 0 || index < logicalEnd.intValue() - max) {
            return null;
        } else {
            return buffer.updateAndGet(index % max, v -> v != null ? v : calculator.apply(index));
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
        return get(logicalEnd.intValue() - offset - 1, calculator);
    }

    /**
     * Return the available start index.
     * 
     * @return
     */
    public int start() {
        int start = logicalEnd.intValue() - max;

        return start < 0 ? 0 : start;
    }

    /**
     * Return the available end index.
     * 
     * @return
     */
    public int end() {
        return logicalEnd.intValue();
    }

    /**
     * Return buffer size.
     * 
     * @return
     */
    public int size() {
        return logicalEnd.intValue();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Iterator<T> iterator() {
        return new Iterator<T>() {

            private int i = start();

            /**
             * {@inheritDoc}
             */
            @Override
            public boolean hasNext() {
                return i < size();
            }

            /**
             * {@inheritDoc}
             */
            @Override
            public T next() {
                return get(i++);
            }
        };
    }

    /**
     * @version 2017/10/11 22:52:17
     */
    private class Block implements Serializable {

        private final long logicalStart;

        private final long logicalEnd;

        private final int size;

        private final AtomicReferenceArray<T> items;

        /**
         * @param logicalStart
         * @param logicalEnd
         * @param size
         */
        private Block(long logicalStart, long logicalEnd, int size) {
            this.logicalStart = logicalStart;
            this.logicalEnd = logicalEnd;
            this.size = size;
            this.items = new AtomicReferenceArray(size);
        }

    }
}
