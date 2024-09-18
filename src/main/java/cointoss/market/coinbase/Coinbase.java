/*
 * Copyright (C) 2024 The COINTOSS Development Team
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          https://opensource.org/licenses/MIT
 */
package cointoss.market.coinbase;

import cointoss.Currency;
import cointoss.MarketService;
import cointoss.MarketSetting;
import cointoss.market.Exchange;
import cointoss.market.MarketAccount;
import cointoss.market.MarketServiceProvider;
import kiss.I;

public final class Coinbase extends MarketServiceProvider {

    public static final MarketService AAVEUSD = new CoinbaseService("AAVE-USD", MarketSetting.with.spot()
            .target(Currency.AAVE.minimumSize(0.001))
            .base(Currency.USD.minimumSize(0.01))
            .acquirableExecutionSize(1000));

    public static final MarketService BTCUSD = new CoinbaseService("BTC-USD", MarketSetting.with.spot()
            .target(Currency.BTC.minimumSize(0.0001))
            .base(Currency.USD.minimumSize(0.1))
            .priceRangeModifier(50)
            .acquirableExecutionSize(1000));

    public static final MarketService BTC_PERP = new CoinbaseService("BTC-PERP-INTX", MarketSetting.with.derivative()
            .target(Currency.BTC.minimumSize(0.0001))
            .base(Currency.USDC.minimumSize(0.1))
            .priceRangeModifier(50)
            .acquirableExecutionSize(1000));

    public static final MarketService COMPUSDC = new CoinbaseService("COMP-USDC", MarketSetting.with.spot()
            .target(Currency.COMP.minimumSize(0.001))
            .base(Currency.USDC.minimumSize(0.01))
            .priceRangeModifier(1)
            .acquirableExecutionSize(1000));

    public static final MarketService DOGE_PERP = new CoinbaseService("DOGE-PERP-INTX", MarketSetting.with.derivative()
            .target(Currency.DOGE.minimumSize(1))
            .base(Currency.USDC.minimumSize(0.00001))
            .priceRangeModifier(1)
            .acquirableExecutionSize(1000));

    public static final MarketService ETHUSD = new CoinbaseService("ETH-USD", MarketSetting.with.spot()
            .target(Currency.ETH.minimumSize(0.0001))
            .base(Currency.USD.minimumSize(0.01))
            .priceRangeModifier(100)
            .acquirableExecutionSize(1000));

    public static final MarketService ETH_PERP = new CoinbaseService("ETH-PERP-INTX", MarketSetting.with.spot()
            .target(Currency.ETH.minimumSize(0.0001))
            .base(Currency.USD.minimumSize(0.01))
            .priceRangeModifier(100)
            .acquirableExecutionSize(1000));

    public static final MarketService XRPUSD = new CoinbaseService("XRP-USD", MarketSetting.with.spot()
            .target(Currency.XRP.minimumSize(0.000001))
            .base(Currency.USD.minimumSize(0.0001))
            .priceRangeModifier(1)
            .acquirableExecutionSize(1000));

    public static final MarketService SOLUSD = new CoinbaseService("SOL-USD", MarketSetting.with.spot()
            .target(Currency.SOL.minimumSize(0.00000001))
            .base(Currency.USD.minimumSize(0.01))
            .priceRangeModifier(1)
            .acquirableExecutionSize(1000));

    public static final MarketService SOL_PERP = new CoinbaseService("SOL-PERP-INTX", MarketSetting.with.derivative()
            .target(Currency.SOL.minimumSize(0.001))
            .base(Currency.USDC.minimumSize(0.001))
            .priceRangeModifier(1)
            .acquirableExecutionSize(1000));

    public static final MarketService SUIUSD = new CoinbaseService("SUI-USD", MarketSetting.with.spot()
            .target(Currency.SUI.minimumSize(0.1))
            .base(Currency.USD.minimumSize(0.0001))
            .priceRangeModifier(1)
            .acquirableExecutionSize(1000));

    public static final MarketService SUI_PERP = new CoinbaseService("SUI-PERP-INTX", MarketSetting.with.derivative()
            .target(Currency.SUI.minimumSize(0.1))
            .base(Currency.USDC.minimumSize(0.0001))
            .priceRangeModifier(1)
            .acquirableExecutionSize(1000));

    /**
     * {@inheritDoc}
     */
    @Override
    public Exchange exchange() {
        return Exchange.Coinbase;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public MarketAccount account() {
        return I.make(CoinbaseAccount.class);
    }
}