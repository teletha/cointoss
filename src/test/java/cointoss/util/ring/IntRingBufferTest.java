/*
 * Copyright (C) 2020 Nameless Production Committee
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          http://opensource.org/licenses/mit-license.php
 */
package cointoss.util.ring;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;

import org.junit.jupiter.api.Test;

class IntRingBufferTest {

    @Test
    void add() {
        IntRingBuffer buffer = new IntRingBuffer(3);
        assert buffer.add(1) == 0;
        assert buffer.add(2) == 0;
        assert buffer.add(3) == 0;
        assert buffer.add(4) == 1;
        assert buffer.add(5) == 2;
        assert buffer.add(6) == 3;
        assert buffer.add(7) == 4;
        assert buffer.add(8) == 5;
    }

    @Test
    void forEach() {
        Function<IntRingBuffer, int[]> array = buffer -> {
            AtomicInteger index = new AtomicInteger();
            int[] all = new int[3];
            buffer.forEach(value -> {
                all[index.getAndIncrement()] = value;
            });
            return all;
        };

        IntRingBuffer buffer = new IntRingBuffer(3);
        buffer.add(1);
        buffer.add(2);
        buffer.add(3);
        int[] set = array.apply(buffer);
        assert set[0] == 1;
        assert set[1] == 2;
        assert set[2] == 3;

        buffer.add(4);
        set = array.apply(buffer);
        assert set[0] == 2;
        assert set[1] == 3;
        assert set[2] == 4;

        buffer.add(5);
        set = array.apply(buffer);
        assert set[0] == 3;
        assert set[1] == 4;
        assert set[2] == 5;
    }

    @Test
    void forEachFromLatest() {
        Function<IntRingBuffer, int[]> array = buffer -> {
            AtomicInteger index = new AtomicInteger();
            int[] all = new int[3];
            buffer.forEachFromLatest(value -> {
                all[index.getAndIncrement()] = value;
            });
            return all;
        };

        IntRingBuffer buffer = new IntRingBuffer(3);
        buffer.add(1);
        buffer.add(2);
        buffer.add(3);
        int[] set = array.apply(buffer);
        assert set[0] == 3;
        assert set[1] == 2;
        assert set[2] == 1;

        buffer.add(4);
        set = array.apply(buffer);
        assert set[0] == 4;
        assert set[1] == 3;
        assert set[2] == 2;

        buffer.add(5);
        set = array.apply(buffer);
        assert set[0] == 5;
        assert set[1] == 4;
        assert set[2] == 3;
    }

    @Test
    void reduce() {
        IntRingBuffer buffer = new IntRingBuffer(3);
        buffer.add(1);
        buffer.add(2);
        buffer.add(3);
        assert buffer.reduce(Math::max) == 3;

        buffer.add(4);
        assert buffer.reduce(Math::max) == 4;

        buffer.add(3);
        assert buffer.reduce(Math::max) == 4;
    }
}
