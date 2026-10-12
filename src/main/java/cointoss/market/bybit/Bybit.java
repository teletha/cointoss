/*
 * Copyright (C) 2025 The COINTOSS Development Team
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

    public static final MarketService BTC_USDT = new BybitService("BTCUSDT", MarketSetting.with.spot()
            .target(Currency.BTC.minimumSize(0.00001))
            .base(Currency.USDT.minimumSize(0.5)));

    public static final MarketService ETH_USDT = new BybitService("ETHUSDT", MarketSetting.with.spot()
            .target(Currency.ETH.minimumSize(0.00001))
            .base(Currency.USDT.minimumSize(0.05))
            .priceRangeModifier(20));

    public static final MarketService HYPE_USDT = new BybitService("HYPEUSDT", MarketSetting.with.derivative()
            .target(Currency.HYPE.minimumSize(0.01))
            .base(Currency.USDT.minimumSize(0.001)));

    public static final MarketService SUI_USDT = new BybitService("SUIUSDT", MarketSetting.with.spot()
            .target(Currency.SUI.minimumSize(0.01))
            .base(Currency.USDT.minimumSize(0.0001)));

    public static final MarketService TRUMP_USDT = new BybitService("TRUMPUSDT", MarketSetting.with.spot()
            .target(Currency.TRUMP.minimumSize(0.01))
            .base(Currency.USDT.minimumSize(0.001)));

    public static final MarketService WLD_USDT = new BybitService("WLDUSDT", MarketSetting.with.spot()
            .target(Currency.WLD.minimumSize(0.01))
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