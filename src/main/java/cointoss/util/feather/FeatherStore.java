/*
 * Copyright (C) 2021 cointoss Development Team
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          https://opensource.org/licenses/MIT
 */
package cointoss.util.feather;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.LongFunction;
import java.util.function.Predicate;

import com.google.common.annotations.VisibleForTesting;

import cointoss.ticker.Span;
import cointoss.util.map.ConcurrentNavigableLongMap;
import cointoss.util.map.LongMap;
import cointoss.util.map.LongMap.LongEntry;
import kiss.Disposable;
import kiss.I;
import kiss.Signal;
import kiss.model.Model;
import psychopath.File;

public final class FeatherStore<E extends TemporalData> implements Disposable {

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

    /** The date-time of first item. */
    private long first = Long.MAX_VALUE;

    /** The date-time of last item. */
    private long last = -1;

    /**
     * Create the store for timeseries data.
     * 
     * @param <E>
     * @param type
     * @param span
     * @return
     */
    public static <E extends TemporalData> FeatherStore<E> create(Class<E> type, Span span) {
        return new FeatherStore<E>(type, span.seconds, (int) (span.segmentSeconds / span.seconds), span.segmentSize);
    }

    /**
     * Create the store for timeseries data.
     * 
     * @param <E>
     * @param type
     * @param span
     * @return
     */
    public static <E extends TemporalData> FeatherStore<E> create(Class<E> type, long itemDuration, int itemSize, int segmentSize) {
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
     * Enable the active data suppliance.
     * 
     * @param passive
     * @return Chainable API.
     */
    public FeatherStore<E> enableDataSupplier(LongFunction<Signal<E>> active) {
        return enableDataSupplier(active, null);
    }

    /**
     * Enable the passive data suppliance.
     * 
     * @param passive
     * @return Chainable API.
     */
    public FeatherStore<E> enableDataSupplier(Signal<E> passive) {
        return enableDataSupplier(null, passive);
    }

    /**
     * Enable the data suppliance.
     * 
     * @param passive
     * @return Chainable API.
     */
    public synchronized FeatherStore<E> enableDataSupplier(LongFunction<Signal<E>> active, Signal<E> passive) {
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
        long start = lastTime();

        active.apply(start).to(e -> {
            System.out.println("Store " + e);
            store(e);
        }, error -> {
            startPassiveSupplier(passive);
        }, () -> {
            long end = lastTime();

            if (start == end) {
                startPassiveSupplier(passive);
            } else {
                startActiveSupplier(active, passive);
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
            add(passive.effectOnDispose(this::commit).effectOnDispose(() -> {
                System.out.println("Dispose store");
            }).to(e -> {
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
        return segment.get((int) index[1]);
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
     * Get all stored time series items in ascending order.
     * 
     * @param each An item processor.
     */
    public void each(Consumer<? super E> each) {
        each().to(each);
    }

    /**
     * Get the time series items stored from the specified start time to end time in ascending
     * order.
     * 
     * @param start A start time (included).
     * @param end A end time (included).
     * @param each An item processor.
     */
    public void each(E start, E end, Consumer<? super E> each) {
        each(start.seconds(), end.seconds(), each);
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
        each(start, end).to(each);
    }

    /**
     * Get the time series items stored from the specified start time to end time in ascending
     * order.
     * 
     * @param start A start time (included).
     * @param end A end time (included).
     * @return An item stream.
     */
    public Signal<E> each() {
        return new Signal<>((observer, disposer) -> {
            for (OnHeap segment : indexed.values()) {
                if (disposer.isDisposed()) {
                    break;
                }
                segment.each(0, itemSize, observer, disposer);
            }
            observer.complete();
            return disposer;
        });
    }

    /**
     * Get the time series items stored from the specified start time to end time in ascending
     * order.
     * 
     * @param start A start time (included).
     * @param end A end time (included).
     * @return An item stream.
     */
    public Signal<E> eachLatest() {
        return new Signal<>((observer, disposer) -> {
            for (OnHeap segment : indexed.descendingMap().values()) {
                if (disposer.isDisposed()) {
                    break;
                }
                segment.eachLatest(itemSize, 0, observer, disposer);
            }
            observer.complete();
            return disposer;
        });
    }

    /**
     * Get the time series items stored from the specified start time to end time in ascending
     * order.
     * 
     * @param start A start time (included).
     * @param end A end time (included).
     * @return An item stream.
     */
    public Signal<E> each(E start, E end) {
        return each(start.seconds(), end.seconds());
    }

    /**
     * Get the time series items stored from the specified start time to end time in ascending
     * order.
     * 
     * @param start A start time (excluded).
     * @param end A end time (excluded).
     * @return An item stream.
     */
    public Signal<E> eachInside(E start, E end) {
        return each(start.seconds() + itemDuration, end.seconds() - itemDuration);
    }

    /**
     * Get the time series items stored from the specified start time to end time in ascending
     * order.
     * 
     * @param start A start time (included).
     * @param end A end time (included).
     * @return An item stream.
     */
    public Signal<E> each(long start, long end) {
        if (end < start) {
            return I.signal();
        }

        return new Signal<>((observer, disposer) -> {
            long[] startIndex = index(start);
            long[] endIndex = index(end);
            ConcurrentNavigableLongMap<OnHeap<E>> sub = indexed.subMap(startIndex[0], true, endIndex[0], true);

            try {
                for (LongEntry<OnHeap<E>> entry : sub.longEntrySet()) {
                    if (disposer.isDisposed()) {
                        break;
                    }
                    long time = entry.getLongKey();
                    int startItemIndex = 0;
                    int endItemIndex = itemSize;

                    if (time == startIndex[0]) {
                        startItemIndex = (int) startIndex[1];
                    }
                    if (time == endIndex[0]) {
                        endItemIndex = (int) endIndex[1];
                    }
                    entry.getValue().each(startItemIndex, endItemIndex, observer, disposer);
                }
                observer.complete();
            } catch (Throwable e) {
                observer.error(e);
            }
            return disposer;
        });
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
            last = -1;
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
     * Get the most recent item that matches the conditions before the indexable item.
     * 
     * @param item An indexable item.
     * @return A matched item or null.
     */
    public E before(E item, Predicate<E> condition) {
        return before(item.seconds(), condition);
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
     * Get the specified number of items before the specified item.
     * 
     * @param item An indexable item.
     * @return
     */
    public List<E> beforeUntil(E item, int maximumSize) {
        return beforeUntil(item.seconds(), maximumSize);
    }

    /**
     * Get the specified number of items before the specified timestamp (epoch seconds).
     * 
     * @param timestamp A time stamp.
     * @return
     */
    public List<E> beforeUntil(long timestamp, int maximumSize) {
        return before(timestamp, maximumSize, false);
    }

    /**
     * Get the specified number of items before the specified item.
     * 
     * @param item An indexable item.
     * @return
     */
    public List<E> beforeUntilWith(E item, int maximumSize) {
        return beforeUntilWith(item.seconds(), maximumSize);
    }

    /**
     * Get the specified number of items before the specified timestamp (epoch seconds).
     * 
     * @param timestamp A time stamp.
     * @return
     */
    public List<E> beforeUntilWith(long timestamp, int maximumSize) {
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
        OnHeap<E> segment = supply(timeIndex);

        if (segment == null) {
            return List.of();
        }

        if (with) {
            E item = segment.get(segmentIndex);
            if (item != null) {
                items.add(item);
            }
        }

        while (items.size() < maximumSize) {
            if (--segmentIndex == -1) {
                timeIndex -= segmentDuration;
                segment = supply(timeIndex);

                if (segment == null) {
                    break;
                } else {
                    segmentIndex = itemSize - 1;
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

        if (start == Option.Latest) {
            E last = last();
            start = last == null ? 0 : last.seconds();
        }

        if (end == -1) {
            if (o.forward) {
                end = Option.Latest;
            } else {
                end = start;
                start = 0;
            }
        }

        boolean forward = start < end;
        long[] startIndex = index(forward ? start : end);
        long[] endIndex = index(forward ? end : start);
        o.forward = o.forward == forward;

        return new Signal<>((observer, disposer) -> {
            // Retrieves the segments that exist on heap or disk in the specified order.
            // If the specified time is out of the range of the actual data time, you can shrink it
            // to within the range of the actual data.
            long segmentStart = Math.max(startIndex[0], first);
            long segmentEnd = Math.min(endIndex[0], last);
            int[] size = {o.max};

            Consumer<OnHeap<E>> consumer = heap -> {
                if (segmentStart == heap.startTime) {
                    if (segmentEnd == heap.startTime) {
                        heap.each((int) startIndex[1], (int) endIndex[1], o.forward, size, observer, disposer);
                    } else {
                        heap.each((int) startIndex[1], itemSize, o.forward, size, observer, disposer);
                    }
                } else if (segmentEnd == heap.startTime) {
                    heap.each(0, (int) endIndex[1], o.forward, size, observer, disposer);
                } else {
                    heap.each(0, itemSize, o.forward, size, observer, disposer);
                }
            };

            if (o.forward) {
                for (long time = segmentStart; time <= segmentEnd && 0 < size[0] && !disposer.isDisposed(); time += segmentDuration) {
                    OnHeap<E> heap = supply(time);
                    if (heap != null) consumer.accept(heap);
                }
            } else {
                for (long time = segmentEnd; segmentStart <= time && 0 < size[0] && !disposer.isDisposed(); time -= segmentDuration) {
                    OnHeap<E> heap = supply(time);
                    if (heap != null) consumer.accept(heap);
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
    public Signal<E> query(TemporalData start, Consumer<Option>... option) {
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
    public Signal<E> query(TemporalData start, TemporalData end, Consumer<Option>... option) {
        return query(start.seconds(), end.seconds(), option);
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

        // Original Data Source
        // if (supplier != null) {
        // Signal<E> supply = supplier.apply(startTime);
        // if (supply != null) {
        // long endTime = startTime + segmentDuration;
        //
        // OnHeap<E> heap = new OnHeap(model, startTime, itemSize);
        // indexed.put(startTime, heap);
        // tryEvict(startTime);
        // supply.to(item -> {
        // long timestamp = item.seconds();
        // if (startTime <= timestamp && timestamp < endTime) {
        // heap.set((int) index(timestamp)[1], item);
        // }
        // });
        // return heap;
        // }
        // }

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
                last = heap == null ? -1 : heap.last().seconds();
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
         * (exclusive) in ascending order.
         * 
         * @param each An item processor.
         */
        void each(Consumer<? super T> each) {
            each(min, max, each, Disposable.empty());
        }

        /**
         * Get the time series items stored from the specified start index (inclusive) to end index
         * (exclusive) in ascending order.
         * 
         * @param start A start index (included).
         * @param end A end index (exclusive).
         * @param each An item processor.
         */
        void each(int start, int end, Consumer<? super T> consumer, Disposable disposer) {
            start = Math.max(min, start);
            end = Math.min(max, end);
            T[] avoidNPE = items; // copy reference to avoid NPE by #clear
            if (avoidNPE != null) {
                for (int i = start; i <= end; i++) {
                    if (disposer.isDisposed()) {
                        return;
                    }
                    consumer.accept(avoidNPE[i]);
                }
            }
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
        void each(int start, int end, boolean forward, int[] size, Consumer<? super T> consumer, Disposable disposer) {
            start = Math.max(min, start);
            end = Math.min(max, end);
            T[] avoidNPE = items; // copy reference to avoid NPE by #clear
            if (avoidNPE != null) {
                if (forward) {
                    for (int i = start; i <= end && 0 < size[0] && !disposer.isDisposed(); i++, size[0]--) {
                        consumer.accept(avoidNPE[i]);
                    }
                } else {
                    for (int i = end; start <= i && 0 < size[0] && !disposer.isDisposed(); i--, size[0]--) {
                        consumer.accept(avoidNPE[i]);
                    }
                }
            }
        }

        /**
         * Get the time series items stored from the specified start index (inclusive) to end index
         * (exclusive) in ascending order.
         * 
         * @param start A start index (included).
         * @param end A end index (exclusive).
         * @param each An item processor.
         */
        void eachLatest(int start, int end, Consumer<? super T> consumer, Disposable disposer) {
            start = Math.min(max, start);
            end = Math.max(min, end);
            T[] avoidNPE = items; // copy reference to avoid NPE by #clear
            if (avoidNPE != null) {
                for (int i = start; end <= i; i--) {
                    if (disposer.isDisposed()) {
                        return;
                    }
                    consumer.accept(avoidNPE[i]);
                }
            }
        }
    }
}