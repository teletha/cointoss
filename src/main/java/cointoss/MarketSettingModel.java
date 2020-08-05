/*
 * Copyright (C) 2020 cointoss Development Team
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          https://opensource.org/licenses/MIT
 */
package cointoss;

import java.util.List;

import cointoss.execution.ExecutionDeltaLogger;
import cointoss.execution.ExecutionLog;
import cointoss.execution.ExecutionLogger;
import cointoss.util.Num;
import icy.manipulator.Icy;

@Icy
interface MarketSettingModel {

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