/*
 * Copyright (C) 2024 The COINTOSS Development Team
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          https://opensource.org/licenses/MIT
 */
package cointoss.market.hyperliquid;

import cointoss.Currency;
import cointoss.MarketService;
import cointoss.MarketSetting;
import cointoss.market.Exchange;
import cointoss.market.MarketAccount;
import cointoss.market.MarketServiceProvider;
import kiss.I;

public final class Hyperliquid extends MarketServiceProvider {

    public static final MarketService AAVE = new HyperliquidService("AAVE", MarketSetting.with.derivative()
            .target(Currency.AAVE.minimumSize(0.01))
            .base(Currency.USDC.minimumSize(0.01)));

    public static final MarketService AI16Z = new HyperliquidService("AI16Z", MarketSetting.with.derivative()
            .target(Currency.AI16Z.minimumSize(0.1))
            .base(Currency.USDC.minimumSize(0.00001)));

    public static final MarketService AIXBT = new HyperliquidService("AIXBT", MarketSetting.with.derivative()
            .target(Currency.AIXBT.minimumSize(1))
            .base(Currency.USDC.minimumSize(0.00001)));

    public static final MarketService AVAX = new HyperliquidService("AVAX", MarketSetting.with.derivative()
            .target(Currency.AVAX.minimumSize(0.01))
            .base(Currency.USDC.minimumSize(0.001)));

    public static final MarketService BERA = new HyperliquidService("BERA", MarketSetting.with.derivative()
            .target(Currency.BERA.minimumSize(0.1))
            .base(Currency.USDC.minimumSize(0.0001)));

    public static final MarketService BNB = new HyperliquidService("BNB", MarketSetting.with.derivative()
            .target(Currency.BNB.minimumSize(0.001))
            .base(Currency.USDC.minimumSize(0.01)));

    public static final MarketService BTC = new HyperliquidService("BTC", MarketSetting.with.derivative()
            .target(Currency.BTC.minimumSize(0.00001))
            .base(Currency.USDC.minimumSize(1)));

    public static final MarketService CAKE = new HyperliquidService("CAKE", MarketSetting.with.derivative()
            .target(Currency.CAKE.minimumSize(0.1))
            .base(Currency.USDC.minimumSize(0.0001)));

    public static final MarketService DOGE = new HyperliquidService("DOGE", MarketSetting.with.derivative()
            .target(Currency.DOGE.minimumSize(1))
            .base(Currency.USDC.minimumSize(0.00001)));

    public static final MarketService ENA = new HyperliquidService("ENA", MarketSetting.with.derivative()
            .target(Currency.ENA.minimumSize(1))
            .base(Currency.USDC.minimumSize(0.00001)));

    public static final MarketService ETH = new HyperliquidService("ETH", MarketSetting.with.derivative()
            .target(Currency.ETH.minimumSize(0.0001))
            .base(Currency.USDC.minimumSize(0.1)));

    public static final MarketService FARTCOIN = new HyperliquidService("FARTCOIN", MarketSetting.with.derivative()
            .target(Currency.FARTCOIN.minimumSize(0.1))
            .base(Currency.USDC.minimumSize(0.00001)));

    public static final MarketService GRASS = new HyperliquidService("GRASS", MarketSetting.with.derivative()
            .target(Currency.GRASS.minimumSize(0.1))
            .base(Currency.USDC.minimumSize(0.0001)));

    public static final MarketService GRIFFAIN = new HyperliquidService("GRIFFAIN", MarketSetting.with.derivative()
            .target(Currency.GRIFFAIN.minimumSize(1))
            .base(Currency.USDC.minimumSize(0.00001)));

    public static final MarketService HYPE = new HyperliquidService("HYPE", MarketSetting.with.derivative()
            .target(Currency.HYPE.minimumSize(0.01))
            .base(Currency.USDC.minimumSize(0.001)));

    public static final MarketService IP = new HyperliquidService("IP", MarketSetting.with.derivative()
            .target(Currency.IP.minimumSize(0.1))
            .base(Currency.USDC.minimumSize(0.0001)));

    public static final MarketService JUP = new HyperliquidService("JUP", MarketSetting.with.derivative()
            .target(Currency.JUP.minimumSize(1))
            .base(Currency.USDC.minimumSize(0.00001)));

