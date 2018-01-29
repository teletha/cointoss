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

import java.nio.file.Path;
import java.time.ZonedDateTime;
import java.util.concurrent.atomic.AtomicReference;

import cointoss.chart.ETick;
import cointoss.chart.TickSpan;
import kiss.Signal;

/**
 * @version 2018/01/29 9:41:02
 */
public class MarketTicker {

    /** The actual log reader. */
    private final MarketLog log;

    /** The cache directory. */
    private final Path cache;

    /**
     * @param log
     * @param cache
     */
    public MarketTicker(MarketLog log, Path cache) {
        this.log = log;
        this.cache = cache;
    }

    /**
     * Read tick data.
     * 
     * @param start
     * @param end
     * @param span
     * @param every
     * @return
     */
    public Signal<ETick> read(ZonedDateTime start, ZonedDateTime end, TickSpan span, boolean every) {
        AtomicReference<ETick> latest = new AtomicReference();

        Signal<ETick> signal = log.range(start, end).map(e -> {
            ETick tick = latest.get();

            if (tick == null || !e.exec_date.isBefore(tick.end)) {
                // add
                ZonedDateTime startTime = span.calculateStartTime(e.exec_date);
                ZonedDateTime endTime = span.calculateEndTime(e.exec_date);

                tick = new ETick(startTime, endTime, e.price);
                latest.set(tick);
            }
            tick.update(e);
            return tick;
        });
        return every ? signal : signal.diff().delay(1);
    }
}
