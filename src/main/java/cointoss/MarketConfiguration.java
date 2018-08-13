/*
 * Copyright (C) 2018 Nameless Production Committee
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          http://opensource.org/licenses/mit-license.php
 */
package cointoss;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import cointoss.util.Num;

/**
 * @version 2018/08/14 0:55:28
 */
public final class MarketConfiguration {

    /**
     * Configure the minimum bid price of the base currency.
     */
    public Num baseCurrencyMinimumBidPrice;

    /**
     * Configure the minimum bid size of the target currency.
     */
    public Num targetCurrencyMinimumBidSize;

    /**
     * Configure the price range of grouped order books.
     */
    public List<Num> orderBookGroupRanges = new ArrayList(4);

    /**
     * Set up and validate.
     */
    void initialize() {
        Objects.requireNonNull(baseCurrencyMinimumBidPrice);
        Objects.requireNonNull(targetCurrencyMinimumBidSize);
        Objects.requireNonNull(orderBookGroupRanges);
    }
}
