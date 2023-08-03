/*
 * Copyright (C) 2023 The COINTOSS Development Team
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          https://opensource.org/licenses/MIT
 */
package cointoss;

import java.util.List;
import java.util.function.UnaryOperator;

import cointoss.execution.ExecutionDeltaLogger;
import cointoss.execution.ExecutionLogger;
import cointoss.util.arithmetic.Num;
import icy.manipulator.Icy;
import kiss.I;

@Icy
interface MarketSettingModel {

    /**
     * Sepcify the market type.
     * 
     * @return
     */
    @Icy.Property
    MarketType type();

    /**
     * Specify the target currency.
     * 
     * @return
     */
    @Icy.Property
    CurrencySetting target();

    /**
     * Specify the base currency.
     * 
     * @return
     */
    @Icy.Property
    CurrencySetting base();

    /**
     * Get the bid size range of target currency.
     */
    @Icy.Property
    default List<Num> targetCurrencyBidSizes() {
        return List.of(Num.ONE);
    }

    /**
     * Get the price range modifier of base currency.
     */
    @Icy.Property
    default int priceRangeModifier() {
        return 10;
    }

    /**
     * Get the recommended price range of base currency.
     */
    default Num recommendedPriceRange() {
        return base().minimumSize.multiply(priceRangeModifier());
    }

    /**
     * Get the maximum orderbook size in one side.
     */
    @Icy.Property
    default int orderbookMaxSize() {
        return 3000;
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
     * Configure {@link ExecutionLogger} parser.
     * 
     * @return
     */
    @Icy.Property
    default Class<? extends ExecutionLogger> executionLogger() {
        return ExecutionDeltaLogger.class;
    }

    /**
     * Get the fee on taking order.
     */
    @Icy.Property
    default UnaryOperator<Num> takerFee() {
        return size -> Num.ZERO;
    }

    /**
     * Get the fee on making order.
     */
    @Icy.Property
    default UnaryOperator<Num> makerFee() {
        return size -> Num.ZERO;
    }

    /**
     * Get the fee on withdraw.
     */
    @Icy.Property
    default UnaryOperator<Num> targetWithdrawingFee() {
        return size -> Num.ZERO;
    }

    /**
     * Get the fee on withdraw.
     */
    @Icy.Property
    default UnaryOperator<Num> baseWithdrawingFee() {
        return size -> Num.ZERO;
    }

    /**
     * Create new {@link ExecutionLogger}.
     * 
     * @return
     */
    default ExecutionLogger createExecutionLogger() {
        return I.make(executionLogger());
    }

    /**
     * Test the currency pair.
     * 
     * @param target
     * @param base
     * @return
     */
    default boolean match(Currency target, Currency base) {
        return target().currency == target && base().currency == base;
    }
}