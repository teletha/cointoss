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

import org.junit.jupiter.api.Test;

class SegmentBufferTest {

    @Test
    void isEmpty() {
        SegmentBuffer<Integer> buffer = new SegmentBuffer<>(Span.Minute1, Integer::longValue);
        assert buffer.isEmpty();

        buffer.add(1);
        assert buffer.isEmpty() == false;
    }

    @Test
    void isNotEmpty() {
        SegmentBuffer<Integer> buffer = new SegmentBuffer<>(Span.Minute1, Integer::longValue);
        assert buffer.isNotEmpty() == false;

        buffer.add(1);
        assert buffer.isNotEmpty();
    }

    @Test
    void add() {
        SegmentBuffer<Integer> buffer = new SegmentBuffer<>(Span.Second5, Integer::longValue);
        buffer.add(0);
        assert buffer.at(0) == 0;
        assert buffer.at(5) == null;
        assert buffer.at(10) == null;

        // update
        buffer.add(2);
        assert buffer.at(0) == 2;
        assert buffer.at(5) == null;
        assert buffer.at(10) == null;

        // add next stamp
        buffer.add(5);
        assert buffer.at(0) == 2;
        assert buffer.at(5) == 5;
        assert buffer.at(10) == null;

        // add next stamp
        buffer.add(10);
        assert buffer.at(0) == 2;
        assert buffer.at(5) == 5;
        assert buffer.at(10) == 10;

        // update
        buffer.add(13);
        assert buffer.at(0) == 2;
        assert buffer.at(5) == 5;
        assert buffer.at(10) == 13;
    }

    @Test
    void get() {
        SegmentBuffer<Integer> buffer = new SegmentBuffer<>(Span.Second5, Integer::longValue);
        buffer.add(0);
        assert buffer.get(0) == 0;
        assert buffer.get(5) == null;
        assert buffer.get(10) == null;
    }

    @Test
    void first() {
        SegmentBuffer<Integer> buffer = new SegmentBuffer<>(Span.Second5, Integer::longValue);
        buffer.add(25);
        assert buffer.first() == 25;

        buffer.add(30);
        assert buffer.first() == 25;

        buffer.add(15);
        assert buffer.first() == 15;

        buffer.add(17);
        assert buffer.first() == 17;

        buffer.add(13);
        assert buffer.first() == 13;

        buffer.add(10);
        assert buffer.first() == 10;
    }

    @Test
    void last() {
        SegmentBuffer<Integer> buffer = new SegmentBuffer<>(Span.Second5, Integer::longValue);
        buffer.add(5);
        assert buffer.last() == 5;

        buffer.add(10);
        assert buffer.last() == 10;

        buffer.add(12);
        assert buffer.last() == 12;

        buffer.add(20);
        assert buffer.last() == 20;

        buffer.add(16);
        assert buffer.last() == 20;

        buffer.add(30);
        assert buffer.last() == 30;
    }

    @Test
    void size() {
        SegmentBuffer<Integer> buffer = new SegmentBuffer<>(Span.Second5, Integer::longValue);
        assert buffer.size() == 0;

        buffer.add(5);
        assert buffer.size() == 1;

        buffer.add(10);
        assert buffer.size() == 2;

        // update will not modify size
        buffer.add(5);
        buffer.add(6);
        buffer.add(7);
        assert buffer.size() == 2;
    }
}
