/*
 * Copyright (C) 2019 CoinToss Development Team
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          https://opensource.org/licenses/MIT
 */
package cointoss.ticker;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.Objects;
import java.util.concurrent.ConcurrentSkipListMap;
import java.util.function.Consumer;
import java.util.function.Function;

import cointoss.util.Chrono;
import kiss.I;
import kiss.Signal;

final class SegmentBuffer<E> {

    /** The fixed segment size. */
    private final int segmentSize;

    /** The key extractor. */
    private final Function<E, LocalDate> extractor;

    /** The completed size. */
    private int completedSize;

    /** The completed data manager. */
    private final ConcurrentSkipListMap<LocalDate, Completed<E>> completeds = new ConcurrentSkipListMap();

    /** The uncompleted segment id. */
    private LocalDate uncompleteTime;

    /** The uncompleted size. */
    private int uncompletedSize;

    /** The uncompleted date manager. */
    private E[] uncompleted;

    /**
     * 
     */
    SegmentBuffer(int segmentSize, Function<E, LocalDate> extractor) {
        if (segmentSize < 0) {
            throw new IllegalArgumentException("Segment size [" + segmentSize + "] must be positive.");
        }
        this.segmentSize = segmentSize;
        this.extractor = Objects.requireNonNull(extractor);
        this.uncompleted = (E[]) new Object[segmentSize];
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
     * @param positions An items to add.
     */
    public void add(E item) {
        try {
            uncompleted[uncompletedSize++] = item;
        } catch (ArrayIndexOutOfBoundsException e) {
            completeds.computeIfAbsent(extractor.apply(uncompleted[0]), key -> {
                Completed segment = new Completed(uncompleted);
                completedSize += segment.size;
                return segment;
            });
            uncompleted = (E[]) new Object[segmentSize];
            uncompletedSize = 0;
            uncompleteTime = extractor.apply(item);
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

    public E get(Span span, long epochSeconds) {
        long[] c = span.calculateStartDayTimeAndIndex(epochSeconds);
        LocalDate date = Chrono.systemBySeconds(c[0]).toLocalDate();
        System.out.println(Arrays.toString(c) + "   " + date + "  " + span + "  " + span.seconds);

        if (date.equals(uncompleteTime)) {
            if (c[1] < uncompletedSize) {
                return uncompleted[(int) c[1]];
            } else {
                return uncompleted[uncompletedSize - 1];
            }
        } else {
            Completed<E> completed = completeds.get(date);

            if (completed == null) {
                return null;
            } else {
                System.out.println(completed.size + "   " + Arrays.toString(completed.items));
                return completed.items[(int) c[1]];
            }
        }
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
                return segment.items[index];
            }
            index -= segment.size;
        }

        // ucompleted
        if (segmentSize <= index) {
            index = segmentSize - 1;
        }
        return uncompleted[index];
    }

    /**
     * Get the first item.
     * 
     * @return
     */
    public E first() {
        if (!completeds.isEmpty()) {
            return completeds.firstEntry().getValue().first();
        }

        if (uncompletedSize != 0) {
            return uncompleted[0];
        }
        return null;
    }

    /**
     * Get the last item.
     * 
     * @return
     */
    public E last() {
        if (uncompletedSize != 0) {
            return uncompleted[uncompletedSize - 1];
        }

        if (!completeds.isEmpty()) {
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
     * Signal all items at the specified {@link LocalDate}.
     * 
     * @param date A target date.
     * @return An item stream.
     */
    public Signal<E> each(LocalDate date) {
        return new Signal<E>((observer, disposer) -> {
            try {
                each(date, observer);
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
                each.accept(uncompleted[i]);
            }
        }
    }

    /**
     * Signal all items at the specified {@link LocalDate}.
     * 
     * @param date A target date.
     * @param each An item processor.
     */
    public void each(LocalDate date, Consumer<? super E> each) {
        // check completed
        Completed<E> completed = completeds.get(date);

        if (completed != null) {
            for (E item : completed.items) {
                each.accept(item);
            }
            return;
        }

        // check uncompleted
        if (uncompletedSize == 0) {
            return;
        }

        // check uncompleted key
        LocalDate key = extractor.apply(uncompleted[0]);

        if (key.equals(date)) {
            for (int i = 0; i < uncompletedSize; i++) {
                each.accept(uncompleted[i]);
            }
        }
    }

    /**
     * Clear all items.
     */
    public void clear() {
        uncompleted = (E[]) new Object[segmentSize];
        completeds.clear();
    }

    /**
     * Completely filled segment.
     */
    private static class Completed<E> {

        /**
         * Return the item size.
         */
        private int size;

        /** The actual data manager. */
        private final E[] items;

        /**
         * Completed segment.
         */
        private Completed(int segmentSize, Signal<E> items) {
            this.items = (E[]) new Object[segmentSize];

            items.to(e -> {
                this.items[size++] = e;
            });
        }

        /**
         * Completed segment.
         */
        private Completed(E[] items) {
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
                each.accept(items[i]);
            }
        }
    }
}
