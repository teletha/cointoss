/*
 * Copyright (C) 2019 Nameless Production Committee
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          http://opensource.org/licenses/mit-license.php
 */
package cointoss.trading;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import cointoss.Direction;
import cointoss.Market;
import cointoss.market.bitflyer.BitFlyer;
import cointoss.ticker.Span;
import cointoss.trade.FundManager;
import cointoss.trade.Scenario;
import cointoss.trade.Trader;
import cointoss.verify.BackTest;

/**
 * 
 */
public class CrossOrder extends Trader {

    public int range = 10000;

    public int trailLosscut = 5000;

    public int trailProfitcut = 0;

    /**
     * {@inheritDoc}
     */
    @Override
    protected void declare(Market market, FundManager fund) {
        when(market.open(Span.Hour1).take(v -> holdSize.isZero()), v -> new Scenario() {
            @Override
            protected void entry() {
                entry(Direction.SELL, 10);
            }

            @Override
            protected void exit() {
            }
        });

        when(market.open(Span.Hour1).take(v -> holdSize.isZero()), v -> new Scenario() {
            @Override
            protected void entry() {
                entry(Direction.BUY, 10);
            }

            @Override
            protected void exit() {
            }
        });
    }

    public static void main(String[] args) {
        Logger log = LogManager.getLogger();
        Thread.setDefaultUncaughtExceptionHandler((t, e) -> log.error(e.getMessage(), e));

        BackTest.with.service(BitFlyer.FX_BTC_JPY).start(2020, 3, 2).end(2020, 3, 2).traders(new CrossOrder()).fast().detail(true).run();
    }
}