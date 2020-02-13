/*
 * Copyright (C) 2019 CoinToss Development Team
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          https://opensource.org/licenses/MIT
 */
package cointoss.market.bitmex;

import cointoss.MarketService;
import cointoss.MarketSetting;
import cointoss.market.MarketAccount;
import cointoss.market.MarketServiceProvider;
import cointoss.util.Num;
import kiss.I;

public final class BitMex extends MarketServiceProvider {

    /** Reusable market configuration. */
    private static MarketSetting FiatBaseSetting = MarketSetting.with.baseCurrencyMinimumBidPrice(Num.of("0.5"))
            .targetCurrencyMinimumBidSize(Num.of("0.01"))
            .orderBookGroupRanges(Num.of(50, 100, 250, 500, 1000, 2500, 5000))
            .targetCurrencyScaleSize(3)
            .acquirableExecutionSize(1000);

    /** Market */
    public static final MarketService XBT_USD = new BitMexService("XBTUSD", FiatBaseSetting);

    /**
     * {@inheritDoc}
     */
    @Override
    public MarketAccount account() {
        return I.make(BitMexAccount.class);
    }
}
