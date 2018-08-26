/*
 * Copyright (C) 2017 Nameless Production Committee
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          http://opensource.org/licenses/mit-license.php
 */
package cointoss;

import java.util.List;

import org.immutables.value.Value;

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
}
