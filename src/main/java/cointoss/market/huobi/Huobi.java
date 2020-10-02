/*
 * Copyright (C) 2020 cointoss Development Team
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          https://opensource.org/licenses/MIT
 */
package cointoss.market.huobi;

import cointoss.Currency;
import cointoss.MarketService;
import cointoss.MarketSetting;
import cointoss.market.MarketAccount;
import cointoss.market.MarketServiceProvider;
import kiss.I;

public final class Huobi extends MarketServiceProvider {

    /** Limitation */
    private static final int AcquirableSize = 1000;

    static final MarketService BTC_USDT = new HuobiService(88, "btcusdt", MarketSetting.with.target(Currency.BTC.minimumSize(0.000001))
            .base(Currency.USDT.minimumSize(0.01))
            .acquirableExecutionSize(AcquirableSize));

    /**
     * {@inheritDoc}
     */
    @Override
    public MarketAccount account() {
        return I.make(HuobiAccount.class);
    }
}