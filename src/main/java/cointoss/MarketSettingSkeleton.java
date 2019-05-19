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

import static org.immutables.value.Value.Style.ImplementationVisibility.*;

import java.time.Duration;
import java.util.List;

import org.immutables.value.Value;

import cointoss.execution.ExecutionDeltaLogger;
import cointoss.execution.ExecutionLog;
import cointoss.execution.ExecutionLogger;
import cointoss.util.Num;
import cointoss.util.RetryPolicy;
import kiss.I;

@Value.Immutable
@Value.Style(typeAbstract = "*Skeleton", typeImmutable = "*", visibility = PUBLIC)
interface MarketSettingSkeleton {

    /**
     * Get the minimum bid price of the base currency.
     */
    Num baseCurrencyMinimumBidPrice();

    /**
     * Get the minimum bid size of the target currency.
     */
    Num targetCurrencyMinimumBidSize();

    /**
     * Get the bid size range of target currency.
     */
    default List<Num> targetCurrencyBidSizes() {
        Num base = targetCurrencyMinimumBidSize();

        return List.of(base, base.multiply(10), base.multiply(100), base.multiply(1000));
    }

    /**
     * Get the price range of grouped order books.
     */
    Num[] orderBookGroupRanges();

    /**
     * Get the human readable size of target currency.
     */
    @Value.Default
    default int targetCurrencyScaleSize() {
        return 0;
    }

    /**
     * Get the price range of grouped order books.
     */
    default List<Num> orderBookGroupRangesWithBase() {
        return I.signal(orderBookGroupRanges()).startWith(baseCurrencyMinimumBidPrice()).toList();
    }

    /**
     * Configure max acquirable execution size per one request.
     * 
     * @return
     */
    @Value.Default
    default int acquirableExecutionSize() {
        return 100;
    }

    /**
     * Configure {@link ExecutionLog} parser.
     * 
     * @return
     */
    @Value.Default
    default Class<? extends ExecutionLogger> executionLogger() {
        return ExecutionDeltaLogger.class;
    }

    /**
     * Configure {@link RetryPolicy}.
     * 
     * @return
     */
    @Value.Default
    default RetryPolicy retryPolicy() {
        return new RetryPolicy().retryMaximum(5).delayLinear(Duration.ofMillis(1000)).delayMaximum(Duration.ofMinutes(2));
    }
}
