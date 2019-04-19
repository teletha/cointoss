/*
 * Copyright (C) 2019 CoinToss Development Team
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          https://opensource.org/licenses/MIT
 */
package cointoss.trader;

import org.junit.jupiter.api.BeforeEach;

import cointoss.Direction;
import cointoss.trade.Trader;
import cointoss.trade.TradingLog;
import cointoss.util.Num;
import cointoss.verify.VerifiableMarket;
import kiss.I;

/**
 * @version 2018/04/02 16:49:10
 */
public abstract class TraderTestSupport extends Trader {

    protected VerifiableMarket market;

    protected TradingLog log;

    /**
     * @param provider
     */
    public TraderTestSupport() {
        super.market = market = new VerifiableMarket();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected final void initialize() {
        // do nothing
    }

    @BeforeEach
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
    protected final void entryAndExit(Direction side, double entrySize, double entryPrice, double exitSize, double exitPrice) {
        entryAndExit(side, Num.of(entrySize), Num.of(entryPrice), Num.of(exitSize), Num.of(exitPrice));
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
    protected final void entryAndExit(Direction side, Num entrySize, Num entryPrice, Num exitSize, Num exitPrice) {
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
    protected final Exit entry(Direction side, double entrySize, double entryPrice) {
        return entry(side, Num.of(entrySize), Num.of(entryPrice));
    }

    /**
     * Entry order.
     * 
     * @param side
     * @param entrySize
     * @param entryPrice
     * @return
     */
    protected final Exit entry(Direction side, Num entrySize, Num entryPrice) {
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
    public final class Exit {

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
        public final Exit exit(double exitSize, double exitPrice, double... executionSize) {
            return exit(Num.of(exitSize), Num.of(exitPrice), Num.of(executionSize));
        }

        /**
         * Exit order.
         * 
         * @param exitSize
         * @param exitPrice
         * @return
         */
        public final Exit exit(Num exitSize, Num exitPrice, Num... executionSize) {
            entry.exitLimit(exitSize, exitPrice, exit -> {
                if (executionSize.length == 0) {
                    market.execute(entry.inverse(), exitSize, exitPrice);
                } else {
                    for (Num execution : executionSize) {
                        market.execute(entry.inverse(), execution, exitPrice);
                    }
                }
            });
            return this;
        }
    }
}