    public static final MarketService LAYER = new HyperliquidService("LAYER", MarketSetting.with.derivative()
            .target(Currency.LAYER.minimumSize(1))
            .base(Currency.USDC.minimumSize(0.00001)));

    public static final MarketService LDO = new HyperliquidService("LDO", MarketSetting.with.derivative()
            .target(Currency.LDO.minimumSize(0.1))
            .base(Currency.USDC.minimumSize(0.0001)));

    public static final MarketService LINK = new HyperliquidService("LINK", MarketSetting.with.derivative()
            .target(Currency.LINK.minimumSize(0.1))
            .base(Currency.USDC.minimumSize(0.001)));

    public static final MarketService LTC = new HyperliquidService("LTC", MarketSetting.with.derivative()
            .target(Currency.LTC.minimumSize(0.01))
            .base(Currency.USDC.minimumSize(0.01)));

    public static final MarketService MELANIA = new HyperliquidService("MELANIA", MarketSetting.with.derivative()
            .target(Currency.MELANIA.minimumSize(0.1))
            .base(Currency.USDC.minimumSize(0.0001)));

    public static final MarketService ONDO = new HyperliquidService("ONDO", MarketSetting.with.derivative()
            .target(Currency.ONDO.minimumSize(1))
            .base(Currency.USDC.minimumSize(0.0001)));

    public static final MarketService PENGU = new HyperliquidService("PENGU", MarketSetting.with.derivative()
            .target(Currency.PENGU.minimumSize(1))
            .base(Currency.USDC.minimumSize(0.000001)));

    public static final MarketService POPCAT = new HyperliquidService("POPCAT", MarketSetting.with.derivative()
            .target(Currency.POPCAT.minimumSize(1))
            .base(Currency.USDC.minimumSize(0.00001)));

    public static final MarketService PURR = new HyperliquidService("PURR", MarketSetting.with.derivative()
            .target(Currency.PURR.minimumSize(1))
            .base(Currency.USDC.minimumSize(0.00001)));

    public static final MarketService SOL = new HyperliquidService("SOL", MarketSetting.with.derivative()
            .target(Currency.SOL.minimumSize(0.01))
            .base(Currency.USDC.minimumSize(0.01)));

    public static final MarketService SUI = new HyperliquidService("SUI", MarketSetting.with.derivative()
            .target(Currency.SUI.minimumSize(0.1))
            .base(Currency.USDC.minimumSize(0.0001)));

    public static final MarketService TRUMP = new HyperliquidService("TRUMP", MarketSetting.with.derivative()
            .target(Currency.TRUMP.minimumSize(0.1))
            .base(Currency.USDC.minimumSize(0.001)));

    public static final MarketService VIRTUAL = new HyperliquidService("VIRTUAL", MarketSetting.with.derivative()
            .target(Currency.VIRTUAL.minimumSize(0.1))
            .base(Currency.USDC.minimumSize(0.0001)));

    public static final MarketService VINE = new HyperliquidService("VINE", MarketSetting.with.derivative()
            .target(Currency.VVV.minimumSize(1))
            .base(Currency.USDC.minimumSize(0.000001)));

    public static final MarketService VVV = new HyperliquidService("VVV", MarketSetting.with.derivative()
            .target(Currency.VVV.minimumSize(0.01))
            .base(Currency.USDC.minimumSize(0.0001)));

    public static final MarketService WIF = new HyperliquidService("WIF", MarketSetting.with.derivative()
            .target(Currency.WIF.minimumSize(1))
            .base(Currency.USDC.minimumSize(0.00001)));

    public static final MarketService XRP = new HyperliquidService("XRP", MarketSetting.with.derivative()
            .target(Currency.XRP.minimumSize(1))
            .base(Currency.USDC.minimumSize(0.0001)));

    public static final MarketService ZEREBRO = new HyperliquidService("ZEREBRO", MarketSetting.with.derivative()
            .target(Currency.ZEREBRO.minimumSize(1))
            .base(Currency.USDC.minimumSize(0.000001)));

    /**
     * {@inheritDoc}
     */
    @Override
    public Exchange exchange() {
        return Exchange.Hyperliquid;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public MarketAccount account() {
        return I.make(HyperliquidAccount.class);
    }
}