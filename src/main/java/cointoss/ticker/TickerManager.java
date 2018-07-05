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
import cointoss.Side;
import cointoss.util.Num;
import kiss.I;
import kiss.Signal;
import kiss.Variable;

/**
 * @version 2018/07/05 10:16:44
 */
public final class TickerManager {

    /** The initial execution. */
    public final Variable<Execution> initialExecution = Variable.empty();

    /** The latest execution. */
    public final Variable<Execution> latestExecution = Variable.of(Execution.BASE);

    /** The number of tickers. */
    private final int size = TickSpan.values().length;

    /** The managed tickers. */
    private final Ticker[] tickers = new Ticker[size];

    /** The initialization state. */
    private boolean initialized;

    /** Total of long volume since application startup. */
    Num longVolume = Num.ZERO;

    /** Total of long price increase since application startup. */
    Num longPriceIncrease = Num.ZERO;

    /** Total of short volume since application startup. */
    Num shortVolume = Num.ZERO;

    /** Total of short price decrease since application startup. */
    Num shortPriceDecrease = Num.ZERO;

    /**
     * Create {@link TickerManager}.
     */
    public TickerManager() {
        for (int i = size - 1; 0 <= i; i--) {
            Ticker ticker = tickers[i] = new Ticker(TickSpan.values()[i]);

            // cache associated tickers
            int index = 0;
            for (int association : ticker.span.uppers) {
                ticker.uppers[index++] = this.tickers[association];
            }
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
            initialExecution.set(execution);

            for (Ticker ticker : tickers) {
                ticker.init(execution, this);
            }
        }

        Num price = execution.price;

        // update tickers
        update(tickers[0], execution, price, price.compareTo(latestExecution.v.price));

        // update totality of related values
        if (execution.side == Side.BUY) {
            longVolume = longVolume.plus(execution.size);
            longPriceIncrease = longPriceIncrease.plus(price.minus(latestExecution.v.price));
        } else {
            shortVolume = shortVolume.plus(execution.size);
            shortPriceDecrease = shortPriceDecrease.plus(latestExecution.v.price.minus(price));
        }

        // update the latest execution at last
        latestExecution.set(execution);

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
