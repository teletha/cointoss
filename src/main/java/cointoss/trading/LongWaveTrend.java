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
import cointoss.ticker.Indicators;
import cointoss.ticker.NumIndicator;
import cointoss.ticker.Span;
import cointoss.ticker.Ticker;
import cointoss.trade.FundManager;
import cointoss.trade.Scenario;
import cointoss.trade.Trader;
import cointoss.trade.Trailing;
import cointoss.verify.BackTest;

/**
 * 
 */
public class LongWaveTrend extends Trader {

    public Span span = Span.Hour1;

    public int entryThreshold = 50;

    public int exitThreshold = -50;

    public int stop = 3;

    public double size = 10;

    public int trailLosscut = 9000;

    public int trailProfitcut = 5000;

    public int profitcut = 50000;

    /**
     * {@inheritDoc}
     */
    @Override
    protected void declare(Market market, FundManager fund) {

        Ticker ticker = market.tickers.on(span);
        NumIndicator indicator = Indicators.waveTrend(ticker);

        when(indicator.valueAt(ticker.open).plug(breakdown(entryThreshold)), v -> new Scenario() {
            @Override
            protected void entry() {
                entry(Direction.SELL, size);
            }

            @Override
            protected void exit() {
                exitAt(Trailing.with.losscut(trailLosscut).profit(trailProfitcut));
                exitAt(entryPrice.minus(profitcut));
                exitWhen(indicator.valueAt(ticker.open).plug(breakup(entryThreshold + stop)));
                exitWhen(indicator.valueAt(ticker.open).plug(breakdown(exitThreshold)));
            }
        });

        when(indicator.valueAt(ticker.open).plug(breakup(-entryThreshold)), v -> new Scenario() {
            @Override
            protected void entry() {
                entry(Direction.BUY, size);
            }

            @Override
            protected void exit() {
                exitAt(Trailing.with.losscut(trailLosscut).profit(trailProfitcut));
                exitAt(entryPrice.plus(profitcut));
                exitWhen(indicator.valueAt(ticker.open).plug(breakdown(-entryThreshold - stop)));
                exitWhen(indicator.valueAt(ticker.open).plug(breakup(-exitThreshold)));
            }
        });
    }

    public static void main(String[] args) {
        Logger log = LogManager.getLogger();
        Thread.setDefaultUncaughtExceptionHandler((t, e) -> log.error(e.getMessage(), e));

        BackTest.with.service(BitFlyer.FX_BTC_JPY).start(2020, 3, 2).end(2020, 3, 2).traders(new LongWaveTrend()).fast().detail(true).run();
    }
}