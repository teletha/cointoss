/*
 * Copyright (C) 2020 cointoss Development Team
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          https://opensource.org/licenses/MIT
 */
package cointoss.market.okex;

import cointoss.Currency;
import cointoss.MarketService;
import cointoss.MarketSetting;
import cointoss.market.MarketAccount;
import cointoss.market.MarketServiceProvider;
import kiss.I;

public final class OKEx extends MarketServiceProvider {

    /** Limitation */
    private static final int AcquirableSize = 10000;

    static final MarketService BTCUSDT = new OKExService("BTC-USDT", MarketSetting.with.target(Currency.BTC.minimumSize(0.001))
            .base(Currency.USDT.minimumSize(0.1))
            .acquirableExecutionSize(AcquirableSize));

    /**
     * {@inheritDoc}
     */
    @Override
    public MarketAccount account() {
        return I.make(OKExAccount.class);
    }
}