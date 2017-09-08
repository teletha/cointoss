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

import kiss.Signal;

/**
 * @version 2017/08/16 8:11:25
 */
public interface MarketBuilder {

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
