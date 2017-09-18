/*
 * Copyright (C) 2017 Nameless Production Committee
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          http://opensource.org/licenses/mit-license.php
 */
package cointoss;

import org.junit.Before;

import eu.verdelhan.ta4j.Decimal;
import kiss.I;

/**
 * @version 2017/09/11 13:34:54
 */
public abstract class TradingTestSupport extends Trading {

    protected TestableMarket market;

    protected TradingLog log;

    /**
     * @param market
     */
    public TradingTestSupport() {
        super(new TestableMarket());

        this.market = (TestableMarket) super.market;
        this.market.tradings.add(this);
    }

    @Before
    public void init() {
        close();

        entries.clear();
    }

    /**
     * Entry and exit order.
     * 
     * @param side
     * @param entrySize
     * @param entryPrice
     * @param exitSize
     * @param exitPrice
     */
    protected final void entryAndExit(Side side, double entrySize, double entryPrice, double exitSize, double exitPrice) {
        entryAndExit(side, Decimal.valueOf(entrySize), Decimal.valueOf(entryPrice), Decimal.valueOf(exitSize), Decimal.valueOf(exitPrice));
    }

    /**
     * Entry and exit order.
     * 
     * @param side
     * @param entrySize
     * @param entryPrice
     * @param exitSize
     * @param exitPrice
     */
    protected final void entryAndExit(Side side, Decimal entrySize, Decimal entryPrice, Decimal exitSize, Decimal exitPrice) {
        entryLimit(side, entrySize, entryPrice, entry -> {
            market.execute(side, entrySize, entryPrice);

            entry.exitLimit(exitSize, exitPrice, exit -> {
                market.execute(side.inverse(), exitSize, exitPrice);
            });
        });
    }

    /**
     * Entry order.
     * 
     * @param side
     * @param entrySize
     * @param entryPrice
     * @return
     */
    protected final Exit entry(Side side, double entrySize, double entryPrice) {
        return entry(side, Decimal.valueOf(entrySize), Decimal.valueOf(entryPrice));
    }

    /**
     * Entry order.
     * 
     * @param side
     * @param entrySize
     * @param entryPrice
     * @return
     */
    protected final Exit entry(Side side, Decimal entrySize, Decimal entryPrice) {
        return new Exit(entryLimit(side, entrySize, entryPrice, entry -> {
            market.execute(side, entrySize, entryPrice);
        }));
    }

    /**
     * Create current log.
     * 
     * @return
     */
    protected final TradingLog createLog() {
        return new TradingLog(market, I.list(this));
    }

    /**
     * @version 2017/09/18 9:07:21
     */
    protected final class Exit {

        private final Entry entry;

        /**
         * @param entryLimit
         */
        private Exit(Entry entry) {
            this.entry = entry;
        }

        /**
         * Exit order.
         * 
         * @param exitSize
         * @param exitPrice
         * @return
         */
        protected final Exit exit(double exitSize, double exitPrice, double... executionSize) {
            return exit(Decimal.valueOf(exitSize), Decimal.valueOf(exitPrice), Decimal.of(executionSize));
        }

        /**
         * Exit order.
         * 
         * @param exitSize
         * @param exitPrice
         * @return
         */
        protected final Exit exit(Decimal exitSize, Decimal exitPrice, Decimal... executionSize) {
            entry.exitLimit(exitSize, exitPrice, exit -> {
                if (executionSize.length == 0) {
                    market.execute(entry.inverse(), exitSize, exitPrice);
                } else {
                    for (Decimal execution : executionSize) {
                        market.execute(entry.inverse(), execution, exitPrice);
                    }
                }
            });
            return this;
        }
    }
}
