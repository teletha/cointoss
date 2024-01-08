/*
 * Copyright (C) 2024 The COINTOSS Development Team
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          https://opensource.org/licenses/MIT
 */
package cointoss.market.bitbank;

import java.util.function.UnaryOperator;

import cointoss.Currency;
import cointoss.MarketService;
import cointoss.MarketSetting;
import cointoss.market.MarketAccount;
import cointoss.market.MarketServiceProvider;
import cointoss.util.arithmetic.Num;
import kiss.I;

public final class Bitbank extends MarketServiceProvider {

    private static final UnaryOperator<Num> taking = size -> size.multiply("0.0012");

    private static final UnaryOperator<Num> making = size -> size.multiply("-0.0002");

    private static final UnaryOperator<Num> withdrawJPY = size -> Num.of("770");

    public static final MarketService BTC_JPY = new BitbankService("btc_jpy", MarketSetting.with.spot()
            .target(Currency.BTC.minimumSize(0.0001))
            .base(Currency.JPY.minimumSize(1))
            .priceRangeModifier(500)
            .takerFee(taking)
            .makerFee(making)
            .baseWithdrawingFee(withdrawJPY)
            .targetWithdrawingFee(size -> Num.of("0.001")));

    public static final MarketService ETH_JPY = new BitbankService("eth_jpy", MarketSetting.with.spot()
            .target(Currency.ETH.minimumSize(0.0001))
            .base(Currency.JPY.minimumSize(1))
            .priceRangeModifier(100)
            .takerFee(taking)
            .makerFee(making)
            .baseWithdrawingFee(withdrawJPY)
            .targetWithdrawingFee(size -> Num.of("0.005")));

    public static final MarketService LTC_JPY = new BitbankService("ltc_jpy", MarketSetting.with.spot()
            .target(Currency.LTC.minimumSize(0.0001))
            .base(Currency.JPY.minimumSize(0.1))
            .takerFee(taking)
            .makerFee(making)
            .baseWithdrawingFee(withdrawJPY)
            .targetWithdrawingFee(size -> Num.of("0.001")));

    public static final MarketService MONA_JPY = new BitbankService("mona_jpy", MarketSetting.with.spot()
            .target(Currency.MONA.minimumSize(0.0001))
            .base(Currency.JPY.minimumSize(0.001))
            .takerFee(taking)
            .makerFee(making));

    public static final MarketService XLM_JPY = new BitbankService("xlm_jpy", MarketSetting.with.spot()
            .target(Currency.XLM.minimumSize(0.0001))
            .base(Currency.JPY.minimumSize(0.001))
            .takerFee(taking)
            .makerFee(making)
            .baseWithdrawingFee(withdrawJPY)
            .targetWithdrawingFee(size -> Num.of("0.01")));

    public static final MarketService XRP_JPY = new BitbankService("xrp_jpy", MarketSetting.with.spot()
            .target(Currency.XRP.minimumSize(0.0001))
            .base(Currency.JPY.minimumSize(0.001))
            .takerFee(taking)
            .makerFee(making)
            .baseWithdrawingFee(withdrawJPY)
            .targetWithdrawingFee(size -> Num.of("0.15")));

    /**
     * {@inheritDoc}
     */
    @Override
    public MarketAccount account() {
        return I.make(BitbankAccount.class);
    }
}