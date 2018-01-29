/*
 * Copyright (C) 2017 Nameless Production Committee
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          http://opensource.org/licenses/mit-license.php
 */
package cointoss.chart;

import java.nio.file.Files;
import java.nio.file.Path;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.atomic.AtomicReference;

import cointoss.MarketLog;
import kiss.I;
import kiss.Signal;

/**
 * @version 2018/01/29 9:41:02
 */
public class Ticker {

    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    /** The actual log reader. */
    private final MarketLog log;

    /** The cache directory. */
    private final Path cache;

    /**
     * @param log
     * @param cache
     */
    public Ticker(MarketLog log, Path cache) {
        this.log = log;
        this.cache = log.cacheRoot();
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
    public Signal<Tick> read(ZonedDateTime start, ZonedDateTime end, TickSpan span, boolean every) {
        AtomicReference<Tick> latest = new AtomicReference();

        Signal<Tick> signal = new Signal<>((observer, disposer) -> {
            ZonedDateTime current = start.withHour(0).withMinute(0).withSecond(0).withNano(0);

            // read from cache
            while (current.isBefore(end)) {
                Path file = file(current, span);

                if (Files.exists(file)) {
                    try {
                        I.signal(Files.lines(file)).map(Tick::new).to(observer);
                    } catch (Exception e) {
                        throw I.quiet(e);
                    }
                    current = current.plusDays(1);
                } else {
                    break;
                }
            }

            // read from execution flow
            return disposer.add(log.range(current, end).map(e -> {
                Tick tick = latest.get();

                if (tick == null || !e.exec_date.isBefore(tick.end)) {
                    ZonedDateTime startTime = span.calculateStartTime(e.exec_date);
                    ZonedDateTime endTime = span.calculateEndTime(e.exec_date);

                    tick = new Tick(startTime, endTime, e.price);
                    latest.set(tick);
                }
                tick.update(e);
                return tick;
            }).effect(tick -> writeCache(tick, span)).to(observer));
        });
        return every ? signal : signal.diff().delay(1);
    }

    private void writeCache(Tick tick, TickSpan span) {
        System.out.println(tick);
    }

    /**
     * Locate cache file.
     * 
     * @param time
     * @param span
     * @return
     */
    private Path file(ZonedDateTime time, TickSpan span) {
        return cache.resolve(span.name()).resolve(formatter.format(time).concat(".log"));
    }
}
