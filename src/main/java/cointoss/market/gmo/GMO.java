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
    private static final int AcquirableSize = 10000;

    static final MarketService BTC = new GMOService("BTC", MarketSetting.with.target(Currency.BTC.minimumSize(0.0001))
            .base(Currency.JPY.minimumSize(1))
            .acquirableExecutionSize(AcquirableSize));

    static final MarketService BTC_DERIVATIVE = new GMOService("BTC_JPY", MarketSetting.with.target(Currency.BTC.minimumSize(0.01))
            .base(Currency.JPY.minimumSize(1))
            .acquirableExecutionSize(AcquirableSize));

    static final MarketService ETH = new GMOService("ETH", MarketSetting.with.target(Currency.ETH.minimumSize(0.01))
            .base(Currency.JPY.minimumSize(1))
            .acquirableExecutionSize(AcquirableSize));

    static final MarketService ETH_DERIVATIVE = new GMOService("ETH_JPY", MarketSetting.with.target(Currency.ETH.minimumSize(0.1))
            .base(Currency.JPY.minimumSize(1))
            .acquirableExecutionSize(AcquirableSize));

    static final MarketService LTC = new GMOService("LTC", MarketSetting.with.target(Currency.LTC.minimumSize(0.1))
            .base(Currency.JPY.minimumSize(1))
            .acquirableExecutionSize(AcquirableSize));

    static final MarketService LTC_DERIVATIVE = new GMOService("LTC_JPY", MarketSetting.with.target(Currency.LTC.minimumSize(1))
            .base(Currency.JPY.minimumSize(1))
            .acquirableExecutionSize(AcquirableSize));

    static final MarketService XRP = new GMOService("XRP", MarketSetting.with.target(Currency.XRP.minimumSize(1))
            .base(Currency.JPY.minimumSize(0.001))
            .acquirableExecutionSize(AcquirableSize));

    static final MarketService XRP_DERIVATIVE = new GMOService("XRP_JPY", MarketSetting.with.target(Currency.XRP.minimumSize(10))
            .base(Currency.JPY.minimumSize(0.001))
            .acquirableExecutionSize(AcquirableSize));

    /**
     * {@inheritDoc}
     */
    @Override
    public MarketAccount account() {
        return I.make(GMOAccount.class);
    }
}