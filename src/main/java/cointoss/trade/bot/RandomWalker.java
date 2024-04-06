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
     * {@inheritDoc}
     */
    @Override
    protected void declareStrategy(Market market, Funds fund) {
        when(market.tickers.on(Hour1).open, x -> trade(new Scenario() {

            @Override
            protected void entry() {
                Direction dir = Direction.BUY;

                entry(dir, 0.01);
            }

            @Override
            protected void exit() {
                // exitAt(entryPrice.plus(this, 1000000));
                exitAt(entryPrice.minus(this, 100000));
            }
        }));
    }
}