/*
 * Copyright (C) 2024 The COINTOSS Development Team
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          https://opensource.org/licenses/MIT
 */
package cointoss.ticker;

import org.junit.jupiter.api.Test;

class TickTest extends TickerTestSupport {

    @Test
    void typicalPrice() {
        Tick tick = manager.generateTick(Span.Minute5, 10, 15, 4, 8);
        assert tick.typicalPrice() == 9;
    }

    @Test
    void medianPrice() {
        Tick tick = manager.generateTick(Span.Minute5, 10, 15, 4, 8);
        assert tick.medianPrice() == 9.5;
    }

    @Test
    void isBear() {
        Tick tick = manager.generateTick(Span.Minute5, 10, 15, 4, 8);
        assert tick.isBear() == true;

        tick = manager.generateTick(Span.Minute5, 10, 15, 4, 12);
        assert tick.isBear() == false;
    }

    @Test
    void isBull() {
        Tick tick = manager.generateTick(Span.Minute5, 10, 15, 4, 8);
        assert tick.isBull() == false;

        tick = manager.generateTick(Span.Minute5, 10, 15, 4, 12);
        assert tick.isBull() == true;
    }

    @Test
    void upperPrice() {
        Tick bear = manager.generateTick(Span.Minute5, 10, 15, 4, 8);
        assert bear.upperPrice() == 10;

        Tick bull = manager.generateTick(Span.Minute5, 10, 15, 4, 15);
        assert bull.upperPrice() == 15;
    }

    @Test
    void lowerPrice() {
        Tick bear = manager.generateTick(Span.Minute5, 10, 15, 4, 8);
        assert bear.lowerPrice() == 8;

        Tick bull = manager.generateTick(Span.Minute5, 10, 15, 4, 15);
        assert bull.lowerPrice() == 10;
    }
}