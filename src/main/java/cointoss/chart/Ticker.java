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

import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicReference;

import org.eclipse.collections.impl.map.mutable.ConcurrentHashMap;

import cointoss.MarketLog;
import kiss.I;
import kiss.Signal;

/**
 * @version 2018/01/29 9:41:02
 */
public class Ticker {

    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    private static final Map<TickSpan, CacheWriter> writers = new ConcurrentHashMap();

    /** The actual log reader. */
    private final MarketLog log;

    /**
     * @param log
     * @param cache
     */
    public Ticker(MarketLog log) {
        this.log = log;
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
            ZonedDateTime day = start;
            ZonedDateTime[] current = new ZonedDateTime[] {start.withHour(0).withMinute(0).withSecond(0).withNano(0)};

            // read from cache
            while (day.isBefore(end)) {
                Path file = file(current[0], span);

                if (Files.exists(file)) {
                    try {
                        I.signal(Files.lines(file))
                                .map(Tick::new)
                                .effect(tick -> current[0] = tick.end)
                                .take(tick -> tick.start.isBefore(end))
                                .to(observer);
                    } catch (Exception e) {
                        break;
                    }
                    day = day.plusDays(1);
                } else {
                    break;
                }
            }

            // read from execution flow
            return disposer.add(log.range(current[0], end).map(e -> {
                Tick tick = latest.get();

                if (tick == null || !e.exec_date.isBefore(tick.end)) {
                    ZonedDateTime startTime = span.calculateStartTime(e.exec_date);
                    ZonedDateTime endTime = span.calculateEndTime(e.exec_date);

                    tick = new Tick(startTime, endTime, e.price);
                    latest.set(tick);
                }
                tick.update(e);
                return tick;
            }).effect(tick -> writers.computeIfAbsent(span, CacheWriter::new).write(tick)).to(observer));
        });
        return every ? signal : signal.diff().delay(1);
    }

    /**
     * Locate cache file.
     * 
     * @param time
     * @param span
     * @return
     */
    private Path file(ZonedDateTime time, TickSpan span) {
        return log.cacheRoot().resolve(span.name()).resolve(formatter.format(time).concat(".log"));
    }

    /**
     * @version 2018/01/29 16:57:46
     */
    private class CacheWriter {

        private final TickSpan span;

        private final Executor writer = Executors.newSingleThreadExecutor();

        private Tick latest;

        /**
         * @param span
         */
        private CacheWriter(TickSpan span) {
            this.span = span;
        }

        /**
         * Write tick to cache.
         * 
         * @param tick
         */
        private void write(Tick tick) {
            if (tick != latest) {
                // write latest tick
                writer.execute(() -> {
                    try {
                        Path path = file(tick.start, span);

                        if (Files.notExists(path)) {
                            Files.createDirectories(path.getParent());
                        }

                        RandomAccessFile store = new RandomAccessFile(path.toFile(), "rw");
                        FileChannel channel = store.getChannel();
                        channel.position(channel.size());
                        channel.write(ByteBuffer.wrap((tick + "\r\n").getBytes(StandardCharsets.UTF_8)));
                        channel.close();
                        store.close();
                    } catch (Exception e) {
                        throw I.quiet(e);
                    }
                });

                // next
                latest = tick;
            }
        }
    }
}
