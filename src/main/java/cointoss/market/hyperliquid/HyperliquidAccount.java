/*
 * Copyright (C) 2024 The COINTOSS Development Team
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          https://opensource.org/licenses/MIT
 */
package cointoss.market.hyperliquid;

import cointoss.market.MarketAccount;
import kiss.Variable;

public class HyperliquidAccount extends MarketAccount<HyperliquidAccount> {

    /** The API key. */
    public final Variable<String> apiKey = Variable.empty();

    /** The API secret. */
    public final Variable<String> apiSecret = Variable.empty();

    /**
     * Hide constructor.
     */
    private HyperliquidAccount() {
        restore().auto();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected boolean validate() {
        return true;
    }
}