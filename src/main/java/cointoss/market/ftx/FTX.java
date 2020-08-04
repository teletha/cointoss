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

import cointoss.Market;
import cointoss.MarketService;
import cointoss.MarketSetting;
import cointoss.market.MarketAccount;
import cointoss.market.MarketServiceProvider;
import kiss.I;

public final class FTX extends MarketServiceProvider {

    /** Market */
    static final MarketService BTC_USD = new FTXService("BTC-PERP", MarketSetting.with.baseCurrencyMinimumBidPrice("0.5")
            .targetCurrencyMinimumBidSize("0.00001")
            .baseCurrencyScaleSize(1)
            .targetCurrencyScaleSize(5)
            .acquirableExecutionSize(5000));

    /** Market */
    public static final MarketService FTT_USDT = new FTXService("FTT/USDT", MarketSetting.with.baseCurrencyMinimumBidPrice("1")
            .targetCurrencyMinimumBidSize("0.001")
            .baseCurrencyScaleSize(1)
            .targetCurrencyScaleSize(3)
            .acquirableExecutionSize(1000));

    /**
     * {@inheritDoc}
     */
    @Override
    public MarketAccount account() {
        return I.make(FTXAccount.class);
    }

    public static void main(String[] args) throws InterruptedException {

        Market market = new Market(FTX.FTT_USDT);
        market.readLog(log -> log.fromYestaday());

        Thread.sleep(1000 * 60 * 30);
    }
}