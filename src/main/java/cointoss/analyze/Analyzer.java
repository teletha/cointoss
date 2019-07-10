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

import cointoss.trade.TradingLog;

public interface Analyzer {

    /**
     * Analyze the given {@link TradingLog}s.
     * 
     * @param logs A list of logs to visialize.
     */
    void analyze(List<TradingLog> logs);
}
