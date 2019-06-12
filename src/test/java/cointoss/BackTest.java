/*
 * Copyright (C) 2019 CoinToss Development Team
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          https://opensource.org/licenses/MIT
 */
package cointoss;

import static cointoss.ticker.TickSpan.Minute1;
import static java.time.temporal.ChronoUnit.*;

import cointoss.trade.Entry;
import cointoss.trade.OrderStrategy;
import cointoss.trade.Trader;
import cointoss.util.Num;

public class BackTest {

    /**
     * 
     */
    @SuppressWarnings("unused")
    private static class BreakoutTrading extends Trader {

        private BreakoutTrading(Market market) {
            super(market);

            // various events
            entryWhen(market.tickers.of(Minute1).add, tick -> {
                return new Entry(Direction.random()) {
                    Num diff = Num.of(-2000);

                    @Override
                    protected void order() {
                        order(0.01).makeBestPrice().cancelAfter(5, MINUTES);
                    }

                    @Override
                    protected void stop() {
                        stop.when(market.timeline.take(e -> e.price.isGreaterThan(this, price)))
                                .how(OrderStrategy.with.makeLowest().cancelAfter(30, SECONDS).take());

                    }

                    @Override
                    protected void stopLoss() {
                        stopLoss.when(market.timeline.take(keep(5, SECONDS, e -> e.price.isLessThan(this, price.plus(diff)))))
                                .how(OrderStrategy.with.make(price.plus(diff)).cancelAfter(30, SECONDS).take());
                    }
                };
            });
        }
    }
}
