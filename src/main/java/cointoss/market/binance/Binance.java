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

public final class Binance extends MarketServiceProvider {

    /** Limitation */
    private static final int AcquirableSize = 1000;

    public static final MarketService AAVE_USDT = new BinanceService("AAVEUSDT", MarketSetting.with.spot()
            .target(Currency.AAVE.minimumSize(0.001))
            .base(Currency.USDT.minimumSize(0.01))
            .acquirableExecutionSize(AcquirableSize));

    public static final MarketService ARB_USDT = new BinanceService("ARBUSDT", MarketSetting.with.spot()
            .target(Currency.ARB.minimumSize(0.1))
            .base(Currency.USDT.minimumSize(0.0001))
            .acquirableExecutionSize(AcquirableSize));

    public static final MarketService BNB_USDT = new BinanceService("BNBUSDT", MarketSetting.with.spot()
            .target(Currency.BNB.minimumSize(0.001))
            .base(Currency.USDT.minimumSize(0.1))
            .acquirableExecutionSize(AcquirableSize));

    public static final MarketService BTC_USDT = new BinanceService("BTCUSDT", MarketSetting.with.spot()
            .target(Currency.BTC.minimumSize(0.000001))
            .base(Currency.USDT.minimumSize(0.01))
            .priceRangeModifier(500)
            .acquirableExecutionSize(AcquirableSize));

    public static final MarketService ETH_USDT = new BinanceService("ETHUSDT", MarketSetting.with.spot()
            .target(Currency.ETH.minimumSize(0.00001))
            .base(Currency.USDT.minimumSize(0.01))
            .priceRangeModifier(100)
            .acquirableExecutionSize(AcquirableSize));

    public static final MarketService FIL_USDT = new BinanceService("FILUSDT", MarketSetting.with.spot()
            .target(Currency.FIL.minimumSize(0.01))
            .base(Currency.USDT.minimumSize(0.001))
            .acquirableExecutionSize(AcquirableSize));

    public static final MarketService GLMR_USDT = new BinanceService("GLMRUSDT", MarketSetting.with.spot()
            .target(Currency.GLMR.minimumSize(0.1))
            .base(Currency.USDT.minimumSize(0.0001))
            .acquirableExecutionSize(AcquirableSize));

    public static final MarketService GMT_USDT = new BinanceService("GMTUSDT", MarketSetting.with.spot()
            .target(Currency.GMT.minimumSize(0.1))
            .base(Currency.USDT.minimumSize(0.0001))
            .acquirableExecutionSize(AcquirableSize));

    public static final MarketService LDO_USDT = new BinanceService("LDOUSDT", MarketSetting.with.spot()
            .target(Currency.LDO.minimumSize(0.01))
            .base(Currency.USDT.minimumSize(0.001))
            .acquirableExecutionSize(AcquirableSize));

    public static final MarketService LINK_USDT = new BinanceService("LINKUSDT", MarketSetting.with.spot()
            .target(Currency.LINK.minimumSize(0.01))
            .base(Currency.USDT.minimumSize(0.0001))
            .acquirableExecutionSize(AcquirableSize));

    public static final MarketService MATIC_USDT = new BinanceService("MATICUSDT", MarketSetting.with.spot()
            .target(Currency.MATIC.minimumSize(0.1))
            .base(Currency.USDT.minimumSize(0.0001))
            .acquirableExecutionSize(AcquirableSize));

    public static final MarketService OP_USDT = new BinanceService("OPUSDT", MarketSetting.with.spot()
            .target(Currency.OP.minimumSize(0.01))
            .base(Currency.USDT.minimumSize(0.001))
            .acquirableExecutionSize(AcquirableSize));

    public static final MarketService SNX_USDT = new BinanceService("SNXUSDT", MarketSetting.with.spot()
            .target(Currency.SNX.minimumSize(0.1))
            .base(Currency.USDT.minimumSize(0.001))
            .acquirableExecutionSize(AcquirableSize));

    public static final MarketService SOL_USDT = new BinanceService("SOLUSDT", MarketSetting.with.spot()
            .target(Currency.SOL.minimumSize(0.01))
            .base(Currency.USDT.minimumSize(0.01))
            .acquirableExecutionSize(AcquirableSize));

    public static final MarketService STRK_USDT = new BinanceService("STRKUSDT", MarketSetting.with.spot()
            .target(Currency.STRK.minimumSize(0.01))
            .base(Currency.USDT.minimumSize(0.001))
            .acquirableExecutionSize(AcquirableSize));

    public static final MarketService UNI_USDT = new BinanceService("UNIUSDT", MarketSetting.with.spot()
            .target(Currency.UNI.minimumSize(0.01))
            .base(Currency.USDT.minimumSize(0.0001))
            .acquirableExecutionSize(AcquirableSize));

    public static final MarketService WLD_USDT = new BinanceService("WLDUSDT", MarketSetting.with.spot()
            .target(Currency.WLD.minimumSize(0.1))
            .base(Currency.USDT.minimumSize(0.001))
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
        return Exchange.Binance;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public MarketAccount account() {
        return I.make(BinanceAccount.class);
    }
}