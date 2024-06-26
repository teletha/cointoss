/*
 * Copyright (C) 2024 The COINTOSS Development Team
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          https://opensource.org/licenses/MIT
 */
package cointoss.analyze;

import java.util.List;

import cointoss.Market;
import cointoss.execution.Execution;
import cointoss.trade.Trader;

public interface Analyzer {

    /**
     * Set up phase.
     * 
     * @param market
     * @param traders
     */
    default void initialize(Market market, List<Trader> traders) {
        // do nothing
    }

    /**
     * Show progress.
     * 
     * @param e
     */
    default void progress(Execution e) {
        // do nothing
    }

    /**
     * Analyze the given {@link TradingStats}s.
     * 
     * @param market A target market.
     * @param logs A list of logs to visialize.
     */
    void analyze(Market market, List<TradingStats> logs, boolean detail);
}