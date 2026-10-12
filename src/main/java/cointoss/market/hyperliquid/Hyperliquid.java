/*
 * Copyright (C) 2025 The COINTOSS Development Team
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

    public static final MarketService AVAX = new HyperliquidService("AVAX", MarketSetting.with.derivative()
            .target(Currency.AVAX.minimumSize(0.01))
            .base(Currency.USDC.minimumSize(0.001)));

    public static final MarketService BNB = new HyperliquidService("BNB", MarketSetting.with.derivative()
            .target(Currency.BNB.minimumSize(0.001))
            .base(Currency.USDC.minimumSize(0.01)));

    public static final MarketService BTC = new HyperliquidService("BTC", MarketSetting.with.derivative()
            .target(Currency.BTC.minimumSize(0.00001))
            .base(Currency.USDC.minimumSize(1)));

    public static final MarketService DOGE = new HyperliquidService("DOGE", MarketSetting.with.derivative()
            .target(Currency.DOGE.minimumSize(1))
            .base(Currency.USDC.minimumSize(0.00001)));

    public static final MarketService ETH = new HyperliquidService("ETH", MarketSetting.with.derivative()
            .target(Currency.ETH.minimumSize(0.0001))
            .base(Currency.USDC.minimumSize(0.1)));

    public static final MarketService HYPE = new HyperliquidService("HYPE", MarketSetting.with.derivative()
            .target(Currency.HYPE.minimumSize(0.01))
            .base(Currency.USDC.minimumSize(0.001)));

    public static final MarketService LINK = new HyperliquidService("LINK", MarketSetting.with.derivative()
            .target(Currency.LINK.minimumSize(0.1))
            .base(Currency.USDC.minimumSize(0.001)));

    public static final MarketService LTC = new HyperliquidService("LTC", MarketSetting.with.derivative()
            .target(Currency.LTC.minimumSize(0.01))
            .base(Currency.USDC.minimumSize(0.01)));

    public static final MarketService SOL = new HyperliquidService("SOL", MarketSetting.with.derivative()
            .target(Currency.SOL.minimumSize(0.01))
            .base(Currency.USDC.minimumSize(0.01)));

    public static final MarketService SUI = new HyperliquidService("SUI", MarketSetting.with.derivative()
            .target(Currency.SUI.minimumSize(0.1))
            .base(Currency.USDC.minimumSize(0.0001)));

    public static final MarketService TRUMP = new HyperliquidService("TRUMP", MarketSetting.with.derivative()
            .target(Currency.TRUMP.minimumSize(0.1))
            .base(Currency.USDC.minimumSize(0.001)));

    public static final MarketService XRP = new HyperliquidService("XRP", MarketSetting.with.derivative()
            .target(Currency.XRP.minimumSize(1))
            .base(Currency.USDC.minimumSize(0.0001)));

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