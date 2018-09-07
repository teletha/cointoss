/*
 * Copyright (C) 2017 Nameless Production Committee
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          http://opensource.org/licenses/mit-license.php
 */
package cointoss.market.bitflyer;

import cointoss.market.MarketAccount;
import kiss.Variable;

/**
 * @version 2018/09/06 21:25:30
 */
public class BitFlyerAccount extends MarketAccount<BitFlyerAccount> {

    /** The API key. */
    public final Variable<String> apiKey = Variable.empty();

    /** The API secret. */
    public final Variable<String> apiSecret = Variable.empty();

    /** The login id. */
    public final Variable<String> loginId = Variable.empty();

    /** The login password. */
    public final Variable<String> loginPassword = Variable.empty();

    /** The account id. */
    public final Variable<String> accountId = Variable.empty();

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
        return apiKey.isNot(String::isEmpty) && apiSecret.isNot(String::isEmpty);
    }
}
