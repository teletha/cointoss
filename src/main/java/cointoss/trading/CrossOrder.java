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

import java.util.concurrent.TimeUnit;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import cointoss.Direction;
import cointoss.Market;
import cointoss.market.bitflyer.BitFlyer;
import cointoss.ticker.Span;
import cointoss.trade.FundManager;
import cointoss.trade.Scenario;
import cointoss.trade.Trader;
import cointoss.util.Num;
import cointoss.verify.BackTest;

/**
 * 
 */
public class CrossOrder extends Trader {

    public Span span = Span.Minute30;

    public int diff = 1000;

    public double rrr = 1;

    /**
     * {@inheritDoc}
     */
    @Override
    protected void declare(Market market, FundManager fund) {
        when(market.close(span), v -> new Scenario() {
            @Override
            protected void entry() {
                entry(market.tickers.on(span).ticks.last().isBear() ? Direction.BUY : Direction.SELL, 0.01);
            }

            @Override
            protected void exit() {
                exitAt(entryPrice.plus(this, Num.of(diff * rrr)));
                exitAfter(span.seconds / 4, TimeUnit.SECONDS);
            }
        });
    }

    public static void main(String[] args) {
        Logger log = LogManager.getLogger();
        Thread.setDefaultUncaughtExceptionHandler((t, e) -> log.error(e.getMessage(), e));

        BackTest.with.service(BitFlyer.FX_BTC_JPY).start(2020, 3, 2).end(2020, 3, 8).traders(new CrossOrder()).fast().detail(true).run();
    }
}