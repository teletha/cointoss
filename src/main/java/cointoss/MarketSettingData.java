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

import java.util.List;

import org.immutables.value.Value;

import cointoss.execution.ExecutionDeltaLogger;
import cointoss.execution.ExecutionLog;
import cointoss.execution.ExecutionLogger;
import cointoss.util.ImmutableData;
import cointoss.util.Num;
import kiss.I;

/**
 * @version 2018/08/22 19:55:10
 */
@ImmutableData
@Value.Immutable
interface MarketSettingData {

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
    default ExecutionLogger executionLogger() {
        return new ExecutionDeltaLogger();
    }
}
