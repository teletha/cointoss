/*
 * Copyright (C) 2021 cointoss Development Team
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          https://opensource.org/licenses/MIT
 */
package cointoss.market.coinbase;

import cointoss.Currency;
import cointoss.MarketService;
import cointoss.MarketSetting;
import cointoss.market.MarketAccount;
import cointoss.market.MarketServiceProvider;
import kiss.I;

public final class Coinbase extends MarketServiceProvider {

    /** Limitation */
    private static final int AcquirableSize = 100;

    public static final MarketService BTCUSD = new CoinbaseService("BTC-USD", MarketSetting.with.spot()
            .target(Currency.BTC.minimumSize(0.0001))
            .base(Currency.USD.minimumSize(0.1))
            .priceRangeModifier(50)
            .acquirableExecutionSize(AcquirableSize));

    public static final MarketService ETHUSD = new CoinbaseService("ETH-USD", MarketSetting.with.spot()
            .target(Currency.ETH.minimumSize(0.0001))
            .base(Currency.USD.minimumSize(0.01))
            .priceRangeModifier(100)
            .acquirableExecutionSize(AcquirableSize));

    /**
     * {@inheritDoc}
     */
    @Override
    public MarketAccount account() {
        return I.make(CoinbaseAccount.class);
    }
}