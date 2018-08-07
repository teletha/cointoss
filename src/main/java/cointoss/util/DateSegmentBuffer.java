/*
 * Copyright (C) 2018 Nameless Production Committee
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          http://opensource.org/licenses/mit-license.php
 */
package cointoss.util;

import java.time.LocalDate;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * @version 2018/08/07 1:48:42
 */
public class DateSegmentBuffer<E> {

    /** The fixed size of row. */
    private static final int FixedRowSize = 1 << 14; // 16384

    /** The actual size. */
    private long size;

    /** The actual data manager. */
    private final CopyOnWriteArrayList<Object[]> rows = new CopyOnWriteArrayList();

    /** The current row. */
    private Object[] row;

    /** The next empty index. */
    private int rowNextIndex;

    /**
     * 
     */
    public DateSegmentBuffer() {
        createNewRow();
    }

    private void createNewRow() {
        rows.add(row = new Object[FixedRowSize]);
        rowNextIndex = 0;
    }

    /**
     * Return the size of this {@link DateSegmentBuffer}.
     * 
     * @return A positive size or zero.
     */
    public long size() {
        return size;
    }

    /**
     * Check whether this {@link DateSegmentBuffer} is empty or not.
     * 
     * @return
     */
    public boolean isEmpty() {
        return size == 0;
    }

    /**
     * Add the given item at end.
     * 
     * @param item
     */
    public void add(E item) {
        row[rowNextIndex++] = item;
        size++;

        if (rowNextIndex == FixedRowSize) {
            createNewRow();
        }
    }

    /**
     * Get an item at the specified index.
     * 
     * @param index
     * @return
     */
    public E get(long index) {
        long remainder = index % FixedRowSize;
        long quotient = (index - remainder) / FixedRowSize;
        return (E) rows.get((int) quotient)[(int) remainder];
    }

    /**
     * Get the first item.
     * 
     * @return
     */
    public E peekFirst() {
        return size == 0 ? null : (E) rows.get(0)[0];
    }

    /**
     * Get the first item.
     * 
     * @return
     */
    public E peekLast() {
        return size == 0 ? null : (E) row[rowNextIndex - 1];
    }

    /**
     * @version 2018/08/07 17:25:58
     */
    private class Segment {

        /** The associated date. */
        private final LocalDate date;

        /** The total size of this segment. */
        private int size;

        /** The actual data manager. */
        private final CopyOnWriteArrayList<Object[]> blocks = new CopyOnWriteArrayList();

        /** The current block */
        private Object[] block;

        /** The next empty index. */
        private int blockNextIndex;

        private Segment(LocalDate date) {
            this.date = date;
            createNewBlock();
        }

        /**
         * Add the given item at end.
         * 
         * @param item
         */
        public void add(E item) {
            block[blockNextIndex++] = item;
            size++;

            if (blockNextIndex == FixedRowSize) {
                createNewBlock();
            }
        }

        /**
         * Get an item at the specified index.
         * 
         * @param index
         * @return
         */
        public E get(long index) {
            long remainder = index & (FixedRowSize - 1);
            long quotient = (index - remainder) >> 14;
            return (E) rows.get((int) quotient)[(int) remainder];
        }

        private void createNewBlock() {
            rows.add(row = new Object[FixedRowSize]);
            rowNextIndex = 0;
        }
    }
}
