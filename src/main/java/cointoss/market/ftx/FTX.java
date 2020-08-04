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

import cointoss.Currency;
import cointoss.Market;
import cointoss.MarketService;
import cointoss.MarketSetting;
import cointoss.market.MarketAccount;
import cointoss.market.MarketServiceProvider;
import kiss.I;

public final class FTX extends MarketServiceProvider {

    private static final int AcquirableSize = 10000;

    /** Market */
    public static final MarketService ADA_PERP = new FTXService("ADA-PERP", MarketSetting.with.target(Currency.ADA)
            .baseCurrencyMinimumBidPrice("0.000005")
            .targetCurrencyMinimumBidSize("1")
            .baseCurrencyScaleSize(6)
            .targetCurrencyScaleSize(0)
            .acquirableExecutionSize(AcquirableSize));

    /** Market */
    public static final MarketService BNB_PERP = new FTXService("BNB-PERP", MarketSetting.with.target(Currency.BNB)
            .baseCurrencyMinimumBidPrice("0.0005")
            .targetCurrencyMinimumBidSize("0.00001")
            .baseCurrencyScaleSize(4)
            .targetCurrencyScaleSize(5)
            .acquirableExecutionSize(AcquirableSize));

    /** Market */
    public static final MarketService BTC_PERP = new FTXService("BTC-PERP", MarketSetting.with.target(Currency.BTC)
            .baseCurrencyMinimumBidPrice("0.5")
            .targetCurrencyMinimumBidSize("0.0001")
            .baseCurrencyScaleSize(1)
            .targetCurrencyScaleSize(4)
            .acquirableExecutionSize(AcquirableSize));

    /** Market */
    public static final MarketService EOS_PERP = new FTXService("EOS-PERP", MarketSetting.with.target(Currency.EOS)
            .baseCurrencyMinimumBidPrice("0.00005")
            .targetCurrencyMinimumBidSize("0.00001")
            .baseCurrencyScaleSize(5)
            .targetCurrencyScaleSize(5)
            .acquirableExecutionSize(AcquirableSize));

    /** Market */
    public static final MarketService ETH_PERP = new FTXService("ETH-PERP", MarketSetting.with.target(Currency.ETH)
            .baseCurrencyMinimumBidPrice("0.01")
            .targetCurrencyMinimumBidSize("0.001")
            .baseCurrencyScaleSize(2)
            .targetCurrencyScaleSize(3)
            .acquirableExecutionSize(AcquirableSize));

    /** Market */
    public static final MarketService FTT_USDT = new FTXService("FTT/USDT", MarketSetting.with.target(Currency.FTT)
            .baseCurrencyMinimumBidPrice("0.001")
            .targetCurrencyMinimumBidSize("1")
            .baseCurrencyScaleSize(3)
            .targetCurrencyScaleSize(0)
            .acquirableExecutionSize(AcquirableSize));

    /**
     * {@inheritDoc}
     */
    @Override
    public MarketAccount account() {
        return I.make(FTXAccount.class);
    }

    public static void main(String[] args) throws InterruptedException {
        Market market = new Market(FTX.ETH_PERP);
        market.readLog(log -> log.fromYestaday());

        Thread.sleep(1000 * 30);
    }
}