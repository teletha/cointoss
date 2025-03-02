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
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Map.Entry;
import java.util.function.Consumer;
import java.util.function.Predicate;

import com.google.common.annotations.VisibleForTesting;

import cointoss.ticker.Span;
import kiss.Disposable;
import kiss.Model;
import kiss.Signal;
import kiss.Variable;
import primavera.map.ConcurrentNavigableLongMap;
import primavera.map.LongMap;
import primavera.map.LongMap.LongEntry;
import typewriter.api.model.IdentifiableModel;
import typewriter.rdb.RDB;

public final class FeatherStore<E extends IdentifiableModel & Timelinable> implements Disposable {

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

    /** The initial value. */
    private static final long FIRST_INIT = Long.MAX_VALUE;

    /** The initial value. */
    private static final long LAST_INIT = Long.MIN_VALUE;

    /** The date-time of last item on heap. */
    private long firstHeap = FIRST_INIT;

    /** The date-time of last item on heap. */
    private long lastHeap = LAST_INIT;

    /** The disk store. */
    private RDB<E> db;

    private Holder<Variable<Long>> firstDisk;

    private Holder<Variable<Long>> lastDisk;

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
        this.eviction = EvictionPolicy.byEldest(segmentSize);
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
        firstDisk = new Holder(() -> db.min(E::getId), db::stamp, 0);
        lastDisk = new Holder(() -> db.max(E::getId), db::stamp, 0);
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
        int size = indexed.size();
        if (size < segmentSize) {
            // ok
        } else {
            long first = indexed.firstLongKey();
            if (first <= time) {
                // ok
            } else {
                if (db != null) {
                    db.updateLazy(item);
                }
                return; // deny
            }
        }
        long[] index = index(time);

        OnHeap<E> segment = loadSegment(false, index[0], index[1], time);

        if (segment == null) {
            segment = new OnHeap(model, index[0], itemSize);
            tryEvict(index[0]);
            indexed.put(index[0], segment);
        }

        segment.set((int) index[1], item);

        // update managed cache time
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
     * Get the date and time of the first element from all stored data, including secondary
     * cache.This store without items will return -1.
     * 
     * @return
     */
    public long firstTime() {
        long firstDisk = computeFirstDiskTime();
        if (firstHeap == FIRST_INIT) {
            return firstDisk == FIRST_INIT ? -1 : firstDisk;
        } else {
            return firstDisk == FIRST_INIT || firstHeap < firstDisk ? firstHeap : firstDisk;
        }
    }

    /**
     * Get the date and time of the last element from all stored data, including secondary cache.
     * This store without items will return -1.
     * 
     * @return
     */
    public long lastTime() {
        long lastDisk = computeLastDiskTime();
        if (lastHeap == LAST_INIT) {
            return lastDisk == LAST_INIT ? -1 : lastDisk;
        } else {
            return lastDisk == LAST_INIT || lastDisk < lastHeap ? lastHeap : lastDisk;
        }
    }

    /**
     * Retrieves the time of the first element from the memory cache only. This store without items
     * will return -1.
     * 
     * @return
     */
    public long firstCacheTime() {
        return firstHeap == FIRST_INIT ? -1 : firstHeap;
    }

    /**
     * Retrieves the time of the last element from the memory cache only. This store without items
     * will return -1.
     * 
     * @return
     */
    public long lastCacheTime() {
        return lastHeap == LAST_INIT ? -1 : lastHeap;
    }

    /**
     * Compute the time of first item on disk.
     * 
     * @return
     */
    private long computeFirstDiskTime() {
        if (db == null) {
            return FIRST_INIT;
        }
        return firstDisk.compute().or(FIRST_INIT);
    }

    /**
     * Compute the time of last item on disk.
     * 
     * @return
     */
    private long computeLastDiskTime() {
        if (db == null) {
            return LAST_INIT;
        }
        return lastDisk.compute().or(LAST_INIT);
    }

    /**
     * Compute the first segment time logically in the current state.
     * 
     * @return
     */
    public long computeLogicalFirstCacheTime() {
        if (indexed.isEmpty()) {
            return -1;
        }

        return indexed.lastLongKey() - segmentDuration * (segmentSize - 1);
    }

    /**
     * Compute the first and last segment time logically in the current state.
     * 
     * @return
     */
    public long[] computeIdealSegmentTime() {
        long last = lastTime();
        if (last == -1) {
            last = System.currentTimeMillis() / 1000L;
        }
        return new long[] {last - segmentDuration * (segmentSize - 1), last};
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
        return at(firstCacheTime());
    }

