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

import cointoss.Direction;
import cointoss.Market;
import cointoss.market.bitflyer.BitFlyer;
import cointoss.ticker.Tick;
import cointoss.ticker.TickSpan;
import cointoss.trade.Trader;
import cointoss.util.Num;

public class BackTestInvoker {

    public static void main(String[] args) throws InterruptedException {
        BackTest backtest = BackTest.with.service(BitFlyer.FX_BTC_JPY).start(2019, 6, 1).end(2019, 6, 1);
        System.out.println(backtest.run(market -> new Sample(market)).get(0));
        System.out.println(backtest.time);
    }

    /**
     * 
     */
    private static class Sample extends Trader {

        private Sample(Market market) {
            super(market);

            when(market.tickers.of(TickSpan.Hour1).add.skip(1), tick -> {

                Tick prev = tick.previous;
                Num range = prev.highPrice().minus(prev.lowPrice());
                System.out.println(tick.previous + " @" + range);

                if (range.isLessThan(2000)) {
                    return null;
                }

                return new Entry(Direction.random()) {

                    @Override
                    protected void order() {
                        order(1, s -> s.make(market.tickers.latest.v.price));
                    }

                    /**
                     * {@inheritDoc}
                     */
                    @Override
                    protected void exit() {
                        exitAt(entryPrice.plus(direction, range.multiply(0.5)));
                        exitAt(entryPrice.plus(direction, range.multiply(0.3).negate()));
                    }
                };
            });
        }
    }
}
