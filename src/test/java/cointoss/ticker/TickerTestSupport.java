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

import java.time.ZonedDateTime;

import cointoss.execution.Execution;
import cointoss.util.Chrono;

public class TickerTestSupport {

    /**
     * Build {@link Ticker} simply with your values.
     * 
     * @param span
     * @param values
     * @return
     */
    public static Ticker ticker(Span span, int... values) {
        TickerManager manager = new TickerManager();

        ZonedDateTime time = Chrono.MIN;

        for (int value : values) {
            manager.update(Execution.with.buy(value).price(value).date(time));
            time = time.plus(span.duration);
        }
        return manager.of(span);
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
    public static Tick tick(Span span, int open, int high, int low, int close) {
        if (high < open || high < low || high < close) {
            throw new IllegalArgumentException("High price is not highest. [open:" + open + " high:" + high + " low:" + low + " close:" + close + "]");
        }

        ZonedDateTime time = Chrono.MIN;

        TickerManager manager = new TickerManager();
        manager.update(Execution.with.buy(1).price(open).date(time));
        manager.update(Execution.with.buy(1).price(high).date(time));
        manager.update(Execution.with.buy(1).price(low).date(time));
        manager.update(Execution.with.buy(1).price(close).date(time));

        return manager.of(span).current;
    }
}
