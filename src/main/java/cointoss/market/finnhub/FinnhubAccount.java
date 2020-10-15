/*
 * Copyright (C) 2020 cointoss Development Team
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          https://opensource.org/licenses/MIT
 */
package cointoss.market.finnhub;

import cointoss.market.MarketAccount;
import kiss.Variable;

public class FinnhubAccount extends MarketAccount<FinnhubAccount> {

    /** The API key. */
    public final Variable<String> apiKey = Variable.empty();

    /** The API secret. */
    public final Variable<String> apiSecret = Variable.empty();

    /**
     * Hide constructor.
     */
    private FinnhubAccount() {
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