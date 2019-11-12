/*
 * Copyright (C) 2019 CoinToss Development Team
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          https://opensource.org/licenses/MIT
 */
package cointoss.execution;

import static java.nio.charset.StandardCharsets.ISO_8859_1;
import static java.nio.file.StandardOpenOption.*;
import static java.util.concurrent.TimeUnit.*;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalAccessor;
import java.util.ArrayDeque;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.Delayed;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Consumer;
import java.util.function.Function;

import org.apache.commons.lang3.RandomUtils;
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

import cointoss.Direction;
import cointoss.Market;
import cointoss.MarketService;
import cointoss.market.bitflyer.BitFlyer;
import cointoss.market.bitmex.BitMex;
import cointoss.util.Chrono;
import cointoss.util.Network;
import cointoss.util.Num;
import cointoss.util.Retry;
import kiss.I;
import kiss.Observer;
import kiss.Signal;
import psychopath.Directory;
import psychopath.File;
import psychopath.Locator;

/**
 * {@link Execution} Log Manager.
 */
public class ExecutionLog {

    /** The logging system. */
    private static final Logger log = LogManager.getLogger(ExecutionLog.class);

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
            .maximumSize(3)
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

    /** The log parser. */
    private final ExecutionLogger logger;

    /** The retry policy. */
    private final Retry policy = Retry.with.limit(100).delayLinear(Duration.ofSeconds(1)).delayMaximum(Duration.ofMinutes(2));

    /**
     * Create log manager.
     * 
     * @param provider
     */
    public ExecutionLog(MarketService service) {
        this(service, Locator.directory(".log").directory(service.exchangeName).directory(service.marketName));
    }

