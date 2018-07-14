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
import java.util.ArrayDeque;
import java.util.LinkedList;
import java.util.Objects;
import java.util.concurrent.Delayed;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.github.luben.zstd.ZstdInputStream;
import com.github.luben.zstd.ZstdOutputStream;
import com.google.common.base.Stopwatch;
import com.univocity.parsers.csv.CsvParser;
import com.univocity.parsers.csv.CsvParserSettings;
import com.univocity.parsers.csv.CsvWriter;
import com.univocity.parsers.csv.CsvWriterSettings;

import cointoss.market.bitflyer.BitFlyerService;
import cointoss.util.Chrono;
import cointoss.util.Span;
import filer.Filer;
import kiss.I;
import kiss.Signal;

/**
 * Log Manager.
 * 
 * @version 2018/07/12 15:50:16
 */
public class MarketLog {

    /** The logging system. */
    private static final Logger log = LogManager.getLogger(MarketLog.class);

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

    /** The active cache. */
    private Cache cache;

    /** The latest cached id. */
    private long cacheId;

    /** Realtime execution observer, this value will be switched. */
    private Consumer<Execution> realtime;

    /** For info. */
    private Stopwatch stopwatch = Stopwatch.createUnstarted();

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

        try {
            ZonedDateTime start = null;
            ZonedDateTime end = null;

            for (Path file : Filer.walk(root, "execution*.*og").toList()) {
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
            this.cacheFirst = start != null ? start : Chrono.utcNow().truncatedTo(ChronoUnit.DAYS);
            this.cacheLast = end != null ? end : start;
            this.cache = new Cache(cacheFirst);
        } catch (Exception e) {
            throw I.quiet(e);
        }
    }

    /**
     * Read date from the specified date.
     * 
     * @param start The start date.
     * @return
     */
    public final synchronized Signal<Execution> from(ZonedDateTime start) {
        ZonedDateTime startDay = Chrono.between(cacheFirst, start, cacheLast).truncatedTo(ChronoUnit.DAYS);

        return I.signal(startDay, day -> day.plusDays(1))
                .takeWhile(day -> day.isBefore(cacheLast) || day.isEqual(cacheLast))
                .flatMap(day -> new Cache(day).read())
                .effect(e -> cacheId = e.id)
                .take(e -> e.isAfter(start))
                .concat(network().effect(this::cache));
    }

    /**
     * Read date from merket server.
     * 
     * @return
     */
    private Signal<Execution> network() {
        return new Signal<Execution>((observer, disposer) -> {
            AtomicReference<Execution> observedLatest = new AtomicReference(service.exectutionLatest());
            realtime = observedLatest::set;

            // read from realtime API
            disposer.add(service.executionsRealtimely().to(e -> {
                realtime.accept(e); // don't use method reference
            }, observer::error, observer::complete));

            // read from REST API
            int size = service.executionMaxAcquirableSize();
            long start = cacheId;

            while (disposer.isNotDisposed()) {
                ArrayDeque<Execution> executions = service.executions(start, start + size).retry().toCollection(new ArrayDeque(size));

                if (executions.isEmpty() == false) {
                    log.info("REST write from {}.  size {}", executions.getFirst().date, executions.size());
                    executions.forEach(observer);
                    start = executions.getLast().id;
                } else {
                    if (start < observedLatest.get().id) {
                        // Although there is no data in the current search range,
                        // since it has not yet reached the latest execution,
                        // shift the range backward and search again.
                        start += size;
                        continue;
                    } else {
                        // Because the REST API has caught up with the real-time API,
                        // it stops the REST API.
                        realtime = observer::accept;
                        log.info("Switch to Realtime API.");
                        break;
                    }
                }
            }
            return disposer;
        }).repeatWhen(s -> s.delay(10, TimeUnit.SECONDS));
    }

