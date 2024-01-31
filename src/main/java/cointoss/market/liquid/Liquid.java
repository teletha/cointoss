/*
 * Copyright (C) 2024 The COINTOSS Development Team
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          https://opensource.org/licenses/MIT
 */
package cointoss.market.liquid;

import cointoss.Currency;
import cointoss.MarketService;
import cointoss.MarketSetting;
import cointoss.market.Exchange;
import cointoss.market.MarketAccount;
import cointoss.market.MarketServiceProvider;
import kiss.I;

public final class Liquid extends MarketServiceProvider {

    /** Limitation */
    private static final int AcquirableSize = 50;

    static final MarketService BTC_JPY = new LiquidService("BTCJPY", MarketSetting.with.spot()
            .target(Currency.BTC.minimumSize(0.001))
            .base(Currency.JPY.minimumSize(1))
            .priceRangeModifier(500)
            .acquirableExecutionSize(AcquirableSize));

    /**
     * {@inheritDoc}
     */
    @Override
    public Exchange exchange() {
        return Exchange.Liquid;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public MarketAccount account() {
        return I.make(LiquidAccount.class);
    }
}