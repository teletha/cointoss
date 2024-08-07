/*
 * Copyright (C) 2024 The COINTOSS Development Team
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          https://opensource.org/licenses/MIT
 */
package cointoss.util.feather;

import java.lang.reflect.Array;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Map.Entry;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Predicate;

import com.google.common.annotations.VisibleForTesting;

import cointoss.ticker.Span;
import kiss.Disposable;
import kiss.Model;
import kiss.Signal;
import primavera.map.ConcurrentNavigableLongMap;
import primavera.map.LongMap;
import primavera.map.LongMap.LongEntry;
import typewriter.api.model.IdentifiableModel;
import typewriter.rdb.RDB;

public final class FeatherStore<E extends IdentifiableModel & Timelinable> implements Disposable {

    /** The initial value. */
    private static final long FIRST_INIT = Long.MAX_VALUE;

    /** The initial value. */
    private static final long LAST_INIT = 0;

    /** The item type. */
    private final Model<E> model;

    /** The duration of item. */
    private final long itemDuration;

    /** The size of item. */
    private final int itemSize;

    /** The size of segment. */
    private final int segmentSize;

    /** The duration of segmenet. */
    private final long segmentDuration;

    /** The completed data manager. */
    private final ConcurrentNavigableLongMap<OnHeap<E>> indexed = LongMap.createSortedMap();

    /** The eviction policy. */
    private EvictionPolicy eviction;

    /** The data accumulator. */
    private BiFunction<E, E, E> accumulator;

    /** The data interpolation. */
    private int interpolation;

    private long firstHeap = FIRST_INIT;

    /** The date-time of last item on heap. */
    private long lastHeap = LAST_INIT;

    /** The date-time of first item on disk. */
    private long firstDisk = FIRST_INIT;

    /** The date-time of last item on disk. */
    private long lastDisk = LAST_INIT;

    /** The disk store. */
    private RDB<E> db;

    /**
     * Create the store for timeseries data.
     * 
     * @param <E>
     * @param type
     * @param span
     * @return
     */
    public static <E extends IdentifiableModel & Timelinable> FeatherStore<E> create(Class<E> type, Span span) {
        return new FeatherStore<E>(type, span.seconds, (int) (span.segmentSeconds / span.seconds), span.segmentSize);
    }

    /**
     * Create the store for timeseries data.
     * 
     * @param <E>
     * @param type
     * @return
     */
    public static <E extends IdentifiableModel & Timelinable> FeatherStore<E> create(Class<E> type, long itemDuration, int itemSize, int segmentSize) {
        return new FeatherStore<E>(type, itemDuration, itemSize, segmentSize);
    }

