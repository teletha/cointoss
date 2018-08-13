/*
 * Copyright (C) 2018 Nameless Production Committee
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          http://opensource.org/licenses/mit-license.php
 */
package cointoss.market.bitflyer;

import java.util.function.Consumer;

import cointoss.MarketConfiguration;
import cointoss.MarketService;
import cointoss.market.MarketProvider;
import cointoss.util.Num;
import kiss.I;

/**
 * @version 2018/08/14 0:56:58
 */
public final class BitFlyer extends MarketProvider {

    /** Reusable market configuration. */
    private static Consumer<MarketConfiguration> FiatBase = config -> {
        config.baseCurrencyMinimumBidPrice = Num.of(1);
        config.targetCurrencyMinimumBidSize = Num.of("0.01");
        config.orderBookGroupRanges = I.list(Num.of(100), Num.of(250), Num.of(500), Num.of(1000), Num.of(5000));
    };

    /** Reusable market configuration. */
    private static Consumer<MarketConfiguration> BTCBase = config -> {
        config.baseCurrencyMinimumBidPrice = Num.of("0.01");
        config.targetCurrencyMinimumBidSize = Num.of("0.01");
    };

    /** Market */
    public static final MarketService BTC_JPY = new BitFlyerService("BTC_JPY", FiatBase);

    /** Market */
    public static final MarketService FX_BTC_JPY = new BitFlyerService("FX_BTC_JPY", FiatBase);

    /** Market */
    public static final MarketService ETH_BTC = new BitFlyerService("ETH_BTC", BTCBase);

    /** Market */
    public static final MarketService BCH_BTC = new BitFlyerService("BCH_BTC", BTCBase);

    /** Market */
    public static final MarketService BTCJPY28SEP2018 = new BitFlyerService("BTCJPY28SEP2018", FiatBase);

    /** Market */
    public static final MarketService BTCJPY03AUG2018 = new BitFlyerService("BTCJPY03AUG2018", FiatBase);

    /** Market */
    public static final MarketService BTCJPY10AUG2018 = new BitFlyerService("BTCJPY10AUG2018", FiatBase);
}
