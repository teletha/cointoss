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
import java.time.Instant;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.List;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.LongFunction;
import java.util.function.Predicate;

import com.google.common.annotations.VisibleForTesting;

import cointoss.ticker.Span;
import kiss.Disposable;
import kiss.Model;
import kiss.Signal;
import primavera.map.ConcurrentNavigableLongMap;
import primavera.map.LongMap;
import primavera.map.LongMap.LongEntry;
import psychopath.File;

public final class FeatherStore<E extends Timelinable> implements Disposable {

    /** The item type. */
    private final Model<E> model;

    /** The duration of item. */
    private final long itemDuration;

    /** The size of item. */
    private final int itemSize;

    /** The duration of segmenet. */
    private final long segmentDuration;

    /** The completed data manager. */
    private final ConcurrentNavigableLongMap<OnHeap<E>> indexed = LongMap.createSortedMap();

    /** The disk store. */
    private DiskStorage<E> disk;

    /** The eviction policy. */
    private EvictionPolicy eviction;

    /** The data accumulator. */
    private BiFunction<E, E, E> accumulator;

    /** The data interpolation. */
    private int interpolation;

    /** The date-time of first item. */
    private long first = Long.MAX_VALUE;

    /** The date-time of last item. */
    private long last = 0;

    /** The bulk data source. */
    private LongFunction<Signal<E>> bulk;

    /**
     * Create the store for timeseries data.
     * 
     * @param <E>
     * @param type
     * @param span
     * @return
     */
    public static <E extends Timelinable> FeatherStore<E> create(Class<E> type, Span span) {
        return new FeatherStore<E>(type, span.seconds, (int) (span.segmentSeconds / span.seconds), span.segmentSize);
    }

    /**
     * Create the store for timeseries data.
     * 
     * @param <E>
     * @param type
     * @return
     */
    public static <E extends Timelinable> FeatherStore<E> create(Class<E> type, long itemDuration, int itemSize, int segmentSize) {
        return new FeatherStore<E>(type, itemDuration, itemSize, segmentSize);
    }

