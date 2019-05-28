/*
 * Copyright (C) 2019 CoinToss Development Team
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          https://opensource.org/licenses/MIT
 */
package cointoss;

import java.time.Duration;
import java.util.List;
import java.util.function.Consumer;

import cointoss.execution.ExecutionDeltaLogger;
import cointoss.execution.ExecutionLog;
import cointoss.execution.ExecutionLogger;
import cointoss.util.Num;
import cointoss.util.RetryPolicy;
import icy.manipulator.Icy;
import kiss.I;

@Icy
public abstract class MarketSettingModel {

    /**
     * Get the minimum bid price of the base currency.
     */
    @Icy.Property
    public abstract Num baseCurrencyMinimumBidPrice();

    /**
     * Get the minimum bid size of the target currency.
     */
    @Icy.Property
    public abstract Num targetCurrencyMinimumBidSize();

    @Icy.Intercept("targetCurrencyMinimumBidSize")
    private Num deriveByMinBid(Num minBid, Consumer<List<Num>> targetCurrencyBidSizes) {
        targetCurrencyBidSizes.accept(List.of(minBid, minBid.multiply(10), minBid.multiply(100), minBid.multiply(1000)));
        return minBid;
    }

    /**
     * Get the bid size range of target currency.
     */
    @Icy.Property
    public List<Num> targetCurrencyBidSizes() {
        return List.of(Num.ONE);
    }

    /**
     * Get the price range of grouped order books.
     */
    @Icy.Property
    public abstract Num[] orderBookGroupRanges();

    /**
     * Get the human readable size of target currency.
     */
    @Icy.Property
    public int targetCurrencyScaleSize() {
        return 0;
    }

    /**
     * Get the price range of grouped order books.
     */
    public List<Num> orderBookGroupRangesWithBase() {
        return I.signal(orderBookGroupRanges()).startWith(baseCurrencyMinimumBidPrice()).toList();
    }

    /**
     * Configure max acquirable execution size per one request.
     * 
     * @return
     */
    @Icy.Property
    public int acquirableExecutionSize() {
        return 100;
    }

    /**
     * Configure {@link ExecutionLog} parser.
     * 
     * @return
     */
    @Icy.Property
    public Class<? extends ExecutionLogger> executionLogger() {
        return ExecutionDeltaLogger.class;
    }

    /**
     * Configure {@link RetryPolicy}.
     * 
     * @return
     */
    @Icy.Property(mutable = true)
    public RetryPolicy retryPolicy() {
        return new RetryPolicy().retryMaximum(5).delayLinear(Duration.ofMillis(1000)).delayMaximum(Duration.ofMinutes(2));
    }
}
