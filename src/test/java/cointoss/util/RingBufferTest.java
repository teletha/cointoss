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
        assert buffer.latest(3) == 5;
        buffer.add(6);
        assert buffer.latest(3) == 6;
    }
}
