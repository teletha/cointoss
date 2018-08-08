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
    private final CopyOnWriteArrayList<Segment> segments = new CopyOnWriteArrayList();

    /** The latest segment. */
    private Segment segment;

    /** The next empty index. */
    private int rowNextIndex;

    /**
     * 
     */
    public DateSegmentBuffer() {
        createNewRow();
    }

    private void createNewRow(LocalDate date) {
        segments.add(segment = new Segment(date));
        rowNextIndex = 0;
    }

    /**
     * Return the size of this {@link DateSegmentBuffer}.
     * 
     * @return A positive size or zero.
     */
    public final long size() {
        return size;
    }

    /**
     * Check whether this {@link DateSegmentBuffer} is empty or not.
     * 
     * @return
     */
    public final boolean isEmpty() {
        return size == 0;
    }

    /**
     * Check whether this {@link DateSegmentBuffer} is empty or not.
     * 
     * @return
     */
    public final boolean isNotEmpty() {
        return size != 0;
    }

    /**
     * Create new segment for the specified {@link LocalDate}.
     * 
     * @param date
     */
    public final void createNewSegment(LocalDate date) {
        if (date != null) {

        }
    }

    /**
     * Add the given item at end.
     * 
     * @param item
     */
    public void add(E item) {
        segment[rowNextIndex++] = item;
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
        long remainder = index & (FixedRowSize - 1);
        long quotient = (index - remainder) >> 14;
        return (E) segments.get((int) quotient)[(int) remainder];
    }

    /**
     * Get the first item.
     * 
     * @return
     */
    public E peekFirst() {
        return size == 0 ? null : (E) segments.get(0)[0];
    }

    /**
     * Get the first item.
     * 
     * @return
     */
    public E peekLast() {
        return size == 0 ? null : (E) segment[rowNextIndex - 1];
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
        private void add(E item) {
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
        private E get(long index) {
            long remainder = index & (FixedRowSize - 1);
            long quotient = (index - remainder) >> 14;
            return (E) segments.get((int) quotient)[(int) remainder];
        }

        private void createNewBlock() {
            segments.add(segment = new Object[FixedRowSize]);
            rowNextIndex = 0;
        }
    }
}
