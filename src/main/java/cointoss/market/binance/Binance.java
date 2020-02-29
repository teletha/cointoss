/*
 * Copyright (C) 2019 CoinToss Development Team
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          https://opensource.org/licenses/MIT
 */
package cointoss.market.binance;

import cointoss.MarketService;
import cointoss.MarketSetting;
import cointoss.market.MarketAccount;
import cointoss.market.MarketServiceProvider;
import cointoss.util.Num;
import kiss.I;

public final class Binance extends MarketServiceProvider {

    /** Market */
    public static final MarketService BTC_USDT = new BinanceService("BTCUSDT", MarketSetting.with.baseCurrencyMinimumBidPrice("0.01")
            .targetCurrencyMinimumBidSize("0.000001")
            .orderBookGroupRanges(Num.of(1, 5, 10, 25))
            .baseCurrencyScaleSize(1)
            .targetCurrencyScaleSize(5)
            .acquirableExecutionSize(1000));

    /**
     * {@inheritDoc}
     */
    @Override
    public MarketAccount account() {
        return I.make(BinanceAccount.class);
    }
}
