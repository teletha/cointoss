/*
 * Copyright (C) 2018 Nameless Production Committee
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          http://opensource.org/licenses/mit-license.php
 */
package cointoss.util;

import static org.junit.jupiter.api.Assertions.*;

import java.time.LocalDate;

import org.junit.jupiter.api.Test;

import kiss.I;

/**
 * @version 2018/08/11 17:20:25
 */
class SegmentBufferTest {

    int size = 10000;

    LocalDate date = LocalDate.now();

    @Test
    void addCompleted() {
        SegmentBuffer<Long> buffer = new SegmentBuffer();
        buffer.addCompleted(date, I.signalRange(0, 100000));

        assert buffer.size() == 100000;
        for (int i = 0; i < 100000; i++) {
            assert buffer.get(i) == i;
        }
    }

    @Test
    void naturalOrder() {
        SegmentBuffer<Integer> buffer = new SegmentBuffer();

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
        SegmentBuffer<Integer> buffer = new SegmentBuffer();

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
        SegmentBuffer<Integer> buffer = new SegmentBuffer();
        buffer.addCompleted(date, 1, 2, 3);

        assertIterableEquals(I.list(1, 2, 3), buffer.each().toList());
    }

    @Test
    void eachSegments() {
        SegmentBuffer<Integer> buffer = new SegmentBuffer();
        buffer.addCompleted(date, 1, 2, 3);
        buffer.addCompleted(date.plusDays(1), 4, 5, 6);

        assertIterableEquals(I.list(1, 2, 3, 4, 5, 6), buffer.each().toList());
    }

    @Test
    void eachSegmentsWithGap() {
        SegmentBuffer<Integer> buffer = new SegmentBuffer();
        buffer.addCompleted(date, 1, 2);
        buffer.addCompleted(date.plusDays(2), 5, 6);

        assertIterableEquals(I.list(1, 2, 5, 6), buffer.each().toList());
    }

    @Test
    void eachRange() {
        SegmentBuffer<Integer> buffer = new SegmentBuffer();
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
        SegmentBuffer<Integer> buffer = new SegmentBuffer();
        buffer.addCompleted(date, 1, 2, 3, 4, 5);

        assertThrows(IndexOutOfBoundsException.class, () -> buffer.each(-1, 0).toList());
        assertThrows(IndexOutOfBoundsException.class, () -> buffer.each(10, 1).toList());
    }

    @Test
    void eachRangeSegments() {
        SegmentBuffer<Integer> buffer = new SegmentBuffer();
        buffer.addCompleted(date, 1, 2, 3);
        buffer.addCompleted(date.plusDays(1), 4, 5, 6);

        assertIterableEquals(I.list(1, 2, 3, 4, 5, 6), buffer.each().toList());
    }
}
