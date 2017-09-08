/*
 * Copyright (C) 2017 Nameless Production Committee
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          http://opensource.org/licenses/mit-license.php
 */
package cointoss;

import java.time.LocalDate;

import cointoss.market.Span;
import kiss.Signal;

/**
 * @version 2017/09/08 18:20:48
 */
public interface MarketLog {

    /**
     * Read the initial execution data.
     * 
     * @return
     */
    Signal<Execution> initialize();

    /**
     * Read date from the specified date.
     * 
     * @param start
     * @return
     */
    Signal<Execution> from(LocalDate start);

    /**
     * Read date from the specified date.
     * 
     * @param start
     * @return
     */
    default Signal<Execution> fromToday() {
        return from(LocalDate.now());
    }

    /**
     * Read date from the specified date.
     * 
     * @param start
     * @return
     */
    default Signal<Execution> fromYestaday() {
        return fromLast(1);
    }

    /**
     * Read date from the specified date.
     * 
     * @param start
     * @return
     */
    default Signal<Execution> fromLast(int days) {
        return from(LocalDate.now().minusDays(days));
    }

    /**
     * Read date from the specified start to end.
     * 
     * @param start
     * @param end
     * @return
     */
    default Signal<Execution> range(Span span) {
        return range(span.start, span.end);
    }

    /**
     * Read date from the specified start to end.
     * 
     * @param start
     * @param end
     * @return
     */
    default Signal<Execution> range(LocalDate start, LocalDate end) {
        return from(start).takeUntil(e -> e.exec_date.toLocalDate().isAfter(end));
    }

    /**
     * Read date from the specified start to end.
     * 
     * @param days
     * @return
     */
    default Signal<Execution> rangeRandom(int days) {
        return range(Span.random(getCacheStart(), getCacheEnd(), days));
    }

    /**
     * Get the starting day of cache.
     * 
     * @return
     */
    LocalDate getCacheStart();

    /**
     * Get the ending day of cache.
     * 
     * @return
     */
    LocalDate getCacheEnd();

}
