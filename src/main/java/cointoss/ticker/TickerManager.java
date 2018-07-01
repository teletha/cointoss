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
    private final Ticker2[] tickers = new Ticker2[size];

    /**
     * 
     */
    public TickerManager() {
        for (int i = 0; i < size; i++) {
            tickers[i] = new Ticker2(TickSpan.values()[i]);
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

    public int count;

    /**
     * Update tick.
     * 
     * @param execution
     */
    public void update(Execution execution) {
        Num price = execution.price;

        count++;
        updateTicker(tickers[0], execution, price.compareTo(base.latestPrice), price);

        // Confirm that the high price is updated in order from the top ticker.
        // If there is an update, it is considered that all tickers below it are updated as well.
        // count++;
        // root: switch (price.compareTo(base.latestPrice)) {
        // case 1:
        // for (int i = index; i < size; i++) {
        // Tick tick = tickers[i].last;
        //
        // count++;
        // if (price.isGreaterThan(tick.highPrice)) {
        // tick.highPrice = price;
        // } else {
        // break root;
        // }
        // }
        // break;
        //
        // case -1:
        // for (int i = index; i < size; i++) {
        // Tick tick = tickers[i].last;
        //
        // count++;
        // if (price.isLessThan(tick.lowPrice)) {
        // tick.lowPrice = price;
        // } else {
        // break root;
        // }
        // }
        // break;
        // }

        // for (int i = index; i < size; i++) {
        // Tick tick = tickers[i].last;
        //
        // count++;
        // if (price.isGreaterThan(tick.highPrice)) {
        // tick.highPrice = price;
        // } else if (price.isLessThan(tick.lowPrice)) {
        // count++;
        // tick.lowPrice = price;
        // } else {
        // count++;
        // break;
        // }
        // }

        // update base
        base.update(execution);

        for (Ticker2 ticker : tickers) {
            ticker.updaters.accept(ticker.last);
        }
    }

    /**
     * Update all tickers.
     * 
     * @param ticker
     * @param execution
     */
    private void updateTicker(Ticker2 ticker, Execution execution, int type, Num price) {
        if (ticker.update(execution, base)) {
            for (int index : ticker.span.associations) {
                updateTicker(tickers[index], execution, type, price);
            }
        } else {
            switch (type) {
            case 1:
                for (int i = ticker.span.ordinal(); i < size; i++) {
                    Tick tick = tickers[i].last;

                    count++;
                    if (price.isGreaterThan(tick.highPrice)) {
                        tick.highPrice = price;
                    } else {
                        return;
                    }
                }
                return;

            case -1:
                for (int i = ticker.span.ordinal(); i < size; i++) {
                    Tick tick = tickers[i].last;

                    count++;
                    if (price.isLessThan(tick.lowPrice)) {
                        tick.lowPrice = price;
                    } else {
                        return;
                    }
                }
                return;
            }
        }
    }
}
