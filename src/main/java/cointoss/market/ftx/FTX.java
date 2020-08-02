/*
 * Copyright (C) 2020 cointoss Development Team
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          https://opensource.org/licenses/MIT
 */
package cointoss.market.ftx;

import cointoss.MarketService;
import cointoss.MarketSetting;
import cointoss.market.MarketAccount;
import cointoss.market.MarketServiceProvider;
import cointoss.util.Num;
import kiss.I;

public final class FTX extends MarketServiceProvider {

    /** Market */
    static final MarketService BTC_USD = new FTXService("BTC-PERP", MarketSetting.with.baseCurrencyMinimumBidPrice("0.5")
            .targetCurrencyMinimumBidSize("0.00001")
            .orderBookGroupRanges(Num.of(1, 5, 10, 25, 50, 100))
            .baseCurrencyScaleSize(1)
            .targetCurrencyScaleSize(5)
            .acquirableExecutionSize(200)
            .acquirableExecutionSizeModifler(FTXService.ID.padding));

    /**
     * {@inheritDoc}
     */
    @Override
    public MarketAccount account() {
        return I.make(FTXAccount.class);
    }
}