/*
 * Copyright (C) 2020 Nameless Production Committee
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          http://opensource.org/licenses/mit-license.php
 */
package cointoss.trade.bot;

import java.util.HashMap;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import cointoss.Direction;
import cointoss.Market;
import cointoss.market.bitflyer.BitFlyer;
import cointoss.ticker.NumIndicator;
import cointoss.ticker.Span;
import cointoss.ticker.Tick;
import cointoss.ticker.Ticker;
import cointoss.trade.Funds;
import cointoss.trade.Scenario;
import cointoss.trade.Trader;
import cointoss.util.arithmetic.Num;
import cointoss.verify.BackTest;
import kiss.Signal;

public class TouchMovingAverage extends Trader {

    private Map<Tick, Scenario> entries = new HashMap();

    /**
     * {@inheritDoc}
     */
    @Override
    protected void declareStrategy(Market market, Funds fund) {
        Span span = Span.Minute5;
        Ticker ticker = market.tickers.on(span);
        NumIndicator sma = NumIndicator.build(ticker, tick -> tick.closePrice()).sma(25);

        Signal<Tick> up = market.timeline.map(e -> e.price())
                .plug(breakup(sma::valueAtLast))
                .map(v -> ticker.ticks.last())
                .diff()
                .take(now -> {
                    Tick latest = sma.findLatest((tick, price) -> Num.within(tick.lowPrice(), price, tick.highPrice()));
                    if (span.distance(latest, now) <= 6) {
                        return false;
                    }

                    if (entries.containsKey(latest)) {
                        entries.remove(latest).stop();
                    }

                    if (!ticker.ticks.eachInside(latest, now).all(tick -> tick.highPrice().isLessThan(sma.valueAt(tick))).to().v) {
                        return false;
                    }
                    return true;
                });

        Signal<Tick> down = market.timeline.map(e -> e.price())
                .plug(breakdown(sma::valueAtLast))
                .map(v -> ticker.ticks.last())
                .diff()
                .take(now -> {
                    Num currentMAPrice = sma.valueAt(now);
                    Tick latest = sma.findLatest((tick, price) -> Num.within(tick.lowPrice(), price, tick.highPrice()));
                    if (span.distance(latest, now) <= 6) {
                        return false;
                    }

                    if (entries.containsKey(latest)) {
                        entries.remove(latest).stop();
                    }

                    if (!ticker.ticks.eachInside(latest, now).all(tick -> tick.highPrice().isGreaterThan(sma.valueAt(tick))).to().v) {
                        return false;
                    }
                    return true;
                });

        when(up, tick -> new Scenario() {
            @Override
            protected void entry() {
                entry(Direction.SELL, 1);
                entries.put(tick, this);
            }

            @Override
            protected void exit() {
                exitAt(entryPrice.plus(this, 12000));
                exitAt(entryPrice.minus(this, 3000));
            }
        });

        when(down, tick -> new Scenario() {
            @Override
            protected void entry() {
                entry(Direction.BUY, 1);
                entries.put(tick, this);
            }

            @Override
            protected void exit() {
                exitAt(entryPrice.plus(this, 12000));
                exitAt(entryPrice.minus(this, 3000));
            }
        });
    }

    public static void main(String[] args) {
        Logger log = LogManager.getLogger();
        Thread.setDefaultUncaughtExceptionHandler((t, e) -> log.error(e.getMessage(), e));

        BackTest.with.service(BitFlyer.FX_BTC_JPY)
                .start(2020, 3, 2)
                .end(2020, 3, 10)
                .traders(new TouchMovingAverage())
                .fast()
                .detail(true)
                .run();
    }
}
