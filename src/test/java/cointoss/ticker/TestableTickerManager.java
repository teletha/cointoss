/*
 * Copyright (C) 2025 The COINTOSS Development Team
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          https://opensource.org/licenses/MIT
 */
package cointoss.ticker;

import java.time.ZonedDateTime;

import cointoss.MarketService;
import cointoss.execution.Execution;
import cointoss.execution.Executions;
import cointoss.util.Chrono;

public class TestableTickerManager extends TickerManager {

    public TestableTickerManager() {
        super();
    }

    public TestableTickerManager(MarketService service) {
        super(service);
    }

    /**
     * Generate {@link Ticker} with random execution data.
     * 
     * @param start
     * @param end
     */
    public Ticker generateTicker(Span span, ZonedDateTime start, ZonedDateTime end) {
        Executions.random(start, end.plusDays(1), span).to(this::update);

        return on(span);
    }

    /**
     * Generate {@link Ticker} simply with your values.
     * 
     * @param span
     * @param values
     */
    public Ticker generateTicker(Span span, int... values) {
        ZonedDateTime time = Chrono.MIN;

        for (int value : values) {
            update(Execution.with.buy(value).price(value).date(time));
            time = time.plus(span.duration);
        }
        return on(span);
    }

    /**
     * Generate {@link Ticker} simply with your values.
     * 
     * @param span
     * @param values
     */
    public Ticker generateTicker(Span span, double... values) {
        ZonedDateTime time = Chrono.MIN;

        for (double value : values) {
            update(Execution.with.buy(value).price(value).date(time));
            time = time.plus(span.duration);
        }
        return on(span);
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
    public Tick generateTick(Span span, int open, int high, int low, int close) {
        if (high < open || high < low || high < close) {
            throw new IllegalArgumentException("High price is not highest. [open:" + open + " high:" + high + " low:" + low + " close:" + close + "]");
        }

        ZonedDateTime time = Chrono.MIN;

        update(Execution.with.buy(1).price(open).date(time));
        update(Execution.with.buy(1).price(high).date(time));
        update(Execution.with.buy(1).price(low).date(time));
        update(Execution.with.buy(1).price(close).date(time));

        return on(span).latest();
    }
}