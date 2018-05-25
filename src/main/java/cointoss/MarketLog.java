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

import java.io.BufferedOutputStream;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
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

import com.github.luben.zstd.ZstdInputStream;
import com.github.luben.zstd.ZstdOutputStream;
import com.univocity.parsers.csv.CsvParser;
import com.univocity.parsers.csv.CsvParserSettings;
import com.univocity.parsers.csv.CsvWriter;
import com.univocity.parsers.csv.CsvWriterSettings;

import cointoss.market.bitflyer.BitFlyerService;
import cointoss.util.Chrono;
import cointoss.util.Num;
import cointoss.util.Span;
import filer.Filer;
import kiss.I;
import kiss.Signal;

/**
 * @version 2018/05/25 9:28:50
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
    private final MarketService service;

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

    /** The current log writer. */
    private Cache logger;

    /**
     * Create log manager.
     * 
     * @param provider
     */
    public MarketLog(MarketService service) {
        this(service, Paths.get(".log").resolve(service.exchangeName).resolve(service.marketName));
    }

    /**
     * Create log manager with the specified log store directory.
     * 
     * @param service A market service.
     * @param root A log store directory.
     */
    MarketLog(MarketService service, Path root) {
        this.service = Objects.requireNonNull(service);
        this.root = Objects.requireNonNull(root);
        this.logger = new Cache(ZonedDateTime.of(2000, 1, 1, 0, 0, 0, 0, Chrono.UTC));

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
                    disposer.add(read(current).effect(e -> latestId = cacheId = e.id)
                            .take(e -> e.exec_date.isAfter(start))
                            .to(observer::accept));
                    current = current.plusDays(1);
                }
            }

            AtomicBoolean completeREST = new AtomicBoolean();

            // read from realtime API
            if (disposer.isNotDisposed()) {
                disposer.add(service.executions().effect(e -> {
                    if (e.id == 0) {
                        e.id = ++realtimeId;
                    }
                    realtimeId = e.id;
                }).skipUntil(e -> completeREST.get()).effect(this::cache).to(observer::accept));
            }

            // read from REST API
            if (disposer.isNotDisposed()) {
                disposer.add(rest(latestId).effect(this::cache).effectOnComplete(() -> completeREST.set(true)).to(observer::accept));
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

                        final File file = locateCacheLog(date).toFile();
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
     * Read data from REST API.
     */
    private Signal<Execution> rest(long id) {
        return new Signal<>((observer, disposer) -> {
            long offset = 0;
            latestId = id;

            while (disposer.isNotDisposed()) {
                try {
                    List<Execution> executions = service.executions(latestId + service.executionMaxAcquirableSize() * offset)
                            .retry()
                            .toList();

                    // skip if there is no new execution
                    if (executions.isEmpty() || executions.get(0).id == latestId) {
                        offset++;
                        continue;
                    }
                    offset = 0;

                    for (int i = executions.size() - 1; 0 <= i; i--) {
                        Execution exe = executions.get(i);

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
                    Thread.sleep(110);
                } catch (final InterruptedException e) {
                    observer.error(e);
                }
            }
            observer.complete();

            return disposer;
        });
    }

    /**
     * Read log at the specified date.
     * 
     * @param year
     * @param month
     * @param day
     * @return
     */
    public final Signal<Execution> at(int year, int month, int day) {
        return at(LocalDate.of(year, month, day));
    }

    /**
     * Read log at the specified date.
     * 
     * @param date
     * @return
     */
    public final Signal<Execution> at(LocalDate date) {
        return at(date.atTime(0, 0).atZone(Chrono.UTC));
    }

    /**
     * Read log at the specified date.
     * 
     * @param date
     * @return
     */
    public final Signal<Execution> at(ZonedDateTime date) {
        if (date.isBefore(service.start())) {
            return Signal.EMPTY;
        }

        // check cache
        Path file = locateCache(date);

        // no cache
        if (file == null) {
            // try the previous day
            return at(date.minusDays(1));
        }
        return read(file);
    }

    /**
     * Locate local cache file by date.
     * 
     * @param dateTime A date info.
     * @return
     */
    private Path locateCache(ZonedDateTime dateTime) {
        String date = Chrono.DateCompact.format(dateTime);

        // search compressed log
        Path file = root.resolve("execution" + date + ".zlog");

        if (Files.exists(file)) {
            return file;
        }

        // search compact log
        file = root.resolve("execution" + date + ".clog");

        if (Files.exists(file)) {
            return file;
        }

        // search normal log
        file = root.resolve("execution" + date + ".log");

        if (Files.exists(file)) {
            return file;
        }
        return null;
    }

    /**
     * Read log from the specified date.
     * 
     * @param start
     * @return
     */
    public final Signal<Execution> fromToday() {
        return from(ZonedDateTime.now());
    }

    /**
     * Read log from the specified date.
     * 
     * @param start
     * @return
     */
    public final Signal<Execution> fromYestaday() {
        return fromLast(1);
    }

    /**
     * Read log from the specified date.
     * 
     * @param start
     * @return
     */
    public final Signal<Execution> fromLast(int days) {
        return fromLast(days, ChronoUnit.DAYS);
    }

    /**
     * Read log from the specified date.
     * 
     * @param time A duration.
     * @param unit A duration unit.
     * @return
     */
    public final Signal<Execution> fromLast(int time, ChronoUnit unit) {
        return from(ZonedDateTime.now(Chrono.UTC).minus(time, unit));
    }

    /**
     * Read log from the specified start to end.
     * 
     * @param start
     * @param end
     * @return
     */
    public final Signal<Execution> rangeAll() {
        return range(cacheFirst, cacheLast);
    }

    /**
     * Read log from the specified start to end.
     * 
     * @param start
     * @param end
     * @return
     */
    public final Signal<Execution> range(Span span) {
        return range(span.start, span.end);
    }

    /**
     * Read log from the specified start to end.
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
     * Read log from the specified start to end.
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
        return read(locateCacheLog(date));
    }

    /**
     * Read {@link Execution} log.
     * 
     * @param file
     * @return
     */
    private final Signal<Execution> read(Path file) {
        return new Signal<>((observer, disposer) -> {
            try {
                Path compact = computeCompactLogFile(file);
                boolean hasCompact = Files.exists(compact);

                CsvParserSettings settings = new CsvParserSettings();
                settings.getFormat().setDelimiter(' ');

                CsvParser parser = new CsvParser(settings);
                if (hasCompact) {
                    parser.beginParsing(new InputStreamReader(new ZstdInputStream(new FileInputStream(compact
                            .toFile())), StandardCharsets.ISO_8859_1));
                } else {
                    parser.beginParsing(Files.newBufferedReader(file));
                }

                String[] row;
                Execution previous = null;

                while ((row = parser.parseNext()) != null && disposer.isNotDisposed()) {
                    observer.accept(previous = service.decode(row, hasCompact ? previous : null));
                }

                parser.stopParsing();
                observer.complete();
                return disposer;
            } catch (Exception e) {
                throw I.quiet(e);
            }
        });
    }

    /**
     * Locate local cache by the specified date.
     * 
     * @param date
     * @return
     */
    final Path locateCacheLog(ZonedDateTime date) {
        return root.resolve("execution" + fileName.format(date) + ".log");
    }

    /**
     * Locate local compressed cache by the specified date.
     * 
     * @param date
     * @return
     */
    final Path locateCompressedLog(ZonedDateTime date) {
        return root.resolve("execution" + fileName.format(date) + ".clog");
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

            CsvWriter writer = new CsvWriter(new OutputStreamWriter(new ZstdOutputStream(new BufferedOutputStream(new FileOutputStream(output
                    .toFile())), 3), StandardCharsets.ISO_8859_1), writerConfig);

            Execution previous = null;
            String[] row = null;
            while ((row = parser.parseNext()) != null) {
                // BitFlyerExecution current = BitFlyerExecution.parse(row, previous);
                // writer.writeRow(service.encode(current, previous));
                // previous = current;
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
        return file.resolveSibling(file.getFileName().toString().replace(".log", ".clog"));
    }

    /**
     * Write the specified {@link Execution} to log file.
     * 
     * @param execution
     */
    void writeLog(Execution execution) {
        if (logger.date.equals(execution.exec_date.toLocalDate())) {
            logger.write(execution);
        } else {

        }
    }

    /**
     * @version 2018/05/19 12:17:09
     */
    private class Cache {

        /** The target date. */
        private final LocalDate date;

        /** The log file. */
        private final Path log;

        /** The compressed log file. */
        private final Path compressed;

        /** The latest id. */
        private long latest;

        /**
         * @param date
         */
        private Cache(ZonedDateTime date) {
            this.date = date.toLocalDate();
            this.log = locateCacheLog(date);
            this.compressed = locateCompressedLog(date);
        }

        /**
         * Read cached date.
         * 
         * @return
         */
        private Signal<Execution> read() {
            return new Signal<>((observer, disposer) -> {

                return disposer;
            });
        }

        /**
         * Write {@link Execution} log.
         * 
         * @param execution
         */
        private void write(Execution execution) {
            System.out.println(execution);
        }
    }

    // public static void main(String[] args) {
    // MarketLog log = new MarketLog(BitFlyerService.FX_BTC_JPY);
    // long start = System.currentTimeMillis();
    // Filer.walk(log.root, "*.log").to(file -> {
    // long s = System.currentTimeMillis();
    // log.read(file).to(e -> {
    // });
    // long e = System.currentTimeMillis();
    // System.out.println("Done " + file + " " + (e - s));
    // });
    // long end = System.currentTimeMillis();
    // System.out.println(end - start);
    // }
    //
    /**
     * @param args
     */
    public static void main2(String[] args) {
        System.out.println(Num.of(10));
        MarketLog log = new MarketLog(BitFlyerService.FX_BTC_JPY);

        Filer.walk(log.root, "execution*.log").to(file -> {

        });
    }

    public static void fix(int year, int month, int day) {
        MarketLog log = new MarketLog(BitFlyerService.FX_BTC_JPY);

        ZonedDateTime date = ZonedDateTime.of(year, month, day, 0, 0, 0, 0, Chrono.UTC);

        // backup
        Path cache = log.locateCache(date);
        // clear
        Filer.delete(cache);

        // restore
        long start = log.at(date.minusDays(1)).last().map(e -> e.id).to().v;
        long end = log.at(date.plusDays(1)).first().map(e -> e.id).to().v;

        log.rest(start).takeWhile(e -> e.id < end).to(log::cache);
        System.out.println("END");
    }
}
