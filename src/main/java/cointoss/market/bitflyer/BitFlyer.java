/*
 * Copyright (C) 2021 cointoss Development Team
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          https://opensource.org/licenses/MIT
 */
package cointoss.market.bitflyer;

import java.util.function.UnaryOperator;

import cointoss.Currency;
import cointoss.MarketService;
import cointoss.MarketSetting;
import cointoss.execution.Execution;
import cointoss.execution.ExecutionDeltaLogger;
import cointoss.market.MarketAccount;
import cointoss.market.MarketServiceProvider;
import cointoss.util.arithmetic.Num;
import kiss.I;

public final class BitFlyer extends MarketServiceProvider {

    /** Limitation */
    private static final int AcquirableSize = 499;

    private static final UnaryOperator<Num> JPYWithdrawFee = size -> Num.of("770");

    public static final MarketService BTC_JPY = new BitFlyerService("BTC_JPY", MarketSetting.with.spot()
            .target(Currency.BTC.minimumSize(0.001).scale(8))
            .base(Currency.JPY.minimumSize(1))
            .priceRangeModifier(500)
            .targetCurrencyBidSizes(Num.of(0.01), Num.of(0.1), Num.of(1))
            .acquirableExecutionSize(AcquirableSize)
            .executionLogger(BitFlyerLogger.class)
            .takerFee(size -> size.multiply("0.0001"))
            .targetWithdrawingFee(size -> Num.of("0.0004"))
            .baseWithdrawingFee(JPYWithdrawFee));

    public static final MarketService FX_BTC_JPY = new BitFlyerService("FX_BTC_JPY", MarketSetting.with.derivative()
            .target(Currency.BTC.minimumSize(0.01).scale(8))
            .base(Currency.JPY.minimumSize(1))
            .priceRangeModifier(500)
            .targetCurrencyBidSizes(Num.of(0.01), Num.of(0.1), Num.of(1))
            .acquirableExecutionSize(AcquirableSize)
            .executionLogger(BitFlyerLogger.class)
            .baseWithdrawingFee(JPYWithdrawFee));

    public static final MarketService ETH_JPY = new BitFlyerService("ETH_JPY", MarketSetting.with.spot()
            .target(Currency.ETH.minimumSize(0.01).scale(8))
            .base(Currency.JPY.minimumSize(1))
            .priceRangeModifier(100)
            .acquirableExecutionSize(AcquirableSize)
            .executionLogger(BitFlyerLogger.class)
            .takerFee(size -> size.multiply("0.0001"))
            .targetWithdrawingFee(size -> Num.of("0.005"))
            .baseWithdrawingFee(JPYWithdrawFee));

    public static final MarketService MONA_JPY = new BitFlyerService("MONA_JPY", MarketSetting.with.spot()
            .target(Currency.MONA.minimumSize(0.1).scale(8))
            .base(Currency.JPY.minimumSize(0.001).scale(3))
            .priceRangeModifier(100)
            .acquirableExecutionSize(AcquirableSize)
            .executionLogger(BitFlyerLogger.class)
            .takerFee(size -> size.multiply("0.0001"))
            .targetWithdrawingFee(size -> Num.of("0.005"))
            .baseWithdrawingFee(JPYWithdrawFee));

    public static final MarketService XRP_JPY = new BitFlyerService("XRP_JPY", MarketSetting.with.spot()
            .target(Currency.XRP.minimumSize(0.1).scale(6))
            .base(Currency.JPY.minimumSize(0.01).scale(2))
            .priceRangeModifier(10)
            .acquirableExecutionSize(AcquirableSize)
            .executionLogger(BitFlyerLogger.class)
            .takerFee(size -> size.multiply("0.0001"))
            .targetWithdrawingFee(size -> Num.of("0.005"))
            .baseWithdrawingFee(JPYWithdrawFee));

    public static final MarketService XLM_JPY = new BitFlyerService("XLM_JPY", MarketSetting.with.spot()
            .target(Currency.XLM.minimumSize(0.1).scale(6))
            .base(Currency.JPY.minimumSize(0.001).scale(3))
            .priceRangeModifier(100)
            .acquirableExecutionSize(AcquirableSize)
            .executionLogger(BitFlyerLogger.class)
            .takerFee(size -> size.multiply("0.0001"))
            .targetWithdrawingFee(size -> Num.of("0.005"))
            .baseWithdrawingFee(JPYWithdrawFee));

    /**
     * {@inheritDoc}
     */
    @Override
    public MarketAccount account() {
        return I.make(BitFlyerAccount.class);
    }

    /** The specialized logger. */
    private static class BitFlyerLogger extends ExecutionDeltaLogger {
        @Override
        protected Num decodePrice(String value, Execution previous) {
            return decodeIntegralDelta(value, previous.price, 0);
        }

        @Override
        protected String encodePrice(Execution execution, Execution previous) {
            return encodeIntegralDelta(execution.price, previous.price, 0);
        }
    }
}