/*
 * Copyright (C) 2024 The COINTOSS Development Team
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          https://opensource.org/licenses/MIT
 */
package cointoss.market.coincheck;

import java.util.function.UnaryOperator;

import cointoss.Currency;
import cointoss.MarketService;
import cointoss.MarketSetting;
import cointoss.market.Exchange;
import cointoss.market.MarketAccount;
import cointoss.market.MarketServiceProvider;
import hypatia.Num;
import kiss.I;

public final class Coincheck extends MarketServiceProvider {

    private static final UnaryOperator<Num> withdrawJPY = size -> Num.of("407");

    public static final MarketService BTC_JPY = new CoincheckService("btc_jpy", MarketSetting.with.spot()
            .target(Currency.BTC.minimumSize(0.001))
            .base(Currency.JPY.minimumSize(1))
            .priceRangeModifier(500)
            .targetWithdrawingFee(size -> Num.of("0.001"))
            .baseWithdrawingFee(withdrawJPY));

    /**
     * {@inheritDoc}
     */
    @Override
    public Exchange exchange() {
        return Exchange.Coincheck;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public MarketAccount account() {
        return I.make(CoincheckAccount.class);
    }
}