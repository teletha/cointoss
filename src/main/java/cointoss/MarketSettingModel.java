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
import java.util.function.Consumer;

import cointoss.execution.ExecutionDeltaLogger;
import cointoss.execution.ExecutionLog;
import cointoss.execution.ExecutionLogger;
import cointoss.util.Num;
import icy.manipulator.Icy;
import kiss.I;

@Icy
interface MarketSettingModel {

    /**
     * Get the minimum bid price of the base currency.
     */
    @Icy.Property
    Num baseCurrencyMinimumBidPrice();

    /**
     * Get the minimum bid size of the target currency.
     */
    @Icy.Property
    Num targetCurrencyMinimumBidSize();

    @Icy.Intercept("targetCurrencyMinimumBidSize")
    private Num deriveByMinBid(Num minBid, Consumer<List<Num>> targetCurrencyBidSizes) {
        targetCurrencyBidSizes.accept(List.of(minBid, minBid.multiply(10), minBid.multiply(100), minBid.multiply(1000)));
        return minBid;
    }

    /**
     * Get the bid size range of target currency.
     */
    @Icy.Property
    default List<Num> targetCurrencyBidSizes() {
        return List.of(Num.ONE);
    }

    /**
     * Get the price range of grouped order books.
     */
    @Icy.Property
    Num[] orderBookGroupRanges();

    /**
     * Get the human readable size of base currency.
     */
    @Icy.Property
    default int baseCurrencyScaleSize() {
        return 0;
    }

    /**
     * Get the human readable size of target currency.
     */
    @Icy.Property
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
    @Icy.Property
    default int acquirableExecutionSize() {
        return 100;
    }

    /**
     * Configure {@link ExecutionLog} parser.
     * 
     * @return
     */
    @Icy.Property
    default Class<? extends ExecutionLogger> executionLogger() {
        return ExecutionDeltaLogger.class;
    }
}
