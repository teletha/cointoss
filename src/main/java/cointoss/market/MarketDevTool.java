/*
 * Copyright (C) 2023 The COINTOSS Development Team
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          https://opensource.org/licenses/MIT
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
        Market m = Market.of(service);
        m.readLog(log -> log.fromToday());
    }
}