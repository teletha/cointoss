/*
 * Copyright (C) 2020 cointoss Development Team
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          https://opensource.org/licenses/MIT
 */
package cointoss.market.bitfinex;

import cointoss.Currency;
import cointoss.MarketService;
import cointoss.MarketSetting;
import cointoss.market.MarketAccount;
import cointoss.market.MarketServiceProvider;
import kiss.I;

public final class Bitfinex extends MarketServiceProvider {

    /** Market */
    public static final MarketService BTC_USD = new BitfinexService("BTCUSD", MarketSetting.with //
            .target(Currency.BTC.minimumSize(0.0001))
            .base(Currency.USD.minimumSize(1))
            .acquirableExecutionSize(10000));

    /**
     * {@inheritDoc}
     */
    @Override
    public MarketAccount account() {
        return I.make(BitfinexAccount.class);
    }
}