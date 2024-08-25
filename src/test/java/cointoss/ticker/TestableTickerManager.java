/*
 * Copyright (C) 2024 Nameless Production Committee
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          http://opensource.org/licenses/mit-license.php
 */
package cointoss.ticker;

import java.time.ZonedDateTime;

import cointoss.MarketService;
import cointoss.execution.Executions;
import cointoss.util.Chrono;

public class TestableTickerManager extends TickerManager {

    /**
     * 
     */
    public TestableTickerManager() {
        super();
    }

    /**
     * @param service
     */
    public TestableTickerManager(MarketService service) {
        super(service);
    }

    /**
     * Create fast log with random execution data.
     * 
     * @param start
     * @param end
     */
    public void generateTicker(ZonedDateTime start, ZonedDateTime end, Span span) {
        for (ZonedDateTime date : Chrono.range(start, end)) {
            Executions.random(date, date.plusDays(1), span).to(this::update);
        }
    }
}
