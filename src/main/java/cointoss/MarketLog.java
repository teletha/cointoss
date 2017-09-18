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

import java.time.ZonedDateTime;

import cointoss.util.Span;
import kiss.Signal;

/**
 * @version 2017/09/08 18:20:48
 */
public interface MarketLog {

    /**
     * Read date from the specified date.
     * 
     * @param start
     * @return
     */
    Signal<Execution> from(ZonedDateTime start);

    /**
     * Read date from the specified date.
     * 
     * @param start
     * @return
     */
    default Signal<Execution> fromToday() {
        return from(ZonedDateTime.now());
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
        return from(ZonedDateTime.now().minusDays(days));
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
    default Signal<Execution> range(ZonedDateTime start, ZonedDateTime end) {
        return from(start).takeUntil(e -> e.exec_date.isAfter(end));
    }

    /**
     * Read date from the specified start to end.
     * 
     * @param days
     * @return
     */
    default Signal<Execution> rangeRandom(int days) {
        return range(Span.random(getCacheStart(), getCacheEnd().minusDays(1), days));
    }

    /**
     * Get the starting day of cache.
     * 
     * @return
     */
    ZonedDateTime getCacheStart();

    /**
     * Get the ending day of cache.
     * 
     * @return
     */
    ZonedDateTime getCacheEnd();

}
