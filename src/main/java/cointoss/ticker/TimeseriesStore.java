/*
 * Copyright (C) 2021 cointoss Development Team
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          https://opensource.org/licenses/MIT
 */
package cointoss.ticker;

import static java.nio.file.StandardOpenOption.*;

import java.io.IOException;
import java.lang.reflect.Array;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.Path;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock.ReadLock;
import java.util.concurrent.locks.ReentrantReadWriteLock.WriteLock;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.LongFunction;
import java.util.function.Predicate;

import com.google.common.annotations.VisibleForTesting;

import cointoss.ticker.data.TimeseriesData;
import cointoss.util.Chrono;
import cointoss.util.arithmetic.Num;
import cointoss.util.map.ConcurrentNavigableLongMap;
import cointoss.util.map.LongMap;
import cointoss.util.map.LongMap.LongEntry;
import kiss.Disposable;
import kiss.I;
import kiss.Signal;
import kiss.model.Model;
import kiss.model.Property;
import psychopath.File;
import psychopath.Locator;

public final class TimeseriesStore<E extends TimeseriesData> {

    /** The table definitions. */
    private static final Map<Model, Definition> definitions = new ConcurrentHashMap();

    /** The item type. */
    private final Model<E> model;

    /** The duration of item. */
    private final long itemDuration;

    /** The size of item. */
    private final int itemSize;

    /** The duration of segmenet. */
    private final long segmentDuration;

    /** The size of segment. */
    private final int segmentSize;

    /** The completed data manager. */
    private final ConcurrentNavigableLongMap<OnHeap> indexed = LongMap.createSortedMap();

    /** The usage of memory shrinking. */
    private boolean shrink = true;

    /** The disk store. */
    private OnDisk disk;

    @SuppressWarnings("serial")
    private final Map<Long, OnHeap> stats = new LinkedHashMap<>(8, 0.75f, true) {

        @Override
        protected boolean removeEldestEntry(Entry<Long, OnHeap> eldest) {
            if (shrink && segmentSize < size()) {
                long time = eldest.getKey();
                OnHeap segment = eldest.getValue();

                if (disk != null) {
                    disk.write(time, segment);
                }

                indexed.remove(time);
                segment.clear();
                return true;
            } else {
                return false;
            }
        }
    };

    /** The data supplier. */
    private LongFunction<Signal<E>> supplier;

    /**
     * Create the store for timeseries data.
     * 
     * @param <E>
     * @param type
     * @param span
     * @return
     */
    public static <E extends TimeseriesData> TimeseriesStore<E> create(Class<E> type, Span span) {
        return new TimeseriesStore<E>(type, span.seconds, (int) (span.segmentSeconds / span.seconds), span.segmentSize);
    }

    /**
     * Create the store for timeseries data.
     * 
     * @param <E>
     * @param type
     * @param span
     * @return
     */
    public static <E extends TimeseriesData> TimeseriesStore<E> create(Class<E> type, long itemDuration, int itemSize, int segmentSize) {
        return new TimeseriesStore<E>(type, itemDuration, itemSize, segmentSize);
    }

    /**
     * 
     */
    private TimeseriesStore(Class<E> type, long itemDuration, int itemSize, int SegmentSize) {
        this.model = Model.of(type);
        this.itemDuration = itemDuration;
        this.itemSize = itemSize;
        this.segmentDuration = itemDuration * itemSize;
        this.segmentSize = SegmentSize;
    }

    /**
     * Enable the data suppliance.
     * 
     * @param supplier
     * @return Chainable API.
     */
    public synchronized TimeseriesStore<E> enableActiveDataSupplier(LongFunction<Signal<E>> supplier) {
        this.supplier = supplier;
        return this;
    }

    /**
     * Enable the data suppliance.
     * 
     * @param supplier
     * @return Chainable API.
     */
    public synchronized TimeseriesStore<E> enablePassiveDataSupplier(Signal<E> supplier, Disposable disposer) {
        if (disposer != null && supplier != null) {
            disposer.add(supplier.effectOnDispose(this::commit).to(e -> {
                store(e);
            }));
        }
        return this;
    }

    /**
     * Disable the data suppliance.
     * 
     * @return Chainable API.
     */
    public synchronized TimeseriesStore<E> disableActiveDataSupplier() {
        this.supplier = null;
        return this;
    }

