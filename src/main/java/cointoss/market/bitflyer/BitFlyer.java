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

    /** Reusable market configuration. */
    private static MarketSetting FiatBaseSetting = MarketSetting.with.target(Currency.BTC)
            .baseCurrencyMinimumBidPrice(Num.of(1))
            .targetCurrencyMinimumBidSize(Num.of("0.01"))
            .targetCurrencyScaleSize(3)
            .acquirableExecutionSize(499)
            .executionLogger(BitFlyerLogger.class);

    /** Reusable market configuration. */
    private static MarketSetting BTCBaseSetting = MarketSetting.with.target(Currency.BTC)
            .baseCurrencyMinimumBidPrice(Num.of("0.00001"))
            .targetCurrencyMinimumBidSize(Num.of("0.01"))
            .baseCurrencyScaleSize(5)
            .targetCurrencyScaleSize(3)
            .acquirableExecutionSize(499);

    /** Market */
    public static final MarketService BTC_JPY = new BitFlyerService("BTC_JPY", FiatBaseSetting);

    /** Market */
    public static final MarketService FX_BTC_JPY = new BitFlyerService("FX_BTC_JPY", FiatBaseSetting);

    /** Market */
    public static final MarketService ETH_JPY = new BitFlyerService("ETH_JPY", FiatBaseSetting);

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