    /**
     * 
     */
    private FeatherStore(Class<E> type, long itemDuration, int itemSize, int segmentSize) {
        this.model = Model.of(type);
        this.itemDuration = itemDuration;
        this.itemSize = itemSize;
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
     * Enable the data source.
     * 
     * @param bulk
     * @return Chainable API.
     */
    public FeatherStore<E> enableOnDemandDataSupplier(LongFunction<Signal<E>> bulk) {
        this.bulk = bulk;
        return this;
    }

    /**
     * Enable the active data suppliance.
     * 
     * @param active
     * @return Chainable API.
     */
    public FeatherStore<E> enableActiveDataSupplier(LongFunction<Signal<E>> active) {
        return enableDataSupplier(active, null);
    }

    /**
     * Enable the passive data suppliance.
     * 
     * @param passive
     * @return Chainable API.
     */
    public FeatherStore<E> enablePassiveDataSupplier(Signal<E> passive) {
        return enableDataSupplier(null, passive);
    }

    /**
     * Enable the data suppliance.
     * 
     * @param passive
     * @return Chainable API.
     */
    public FeatherStore<E> enableDataSupplier(LongFunction<Signal<E>> active, Signal<E> passive) {
        if (active != null) {
            startActiveSupplier(active, passive);
        } else {
            startPassiveSupplier(passive);
        }
        return this;
    }

    /**
     * Invoke the active data supplier at start up.
     * 
     * @param active
     * @param passive
     */
    private void startActiveSupplier(LongFunction<Signal<E>> active, Signal<E> passive) {
        long startingLatestTime = lastTime();

        active.apply(last).to(e -> {
            store(e);
        }, error -> {
            startPassiveSupplier(passive);
        }, () -> {
            // If the new data is being retrieved, then the last time before and after the method
            // call is probably different.
            long currentLatestTime = lastTime();

            if (startingLatestTime != currentLatestTime) {
                startActiveSupplier(active, passive);
            } else {
                startPassiveSupplier(passive);
            }
        });
    }

    /**
     * Invoke the passive data supplier at start up.
     * 
     * @param passive
     */
    private void startPassiveSupplier(Signal<E> passive) {
        if (passive != null) {
            add(passive.effectOnDispose(this::commit).to(e -> {
                store(e);
            }));
        }
    }

    /**
     * Enable the transparent disk persistence.
     * 
     * @param databaseFile An actual file to store data.
     * @return Chainable API.
     */
    public synchronized FeatherStore<E> enableDiskStore(File databaseFile) {
        return enableDiskStore(databaseFile, null);
    }

    /**
     * Enable the transparent disk persistence.
     * 
     * @param databaseFile An actual file to store data.
     * @return Chainable API.
     */
    public synchronized FeatherStore<E> enableDiskStore(File databaseFile, DataCodec<E> dataType) {
        if (databaseFile != null && this.disk == null) {
            this.disk = new DiskStorage(databaseFile, dataType != null ? dataType : DataCodec.of(model), itemDuration);
            if (disk.startTime() < first) {
                first = disk.startTime();
            }
            if (last < disk.endTime()) {
                last = disk.endTime();
            }
        }
        return this;
    }

    /**
     * Disable the automatic memory saving.
     * 
     * @return Chainable API.
     */
    public synchronized FeatherStore<E> disableMemorySaving() {
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
     * Stores the specified time series item.
     * 
     * @param item Time series items to store.
     */
    public void store(E item) {
        long time = item.seconds();
        long[] index = index(time);

        OnHeap<E> segment = supply(index[0]);

        if (segment == null) {
            segment = new OnHeap(model, index[0], itemSize);
            tryEvict(index[0]);
            indexed.put(index[0], segment);
        }

        if (accumulator == null) {
            segment.set((int) index[1], item);
        } else {
            E previous = segment.get((int) index[1]);
            segment.set((int) index[1], previous == null ? item : accumulator.apply(previous, item));
        }

        if (itemDuration == Span.Hour1.seconds) {
            System.out.println(Instant.ofEpochSecond(item.seconds()));
        }
        // update managed time
        if (time < first) {
            first = time;
        }
        if (last < time) {
            last = time;
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

        if (first == Long.MAX_VALUE || other.first < first) {
            first = other.first;
        }

        if (last == 0 || last < other.last) {
            last = other.last;
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
        if (timestamp < 0) {
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

    public long firstTime() {
        return first;
    }

    public long lastTime() {
        return last;
    }

    /**
     * Get the first stored time series item.
     * 
     * @return The first stored time series item.
     */
    public E first() {
        return at(first);
    }

    /**
     * Get the last stored time series item which is .
     * 
     * @return The last stored time series item.
     */
    public E last() {
        return at(last);
    }

    /**
     * Clear all items from heap.
     */
    public void clear() {
        for (OnHeap segment : indexed.values()) {
            segment.clear();
        }
        indexed.clear();

        if (disk == null) {
            first = Long.MAX_VALUE;
            last = 0;
        } else {
            first = disk.startTime();
            last = disk.endTime();
        }
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
                for (long time = segmentEnd; segmentStart <= time && 0 < remaining && !disposer.isDisposed(); time -= segmentDuration) {
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
        if (disk != null) {
            segment = new OnHeap(model, startTime, itemSize);
            int[] result = disk.read(startTime, segment.items);
            segment.sync = true;

            if (1 <= result[0]) {
                segment.min = result[1];
                segment.max = result[2];
                tryEvict(startTime);
                indexed.put(startTime, segment);

                return segment;
            }
        }

        // Bulk Data Source
        if (bulk != null) {
            Signal<E> supply = bulk.apply(startTime);
            if (supply != null) {
                long[] times = {startTime, startTime + segmentDuration};
                Deque<OnHeap<E>> heaps = new ArrayDeque();
                heaps.add(new OnHeap(model, times[0], itemSize));
                tryEvict(times[0]);
                indexed.put(times[0], heaps.peekLast());

                supply.to(item -> {
                    long timestamp = item.seconds();
                    if (times[0] <= timestamp) {
                        if (timestamp < times[1]) {
                            heaps.peekLast().set((int) index(timestamp)[1], item);
                        } else {
                            times[0] = times[1];
                            times[1] = times[0] + segmentDuration;
                            heaps.add(new OnHeap(model, times[0], itemSize));
                            tryEvict(times[0]);
                            indexed.put(times[0], heaps.peekLast());

                            heaps.peekLast().set((int) index(timestamp)[1], item);
                        }
                    }
                });
                return heaps.peekFirst();
            }
        }

        // Not Found
        return null;
    }

    /**
     * Forcibly saves all data that currently exists on the heap to disk immediately. All data on
     * disk will be overwritten. If the disk store is not enabled, nothing will happen.
     */
    public void commit() {
        if (disk != null) {
            for (Entry<Long, OnHeap<E>> entry : indexed.entrySet()) {
                disk.write(entry.getKey(), entry.getValue().items);
            }
        }
    }

    /**
     * Try to evict segment for memory compaction.
     * 
     * @param time
     */
    private void tryEvict(long time) {
        long evictableTime = eviction.access(time);
        if (evictableTime != -1) {
            OnHeap<E> segment = indexed.remove(evictableTime);

            if (disk != null) {
                disk.write(evictableTime, segment.items);
            }
            segment.clear();

            if (evictableTime <= first) {
                OnHeap<E> heap = indexed.firstValue();
                first = heap == null ? Long.MAX_VALUE : heap.first().seconds();
            }

            if (last < evictableTime + segmentDuration) {
                OnHeap<E> heap = indexed.lastValue();
                last = heap == null ? 0 : heap.last().seconds();
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
        if (disk == null) {
            return false;
        }

        E[] container = (E[]) Array.newInstance(model.type, 1);
        long[] index = index(item.seconds());
        disk.read(index[0], container);
        return Objects.equals(item, container[(int) index[1]]);
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
    }
}