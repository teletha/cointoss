/*
 * Copyright (C) 2020 Nameless Production Committee
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          http://opensource.org/licenses/mit-license.php
 */
package cointoss.execution;

import java.time.LocalDate;
import java.time.ZonedDateTime;

import cointoss.MarketService;
import cointoss.util.Chrono;
import kiss.Signal;

public abstract class ExecutionLogRepository {

    /** The target service. */
    protected final MarketService service;

    /**
     * @param service
     */
    protected ExecutionLogRepository(MarketService service) {
        this.service = service;
    }

    /**
     * Get the first day.
     * 
     * @return
     */
    public final ZonedDateTime first() {
        return collect().first().waitForTerminate().to().exact();
    }

    /**
     * Collect all resource locations.
     * 
     * @param service
     * @return
     */
    public abstract Signal<ZonedDateTime> collect();

    /**
     * Convert data.
     * 
     * @param url
     * @return
     */
    public final Signal<Execution> convert(LocalDate date) {
        return convert(date.atTime(0, 0).atZone(Chrono.UTC));
    }

    /**
     * Convert data.
     * 
     * @param url
     * @return
     */
    public abstract Signal<Execution> convert(ZonedDateTime date);
}
