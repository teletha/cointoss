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

import static java.util.stream.Collectors.*;
import static java.util.stream.IntStream.*;
import static org.junit.jupiter.api.Assertions.*;

import java.time.LocalDate;
import java.util.function.Function;

import org.junit.jupiter.api.Test;

import kiss.I;

class SegmentBufferTest {

    int size = 10000;

    LocalDate date = LocalDate.now();

    Function<?, LocalDate> extractor = e -> date;

    @Test
    void addCompleted() {
        SegmentBuffer<Long> buffer = new SegmentBuffer(100000, extractor);
        buffer.addCompleted(date, I.signal(0L).recurse(v -> v + 1).take(100000));

        assert buffer.size() == 100000;
        for (int i = 0; i < 100000; i++) {
            assert buffer.get(i) == i;
        }
    }

    @Test
    void firstCompleted() {
        SegmentBuffer<Integer> buffer = new SegmentBuffer(1, extractor);
        for (int i = 0; i < size; i++) {
            buffer.addCompleted(date.plusDays(i), i);
            assert buffer.first() == 0;
        }
    }

    @Test
    void lastCompleted() {
        SegmentBuffer<Integer> buffer = new SegmentBuffer(1, extractor);
        for (int i = 0; i < size; i++) {
            buffer.addCompleted(date.plusDays(i), i);
            assert buffer.last() == i;
        }
    }

    @Test
    void naturalOrder() {
        SegmentBuffer<Integer> buffer = new SegmentBuffer(1, extractor);

        for (int i = 0; i < size; i++) {
            buffer.addCompleted(date.plusDays(i), i);
        }

        assert buffer.size() == size;
        for (int i = 0; i < size; i++) {
            assert buffer.get(i) == i;
        }
    }

    @Test
    void reverseOrder() {
        SegmentBuffer<Integer> buffer = new SegmentBuffer(1, extractor);

        for (int i = 0; i < size; i++) {
            buffer.addCompleted(date.minusDays(i), size - 1 - i);
        }

        assert buffer.size() == size;
        for (int i = 0; i < size; i++) {
            assert buffer.get(i) == i;
        }
    }

    @Test
    void each() {
        SegmentBuffer<Integer> buffer = new SegmentBuffer(3, extractor);
        buffer.addCompleted(date, 1, 2, 3);

        assertIterableEquals(I.list(1, 2, 3), buffer.each().toList());
    }

    @Test
    void eachSegments() {
        SegmentBuffer<Integer> buffer = new SegmentBuffer(size, extractor);
        buffer.addCompleted(date, 1, 2, 3);
        buffer.addCompleted(date.plusDays(1), 4, 5, 6);

        assertIterableEquals(I.list(1, 2, 3, 4, 5, 6), buffer.each().toList());
    }

    @Test
    void eachSegmentsWithGap() {
        SegmentBuffer<Integer> buffer = new SegmentBuffer(size, extractor);
        buffer.addCompleted(date, 1, 2);
        buffer.addCompleted(date.plusDays(2), 5, 6);

        assertIterableEquals(I.list(1, 2, 5, 6), buffer.each().toList());
    }

    @Test
    void eachRange() {
        SegmentBuffer<Integer> buffer = new SegmentBuffer(size, extractor);
        buffer.addCompleted(date, 1, 2, 3, 4, 5);

        assertIterableEquals(I.list(1, 2, 3), buffer.each(0, 3).toList());
        assertIterableEquals(I.list(2, 3, 4), buffer.each(1, 4).toList());
        assertIterableEquals(I.list(3, 4, 5), buffer.each(2, 5).toList());
        assertIterableEquals(I.list(4, 5), buffer.each(3, 6).toList());
        assertIterableEquals(I.list(5), buffer.each(4, 7).toList());
        assertIterableEquals(I.list(), buffer.each(5, 8).toList());
    }

    @Test
    void eachInvalidRange() {
        SegmentBuffer<Integer> buffer = new SegmentBuffer(size, extractor);
        buffer.addCompleted(date, 1, 2, 3, 4, 5);

        assertThrows(IndexOutOfBoundsException.class, () -> buffer.each(-1, 0).toList());
        assertThrows(IndexOutOfBoundsException.class, () -> buffer.each(10, 1).toList());
    }

