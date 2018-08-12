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

import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.junit.jupiter.api.Test;

import kiss.I;

/**
 * @version 2018/08/11 7:52:02
 */
class UncompletedSegmentTest {

    @Test
    void add() {
        SegmentBuffer buffer = new SegmentBuffer();
        assert buffer.size() == 0;

        buffer.add(1);
        assert buffer.size() == 1;

        buffer.add(2);
        assert buffer.size() == 2;
    }

    @Test
    void get() {
        SegmentBuffer<Integer> buffer = new SegmentBuffer();
        int size = 1000000;
        for (int i = 0; i < size; i++) {
            buffer.add(i);
        }

        for (int i = 0; i < size; i++) {
            assert buffer.get(i) == i;
        }
    }

    @Test
    void first() {
        SegmentBuffer<Integer> buffer = new SegmentBuffer();
        buffer.add(0);
        assert buffer.first() == 0;
        buffer.add(1);
        assert buffer.first() == 0;
        buffer.add(2);
        assert buffer.first() == 0;
        buffer.add(3);
        assert buffer.first() == 0;
    }

    @Test
    void last() {
        SegmentBuffer<Integer> buffer = new SegmentBuffer();
        buffer.add(0);
        assert buffer.last() == 0;
        buffer.add(1);
        assert buffer.last() == 1;
        buffer.add(2);
        assert buffer.last() == 2;
        buffer.add(3);
        assert buffer.last() == 3;
    }

    @Test
    void each() {
        SegmentBuffer<Integer> buffer = new SegmentBuffer();
        for (int i = 0; i < 5; i++) {
            buffer.add(i);
        }

        assertIterableEquals(I.list(0, 1, 2), buffer.each(0, 3).toList());
        assertIterableEquals(I.list(1, 2, 3), buffer.each(1, 4).toList());
        assertIterableEquals(I.list(2, 3, 4), buffer.each(2, 5).toList());
        assertIterableEquals(I.list(3, 4), buffer.each(3, 6).toList());
        assertIterableEquals(I.list(4), buffer.each(4, 7).toList());
        assertIterableEquals(I.list(), buffer.each(5, 8).toList());
        assertIterableEquals(I.list(), buffer.each(6, 9).toList());
    }

    @Test
    void eachHuge() {
        int size = 1000000;
        SegmentBuffer<Integer> buffer = new SegmentBuffer();
        for (int i = 0; i < size; i++) {
            buffer.add(i);
        }

        assertIterableEquals(IntStream.range(0, size).boxed().collect(Collectors.toList()), buffer.each().toList());
    }
}
