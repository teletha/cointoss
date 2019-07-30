/*
 * Copyright (C) 2019 CoinToss Development Team
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          https://opensource.org/licenses/MIT
 */
package cointoss.verify;

import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import cointoss.Direction;
import cointoss.Market;
import cointoss.market.bitflyer.BitFlyer;
import cointoss.ticker.Tick;
import cointoss.ticker.TickSpan;
import cointoss.trade.Trader;
import cointoss.util.Num;
import kiss.Variable;

public class BackTestInvoker {

    public static void main(String[] args) throws InterruptedException {
        Logger log = LogManager.getLogger();
        Thread.setDefaultUncaughtExceptionHandler((t, e) -> log.error(e.getMessage(), e));

        BackTest.with.service(BitFlyer.FX_BTC_JPY)
                .start(2019, 7, 22)
                .end(2019, 7, 22)
                .initialBaseCurrency(3000000)
                .exclusiveExecution(false)
                .runs(market -> List
                        .of(new Sample(market), new Sample(market), new Sample(market), new Sample(market), new Sample(market)));
    }

    /**
     * 
     */
    private static class Sample extends Trader {

        private Sample(Market market) {
            super(market);

            when(market.tickers.of(TickSpan.Minute5).add, tick -> {
                Tick prev = tick.previous;
                Num upperBound = prev.highPrice().minus(prev.closePrice());
                Num lowerBound = prev.closePrice().minus(prev.lowPrice());
                Num boundRatio = upperBound.divide(lowerBound);
                Num boundMax = Num.max(upperBound, lowerBound);

                return new Entry(Direction.random()) {

                    @Override
                    protected void entry() {
                        entry(0.5, s -> s.make(market.tickers.latest.v.price));
                    }

                    /**
                     * {@inheritDoc}
                     */
                    @Override
                    protected void exit() {
                        exitAt(entryPrice.plus(direction, boundMax));

                        Variable<Num> loss = market.tickers.of(TickSpan.Second5).add.map(Tick::openPrice)
                                .startWith(entryPrice)
                                .scan(p -> p, (prev, now) -> Num.max(direction, prev, now))
                                .map(p -> p.minus(direction, boundMax.divide(0.75)))
                                .to();
                        exitAt(loss);
                    }
                };
            });
        }
    }
}
