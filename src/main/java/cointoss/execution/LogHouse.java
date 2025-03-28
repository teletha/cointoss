/*
 * Copyright (C) 2025 The COINTOSS Development Team
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          https://opensource.org/licenses/MIT
 */
package cointoss.execution;

import java.time.LocalDate;
import java.time.ZonedDateTime;

import cointoss.MarketService;
import cointoss.util.Chrono;
import kiss.I;
import kiss.Signal;

public abstract class LogHouse {

    /** The invalid log house. */
    public static final LogHouse INVALID = new LogHouse(null) {

        @Override
        public Signal<Execution> convert(ZonedDateTime date) {
            return I.signal();
        }

        @Override
        public Signal<ZonedDateTime> collect() {
            return I.signal();
        }
    };

    /** The target service. */
    protected final MarketService service;

    /**
     * @param service
     */
    protected LogHouse(MarketService service) {
        this.service = service;
    }

    /**
     * Check whether this log house is valid or not.
     * 
     * @return
     */
    public final boolean isValid() {
        return service != null;
    }

    /**
     * Get the first day.
     * 
     * @return
     */
    public Signal<ZonedDateTime> first() {
        return collect().first();
    }

    /**
     * Get the last day.
     * 
     * @return
     */
    public Signal<ZonedDateTime> last() {
        return collect().last();
    }

    /**
     * Collect all resource locations.
     * 
     * @return
     */
    public abstract Signal<ZonedDateTime> collect();

    /**
     * Check if the log for the specified date exists.
     * 
     * @param date
     * @return
     */
    public final boolean has(LocalDate date) {
        return has(Chrono.utc(date));
    }

    /**
     * Check if the log for the specified date exists.
     * 
     * @param date
     * @return
     */
    public boolean has(ZonedDateTime date) {
        return collect().any(v -> v.isEqual(date)).waitForTerminate().to().exact();
    }

    /**
     * Convert data.
     * 
     * @return
     */
    public final Signal<Execution> convert(LocalDate date) {
        return convert(Chrono.utc(date));
    }

    /**
     * Convert data.
     * 
     * @return
     */
    public abstract Signal<Execution> convert(ZonedDateTime date);
}