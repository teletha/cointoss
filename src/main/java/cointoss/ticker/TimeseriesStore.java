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
import java.util.function.ToLongFunction;

import com.google.common.annotations.VisibleForTesting;
import com.univocity.parsers.csv.CsvParser;
import com.univocity.parsers.csv.CsvParserSettings;
import com.univocity.parsers.csv.CsvWriter;
import com.univocity.parsers.csv.CsvWriterSettings;

import cointoss.util.Chrono;
import cointoss.util.map.ConcurrentNavigableLongMap;
import cointoss.util.map.LongMap;
import cointoss.util.map.LongMap.LongEntry;
import kiss.Decoder;
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
     * 
     */
    public TimeseriesStore(Span span, ToLongFunction<E> timestampExtractor) {
        this.span = Objects.requireNonNull(span);
        this.length = (int) (span.segment / span.seconds);
        this.timestampExtractor = Objects.requireNonNull(timestampExtractor);
    }

    /**
     * Enable the data suppliance.
     * 
     * @param supplier
     * @return
     */
    public synchronized TimeseriesStore<E> enableDataSupplier(LongFunction<Signal<E>> supplier) {
        this.supplier = supplier;
        return this;
    }

    /**
     * Enable the transparent disk persistence using property-based encoder and decoder.
     * 
     * @param directory A root directory to store data.
     * @param type A type of items.
     * @return Chainable API.
     */
    public synchronized TimeseriesStore<E> enableDiskStore(Path directory, Class<E> type) {
        if (type != null) {
            Model<E> model = Model.of(type);

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
                        values[i] = I.find(Encoder.class, properties.get(i).model.type).encode(item);
                    }
                    return values;
                }, values -> {
                    E item = I.make(type);
                    for (int i = 0; i < values.length; i++) {
                        Property property = model.properties().get(i);
                        model.set(item, property, I.find(Decoder.class, property.model.type).decode(values[i]));
                    }
                    return item;
                });
            }
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
        long remainder = timestamp % span.segment;
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
     * Get the last stored time series item.
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
        for (OnHeap segment : indexed.values()) {
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
        ConcurrentNavigableLongMap<OnHeap> sub = indexed.subMap(startIndex[0], true, endIndex[0], true);

        for (LongEntry<OnHeap> entry : sub.longEntrySet()) {
            long time = entry.getLongKey();
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
        OnHeap segment = supply(timeIndex);

        if (with) {
            E item = segment.get(segmentIndex);
            if (item != null) {
                items.add(item);
            }
        }

        while (items.size() < maximumSize) {
            if (--segmentIndex == -1) {
                timeIndex -= span.segment;
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
                long endTime = startTime + span.segment;

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
            return items[index];
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
        E first() {
            return items == null ? null : items[min];
        }

        /**
         * Retrieve last item in this container.
         * 
         * @return A last item or null.
         */
        E last() {
            return items == null ? null : items[max];
        }

        /**
         * Get the time series items stored from the specified start index (inclusive) to end index
         * (exclusive) in ascending order.
         * 
         * @param each An item processor.
         */
        void each(Consumer<? super E> each) {
            each(min, max, each);
        }

        /**
         * Get the time series items stored from the specified start index (inclusive) to end index
         * (exclusive) in ascending order.
         * 
         * @param start A start index (included).
         * @param end A end index (exclusive).
         * @param each An item processor.
         */
        void each(int start, int end, Consumer<? super E> consumer) {
            start = Math.max(min, start);
            end = Math.min(max, end);
            E[] avoidNPE = items; // copy reference to avoid NPE by #clear
            if (avoidNPE != null) {
                for (int i = start; i <= end; i++) {
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
            root.walkFile("*.cache").to(file -> {

            });
        }

        /**
         * Store data to disk cache.
         * 
         * @param time
         * @param segment
         */
        private void store(long time, OnHeap segment) {
            File file = name(time);

            if (file.isAbsent()) {
                CsvWriterSettings setting = new CsvWriterSettings();
                setting.getFormat().setDelimiter(' ');
                setting.getFormat().setComment('無');

                CsvWriter writer = new CsvWriter(file.newOutputStream(), StandardCharsets.ISO_8859_1, setting);
                segment.each(item -> {
                    writer.writeRow(encoder.apply(item));
                });
                writer.close();
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

            return heap;
        }

        /**
         * Compute cache file name by epoch second.
         * 
         * @param time
         * @return
         */
        private File name(long time) {
            return root.directory(span.name()).file(FileName.format(Chrono.utcBySeconds(time)) + ".cache");
        }
    }
}