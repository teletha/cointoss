/*
 * Copyright (C) 2020 cointoss Development Team
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          https://opensource.org/licenses/MIT
 */
package cointoss.ticker;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.concurrent.ConcurrentNavigableMap;
import java.util.concurrent.ConcurrentSkipListMap;
import java.util.function.Consumer;
import java.util.function.ToLongFunction;

import cointoss.util.Chrono;

public final class TimeseriesStore<E> {

    /** The span. */
    private final Span span;

    /** The item length. */
    private final int length;

    /** The key extractor. */
    private final ToLongFunction<E> timestampExtractor;

    /** The completed data manager. */
    private final ConcurrentSkipListMap<Long, Segment> indexed = new ConcurrentSkipListMap();

    /** A number of items. */
    private int size;

    /** The maximum item size on heap. */
    private int maximumHeapItem;

    @SuppressWarnings("serial")
    private final Map<Long, Segment> stats = new LinkedHashMap<>(8, 0.75f, true) {

        @Override
        protected boolean removeEldestEntry(Entry<Long, Segment> eldest) {
            if (maximumHeapItem < size()) {
                persist(eldest.getKey(), eldest.getValue());
                return true;
            } else {
                return false;
            }
        }
    };

    /**
     * 
     */
    public TimeseriesStore(Span span, ToLongFunction<E> timestampExtractor) {
        this.span = Objects.requireNonNull(span);
        this.length = span.ticksPerDay();
        this.timestampExtractor = Objects.requireNonNull(timestampExtractor);
        this.maximumHeapItem = switch (span) {
            case Second5 -> 2;
            case Minute1 -> 3;
            case Minute5 -> 5;
            case Minute15 -> 10;
            case Minute30 -> 20;
            case Hour1 -> 40;
            case Hour2 -> 80;
            case Hour4 -> 160;
            case Hour6 -> 240;
            case Day1 -> 960;
            default -> Integer.MAX_VALUE;
        };
    }

