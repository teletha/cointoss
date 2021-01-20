/*
 * Copyright (C) 2021 Nameless Production Committee
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          http://opensource.org/licenses/mit-license.php
 */
package cointoss.trading;

import cointoss.Currency;
import cointoss.Direction;
import cointoss.Market;
import cointoss.arbitrage.Arbitrage;
import cointoss.trade.Funds;
import cointoss.trade.Scenario;
import cointoss.trade.Trader;

public class Arbitrager extends Trader {

    /**
     * {@inheritDoc}
     */
    @Override
    protected void declareStrategy(Market market, Funds fund) {
        when(Arbitrage.by(Currency.ETH, Currency.JPY), arb -> new Scenario() {

            @Override
            protected void entry() {
                entry(arb.buyMarket, Direction.BUY, arb.size);
                entry(arb.sellMarket, Direction.SELL, arb.size);
            }

            @Override
            protected void exit() {
            }
        });
    }
}
