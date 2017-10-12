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
import java.util.LinkedList;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReferenceArray;
import java.util.function.IntFunction;

/**
 * @version 2017/09/10 11:55:00
 */
public class Buffer<T> {

    /** The name */
    private final String name;

    /** The logical index. */
    private final AtomicInteger logicalStart = new AtomicInteger();

    /** The logical index. */
    private final AtomicInteger logicalEnd = new AtomicInteger(-1);

    /** The physical index. */
    private final AtomicInteger physicalStart = new AtomicInteger();

    /** The physical index. */
    private final AtomicInteger physicalEnd = new AtomicInteger(-1);

    /** The block size. */
    private final int size;

    /** The block length. */
    private final int length;

    /** The block list. */
    private final LinkedList<Block> blocks = new LinkedList<>();

    /** The last block. (this block is always on memory) */
    private Block last;

    /**
     * @param size
     * @param length
     * @param name
     */
    public Buffer(int size, int length, String name) {
        this.name = name;
        this.size = size;
        this.length = length;
        this.last = new Block(0, length);
    }

    /**
     * Add new item.
     * 
     * @param item
     */
    public void append(T item) {
        // increment logical end index
        int end = logicalEnd.incrementAndGet();

        if (end <= last.logicalEnd) {
            // current block
            last.append(item);
        } else {
            // store current block
            blocks.add(last);

            // next block
            last = new Block(end, length);
            last.append(item);
        }
    }

    /**
     * Add items to tail.
     * 
     * @param items
     */
    public final void append(T... items) {
        for (T item : items) {
            append(item);
        }
    }

    /**
     * Return tail item.
     * 
     * @return
     */
    public final T end() {
        return end(0);
    }

    /**
     * Return tail item.
     * 
     * @return
     */
    public final T end(int offset) {
        return end(offset, v -> null);
    }

    /**
     * Return tail item.
     * 
     * @return
     */
    public T end(int offset, IntFunction<T> calculator) {
        // compute block index
        if (offset < last.physicalEnd) {
            // last block
            return last.end(offset, calculator);
        } else {
            // stored block
            // If this exception will be thrown, it is bug of this program. So we must rethrow the
            // wrapped error in here.
            throw new Error();
        }
    }

    /**
     * Return the available start index.
     * 
     * @return
     */
    public final int startIndex() {
        return logicalStart.get();
    }

    /**
     * Return the available end index.
     * 
     * @return
     */
    public final int endIndex() {
        return logicalEnd.get();
    }

    /**
     * Return the available start index.
     * 
     * @return
     */
    public final int firstIndex() {
        return physicalStart.get();
    }

    /**
     * Return the available end index.
     * 
     * @return
     */
    public final int lastIndex() {
        return physicalEnd.get();
    }

    /**
     * @version 2017/10/11 22:52:17
     */
    private class Block implements Serializable {

        private final int logicalStart;

        private final int logicalEnd;

        private int physicalEnd;

        private final int length;

        private final AtomicReferenceArray<T> items;

        /**
         * @param logicalStart
         * @param logicalEnd
         * @param length
         */
        private Block(int logicalStart, int length) {
            this.logicalStart = logicalStart;
            this.logicalEnd = logicalStart + length - 1;
            this.physicalEnd = 0;
            this.length = length;
            this.items = new AtomicReferenceArray(length);
        }

        private boolean isFull() {
            return physicalEnd == length - 1;
        }

        private void append(T item) {
            items.set(physicalEnd++, item);
        }

        private T end(int offset, IntFunction<T> calculator) {
            return items.get(physicalEnd - offset - 1);
        }
    }
}
