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

import org.junit.jupiter.api.Test;

import cointoss.util.SegmentBuffer.UncompletedSegment;
import kiss.I;

/**
 * @version 2018/08/11 7:52:02
 */
class UncompletedSegmentTest {

    @Test
    void add() {
        UncompletedSegment buffer = new UncompletedSegment(null);
        assert buffer.size == 0;

        buffer.add(1);
        assert buffer.size == 1;

        buffer.add(2);
        assert buffer.size == 2;
    }

    @Test
    void get() {
        UncompletedSegment<Integer> buffer = new UncompletedSegment(null);
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
        UncompletedSegment<Integer> buffer = new UncompletedSegment(null);
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
        UncompletedSegment<Integer> buffer = new UncompletedSegment(null);
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
        UncompletedSegment<Integer> buffer = new UncompletedSegment(null);
        buffer.add(0);
        buffer.add(1);
        buffer.add(2);

        assertIterableEquals(I.list(0, 1, 2), buffer.each(0, 3).toList());
    }
}
