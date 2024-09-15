/*
 * Copyright (C) 2024 The COINTOSS Development Team
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
import cointoss.market.Exchange;
import cointoss.market.MarketAccount;
import cointoss.market.MarketServiceProvider;
import kiss.I;

public final class Coinbase2 extends MarketServiceProvider {

    public static final MarketService BTCUSD = new CoinbaseService2("BTC-USD", MarketSetting.with.spot()
            .target(Currency.BTC.minimumSize(0.0001))
            .base(Currency.USD.minimumSize(0.1))
            .priceRangeModifier(50)
            .acquirableExecutionSize(1000)
            .acquirableExecutionBulkModifier(8)
            .acquirableExecutionIncrement(1000 * 60 * 5 * CoinbaseService2.support.padding));

    public static final MarketService ETHUSD = new CoinbaseService2("ETH-USD", MarketSetting.with.spot()
            .target(Currency.ETH.minimumSize(0.0001))
            .base(Currency.USD.minimumSize(0.01))
            .priceRangeModifier(100)
            .acquirableExecutionSize(1000)
            .acquirableExecutionBulkModifier(8)
            .acquirableExecutionIncrement(1000 * 60 * 5 * CoinbaseService2.support.padding));

    public static final MarketService XRPUSD = new CoinbaseService2("XRP-USD", MarketSetting.with.spot()
            .target(Currency.XRP.minimumSize(0.000001))
            .base(Currency.USD.minimumSize(0.0001))
            .priceRangeModifier(1)
            .acquirableExecutionSize(1000)
            .acquirableExecutionBulkModifier(8)
            .acquirableExecutionIncrement(1000 * 60 * 5 * CoinbaseService2.support.padding));

    /**
     * {@inheritDoc}
     */
    @Override
    public Exchange exchange() {
        return Exchange.Coinbase2;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public MarketAccount account() {
        return I.make(CoinbaseAccount.class);
    }
}