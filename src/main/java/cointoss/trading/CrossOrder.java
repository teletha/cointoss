/*
 * Copyright (C) 2020 cointoss Development Team
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          https://opensource.org/licenses/MIT
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

    public Span span = Span.Hour1;

    public int diff = 10000;

    public double size = 0.01;

    /**
     * {@inheritDoc}
     */
    @Override
    protected void declare(Market market, FundManager fund) {
        when(market.close(span), v -> new Scenario() {
            @Override
            protected void entry() {
                entry(Direction.random(), size);
            }

            @Override
            protected void exit() {
                exitAt(entryPrice.plus(this, diff * 10));
                exitAt(entryPrice.minus(this, diff));
            }
        });
    }

    public static void main(String[] args) {
        Logger log = LogManager.getLogger();
        Thread.setDefaultUncaughtExceptionHandler((t, e) -> log.error(e.getMessage(), e));

        BackTest.with.service(BitFlyer.FX_BTC_JPY).start(2020, 3, 2).end(2020, 3, 14).traders(new CrossOrder()).fast().detail(true).run();
    }
}