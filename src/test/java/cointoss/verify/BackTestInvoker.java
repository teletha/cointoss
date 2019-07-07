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
import cointoss.ticker.TickSpan;
import cointoss.trade.Trader;
import cointoss.util.Num;

public class BackTestInvoker {

    public static void main(String[] args) throws InterruptedException {
        BackTest backtest = BackTest.with.service(BitFlyer.FX_BTC_JPY).start(2019, 6, 10).end(2019, 6, 10).initialBaseCurrency(3000000);
        System.out.println(backtest.run(market -> new Sample(market)).get(0));
        System.out.println(backtest.time);
    }

    /**
     * 
     */
    private static class Sample extends Trader {

        private Sample(Market market) {
            super(market);

            when(market.tickers.of(TickSpan.Hour1).add.skip(10), tick -> {
                Num averageRange = tick.previous(10)
                        .scanWith(Num.ZERO, (v, t) -> v.plus(t.highPrice().minus(t.lowPrice())))
                        .last()
                        .map(v -> v.divide(10))
                        .to().v;

                int trend = tick.previous.previous(3).take(t -> t.closePrice().minus(t.openPrice).isPositiveOrZero()).toList().size();

                return new Entry(1 < trend ? Direction.BUY : Direction.SELL) {

                    @Override
                    protected void entry() {
                        entry(3, s -> s.make(market.tickers.latest.v.price));
                    }

                    /**
                     * {@inheritDoc}
                     */
                    @Override
                    protected void exit() {
                        exitAt(entryPrice.plus(direction, averageRange.multiply(2)));
                        exitAt(entryPrice.minus(direction, averageRange.multiply(0.5)));
                    }
                };
            });
        }
    }
}
