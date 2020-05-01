/*
 * Copyright (C) 2019 CoinToss Development Team
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          https://opensource.org/licenses/MIT
 */
package cointoss.market.bybit;

import cointoss.MarketService;
import cointoss.MarketSetting;
import cointoss.market.MarketAccount;
import cointoss.market.MarketServiceProvider;
import cointoss.util.Num;
import kiss.I;

public final class Bybit extends MarketServiceProvider {

    /** Market */
    public static final MarketService BTC_USD = new BybitService("BTCUSD", MarketSetting.with.baseCurrencyMinimumBidPrice("0.5")
            .targetCurrencyMinimumBidSize("0.00001")
            .orderBookGroupRanges(Num.of(1, 5, 10, 25, 50, 100))
            .baseCurrencyScaleSize(1)
            .targetCurrencyScaleSize(5)
            .acquirableExecutionSize(1000));

    /**
     * {@inheritDoc}
     */
    @Override
    public MarketAccount account() {
        return I.make(BybitAccount.class);
    }
}
