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

import java.util.Objects;

import cointoss.Market;

public abstract class Trader2 {

    /** The target market. */
    protected final Market market;

    /**
     * Declare your strategy.
     * 
     * @param market A target market to deal.
     */
    protected Trader2(Market market) {
        this.market = Objects.requireNonNull(market);
    }
}
