/*
 * Copyright (C) 2018 Nameless Production Committee
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          http://opensource.org/licenses/mit-license.php
 */
package cointoss.market;

import cointoss.Market;
import cointoss.MarketService;

public class MarketDevTool {

    /**
     * Collect all trade log and store it.
     * 
     * @param service
     */
    public static void collectLog(MarketService service) {
        Market m = new Market(service);
        m.readLog(log -> log.fromToday());
    }
}
