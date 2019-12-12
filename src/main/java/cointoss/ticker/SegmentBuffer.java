/*
 * Copyright (C) 2019 CoinToss Development Team
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          https://opensource.org/licenses/MIT
 */
package cointoss.ticker;

import java.util.Objects;
import java.util.concurrent.ConcurrentSkipListMap;
import java.util.function.Consumer;
import java.util.function.ToLongFunction;

import kiss.Signal;

final class SegmentBuffer<E> {

    /** The span. */
    private final Span span;

    /** The fixed segment size. */
    private final int segmentSize;

    /** The key extractor. */
    private final ToLongFunction<E> timestampExtractor;

    /** The completed size. */
    private int completedSize;

    /** The completed data manager. */
    private final ConcurrentSkipListMap<Long, Completed<E>> completeds = new ConcurrentSkipListMap();

    /** The uncompleted size. */
    private int uncompletedSize;

    /** The uncompleted date manager. */
    private E[] uncompleted;

    /**
     * 
     */
    public SegmentBuffer(Span span, ToLongFunction<E> timestampExtractor) {
        this.span = Objects.requireNonNull(span);
        this.segmentSize = span.ticksPerDay();
        this.timestampExtractor = Objects.requireNonNull(timestampExtractor);
        this.uncompleted = (E[]) new Object[segmentSize];
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
     * @param positions An items to add.
     */
    public void add(E item) {
        try {
            uncompleted[uncompletedSize++] = item;
        } catch (ArrayIndexOutOfBoundsException e) {
            completeds.computeIfAbsent(timestampExtractor.applyAsLong(uncompleted[0]), key -> {
                Completed segment = new Completed(uncompleted);
                completedSize += segment.size;
                return segment;
            });
            uncompleted = (E[]) new Object[segmentSize];
            uncompletedSize = 0;
            add(item);
        }
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
     * Get an item at the specified index.
     * 
     * @param index
     * @return
     */
    public E get(int index) {
        // completed
        for (Completed<E> segment : completeds.values()) {
            if (index < segment.size) {
                return segment.items[index];
            }
            index -= segment.size;
        }

        // ucompleted
        if (segmentSize <= index) {
            index = segmentSize - 1;
        }
        return uncompleted[index];
    }

    /**
     * Get the first item.
     * 
     * @return
     */
    public E first() {
        if (!completeds.isEmpty()) {
            return completeds.firstEntry().getValue().first();
        }

        if (uncompletedSize != 0) {
            return uncompleted[0];
        }
        return null;
    }

    /**
     * Get the last item.
     * 
     * @return
     */
    public E last() {
        if (uncompletedSize != 0) {
            return uncompleted[uncompletedSize - 1];
        }

        if (!completeds.isEmpty()) {
            return completeds.lastEntry().getValue().last();
        }
        return null;
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
        for (Completed segment : completeds.values()) {
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
        if (0 < uncompletedSize) {
            for (int i = start; i < Math.min(end, uncompletedSize); i++) {
                each.accept(uncompleted[i]);
            }
        }
    }

    /**
     * Clear all items.
     */
    public void clear() {
        uncompleted = (E[]) new Object[segmentSize];
        completeds.clear();
    }

    /**
     * Completely filled segment.
     */
    private static class Completed<E> {

        /**
         * Return the item size.
         */
        private int size;

        /** The actual data manager. */
        private final E[] items;

        /**
         * Completed segment.
         */
        private Completed(int segmentSize, Signal<E> items) {
            this.items = (E[]) new Object[segmentSize];

            items.to(e -> {
                this.items[size++] = e;
            });
        }

        /**
         * Completed segment.
         */
        private Completed(E[] items) {
            this.items = items;
            this.size = items.length;
        }

        /**
         * Get the first item.
         * 
         * @return
         */
        private E first() {
            return size == 0 ? null : (E) items[0];
        }

        /**
         * Get the first item.
         * 
         * @return
         */
        private E last() {
            return size == 0 ? null : (E) items[size - 1];
        }

        /**
         * Signal all items from start to end.
         * 
         * @param start A start index (included).
         * @param end A end index (excluded).
         * @param each An item processor.
         */
        private void each(int start, int end, Consumer<? super E> each) {
            int stop = Math.min(end, size);

            for (int i = start; i < stop; i++) {
                each.accept(items[i]);
            }
        }
    }
}