    /**
     * Persiste data from heap to disk.
     * 
     * @param time
     * @param segment
     */
    private void persist(long time, Segment segment) {
        System.out.println("Persist data [" + span + "]  " + Chrono.utcBySeconds(time));
        indexed.remove(time);
        segment.clear();
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
     * Return the size of this {@link TimeseriesStore}.
     * 
     * @return A positive size or zero.
     */
    public int size() {
        return size;
    }

    /**
     * Check whether this {@link TimeseriesStore} is empty or not.
     * 
     * @return Result.
     */
    public boolean isEmpty() {
        return indexed.isEmpty();
    }

    /**
     * Check whether this {@link TimeseriesStore} is empty or not.
     * 
     * @return Result.
     */
    public boolean isNotEmpty() {
        return !indexed.isEmpty();
    }

    /**
     * Stores the specified time series item.
     * 
     * @param item Time series items to store.
     */
    public void store(E item) {
        long[] index = index(timestampExtractor.applyAsLong(item));

        Segment segment = indexed.get(index[0]);

        if (segment == null) {
            segment = new OnHeap();
            indexed.put(index[0], segment);
            stats.put(index[0], segment);
        }
        size += segment.set((int) index[1], item);
    }

    /**
     * Stores the specified time series items.
     * 
     * @param items Time series items to store.
     */
    public void store(E... items) {
        for (E item : items) {
            store(item);
        }
    }

    /**
     * Get the item for the specified timestamp (epoch seconds).
     * 
     * @param timestamp A time stamp.
     * @return
     */
    public E at(long timestamp) {
        if (timestamp < 0) {
            return null;
        }

        long[] index = index(timestamp);

        Segment segment = indexed.get(index[0]);

        if (segment == null) {
            return null;
        }
        return segment.get((int) index[1]);
    }

    /**
     * Get the first stored time series item.
     * 
     * @return The first stored time series item.
     */
    public E first() {
        Entry<Long, Segment> entry = indexed.firstEntry();

        if (entry == null) {
            return null;
        }

        return entry.getValue().first();
    }

    /**
     * Get the last stored time series item.
     * 
     * @return The last stored time series item.
     */
    public E last() {
        Entry<Long, Segment> entry = indexed.lastEntry();

        if (entry == null) {
            return null;
        }

        return entry.getValue().last();
    }

    /**
     * Get all stored time series items in ascending order.
     * 
     * @param each An item processor.
     */
    public void each(Consumer<? super E> each) {
        for (Segment segment : indexed.values()) {
            segment.each(0, length, each);
        }
    }

    /**
     * Get the time series items stored from the specified start time to end time in ascending
     * order.
     * 
     * @param start A start time (included).
     * @param end A end time (included).
     * @param each An item processor.
     */
    public void each(long start, long end, Consumer<? super E> each) {
        if (end < start) {
            return;
        }

        long[] startIndex = index(start);
        long[] endIndex = index(end);
        ConcurrentNavigableMap<Long, Segment> sub = indexed.subMap(startIndex[0], true, endIndex[0], true);

        for (Entry<Long, Segment> entry : sub.entrySet()) {
            long time = entry.getKey();
            int startItemIndex = 0;
            int endItemIndex = length;

            if (time == startIndex[0]) {
                startItemIndex = (int) startIndex[1];
            }
            if (time == endIndex[0]) {
                endItemIndex = (int) endIndex[1];
            }
            entry.getValue().each(startItemIndex, endItemIndex, each);
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
     * Get the item just before the specified item.
     * 
     * @param item An indexable item.
     * @return
     */
    public E before(E item) {
        return before(timestampExtractor.applyAsLong(item));
    }

    /**
     * Get the item just before the specified timestamp (epoch seconds).
     * 
     * @param timestamp A time stamp.
     * @return
     */
    public E before(long timestamp) {
        return at(timestamp - span.seconds);
    }

    /**
     * Get the specified number of items before the specified item.
     * 
     * @param item An indexable item.
     * @return
     */
    public List<E> before(E item, int maximumSize) {
        return before(timestampExtractor.applyAsLong(item), maximumSize);
    }

    /**
     * Get the specified number of items before the specified timestamp (epoch seconds).
     * 
     * @param timestamp A time stamp.
     * @return
     */
    public List<E> before(long timestamp, int maximumSize) {
        return before(timestamp, maximumSize, false);
    }

    /**
     * Get the specified number of items before the specified item.
     * 
     * @param item An indexable item.
     * @return
     */
    public List<E> beforeWith(E item, int maximumSize) {
        return beforeWith(timestampExtractor.applyAsLong(item), maximumSize);
    }

    /**
     * Get the specified number of items before the specified timestamp (epoch seconds).
     * 
     * @param timestamp A time stamp.
     * @return
     */
    public List<E> beforeWith(long timestamp, int maximumSize) {
        return before(timestamp, maximumSize, true);
    }

    /**
     * Get the specified number of items before the specified timestamp (epoch seconds).
     * 
     * @param timestamp A time stamp.
     * @return
     */
    private List<E> before(long timestamp, int maximumSize, boolean with) {
        List<E> items = new ArrayList();

        long[] index = index(timestamp);
        long timeIndex = index[0];
        int segmentIndex = ((int) index[1]);
        Segment segment = indexed.get(timeIndex);

        if (with) {
            E item = segment.get(segmentIndex);
            if (item != null) {
                items.add(item);
            }
        }

        while (items.size() < maximumSize) {
            if (--segmentIndex == -1) {
                timeIndex -= 86400; // 60x60x24
                segment = indexed.get(timeIndex);

                if (segment == null) {
                    break;
                } else {
                    segmentIndex = length - 1;
                }
            }

            E item = segment.get(segmentIndex);
            if (item != null) {
                items.add(item);
            }
        }

        return items;
    }

    /**
     * Item container.
     */
    private abstract class Segment {
        /**
         * Return the size of this {@link Segment}.
         * 
         * @return A positive size or zero.
         */
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

        /**
         * Clear data.
         */
        abstract void clear();

        /**
         * Retrieve first item in this container.
         * 
         * @return A first item or null.
         */
        abstract E first();

        /**
         * Retrieve last item in this container.
         * 
         * @return A last item or null.
         */
        abstract E last();

        /**
         * Get the time series items stored from the specified start index (inclusive) to end index
         * (exclusive) in ascending order.
         * 
         * @param start A start index (included).
         * @param end A end index (exclusive).
         * @param each An item processor.
         */
        abstract void each(int start, int end, Consumer<? super E> each);
    }

    /**
     * On heap container.
     */
    private class OnHeap extends Segment {

        /** The managed items. */
        private E[] items = (E[]) new Object[length];

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
        E first() {
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
            start = Math.max(min, start);
            end = Math.min(max, end);

            for (int i = start; i <= end; i++) {
                consumer.accept(items[i]);
            }
        }
    }

    /**
     * On disk container.
     */
    private class OnDisk extends Segment {

        private Segment heap;

        private int size;

        private void load() {

        }

        /**
         * {@inheritDoc}
         */
        @Override
        int size() {
            return size;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        E get(int index) {
            return null;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        int set(int index, E item) {
            return 0;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        void clear() {
        }

        /**
         * {@inheritDoc}
         */
        @Override
        E first() {
            return null;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        E last() {
            return null;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        void each(int start, int end, Consumer<? super E> each) {
        }
    }
}