/*
 * Copyright (C) 2018 Nameless Production Committee
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          http://opensource.org/licenses/mit-license.php
 */
package cointoss.ticker;

import cointoss.Execution;
import cointoss.util.Num;
import kiss.I;
import kiss.Signal;

/**
 * @version 2018/06/30 1:22:20
 */
public class TickerManager {

    /** The base tick. */
    private final BaseStatistics base = new BaseStatistics();

    /** The number of tickers. */
    private final int size = TickSpan.values().length;

    /** The actual tickers. */
    private final Ticker2[] tickers = new Ticker2[size];

    /**
     * 
     */
    public TickerManager() {
        TickSpan[] spans = TickSpan.values();

        for (int i = 0; i < spans.length; i++) {
            tickers[i] = new Ticker2(spans[i]);
        }
    }

    /**
     * Retrieve the {@link Ticker2} by {@link TickSpan}.
     * 
     * @param span The target {@link TickSpan}.
     */
    public Ticker2 tickerBy(TickSpan span) {
        return tickers[span.ordinal()];
    }

    /**
     * Retrieve all {@link Ticker2}s.
     * 
     * @return
     */
    public Signal<Ticker2> tickers() {
        return I.signal(tickers);
    }

    /**
     * Update tick.
     * 
     * @param execution
     */
    public void update(Execution execution) {
        updateTicker(tickers[0], execution);

        // update base
        base.update(execution);

        Num price = execution.price;

        // Confirm that the high price is updated in order from the top ticker.
        // If there is an update, it is considered that all tickers below it are updated as well.
        for (int i = 0; i < size; i++) {
            Tick tick = tickers[i].last;

            if (price.isGreaterThan(tick.highPrice)) {
                tick.highPrice = price;
            } else {
                break;
            }
        }

        for (int i = 0; i < size; i++) {
            Tick tick = tickers[i].last;

            if (price.isLessThan(tick.lowPrice)) {
                tick.lowPrice = price;
            } else {
                break;
            }
        }
    }

    /**
     * Update all tickers.
     * 
     * @param ticker
     * @param execution
     */
    private void updateTicker(Ticker2 ticker, Execution execution) {
        if (ticker.update(execution, base)) {
            for (int index : ticker.span.associations) {
                updateTicker(tickers[index], execution);
            }
        }
    }
}
