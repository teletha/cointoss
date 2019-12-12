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

import cointoss.util.Chrono;

class SegmentBufferTest {

    int size = 10000;

    long now = Chrono.utcNow().toEpochSecond();

    @Test
    void first() {
        SegmentBuffer<Long> buffer = new SegmentBuffer<>(Span.Minute1, Long::longValue);
        for (int i = 0; i < size; i++) {
            buffer.add(now + i);
            assert buffer.first() == now;
        }
    }

    @Test
    void last() {
        SegmentBuffer<Long> buffer = new SegmentBuffer<>(Span.Minute1, Long::longValue);
        for (int i = 0; i < size; i++) {
            buffer.add(now + i);
            assert buffer.last() == now + i;
        }
    }
}
