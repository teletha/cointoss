/*
 * Copyright (C) 2019 Nameless Production Committee
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          http://opensource.org/licenses/mit-license.php
 */
package cointoss.trade;

import cointoss.Market;

public class DiscreteTrader extends Trader {

    /**
     * 
     */
    public DiscreteTrader(Market market) {
        initialize(market);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected final void declare(Market market, FundManager fund) {
    }
}
