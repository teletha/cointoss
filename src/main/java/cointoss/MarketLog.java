/*
 * Copyright (C) 2018 CoinToss Development Team
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          https://opensource.org/licenses/MIT
 */
package cointoss;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;

import com.univocity.parsers.csv.CsvParser;
import com.univocity.parsers.csv.CsvParserSettings;
import com.univocity.parsers.csv.CsvWriter;
import com.univocity.parsers.csv.CsvWriterSettings;

import cointoss.market.bitflyer.BitFlyer;
import cointoss.market.bitflyer.BitFlyerService;
import cointoss.util.Chrono;
import cointoss.util.Span;
import filer.Filer;
import kiss.I;
import kiss.Signal;

/**
 * @version 2018/04/25 4:13:13
 */
public class MarketLog {

    /** The writer thread. */
    private static final ExecutorService writer = Executors.newSingleThreadExecutor(run -> {
        final Thread thread = new Thread(run);
        thread.setName("Log Writer");
        thread.setDaemon(true);
        return thread;
    });

    /** The file data format */
    private static final DateTimeFormatter fileName = DateTimeFormatter.ofPattern("yyyyMMdd");

    /** The market provider. */
    private final MarketProvider provider;

    /** The root directory of logs. */
    private final Path root;

    /** The first day. */
    private ZonedDateTime cacheFirst;

    /** The last day. */
    private ZonedDateTime cacheLast;

    /** The latest execution id. */
    private long latestId = 23164000;

    /** The latest cached id. */
    private long cacheId;

    /** The latest realtime id. */
    private long realtimeId;

    /** The current processing cache file. */
    private BufferedWriter cache;

    /**
     * Create log manager.
     * 
     * @param provider
     */
    public MarketLog(MarketProvider provider) {
        this.provider = Objects.requireNonNull(provider);
        this.root = Paths.get(".log").resolve(provider.exchangeName()).resolve(provider.name());

        try {
            ZonedDateTime start = null;
            ZonedDateTime end = null;

            for (Path file : Filer.walk(root, "execution*.log").toList()) {
                String name = file.getFileName().toString();
                ZonedDateTime date = LocalDate.parse(name.substring(9, 17), fileName).atTime(0, 0, 0, 0).atZone(Chrono.UTC);

                if (start == null || end == null) {
                    start = date;
                    end = date;
                } else {
                    if (start.isAfter(date)) {
                        start = date;
                    }

                    if (end.isBefore(date)) {
                        end = date;
                    }
                }
            }
            this.cacheFirst = start;
            this.cacheLast = end;
        } catch (Exception e) {
            throw I.quiet(e);
        }
    }

    /**
     * Read date from the specified date.
     * 
     * @param start
     * @return
     */
    public final synchronized Signal<Execution> from(final ZonedDateTime start) {
        return new Signal<Execution>((observer, disposer) -> {
            // read from cache
            if (cacheFirst != null) {
                ZonedDateTime current = start.isBefore(cacheFirst) ? cacheFirst : start.isAfter(cacheLast) ? cacheLast : start;
                current = current.withHour(0).withMinute(0).withSecond(0).withNano(0);

                while (disposer.isDisposed() == false && !current.isAfter(cacheLast)) {
                    disposer.add(read(current).effect(e -> latestId = cacheId = e.id).take(e -> e.exec_date.isAfter(start)).effect(e -> {
                        e.delay = BitFlyerService.estimateDelay(e);
                    }).to(observer::accept));
                    current = current.plusDays(1);
                }
            }

            final AtomicBoolean completeREST = new AtomicBoolean();

            // read from realtime API
            if (disposer.isDisposed() == false) {
                disposer.add(provider.service().executions().effect(e -> {
                    if (e.id == 0) {
                        e.id = ++realtimeId;
                    }
                    realtimeId = e.id;
                }).skipUntil(e -> completeREST.get()).effect(this::cache).to(observer::accept));
            }

            // read from REST API
            if (disposer.isDisposed() == false) {
                disposer.add(rest().effect(this::cache).effectOnComplete(() -> completeREST.set(true)).to(observer::accept));
            }

            return disposer;
        });
    }

    /**
     * Store cache data.
     * 
     * @param exe
     */
    private void cache(final Execution exe) {
        if (cacheId < exe.id) {
            cacheId = exe.id;

            writer.submit(() -> {
                try {
                    final ZonedDateTime date = exe.exec_date;

                    if (cache == null || cacheLast.isBefore(date)) {
                        I.quiet(cache);

                        final File file = localCacheFile(date).toFile();
                        file.createNewFile();

                        cache = new BufferedWriter(new FileWriter(file, true));
                        cacheLast = date.withHour(23).withMinute(59).withSecond(59).withNano(999999999);
                    }
                    cache.write(exe.toString() + "\r\n");
                    cache.flush();
                } catch (final IOException e) {
                    throw I.quiet(e);
                }
            });
        }
    }