    @Test
    void eachRangeSegments() {
        SegmentBuffer<Integer> buffer = new SegmentBuffer(size, extractor);
        buffer.addCompleted(date, 1, 2, 3);
        buffer.addCompleted(date.plusDays(1), 4, 5, 6);

        assertIterableEquals(I.list(1, 2, 3, 4, 5, 6), buffer.each().toList());
    }

    @Test
    void addUncompleted() {
        SegmentBuffer<Long> buffer = new SegmentBuffer(100000, extractor);
        buffer.add(I.signal(0L).recurse(v -> v + 1).take(100000));

        assert buffer.size() == 100000;
        for (int i = 0; i < 100000; i++) {
            assert buffer.get(i) == i;
        }
    }

    @Test
    void addUncompletedOverflow() {
        SegmentBuffer<Long> buffer = new SegmentBuffer<>(10000, e -> LocalDate.now().plusDays(e / 10000));
        buffer.add(I.signal(0L).recurse(v -> v + 1).take(100000));

        assert buffer.size() == 100000;
        for (int i = 0; i < 100000; i++) {
            assert buffer.get(i) == i;
        }
    }

    @Test
    void firstUncompleted() {
        SegmentBuffer<Integer> buffer = new SegmentBuffer(100000, extractor);
        for (int i = 0; i < 100000; i++) {
            buffer.add(i);
            assert buffer.size() == i + 1;
            assert buffer.first() == 0;
        }
    }

    @Test
    void lastUncompleted() {
        SegmentBuffer<Integer> buffer = new SegmentBuffer(100000, extractor);
        for (int i = 0; i < 100000; i++) {
            buffer.add(i);
            assert buffer.size() == i + 1;
            assert buffer.last() == i;
        }
    }

    @Test
    void eachUncompleted() {
        SegmentBuffer<Integer> buffer = new SegmentBuffer(size, extractor);
        buffer.add(1, 2, 3, 4, 5);

        assertIterableEquals(I.list(1, 2, 3, 4, 5), buffer.each().toList());
        assertIterableEquals(I.list(1, 2, 3), buffer.each(0, 3).toList());
        assertIterableEquals(I.list(2, 3, 4), buffer.each(1, 4).toList());
        assertIterableEquals(I.list(3, 4, 5), buffer.each(2, 5).toList());
        assertIterableEquals(I.list(4, 5), buffer.each(3, 6).toList());
        assertIterableEquals(I.list(5), buffer.each(4, 7).toList());
        assertIterableEquals(I.list(), buffer.each(5, 8).toList());
    }

    @Test
    void eachUncompletedLarge() {
        int size = 1000000;
        SegmentBuffer<Integer> buffer = new SegmentBuffer(size, extractor);
        for (int i = 0; i < size; i++) {
            buffer.add(i);
        }
        assertIterableEquals(range(0, size).boxed().collect(toList()), buffer.each().toList());
        assertIterableEquals(range(10000, 80000).boxed().collect(toList()), buffer.each(10000, 80000).toList());
        assertIterableEquals(range(size - 10, size).boxed().collect(toList()), buffer.each(size - 10, size).toList());
        assertIterableEquals(range(size - 10, size).boxed().collect(toList()), buffer.each(size - 10, size + 10).toList());
    }

    @Test
    void eachDateSegment() {
        SegmentBuffer<Double> buffer = new SegmentBuffer<>(2, item -> date.plusDays((long) Math.floor(item)));
        buffer.add(1d, 1.5, 2d, 2.5, 3d, 3.5, 4d, 4.5, 5d);

        assertIterableEquals(I.list(1d, 1.5d), buffer.each(date.plusDays(1)).toList());
        assertIterableEquals(I.list(2d, 2.5d), buffer.each(date.plusDays(2)).toList());
        assertIterableEquals(I.list(3d, 3.5d), buffer.each(date.plusDays(3)).toList());
        assertIterableEquals(I.list(4d, 4.5d), buffer.each(date.plusDays(4)).toList());
        assertIterableEquals(I.list(5d), buffer.each(date.plusDays(5)).toList());

        assert buffer.each(date.plusDays(10)).toList().isEmpty();
    }
}
