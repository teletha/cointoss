/*
 * Copyright (C) 2018 CoinToss Development Team
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          https://opensource.org/licenses/MIT
 */
package cointoss.util;

import java.util.Iterator;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReferenceArray;
import java.util.function.IntFunction;

/**
 * @version 2017/09/10 11:55:00
 */
public class RingBuffer<T> implements Iterable<T> {

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
    private final AtomicReferenceArray<T> buffer;

    /** The max size. */
    private final int size;

    /**
     * @param size
     */
    public RingBuffer(int size, String name) {
        this.name = name;
        this.size = size;
        this.buffer = new AtomicReferenceArray<>(size);
    }

    /**
     * @param size
     */
    public RingBuffer(RingBuffer buffer, String name) {
        this.name = name;
        this.size = buffer.size;
        this.logicalEnd.set(buffer.logicalEnd.intValue());
        this.physicalEnd.set(buffer.physicalEnd.intValue());
        this.buffer = new AtomicReferenceArray<>(size);
    }

    /**
     * Add new item.
     * 
     * @param item
     */
    public T add(T item) {
        T removed = buffer.getAndSet(physicalEnd.intValue(), item);
        buffer.set(physicalEnd.intValue(), item);

        // increment index
        logicalEnd.incrementAndGet();
        if (physicalEnd.incrementAndGet() == size) {
            physicalEnd.set(0);
        }
        return removed;
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
        if (index < 0 || index < logicalEnd.intValue() - size) {
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
        return get(logicalEnd.intValue() - offset - 1, calculator);
    }

    /**
     * Return the available start index.
     * 
     * @return
     */
    public int start() {
        int start = logicalEnd.intValue() - size;

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
}
