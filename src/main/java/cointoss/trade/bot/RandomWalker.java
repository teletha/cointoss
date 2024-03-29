/*
 * Copyright (C) 2024 The COINTOSS Development Team
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          https://opensource.org/licenses/MIT
 */
package cointoss.trade.bot;

import static cointoss.ticker.Span.*;

import cointoss.Direction;
import cointoss.Market;
import cointoss.trade.Funds;
import cointoss.trade.Scenario;
import cointoss.trade.Trader;

public class RandomWalker extends Trader {

    /**
     * 
     */
    public RandomWalker() {
        enable();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void declareStrategy(Market market, Funds fund) {
        when(market.tickers.on(Hour4).open, x -> trade(new Scenario() {

            @Override
            protected void entry() {
                Direction dir = Direction.random();

                entry(dir, 0.2, o -> o.make(market.tickers.latest.v.price.minus(dir, 300)));
            }

            @Override
            protected void exit() {
                exitAt(entryPrice.minus(this, 10000));
            }
        }));
    }
}