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
        BackTest.with.service(BitFlyer.FX_BTC_JPY)
                .start(2019, 6, 14)
                .end(2019, 6, 15)
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

            when(market.tickers.of(TickSpan.Hour1).add, tick -> {
                return new Entry(Direction.random()) {

                    @Override
                    protected void entry() {
                        entry(3, s -> s.make(market.tickers.latest.v.price));
                    }

                    /**
                     * {@inheritDoc}
                     */
                    @Override
                    protected void exit() {
                        exitAt(entryPrice.plus(direction, 5000));

                        Variable<Num> loss = market.tickers.of(TickSpan.Second5).add.map(Tick::openPrice)
                                .startWith(entryPrice)
                                .scan(p -> p, (prev, now) -> Num.max(direction, prev, now))
                                .map(p -> p.minus(direction, 4000))
                                .to();
                        exitAt(loss);
                    }
                };
            });
        }
    }
}