    /**
     * Create log manager with the specified log store directory.
     * 
     * @param service A market service.
     * @param root A log store directory.
     */
    ExecutionLog(MarketService service, Directory root) {
        this.service = Objects.requireNonNull(service);
        this.root = Objects.requireNonNull(root);
        this.logger = I.make(service.setting.executionLogger());

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

        return I.signal(startDay)
                .recurse(day -> day.plusDays(1))
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
            BufferFromRestToRealtime buffer = new BufferFromRestToRealtime(service.setting.executionWithSequentialId, observer::error);

            // read from realtime API
            disposer.add(service.executionsRealtimely().to(buffer));

            // read from REST API
            int size = service.setting.acquirableExecutionSize();
            long startId = cacheId != 0 ? cacheId : service.estimateInitialExecutionId();
            Num coefficient = Num.ONE;

            while (disposer.isNotDisposed()) {
                ArrayDeque<Execution> rests = service.executions(startId, startId + coefficient.multiply(size).toLong())
                        .retry()
                        .toCollection(new ArrayDeque(size));

                int retrieved = rests.size();

                if (retrieved != 0) {
                    // REST API returns some executions
                    if (size < retrieved) {
                        // Since there are too many data acquired,
                        // narrow the data range and get it again.
                        coefficient = Num.max(Num.ONE, coefficient.minus(1));
                        continue;
                    } else {
                        log.info("REST write from {}.  size {} ({})", rests.getFirst().date, rests.size(), coefficient);

                        for (Execution execution : rests) {
                            if (!buffer.canSwitch(execution)) {
                                observer.accept(execution);
                            } else {
                                // REST API has caught up with the real-time API,
                                // we must switch to realtime API.
                                buffer.switchToRealtime(execution.id, observer);
                                return disposer;
                            }
                        }
                        startId = rests.peekLast().id;

                        // The number of acquired data is too small,
                        // expand the data range slightly from next time.
                        if (retrieved < size * 0.1) {
                            coefficient = coefficient.plus("0.1");
                        }
                    }
                } else {
                    // REST API returns empty execution
                    if (buffer.realtime.isEmpty() || startId < buffer.realtime.peek().id) {
                        // Although there is no data in the current search range,
                        // since it has not yet reached the latest execution,
                        // shift the range backward and search again.
                        startId += coefficient.multiply(size).toInt() - 1;
                        coefficient = coefficient.plus("0.1");
                        continue;
                    } else {
                        // REST API has caught up with the real-time API,
                        // we must switch to realtime API.
                        buffer.switchToRealtime(startId, observer);
                        break;
                    }
                }
            }
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
     * @param start
     * @param end
     * @return
     */
    public final Signal<Execution> range(ZonedDateTime start, ZonedDateTime end) {
        return I.signal(start).recurse(day -> day.plusDays(1)).takeUntil(day -> day.isEqual(end)).flatMap(day -> at(day));
    }

    /**
     * Read log from the specified start to end.
     * 
     * @param days
     * @return
     */
    public final Signal<Execution> rangeRandom(int days) {
        long range = ChronoUnit.DAYS.between(cacheFirst, cacheLast.minusDays(days + 1));
        long offset = RandomUtils.nextLong(0, range);
        return range(cacheFirst.plusDays(offset), cacheFirst.plusDays(offset + days));
    }

    /**
     * Read all caches.
     * 
     * @return
     */
    final Signal<Cache> caches() {
        return I.signal(cacheFirst)
                .recurse(day -> day.plusDays(1))
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
         * Check whether the cache file exist or not.
         * 
         * @return
         */
        private boolean exist() {
            return normal.isPresent() || compact.isPresent();
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
                            .scanWith(Market.BASE, logger::decode)
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
                            .map(this::parse)
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
         * Parse execution data.
         * 
         * @param values
         * @return
         */
        private Execution parse(String[] values) {
            return Execution.with.direction(Direction.parse(values[2]), Num.of(values[4]))
                    .price(Num.of(values[3]))
                    .id(Long.parseLong(values[0]))
                    .date(LocalDateTime.parse(values[1]).atZone(Chrono.UTC))
                    .consecutive(Integer.parseInt(values[5]))
                    .delay(Integer.parseInt(values[6]));
        }

        /**
         * Try to download from market server.
         * 
         * @return
         */
        private Signal<Execution> download() {
            return I.signal();
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
            try (FileChannel channel = FileChannel.open(normal.create().asJavaPath(), CREATE, APPEND)) {
                channel.write(ByteBuffer.wrap(text.toString().getBytes(ISO_8859_1)));

                log.info("Write log until " + remaining.peekLast().date + " at " + service + ".");
            } catch (IOException e) {
                e.printStackTrace();
                throw I.quiet(e);
            }
        }

        /**
         * Convert normal log to compact log asynchronously.
         */
        private void compact() {
            if (compact.isAbsent() && (!queue.isEmpty() || normal.isPresent())) {
                I.schedule(5, SECONDS, () -> {
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

                return executions.maps(Market.BASE, (prev, e) -> {
                    writer.writeRow(logger.encode(prev, e));
                    return e;
                }).effectOnComplete(writer::close);
            } catch (IOException e) {
                throw I.quiet(e);
            }
        }
    }

    /**
     * 
     */
    private class BufferFromRestToRealtime implements Observer<Execution> {

        /** The flag whether execution record holds the sequential id or not. */
        private final boolean sequntial;

        /** The upper error handler. */
        private final Consumer<? super Throwable> error;

        /** The locally managed sequential id. */
        private final AtomicLong id = new AtomicLong();

        /** The id rewriter. (default is not rewritable) */
        private Function<Execution, Execution> rewriter = Function.identity();

        /** The actual realtime execution buffer. */
        private ConcurrentLinkedDeque<Execution> realtime = new ConcurrentLinkedDeque();

        /** The execution event receiver. */
        private Observer<? super Execution> destination = realtime::add;

        /**
         * Build {@link BufferFromRestToRealtime}.
         * 
         * @param sequntial
         */
        private BufferFromRestToRealtime(boolean sequntial, Consumer<? super Throwable> error) {
            this.sequntial = sequntial;
            this.error = error;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public void accept(Execution e) {
            destination.accept(rewriter.apply(e));
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public void error(Throwable e) {
            error.accept(e);
        }

        /**
         * Test whether the specified execution is queued in realtime buffer or not.
         * 
         * @param e An execution which is retrieved by REST API.
         * @return
         */
        private boolean canSwitch(Execution e) {
            Execution first = realtime.peekFirst();

            // realtime buffer is empty
            if (first == null) {
                return false;
            }
            return sequntial ? e.id == first.id : e.buyer.equals(first.buyer);
        }

        /**
         * Switch to realtime API.
         * 
         * @param currentId
         * @param observer
         */
        private void switchToRealtime(long currentId, Observer<? super Execution> observer) {
            log.info(service.marketIdentity() + " switch to Realtime API.");

            if (!sequntial) {
                id.set(currentId);
                rewriter = e -> e.assignId(id.getAndIncrement());
            }

            while (!realtime.isEmpty()) {
                ConcurrentLinkedDeque<Execution> buffer = realtime;
                realtime = new ConcurrentLinkedDeque();
                for (Execution e : buffer) {
                    observer.accept(rewriter.apply(e));
                }
                log.info("Realtime buffer write from {} to {}.  size {}", buffer.peek().date, buffer.peekLast().date, buffer.size());
            }
            destination = observer;
            policy.reset();
        }
    }

    /**
     * Restore normal log of the specified market and date.
     * 
     * @param service
     * @param date
     */
    public static void restoreNormalLog(MarketService service, ZonedDateTime date) {
        ExecutionLog log = new ExecutionLog(service);
        Cache cache = log.cache(date);
        cache.read().to(cache.queue::add);
        cache.write();
    }

    public static void main(String[] args) {
        restoreNormalLog(BitFlyer.FX_BTC_JPY, Chrono.utc(2019, 11, 8));
    }

    public static void main2(String[] args) {
        Network.proxy("54.39.53.104", 3128);

        ExecutionLog log = new ExecutionLog(BitMex.XBT_USD);
        log.fetch(115364009, Chrono.utc(2018, 9, 1), Chrono.utc(2018, 12, 31));

        Network.terminate();
    }

    public static void main7(String[] args) {
        Network.proxy("178.128.231.246", 3128);

        ExecutionLog log = new ExecutionLog(BitMex.XBT_USD);
        log.fetch(0, Chrono.utc(2018, 1, 1), Chrono.utc(2019, 7, 31));

        Network.terminate();
    }

    /**
     * Helper to collect log from the specified starting point. <br>
     * 201908 - 308219999<br>
     * 201907 - 276877099<br>
     * 201906 - 246639999<br>
     * 201904 - 204569409<br>
     * 
     * @param startId
     */
    private void fetch(long startId, ZonedDateTime startDay, ZonedDateTime endDay) {
        Cache latestCache = new Cache(startDay);

        while (latestCache.exist()) {
            startDay = startDay.plusDays(1);
            Cache nextCache = new Cache(startDay);

            if (!nextCache.exist()) {
                startId = latestCache.read().last().to().v.id;
                break;
            } else {
                latestCache = nextCache;
            }
        }

        // read from REST API
        int size = service.setting.acquirableExecutionSize();
        Num coefficient = Num.ONE;

        while (true) {
            ArrayDeque<Execution> executions = service.executions(startId, startId + coefficient.multiply(size).toLong())
                    .effectOnError(Throwable::printStackTrace)
                    .retry()
                    .toCollection(new ArrayDeque(size));

            int retrieved = executions.size();

            if (retrieved != 0) {
                if (size < retrieved) {
                    // Since there are too many data acquired, narrow the data range and get it
                    // again.
                    coefficient = Num.max(Num.ONE, coefficient.minus(1));
                    continue;
                } else {
                    log.info("REST write from {}.  size {} ({})", executions.getFirst().date, executions.size(), coefficient);
                    executions.forEach(this::cache);
                    startId = executions.getLast().id;

                    // Since the number of acquired data is too small, expand the data range
                    // slightly from next time.
                    if (retrieved < size * 0.1) {
                        coefficient = coefficient.plus("0.1");
                    }
                }
            } else {
                break;
            }
        }
    }
}
