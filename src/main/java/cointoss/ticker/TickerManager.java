/*
 * Copyright (C) 2019 CoinToss Development Team
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          https://opensource.org/licenses/MIT
 */
package cointoss.ticker;

import cointoss.Direction;
import cointoss.Market;
import cointoss.execution.Execution;
import cointoss.util.Num;
import kiss.I;
import kiss.Signal;
import kiss.Variable;

/**
 * @version 2018/07/06 10:21:22
 */
public final class TickerManager {

    /** The initial execution. */
    public final Variable<Execution> initial = Variable.empty();

    /** The latest execution. */
    public final Variable<Execution> latest = Variable.of(Market.BASE);

    public final RealtimeTicker realtime = new RealtimeTicker(TickSpan.Minute1, latest);

    /** Total of long volume since application startup. */
    Num longVolume = Num.ZERO;

    /** Total of long price increase since application startup. */
    Num longPriceIncrease = Num.ZERO;

    /** Total of short volume since application startup. */
    Num shortVolume = Num.ZERO;

    /** Total of short price decrease since application startup. */
    Num shortPriceDecrease = Num.ZERO;

    /** The number of tickers. */
    private final int size = TickSpan.values().length;

    /** The managed tickers. */
    private final Ticker[] tickers = new Ticker[size];

    /** The initialization state. */
    private boolean initialized;

    /**
     * Create {@link TickerManager}.
     */
    public TickerManager() {
        for (int i = size - 1; 0 <= i; i--) {
            Ticker ticker = tickers[i] = new Ticker(TickSpan.values()[i]);

            // cache associated upper tickers
            int index = 0;
            for (int upper : ticker.span.uppers) {
                ticker.uppers[index++] = this.tickers[upper];
            }
        }
    }

    /**
     * Retrieve the {@link Ticker} by {@link TickSpan}.
     * 
     * @param span The target {@link TickSpan}.
     */
    public Ticker of(TickSpan span) {
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
     * @param e The latest {@link Execution}.
     */
    public void update(Execution e) {
        if (initialized == false) {
            // initialize tickers once if needed
            initialized = true;
            initial.set(e);

            for (Ticker ticker : tickers) {
                ticker.init(e, this);
            }
        } else {
            // update tickers
            update(tickers[0], e, e.price, e.price.compareTo(latest.v.price));
        }

        // update totality of related values
        if (e.direction == Direction.BUY) {
            longVolume = longVolume.plus(e.size);
            longPriceIncrease = longPriceIncrease.plus(e.price.minus(latest.v.price));
        } else {
            shortVolume = shortVolume.plus(e.size);
            shortPriceDecrease = shortPriceDecrease.plus(latest.v.price.minus(e.price));
        }

        // update the latest execution at last
        latest.set(e);

        // notify update event
        for (Ticker ticker : tickers) {
            ticker.updaters.accept(ticker.current);
        }
    }

    /**
     * Update the specified {@link Ticker}.
     * 
     * @param ticker A target ticker to update.
     * @param execution A latest {@link Execution}.
     * @param price A latest price to cache.
     * @param comparisonResult The comparison result between previous price and current price.
     */
    private void update(Ticker ticker, Execution execution, Num price, int comparisonResult) {
        if (ticker.createTick(execution, this)) {
            for (Ticker upper : ticker.uppers) {
                update(upper, execution, price, comparisonResult);
            }
        } else {
            // If a new tick is not added, the maximum value and the minimum value will be updated.
            switch (comparisonResult) {
            case 1:
                // If it is higher than the previous price, since it is impossible to update the
                // minimum price in all upper tickers, only update the maximum price.
                updateHighPrice(ticker, price);
                break;
            case -1:
                // If it is lower than the previous price, since it is impossible to update the
                // maximum price in all upper tickers, only update the minimum price.
                updateLowPrice(ticker, price);
                break;
            }
        }
    }

    /**
     * Update high price of the specified {@link Ticker}.
     * 
     * @param ticker A target {@link Ticker} to update high price.
     * @param price A current price.
     */
    private void updateHighPrice(Ticker ticker, Num price) {
        if (price.isGreaterThan(ticker.current.highPrice)) {
            ticker.current.highPrice = price;

            for (Ticker upper : ticker.uppers) {
                updateHighPrice(upper, price);
            }
        }
    }

    /**
     * Update low price of the specified {@link Ticker}.
     * 
     * @param ticker A target {@link Ticker} to update low price.
     * @param price A current price.
     */
    private void updateLowPrice(Ticker ticker, Num price) {
        if (price.isLessThan(ticker.current.lowPrice)) {
            ticker.current.lowPrice = price;

            for (Ticker upper : ticker.uppers) {
                updateLowPrice(upper, price);
            }
        }
    }
}
