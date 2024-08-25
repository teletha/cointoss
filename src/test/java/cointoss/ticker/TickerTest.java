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

import java.time.ZonedDateTime;
import java.util.List;

import org.junit.jupiter.api.Test;

import cointoss.execution.Execution;
import cointoss.util.Chrono;

class TickerTest extends TickerTestSupport {

    @Test
    void open() {
        Ticker ticker = manager.on(Span.Minute1);
        List<Tick> open = ticker.open.toList();
        assert open.size() == 0;

        manager.update(Execution.with.buy(1).price(10));
        assert open.size() == 0;
        manager.update(Execution.with.buy(1).price(20).date(afterMinute(1)));
        assert open.size() == 1;
        manager.update(Execution.with.buy(1).price(10).date(afterMinute(2)));
        assert open.size() == 2;
        manager.update(Execution.with.buy(1).price(10).date(afterMinute(3)));
        assert open.size() == 3;
    }

    @Test
    void openSkipEmptyTick() {
        Ticker ticker = manager.on(Span.Minute1);
        List<Tick> open = ticker.open.toList();
        assert open.size() == 0;

        manager.update(Execution.with.buy(1).price(10).date(afterMinute(1)));
        assert open.size() == 1;
        manager.update(Execution.with.buy(1).price(10).date(afterMinute(20)));
        assert open.size() == 2;
    }

    @Test
    void close() {
        Ticker ticker = manager.on(Span.Minute1);
        List<Tick> close = ticker.close.toList();
        assert close.size() == 0;

        manager.update(Execution.with.buy(1).price(10));
        assert close.size() == 0;
        manager.update(Execution.with.buy(1).price(20).date(afterMinute(1)));
        assert close.size() == 1;
        manager.update(Execution.with.buy(1).price(10).date(afterMinute(2)));
        assert close.size() == 2;
        manager.update(Execution.with.buy(1).price(10).date(afterMinute(3)));
        assert close.size() == 3;
    }

    @Test
    void latestIsLastCache() {
        ZonedDateTime start = Chrono.utc(2020, 1, 1);
        ZonedDateTime end = Chrono.utc(2020, 1, 7);

        manager.generateTicker(start, end, Span.Minute1);
    }
}