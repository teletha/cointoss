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

import org.magicwerk.brownies.collections.BigList;

import cointoss.Execution;
import cointoss.util.Num;

/**
 * @version 2018/06/30 1:22:20
 */
public class TickerManager {

    /** The base tick. */
    private final Baseline base = new Baseline();

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
     * Update tick.
     * 
     * @param execution
     */
    public void update(Execution execution) {
        Num price = execution.price;

        // Confirm that the high price is updated in order from the top ticker.
        // If there is an update, it is considered that all tickers below it are updated as well.
        for (int i = size - 1; 0 <= i; i--) {
            if (price.isGreaterThan(tickers[i].last.highPrice)) {

            }
        }
    }

    /**
     * @version 2018/06/30 1:27:18
     */
    private class Baseline {

        private Num longVolume = Num.ZERO;

        private Num shortVolume = Num.ZERO;
    }

    /**
     * @version 2018/06/30 1:32:14
     */
    private static class Ticker2 {

        /** The tick manager. */
        private final BigList<Tick> ticks = new BigList();

        /** The latest tick. */
        private Tick last = null;

        /**
         * @param span
         */
        public Ticker2(TickSpan span) {
        }
    }
}
