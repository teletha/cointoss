/*
 * Copyright (C) 2021 cointoss Development Team
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
import cointoss.market.MarketServiceProvider;
import kiss.I;

public final class Binance extends MarketServiceProvider {

    /** Limitation */
    private static final int AcquirableSize = 1000;

    public static final MarketService BTC_USDT = new BinanceService("BTCUSDT", MarketSetting.with.spot()
            .target(Currency.BTC.minimumSize(0.000001))
            .base(Currency.USDT.minimumSize(0.01))
            .priceRangeModifier(500)
            .acquirableExecutionSize(AcquirableSize));

    public static final MarketService EOS_USDT = new BinanceService("EOSUSDT", MarketSetting.with.spot()
            .target(Currency.EOS.minimumSize(0.01))
            .base(Currency.USDT.minimumSize(0.0001))
            .acquirableExecutionSize(AcquirableSize));

    public static final MarketService ETH_USDT = new BinanceService("ETHUSDT", MarketSetting.with.spot()
            .target(Currency.ETH.minimumSize(0.00001))
            .base(Currency.USDT.minimumSize(0.01))
            .priceRangeModifier(100)
            .acquirableExecutionSize(AcquirableSize));

    public static final MarketService LINK_USDT = new BinanceService("LINKUSDT", MarketSetting.with.spot()
            .target(Currency.LINK.minimumSize(0.01))
            .base(Currency.USDT.minimumSize(0.0001))
            .acquirableExecutionSize(AcquirableSize));

    public static final MarketService SRM_USDT = new BinanceService("SRMUSDT", MarketSetting.with.spot()
            .target(Currency.SRM.minimumSize(0.01))
            .base(Currency.USDT.minimumSize(0.0001))
            .acquirableExecutionSize(AcquirableSize));

    public static final MarketService XRP_USDT = new BinanceService("XRPUSDT", MarketSetting.with.spot()
            .target(Currency.XRP.minimumSize(0.1))
            .base(Currency.USDT.minimumSize(0.00001))
            .acquirableExecutionSize(AcquirableSize));

    public static final MarketService FUTURE_BTC_USDT = new BinanceService("BTCUSDT", MarketSetting.with.derivative()
            .target(Currency.BTC.minimumSize(0.001))
            .base(Currency.USDT.minimumSize(0.01))
            .priceRangeModifier(500)
            .acquirableExecutionSize(AcquirableSize));

    public static final MarketService FUTURE_EHT_USDT = new BinanceService("ETHUSDT", MarketSetting.with.derivative()
            .target(Currency.ETH.minimumSize(0.001))
            .base(Currency.USDT.minimumSize(0.01))
            .priceRangeModifier(100)
            .acquirableExecutionSize(AcquirableSize));

    public static final MarketService FUTURE_COMP_USDT = new BinanceService("COMPUSDT", MarketSetting.with.derivative()
            .target(Currency.COMP.minimumSize(0.001))
            .base(Currency.USDT.minimumSize(0.01))
            .acquirableExecutionSize(AcquirableSize));

    public static final MarketService FUTURE_MKR_USDT = new BinanceService("MKRUSDT", MarketSetting.with.derivative()
            .target(Currency.MKR.minimumSize(0.001))
            .base(Currency.USDT.minimumSize(0.01))
            .acquirableExecutionSize(AcquirableSize));

    public static final MarketService FUTURE_UNI_USDT = new BinanceService("UNIUSDT", MarketSetting.with.derivative()
            .target(Currency.UNI.minimumSize(1))
            .base(Currency.USDT.minimumSize(0.0001))
            .acquirableExecutionSize(AcquirableSize));

    public static final MarketService FUTURE_DOGE_USDT = new BinanceService("DOGEUSDT", MarketSetting.with.derivative()
            .target(Currency.DOGE.minimumSize(1))
            .base(Currency.USDT.minimumSize(0.000001))
            .acquirableExecutionSize(AcquirableSize));

    // public static final MarketService FUTURE_BTCUSD_210326 = new BinanceService("BTCUSD_210326",
    // MarketSetting.with.derivative()
    // .target(Currency.BTC.minimumSize(0.00001))
    // .base(Currency.USD.minimumSize(0.1))
    // .priceRangeModifier(500)
    // .acquirableExecutionSize(AcquirableSize));

    public static final MarketService FUTURE_BTCUSD_210924 = new BinanceService("BTCUSD_210924", MarketSetting.with.derivative()
            .target(Currency.BTC.minimumSize(0.00001))
            .base(Currency.USD.minimumSize(0.1))
            .priceRangeModifier(500)
            .acquirableExecutionSize(AcquirableSize));

    public static final MarketService FUTURE_BTCUSD_211231 = new BinanceService("BTCUSD_211231", MarketSetting.with.derivative()
            .target(Currency.BTC.minimumSize(0.00001))
            .base(Currency.USD.minimumSize(0.1))
            .priceRangeModifier(500)
            .acquirableExecutionSize(AcquirableSize));

    /**
     * {@inheritDoc}
     */
    @Override
    public MarketAccount account() {
        return I.make(BinanceAccount.class);
    }
}