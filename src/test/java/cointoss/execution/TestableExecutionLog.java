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

import java.nio.file.FileSystem;
import java.time.ZonedDateTime;

import com.google.common.jimfs.Jimfs;

import cointoss.MarketService;
import cointoss.ticker.Span;
import cointoss.ticker.TickerBuilder;
import cointoss.util.Chrono;
import kiss.I;
import psychopath.Locator;

public class TestableExecutionLog extends ExecutionLog {

    private static final FileSystem fs = Jimfs.newFileSystem();

    public TestableExecutionLog(MarketService service) {
        super(service, Locator.directory(fs.getPath(service.marketName)));
    }

    /**
     * Create fast log with random execution data.
     * 
     * @param start
     * @param end
     */
    public void generateFastLog(ZonedDateTime start, ZonedDateTime end, Span span, boolean buildTickerData) {
        for (ZonedDateTime date : Chrono.range(start, end)) {
            cache(date).writeFast(Executions.random(start, end, span))
                    .effectOnLifecycle(buildTickerData ? new TickerBuilder(service) : null)
                    .to(I.NoOP);
        }
    }
}
