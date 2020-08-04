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

    /** Market */
    public static final MarketService XBT_USD = new BitMexService(88, "XBTUSD", MarketSetting.with.target(Currency.BTC)
            .baseCurrencyMinimumBidPrice("0.5")
            .targetCurrencyMinimumBidSize("0.00001")
            .baseCurrencyScaleSize(1)
            .targetCurrencyScaleSize(5)
            .acquirableExecutionSize(1000));

    /** Market */
    public static final MarketService ETH_USD = new BitMexService(297, "ETHUSD", MarketSetting.with.target(Currency.ETH)
            .baseCurrencyMinimumBidPrice("0.05")
            .targetCurrencyMinimumBidSize("0.00001")
            .baseCurrencyScaleSize(2)
            .targetCurrencyScaleSize(5)
            .acquirableExecutionSize(1000));

    /** Market */
    public static final MarketService XRP_USD = new BitMexService(377, "XRPUSD", MarketSetting.with.target(Currency.XRP)
            .baseCurrencyMinimumBidPrice("0.0001")
            .targetCurrencyMinimumBidSize("0.00001")
            .baseCurrencyScaleSize(4)
            .targetCurrencyScaleSize(5)
            .acquirableExecutionSize(1000));

    /**
     * {@inheritDoc}
     */
    @Override
    public MarketAccount account() {
        return I.make(BitMexAccount.class);
    }
}