    /**
     * Store the {@link Execution} to local cache.
     * 
     * @param e An {@link Execution} to store.
     */
    private void cache(Execution e) {
        if (cacheId < e.id) {
            cacheId = e.id;

            if (!cache.date.isEqual(e.date.toLocalDate())) {
                cache.disableAutoSave();
                cache.compact();
                cache = new Cache(e.date).enableAutoSave();
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
            return from(start).takeWhile(e -> e.date.isBefore(end));
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
     * Read all caches.
     * 
     * @return
     */
    final Signal<Cache> caches() {
        return I.signal(cacheFirst, day -> day.plusDays(1))
                .takeWhile(day -> day.isBefore(cacheLast) || day.isEqual(cacheLast))
                .map(Cache::new);
    }

    /**
     * Create the specified date cache for TEST.
     * 
     * @param date
     */
    final Cache cache(ZonedDateTime date) {
        return new Cache(date);
    }

    /**
     * Locate execution log.
     * 
     * @param date A date time.
     * @return A file location.
     */
    final Path locateLog(TemporalAccessor date) {
        return root.resolve("execution" + Chrono.DateCompact.format(date) + ".log");
    }

    /**
     * Locate compressed execution log.
     * 
     * @param date A date time.
     * @return A file location.
     */
    final Path locateCompactLog(TemporalAccessor date) {
        return root.resolve("execution" + Chrono.DateCompact.format(date) + ".clog");
    }

    /**
     * @version 2018/05/27 10:31:20
     */
    class Cache {

        /** The end date */
        private final LocalDate date;

        /** The log file. */
        private final Path normal;

        /** The compact log file. */
        private final Path compact;

        /** The log writing task. */
        private ScheduledFuture task = NOOP;

        /** The writing execution queue. */
        private LinkedList<Execution> queue = new LinkedList();

        /**
         * @param date
         */
        private Cache(ZonedDateTime date) {
            this.date = date.toLocalDate();
            this.normal = locateLog(date);
            this.compact = locateCompactLog(date);
        }

        /**
         * Start writing log automatically.
         * 
         * @return Chainable API
         */
        private Cache enableAutoSave() {
            if (task == NOOP) {
                task = scheduler.scheduleWithFixedDelay(this::write, 20, 90, TimeUnit.SECONDS);
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
         * Check whether this cache is completed or not.
         * 
         * @return
         */
        private boolean isCompleted() {
            LocalDate nextDay = date.plusDays(1);
            return Files.exists(locateCompactLog(nextDay)) || Files.exists(locateLog(nextDay));
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
                            .effectOnComplete(parser::stopParsing)
                            .effectOnObserve(() -> stopwatch.reset().start())
                            .effectOnComplete(() -> {
                                log.info("Read compact log [{}] {}", date, stopwatch.stop().elapsed());
                            });
                } else if (Files.notExists(normal)) {
                    // no data
                    return download();
                } else {
                    // read normal
                    Signal<Execution> signal = I.signal(parser.iterate(newInputStream(normal), ISO_8859_1))
                            .map(Execution::new)
                            .effectOnComplete(parser::stopParsing)
                            .effectOnObserve(() -> stopwatch.reset().start())
                            .effectOnComplete(() -> {
                                log.info("Read log [{}] {}", date, stopwatch.stop().elapsed());
                            });

                    // make log compact coinstantaneously
                    if (isCompleted()) {
                        signal = compact(signal);
                    }
                    return signal;
                }
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

            // write normal log
            try (FileChannel channel = FileChannel.open(normal, CREATE, APPEND)) {
                channel.write(ByteBuffer.wrap(text.toString().getBytes(ISO_8859_1)));

                log.info("Write log until " + remaining.peekLast().date + " at " + service + ".");
            } catch (IOException e) {
                throw I.quiet(e);
            }
        }

        /**
         * Convert normal log to compact log asynchronously.
         */
        private void compact() {
            if (Files.notExists(compact)) {
                I.schedule(10, TimeUnit.SECONDS, true, () -> {
                    compact(read()).effectOnComplete(() -> Filer.delete(normal)).to(I.NoOP);
                });
            }
        }

        /**
         * Write compact log from the specified executions.
         */
        Signal<Execution> compact(Signal<Execution> executions) {
            try {
                Files.createDirectories(compact.getParent());

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
    }

    public static void main(String[] args) {
        Market market = new Market(BitFlyerService.FX_BTC_JPY);
        market.readLog(log -> log.caches().skip(255).take(6).concatMap(c -> c.read()));

        market.dispose();
    }
}
