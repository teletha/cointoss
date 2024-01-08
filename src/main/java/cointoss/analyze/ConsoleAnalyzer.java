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

public class ConsoleAnalyzer implements Analyzer {

    /**
     * {@inheritDoc}
     */
    @Override
    public void analyze(Market market, List<TradingStats> logs, boolean detail) {
        for (TradingStats log : logs) {
            log.showByText(System.out, detail);
        }
    }
}