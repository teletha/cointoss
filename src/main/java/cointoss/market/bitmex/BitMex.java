/*
 * Copyright (C) 2020 cointoss Development Team
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          https://opensource.org/licenses/MIT
 */
package cointoss.market.bitmex;

import cointoss.Currency;
import cointoss.MarketService;
import cointoss.MarketSetting;
import cointoss.market.MarketAccount;
import cointoss.market.MarketServiceProvider;
import kiss.I;

public final class BitMex extends MarketServiceProvider {

    /** Limitation */
    private static final int AcquirableSize = 1000;

    public static final MarketService XBT_USD = new BitMexService(88, "XBTUSD", MarketSetting.with.target(Currency.BTC.minimumSize(0.00001))
            .base(Currency.USD.minimumSize(0.5))
            .acquirableExecutionSize(AcquirableSize));

    public static final MarketService ETH_USD = new BitMexService(297, "ETHUSD", MarketSetting.with
            .target(Currency.ETH.minimumSize(0.00001))
            .base(Currency.USD.minimumSize(0.05))
            .acquirableExecutionSize(AcquirableSize));

    public static final MarketService XRP_USD = new BitMexService(377, "XRPUSD", MarketSetting.with
            .target(Currency.XRP.minimumSize(0.00001))
            .base(Currency.USD.minimumSize(0.0001))
            .acquirableExecutionSize(AcquirableSize));

    /**
     * {@inheritDoc}
     */
    @Override
    public MarketAccount account() {
        return I.make(BitMexAccount.class);
    }
}