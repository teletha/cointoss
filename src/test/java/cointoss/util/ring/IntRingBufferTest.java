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

import java.util.function.ToIntFunction;

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
        ToIntFunction<IntRingBuffer> total = buffer -> {
            int[] all = new int[1];
            buffer.forEach(value -> {
                all[0] += value;
            });
            return all[0];
        };

        IntRingBuffer buffer = new IntRingBuffer(3);
        buffer.add(1);
        buffer.add(2);
        buffer.add(3);
        assert total.applyAsInt(buffer) == 6;

        buffer.add(4);
        assert total.applyAsInt(buffer) == 9;

        buffer.add(5);
        assert total.applyAsInt(buffer) == 12;
    }
}
