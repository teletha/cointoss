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

import cointoss.execution.Execution;
import cointoss.market.TestableMarketService;
import cointoss.util.Chrono;
import cointoss.util.TimebaseSupport;

public class TickerTestSupport implements TimebaseSupport {

    protected final TestableMarketService service = new TestableMarketService();

    protected final TestableTickerManager manager = new TestableTickerManager(service);

    /**
     * Build {@link Ticker} simply with your values.
     * 
     * @param span
     * @param values
     * @return
     */
    public Ticker ticker(Span span, int... values) {
        ZonedDateTime time = Chrono.MIN;

        for (int value : values) {
            manager.update(Execution.with.buy(value).price(value).date(time));
            time = time.plus(span.duration);
        }
        return manager.on(span);
    }

    /**
     * Build {@link Ticker} simply with your values.
     * 
     * @param span
     * @param values
     * @return
     */
    public Ticker ticker(Span span, double... values) {
        ZonedDateTime time = Chrono.MIN;

        for (double value : values) {
            manager.update(Execution.with.buy(value).price(value).date(time));
            time = time.plus(span.duration);
        }
        return manager.on(span);
    }

    /**
     * Build {@link Tick} simply with tou values.
     * 
     * @param span
     * @param open
     * @param high
     * @param low
     * @param close
     * @return
     */
    public Tick tick(Span span, int open, int high, int low, int close) {
        if (high < open || high < low || high < close) {
            throw new IllegalArgumentException("High price is not highest. [open:" + open + " high:" + high + " low:" + low + " close:" + close + "]");
        }

        ZonedDateTime time = Chrono.MIN;

        manager.update(Execution.with.buy(1).price(open).date(time));
        manager.update(Execution.with.buy(1).price(high).date(time));
        manager.update(Execution.with.buy(1).price(low).date(time));
        manager.update(Execution.with.buy(1).price(close).date(time));

        return manager.on(span).current;
    }
}