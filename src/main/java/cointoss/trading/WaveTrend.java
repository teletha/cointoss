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

import java.util.function.Function;

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
import cointoss.util.Num;
import cointoss.verify.BackTest;
import kiss.Signal;

/**
 * 
 */
public class WaveTrend extends Trader {

    public TimeSpan span = TimeSpan.Second30;

    public int entryThreshold = 58;

    public int exitThreshold = -30;

    /**
     * {@inheritDoc}
     */
    @Override
    protected void declare(Market market, FundManager fund) {
        Ticker ticker = market.tickers.on(span);
        NumIndicator indicator = Indicators.waveTrend(ticker);

        when(indicator.updateBy(ticker).plug(breakdown(entryThreshold)), v -> new Scenario() {
            @Override
            protected void entry() {
                entry(Direction.SELL, 0.1, Orderable::take);
            }

            @Override
            protected void exit() {
                exitWhen(indicator.updateBy(ticker).plug(breakup(entryThreshold)), Orderable::take);
                exitWhen(indicator.updateBy(ticker).plug(breakdown(exitThreshold)), Orderable::take);
            }
        });
    }

    protected final Function<Signal<Num>, Signal<Num>> breakup(double threshold) {
        return s -> s.take(Num.ZERO, (previous, current) -> previous.isLessThan(threshold) && current.isGreaterThanOrEqual(threshold));
    }

    protected final Function<Signal<Num>, Signal<Num>> breakdown(double threshold) {
        return s -> s.take(Num.ZERO, (previous, current) -> previous.isGreaterThan(threshold) && current.isLessThanOrEqual(threshold));
    }

    public static void main(String[] args) {
        BackTest.with.service(BitFlyer.FX_BTC_JPY).start(2020, 4, 2).end(2020, 4, 2).traders(new WaveTrend()).fast().detail(true).run();
    }
}