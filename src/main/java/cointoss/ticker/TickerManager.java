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
 * @version 2018/07/03 18:06:20
 */
public class TickerManager {

    /** The total sum. */
    private final Totality totality = new Totality();

    /** The number of tickers. */
    private final int size = TickSpan.values().length;

    /** The managed tickers. */
    private final Ticker[] tickers = new Ticker[size];

    /** The initialization state. */
    private boolean initialized;

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

    /**
     * Update all {@link Ticker}s by {@link Execution}.
     * 
     * @param execution The latest {@link Execution}.
     */
    public void update(Execution execution) {
        // initialize tickers once if needed
        if (initialized == false) {
            initialized = true;

            for (Ticker ticker : tickers) {
                ticker.init(execution, totality);
            }
        }

        Num price = execution.price;

        update(tickers[0], execution, price, price.compareTo(totality.latestPrice));

        // update base
        totality.update(execution);

        // notify update event
        for (Ticker ticker : tickers) {
            ticker.updaters.accept(ticker.current);
        }
    }

    private void update(Ticker ticker, Execution execution, Num price, int compare) {
        if (ticker.createTick(execution, totality)) {
            for (int index : ticker.span.associations) {
                update(tickers[index], execution, price, compare);
            }
        } else {
            switch (compare) {
            case 1:
                updateHighPrice(ticker, price);
                break;
            case -1:
                updateLowPrice(ticker, price);
                break;
            }
        }
    }

    private void updateHighPrice(Ticker ticker, Num price) {
        if (price.isGreaterThan(ticker.current.highPrice)) {
            ticker.current.highPrice = price;

            for (int index : ticker.span.associations) {
                updateHighPrice(tickers[index], price);
            }
        }
    }

    private void updateLowPrice(Ticker ticker, Num price) {
        if (price.isLessThan(ticker.current.lowPrice)) {
            ticker.current.lowPrice = price;

            for (int index : ticker.span.associations) {
                updateLowPrice(tickers[index], price);
            }
        }
    }
}
