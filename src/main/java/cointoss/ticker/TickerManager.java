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

    /** The managed tickers. */
    private final Ticker[] tickers = new Ticker[size];

    /**
     * 
     */
    public TickerManager() {
        for (int i = 0; i < size; i++) {
            tickers[i] = new Ticker(TickSpan.values()[i]);
        }
    }

    /**
     * Retrieve the {@link Ticker} by {@link TickSpan}.
     * 
     * @param span The target {@link TickSpan}.
     */
    public Ticker tickerBy(TickSpan span) {
        return tickers[span.ordinal()];
    }

    /**
     * Retrieve all {@link Ticker}s.
     * 
     * @return
     */
    public Signal<Ticker> tickers() {
        return I.signal(tickers);
    }

    public int count;

    /**
     * Update tick.
     * 
     * @param execution
     */
    public void update(Execution execution) {
        int index = updateTicker(tickers[0], execution, 0);
        Num price = execution.price;

        // Confirm that the high price is updated in order from the top ticker.
        // If there is an update, it is considered that all tickers below it are updated as well.
        count++;
        switch (price.compareTo(base.latestPrice)) {
        case 1:
            for (int i = index; i < size; i++) {
                Tick tick = tickers[i].currentTick;

                count++;
                if (price.isGreaterThan(tick.highPrice)) {
                    tick.highPrice = price;
                } else {
                    break;
                }
            }
            break;

        case -1:
            for (int i = index; i < size; i++) {
                Tick tick = tickers[i].currentTick;

                count++;
                if (price.isLessThan(tick.lowPrice)) {
                    tick.lowPrice = price;
                } else {
                    break;
                }
            }
            break;
        }

        // update base
        base.update(execution);

        for (Ticker ticker : tickers) {
            ticker.updaters.accept(ticker.currentTick);
        }
    }

    /**
     * Update all tickers.
     * 
     * @param ticker
     * @param execution
     */
    private int updateTicker(Ticker ticker, Execution execution, int id) {
        if (ticker.update(execution, base)) {
            // added the new tick
            id++;
            for (int index : ticker.span.associations) {
                id = updateTicker(tickers[index], execution, id);
            }
            return id;
        } else {
            // kept the current tick
            return id;
        }
    }
}
