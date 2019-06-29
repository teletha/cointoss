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
import cointoss.trade.TradingLog;

public class BackTestInvoker {

    public static void main(String[] args) throws InterruptedException {
        TradingLog log = BackTest.with.service(BitFlyer.FX_BTC_JPY)
                .start(2019, 6, 26)
                .end(2019, 6, 26)
                .run(market -> new Sample(market))
                .get(0);

        System.out.println(log);
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
                    protected void order() {
                        order(3, s -> s.make(market.tickers.latest.v.price));
                    }

                    /**
                     * {@inheritDoc}
                     */
                    @Override
                    protected void exit() {
                        exitAt(entryPrice.plus(direction, 30000));
                        exitAt(entryPrice.plus(direction, -20000));
                    }
                };
            });
        }
    }
}
