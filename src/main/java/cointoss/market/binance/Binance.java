/*
 * Copyright (C) 2020 cointoss Development Team
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          https://opensource.org/licenses/MIT
 */
package cointoss.market.binance;

import cointoss.Currency;
import cointoss.MarketService;
import cointoss.MarketSetting;
import cointoss.market.MarketAccount;
import cointoss.market.MarketServiceProvider;
import kiss.I;

public final class Binance extends MarketServiceProvider {

    private static final int AcuirableSize = 1000;

    /** Market */
    public static final MarketService BTC_USDT = new BinanceService("BTCUSDT", false, MarketSetting.with
            .target(Currency.BTC.minimumSize(0.000001))
            .base(Currency.USDT.minimumSize(0.01))
            .acquirableExecutionSize(AcuirableSize));

    /** Market */
    public static final MarketService FUTURE_BTC_USDT = new BinanceService("BTCUSDT", true, MarketSetting.with
            .target(Currency.BTC.minimumSize(0.001))
            .base(Currency.USDT.minimumSize(0.01))
            .acquirableExecutionSize(AcuirableSize));

    /**
     * {@inheritDoc}
     */
    @Override
    public MarketAccount account() {
        return I.make(BinanceAccount.class);
    }
}