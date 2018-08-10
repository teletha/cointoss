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

import org.junit.jupiter.api.Test;

import cointoss.util.DateSegmentBuffer.CompletedSegment;

/**
 * @version 2018/08/11 7:52:02
 */
public class DateSegmentTest {

    @Test
    void add() {
        CompletedSegment buffer = new CompletedSegment(null);
        assert buffer.size == 0;

        buffer.add(1);
        assert buffer.size == 1;

        buffer.add(2);
        assert buffer.size == 2;
    }

    @Test
    void get() {
        CompletedSegment<Integer> buffer = new CompletedSegment(null);
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
        CompletedSegment<Integer> buffer = new CompletedSegment(null);
        buffer.add(0);
        assert buffer.peekFirst() == 0;
        buffer.add(1);
        assert buffer.peekFirst() == 0;
        buffer.add(2);
        assert buffer.peekFirst() == 0;
        buffer.add(3);
        assert buffer.peekFirst() == 0;
    }

    @Test
    void last() {
        CompletedSegment<Integer> buffer = new CompletedSegment(null);
        buffer.add(0);
        assert buffer.peekLast() == 0;
        buffer.add(1);
        assert buffer.peekLast() == 1;
        buffer.add(2);
        assert buffer.peekLast() == 2;
        buffer.add(3);
        assert buffer.peekLast() == 3;
    }
}
