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
import cointoss.ticker.TickSpan;
import cointoss.trade.Scenario;
import cointoss.trade.Trader;

public class BackTestInvoker {

    public static void main(String[] args) throws InterruptedException {
        BackTest.with.service(BitFlyer.FX_BTC_JPY)
                .start(2019, 10, 25)
                .end(2019, 10, 26)
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

            when(market.tickers.of(TickSpan.Minute5).add, tick -> new Scenario() {

                @Override
                protected void entry() {
                    entry(Direction.random(), 0.1, s -> s.make(market.tickers.latestPrice.v).cancelAfter(2, MINUTES));
                }

                @Override
                protected void exit() {
                    exitAt(entryPrice.plus(this, 3900));
                    // exitAt(trailing(price -> price.minus(this, 13500)));
                    exitAt(trailing2(up -> entryPrice.minus(this, 1300).plus(this, up)));
                }
            });
        }
    }
}
