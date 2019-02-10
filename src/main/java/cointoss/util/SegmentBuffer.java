/*
 * Copyright (C) 2019 CoinToss Development Team
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          https://opensource.org/licenses/MIT
 */
package cointoss.util;

import java.time.LocalDate;
import java.util.Objects;
import java.util.concurrent.ConcurrentSkipListMap;
import java.util.function.Consumer;
import java.util.function.Function;

import kiss.I;
import kiss.Signal;

/**
 * @version 2018/08/13 17:28:37
 */
public final class SegmentBuffer<E> {

    /** The fixed segment size. */
    private final int segmentSize;

    /** The key extractor. */
    private final Function<E, LocalDate> extractor;

    /** The completed size. */
    private int completedSize;

    /** The completed data manager. */
    private final ConcurrentSkipListMap<LocalDate, Completed<E>> completeds = new ConcurrentSkipListMap();

    /** The uncompleted size. */
    private int uncompletedSize;

    /** The uncompleted date manager. */
    private Object[] uncompleted;

    /**
     * 
     */
    public SegmentBuffer(int segmentSize) {
        this(segmentSize, e -> LocalDate.now());
    }

    /**
     * 
     */
    public SegmentBuffer(int segmentSize, Function<E, LocalDate> extractor) {
        if (segmentSize < 0) {
            throw new IllegalArgumentException("Segment size [" + segmentSize + "] must be positive.");
        }
        this.segmentSize = segmentSize;
        this.extractor = Objects.requireNonNull(extractor);
        this.uncompleted = new Object[segmentSize];
    }

    /**
     * Return the size of this {@link SegmentBuffer}.
     * 
     * @return A positive size or zero.
     */
    public int size() {
        return completedSize + uncompletedSize;
    }

    /**
     * Check whether this {@link SegmentBuffer} is empty or not.
     * 
     * @return
     */
    public boolean isEmpty() {
        return (completedSize + uncompletedSize) == 0;
    }

    /**
     * Check whether this {@link SegmentBuffer} is empty or not.
     * 
     * @return
     */
    public boolean isNotEmpty() {
        return (completedSize + uncompletedSize) != 0;
    }

    /**
     * Add realtime item.
     * 
     * @param items An items to add.
     */
    public void add(E item) {
        try {
            uncompleted[uncompletedSize++] = item;
        } catch (ArrayIndexOutOfBoundsException e) {
            completeds.computeIfAbsent(extractor.apply((E) uncompleted[0]), key -> {
                Completed segment = new Completed(uncompleted);
                completedSize += segment.size;
                return segment;
            });
            uncompleted = new Object[segmentSize];
            uncompletedSize = 0;
            add(item);
        }
    }

    /**
     * Add all realtime items.
     * 
     * @param items An items to add.
     */
    public void add(E... items) {
        for (E item : items) {
            add(item);
        }
    }

    /**
     * Add all realtime items.
     * 
     * @param items An items to add.
     */
    public void add(Signal<E> items) {
        items.to((Consumer<E>) this::add);
    }

    /**
     * Add all completed items of the specified date.
     * 
     * @param date
     * @param items An items to add.
     */
    public void addCompleted(LocalDate date, E... items) {
        addCompleted(date, I.signal(items));
    }

    /**
     * Add all completed items of the specified date.
     * 
     * @param date
     * @param items An items to add.
     */
    public void addCompleted(LocalDate date, Signal<E> items) {
        completeds.computeIfAbsent(date, key -> {
            Completed segment = new Completed(segmentSize, items);
            completedSize += segment.size;
            return segment;
        });
    }

    /**
     * Get an item at the specified index.
     * 
     * @param index
     * @return
     */
    public E get(int index) {
        // completed
        for (Completed<E> segment : completeds.values()) {
            if (index < segment.size) {
                return (E) segment.items[index];
            }
            index -= segment.size;
        }

        // ucompleted
        return (E) uncompleted[index];
    }

    /**
     * Get the first item.
     * 
     * @return
     */
    public E first() {
        if (completedSize != 0) {
            return completeds.firstEntry().getValue().first();
        }

        if (uncompletedSize != 0) {
            return (E) uncompleted[0];
        }
        return null;
    }

    /**
     * Get the first item.
     * 
     * @return
     */
    public E last() {
        if (uncompletedSize != 0) {
            return (E) uncompleted[uncompletedSize - 1];
        }

        if (completedSize != 0) {
            return completeds.lastEntry().getValue().last();
        }
        return null;
    }

    /**
     * Signal all items.
     * 
     * @return An item stream.
     */
    public Signal<E> each() {
        return each(0, size());
    }

    /**
     * Signal all items from start to last.
     * 
     * @param start A start index (included).
     * @return An item stream.
     */
    public Signal<E> each(int start) {
        return each(start, size());
    }

    /**
     * Signal all items from start to end.
     * 
     * @param start A start index (included).
     * @param end A end index (excluded).
     * @return An item stream.
     */
    public Signal<E> each(int start, int end) {
        return new Signal<>((observer, disposer) -> {
            try {
                each(start, end, observer);
                observer.complete();
            } catch (Throwable e) {
                observer.error(e);
            }
            return disposer;
        });
    }

    /**
     * Signal all items.
     * 
     * @param each An item processor.
     */
    public void each(Consumer<? super E> each) {
        each(0, size(), each);
    }

    /**
     * Signal all items from start to last.
     * 
     * @param start A start index (included).
     * @param each An item processor.
     */
    public void each(int start, Consumer<? super E> each) {
        each(start, size(), each);
    }

    /**
     * Signal all items from start to end.
     * 
     * @param start A start index (included).
     * @param end A end index (excluded).
     * @param each An item processor.
     */
    public void each(int start, int end, Consumer<? super E> each) {
        if (end < start) {
            throw new IndexOutOfBoundsException("Start[" + start + "] must be less than end[" + end + "].");
        }

        // completed
        for (Completed segment : completeds.values()) {
            int size = segment.size;
            if (start < size) {
                if (end <= size) {
                    segment.each(start, end, each);
                    return;
                } else {
                    segment.each(start, size, each);
                    start = 0;
                    end -= size;
                }
            } else {
                start -= size;
                end -= size;
            }
        }

        // uncompleted
        if (0 < uncompletedSize) {
            for (int i = start; i < Math.min(end, uncompletedSize); i++) {
                each.accept((E) uncompleted[i]);
            }
        }
    }

    /**
     * @version 2018/08/13 7:22:22
     */
    private static class Completed<E> {

        /**
         * Return the item size.
         */
        private int size;

        /** The actual data manager. */
        private final Object[] items;

        /**
         * Completed segment.
         */
        private Completed(int segmentSize, Signal<E> items) {
            this.items = new Object[segmentSize];

            items.to(e -> {
                this.items[size++] = e;
            });
        }

        /**
         * Completed segment.
         */
        private Completed(Object[] items) {
            this.items = items;
            this.size = items.length;
        }

        /**
         * Get the first item.
         * 
         * @return
         */
        private E first() {
            return size == 0 ? null : (E) items[0];
        }

        /**
         * Get the first item.
         * 
         * @return
         */
        private E last() {
            return size == 0 ? null : (E) items[size - 1];
        }

        /**
         * Signal all items from start to end.
         * 
         * @param start A start index (included).
         * @param end A end index (excluded).
         * @param each An item processor.
         */
        private void each(int start, int end, Consumer<? super E> each) {
            int stop = Math.min(end, size);

            for (int i = start; i < stop; i++) {
                each.accept((E) items[i]);
            }
        }
    }
}
