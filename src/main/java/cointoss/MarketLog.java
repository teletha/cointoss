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

import static java.nio.charset.StandardCharsets.*;
import static java.nio.file.Files.*;
import static java.nio.file.StandardOpenOption.*;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalAccessor;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.Delayed;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
 * Log Manager.
 * 
 * @version 2018/05/26 10:41:05
 */
public class MarketLog {

    /** NOOP TASK */
    private static final ScheduledFuture NOOP = new ScheduledFuture() {

        /**
         * {@inheritDoc}
         */
        @Override
        public int compareTo(Delayed o) {
            return 0;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public boolean cancel(boolean mayInterruptIfRunning) {
            return false;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public boolean isCancelled() {
            return false;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public boolean isDone() {
            return false;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public Object get() throws InterruptedException, ExecutionException {
            return null;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public Object get(long timeout, TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
            return null;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public long getDelay(TimeUnit unit) {
            return 0;
        }
    };

    /** The log writer. */
    private static final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(2, run -> {
        Thread thread = new Thread(run);
        thread.setName("Market Log Writer");
        thread.setDaemon(true);
        return thread;
    });

    /** The file data format */
    private static final DateTimeFormatter fileName = DateTimeFormatter.ofPattern("yyyyMMdd");

    /** The file name pattern. */
    private static final Pattern Name = Pattern.compile("\\D.(\\d.)\\.log");

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

    /** The active cache. */
    private Cache cache;

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
        this.cache = new Cache(ZonedDateTime.of(2000, 1, 1, 0, 0, 0, 0, Chrono.UTC));

        try {
            ZonedDateTime start = null;
            ZonedDateTime end = null;

            for (Path file : Filer.walk(root, "execution*.log").toList()) {
                String name = file.getFileName().toString();
                ZonedDateTime date = LocalDate.parse(name.substring(9, 17), fileName).atTime(0, 0, 0, 0).atZone(Chrono.UTC);
                System.out.println(date);
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
    public final synchronized Signal<Execution> from(ZonedDateTime start) {
        return new Signal<Execution>((observer, disposer) -> {
            // read from cache
            if (cacheFirst != null) {
                ZonedDateTime current = start.isBefore(cacheFirst) ? cacheFirst : start.isAfter(cacheLast) ? cacheLast : start;
                current = current.withHour(0).withMinute(0).withSecond(0).withNano(0);

                while (disposer.isDisposed() == false && !current.isAfter(cacheLast)) {
                    disposer.add(new Cache(current).read()
                            .effect(e -> latestId = cacheId = e.id)
                            .take(e -> e.exec_date.isAfter(start))
                            .to(observer::accept));
                    current = current.plusDays(1);
                }
            }

            AtomicBoolean completeREST = new AtomicBoolean();

            // read from realtime API
            if (disposer.isNotDisposed()) {
                disposer.add(service.executionsEternally().effect(e -> {
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

    private Signal<Execution> rest(AtomicLong realtimeLatest) {
        return new Signal<>((observer, disposer) -> {
            long start = 0;
            long end = service.exectutionLatest().id;

            LinkedList<Execution> executions = service.executions(start, end).retry().to(LinkedList.class);
            Iterator<Execution> iterator = executions.descendingIterator();
            while (iterator.hasNext()) {
                Execution execution = iterator.next();

                observer.accept(execution);
            }

            return disposer;
        });
    }

    /**
     * Read data from REST API.
     */
    private Signal<Execution> rest(long id) {
        return new Signal<>((observer, disposer) -> {
            long offset = 1;
            latestId = id;

            while (disposer.isNotDisposed()) {
                try {
                    List<Execution> executions = service.executions(latestId, latestId + service.executionMaxAcquirableSize() * offset)
                            .retry(5)
                            .toList();

                    // skip if there is no new execution
                    if (executions.isEmpty()) {
                        offset++;
                        continue;
                    }

                    offset = 1;

                    for (Execution exe : executions) {
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
            }
            observer.complete();

            return disposer;
        });
    }

    /**
     * Store the {@link Execution} to local cache.
     * 
     * @param e An {@link Execution} to store.
     */
    private void cache(Execution e) {
        if (cacheId < e.id) {
            cacheId = e.id;

            if (!cache.date.isEqual(e.exec_date.toLocalDate())) {
                cache.disableAutoSave();
                cache = new Cache(e.exec_date).enableAutoSave();
            }
            cache.queue.add(e);
        }
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
        return new Cache(date).read();
    }

    /**
     * Locate execution log.
     * 
     * @param date A date time.
     * @return A file location.
     */
    Path locateLog(TemporalAccessor date) {
        return root.resolve("execution" + Chrono.DateCompact.format(date) + ".log");
    }

    /**
     * Locate compressed execution log.
     * 
     * @param date A date time.
     * @return A file location.
     */
    Path locateCompactLog(TemporalAccessor date) {
        return root.resolve("compact" + Chrono.DateCompact.format(date) + ".log");
    }

    /**
     * Parse date from file name.
     * 
     * @param file
     * @return
     */
    private ZonedDateTime parse(Path file) {
        String name = file.getFileName().toString();
        Matcher matcher = Name.matcher(name);

        if (matcher.matches()) {
            return ZonedDateTime.of(LocalDateTime.parse(matcher.group(1)), Chrono.UTC);
        } else {
            throw new Error("Illegal file name [" + name + "]");
        }
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
     * @param limit
     * @return
     */
    public final Signal<Execution> rangeAll() {
        return range(cacheFirst, cacheLast);
    }

    /**
     * Read log from the specified start to end.
     * 
     * @param start
     * @param limit
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
     * Compact log at the specified date.
     * 
     * @param date A target date.
     */
    void compact(ZonedDateTime date) {

    }

    /**
     * Write compact execution log for test.
     *
     * @param date A target date.
     * @param executions A execution log.
     */
    void writeCompactLog(ZonedDateTime date, Signal<Execution> executions) {
        new Cache(date).compact(executions).to(e -> {
            // do nothing
        });
    }

    /**
     * @version 2018/05/27 10:31:20
     */
    private class Cache {

        /** The end date */
        private final LocalDate date;

        /** The log file. */
        private final Path log;

        /** The compact log file. */
        private final Path compact;

        /** The log writing task. */
        private ScheduledFuture task = NOOP;

        /** The writing execution queue. */
        private LinkedList<Execution> queue = new LinkedList();

        /** The latest cached id. */
        private long latest;

        /**
         * @param date
         */
        private Cache(ZonedDateTime date) {
            this.date = date.toLocalDate();
            this.log = locateLog(date);
            this.compact = locateCompactLog(date);
        }

        /**
         * Start writing log automatically.
         * 
         * @return Chainable API
         */
        private Cache enableAutoSave() {
            if (task == NOOP) {
                task = scheduler.scheduleWithFixedDelay(this::write, 30, 30, TimeUnit.SECONDS);
            }
            return this;
        }

        /**
         * Stop writing log automatically.
         * 
         * @return
         */
        private Cache disableAutoSave() {
            if (task != NOOP) {
                task.cancel(false);
                task = NOOP;
                write();
            }
            return this;
        }

        /**
         * Read cached date.
         * 
         * @return
         */
        private Signal<Execution> read() {
            try {
                CsvParserSettings setting = new CsvParserSettings();
                setting.getFormat().setDelimiter(' ');
                CsvParser parser = new CsvParser(setting);
                if (Files.exists(compact)) {
                    // read compact
                    return I.signal(parser.iterate(new ZstdInputStream(newInputStream(compact)), ISO_8859_1))
                            .scanWith(Execution.BASE, service::decode)
                            .effectOnComplete(parser::stopParsing);
                } else if (Files.notExists(log)) {
                    // no data
                    return download();
                } else {
                    // read normal
                    Signal<Execution> signal = I.signal(parser.iterate(newInputStream(log), ISO_8859_1))
                            .map(Execution::new)
                            .effectOnComplete(parser::stopParsing);

                    // make log compact coinstantaneously
                    LocalDate nextDay = date.plusDays(1);
                    if (Files.exists(locateCompactLog(nextDay)) || Files.exists(locateLog(nextDay))) {
                        signal = compact(signal);
                    }

                    return signal;
                }
            } catch (IOException e) {
                throw I.quiet(e);
            }
        }

        /**
         * Write compact log from the specified executions.
         */
        private Signal<Execution> compact(Signal<Execution> executions) {
            try {
                CsvWriterSettings setting = new CsvWriterSettings();
                setting.getFormat().setDelimiter(' ');
                CsvWriter writer = new CsvWriter(new ZstdOutputStream(newOutputStream(compact), 1), ISO_8859_1, setting);

                return executions.map(Execution.BASE, (prev, e) -> {
                    writer.writeRow(service.encode(prev, e));
                    return e;
                }).effectOnComplete(writer::close);
            } catch (IOException e) {
                throw I.quiet(e);
            }
        }

        /**
         * Try to download from market server.
         * 
         * @return
         */
        private Signal<Execution> download() {
            System.out.println(cacheFirst);
            return Signal.EMPTY;
        }

        /**
         * Write all queued executions to log file.
         */
        private void write() {
            if (queue.isEmpty()) {
                return;
            }

            // switch buffer
            LinkedList<Execution> remaining = queue;
            queue = new LinkedList();

            // build text
            StringBuilder text = new StringBuilder();

            for (Execution e : remaining) {
                text.append(e).append("\r\n");
            }

            // write to file
            try (FileChannel channel = FileChannel.open(log, CREATE, APPEND)) {
                channel.write(ByteBuffer.wrap(text.toString().getBytes(ISO_8859_1)));
            } catch (IOException e) {
                throw I.quiet(e);
            }
        }

    }

    /**
     * @param args
     */
    public static void main2(String[] args) {
        System.out.println(Num.of(10));
        MarketLog log = new MarketLog(BitFlyerService.FX_BTC_JPY);

        log.at(2016, 5, 21).to(e -> {
            System.out.println(e);
        });

        // Filer.walk(log.root, "execution*.log").to(file -> {
        //
        // });
    }
}
