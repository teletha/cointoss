/*
 * Copyright (C) 2020 cointoss Development Team
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          https://opensource.org/licenses/MIT
 */
package cointoss.market.gmo;

import cointoss.Currency;
import cointoss.MarketService;
import cointoss.MarketSetting;
import cointoss.market.MarketAccount;
import cointoss.market.MarketServiceProvider;
import kiss.I;

public final class GMO extends MarketServiceProvider {

    /** Limitation */
    private static final int AcquirableSize = 1000 * 1000 * 60 * 60 * 24;

    static final MarketService BTC = new GMOService("BTC", MarketSetting.with.target(Currency.BTC.minimumSize(0.00001))
            .base(Currency.JPY.minimumSize(1))
            .acquirableExecutionSize(AcquirableSize));

    static final MarketService BTC_JPY = new GMOService("BTC_JPY", MarketSetting.with.target(Currency.BTC.minimumSize(0.00001))
            .base(Currency.JPY.minimumSize(1))
            .acquirableExecutionSize(AcquirableSize));

    static final MarketService ETH = new GMOService("ETH", MarketSetting.with.target(Currency.ETH.minimumSize(0.00001))
            .base(Currency.JPY.minimumSize(1))
            .acquirableExecutionSize(AcquirableSize));

    /**
     * {@inheritDoc}
     */
    @Override
    public MarketAccount account() {
        return I.make(GMOAccount.class);
    }
}