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
import java.util.ArrayList;
import java.util.concurrent.ConcurrentSkipListMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Consumer;

import kiss.I;
import kiss.Signal;

/**
 * @version 2018/08/07 1:48:42
 */
public final class DateSegmentBuffer<E> {

    /** The fixed size of row. */
    private static final int FixedRowSize = 1 << 14; // 16384

    /** The actual size. */
    private int size;

    /** The actual data manager. */
    private final ConcurrentSkipListMap<LocalDate, CompletedSegment<E>> segments = new ConcurrentSkipListMap();

    /** The latest segment. */
    private CompletedSegment<E> segment;

    /**
     * 
     */
    public DateSegmentBuffer() {
    }

    /**
     * Return the size of this {@link DateSegmentBufferTest}.
     * 
     * @return A positive size or zero.
     */
    public int size() {
        return size;
    }

    /**
     * Check whether this {@link DateSegmentBufferTest} is empty or not.
     * 
     * @return
     */
    public boolean isEmpty() {
        return size == 0;
    }

    /**
     * Check whether this {@link DateSegmentBufferTest} is empty or not.
     * 
     * @return
     */
    public boolean isNotEmpty() {
        return size != 0;
    }

    /**
     * Add all items for the specified date.
     * 
     * @param date
     * @param items
     */
    public void add(LocalDate date, E... items) {
        add(date, I.signal(items));
    }

    /**
     * Add all items for the specified date.
     * 
     * @param date
     * @param items
     */
    public void add(LocalDate date, Signal<E> items) {
        segments.computeIfAbsent(date, key -> {
            CompletedSegment segment = new CompletedSegment(date, items);
            size += segment.size;
            return segment;
        });
    }

    /**
     * Get an item at the specified index.
     * 
     * @param index
     * @return
     */
    public E get(int index) {
        for (CompletedSegment<E> segment : segments.values()) {
            if (index < segment.size) {
                return segment.get(index);
            }
            index -= segment.size;
        }

        // If this exception will be thrown, it is bug of this program.
        // So we must rethrow the wrapped error in here.
        throw new Error("FIX ME");
    }

    /**
     * Get the first item.
     * 
     * @return
     */
    public E first() {
        return size == 0 ? null : segments.firstEntry().getValue().first();
    }

    /**
     * Get the first item.
     * 
     * @return
     */
    public E last() {
        return size == 0 ? null : segments.lastEntry().getValue().last();
    }

    /**
     * Signal all items.
     * 
     * @param each An item processor.
     */
    public void each(Consumer<? super E> each) {
        each(0, size, each);
    }

    /**
     * Signal all items from start to last.
     * 
     * @param start A start index (included).
     * @param each An item processor.
     */
    public void each(int start, Consumer<? super E> each) {
        each(start, size, each);
    }

    /**
     * Signal all items from start to end.
     * 
     * @param start A start index (included).
     * @param end A end index (excluded).
     * @param each An item processor.
     */
    public void each(int start, int end, Consumer<? super E> each) {
        for (CompletedSegment segment : segments.values()) {
            int size = segment.size;

            if (start < size) {
                if (end <= size) {
                    segment.each(start, end, each);
                    return;
                } else {
                    segment.each(start, size, each);
                    start = 0;
                    end -= size;
                }
            } else {
                start -= size;
                end -= size;
            }
        }
    }

    /**
     * Signal all items.
     * 
     * @return An item stream.
     */
    public Signal<E> each() {
        return each(0, size);
    }

    /**
     * Signal all items from start to last.
     * 
     * @param start A start index (included).
     * @return An item stream.
     */
    public Signal<E> each(int start) {
        return each(start, size);
    }

    /**
     * Signal all items from start to end.
     * 
     * @param start A start index (included).
     * @param end A end index (excluded).
     * @return An item stream.
     */
    public Signal<E> each(int start, int end) {
        return new Signal<>((observer, disposer) -> {
            try {
                each(start, end, observer);
                observer.complete();
            } catch (Throwable e) {
                observer.error(e);
            }
            return disposer;
        });
    }

    /**
     * @version 2018/08/12 7:35:26
     */
    private static interface Segment<E> {
        /**
         * Get an item at the specified index.
         * 
         * @param index
         * @return
         */
        E get(int index);

        /**
         * Get the first item.
         * 
         * @return
         */
        E first();

        /**
         * Get the first item.
         * 
         * @return
         */
        E last();

        /**
         * Signal all items from start to end.
         * 
         * @param start A start index (included).
         * @param end A end index (excluded).
         * @param each An item processor.
         */
        void each(int start, int end, Consumer<? super E> each);
    }

    /**
     * @version 2018/08/07 17:25:58
     */
    static class CompletedSegment<E> implements Segment<E> {

        /** The associated date. */
        private final LocalDate date;

        /** The total size of this segment. */
        private final int size;

        /** The actual data manager. */
        private final E[] items;

        /**
         * Completed segment.
         * 
         * @param date
         */
        CompletedSegment(LocalDate date, Signal<E> items) {
            this.date = date;

            ArrayList<E> list = new ArrayList(FixedRowSize);
            items.to(list::add);
            this.items = (E[]) list.toArray();
            this.size = this.items.length;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public E get(int index) {
            return items[index];
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public E first() {
            return size == 0 ? null : items[0];
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public E last() {
            return size == 0 ? null : items[size - 1];
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public void each(int start, int end, Consumer<? super E> each) {
            for (int i = start; i < end; i++) {
                each.accept(items[i]);
            }
        }
    }

    /**
     * @version 2018/08/07 17:25:58
     */
    static class UncompletedSegment<E> {

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
        UncompletedSegment(LocalDate date) {
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
        E first() {
            return size == 0 ? null : (E) blocks.get(0)[0];
        }

        /**
         * Get the first item.
         * 
         * @return
         */
        E last() {
            return size == 0 ? null : (E) block[blockNextIndex - 1];
        }

        private void createNewBlock() {
            blocks.add(block = new Object[FixedRowSize]);
            blockNextIndex = 0;
        }
    }
}