    /**
     * 
     */
    private FeatherStore(Class<E> type, long itemDuration, int itemSize, int segmentSize) {
        this.model = Model.of(type);
        this.itemDuration = itemDuration;
        this.itemSize = itemSize;
        this.segmentSize = segmentSize;
        this.segmentDuration = itemDuration * itemSize;
        this.eviction = EvictionPolicy.byLRU(segmentSize);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void vandalize() {
    }

    /**
     * Enable the transparent disk persistence.
     * 
     * @return Chainable API.
     */
    public synchronized FeatherStore<E> enablePersistence(Object... qualifers) {
        db = RDB.of(model.type, qualifers);
        firstDisk = firstDiskTime();
        lastDisk = lastDiskTime();
        return this;
    }

    /**
     * Disable the automatic memory saving.
     * 
     * @return Chainable API.
     */
    public synchronized FeatherStore<E> disableMemoryCompaction() {
        eviction = EvictionPolicy.never();
        return this;
    }

    /**
     * Enable data accumulator.
     * 
     * @param accumulator
     * @return Chainable API.
     */
    public FeatherStore<E> enableAccumulator(BiFunction<E, E, E> accumulator) {
        this.accumulator = accumulator;
        return this;
    }

    /**
     * Enable empty data interpolation.
     * 
     * @return Chainable API.
     */
    public FeatherStore<E> enableInterpolation(int range) {
        this.interpolation = range;
        return this;
    }

    /**
     * Disable data accumulator.
     * 
     * @return Chainable API.
     */
    public FeatherStore<E> disableAccumulator() {
        this.accumulator = null;
        return this;
    }

    /**
     * Convert timestamp (epoch seconds) to timeindex (start epoch time of day).
     * 
     * @param timestamp
     * @return
     */
    @VisibleForTesting
    long[] index(long timestamp) {
        long remainder = timestamp % segmentDuration;
        return new long[] {timestamp - remainder, remainder / itemDuration};
    }

    /**
     * Return the size of this {@link FeatherStore}.
     * 
     * @return A positive size or zero.
     */
    public int size() {
        return indexed.values().stream().mapToInt(OnHeap::size).sum();
    }

    /**
     * Return the size of the segments.
     * 
     * @return
     */
    public int segmentSize() {
        return indexed.size();
    }

    /**
     * Check whether this {@link FeatherStore} is empty or not.
     * 
     * @return Result.
     */
    public boolean isEmpty() {
        return indexed.isEmpty();
    }

    /**
     * Check whether this {@link FeatherStore} is empty or not.
     * 
     * @return Result.
     */
    public boolean isNotEmpty() {
        return !indexed.isEmpty();
    }

    /**
     * Test whether the heap are filled or not.
     * 
     * @return
     */
    public boolean isFilled() {
        return segmentSize <= indexed.size();
    }

    /**
     * Test wheterh the persistence is active or not.
     * 
     * @return
     */
    public boolean isPersistable() {
        return db != null;
    }

    /**
     * Stores the specified time series item.
     * 
     * @param item Time series items to store.
     */
    public void store(E item) {
        if (item == null) {
            return;
        }

        long time = item.seconds();
        long[] index = index(time);

        OnHeap<E> segment = supply(index[0]);
        boolean add = false;

        if (segment == null) {
            add = true;
            segment = new OnHeap(model, index[0], itemSize);
            tryEvict(index[0]);
        }

        if (accumulator == null) {
            segment.set((int) index[1], item);
        } else {
            E previous = segment.get((int) index[1]);
            segment.set((int) index[1], previous == null ? item : accumulator.apply(previous, item));
        }

        if (add) {
            indexed.put(index[0], segment);
        }

        // update managed time
        if (time < firstHeap) {
            firstHeap = time;
        }
        if (lastHeap < time) {
            lastHeap = time;
        }
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
     * Stores the specified time series items.
     * 
     * @param items Time series items to store.
     */
    public void store(List<E> items) {
        for (E item : items) {
            store(item);
        }
    }

    /**
     * Stores the specified time series items.
     * 
     * @param items Time series items to store.
     */
    public void store(Signal<E> items) {
        items.to(e -> store(e));
    }

    /**
     * Import time series items from other store.
     * 
     * @param other Time series store.
     */
    public void merge(FeatherStore<E> other) {
        if (model != other.model) {
            throw new IllegalArgumentException("Invalid models [" + model.type + "] and [" + other.model.type + "]");
        }

        if (itemDuration != other.itemDuration) {
            throw new IllegalArgumentException("Different time span [" + itemDuration + "] and [" + other.itemDuration + "]");
        }

        for (LongEntry<OnHeap<E>> entry : other.indexed.longEntrySet()) {
            tryEvict(entry.getLongKey());

            OnHeap<E> merged = indexed.get(entry.getLongKey());
            if (merged == null) {
                indexed.put(entry.getLongKey(), entry.getValue());
            } else {
                OnHeap<E> merging = entry.getValue();
                for (int i = 0; i < merging.items.length; i++) {
                    E e = merging.items[i];
                    if (e != null) {
                        merged.set(i, e);
                    }
                }
            }
        }

        if (firstHeap == Long.MAX_VALUE || other.firstHeap < firstHeap) {
            firstHeap = other.firstHeap;
        }
        if (lastHeap == 0 || lastHeap < other.lastHeap) {
            lastHeap = other.lastHeap;
        }
        other.indexed.clear();
    }

    /**
     * Get the item for the specified timestamp (epoch seconds).
     * 
     * @param timestamp A time stamp.
     * @return
     */
    public E at(long timestamp) {
        if (timestamp < 0 || timestamp < firstIdealCacheTime()) {
            return null;
        }

        long[] index = index(timestamp);
        OnHeap<E> segment = supply(index[0]);
        if (segment == null) {
            return null;
        }

        E item = segment.get((int) index[1]);
        if (item == null) {
            if (0 < interpolation) {
                for (int i = (int) index[1] - 1, j = interpolation; 0 <= i && 0 < j && item == null; i--, j--) {
                    item = segment.get(i);
                }
            }
        }
        return item;
    }

    /**
     * Get the date and time of the first element from all stored data, including secondary cache.
     * 
     * @return
     * @see #first()
     */
    public long firstTime() {
        if (firstHeap == FIRST_INIT) {
            if (firstDisk == FIRST_INIT) {
                return -1;
            } else {
                return firstDisk;
            }
        } else {
            if (firstDisk == FIRST_INIT) {
                return firstHeap;
            } else {
                return firstHeap < firstDisk ? firstHeap : firstDisk;
            }
        }
    }

    /**
     * Get the date and time of the last element from all stored data, including secondary cache.
     * 
     * @return
     * @see #last()
     */
    public long lastTime() {
        if (lastHeap == LAST_INIT) {
            if (lastDisk == LAST_INIT) {
                return -1;
            } else {
                return lastDisk;
            }
        } else {
            if (lastDisk == LAST_INIT) {
                return lastHeap;
            } else {
                return lastHeap < lastDisk ? lastDisk : lastHeap;
            }
        }
    }

    /**
     * Retrieves the time of the first element from the memory cache only.
     * 
     * @return
     */
    public long firstCacheTime() {
        return firstHeap == FIRST_INIT ? -1 : firstHeap;
    }

    /**
     * Retrieves the time of the last element from the memory cache only.
     * 
     * @return
     */
    public long lastCacheTime() {
        return lastHeap == LAST_INIT ? -1 : lastHeap;
    }

    /**
     * Compute the first segment time logically in the current state.
     * 
     * @return
     */
    public long firstIdealCacheTime() {
        if (indexed.isEmpty()) {
            return -1;
        }

        return indexed.lastLongKey() - segmentDuration * (segmentSize - 1);
    }

    /**
     * Compute the time of first item on disk.
     * 
     * @return
     */
    private long firstDiskTime() {
        return db == null ? FIRST_INIT : db.min(E::getId);
    }

    /**
     * Compute the time of last item on disk.
     * 
     * @return
     */
    private long lastDiskTime() {
        return db == null ? LAST_INIT : db.max(E::getId);
    }

    /**
     * Retrieve the first element from all stored data, including secondary cache.
     * 
     * @return The first stored time series item.
     * @see #firstTime()
     */
    public E first() {
        return at(firstTime());
    }

    /**
     * Retrieve the last element from all stored data, including secondary cache.
     * 
     * @return The last stored time series item.
     * @see #lastTime()
     */
    public E last() {
        return at(lastTime());
    }

    /**
     * Retrieve the item at {@link #firstCacheTime()}.
     * 
     * @return
     */
    public E firstCache() {
        E item = at(firstCacheTime());

        if (item == null) {
            System.out.println("first null " + Instant.ofEpochSecond(firstHeap) + "  " + this);
        }
        return item;
    }

    /**
     * Retrieve the item at {@link #lastCacheTime()}.
     * 
     * @return
     */
    public E lastCache() {
        E item = at(lastCacheTime());

        if (item == null) {
            System.out.println("last null " + Instant.ofEpochSecond(lastHeap) + "  " + this);
        }
        return item;
    }

    /**
     * Retrieve the item at {@link #firstIdealCacheTime()}.
     * 
     * @return
     */
    public E firstIdealCache() {
        return at(firstIdealCacheTime());
    }

    /**
     * Clear all items from heap.
     */
    public void clearHeap() {
        for (OnHeap segment : indexed.values()) {
            segment.clear();
        }
        indexed.clear();

        firstHeap = FIRST_INIT;
        lastHeap = LAST_INIT;
    }

    /**
     * Get the item just before the specified item.
     * 
     * @param item An indexable item.
     * @return
     */
    public E before(E item) {
        return before(item.seconds());
    }

    /**
     * Get the item just before the specified timestamp (epoch seconds).
     * 
     * @param timestamp A time stamp.
     * @return
     */
    public E before(long timestamp) {
        return at(timestamp - itemDuration);
    }

    /**
     * Get the most recent item that matches the conditions before the indexable item.
     * 
     * @param item An indexable item.
     * @return A matched item or null.
     */
    public E before(E item, Predicate<E> condition) {
        return before(item.seconds(), condition);
    }

    /**
     * Get the most recent item that matches the conditions before the indexable item.
     * 
     * @return A matched item or null.
     */
    public E before(long timestamp, Predicate<E> condition) {
        for (int i = 1; i < Integer.MAX_VALUE; i++) {
            E item = at(timestamp - itemDuration * i);

            if (item == null) {
                break;
            }

            if (condition.test(item)) {
                return item;
            }
        }
        return null;
    }

    /**
     * Query items by temporal options.
     * 
     * @param start A starting time.
     * @param option Your options.
     * @return A result.
     */
    public Signal<E> query(long start, Consumer<Option>... option) {
        return query(start, -1, option);
    }

    /**
     * Query items by temporal options.
     * 
     * @param start A starting time.
     * @param end A ending time.
     * @param option Your options.
     * @return A result.
     */
    public Signal<E> query(long start, long end, Consumer<Option>... option) {
        if (start < 0) {
            throw new IllegalArgumentException("Start time must be position.");
        }

        if (end < -1) {
            throw new IllegalArgumentException("End time must be position.");
        }

        // configure options
        Option o = new Option();
        for (Consumer<Option> x : option) {
            x.accept(o);
        }

        return new Signal<>((observer, disposer) -> {
            long s = start;
            long e = end;
            long first = firstTime();
            long last = lastTime();

            if (s == Option.Latest) {
                s = last;
            }

            if (e == -1) {
                if (o.forward) {
                    e = Math.max(s, last);
                } else {
                    e = s;
                    s = 0;
                }
            }

            boolean forward = s < e;
            long[] startIndex = index(forward ? s : e);
            long[] endIndex = index(forward ? e : s);
            forward = o.forward == forward;

            if (o.excludeStart) {
                if (forward) {
                    startIndex[1] += 1;
                } else {
                    if (e <= last) {
                        endIndex[1] -= 1;
                    }
                }
            }

            // Retrieves the segments that exist on heap or disk in the specified order.
            // If the specified time is out of the range of the actual data time, you can shrink it
            // to within the range of the actual data.
            long segmentStart = Math.max(startIndex[0], index(first)[0]);
            long segmentEnd = Math.min(endIndex[0], index(last)[0]);
            long ideal = firstIdealCacheTime();
            int remaining = o.max;

            if (forward) {
                for (long time = segmentStart; time <= segmentEnd && 0 < remaining && !disposer.isDisposed(); time += segmentDuration) {
                    OnHeap<E> heap = supply(time);
                    if (heap != null) {
                        int open = heap.startTime == segmentStart ? (int) startIndex[1] : 0;
                        int close = heap.startTime == segmentEnd ? (int) endIndex[1] : itemSize;
                        remaining = heap.each(open, close, forward, remaining, observer, disposer);
                    }
                }
            } else {
                for (long time = segmentEnd; segmentStart <= time && 0 < remaining && !disposer
                        .isDisposed() && (!o.safe || ideal < time); time -= segmentDuration) {
                    OnHeap<E> heap = supply(time);
                    if (heap != null) {
                        int open = heap.startTime == segmentStart ? (int) startIndex[1] : 0;
                        int close = heap.startTime == segmentEnd ? (int) endIndex[1] : itemSize;
                        remaining = heap.each(open, close, forward, remaining, observer, disposer);
                    }
                }
            }
            observer.complete();

            return disposer;
        });
    }

    /**
     * Query items by temporal options.
     * 
     * @param start A starting time.
     * @param option Your options.
     * @return A result.
     */
    public Signal<E> query(Timelinable start, Consumer<Option>... option) {
        return query(start.seconds(), option);
    }

    /**
     * Query items by temporal options.
     * 
     * @param start A starting time.
     * @param end A ending time.
     * @param option Your options.
     * @return A result.
     */
    public Signal<E> query(Timelinable start, Timelinable end, Consumer<Option>... option) {
        return query(start.seconds(), end.seconds(), option);
    }

    /**
     * Query the latest items with temporal options.
     * 
     * @param option Your options.
     * @return A result.
     */
    public Signal<E> queryLatest(Consumer<Option>... option) {
        return query(Option.Latest, 0, option);
    }

    /**
     * Get the data segment at the specified date and time.
     * 
     * @param startTime
     * @return
     */
    private OnHeap<E> supply(long startTime) {
        // Memory Cache
        OnHeap<E> segment = indexed.get(startTime);
        if (segment != null) {
            return segment;
        }

        // Disk Cache
        if (db != null) {
            OnHeap<E> heap = new OnHeap(model, startTime, itemSize);
            db.findBy(E::getId, x -> x.isOrMoreThan(startTime).isLessThan(startTime + itemSize * itemDuration)).to(item -> {
                long[] index = index(item.seconds());
                heap.set((int) index[1], item);
            });

            tryEvict(startTime);
            indexed.put(startTime, heap);

            if (segmentDuration == Span.Minute5.segmentSeconds) {
                System.out.println("Restore " + this);
            }
            return heap;
        }

        // Not Found
        return null;
    }

    /**
     * Forcibly saves all data that currently exists on the heap to disk immediately. All data on
     * disk will be overwritten. If the disk store is not enabled, nothing will happen.
     */
    public void commit() {
        if (db != null) {
            for (Entry<Long, OnHeap<E>> entry : indexed.entrySet()) {
                OnHeap<E> value = entry.getValue();
                db.updateAll(value.items);
            }
            firstDisk = firstDiskTime();
            lastDisk = lastDiskTime();
        }
    }

    /**
     * Restore data from disk.
     */
    public void restore(long start, long end) {
        if (db != null) {
            db.findBy(E::getId, x -> x.isOrMoreThan(start).isOrLessThan(end)).to(x -> {
                store(x);
            });
        }
    }

    /**
     * Try to evict segment for memory compaction.
     * 
     * @param time
     */
    private void tryEvict(long time) {
        long evictableTime = eviction.access(time);
        if (evictableTime != -1 && isFilled()) {
            OnHeap<E> segment = indexed.remove(evictableTime);

            if (segment != null) {
                if (db != null) {
                    db.updateAll(segment.items);
                }

                if (segmentDuration == Span.Minute5.segmentSeconds) {
                    System.out.println("Evicted by " + Instant.ofEpochSecond(time) + "  " + Instant
                            .ofEpochSecond(segment.startTime) + "  " + this);

                    new Error().printStackTrace();
                }

                segment.clear();
            }

            // if (evictableTime <= first) {
            // OnHeap<E> heap = indexed.firstValue();
            // first = heap == null ? Long.MAX_VALUE : heap.first().seconds();
            // }
            // if (last < evictableTime + segmentDuration) {
            // OnHeap<E> heap = indexed.lastValue();
            // last = heap == null ? 0 : heap.last().seconds();
            // }
            if (evictableTime <= firstHeap) {
                OnHeap<E> heap = indexed.firstValue();
                firstHeap = heap == null ? Long.MAX_VALUE : heap.first().seconds();
            }
            if (lastHeap < evictableTime + segmentDuration) {
                OnHeap<E> heap = indexed.lastValue();
                lastHeap = heap == null ? 0 : heap.last().seconds();
            }
        }
    }

    /**
     * For test.
     * 
     * @param item
     * @return
     */
    @VisibleForTesting
    boolean existOnHeap(E item) {
        long[] index = index(item.seconds());
        OnHeap segment = indexed.get(index[0]);
        if (segment == null) {
            return false;
        }
        return item.equals(segment.get((int) index[1]));
    }

    /**
     * For test.
     * 
     * @param item
     * @return
     */
    @VisibleForTesting
    boolean existOnDisk(E item) {
        if (db == null) {
            return false;
        }
        return db.findBy(item.getId()).to().is(item);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return new StringBuilder("FeatherStore [").append(Duration.ofSeconds(itemDuration))
                .append("  segment: " + segmentSize() + "/" + segmentSize)
                .append("  item: " + size() + "/" + (itemSize * segmentSize))
                .append("  first: " + (0 <= firstTime() ? Instant.ofEpochSecond(Math.min(firstTime(), 31556889864403199L)) : "NoData"))
                .append("  last: " + (0 <= lastTime() ? Instant.ofEpochSecond(lastTime()) : "NoData"))
                .append("  keys: " + indexed.entrySet()
                        .stream()
                        .map(e -> Instant.ofEpochSecond(e.getKey()) + "(" + e.getValue().size() + ")")
                        .toList())
                .append("]")
                .toString();
    }

    /**
     * On-Heap data container.
     */
    private static class OnHeap<T> {

        private final long startTime;

        /** The managed items. */
        private T[] items;

        /** The first item index. */
        private int min = Integer.MAX_VALUE;

        /** The last item index. */
        private int max = Integer.MIN_VALUE;

        /** Flag whether the data is in sync with disk. */
        private boolean sync;

        /**
         * @param startTime The starting time (epoch seconds).
         */
        private OnHeap(Model<T> model, long startTime, int size) {
            this.startTime = startTime;
            this.items = (T[]) Array.newInstance(model.type, size);
        }

        /**
         * Check empty segment.
         * 
         * @return A positive size or zero.
         */
        boolean isEmpty() {
            return max < 0;
        }

        /**
         * Return the size of this {@link OnHeap}.
         * 
         * @return A positive size or zero.
         */
        int size() {
            return max < 0 ? 0 : max - min + 1;
        }

        /**
         * Retrieve item by index.
         * 
         * @param index An item index.
         * @return An item or null.
         */
        T get(int index) {
            return items == null ? null : items[index];
        }

        /**
         * Set item by index.
         * 
         * @param index An item index.
         * @param item An item to set.
         */
        void set(int index, T item) {
            items[index] = item;

            // FAILSAFE : update min and max index after inserting item
            if (index < min) min = index;
            if (max < index) max = index;

            sync = false;
        }

        /**
         * Clear data.
         */
        void clear() {
            items = null;
            sync = false;
        }

        /**
         * Retrieve first item in this container.
         * 
         * @return A first item or null.
         */
        T first() {
            return items == null || min < 0 ? null : items[min];
        }

        /**
         * Retrieve last item in this container.
         * 
         * @return A last item or null.
         */
        T last() {
            return items == null || max < 0 ? null : items[max];
        }

        /**
         * Get the time series items stored from the specified start index (inclusive) to end index
         * (inclusive) in ascending order.
         * 
         * @param start A start index (included).
         * @param end A end index (exclusive).
         * @param forward A iteration order.
         * @param each An item processor.
         * @param disposer A iteration stopper.
         */
        private int each(int start, int end, boolean forward, int size, Consumer<? super T> consumer, Disposable disposer) {
            start = Math.max(min, start);
            end = Math.min(max, end);
            T[] avoidNPE = items; // copy reference to avoid NPE by #clear
            if (avoidNPE != null) {
                if (forward) {
                    for (int i = start; i <= end && 0 < size && !disposer.isDisposed(); i++) {
                        if (avoidNPE[i] != null) {
                            consumer.accept(avoidNPE[i]);
                            size--;
                        }
                    }
                } else {
                    for (int i = end; start <= i && 0 < size && !disposer.isDisposed(); i--) {
                        if (avoidNPE[i] != null) {
                            consumer.accept(avoidNPE[i]);
                            size--;
                        }
                    }
                }
            }
            return size;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public String toString() {
            return "OnHeap[size: " + size() + " min: " + min + " max: " + max + " time: " + Instant.ofEpochSecond(startTime) + "]";
        }
    }
}