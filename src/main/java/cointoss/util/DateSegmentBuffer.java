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

import kiss.Signal;

/**
 * @version 2018/08/07 1:48:42
 */
public final class DateSegmentBuffer<E> {

    /** The fixed size of row. */
    private static final int FixedRowSize = 1 << 14; // 16384

    /** The actual size. */
    private long size;

    /** The actual data manager. */
    private final CopyOnWriteArrayList<CompletedSegment<E>> segments = new CopyOnWriteArrayList();

    /** The latest segment. */
    private CompletedSegment<E> segment;

    /** The next empty index. */
    private int rowNextIndex;

    /**
     * 
     */
    public DateSegmentBuffer() {
        createNewRow();
    }

    private void createNewRow(LocalDate date) {
        segments.add(segment = new CompletedSegment(date));
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
     * Add all items for the specified date.
     * 
     * @param date
     * @param items
     */
    public void add(LocalDate date, Signal<E> items) {
        if (segments.isEmpty() || date.isBefore(segments.get(0).date)) {
            CompletedSegment<E> segment = new CompletedSegment(date);
            segments.add(0, segment);
            items.to(segment::add);
        }
    }

    /**
     * Get an item at the specified index.
     * 
     * @param index
     * @return
     */
    public E get(long index) {
        int total = 0;
        
        for (CompletedSegment<E> segment : segments) {
            if (index < total +) {
                return segment.get(total );
            }
        }
        long remainder = index & (FixedRowSize - 1);
        long quotient = (index - remainder) >> 14;
        return (E) blocks.get((int) quotient)[(int) remainder];
    }

    /**
     * Get the first item.
     * 
     * @return
     */
    public E peekFirst() {
        return size == 0 ? null : segments.get(0).peekFirst();
    }

    /**
     * Get the first item.
     * 
     * @return
     */
    public E peekLast() {
        return size == 0 ? null : segment.peekLast();
    }

    public

    /**
     * @version 2018/08/07 17:25:58
     */
    static class CompletedSegment<E> {

        /** The associated date. */
        private final LocalDate date;

        /** The total size of this segment. */
        int size;

        int total;

        /** The actual data manager. */
        private final CopyOnWriteArrayList<Object[]> blocks = new CopyOnWriteArrayList();

        /** The current block */
        private Object[] block;

        /** The next empty index. */
        private int blockNextIndex;

        /**
         * Completed segment.
         * 
         * @param date
         */
        CompletedSegment(LocalDate date) {
            this.date = date;
            createNewBlock();
        }

        /**
         * Add the given item at end.
         * 
         * @param item
         */
        void add(E item) {
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
        E get(long index) {
            long remainder = index & (FixedRowSize - 1);
            long quotient = (index - remainder) >> 14;
            return (E) blocks.get((int) quotient)[(int) remainder];
        }

        /**
         * Get the first item.
         * 
         * @return
         */
        E peekFirst() {
            return size == 0 ? null : (E) blocks.get(0)[0];
        }

        /**
         * Get the first item.
         * 
         * @return
         */
        E peekLast() {
            return size == 0 ? null : (E) block[blockNextIndex - 1];
        }

        private void createNewBlock() {
            blocks.add(block = new Object[FixedRowSize]);
            blockNextIndex = 0;
        }
    }
}
