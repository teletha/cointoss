/*
 * Copyright (C) 2019 Nameless Production Committee
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          http://opensource.org/licenses/mit-license.php
 */
package cointoss.ticker;

import java.util.Map.Entry;
import java.util.Objects;
import java.util.concurrent.ConcurrentSkipListMap;
import java.util.function.ToLongFunction;

public class TimelineBuffer<E> {

    /** The buffer span. */
    private final Span span;

    /** The timestamp extractor. */
    private final ToLongFunction<E> timestampExtractor;

    /** The item manager. */
    private final ConcurrentSkipListMap<Long, Cached> indexed = new ConcurrentSkipListMap();

    /** A number of items. */
    private int size;

    /**
     * 
     */
    public TimelineBuffer(Span span, ToLongFunction<E> timestampExtractor) {
        this.span = Objects.requireNonNull(span);
        this.timestampExtractor = Objects.requireNonNull(timestampExtractor);
    }

    /**
     * @return
     */
    public boolean isEmpty() {
        return indexed.isEmpty();
    }

    public boolean isNotEmpty() {
        return !indexed.isEmpty();
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

    public void add(E item) {
        long[] index = index(timestampExtractor.applyAsLong(item));

        Cached cached = indexed.get(index[0]);

        if (cached == null) {
            cached = new InMemory();
            indexed.put(index[0], cached);
        }
        size += cached.set((int) index[1], item);
    }

    /**
     * Get the item for the specified timestamp (epoch seconds).
     * 
     * @param timestamp
     * @return
     */
    public E get(long timestamp) {
        long[] index = index(timestamp);

        Cached cached = indexed.get(index[0]);

        if (cached == null) {
            return null;
        }
        return cached.get((int) index[1]);
    }

    /**
     * Get the first item.
     * 
     * @return
     */
    public E first() {
        Entry<Long, TimelineBuffer<E>.Cached> entry = indexed.firstEntry();

        if (entry == null) {
            return null;
        }

        return entry.getValue().fisrt();
    }

    /**
     * Get the first item.
     * 
     * @return
     */
    public E last() {
        Entry<Long, TimelineBuffer<E>.Cached> entry = indexed.lastEntry();

        if (entry == null) {
            return null;
        }

        return entry.getValue().last();
    }

    /**
     * Clear all items.
     */
    public void clear() {
        for (TimelineBuffer<E>.Cached cached : indexed.values()) {
            cached.clear();
        }
        indexed.clear();
    }

    /**
     * Return a number of items.
     * 
     * @return
     */
    public int size() {
        return size;
    }

    /**
     * 
     */
    private abstract class Cached {

        abstract E get(int index);

        abstract int set(int index, E item);

        abstract void clear();

        abstract E fisrt();

        abstract E last();
    }

    /**
     * 
     */
    private class InMemory extends Cached {

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
    }
}
