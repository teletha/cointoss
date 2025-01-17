/*
 * Copyright (C) 2024 The COINTOSS Development Team
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          https://opensource.org/licenses/MIT
 */
package cointoss.market.bybit;

import cointoss.Currency;
import cointoss.MarketService;
import cointoss.MarketSetting;
import cointoss.market.Exchange;
import cointoss.market.MarketAccount;
import cointoss.market.MarketServiceProvider;
import kiss.I;

public final class Bybit extends MarketServiceProvider {

    public static final MarketService AAVE_USDT = new BybitService("AAVEUSDT", MarketSetting.with.spot()
            .target(Currency.AAVE.minimumSize(1).scale(3))
            .base(Currency.USDT.minimumSize(0.001)));

    public static final MarketService ARB_USDT = new BybitService("ARBUSDT", MarketSetting.with.spot()
            .target(Currency.ARB.minimumSize(1).scale(2))
            .base(Currency.USDT.minimumSize(0.001)));

    public static final MarketService BTC_USDT = new BybitService("BTCUSDT", MarketSetting.with.spot()
            .target(Currency.BTC.minimumSize(1).scale(5))
            .base(Currency.USDT.minimumSize(0.5)));

    public static final MarketService CAKE_USDT = new BybitService("CAKEUSDT", MarketSetting.with.spot()
            .target(Currency.CAKE.minimumSize(1).scale(3))
            .base(Currency.USDT.minimumSize(0.0001)));

    public static final MarketService EOS_USDT = new BybitService("EOSUSDT", MarketSetting.with.spot()
            .target(Currency.EOS.minimumSize(1).scale(5))
            .base(Currency.USDT.minimumSize(0.001)));

    public static final MarketService ETH_USDT = new BybitService("ETHUSDT", MarketSetting.with.spot()
            .target(Currency.ETH.minimumSize(1).scale(5))
            .base(Currency.USDT.minimumSize(0.05))
            .priceRangeModifier(20));

    public static final MarketService FIL_USDT = new BybitService("FILUSDT", MarketSetting.with.spot()
            .target(Currency.FIL.minimumSize(1).scale(2))
            .base(Currency.USDT.minimumSize(0.001)));

    public static final MarketService HYPE_USDT = new BybitService("HYPEUSDT", MarketSetting.with.derivative()
            .target(Currency.HYPE.minimumSize(1).scale(2))
            .base(Currency.USDT.minimumSize(0.001)));

    public static final MarketService LDO_USDT = new BybitService("LDOUSDT", MarketSetting.with.spot()
            .target(Currency.LDO.minimumSize(1).scale(2))
            .base(Currency.USDT.minimumSize(0.001)));

    public static final MarketService MNT_USDT = new BybitService("MNTUSDT", MarketSetting.with.spot()
            .target(Currency.MNT.minimumSize(1).scale(2))
            .base(Currency.USDT.minimumSize(0.0001)));

    public static final MarketService OP_USDT = new BybitService("OPUSDT", MarketSetting.with.spot()
            .target(Currency.OP.minimumSize(1).scale(2))
            .base(Currency.USDT.minimumSize(0.001)));

    public static final MarketService SNX_USDT = new BybitService("SNXUSDT", MarketSetting.with.spot()
            .target(Currency.SNX.minimumSize(1).scale(2))
            .base(Currency.USDT.minimumSize(0.001)));

    public static final MarketService SOL_USDT = new BybitService("SOLUSDT", MarketSetting.with.spot()
            .target(Currency.SOL.minimumSize(1).scale(3))
            .base(Currency.USDT.minimumSize(0.01)));

    public static final MarketService SOLV_USDT = new BybitService("SOLVUSDT", MarketSetting.with.spot()
            .target(Currency.SOLV.minimumSize(1).scale(1))
            .base(Currency.USDT.minimumSize(0.0001)));

    public static final MarketService SUI_USDT = new BybitService("SUIUSDT", MarketSetting.with.spot()
            .target(Currency.SUI.minimumSize(1).scale(2))
            .base(Currency.USDT.minimumSize(0.0001)));

    public static final MarketService STRK_USDT = new BybitService("STRKUSDT", MarketSetting.with.spot()
            .target(Currency.STRK.minimumSize(1).scale(2))
            .base(Currency.USDT.minimumSize(0.0001)));

    public static final MarketService WLD_USDT = new BybitService("WLDUSDT", MarketSetting.with.spot()
            .target(Currency.WLD.minimumSize(1).scale(2))
            .base(Currency.USDT.minimumSize(0.0001)));

    public static final MarketService XRP_USDT = new BybitService("XRPUSDT", MarketSetting.with.spot()
            .target(Currency.XRP.minimumSize(1).scale(5))
            .base(Currency.USDT.minimumSize(0.0001)));

    /**
     * {@inheritDoc}
     */
    @Override
    public Exchange exchange() {
        return Exchange.Bybit;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public MarketAccount account() {
        return I.make(BybitAccount.class);
    }
}