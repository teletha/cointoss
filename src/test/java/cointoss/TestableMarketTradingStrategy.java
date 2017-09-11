/*
 * Copyright (C) 2017 Nameless Production Committee
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          http://opensource.org/licenses/mit-license.php
 */
package cointoss;

/**
 * @version 2017/09/09 10:01:55
 */
class TestableMarketTradingStrategy extends TradingStrategy {

    /**
     * @param market
     */
    TestableMarketTradingStrategy(Market market) {
        super(market);

        market.strategy = this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void timeline(Execution exe) {
    }
}
