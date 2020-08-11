/*
 * Copyright (C) 2020 cointoss Development Team
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          https://opensource.org/licenses/MIT
 */
package cointoss.market.bitflyer;

import cointoss.Currency;
import cointoss.MarketService;
import cointoss.MarketSetting;
import cointoss.execution.Execution;
import cointoss.execution.ExecutionDeltaLogger;
import cointoss.market.MarketAccount;
import cointoss.market.MarketServiceProvider;
import cointoss.util.Num;
import kiss.I;

public final class BitFlyer extends MarketServiceProvider {

    /** Limitation */
    private static final int AcquirableSize = 499;

    public static final MarketService BTC_JPY = new BitFlyerService("BTC_JPY", MarketSetting.with
            .target(Currency.BTC.minimumSize(0.01).scale(8))
            .base(Currency.JPY.minimumSize(1))
            .targetCurrencyBidSizes(Num.of(0.01), Num.of(0.1), Num.of(1))
            .acquirableExecutionSize(AcquirableSize)
            .executionLogger(BitFlyerLogger.class));

    public static final MarketService FX_BTC_JPY = new BitFlyerService("FX_BTC_JPY", MarketSetting.with
            .target(Currency.BTC.minimumSize(0.01).scale(8))
            .base(Currency.JPY.minimumSize(1))
            .targetCurrencyBidSizes(Num.of(0.01), Num.of(0.1), Num.of(1))
            .acquirableExecutionSize(AcquirableSize)
            .executionLogger(BitFlyerLogger.class));

    public static final MarketService ETH_JPY = new BitFlyerService("ETH_JPY", MarketSetting.with
            .target(Currency.ETH.minimumSize(0.01).scale(8))
            .base(Currency.JPY.minimumSize(1))
            .acquirableExecutionSize(AcquirableSize)
            .executionLogger(BitFlyerLogger.class));

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