    /**
     * Retrieve the item at {@link #lastCacheTime()}.
     * 
     * @return
     */
    public E lastCache() {
        return at(lastCacheTime());
    }

    /**
     * Get the item for the specified timestamp (epoch seconds).
     * 
     * @param timestamp A time stamp.
     * @return
     */
    public E at(Timelinable timestamp) {
        return at(timestamp.seconds());
    }

    /**
     * Get the item for the specified date.
     * 
     * @param date
     * @return
     */
    public E at(ZonedDateTime date) {
        return at(date.toEpochSecond());
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

        OnHeap<E> segment = loadSegment(true, index[0], index[1], timestamp);
        if (segment == null) {
            return null;
        }
        return segment.get((int) index[1]);
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
    public Signal<E> query(Timelinable start, Consumer<Option>... option) {
        return query(start.seconds(), option);
    }

    /**
     * Query items by temporal options.
     * 
     * @param start A starting time.
     * @param option Your options.
     * @return A result.
     */
    public Signal<E> query(ZonedDateTime start, Consumer<Option>... option) {
        return query(start.toEpochSecond(), option);
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
    public Signal<E> query(Timelinable start, Timelinable end, Consumer<Option>... option) {
        return query(start.seconds(), end.seconds(), option);
    }

    /**
     * Query items by temporal options.
     * 
     * @param start A starting time.
     * @param end A ending time.
     * @param option Your options.
     * @return A result.
     */
    public Signal<E> query(ZonedDateTime start, ZonedDateTime end, Consumer<Option>... option) {
        return query(start.toEpochSecond(), end.toEpochSecond(), option);
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
            long actualStart = start;
            long actualEnd = end;
            long first = firstTime();
            long last = lastTime();

            if (actualStart == Option.Latest) {
                actualStart = last;
            }

            if (actualEnd == -1) {
                if (o.forward) {
                    actualStart = Math.max(actualStart, first);
                    actualEnd = Math.max(actualStart, last);
                } else {
                    actualEnd = actualStart;
                    actualStart = 0;
                }
            }

            boolean forward = actualStart <= actualEnd;
            long[] startIndex = index(forward ? actualStart : actualEnd);
            long[] endIndex = index(forward ? actualEnd : actualStart);
            forward = o.forward == forward;

            if (o.excludeStart) {
                if (forward) {
                    startIndex[1] += 1;
                } else {
                    if (actualEnd <= last) {
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
                    OnHeap<E> heap = loadSegment(true, time, 0, Math.max(actualStart, time));
                    if (heap != null) {
                        int open = heap.startTime == segmentStart ? (int) startIndex[1] : 0;
                        int close = heap.startTime == segmentEnd ? (int) endIndex[1] : itemSize;
                        remaining = heap.each(open, close, forward, remaining, observer, disposer);
                    }
                }
            } else {
                for (long time = segmentEnd; segmentStart <= time && 0 < remaining && !disposer.isDisposed(); time -= segmentDuration) {
                    OnHeap<E> heap = loadSegment(true, time, 0, Math.min(actualEnd, time));
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
     * Query the latest items with temporal options.
     * 
     * @param option Your options.
     * @return A result.
     */
    public Signal<E> queryLatest(Consumer<Option>... option) {
        return query(Option.Latest, 0, option);
    }

    /**
     * Load the data segment at the specified date and time.
     */
    private OnHeap<E> loadSegment(boolean readMode, long segmentTime, long segmentIndex, long itemTime) {
        OnHeap<E> segment = indexed.get(segmentTime);
        if (segment != null) {
            // memory cache

            // If data outside the cached range is requested, an attempt is made to read the data.
            // However, only in read mode. In write mode, it does not re-read the data as it is
            // common to write outside the range and is likely to overwrite the DB data anyway.
            if (readMode && (segmentIndex < segment.min || segment.max < segmentIndex)) {
                return loadData(segment, readMode, itemTime);
            } else {
                return segment;
            }
        } else {
            // create new memory cache and load data if DB is enabled
            OnHeap<E> heap = new OnHeap(model, segmentTime, itemSize);
            loadData(heap, readMode, itemTime);

            if (readMode && heap.size() == 0) {
                return null;
            }

            tryEvict(segmentTime);
            indexed.put(segmentTime, heap);
            return heap;
        }
    }

    /**
     * Load data on the specified segment from DB.
     */
    private OnHeap<E> loadData(OnHeap<E> heap, boolean idealSafe, long timestamp) {
        long firstDisk = computeFirstDiskTime();
        if (firstDisk == FIRST_INIT) {
            return heap;
        }

        if (db != null && firstDisk <= timestamp && (!idealSafe || computeLogicalFirstCacheTime() <= heap.startTime)) {
            db.findBy(E::getId, x -> x.isOrMoreThan(heap.startTime).isLessThan(heap.startTime + itemSize * itemDuration)).to(item -> {
                long[] index = index(item.seconds());
                heap.set((int) index[1], item);
            });

            // update managed cache time
            if (heap.size() != 0) {
                if (heap.first().seconds() < firstHeap) {
                    firstHeap = heap.first().seconds();
                }
                if (lastHeap < heap.last().seconds()) {
                    lastHeap = heap.last().seconds();
                }
            }
        }
        return heap;
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

        if (firstHeap == FIRST_INIT || other.firstHeap < firstHeap) {
            firstHeap = other.firstHeap;
        }
        if (lastHeap == 0 || lastHeap < other.lastHeap) {
            lastHeap = other.lastHeap;
        }
        other.indexed.clear();
    }

    /**
     * Clear all items from heap.
     */
    public void clear() {
        for (OnHeap segment : indexed.values()) {
            segment.clear();
        }
        indexed.clear();

        firstHeap = FIRST_INIT;
        lastHeap = LAST_INIT;
    }

    /**
     * Forcibly saves all data that currently exists on the heap to disk immediately. All data on
     * disk will be overwritten. If the disk store is not enabled, nothing will happen.
     */
    public void commit() {
        if (db != null) {
            for (Entry<Long, OnHeap<E>> entry : indexed.entrySet()) {
                OnHeap<E> value = entry.getValue();
                if (value.modified) {
                    db.updateAll(value.items);
                    value.modified = false;
                }
            }
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
                segment.clear();
            }

            if (evictableTime <= firstHeap) {
                OnHeap<E> heap = indexed.firstValue();
                firstHeap = heap == null ? FIRST_INIT : heap.first().seconds();
            }
            if (lastHeap < evictableTime + segmentDuration) {
                OnHeap<E> heap = indexed.lastValue();
                lastHeap = heap == null ? LAST_INIT : heap.last().seconds();
            }
        }
    }

    /**
     * For test.
     */
    @VisibleForTesting
    boolean exist(E item) {
        return existOnHeap(item) || existOnDisk(item);
    }

    /**
     * For test.
     */
    @VisibleForTesting
    boolean exist(long timestamp) {
        return existOnHeap(timestamp) || existOnDisk(timestamp);
    }

    /**
     * For test.
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
     */
    @VisibleForTesting
    boolean existOnHeap(long timestamp) {
        long[] index = index(timestamp);
        OnHeap segment = indexed.get(index[0]);
        if (segment == null) {
            return false;
        }
        return segment.get((int) index[1]) != null;
    }

    /**
     * For test.
     */
    @VisibleForTesting
    boolean existOnDisk(E item) {
        if (db == null) {
            return false;
        }
        return db.findBy(item.getId()).to().is(item);
    }

    /**
     * For test.
     */
    @VisibleForTesting
    boolean existOnDisk(long timestamp) {
        if (db == null) {
            return false;
        }
        return db.findBy(timestamp).to().isPresent();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return new StringBuilder("FeatherStore [").append(Duration.ofSeconds(itemDuration))
                .append("  segment: " + segmentSize() + "/" + segmentSize)
                .append("  item: " + size() + "/" + (itemSize * segmentSize))
                .append("  first: " + (0 <= firstTime() ? Instant.ofEpochSecond(firstTime()) : "NoData"))
                .append("  last: " + (0 <= lastTime() ? Instant.ofEpochSecond(lastTime()) : "NoData"))
                .append("  diskFirst: " + (computeFirstDiskTime() != FIRST_INIT ? Instant.ofEpochSecond(computeFirstDiskTime()) : "NoData"))
                .append("  diskLast: " + (computeLastDiskTime() != LAST_INIT ? Instant.ofEpochSecond(computeLastDiskTime()) : "NoData"))
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

        /** Flag to check if any external changes were made after reading from disk. */
        private boolean modified;

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

            modified = true;
        }

        /**
         * Clear data.
         */
        void clear() {
            items = null;
        }

        /**
         * Retrieve first item in this container.
         * 
         * @return A first item or null.
         */
        T first() {
            return items == null || min < 0 || min == Integer.MAX_VALUE ? null : items[min];
        }

        /**
         * Retrieve last item in this container.
         * 
         * @return A last item or null.
         */
        T last() {
            return items == null || max < 0 || max == Integer.MIN_VALUE ? null : items[max];
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