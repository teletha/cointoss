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
import cointoss.trade.Scenario;
import cointoss.trade.Trader;
import cointoss.util.Num;

public class BackTestInvoker {

    public static void main(String[] args) throws InterruptedException {
        BackTest.with.service(BitFlyer.FX_BTC_JPY)
                .start(2019, 8, 13)
                .end(2019, 8, 13)
                .trader(Sample::new)
                .initialBaseCurrency(3000000)
                .exclusiveExecution(true)
                .run();
    }

    /**
     * 
     */
    private static class Sample extends Trader {

        private Sample(Market market) {
            super(market);

            when(market.tickers.of(TickSpan.Second5).add.skip(12), tick -> new Scenario() {

                @Override
                protected void entry() {
                    Indicator indicator = new Indicator(market);

                    if (indicator.diff.isGreaterThan(8)) {
                        entry(indicator.direction, 0.1, s -> s.make(market.latestPrice().minus(indicator.direction, 150)));
                    }
                }

                @Override
                protected void exit() {
                    exitAt(entryPrice.plus(this, 2000));
                    exitAt(entryPrice.minus(this, 1400));
                }
            });
        }
    }

    private static class Indicator {

        Num buyVolume = Num.ZERO;

        Num sellVolume = Num.ZERO;

        Direction direction;

        Num diff;

        private Indicator(Market market) {
            Tick t = market.tickers.of(TickSpan.Second5).last();

            for (int i = 12; 0 < i; i--) {
                buyVolume = buyVolume.plus(t.buyVolume());
                sellVolume = sellVolume.plus(t.sellVolume());
                t = t.previous;
            }
            direction = buyVolume.isGreaterThan(sellVolume) ? Direction.BUY : Direction.SELL;

            diff = buyVolume.minus(sellVolume).abs();
        }
    }
}
