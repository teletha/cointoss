/*
 * Copyright (C) 2022 Nameless Production Committee
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          http://opensource.org/licenses/mit-license.php
 */
package cointoss.trade.bot;

import static cointoss.ticker.Span.*;

import cointoss.Direction;
import cointoss.Market;
import cointoss.ticker.Span;
import cointoss.trade.Funds;
import cointoss.trade.Scenario;
import cointoss.trade.Trader;
import cointoss.trade.Trailing;

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
        when(market.open(Hour1), x -> trade(new Scenario() {

            @Override
            protected void entry() {
                entry(Direction.random(), 0.2);
            }

            @Override
            protected void exit() {
                exitAt(Trailing.with.losscut(20000).profit(40000).update(Span.Second10));
                // exitAtRiskRewardRatio(0.8, Span.Hour4);
            }
        }));
    }

    class Random extends Scenario {

        /**
         * {@inheritDoc}
         */
        @Override
        protected void entry() {
        }

        /**
         * {@inheritDoc}
         */
        @Override
        protected void exit() {
        }
    }
}
