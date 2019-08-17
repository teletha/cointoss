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

public class BackTestInvoker {

    public static void main(String[] args) throws InterruptedException {
        Logger log = LogManager.getLogger();
        Thread.setDefaultUncaughtExceptionHandler((t, e) -> log.error(e.getMessage(), e));

        BackTest.with.service(BitFlyer.FX_BTC_JPY)
                .start(2019, 8, 13)
                .end(2019, 8, 13)
                .initialBaseCurrency(3000000)
                .exclusiveExecution(false)
                .runs(market -> List.of(new Sample(market)));
    }

    /**
     * 
     */
    private static class Sample extends Trader {

        private Sample(Market market) {
            super(market);

            when(market.tickers.of(TickSpan.Minute1).add.skip(1), tick -> {
                if (market.positions.hasPosition()) {
                    return null;
                }

                Indicator indicator = new Indicator();

                return new Entry(indicator.direction) {

                    @Override
                    protected void entry() {
                        entry(0.5, s -> s.take());
                    }

                    /**
                     * {@inheritDoc}
                     */
                    @Override
                    protected void exit() {
                        exitWhen(market.tickers.of(TickSpan.Second5).add.take(t -> new Indicator().direction != direction).first(), s -> {
                            s.take();
                        });
                    }
                };
            });
        }

        private class Indicator {

            Num buyVolume = Num.ZERO;

            Num sellVolume = Num.ZERO;

            Direction direction;

            private Indicator() {
                Tick t = market.tickers.of(TickSpan.Second5).last();

                for (int i = 10; 0 < i; i--) {
                    buyVolume = buyVolume.plus(t.buyVolume().multiply(i));
                    sellVolume = sellVolume.plus(t.sellVolume().multiply(i));
                    t = t.previous;
                }
                direction = buyVolume.isGreaterThan(sellVolume) ? Direction.BUY : Direction.SELL;
            }
        }
    }
}
