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

/**
 * @version 2018/08/07 1:49:30
 */
public class DateSegmentBufferTest {

    @Test
    void add() {
        DateSegmentBuffer buffer = new DateSegmentBuffer();
        assert buffer.size() == 0;

        buffer.add(1);
        assert buffer.size() == 1;

        buffer.add(2);
        assert buffer.size() == 2;
    }

    @Test
    void get() {
        DateSegmentBuffer<Integer> buffer = new DateSegmentBuffer();
        buffer.add(0);
        buffer.add(1);
        buffer.add(2);
        buffer.add(3);

        assert buffer.get(0) == 0;
        assert buffer.get(1) == 1;
        assert buffer.get(2) == 2;
        assert buffer.get(3) == 3;
    }

    @Test
    void first() {
        DateSegmentBuffer<Integer> buffer = new DateSegmentBuffer();
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
        DateSegmentBuffer<Integer> buffer = new DateSegmentBuffer();
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
