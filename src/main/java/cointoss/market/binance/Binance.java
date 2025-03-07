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

    public static final MarketService AAVE_USDT = new BinanceService("AAVEUSDT", MarketSetting.with.spot()
            .target(Currency.AAVE.minimumSize(0.001))
            .base(Currency.USDT.minimumSize(0.01)));

    public static final MarketService ARB_USDT = new BinanceService("ARBUSDT", MarketSetting.with.spot()
            .target(Currency.ARB.minimumSize(0.1))
            .base(Currency.USDT.minimumSize(0.0001)));

    public static final MarketService BERA_USDT = new BinanceService("BERAUSDT", MarketSetting.with.spot()
            .target(Currency.BERA.minimumSize(0.001))
            .base(Currency.USDT.minimumSize(0.001)));

    public static final MarketService BNB_USDT = new BinanceService("BNBUSDT", MarketSetting.with.spot()
            .target(Currency.BNB.minimumSize(0.001))
            .base(Currency.USDT.minimumSize(0.1)));

    public static final MarketService BTC_USDT = new BinanceService("BTCUSDT", MarketSetting.with.spot()
            .target(Currency.BTC.minimumSize(0.000001))
            .base(Currency.USDT.minimumSize(0.01))
            .priceRangeModifier(500));

    public static final MarketService EOS_USDT = new BinanceService("EOSUSDT", MarketSetting.with.spot()
            .target(Currency.EOS.minimumSize(0.0001))
            .base(Currency.USDT.minimumSize(0.1)));

    public static final MarketService ETH_USDT = new BinanceService("ETHUSDT", MarketSetting.with.spot()
            .target(Currency.ETH.minimumSize(0.00001))
            .base(Currency.USDT.minimumSize(0.01))
            .priceRangeModifier(100));

    public static final MarketService FIL_USDT = new BinanceService("FILUSDT", MarketSetting.with.spot()
            .target(Currency.FIL.minimumSize(0.01))
            .base(Currency.USDT.minimumSize(0.001)));

    public static final MarketService GLMR_USDT = new BinanceService("GLMRUSDT", MarketSetting.with.spot()
            .target(Currency.GLMR.minimumSize(0.1))
            .base(Currency.USDT.minimumSize(0.0001)));

    public static final MarketService GMT_USDT = new BinanceService("GMTUSDT", MarketSetting.with.spot()
            .target(Currency.GMT.minimumSize(0.1))
            .base(Currency.USDT.minimumSize(0.0001)));

    public static final MarketService LDO_USDT = new BinanceService("LDOUSDT", MarketSetting.with.spot()
            .target(Currency.LDO.minimumSize(0.01))
            .base(Currency.USDT.minimumSize(0.001)));

    public static final MarketService LINK_USDT = new BinanceService("LINKUSDT", MarketSetting.with.spot()
            .target(Currency.LINK.minimumSize(0.01))
            .base(Currency.USDT.minimumSize(0.0001)));

    public static final MarketService MATIC_USDT = new BinanceService("MATICUSDT", MarketSetting.with.spot()
            .target(Currency.MATIC.minimumSize(0.1))
            .base(Currency.USDT.minimumSize(0.0001)));

    public static final MarketService OP_USDT = new BinanceService("OPUSDT", MarketSetting.with.spot()
            .target(Currency.OP.minimumSize(0.01))
            .base(Currency.USDT.minimumSize(0.001)));

    public static final MarketService SNX_USDT = new BinanceService("SNXUSDT", MarketSetting.with.spot()
            .target(Currency.SNX.minimumSize(0.1))
            .base(Currency.USDT.minimumSize(0.001)));

    public static final MarketService SOL_USDT = new BinanceService("SOLUSDT", MarketSetting.with.spot()
            .target(Currency.SOL.minimumSize(0.01))
            .base(Currency.USDT.minimumSize(0.01)));

    public static final MarketService SOLV_USDT = new BinanceService("SOLVUSDT", MarketSetting.with.spot()
            .target(Currency.SOLV.minimumSize(0.01))
            .base(Currency.USDT.minimumSize(0.0001)));

    public static final MarketService STRK_USDT = new BinanceService("STRKUSDT", MarketSetting.with.spot()
            .target(Currency.STRK.minimumSize(0.01))
            .base(Currency.USDT.minimumSize(0.001)));

    public static final MarketService SUI_USDT = new BinanceService("SUIUSDT", MarketSetting.with.spot()
            .target(Currency.SUI.minimumSize(0.1))
            .base(Currency.USDT.minimumSize(0.001)));

    public static final MarketService UNI_USDT = new BinanceService("UNIUSDT", MarketSetting.with.spot()
            .target(Currency.UNI.minimumSize(0.01))
            .base(Currency.USDT.minimumSize(0.0001)));

    public static final MarketService WLD_USDT = new BinanceService("WLDUSDT", MarketSetting.with.spot()
            .target(Currency.WLD.minimumSize(0.1))
            .base(Currency.USDT.minimumSize(0.001)));

    public static final MarketService XRP_USDT = new BinanceService("XRPUSDT", MarketSetting.with.spot()
            .target(Currency.XRP.minimumSize(0.1))
            .base(Currency.USDT.minimumSize(0.00001)));

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