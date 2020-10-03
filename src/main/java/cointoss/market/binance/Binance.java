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
import cointoss.market.MarketDevTool;
import cointoss.market.MarketServiceProvider;
import kiss.I;

public final class Binance extends MarketServiceProvider {

    /** Limitation */
    private static final int AcquirableSize = 1000;

    public static final MarketService BTC_USDT = new BinanceService("BTCUSDT", false, MarketSetting.with
            .target(Currency.BTC.minimumSize(0.000001))
            .base(Currency.USDT.minimumSize(0.01))
            .acquirableExecutionSize(AcquirableSize));

    static final MarketService ETH_USDT = new BinanceService("ETHUSDT", false, MarketSetting.with.target(Currency.ETH.minimumSize(0.01))
            .base(Currency.USDT.minimumSize(0.00001))
            .acquirableExecutionSize(AcquirableSize));

    public static final MarketService LINK_USDT = new BinanceService("LINKUSDT", false, MarketSetting.with
            .target(Currency.LINK.minimumSize(0.01))
            .base(Currency.USDT.minimumSize(0.0001))
            .acquirableExecutionSize(AcquirableSize));

    public static final MarketService SRM_USDT = new BinanceService("SRMUSDT", false, MarketSetting.with
            .target(Currency.SRM.minimumSize(0.01))
            .base(Currency.USDT.minimumSize(0.0001))
            .acquirableExecutionSize(AcquirableSize));

    public static final MarketService FUTURE_BTC_USDT = new BinanceService("BTCUSDT", true, MarketSetting.with
            .target(Currency.BTC.minimumSize(0.001))
            .base(Currency.USDT.minimumSize(0.01))
            .acquirableExecutionSize(AcquirableSize));

    public static final MarketService FUTURE_COMP_USDT = new BinanceService("COMPUSDT", true, MarketSetting.with
            .target(Currency.COMP.minimumSize(0.001))
            .base(Currency.USDT.minimumSize(0.01))
            .acquirableExecutionSize(AcquirableSize));

    public static final MarketService FUTURE_MKR_USDT = new BinanceService("MKRUSDT", true, MarketSetting.with
            .target(Currency.MKR.minimumSize(0.001))
            .base(Currency.USDT.minimumSize(0.01))
            .acquirableExecutionSize(AcquirableSize));

    /**
     * {@inheritDoc}
     */
    @Override
    public MarketAccount account() {
        return I.make(BinanceAccount.class);
    }

    public static void main(String[] args) {
        MarketDevTool.collectLog(Binance.ETH_USDT);
    }
}