    /**
     * Enable the transparent disk persistence.
     * 
     * @param databaseFile An actual file to store data.
     * @return Chainable API.
     */
    public synchronized TimeseriesStore<E> enableDiskStore(Path databaseFile) {
        return enableDiskStore(Locator.file(databaseFile));
    }

    /**
     * Enable the transparent disk persistence.
     * 
     * @param databaseFile An actual file to store data.
     * @return Chainable API.
     */
    public synchronized TimeseriesStore<E> enableDiskStore(File databaseFile) {
        if (databaseFile != null && this.disk == null) {
            this.disk = new OnDisk(databaseFile);
        }
        return this;
    }

    /**
     * Disable the transparent disk persistence.
     * 
     * @return
     */
    public synchronized TimeseriesStore<E> disableDiskStore() {
        this.disk = null;
        return this;
    }

    /**
     * Enable the automatic memory saving.
     * 
     * @return Chainable API.
     */
    public synchronized TimeseriesStore<E> enableMemorySaving() {
        this.shrink = true;
        return this;
    }

    /**
     * Disable the automatic memory saving.
     * 
     * @return Chainable API.
     */
    public synchronized TimeseriesStore<E> disableMemorySaving() {
        this.shrink = false;
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
     * Return the size of this {@link TimeseriesStore}.
     * 
     * @return A positive size or zero.
     */
    public int size() {
        return indexed.values().stream().mapToInt(OnHeap::size).sum();
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
        long[] index = index(item.epochSeconds());

        OnHeap segment = supply(index[0]);

        if (segment == null) {
            segment = new OnHeap(index[0]);
            indexed.put(index[0], segment);
            stats.put(index[0], segment);
        }
        segment.set((int) index[1], item);
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
        OnHeap segment = supply(index[0]);

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
        OnHeap entry = indexed.firstValue();
        if (entry == null) {
            return null;
        }
        return entry.first();
    }

    /**
     * Get the last stored time series item which is .
     * 
     * @return The last stored time series item.
     */
    public E last() {
        OnHeap entry = indexed.lastValue();
        if (entry == null) {
            return null;
        }
        return entry.last();
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
        each(start.epochSeconds(), end.epochSeconds(), each);
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
        return each(start.epochSeconds(), end.epochSeconds());
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
        return each(start.epochSeconds() + itemDuration, end.epochSeconds() - itemDuration);
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
            ConcurrentNavigableLongMap<OnHeap> sub = indexed.subMap(startIndex[0], true, endIndex[0], true);

            try {
                for (LongEntry<OnHeap> entry : sub.longEntrySet()) {
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
    }

    /**
     * Get the item just before the specified item.
     * 
     * @param item An indexable item.
     * @return
     */
    public E before(E item) {
        return before(item.epochSeconds());
    }

    /**
     * Get the most recent item that matches the conditions before the indexable item.
     * 
     * @param item An indexable item.
     * @return A matched item or null.
     */
    public E before(E item, Predicate<E> condition) {
        return before(item.epochSeconds(), condition);
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
        return beforeUntil(item.epochSeconds(), maximumSize);
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
        return beforeUntilWith(item.epochSeconds(), maximumSize);
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
        OnHeap segment = supply(timeIndex);

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
     * Get the data segment at the specified date and time.
     * 
     * @param startTime
     * @return
     */
    private OnHeap supply(long startTime) {
        // Memory Cache
        OnHeap segment = indexed.get(startTime);
        if (segment != null) {
            return segment;
        }

        // Disk Cache
        if (disk != null) {
            segment = disk.read(startTime);

            if (segment != null) {
                indexed.put(startTime, segment);
                stats.put(startTime, segment);

                return segment;
            }
        }

        // Original Data Source
        if (supplier != null) {
            Signal<E> supply = supplier.apply(startTime);
            if (supply != null) {
                long endTime = startTime + segmentDuration;

                OnHeap heap = new OnHeap(startTime);
                indexed.put(startTime, heap);
                stats.put(startTime, heap);
                supply.to(item -> {
                    long timestamp = item.epochSeconds();
                    if (startTime <= timestamp && timestamp < endTime) {
                        heap.set((int) index(timestamp)[1], item);
                    }
                });
                return heap;
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
            for (Entry<Long, TimeseriesStore<E>.OnHeap> entry : stats.entrySet()) {
                disk.write(entry.getKey(), entry.getValue());
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
        long[] index = index(item.epochSeconds());
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

        long[] index = index(item.epochSeconds());
        E read = disk.read(index[0]).items[(int) index[1]];
        return Objects.equals(item, read);
    }

    /**
     * On-Heap data container.
     */
    private class OnHeap {

        /** The managed items. */
        private E[] items;

        /** The first item index. */
        private int min = Integer.MAX_VALUE;

        /** The last item index. */
        private int max = Integer.MIN_VALUE;

        /** Flag whether the data is in sync with disk. */
        private boolean sync;

        /**
         * @param startTime The starting time (epoch seconds).
         */
        private OnHeap(long startTime) {
            this(startTime, itemSize);
        }

        /**
         * @param startTime The starting time (epoch seconds).
         */
        private OnHeap(long startTime, int size) {
            this.items = (E[]) Array.newInstance(model.type, size);
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
        E get(int index) {
            return items == null ? null : items[index];
        }

        /**
         * Set item by index.
         * 
         * @param index An item index.
         * @param item An item to set.
         */
        void set(int index, E item) {
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
        E first() {
            return items == null || min < 0 ? null : items[min];
        }

        /**
         * Retrieve last item in this container.
         * 
         * @return A last item or null.
         */
        E last() {
            return items == null || max < 0 ? null : items[max];
        }

        /**
         * Get the time series items stored from the specified start index (inclusive) to end index
         * (exclusive) in ascending order.
         * 
         * @param each An item processor.
         */
        void each(Consumer<? super E> each) {
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
        void each(int start, int end, Consumer<? super E> consumer, Disposable disposer) {
            start = Math.max(min, start);
            end = Math.min(max, end);
            E[] avoidNPE = items; // copy reference to avoid NPE by #clear
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
         * (exclusive) in ascending order.
         * 
         * @param start A start index (included).
         * @param end A end index (exclusive).
         * @param each An item processor.
         */
        void eachLatest(int start, int end, Consumer<? super E> consumer, Disposable disposer) {
            start = Math.min(max, start);
            end = Math.max(min, end);
            E[] avoidNPE = items; // copy reference to avoid NPE by #clear
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

    /**
     * 
     */
    private class OnDisk {

        /** The table definition. */
        private final Definition<E> definition = definitions.computeIfAbsent(model, Definition::new);

        private final FileChannel channel;

        private final ReentrantReadWriteLock lock = new ReentrantReadWriteLock();

        /**
         * @param root
         */
        private OnDisk(File databaseFile) {
            this.channel = databaseFile.newFileChannel(CREATE, SPARSE, READ, WRITE);
        }

        /**
         * Write data to disk cache.
         * 
         * @param truncatedTime
         * @param segment
         */
        private void write(long truncatedTime, OnHeap segment) {
            if (!segment.sync) {
                WriteLock writeLock = lock.writeLock();
                writeLock.lock();

                try {
                    long startPosition = truncatedTime / itemDuration * definition.width;
                    ByteBuffer buffer = ByteBuffer.allocate(definition.width * itemSize);

                    for (int i = 0; i < itemSize; i++) {
                        E item = segment.items[i];
                        if (item == null) {
                            if (buffer.position() != 0) {
                                buffer.flip();
                                channel.write(buffer, startPosition);
                                buffer.clear();
                            }
                            startPosition = (truncatedTime / itemDuration + i + 1) * definition.width;
                        } else {
                            for (int k = 0; k < definition.readers.length; k++) {
                                definition.writers[k].accept(item, buffer);
                            }
                        }
                    }

                    if (buffer.position() != 0) {
                        buffer.flip();
                        channel.write(buffer, startPosition);
                    }
                    segment.sync = true;
                } catch (IOException e) {
                    throw I.quiet(e);
                } finally {
                    writeLock.unlock();
                }
            }
        }

        /**
         * Read data from disk cache.
         * 
         * @param truncatedTime
         * @return
         */
        private OnHeap read(long truncatedTime) {
            OnHeap heap = new OnHeap(truncatedTime, itemSize);
            ReadLock readLock = lock.readLock();

            try {
                readLock.lock();

                ByteBuffer buffer = ByteBuffer.allocate(definition.width * itemSize);
                int size = channel.read(buffer, truncatedTime / itemDuration * definition.width);

                if (size != -1) {
                    buffer.flip();

                    int readableItemSize = size / definition.width;
                    for (int i = 0; i < readableItemSize; i++) {
                        heap.items[i] = I.make(model.type);
                        for (int k = 0; k < definition.readers.length; k++) {
                            definition.readers[k].accept(heap.items[i], buffer);
                        }
                    }
                    heap.sync = true;
                }
            } catch (IOException e) {
                throw I.quiet(e);
            } finally {
                readLock.unlock();
            }
            return heap;
        }
    }

    /**
     * 
     */
    private static class Definition<E> {

        private final int width;

        private final BiConsumer<E, ByteBuffer>[] readers;

        private final BiConsumer<E, ByteBuffer>[] writers;

        private Definition(Model<E> model) {
            int width = 0;
            List<Property> properties = model.properties();
            this.readers = new BiConsumer[properties.size()];
            this.writers = new BiConsumer[properties.size()];

            for (int i = 0; i < properties.size(); i++) {
                Property property = properties.get(i);
                Class c = property.model.type;
                if (c == boolean.class) {
                    width += 1;
                    readers[i] = (o, b) -> model.set(o, property, b.get() == 0 ? Boolean.FALSE : Boolean.TRUE);
                    writers[i] = (o, b) -> b.put((byte) (model.get(o, property) == Boolean.FALSE ? 0 : 1));
                } else if (c == byte.class) {
                    width += 1;
                    readers[i] = (o, b) -> model.set(o, property, b.get());
                    writers[i] = (o, b) -> b.put((byte) model.get(o, property));
                } else if (c == short.class) {
                    width += 2;
                    readers[i] = (o, b) -> model.set(o, property, b.getShort());
                    writers[i] = (o, b) -> b.putShort((short) model.get(o, property));
                } else if (c == char.class) {
                    width += 2;
                    readers[i] = (o, b) -> model.set(o, property, b.getChar());
                    writers[i] = (o, b) -> b.putChar((char) model.get(o, property));
                } else if (c == int.class) {
                    width += 4;
                    readers[i] = (o, b) -> model.set(o, property, b.getInt());
                    writers[i] = (o, b) -> b.putInt((int) model.get(o, property));
                } else if (c == float.class) {
                    width += 4;
                    readers[i] = (o, b) -> model.set(o, property, b.getFloat());
                    writers[i] = (o, b) -> b.putFloat((float) model.get(o, property));
                } else if (c == long.class) {
                    width += 8;
                    readers[i] = (o, b) -> model.set(o, property, b.getLong());
                    writers[i] = (o, b) -> b.putLong((long) model.get(o, property));
                } else if (c == double.class) {
                    width += 8;
                    readers[i] = (o, b) -> model.set(o, property, b.getDouble());
                    writers[i] = (o, b) -> b.putDouble((double) model.get(o, property));
                } else if (c.isEnum()) {
                    int size = c.getEnumConstants().length;
                    if (size < 8) {
                        width += 1;
                        readers[i] = (o, b) -> model.set(o, property, property.model.type.getEnumConstants()[b.get()]);
                        writers[i] = (o, b) -> b.put((byte) ((Enum) model.get(o, property)).ordinal());
                    } else if (size < 128) {
                        width += 2;
                        readers[i] = (o, b) -> model.set(o, property, property.model.type.getEnumConstants()[b.getShort()]);
                        writers[i] = (o, b) -> b.putShort((short) ((Enum) model.get(o, property)).ordinal());
                    } else {
                        width += 4;
                        readers[i] = (o, b) -> model.set(o, property, property.model.type.getEnumConstants()[b.getInt()]);
                        writers[i] = (o, b) -> b.putInt(((Enum) model.get(o, property)).ordinal());
                    }
                } else if (Num.class.isAssignableFrom(c)) {
                    width += 4;
                    readers[i] = (o, b) -> model.set(o, property, Num.of(b.getFloat()));
                    writers[i] = (o, b) -> b.putFloat(((Num) model.get(o, property)).floatValue());
                } else if (ZonedDateTime.class.isAssignableFrom(c)) {
                    width += 8;
                    readers[i] = (o, b) -> {
                        long time = b.getLong();
                        model.set(o, property, time == -1 ? null : Chrono.utcByMills(time));
                    };
                    writers[i] = (o, b) -> {
                        ZonedDateTime time = (ZonedDateTime) model.get(o, property);
                        b.putLong(time == null ? -1 : time.toInstant().toEpochMilli());
                    };
                } else {
                    throw new IllegalArgumentException("Unspported property type [" + c.getName() + "] on " + model.type.getName() + ".");
                }
            }
            this.width = width;
        }
    }
}