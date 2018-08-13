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
 * @version 2018/08/13 7:16:42
 */
public final class SegmentBuffer<E> {

    /** The fixed size of row. */
    private static final int FixedRowSize = 1 << 14; // 16384

    /** The completed size. */
    private int completedSize;

    /** The completed data manager. */
    private final ConcurrentSkipListMap<LocalDate, Segment<E>> completeds = new ConcurrentSkipListMap();

    /** The uncompleted size. */
    private int uncompletedSize;

    /** The realtime data manager. */
    private final CopyOnWriteArrayList<Object[]> blocks = new CopyOnWriteArrayList();

    /** The current block */
    private Object[] block;

    /** The next empty index. */
    private int blockNextIndex;

    /**
     * 
     */
    public SegmentBuffer() {
        createNewBlock();
    }

    /**
     * Return the size of this {@link SegmentBuffer}.
     * 
     * @return A positive size or zero.
     */
    public int size() {
        return completedSize + uncompletedSize;
    }

    /**
     * Check whether this {@link SegmentBuffer} is empty or not.
     * 
     * @return
     */
    public boolean isEmpty() {
        return (completedSize + uncompletedSize) == 0;
    }

    /**
     * Check whether this {@link SegmentBuffer} is empty or not.
     * 
     * @return
     */
    public boolean isNotEmpty() {
        return (completedSize + uncompletedSize) != 0;
    }

    /**
     * Add realtime item.
     * 
     * @param items An items to add.
     */
    public void add(E item) {
        if (blockNextIndex == FixedRowSize) {
            createNewBlock();
        }

        block[blockNextIndex++] = item;
        uncompletedSize++;
    }

    /**
     * Add all realtime items.
     * 
     * @param items An items to add.
     */
    public void add(E... items) {
        for (E item : items) {
            add(item);
        }
    }

    /**
     * Add all realtime items.
     * 
     * @param items An items to add.
     */
    public void add(Signal<E> items) {
        items.to((Consumer<E>) this::add);
    }

    /**
     * Add all completed items of the specified date.
     * 
     * @param date
     * @param items An items to add.
     */
    public void addCompleted(LocalDate date, E... items) {
        addCompleted(date, I.signal(items));
    }

    /**
     * Add all completed items of the specified date.
     * 
     * @param date
     * @param items An items to add.
     */
    public void addCompleted(LocalDate date, Signal<E> items) {
        completeds.computeIfAbsent(date, key -> {
            CompletedSegment segment = new CompletedSegment(items);
            completedSize += segment.size;
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
        // completed
        for (Segment<E> segment : completeds.values()) {
            if (index < segment.size) {
                return segment.get(index);
            }
            index -= segment.size;
        }

        // ucompleted
        int remainder = index & (FixedRowSize - 1);
        int quotient = (index - remainder) >> 14;
        return (E) blocks.get(quotient)[remainder];
    }

    /**
     * Get the first item.
     * 
     * @return
     */
    public E first() {
        return completedSize != 0 ? completeds.firstEntry().getValue().first() : uncompletedSize != 0 ? (E) blocks.get(0)[0] : null;
    }

    /**
     * Get the first item.
     * 
     * @return
     */
    public E last() {
        return uncompletedSize != 0 ? (E) block[blockNextIndex - 1] : completedSize != 0 ? completeds.lastEntry().getValue().last() : null;
    }

    /**
     * Signal all items.
     * 
     * @return An item stream.
     */
    public Signal<E> each() {
        return each(0, size());
    }

    /**
     * Signal all items from start to last.
     * 
     * @param start A start index (included).
     * @return An item stream.
     */
    public Signal<E> each(int start) {
        return each(start, size());
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
     * Signal all items.
     * 
     * @param each An item processor.
     */
    public void each(Consumer<? super E> each) {
        each(0, size(), each);
    }

    /**
     * Signal all items from start to last.
     * 
     * @param start A start index (included).
     * @param each An item processor.
     */
    public void each(int start, Consumer<? super E> each) {
        each(start, size(), each);
    }

    /**
     * Signal all items from start to end.
     * 
     * @param start A start index (included).
     * @param end A end index (excluded).
     * @param each An item processor.
     */
    public void each(int start, int end, Consumer<? super E> each) {
        if (end < start) {
            throw new IndexOutOfBoundsException("Start[" + start + "] must be less than end[" + end + "].");
        }

        // completed
        for (Segment segment : completeds.values()) {
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

        // uncompleted
        for (Object[] segment : blocks) {
            if (start < FixedRowSize) {
                if (end <= FixedRowSize) {
                    int stop = Math.min(end, uncompletedSize);
                    for (int i = start; i < stop; i++) {
                        each.accept((E) segment[i]);
                    }
                    return;
                } else {
                    for (int i = start; i < FixedRowSize; i++) {
                        each.accept((E) segment[i]);
                    }
                    start = 0;
                    end -= FixedRowSize;
                }
            } else {
                start -= FixedRowSize;
                end -= FixedRowSize;
            }
        }
    }

    private void createNewBlock() {
        blocks.add(block = new Object[FixedRowSize]);
        blockNextIndex = 0;
    }

    /**
     * @version 2018/08/12 7:35:26
     */
    private static abstract class Segment<E> {

        /**
         * Return the item size.
         */
        protected int size;

        /**
         * Get an item at the specified index.
         * 
         * @param index
         * @return
         */
        abstract E get(int index);

        /**
         * Get the first item.
         * 
         * @return
         */
        abstract E first();

        /**
         * Get the first item.
         * 
         * @return
         */
        abstract E last();

        /**
         * Signal all items from start to end.
         * 
         * @param start A start index (included).
         * @param end A end index (excluded).
         * @param each An item processor.
         */
        abstract void each(int start, int end, Consumer<? super E> each);

        /**
         * Signal all items from start to end.
         * 
         * @param start A start index (included).
         * @param end A end index (excluded).
         * @return An item stream.
         */
        final Signal<E> each(int start, int end) {
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
    }

    /**
     * @version 2018/08/13 7:22:22
     */
    private static class CompletedSegment<E> extends Segment<E> {

        /** The actual data manager. */
        private final E[] items;

        /**
         * Completed segment.
         */
        private CompletedSegment(Signal<E> items) {
            ArrayList<E> list = new ArrayList(FixedRowSize);
            items.to(list::add);
            this.items = (E[]) list.toArray();
            this.size = this.items.length;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        E get(int index) {
            return items[index];
        }

        /**
         * {@inheritDoc}
         */
        @Override
        E first() {
            return size == 0 ? null : items[0];
        }

        /**
         * {@inheritDoc}
         */
        @Override
        E last() {
            return size == 0 ? null : items[size - 1];
        }

        /**
         * {@inheritDoc}
         */
        @Override
        void each(int start, int end, Consumer<? super E> each) {
            for (int i = start; i < end; i++) {
                each.accept(items[i]);
            }
        }
    }
}
