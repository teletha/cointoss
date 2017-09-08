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
 * @version 2017/08/16 8:11:25
 */
public abstract class MarketLogBuilder {

    /** The start day. */
    protected LocalDate start;

    /** The end day. */
    protected LocalDate end;

    /**
     * Set the start property of this {@link MarketLogBuilder}.
     * 
     * @param start The start value to set.
     */
    public final MarketLogBuilder start(LocalDate start) {
        this.start = start;

        return this;
    }

    /**
     * Set the end property of this {@link MarketLogBuilder}.
     * 
     * @param end The end value to set.
     */
    public final MarketLogBuilder end(LocalDate end) {
        this.end = end;

        return this;
    }

    /**
     * Read the initial execution data.
     * 
     * @return
     */
    public abstract Signal<Execution> initialize();

    /**
     * Read date from the specified date.
     * 
     * @param start
     * @return
     */
    public abstract Signal<Execution> from(LocalDate start);

    /**
     * Read date from the specified start to end.
     * 
     * @param start
     * @param end
     * @return
     */
    public final Signal<Execution> range(Span span) {
        return range(span.start, span.end);
    }

    /**
     * Read date from the specified start to end.
     * 
     * @param start
     * @param end
     * @return
     */
    public final Signal<Execution> range(LocalDate start, LocalDate end) {
        return from(start).takeUntil(e -> e.exec_date.toLocalDate().isAfter(end));
    }

    /**
     * Read date from the specified start to end.
     * 
     * @param days
     * @return
     */
    public final Signal<Execution> rangeRandom(int days) {
        return range(Span.random(getCacheStart(), getCacheEnd(), days));
    }

    /**
     * Get the starting day of cache.
     * 
     * @return
     */
    public abstract LocalDate getCacheStart();

    /**
     * Get the ending day of cache.
     * 
     * @return
     */
    public abstract LocalDate getCacheEnd();

}
