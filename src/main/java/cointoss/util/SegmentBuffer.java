/*
 * Copyright (C) 2019 CoinToss Development Team
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          https://opensource.org/licenses/MIT
 */
package cointoss.util;

import java.util.Iterator;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.concurrent.ConcurrentNavigableMap;
import java.util.concurrent.ConcurrentSkipListMap;
import java.util.function.Consumer;
import java.util.function.ToLongFunction;

import cointoss.ticker.Span;

public final class SegmentBuffer<E> {

    /** The span. */
    private final Span span;

    /** The key extractor. */
    private final ToLongFunction<E> timestampExtractor;

    /** The completed data manager. */
    private final ConcurrentSkipListMap<Long, Segment> indexed = new ConcurrentSkipListMap();

    /** A number of items. */
    private int size;

    /**
     * 
     */
    public SegmentBuffer(Span span, ToLongFunction<E> timestampExtractor) {
        this.span = Objects.requireNonNull(span);
        this.timestampExtractor = Objects.requireNonNull(timestampExtractor);
    }

    /**
     * Convert timestamp (epoch seconds) to timeindex (start epoch time of day).
     * 
     * @param timestamp
     * @return
     */
    long[] index(long timestamp) {
        long remainder = timestamp % 86400; // 60 * 60 * 24 //
        return new long[] {timestamp - remainder, remainder / span.seconds};
    }

    /**
     * Return the size of this {@link SegmentBuffer}.
     * 
     * @return A positive size or zero.
     */
    public int size() {
        return size;
    }

    /**
     * Check whether this {@link SegmentBuffer} is empty or not.
     * 
     * @return
     */
    public boolean isEmpty() {
        return indexed.isEmpty();
    }

    /**
     * Check whether this {@link SegmentBuffer} is empty or not.
     * 
     * @return
     */
    public boolean isNotEmpty() {
        return !indexed.isEmpty();
    }

    /**
     * Add realtime item.
     * 
     * @param positions An items to add.
     */
    public void add(E item) {
        long[] index = index(timestampExtractor.applyAsLong(item));

        Segment segment = indexed.get(index[0]);

        if (segment == null) {
            segment = new OnHeap();
            indexed.put(index[0], segment);
        }
        size += segment.set((int) index[1], item);
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
        for (Segment segment : indexed.values()) {
            if (index < segment.size()) {
                return segment.get(index);
            }
            index -= segment.size();
        }
        return null;
    }

    /**
     * Get the item for the specified timestamp (epoch seconds).
     * 
     * @param timestamp A time stamp.
     * @return
     */
    public E at(long timestamp) {
        long[] index = index(timestamp);

        Segment segment = indexed.get(index[0]);

        if (segment == null) {
            return null;
        }
        return segment.get((int) index[1]);
    }

    /**
     * Get the first item.
     * 
     * @return
     */
    public E first() {
        Entry<Long, Segment> entry = indexed.firstEntry();

        if (entry == null) {
            return null;
        }

        return entry.getValue().fisrt();
    }

    /**
     * Get the last item.
     * 
     * @return
     */
    public E last() {
        Entry<Long, Segment> entry = indexed.lastEntry();

        if (entry == null) {
            return null;
        }

        return entry.getValue().last();
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

        for (Segment segment : indexed.values()) {
            int size = segment.size();
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
     * Signal all items from start to end.
     * 
     * @param start A start timestamp (included).
     * @param end A end timestamp (included).
     * @param each An item processor.
     */
    public void eachAt(long start, long end, Consumer<? super E> each) {
        if (end < start) {
            throw new IndexOutOfBoundsException("Start[" + start + "] must be less than end[" + end + "].");
        }

        long[] startIndex = index(start);
        long[] endIndex = index(end);

        ConcurrentNavigableMap<Long, Segment> sub = indexed.subMap(startIndex[0], true, endIndex[0], true);
        Iterator<Segment> iterator = sub.values().iterator();
        boolean first = true;

        if (iterator.hasNext()) {
            // first
            while (iterator.hasNext()) {
                Segment next = iterator.next();

                if (iterator.hasNext()) {
                    if (first) {
                        // first
                        first = false;
                        next.eachAt((int) startIndex[1], each);
                    } else {
                        // middle
                        next.eachAt(0, each);
                    }
                } else {
                    if (first) {
                        first = false;
                        next.eachAt((int) startIndex[1], (int) endIndex[1], each);
                    } else {
                        // last
                        next.eachAt(0, (int) endIndex[1], each);
                    }
                }
            }
        }
    }

    /**
     * Clear all items.
     */
    public void clear() {
        for (Segment segment : indexed.values()) {
            segment.clear();
        }
        indexed.clear();
    }

    /**
     * Item container.
     */
    private abstract class Segment {

        abstract int size();

        /**
         * Retrieve item by index.
         * 
         * @param index An item index.
         * @return An item or null.
         */
        abstract E get(int index);

        /**
         * Set item by index.
         * 
         * @param index An item index.
         * @param item An item to set.
         * @return Adding new item returns 1, updating item returns 0.
         */
        abstract int set(int index, E item);

        abstract void clear();

        /**
         * Retrieve first item in this container.
         * 
         * @return A first item or null.
         */
        abstract E fisrt();

        /**
         * Retrieve last item in this container.
         * 
         * @return A last item or null.
         */
        abstract E last();

        final void each(int start, Consumer<? super E> consumer) {
            each(start, size(), consumer);
        }

        abstract void each(int start, int end, Consumer<? super E> consumer);

        final void eachAt(int start, Consumer<? super E> consumer) {
            eachAt(start, size(), consumer);
        }

        abstract void eachAt(int start, int end, Consumer<? super E> consumer);
    }

    /**
     * On heap container.
     */
    private class OnHeap extends Segment {

        /** The managed items. */
        private E[] items = (E[]) new Object[span.ticksPerDay()];

        /** The first item index. */
        private int min = Integer.MAX_VALUE;

        /** THe last item index. */
        private int max = Integer.MIN_VALUE;

        /**
         * {@inheritDoc}
         */
        @Override
        int size() {
            return max < 0 ? 0 : max - min + 1;
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
        int set(int index, E item) {
            boolean added = items[index] == null;
            items[index] = item;

            // FAILSAFE : update min and max index after inserting item
            min = Math.min(min, index);
            max = Math.max(max, index);

            return added ? 1 : 0;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        void clear() {
            items = null;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        E fisrt() {
            return items[min];
        }

        /**
         * {@inheritDoc}
         */
        @Override
        E last() {
            return items[max];
        }

        /**
         * {@inheritDoc}
         */
        @Override
        void each(int start, int end, Consumer<? super E> consumer) {
            for (int i = min + start; i < min + end; i++) {
                consumer.accept(items[i]);
            }
        }

        /**
         * {@inheritDoc}
         */
        @Override
        void eachAt(int start, int end, Consumer<? super E> consumer) {
            start = Math.max(min, start);
            end = Math.min(max, end);

            for (int i = start; i <= end; i++) {
                consumer.accept(items[i]);
            }
        }
    }
}
