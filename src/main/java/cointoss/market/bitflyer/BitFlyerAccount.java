/*
 * Copyright (C) 2023 The COINTOSS Development Team
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          https://opensource.org/licenses/MIT
 */
package cointoss.market.bitflyer;

import cointoss.market.MarketAccount;
import kiss.Variable;

public class BitFlyerAccount extends MarketAccount<BitFlyerAccount> {

    /** The API key. */
    public final Variable<String> apiKey = Variable.of("undefined");

    /** The API secret. */
    public final Variable<String> apiSecret = Variable.of("undefined");

    /** The login id. */
    public final Variable<String> loginId = Variable.empty();

    /** The login password. */
    public final Variable<String> loginPassword = Variable.empty();

    /**
     * Hide constructor.
     */
    private BitFlyerAccount() {
        restore().auto();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected boolean validate() {
        return apiKey.isPresent() && apiKey.isNot(String::isEmpty) && apiSecret.isPresent() && apiSecret.isNot(String::isEmpty);
    }
}