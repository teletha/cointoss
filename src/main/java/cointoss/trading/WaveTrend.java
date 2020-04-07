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

import cointoss.Direction;
import cointoss.Market;
import cointoss.market.bitflyer.BitFlyer;
import cointoss.order.OrderStrategy.Orderable;
import cointoss.ticker.Indicators;
import cointoss.ticker.NumIndicator;
import cointoss.ticker.Ticker;
import cointoss.ticker.TimeSpan;
import cointoss.trade.FundManager;
import cointoss.trade.Scenario;
import cointoss.trade.Trader;
import cointoss.verify.BackTest;

/**
 * 
 */
public class WaveTrend extends Trader {

    public TimeSpan span = TimeSpan.Second15;

    public int entryThreshold = 80;

    public int exitThreshold = -30;

    public int stop = 3;

    public double size = 0.1;

    /**
     * {@inheritDoc}
     */
    @Override
    protected void declare(Market market, FundManager fund) {
        Ticker ticker = market.tickers.on(span);
        NumIndicator indicator = Indicators.waveTrend(ticker);

        when(indicator.valueAt(ticker.update).plug(breakdown(entryThreshold)), v -> new Scenario() {
            @Override
            protected void entry() {
                entry(Direction.SELL, size, Orderable::take);
            }

            @Override
            protected void exit() {
                exitWhen(indicator.valueAt(ticker.open).plug(breakup(entryThreshold + stop)), Orderable::take);
                exitWhen(indicator.valueAt(ticker.open).plug(breakdown(exitThreshold)), Orderable::take);
            }
        });

        when(indicator.valueAt(ticker.update).plug(breakup(-entryThreshold)), v -> new Scenario() {
            @Override
            protected void entry() {
                entry(Direction.BUY, size, Orderable::take);
            }

            @Override
            protected void exit() {
                exitWhen(indicator.valueAt(ticker.open).plug(breakdown(-entryThreshold - stop)), Orderable::take);
                exitWhen(indicator.valueAt(ticker.open).plug(breakup(-exitThreshold)), Orderable::take);
            }
        });
    }

    public static void main(String[] args) {
        BackTest.with.service(BitFlyer.FX_BTC_JPY).start(2020, 3, 16).end(2020, 3, 16).traders(new WaveTrend()).fast().detail(true).run();
    }
}