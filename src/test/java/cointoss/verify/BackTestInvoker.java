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

import static java.time.temporal.ChronoUnit.MINUTES;

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
                .start(2019, 11, 1)
                .end(2019, 11, 1)
                .traders(Sample::new)
                .initialBaseCurrency(3000000)
                .run();
    }

    /**
     * 
     */
    private static class Sample extends Trader {

        private Sample(Market market) {
            super(market);

            when(market.tickers.of(TickSpan.Minute1).add.skip(4), tick -> new Scenario() {

                @Override
                protected void entry() {
                    entry(Direction.random(), 0.1, s -> s.make(market.tickers.latestPrice.v.minus(this, 300)).cancelAfter(5, MINUTES));
                }

                @Override
                protected void exit() {
                    exitAt(entryPrice.plus(this, 3900));

                    exitAt(trailing(up -> entryPrice.minus(this, 1300).plus(this, up)));
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
