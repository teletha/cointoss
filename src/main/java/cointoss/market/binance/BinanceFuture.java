/*
 * Copyright (C) 2024 The COINTOSS Development Team
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
import cointoss.market.Exchange;
import cointoss.market.MarketAccount;
import cointoss.market.MarketServiceProvider;
import kiss.I;

public final class BinanceFuture extends MarketServiceProvider {

    /** Limitation */
    private static final int AcquirableSize = 1000;

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

    public static final MarketService FUTURE_UNI_USDT = new BinanceService("UNIUSDT", MarketSetting.with.derivative()
            .target(Currency.UNI.minimumSize(1))
            .base(Currency.USDT.minimumSize(0.0001))
            .acquirableExecutionSize(AcquirableSize));

    public static final MarketService FUTURE_DOGE_USDT = new BinanceService("DOGEUSDT", MarketSetting.with.derivative()
            .target(Currency.DOGE.minimumSize(1))
            .base(Currency.USDT.minimumSize(0.000001))
            .acquirableExecutionSize(AcquirableSize));

    // public static final MarketService FUTURE_BTCUSD_2301229 = new BinanceService("BTCUSD_231229",
    // MarketSetting.with.derivative()
    // .target(Currency.BTC.minimumSize(0.00001))
    // .base(Currency.USD.minimumSize(0.1))
    // .priceRangeModifier(500)
    // .acquirableExecutionSize(AcquirableSize));

    // public static final MarketService FUTURE_BTCUSD_230929 = new BinanceService("BTCUSD_230929",
    // MarketSetting.with.derivative()
    // .target(Currency.BTC.minimumSize(0.00001))
    // .base(Currency.USD.minimumSize(0.1))
    // .priceRangeModifier(500)
    // .acquirableExecutionSize(AcquirableSize));

    /**
     * {@inheritDoc}
     */
    @Override
    public Exchange exchange() {
        return Exchange.BinanceF;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public MarketAccount account() {
        return I.make(BinanceAccount.class);
    }
}