/*
 * Copyright (C) 2020 cointoss Development Team
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          https://opensource.org/licenses/MIT
 */
package cointoss.ticker;

import org.junit.jupiter.api.Test;

import cointoss.execution.Execution;

class TickTest extends TickerTestSupport {

    @Test
    void typicalPrice() {
        Tick tick = tick(Span.Second5, 10, 15, 4, 8);
        assert tick.typicalPrice().is(9);
    }

    @Test
    void medianPrice() {
        Tick tick = tick(Span.Second5, 10, 15, 4, 8);
        assert tick.medianPrice().is(9.5);
    }

    @Test
    void isBear() {
        Tick tick = tick(Span.Second5, 10, 15, 4, 8);
        assert tick.isBear() == true;

        tick = tick(Span.Second5, 10, 15, 4, 12);
        assert tick.isBear() == false;
    }

    @Test
    void isBull() {
        Tick tick = tick(Span.Second5, 10, 15, 4, 8);
        assert tick.isBull() == false;

        tick = tick(Span.Second5, 10, 15, 4, 12);
        assert tick.isBull() == true;
    }

    @Test
    void longCount() {
        Ticker ticker = manager.on(Span.Second5);
        manager.update(Execution.with.buy(1).price(10));

        Tick tick = ticker.ticks.last();
        assert tick.longCount() == 1;

        manager.update(Execution.with.buy(1).price(10));
        assert tick.longCount() == 2;

        manager.update(Execution.with.sell(1).price(10));
        assert tick.longCount() == 2;
    }

    @Test
    void shortCount() {
        Ticker ticker = manager.on(Span.Second5);
        manager.update(Execution.with.sell(1).price(10));

        Tick tick = ticker.ticks.last();
        assert tick.shortCount() == 1;

        manager.update(Execution.with.sell(1).price(10));
        assert tick.shortCount() == 2;

        manager.update(Execution.with.buy(1).price(10));
        assert tick.shortCount() == 2;
    }
}