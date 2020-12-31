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

import static java.nio.file.StandardOpenOption.*;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.LongFunction;
import java.util.function.Predicate;
import java.util.function.ToLongFunction;
import java.util.stream.IntStream;

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
import psychopath.Directory;

public final class TimeseriesStore<E> {

    private static final DateTimeFormatter FileName = DateTimeFormatter.ofPattern("yyyy-MM-dd HH");

    private static final Map<Model, Definition> definitions = new HashMap();

    /** The item type. */
    private final Model<E> model;

    /** The span. */
    private final Span span;

    /** The item length. */
    private final int length;

    /** The key extractor. */
    private final ToLongFunction<E> timestampExtractor;

    /** The completed data manager. */
    private final ConcurrentNavigableLongMap<OnHeap> indexed = LongMap.createSortedMap();

    /** The usage of memory shrinking. */
    private boolean shrink = true;

    /** The disk store. */
    private OnDisk disk;

    /** The disk synchronous mode. */
    private boolean autoDiskSync;

    @SuppressWarnings("serial")
    private final Map<Long, OnHeap> stats = new LinkedHashMap<>(8, 0.75f, true) {

        @Override
        protected boolean removeEldestEntry(Entry<Long, OnHeap> eldest) {
            if (shrink && span.segmentSize < size()) {
                long time = eldest.getKey();
                OnHeap segment = eldest.getValue();

                if (disk != null) {
                    disk.store(time, segment);
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
        return new TimeseriesStore<E>(span, type, E::epochSeconds);
    }

    /**
     * 
     */
    private TimeseriesStore(Span span, Class<E> type, ToLongFunction<E> timestampExtractor) {
        this.model = Model.of(type);
        this.span = Objects.requireNonNull(span);
        this.length = (int) (span.segmentSeconds / span.seconds);
        this.timestampExtractor = Objects.requireNonNull(timestampExtractor);
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
            disposer.add(supplier.effectOnDispose(this::persist).to(e -> {
                store(e);
                System.out.println("store data " + e);
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
     * @param directory A root directory to store data.
     * @return Chainable API.
     */
    public synchronized TimeseriesStore<E> enableDiskStore(Directory directory) {
        return enableDiskStore(directory.asJavaPath());
    }

    /**
     * Enable the transparent disk persistence.
     * 
     * @param directory A root directory to store data.
     * @return Chainable API.
     */
    public synchronized TimeseriesStore<E> enableDiskStore(Directory directory, boolean autoSync) {
        return enableDiskStore(directory.asJavaPath(), autoSync);
    }

    /**
     * Enable the transparent disk persistence.
     * 
     * @param directory A root directory to store data.
     * @return Chainable API.
     */
    public synchronized TimeseriesStore<E> enableDiskStore(Path directory) {
        return enableDiskStore(directory, false);
    }

    /**
     * Enable the transparent disk persistence.
     * 
     * @param directory A root directory to store data.
     * @return Chainable API.
     */
    public synchronized TimeseriesStore<E> enableDiskStore(Path directory, boolean autoSync) {
        if (directory != null && this.disk == null) {
            this.disk = new OnDisk(directory);
            this.autoDiskSync = autoSync;
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
        long remainder = timestamp % span.segmentSeconds;
        return new long[] {timestamp - remainder, remainder / span.seconds};
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
        long[] index = index(timestampExtractor.applyAsLong(item));

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
        each(timestampExtractor.applyAsLong(start), timestampExtractor.applyAsLong(end), each);
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
                segment.each(0, length, observer, disposer);
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
                segment.eachLatest(length, 0, observer, disposer);
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
        return each(timestampExtractor.applyAsLong(start), timestampExtractor.applyAsLong(end));
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
        return each(timestampExtractor.applyAsLong(start) + span.seconds, timestampExtractor.applyAsLong(end) - span.seconds);
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
                    int endItemIndex = length;

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
        return before(timestampExtractor.applyAsLong(item));
    }

    /**
     * Get the most recent item that matches the conditions before the indexable item.
     * 
     * @param item An indexable item.
     * @return A matched item or null.
     */
    public E before(E item, Predicate<E> condition) {
        return before(timestampExtractor.applyAsLong(item), condition);
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
     * Get the most recent item that matches the conditions before the indexable item.
     * 
     * @param item An indexable item.
     * @return A matched item or null.
     */
    public E before(long timestamp, Predicate<E> condition) {
        for (int i = 1; i < Integer.MAX_VALUE; i++) {
            E item = at(timestamp - span.seconds * i);

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
        return beforeUntil(timestampExtractor.applyAsLong(item), maximumSize);
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
        return beforeUntilWith(timestampExtractor.applyAsLong(item), maximumSize);
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
                timeIndex -= span.segmentSeconds;
                segment = supply(timeIndex);

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
            segment = disk.restore(startTime);

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
                long endTime = startTime + span.segmentSeconds;

                OnHeap heap = new OnHeap(startTime);
                indexed.put(startTime, heap);
                stats.put(startTime, heap);
                supply.to(item -> {
                    long timestamp = timestampExtractor.applyAsLong(item);
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
    public void persist() {
        if (disk != null) {
            for (Entry<Long, TimeseriesStore<E>.OnHeap> entry : stats.entrySet()) {
                disk.store(entry.getKey(), entry.getValue());
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
        long[] index = index(timestampExtractor.applyAsLong(item));
        OnHeap segment = indexed.get(index[0]);
        if (segment == null) {
            return false;
        }
        return item.equals(segment.get((int) index[1]));
    }

    /**
     * On-Heap data container.
     */
    private class OnHeap {

        /** The starting time (epoch seconds). */
        private final long startTime;

        /** The managed items. */
        private E[] items = (E[]) new Object[length];

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
            this.startTime = startTime;
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

            if (autoDiskSync) {
                disk.store(startTime, index, item);
            } else {
                sync = false;
            }
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

        /** The disk store. */
        private final Path directory;

        /** The table definition. */
        private final Definition<E> definition = definitions.computeIfAbsent(model, Definition::new);

        /**
         * @param root
         */
        private OnDisk(Path root) {
            try {
                this.directory = root.resolve(span.name());
                Files.createDirectories(directory);
            } catch (IOException e) {
                throw I.quiet(e);
            }
        }

        /**
         * Store data to disk cache.
         * 
         * @param time
         * @param segment
         */
        private void store(long time, int index, E item) {
            if (item == null) {
                return;
            }

            try (FileChannel ch = FileChannel.open(file(time), CREATE, WRITE); FileLock lock = ch.tryLock()) {
                if (lock != null) {
                    ByteBuffer buffer = ByteBuffer.allocate(definition.widthTotal);
                    ch.position(index * definition.widthTotal);

                    for (int k = 0; k < definition.width.length; k++) {
                        definition.writers[k].accept(item, buffer);
                    }
                    ch.write(buffer.flip());
                }
            } catch (IOException e) {
                throw I.quiet(e);
            }
        }

        /**
         * Store data to disk cache.
         * 
         * @param time
         * @param segment
         */
        private void store(long time, OnHeap segment) {
            if (!segment.sync) {
                try (FileChannel ch = FileChannel.open(file(time), CREATE, WRITE); FileLock lock = ch.tryLock()) {
                    if (lock != null) {
                        ByteBuffer buffer = ByteBuffer.allocate(definition.widthTotal);
                        for (int i = 0; i < length; i++) {
                            E item = segment.items[i];
                            if (item != null) {
                                ch.position(i * definition.widthTotal);

                                for (int k = 0; k < definition.width.length; k++) {
                                    definition.writers[k].accept(item, buffer);
                                }
                                buffer.flip();
                                ch.write(buffer);
                                buffer.flip();
                            }
                        }
                        segment.sync = true;
                    }
                } catch (IOException e) {
                    throw I.quiet(e);
                }
            }
        }

        /**
         * Restore data from disk cache.
         * 
         * @param time
         * @return
         */
        private OnHeap restore(long time) {
            Path file = file(time);

            if (!Files.exists(file)) {
                return null;
            }

            OnHeap heap = new OnHeap(time);

            try (FileChannel ch = FileChannel.open(file, READ); FileLock lock = ch.tryLock()) {
                if (lock != null) {
                    ByteBuffer buffer = ByteBuffer.allocate(definition.widthTotal);
                    for (int i = 0; i < length; i++) {
                        ch.read(buffer);
                        buffer.flip();
                        if (buffer.limit() != 0) {
                            E item = I.make(model.type);
                            for (int k = 0; k < definition.width.length; k++) {
                                definition.readers[k].accept(item, buffer);
                            }
                            heap.items[i] = item;
                            buffer.flip();
                        }
                    }
                    heap.sync = true;
                }
            } catch (IOException e) {
                throw I.quiet(e);
            }
            return heap;
        }

        /**
         * Compute cache file name by epoch second.
         * 
         * @param time
         * @return
         */
        private Path file(long time) {
            return directory.resolve(FileName.format(Chrono.utcBySeconds(time)) + ".db");
        }
    }

    /**
     * 
     */
    private static class Definition<E> {

        private final int[] width;

        private final int widthTotal;

        private final BiConsumer<E, ByteBuffer>[] readers;

        private final BiConsumer<E, ByteBuffer>[] writers;

        private Definition(Model<E> model) {
            List<Property> properties = model.properties();
            this.width = new int[properties.size()];
            this.readers = new BiConsumer[width.length];
            this.writers = new BiConsumer[width.length];

            for (int i = 0; i < width.length; i++) {
                Property property = properties.get(i);
                Class c = property.model.type;
                if (c == boolean.class) {
                    width[i] = 1;
                    readers[i] = (o, b) -> model.set(o, property, b.get() == 0 ? Boolean.FALSE : Boolean.TRUE);
                    writers[i] = (o, b) -> b.put((byte) (model.get(o, property) == Boolean.FALSE ? 0 : 1));
                } else if (c == byte.class) {
                    width[i] = 1;
                    readers[i] = (o, b) -> model.set(o, property, b.get());
                    writers[i] = (o, b) -> b.put((byte) model.get(o, property));
                } else if (c == short.class) {
                    width[i] = 2;
                    readers[i] = (o, b) -> model.set(o, property, b.getShort());
                    writers[i] = (o, b) -> b.putShort((short) model.get(o, property));
                } else if (c == char.class) {
                    width[i] = 2;
                    readers[i] = (o, b) -> model.set(o, property, b.getChar());
                    writers[i] = (o, b) -> b.putChar((char) model.get(o, property));
                } else if (c == int.class) {
                    width[i] = 4;
                    readers[i] = (o, b) -> model.set(o, property, b.getInt());
                    writers[i] = (o, b) -> b.putInt((int) model.get(o, property));
                } else if (c == float.class) {
                    width[i] = 4;
                    readers[i] = (o, b) -> model.set(o, property, b.getFloat());
                    writers[i] = (o, b) -> b.putFloat((float) model.get(o, property));
                } else if (c == long.class) {
                    width[i] = 8;
                    readers[i] = (o, b) -> model.set(o, property, b.getLong());
                    writers[i] = (o, b) -> b.putLong((long) model.get(o, property));
                } else if (c == double.class) {
                    width[i] = 8;
                    readers[i] = (o, b) -> model.set(o, property, b.getDouble());
                    writers[i] = (o, b) -> b.putDouble((double) model.get(o, property));
                } else if (c.isEnum()) {
                    int size = c.getEnumConstants().length;
                    if (size < 8) {
                        width[i] = 1;
                        readers[i] = (o, b) -> model.set(o, property, property.model.type.getEnumConstants()[b.get()]);
                        writers[i] = (o, b) -> b.put((byte) ((Enum) model.get(o, property)).ordinal());
                    } else if (size < 128) {
                        width[i] = 2;
                        readers[i] = (o, b) -> model.set(o, property, property.model.type.getEnumConstants()[b.getShort()]);
                        writers[i] = (o, b) -> b.putShort((short) ((Enum) model.get(o, property)).ordinal());
                    } else {
                        width[i] = 4;
                        readers[i] = (o, b) -> model.set(o, property, property.model.type.getEnumConstants()[b.getInt()]);
                        writers[i] = (o, b) -> b.putInt(((Enum) model.get(o, property)).ordinal());
                    }
                } else if (Num.class.isAssignableFrom(c)) {
                    width[i] = 4;
                    readers[i] = (o, b) -> model.set(o, property, Num.of(b.getFloat()));
                    writers[i] = (o, b) -> b.putFloat(((Num) model.get(o, property)).floatValue());
                } else if (ZonedDateTime.class.isAssignableFrom(c)) {
                    width[i] = 8;
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
            this.widthTotal = IntStream.of(width).sum();
        }
    }
}