/*
 * Copyright (C) 2019 CoinToss Development Team
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          https://opensource.org/licenses/MIT
 */
package cointoss;

import static java.nio.charset.StandardCharsets.*;
import static java.nio.file.StandardOpenOption.*;
import static java.util.concurrent.TimeUnit.*;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.time.Duration;
import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalAccessor;
import java.util.ArrayDeque;
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
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.github.luben.zstd.ZstdInputStream;
import com.github.luben.zstd.ZstdOutputStream;
import com.google.common.base.Stopwatch;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.univocity.parsers.csv.CsvParser;
import com.univocity.parsers.csv.CsvParserSettings;
import com.univocity.parsers.csv.CsvWriter;
import com.univocity.parsers.csv.CsvWriterSettings;

import cointoss.market.bitflyer.BitFlyer;
import cointoss.util.Chrono;
import cointoss.util.Network;
import cointoss.util.Num;
import cointoss.util.RetryPolicy;
import cointoss.util.Span;
import kiss.I;
import kiss.Signal;
import psychopath.Directory;
import psychopath.File;
import psychopath.Locator;

/**
 * Log Manager.
 * 
 * @version 2018/08/03 8:34:01
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

    /** The market provider. */
    private final MarketService service;

    /** The root directory of logs. */
    private final Directory root;

    /** In-memory cache. */
    private final LoadingCache<ZonedDateTime, List<Execution>> memory = CacheBuilder.newBuilder()
            .maximumSize(25)
            .expireAfterAccess(15, MINUTES)
            .build(new CacheLoader<>() {

                /**
                 * {@inheritDoc}
                 */
                @Override
                public List<Execution> load(ZonedDateTime key) throws Exception {
                    return new Cache(key).read().toList();
                }
            });

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

    /** The retry policy. */
    private final RetryPolicy policy = new RetryPolicy().retryMaximum(100)
            .delayLinear(Duration.ofSeconds(1))
            .delayMaximum(Duration.ofMinutes(2));

    /**
     * Create log manager.
     * 
     * @param provider
     */
    public MarketLog(MarketService service) {
        this(service, Locator.directory(".log").directory(service.exchangeName).directory(service.marketName));
    }

    /**
     * Create log manager with the specified log store directory.
     * 
     * @param service A market service.
     * @param root A log store directory.
     */
    MarketLog(MarketService service, Directory root) {
        this.service = Objects.requireNonNull(service);
        this.root = Objects.requireNonNull(root);

        try {
            ZonedDateTime start = null;
            ZonedDateTime end = null;

            for (File file : root.walkFile("execution*.*og").toList()) {
                String name = file.name();
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
            this.cacheLast = end != null ? end : cacheFirst;
            this.cache = new Cache(cacheFirst);
        } catch (Exception e) {
            throw I.quiet(e);
        }
    }

    /**
     * Return the first cached date.
     * 
     * @return
     */
    public final ZonedDateTime firstCacheDate() {
        return cacheFirst;
    }

    /**
     * Return the last cached date.
     * 
     * @return
     */
    public final ZonedDateTime lastCacheDate() {
        return cacheLast;
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
            AtomicReference<Execution> observedLatest = new AtomicReference<>();
            disposer.add(service.executionLatest().to(observedLatest::set));

            realtime = observedLatest::set;

            // read from realtime API
            disposer.add(service.executionsRealtimely().to(e -> {
                realtime.accept(e); // don't use method reference
            }, observer::error));

            // read from REST API
            int size = service.executionMaxAcquirableSize();
            long start = cacheId != 0 ? cacheId : service.estimateInitialExecutionId();
            Num coefficient = Num.ONE;

            while (disposer.isNotDisposed()) {
                ArrayDeque<Execution> executions = service.executions(start, start + coefficient.multiply(size).toInt())
                        .retry()
                        .toCollection(new ArrayDeque(size));

                int retrieved = executions.size();

                if (retrieved != 0) {
                    if (size <= retrieved) {
                        // Since there are too many data acquired, narrow the data range and get it
                        // again.
                        coefficient = Num.max(Num.ONE, coefficient.minus(1));
                        continue;
                    } else {
                        log.info("REST write from {}.  size {} ({})", executions.getFirst().date, executions.size(), coefficient);
                        executions.forEach(observer);
                        start = executions.getLast().id;

                        // Since the number of acquired data is too small, expand the data range
                        // slightly from next time.
                        if (retrieved < size * 0.1) {
                            coefficient = coefficient.plus("0.1");
                        }
                    }
                } else {
                    if (start < observedLatest.get().id) {
                        // Although there is no data in the current search range,
                        // since it has not yet reached the latest execution,
                        // shift the range backward and search again.
                        start += coefficient.multiply(size).toInt() - 1;
                        coefficient = coefficient.plus("0.1");
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
            policy.reset();
            return disposer;
        }).retryWhen(policy);
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
        Stopwatch stopwatch = Stopwatch.createUnstarted();

        try {
            return I.signal(memory.get(date)).effectOnObserve(() -> stopwatch.reset().start()).effectOnTerminate(() -> {
                log.info("Process executions [{}] {}", date, stopwatch.stop().elapsed());
            });
        } catch (ExecutionException e) {
            throw I.quiet(e);
        }
    }

    /**
     * Read log from the specified date.
     * 
     * @param init
     * @return
     */
    public final Signal<Execution> fromToday() {
        return fromLast(0);
    }

    /**
     * Read log from the specified date.
     * 
     * @param init
     * @return
     */
    public final Signal<Execution> fromYestaday() {
        return fromLast(1);
    }

    /**
     * Read log from the specified date.
     * 
     * @param init
     * @return
     */
    public final Signal<Execution> fromLast(int days) {
        return from(Chrono.utcNow().minusDays(days));
    }

    /**
     * Read log from the specified start to end.
     * 
     * @param init
     * @param limit
     * @return
     */
    public final Signal<Execution> rangeAll() {
        return range(cacheFirst, cacheLast);
    }

    /**
     * Read log from the specified start to end.
     * 
     * @param init
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
        return I.signal(start, day -> day.plusDays(1)).takeUntil(day -> day.isEqual(end)).flatMap(day -> at(day));
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
    final File locateLog(TemporalAccessor date) {
        return root.file("execution" + Chrono.DateCompact.format(date) + ".log");
    }

    /**
     * Locate compressed execution log.
     * 
     * @param date A date time.
     * @return A file location.
     */
    final File locateCompactLog(TemporalAccessor date) {
        return root.file("execution" + Chrono.DateCompact.format(date) + ".clog");
    }

    /**
     * @version 2018/05/27 10:31:20
     */
    class Cache {

        /** The end date */
        private final LocalDate date;

        /** The log file. */
        private final File normal;

        /** The compact log file. */
        private final File compact;

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
         * Read cached date.
         * 
         * @return
         */
        private Signal<Execution> read() {
            Stopwatch stopwatch = Stopwatch.createUnstarted();

            try {
                CsvParserSettings setting = new CsvParserSettings();
                setting.getFormat().setDelimiter(' ');
                setting.getFormat().setComment('無');

                CsvParser parser = new CsvParser(setting);
                if (compact.isPresent()) {
                    // read compact
                    return I.signal(parser.iterate(new ZstdInputStream(compact.newInputStream()), ISO_8859_1))
                            .scanWith(Execution.BASE, service::decode)
                            .effectOnComplete(parser::stopParsing)
                            .effectOnObserve(stopwatch::start)
                            .effectOnComplete(() -> {
                                log.info("Read compact log [{}] {}", date, stopwatch.stop().elapsed());
                            });
                } else if (normal.isAbsent()) {
                    // no data
                    return download();
                } else {
                    // read normal
                    Signal<Execution> signal = I.signal(parser.iterate(normal.newInputStream(), ISO_8859_1))
                            .map(Execution::new)
                            .effectOnComplete(parser::stopParsing)
                            .effectOnObserve(stopwatch::start)
                            .effectOnComplete(() -> {
                                log.info("Read log [{}] {}", date, stopwatch.stop().elapsed());
                            });
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
            return Signal.empty();
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
            try (FileChannel channel = FileChannel.open(normal.asJavaPath(), CREATE, APPEND)) {
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
            if (compact.isAbsent() && (!queue.isEmpty() || normal.isPresent())) {
                I.schedule(5, SECONDS, I.parallel, () -> {
                    compact(read()).effectOnComplete(() -> normal.delete()).to(I.NoOP);
                });
            }
        }

        /**
         * Write compact log from the specified executions.
         */
        Signal<Execution> compact(Signal<Execution> executions) {
            try {
                compact.parent().create();

                CsvWriterSettings setting = new CsvWriterSettings();
                setting.getFormat().setDelimiter(' ');
                setting.getFormat().setComment('無');
                CsvWriter writer = new CsvWriter(new ZstdOutputStream(compact.newOutputStream(), 1), ISO_8859_1, setting);

                return executions.maps(Execution.BASE, (prev, e) -> {
                    writer.writeRow(service.encode(prev, e));
                    return e;
                }).effectOnComplete(writer::close);
            } catch (IOException e) {
                throw I.quiet(e);
            }
        }
    }

    public static void main(String[] args) {
        LocalDate start = LocalDate.of(2018, 2, 1);
        LocalDate end = LocalDate.of(2018, 2, 5);

        VerifiableMarket market = new VerifiableMarket(BitFlyer.FX_BTC_JPY);

        Signal<LocalDate> range = I.signal(start, day -> day.plusDays(1)).takeUntil(day -> day.isEqual(end));

        market.readLog(log -> range.flatMap(day -> log.at(day)));
        market.readLog(log -> range.flatMap(day -> log.at(day)));

        market.dispose();
        Network.terminate();
    }
}
