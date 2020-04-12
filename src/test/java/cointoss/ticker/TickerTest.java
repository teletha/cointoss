/*
 * Copyright (C) 2019 Nameless Production Committee
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          http://opensource.org/licenses/mit-license.php
 */
package cointoss.ticker;

import java.util.List;

import org.junit.jupiter.api.Test;

import cointoss.execution.Execution;

class TickerTest extends TickerTestSupport {

    @Test
    void open() {
        Ticker ticker = manager.on(Span.Second5);
        List<Tick> open = ticker.open.toList();
        assert open.size() == 0;

        manager.update(Execution.with.buy(1).price(10));
        assert open.size() == 1;
        manager.update(Execution.with.buy(1).price(20));
        assert open.size() == 1;
        manager.update(Execution.with.buy(1).price(10).date(afterSecond(5)));
        assert open.size() == 2;
        manager.update(Execution.with.buy(1).price(10).date(afterSecond(11)));
        assert open.size() == 3;
    }

    @Test
    void openSkipEmptyTick() {
        Ticker ticker = manager.on(Span.Second5);
        List<Tick> open = ticker.open.toList();
        assert open.size() == 0;

        manager.update(Execution.with.buy(1).price(10));
        assert open.size() == 1;
        manager.update(Execution.with.buy(1).price(10).date(afterSecond(20)));
        assert open.size() == 2;
    }

    @Test
    void close() {
        Ticker ticker = manager.on(Span.Second5);
        List<Tick> close = ticker.close.toList();
        assert close.size() == 0;

        manager.update(Execution.with.buy(1).price(10));
        assert close.size() == 0;
        manager.update(Execution.with.buy(1).price(20));
        assert close.size() == 0;
        manager.update(Execution.with.buy(1).price(10).date(afterSecond(5)));
        assert close.size() == 1;
        manager.update(Execution.with.buy(1).price(10).date(afterSecond(11)));
        assert close.size() == 2;
    }

    @Test
    void closeSkipEmptyTick() {
        Ticker ticker = manager.on(Span.Second5);
        List<Tick> close = ticker.close.toList();
        assert close.size() == 0;

        manager.update(Execution.with.buy(1).price(10));
        assert close.size() == 0;
        manager.update(Execution.with.buy(1).price(10).date(afterSecond(20)));
        assert close.size() == 1;
    }
}
