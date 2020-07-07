/*
 * Copyright (C) 2020 cointoss Development Team
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
import static java.util.concurrent.TimeUnit.SECONDS;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.nio.channels.OverlappingFileLockException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalAccessor;
import java.util.ArrayDeque;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Objects;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.Delayed;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.function.Consumer;

import org.apache.commons.lang3.RandomUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.github.luben.zstd.ZstdInputStream;
import com.github.luben.zstd.ZstdOutputStream;
import com.google.common.base.Stopwatch;
import com.univocity.parsers.csv.CsvParser;
import com.univocity.parsers.csv.CsvParserSettings;
import com.univocity.parsers.csv.CsvWriter;
import com.univocity.parsers.csv.CsvWriterSettings;

import cointoss.Direction;
import cointoss.Market;
import cointoss.MarketService;
import cointoss.market.MarketServiceProvider;
import cointoss.market.bitflyer.BitFlyer;
import cointoss.market.bitmex.BitMex;
import cointoss.ticker.Span;
import cointoss.ticker.Ticker;
import cointoss.ticker.TickerManager;
import cointoss.util.Chrono;
import cointoss.util.Network;
import cointoss.util.Num;
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
        thread.setName("ExecutionLog Writer");
        thread.setDaemon(true);
        return thread;
    });

    /** The file data format */
    private static final DateTimeFormatter fileName = DateTimeFormatter.ofPattern("yyyyMMdd");

    /** The market provider. */
    private final MarketService service;

    /** The root directory of logs. */
    private final Directory root;

    /** The latest id store in local file. */
    private final File store;

    /** The first day. */
    private ZonedDateTime cacheFirst;

    /** The last day. */
    private ZonedDateTime cacheLast;

    /** The active cache. */
    private Cache cache;

    /** The latest cached id. */
    private long cacheId;

    /** The latest stored id in local cache file. */
    private long storedId;

    /** The log parser. */
    private final ExecutionLogger logger;

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
        this.store = root.file("lastID.log");
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
     * Clear all fast log.
     */
    public final void clearFastCache() {
        root.delete("*.flog");
    }

    /**
     * Read date from the specified date.
     * 
     * @param start The start date.
     * @return
     */
    public final synchronized Signal<Execution> from(ZonedDateTime start, LogType... type) {
        ZonedDateTime startDay = Chrono.between(cacheFirst, start, cacheLast).truncatedTo(ChronoUnit.DAYS);

        return I.signal(startDay)
                .recurse(day -> day.plusDays(1))
                .takeWhile(day -> day.isBefore(cacheLast) || day.isEqual(cacheLast))
                .flatMap(day -> new Cache(day).read(type))
                .effect(e -> cacheId = e.id)
                .take(e -> e.isAfter(start))
                .effectOnComplete(() -> storedId = cacheId)
                .concat(network().effect(this::cache));
    }

    /**
     * Read date from merket server.
     * 
     * @return
     */
    private Signal<Execution> network() {
        return new Signal<Execution>((observer, disposer) -> {
            BufferFromRestToRealtime buffer = new BufferFromRestToRealtime(observer::error);

            // If you connect to the real-time API first, two errors may occur at the same time for
            // the real-time API and the REST API (because the real-time API is asynchronous). In
            // that case, there is a possibility that the retry operation may be hindered.
            // Therefore, the real-time API will connect after the connection of the REST API
            // is confirmed.
            // disposer.add(service.executionsRealtimely().to(buffer, observer::error));
            boolean activeRealtime = false;

            // read from REST API
            int size = service.setting.acquirableExecutionSize();
            long startId = cacheId != 0 ? cacheId : service.estimateInitialExecutionId();
            Num coefficient = Num.ONE;

            while (disposer.isNotDisposed()) {
                ArrayDeque<Execution> rests = new ArrayDeque(size);
                service.executions(startId, startId + coefficient.multiply(size).longValue()).to(rests::add, observer::error);

                // Since the synchronous REST API did not return an error, it can be determined that
                // the server is operating normally, so the real-time API is also connected.
                if (activeRealtime == false) {
                    activeRealtime = true;
                    disposer.add(service.executionsRealtimely().to(buffer, observer::error));
                }

                int retrieved = rests.size();

                if (retrieved != 0) {
                    // REST API returns some executions
                    if (size <= retrieved && coefficient.isGreaterThan(1)) {
                        // Since there are too many data acquired,
                        // narrow the data range and get it again.
                        coefficient = Num.max(Num.ONE, coefficient.minus(5));
                        continue;
                    } else {
                        log.info("REST write on " + service + " from {}.  size {} ({})", rests.getFirst().date, rests.size(), coefficient);

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
                            coefficient = coefficient.plus("3");
                        } else if (retrieved < size * 0.3) {
                            coefficient = coefficient.plus("0.5");
                        } else if (retrieved < size * 0.5) {
                            coefficient = coefficient.plus("0.3");
                        } else if (retrieved < size * 0.7) {
                            coefficient = coefficient.plus("0.1");
                        }
                    }
                } else {
                    // REST API returns empty execution
                    if (startId < buffer.realtimeFirstId()) {
                        // Although there is no data in the current search range,
                        // since it has not yet reached the latest execution,
                        // shift the range backward and search again.
                        startId += coefficient.multiply(size).intValue() - 1;
                        coefficient = coefficient.plus("0.1");
                        continue;
                    }

                    // REST API has caught up with the real-time API,
                    // we must switch to realtime API.
                    buffer.switchToRealtime(startId, observer);
                    break;
                }
            }
            return disposer;
        }).retryWhen(service.retryPolicy(500, "ExecutionLog"));
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
                cache.writeCompact();
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
    public final Signal<Execution> at(int year, int month, int day, LogType... type) {
        return at(LocalDate.of(year, month, day), type);
    }

    /**
     * Read log at the specified date.
     * 
     * @param date
     * @return
     */
    public final Signal<Execution> at(LocalDate date, LogType... type) {
        return at(date.atTime(0, 0).atZone(Chrono.UTC), type);
    }

    /**
     * Read log at the specified date.
     * 
     * @param date
     * @return
     */
    public final Signal<Execution> at(ZonedDateTime date, LogType... type) {
        return new Cache(date).read(type);
    }

    /**
     * Read log from the specified date.
     * 
     * @param init
     * @return
     */
    public final Signal<Execution> fromToday(LogType... type) {
        return fromLast(0, type);
    }

    /**
     * Read log from the specified date.
     * 
     * @param init
     * @return
     */
    public final Signal<Execution> fromYestaday(LogType... type) {
        return fromLast(1, type);
    }

    /**
     * Read log from the specified date.
     * 
     * @param init
     * @return
     */
    public final Signal<Execution> fromLast(int days, LogType... type) {
        return from(Chrono.utcNow().minusDays(days), type);
    }

    /**
     * Read log from the specified start to end.
     * 
     * @param init
     * @param limit
     * @return
     */
    public final Signal<Execution> rangeAll(LogType... type) {
        return range(cacheFirst, cacheLast, type);
    }

    /**
     * Read log from the specified start to end.
     * 
     * @param start
     * @param end
     * @return
     */
    public final Signal<Execution> range(ZonedDateTime start, ZonedDateTime end, LogType... type) {
        return I.signal(start).recurse(day -> day.plusDays(1)).takeUntil(day -> day.isEqual(end)).flatMap(day -> at(day, type));
    }

    /**
     * Read log from the specified start to end.
     * 
     * @param days
     * @return
     */
    public final Signal<Execution> rangeRandom(int days, LogType... type) {
        long range = ChronoUnit.DAYS.between(cacheFirst, cacheLast.minusDays(days + 1));
        long offset = RandomUtils.nextLong(0, range);
        return range(cacheFirst.plusDays(offset), cacheFirst.plusDays(offset + days), type);
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
     * Locate fast execution log.
     * 
     * @param date A date time.
     * @return A file location.
     */
    final File locateFastLog(TemporalAccessor date) {
        return root.file("execution" + Chrono.DateCompact.format(date) + ".flog");
    }

    /**
     * 
     */
    public enum LogType {
        Normal, Fast;
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

        /** The fast log file. */
        private final File fast;

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
            this.fast = locateFastLog(date);
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
                task = scheduler.scheduleWithFixedDelay(this::write, 30, 120, TimeUnit.SECONDS);
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
         * @param types
         * @return
         */
        private Signal<Execution> read(LogType... types) {
            LogType type = types == null || types.length == 0 ? LogType.Normal : types[0];
            Stopwatch stopwatch = Stopwatch.createUnstarted();

            try {
                CsvParserSettings setting = new CsvParserSettings();
                setting.getFormat().setDelimiter(' ');
                setting.getFormat().setComment('無');

                CsvParser parser = new CsvParser(setting);
                if (compact.isPresent()) {
                    if (type == LogType.Fast) {
                        // read fast
                        writeFast();
                        return I.signal(parser.iterate(new ZstdInputStream(fast.newInputStream()), ISO_8859_1))
                                .scanWith(Market.BASE, logger::decode)
                                .effectOnComplete(parser::stopParsing)
                                .effectOnObserve(stopwatch::start)
                                .effectOnComplete(() -> {
                                    log.info("Read fast log {} [{}] {}", service.marketIdentity(), date, stopwatch.stop().elapsed());
                                });
                    } else {
                        // read compact
                        return I.signal(parser.iterate(new ZstdInputStream(compact.newInputStream()), ISO_8859_1))
                                .scanWith(Market.BASE, logger::decode)
                                .effectOnComplete(parser::stopParsing)
                                .effectOnObserve(stopwatch::start)
                                .effectOnComplete(() -> {
                                    log.info("Read compact log {} [{}] {}", service.marketIdentity(), date, stopwatch.stop().elapsed());
                                });
                    }
                } else if (normal.isAbsent()) {
                    // no data
                    return download();
                } else {
                    // read normal
                    return I.signal(parser.iterate(normal.newInputStream(), ISO_8859_1))
                            .map(this::parse)
                            .effectOnComplete(parser::stopParsing)
                            .effectOnObserve(stopwatch::start)
                            .effectOnComplete(() -> {
                                log.info("Read normal log {} [{}] {}", service.marketIdentity(), date, stopwatch.stop().elapsed());
                            });
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

            root.lock().recover(OverlappingFileLockException.class, (FileLock) null).to(o -> {
                readStoredId();

                // switch buffer
                LinkedList<Execution> remaining = queue;
                queue = new LinkedList();

                // build text
                StringBuilder text = new StringBuilder();

                for (Execution e : remaining) {
                    if (storedId < e.id) {
                        text.append(e).append("\r\n");
                    }
                }

                // write normal log
                try (FileChannel channel = FileChannel.open(normal.create().asJavaPath(), CREATE, APPEND)) {
                    channel.write(ByteBuffer.wrap(text.toString().getBytes(ISO_8859_1)));
                    writeStoredId(remaining.getLast().id);

                    log.info("Write log until " + remaining.peekLast().date + " at " + service + ".");
                } catch (IOException e) {
                    e.printStackTrace();
                    throw I.quiet(e);
                }
            }, npe -> {
                readStoredId();

                // remove older execution from memory cache
                Iterator<Execution> iterator = queue.iterator();
                while (iterator.hasNext()) {
                    Execution e = iterator.next();

                    if (e.id <= storedId) {
                        iterator.remove();
                    }
                }
            });
        }

        /**
         * Read the latest stored id.
         */
        private void readStoredId() {
            try {
                storedId = Long.parseLong(store.text().trim());
            } catch (NumberFormatException e) {
                // do nothing
            }
        }

        /**
         * Write the latest stored id.
         */
        private void writeStoredId(long id) {
            store.text(Long.toString(storedId = id));
        }

        /**
         * Convert normal log to compact log asynchronously.
         */
        private void writeCompact() {
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

                CsvWriter writer = buildCsvWriter(new ZstdOutputStream(compact.newOutputStream(), 1));
                return executions.maps(Market.BASE, (prev, e) -> {
                    writer.writeRow(logger.encode(prev, e));
                    return e;
                }).effectOnComplete(writer::close);
            } catch (IOException e) {
                throw I.quiet(e);
            }
        }

        /**
         * Convert compact log to fast log synchronously.
         */
        private void writeFast() {
            if (fast.isAbsent() && compact.isPresent()) {
                try {
                    int scale = service.setting.targetCurrencyScaleSize;
                    TickerManager manager = new TickerManager();
                    read().to(manager::update);
                    Ticker ticker = manager.on(Span.Second5);

                    Execution[] prev = {Market.BASE};

                    CsvWriter writer = buildCsvWriter(new ZstdOutputStream(fast.newOutputStream(), 1));
                    ticker.ticks.each(tick -> {
                        long id = tick.openId;
                        Num buy = Num.of(tick.longVolume()).scale(scale).divide(2);
                        Num sell = Num.of(tick.shortVolume()).scale(scale).divide(2);
                        Direction buySide = Direction.BUY;
                        Direction sellSide = Direction.SELL;

                        if (buy.isZero()) {
                            if (sell.isZero()) return;
                            buy = sell = sell.divide(2);
                            buySide = sellSide;
                        } else if (sell.isZero()) {
                            buy = sell = buy.divide(2);
                            sellSide = buySide;
                        }

                        Direction[] sides = tick.isBull() ? new Direction[] {sellSide, buySide, sellSide, buySide}
                                : new Direction[] {buySide, sellSide, buySide, sellSide};
                        Num[] sizes = tick.isBull() ? new Num[] {sell, buy, sell, buy} : new Num[] {buy, sell, buy, sell};
                        Num[] prices = tick.isBull() ? new Num[] {tick.openPrice, tick.lowPrice(), tick.highPrice(), tick.closePrice()}
                                : new Num[] {tick.openPrice, tick.highPrice(), tick.lowPrice(), tick.closePrice()};

                        for (int i = 0; i < prices.length; i++) {
                            Execution e = Execution.with.direction(sides[i], sizes[i])
                                    .price(prices[i])
                                    .id(id + i)
                                    .date(tick.start().plusSeconds(i))
                                    .consecutive(Execution.ConsecutiveDifference)
                                    .delay(Execution.DelayInestimable);

                            writer.writeRow(logger.encode(prev[0], e));
                            prev[0] = e;
                        }
                    });
                    writer.close();
                } catch (Exception e) {
                    throw I.quiet(e);
                }
            }
        }

        /**
         * Convert compact log to normal log.
         */
        private void writeNormal() {
            if (normal.isAbsent() && compact.isPresent()) {
                CsvWriter writer = buildCsvWriter(normal.newOutputStream());
                read().to(e -> writer.writeRow(e.toString()));
                writer.close();
            }
        }

        /**
         * Create new CSV writer.
         * 
         * @param out
         * @return
         */
        private CsvWriter buildCsvWriter(OutputStream out) {
            CsvWriterSettings setting = new CsvWriterSettings();
            setting.getFormat().setDelimiter(' ');
            setting.getFormat().setComment('無');
            return new CsvWriter(out, ISO_8859_1, setting);
        }
    }

    /**
     * 
     */
    private class BufferFromRestToRealtime implements Observer<Execution> {

        /** The upper error handler. */
        private final Consumer<? super Throwable> error;

        /** The actual realtime execution buffer. */
        private ConcurrentLinkedDeque<Execution> realtime = new ConcurrentLinkedDeque();

        /** The execution event receiver. */
        private Observer<? super Execution> destination = realtime::add;

        /** The no-realtime latest execution id. */
        private long latestId = -1;

        /**
         * Build {@link BufferFromRestToRealtime}.
         * 
         * @param sequntial
         */
        private BufferFromRestToRealtime(Consumer<? super Throwable> error) {
            this.error = error;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public void accept(Execution e) {
            destination.accept(e);
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
            return service.checkEquality(e, first);
        }

        /**
         * Switch to realtime API.
         * 
         * @param currentId
         * @param observer
         */
        private void switchToRealtime(long currentId, Observer<? super Execution> observer) {
            log.info(service.marketIdentity() + " switch to Realtime API.");

            while (!realtime.isEmpty()) {
                ConcurrentLinkedDeque<Execution> buffer = realtime;
                realtime = new ConcurrentLinkedDeque();
                for (Execution e : buffer) {
                    observer.accept(e);
                }
                log.info("Realtime buffer write from {} to {}.  size {}", buffer.peek().date, buffer.peekLast().date, buffer.size());
            }
            destination = observer;
        }

        /**
         * Compute the first execution id in realtime buffer.
         * 
         * @return
         */
        private long realtimeFirstId() {
            if (!realtime.isEmpty()) {
                return realtime.peek().id;
            } else if (0 < latestId) {
                return latestId;
            } else {
                return latestId = service.executionLatest().to().map(v -> v.id).or(0L);
            }
        }
    }

    /**
     * Restore normal log of the specified market and date.
     * 
     * @param service
     * @param date
     */
    public static void restoreNormal(MarketService service, ZonedDateTime date) {
        ExecutionLog log = new ExecutionLog(service);
        Cache cache = log.cache(date);
        cache.writeNormal();
    }

    /**
     * Helper method to clear all fast log.
     */
    public static void clearFastLog() {
        I.load(Market.class);

        MarketServiceProvider.availableMarketServices().to(service -> {
            ExecutionLog log = new ExecutionLog(service);
            log.clearFastCache();
        });
    }

    public static void main3(String[] args) {
        clearFastLog();
    }

    public static void main(String[] args) throws InterruptedException {
        ExecutionLog log = new ExecutionLog(BitFlyer.BTC_JPY);
        Cache cache2 = log.cache(Chrono.utc(2020, 2, 9));
        cache2.writeNormal();

        Thread.sleep(30000);
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
            ArrayDeque<Execution> executions = service.executions(startId, startId + coefficient.multiply(size).longValue())
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