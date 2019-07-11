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
import cointoss.trade.TradingLog;

public class ConsoleAnalyzer implements Analyzer {

    /**
     * {@inheritDoc}
     */
    @Override
    public void analyze(Market market, List<TradingLog> logs) {
        for (TradingLog log : logs) {
            System.out.println(log);
        }
    }
}
