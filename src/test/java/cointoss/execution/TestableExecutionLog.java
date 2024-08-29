/*
 * Copyright (C) 2024 Nameless Production Committee
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          http://opensource.org/licenses/mit-license.php
 */
package cointoss.execution;

import java.time.ZonedDateTime;

import cointoss.MarketService;
import cointoss.ticker.Span;
import cointoss.util.Chrono;
import kiss.I;

public class TestableExecutionLog extends ExecutionLog {

    public TestableExecutionLog(MarketService service) {
        super(service, service.directory("log"));
    }

    /**
     * Create fast log with random execution data.
     * 
     * @param start
     * @param end
     */
    public void generateFastLog(ZonedDateTime start, ZonedDateTime end, Span span) {
        Chrono.range(start, end).to(date -> {
            cache(date).writeFast(Executions.random(date, date.plusDays(1), span)).to(I.NoOP);
        });
    }
}
