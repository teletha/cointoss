/*
 * Copyright (C) 2024 The COINTOSS Development Team
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          https://opensource.org/licenses/MIT
 */
package cointoss.market.huobi;

import cointoss.market.Exchange;
import cointoss.market.MarketAccount;
import cointoss.market.MarketServiceProvider;
import kiss.I;

public final class Huobi extends MarketServiceProvider {

    /** Limitation */
    private static final int AcquirableSize = 1000;

    // public static final MarketService BTC_USDT = new HuobiService("btcusdt",
    // MarketSetting.with.derivative()
    // .target(Currency.BTC.minimumSize(0.000001))
    // .base(Currency.USDT.minimumSize(0.01))
    // .acquirableExecutionSize(AcquirableSize));

    /**
     * {@inheritDoc}
     */
    @Override
    public Exchange exchange() {
        return Exchange.Huobi;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public MarketAccount account() {
        return I.make(HuobiAccount.class);
    }
}