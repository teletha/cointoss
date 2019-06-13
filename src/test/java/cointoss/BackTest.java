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

import static cointoss.ticker.TickSpan.*;
import static java.time.temporal.ChronoUnit.*;

import cointoss.trade.Entry;
import cointoss.trade.Trader;

public class BackTest {

    /**
     * 
     */
    @SuppressWarnings("unused")
    private static class Sample extends Trader {

        private Sample(Market market) {
            super(market);

            // various events
            entryWhen(market.tickers.of(Minute1).add, tick -> {
                return new Entry(Direction.random()) {

                    @Override
                    protected void order() {
                        order(0.01).makeBestPrice().cancelAfter(5, MINUTES);
                    }
                };
            });
        }
    }
}