    /**
     * Read data in realtime.
     * 
     * @return
     */
    /**
     * Read data from REST API.
     */
    private Signal<Execution> rest() {
        return new Signal<Execution>((observer, disposer) -> {
            int offset = 0;

            while (disposer.isDisposed() == false) {
                try {
                    List<Execution> executions = provider.service().executions(latestId + 499 * offset).toList();

                    // skip if there is no new execution
                    if (executions.get(0).id == latestId) {
                        offset++;
                        continue;
                    }
                    offset = 0;

                    for (int i = executions.size() - 1; 0 <= i; i--) {
                        final Execution exe = executions.get(i);

                        if (latestId < exe.id) {
                            observer.accept(exe);
                            latestId = exe.id;
                        }
                    }
                } catch (Exception e) {
                    // ignore to retry
                }

                if (realtimeId != 0 && realtimeId <= latestId) {
                    break;
                }

                try {
                    Thread.sleep(222);
                } catch (final InterruptedException e) {
                    observer.error(e);
                }
            }
            observer.complete();

            return disposer;
        });
    }

    /**
     * Read date from the specified date.
     * 
     * @param start
     * @return
     */
    public final Signal<Execution> fromToday() {
        return from(ZonedDateTime.now());
    }

    /**
     * Read date from the specified date.
     * 
     * @param start
     * @return
     */
    public final Signal<Execution> fromYestaday() {
        return fromLast(1);
    }

    /**
     * Read date from the specified date.
     * 
     * @param start
     * @return
     */
    public final Signal<Execution> fromLast(int days) {
        return fromLast(days, ChronoUnit.DAYS);
    }

    /**
     * Read date from the specified date.
     * 
     * @param time A duration.
     * @param unit A duration unit.
     * @return
     */
    public final Signal<Execution> fromLast(int time, ChronoUnit unit) {
        return from(ZonedDateTime.now(Chrono.UTC).minus(time, unit));
    }

    /**
     * Read date from the specified start to end.
     * 
     * @param start
     * @param end
     * @return
     */
    public final Signal<Execution> rangeAll() {
        return range(cacheFirst, cacheLast);
    }

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
    public final Signal<Execution> range(ZonedDateTime start, ZonedDateTime end) {
        if (start.isBefore(end)) {
            return from(start).takeWhile(e -> e.exec_date.isBefore(end));
        } else {
            return Signal.EMPTY;
        }
    }

    /**
     * Read date from the specified start to end.
     * 
     * @param days
     * @return
     */
    public final Signal<Execution> rangeRandom(int days) {
        return range(Span.random(cacheFirst, cacheLast.minusDays(1), days));
    }

    /**
     * Read {@link Execution} log of the specified {@link LocalDate}.
     * 
     * @param date
     * @return
     */
    public final Signal<Execution> read(ZonedDateTime date) {
        return read(localCacheFile(date));
    }

    /**
     * Read {@link Execution} log.
     * 
     * @param file
     * @return
     */
    public final Signal<Execution> read(Path file) {
        return new Signal<>((observer, disposer) -> {
            Path compact = computeCompactLogFile(file);
            boolean hasCompact = Files.exists(compact);

            CsvParserSettings settings = new CsvParserSettings();
            settings.getFormat().setDelimiter(' ');

            CsvParser parser = new CsvParser(settings);
            parser.beginParsing((hasCompact ? compact : file).toFile());

            String[] row;
            Execution previous = null;

            while ((row = parser.parseNext()) != null) {
                observer.accept(previous = provider.service().decode(row, hasCompact ? previous : null));
            }

            parser.stopParsing();
            return disposer;
        });
    }

    /**
     * Read date from local cache.
     * 
     * @param date
     * @return
     */
    private final Path localCacheFile(ZonedDateTime date) {
        return root.resolve("execution" + fileName.format(date) + ".log");
    }

    /**
     * Compact log.
     * 
     * @param file
     */
    public final void compact(Path file) {
        try {
            Path output = computeCompactLogFile(file);

            if (Files.exists(output)) {
                return;
            }

            CsvParserSettings settings = new CsvParserSettings();
            settings.getFormat().setDelimiter(' ');

            CsvParser parser = new CsvParser(settings);
            parser.beginParsing(file.toFile());

            CsvWriterSettings writerConfig = new CsvWriterSettings();
            writerConfig.getFormat().setDelimiter(' ');

            CsvWriter writer = new CsvWriter(output.toFile(), writerConfig);

            Execution previous = null;
            String[] row = null;
            while ((row = parser.parseNext()) != null) {
                Execution current = new Execution(row);
                writer.writeRow(provider.service().encode(current, previous));
                previous = current;
            }
            writer.close();
            parser.stopParsing();
        } catch (Exception e) {
            throw I.quiet(e);
        }
    }

    /**
     * Compute compact log file.
     * 
     * @param file A log file.
     * @return
     */
    private Path computeCompactLogFile(Path file) {
        return file.resolveSibling(file.getFileName().toString().replace(".log", ".alog"));
    }

    /**
     * @param args
     */
    public static void main(String[] args) {
        MarketLog log = new MarketLog(BitFlyer.FX_BTC_JPY);

        Filer.walk(log.root, "*20180414.log").to(file -> {
            log.compact(file);
        });
    }
}
