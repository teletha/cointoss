/*
 * Copyright (C) 2019 Nameless Production Committee
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          http://opensource.org/licenses/mit-license.php
 */
package cointoss.analyze;

import java.util.List;

import cointoss.Market;
import cointoss.Trader;

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
     * Analyze the given {@link TradingStatistics}s.
     * 
     * @param market A target market.
     * @param logs A list of logs to visialize.
     */
    void analyze(Market market, List<TradingStatistics> logs, boolean detail);
}
