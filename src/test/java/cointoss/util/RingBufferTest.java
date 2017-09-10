/*
 * Copyright (C) 2017 Nameless Production Committee
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          http://opensource.org/licenses/mit-license.php
 */
package cointoss.util;

import org.junit.Test;

/**
 * @version 2017/09/10 9:27:14
 */
public class RingBufferTest {

    @Test
    public void latest() throws Exception {
        RingBuffer<Integer> buffer = new RingBuffer<>(3);
        buffer.add(1);
        assert buffer.latest() == 1;
        buffer.add(2);
        assert buffer.latest() == 2;
        buffer.add(3);
        assert buffer.latest() == 3;
        buffer.add(4);
        assert buffer.latest() == 4;
        buffer.add(5);
        assert buffer.latest() == 5;
        buffer.add(6);
        assert buffer.latest() == 6;
    }

    @Test
    public void latestOffset() throws Exception {
        RingBuffer<Integer> buffer = new RingBuffer<>(3);
        buffer.add(1);
        assert buffer.latest(1) == null;
        buffer.add(2);
        assert buffer.latest(1) == 1;
        buffer.add(3);
        assert buffer.latest(2) == 1;
        buffer.add(4);
        assert buffer.latest(2) == 2;
        buffer.add(5);
        assert buffer.latest(2) == 3;
        assert buffer.latest(3) == null;
        buffer.add(6);
        assert buffer.latest(2) == 4;
        assert buffer.latest(3) == null;
    }

    @Test
    public void set() throws Exception {
        RingBuffer<Integer> buffer = new RingBuffer<>(5);
        buffer.set(1, 1);
        assert buffer.get(0) == null;
        assert buffer.get(1) == 1;
        buffer.set(3, 3);
        assert buffer.get(2) == null;
        assert buffer.get(3) == 3;
        buffer.set(5, 5);
        assert buffer.get(4) == null;
        assert buffer.get(5) == 5;
        buffer.set(7, 7);
        assert buffer.get(0) == null;
        assert buffer.get(1) == null;
        assert buffer.get(2) == null;
        assert buffer.get(3) == 3;
        assert buffer.get(4) == null;
        assert buffer.get(5) == 5;
        assert buffer.get(6) == null;
        assert buffer.get(7) == 7;
    }
}
