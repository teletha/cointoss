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
import cointoss.market.MarketDevTool;
import cointoss.market.MarketServiceProvider;
import kiss.I;

public final class Bitfinex extends MarketServiceProvider {

    /** Limitation */
    private static final int AcquirableSize = 10000;

    public static final MarketService BTC_USD = new BitfinexService("BTCUSD", MarketSetting.with.target(Currency.BTC.minimumSize(0.0001))
            .base(Currency.USD.minimumSize(1))
            .acquirableExecutionSize(AcquirableSize));

    public static final MarketService COMP_USD = new BitfinexService("COMP:USD", MarketSetting.with
            .target(Currency.COMP.minimumSize(0.0001))
            .base(Currency.USD.minimumSize(0.01))
            .acquirableExecutionSize(AcquirableSize));

    public static final MarketService ETH_USD = new BitfinexService("ETHUSD", MarketSetting.with.target(Currency.ETH.minimumSize(0.0001))
            .base(Currency.USD.minimumSize(0.01).scale(8))
            .acquirableExecutionSize(AcquirableSize));

    static final MarketService XRP_USD = new BitfinexService("XRPUSD", MarketSetting.with.target(Currency.XRP.minimumSize(0.0001))
            .base(Currency.USD.minimumSize(0.00001))
            .acquirableExecutionSize(AcquirableSize));

    /**
     * {@inheritDoc}
     */
    @Override
    public MarketAccount account() {
        return I.make(BitfinexAccount.class);
    }
}