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

import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.LongFunction;
import java.util.function.Predicate;
import java.util.function.ToLongFunction;

import com.google.common.annotations.VisibleForTesting;
import com.univocity.parsers.csv.CsvParser;
import com.univocity.parsers.csv.CsvParserSettings;
import com.univocity.parsers.csv.CsvWriter;
import com.univocity.parsers.csv.CsvWriterSettings;

import cointoss.ticker.data.TimeseriesData;
import cointoss.util.Chrono;
import cointoss.util.map.ConcurrentNavigableLongMap;
import cointoss.util.map.LongMap;
import cointoss.util.map.LongMap.LongEntry;
import kiss.Decoder;
import kiss.Disposable;
import kiss.Encoder;
import kiss.I;
import kiss.Signal;
import kiss.model.Model;
import kiss.model.Property;
import psychopath.Directory;
import psychopath.File;
import psychopath.Locator;

public final class TimeseriesStore<E> {

    /** Reusable format. yyyyMMdd'T'HHmmss */
    private static final DateTimeFormatter FileName = DateTimeFormatter.ofPattern("yyyyMMddHH");

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
        return create(type, E::epochSeconds, span);
    }

    /**
     * Create the store for timeseries data.
     * 
     * @param <E>
     * @param type
     * @param timestampExtractor
     * @param span
     * @return
     */
    public static <E> TimeseriesStore<E> create(Class<E> type, ToLongFunction<E> timestampExtractor, Span span) {
        return new TimeseriesStore(span, type, timestampExtractor);
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
    public synchronized TimeseriesStore<E> enableDataSupplier(LongFunction<Signal<E>> supplier) {
        this.supplier = supplier;
        return this;
    }

    /**
     * Disable the data suppliance.
     * 
     * @return Chainable API.
     */
    public synchronized TimeseriesStore<E> disableDataSupplier() {
        this.supplier = null;
        return this;
    }

    /**
     * Enable the transparent disk persistence using property-based encoder and decoder.
     * 
     * @param directory A root directory to store data.
     * @param type A type of items.
     * @return Chainable API.
     */
    public synchronized TimeseriesStore<E> enableDiskStore(Directory directory) {
        return enableDiskStore(directory.asJavaPath());
    }

    /**
     * Enable the transparent disk persistence using property-based encoder and decoder.
     * 
     * @param directory A root directory to store data.
     * @param type A type of items.
     * @return Chainable API.
     */
    public synchronized TimeseriesStore<E> enableDiskStore(Path directory) {
        if (model.atomic) {
            enableDiskStore(directory, item -> {
                return new String[] {I.find(Encoder.class, model.type).encode(item)};
            }, values -> {
                return (E) I.find(Decoder.class, model.type).decode(values[0]);
            });
        } else {
            enableDiskStore(directory, item -> {
                List<Property> properties = model.properties();
                String[] values = new String[properties.size()];
                for (int i = 0; i < values.length; i++) {
                    values[i] = I.find(Encoder.class, properties.get(i).model.type).encode(model.get(item, properties.get(i)));
                }
                return values;
            }, values -> {
                E item = I.make(model.type);
                for (int i = 0; i < values.length; i++) {
                    Property property = model.properties().get(i);
                    model.set(item, property, I.find(Decoder.class, property.model.type).decode(values[i]));
                }
                return item;
            });
        }
        return this;
    }

    /**
     * Enable the transparent disk persistence.
     * 
     * @param directory A root directory to store data.
     * @param encoder A date serializer.
     * @param decoder A date deserializer.
     * @return Chainable API.
     */
    public synchronized TimeseriesStore<E> enableDiskStore(Path directory, Function<E, String[]> encoder, Function<String[], E> decoder) {
        if (directory != null && this.disk == null && encoder != null && decoder != null) {
            this.disk = new OnDisk(Locator.directory(directory), encoder, decoder);
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
            segment = new OnHeap();
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

                OnHeap heap = new OnHeap();
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

        /** The managed items. */
        private E[] items = (E[]) new Object[length];

        /** The first item index. */
        private int min = Integer.MAX_VALUE;

        /** The last item index. */
        private int max = Integer.MIN_VALUE;

        /** Flag whether the data is in sync with disk. */
        private boolean sync;

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
            min = Math.min(min, index);
            max = Math.max(max, index);

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

        /** The disk store. */
        private final Directory root;

        /** The data serializer. */
        private final Function<E, String[]> encoder;

        /** The data deserializer. */
        private final Function<String[], E> decoder;

        /**
         * @param root
         * @param encoder
         * @param decoder
         */
        private OnDisk(Directory root, Function<E, String[]> encoder, Function<String[], E> decoder) {
            this.root = root;
            this.encoder = encoder;
            this.decoder = decoder;

            // read all caches
            root.walkFile("*.log").to(file -> {

            });
        }

        /**
         * Store data to disk cache.
         * 
         * @param time
         * @param segment
         */
        private void store(long time, OnHeap segment) {
            if (!segment.sync) {
                File file = name(time);

                CsvWriterSettings setting = new CsvWriterSettings();
                setting.getFormat().setDelimiter(' ');
                setting.getFormat().setComment('無');

                CsvWriter writer = new CsvWriter(file.newOutputStream(), StandardCharsets.ISO_8859_1, setting);
                segment.each(item -> {
                    writer.writeRow(encoder.apply(item));
                });
                writer.close();
                segment.sync = true;
            }
        }

        /**
         * Restore data from disk cache.
         * 
         * @param time
         * @return
         */
        private OnHeap restore(long time) {
            File file = name(time);

            if (file.isAbsent()) {
                return null;
            }

            OnHeap heap = new OnHeap();

            CsvParserSettings setting = new CsvParserSettings();
            setting.getFormat().setDelimiter(' ');
            setting.getFormat().setComment('無');
            CsvParser reader = new CsvParser(setting);
            reader.iterate(file.newInputStream(), StandardCharsets.ISO_8859_1).forEach(values -> {
                E item = decoder.apply(values);
                heap.set((int) index(timestampExtractor.applyAsLong(item))[1], item);
            });

            heap.sync = true;
            return heap;
        }

        /**
         * Compute cache file name by epoch second.
         * 
         * @param time
         * @return
         */
        private File name(long time) {
            return root.directory(span.name()).file(FileName.format(Chrono.utcBySeconds(time)) + ".log");
        }
    }
}