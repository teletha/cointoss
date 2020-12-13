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
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map.Entry;
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

import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.RandomUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.github.luben.zstd.ZstdInputStream;
import com.github.luben.zstd.ZstdOutputStream;
import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Stopwatch;
import com.google.common.collect.TreeMultimap;
import com.univocity.parsers.csv.CsvParser;
import com.univocity.parsers.csv.CsvParserSettings;
import com.univocity.parsers.csv.CsvWriter;
import com.univocity.parsers.csv.CsvWriterSettings;

import cointoss.Direction;
import cointoss.Market;
import cointoss.MarketService;
import cointoss.market.Exchange;
import cointoss.market.MarketServiceProvider;
import cointoss.ticker.Span;
import cointoss.ticker.Ticker;
import cointoss.ticker.TickerManager;
import cointoss.util.Chrono;
import cointoss.util.arithmetic.Num;
import kiss.I;
import kiss.Observer;
import kiss.Signal;
import kiss.Signaling;
import kiss.Storable;
import psychopath.Directory;
import psychopath.File;

/**
 * {@link Execution} Log Manager.
 */
public class ExecutionLog {

    /** The logging system. */
    private static final Logger log = LogManager.getLogger(ExecutionLog.class);

    /** The message aggregator. */
    private static final Signaling<MarketService> aggregateWritingLog = new Signaling();

    static {
        aggregateWritingLog.expose.debounceAll(5, TimeUnit.SECONDS).to(services -> {
            TreeMultimap<Exchange, String> map = TreeMultimap.create();

            for (MarketService service : services) {
                map.put(service.exchange, service.marketName);
            }

            for (Entry<Exchange, Collection<String>> entry : map.asMap().entrySet()) {
                log.info("Saved execution log for " + entry.getValue().size() + " markets in " + entry.getKey() + ". " + entry.getValue());
            }
        });
    }

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
    private static final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1, run -> {
        Thread thread = new Thread(run);
        thread.setName("ExecutionLog Writer");
        thread.setDaemon(true);
        return thread;
    });

    /** The file data format */
    private static final DateTimeFormatter FileNamePattern = DateTimeFormatter.ofPattern("yyyyMMdd");

    /** The market provider. */
    private final MarketService service;

    /** The root directory of logs. */
    private final Directory root;

    /** The latest id store in local file. */
    private final File store;

    /** The repository. */
    private final Repository repository;

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
        this(service, service.directory());
        service.add(() -> cache.write() /* Don't use method reference */);
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
        this.repository = new Repository(service.externalRepository());

        this.cacheFirst = repository.firstZDT();
        this.cacheLast = repository.lastZDT();

        this.cache = new Cache(cacheFirst);
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
        ZonedDateTime endDay = repository.lastZDT();
        ZonedDateTime startDay = Chrono.between(repository.firstZDT(), start, endDay).truncatedTo(ChronoUnit.DAYS);

        return I.signal(startDay)
                .recurse(day -> day.plusDays(1))
                .takeWhile(day -> day.isBefore(endDay) || day.isEqual(endDay))
                .concatMap(day -> new Cache(day).read(type))
                .effect(e -> cacheId = e.id)
                .take(e -> e.isAfter(start))
                .effectOnComplete(() -> storedId = cacheId)
                .concat(network(-1).effect(this::cache));
    }

    /**
     * Read date from merket server.
     * 
     * @param fromId A starting id.
     * @return
     */
    private Signal<Execution> network(long fromId) {
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
            long startId = fromId != -1 ? fromId : cacheId != 0 ? cacheId : estimateInitialExecutionId();
            Num coefficient = Num.ONE;
            ArrayDeque<Execution> rests = new ArrayDeque(size);
            while (!disposer.isDisposed()) {
                rests.clear();

                long range = Math.round(service.setting.acquirableExecutionSize * coefficient.doubleValue());
                service.executions(startId, startId + range).waitForTerminate().to(rests::add, observer::error);

                // Since the synchronous REST API did not return an error, it can be determined that
                // the server is operating normally, so the real-time API is also connected.
                if (activeRealtime == false) {
                    activeRealtime = true;
                    disposer.add(service.executionsRealtimely(false).to(buffer, observer::error));
                }
                int retrieved = rests.size();

                if (retrieved != 0) {
                    // REST API returns some executions
                    if (size <= retrieved && coefficient.isGreaterThan(1)) {
                        // Since there are too many data acquired,
                        // narrow the data range and get it again.
                        coefficient = Num
                                .max(Num.ONE, coefficient.isGreaterThan(50) ? coefficient.divide(2).scale(0) : coefficient.minus(5));
                        continue;
                    } else {
                        log.info("REST write on " + service + " from {}.  size {} ({})", rests.getFirst().date, retrieved, coefficient);

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

                        long latestId = rests.peekLast().id;
                        if (retrieved == 1 && buffer.realtime.isEmpty() && startId == latestId) {
                            // REST API has caught up with the real-time API,
                            // we must switch to realtime API.
                            buffer.switchToRealtime(latestId, observer);
                            return disposer;
                        }
                        startId = latestId;

                        // The number of acquired data is too small,
                        // expand the data range slightly from next time.
                        if (retrieved < size * 0.05) {
                            coefficient = coefficient.plus("50");
                        } else if (retrieved < size * 0.1) {
                            coefficient = coefficient.plus("5");
                        } else if (retrieved < size * 0.3) {
                            coefficient = coefficient.plus("2");
                        } else if (retrieved < size * 0.5) {
                            coefficient = coefficient.plus("0.5");
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
                        startId += range - 1;
                        coefficient = coefficient.plus("50");
                        continue;
                    }

                    // REST API has caught up with the real-time API,
                    // we must switch to realtime API.
                    buffer.switchToRealtime(startId, observer);
                    break;
                }
            }
            return disposer;
        }).effectOnError(e -> e.printStackTrace()).retryWhen(service.retryPolicy(500, "ExecutionLog"));
    }

    /**
     * Estimate the inital execution id of the {@link Market}.
     * 
     * @return
     */
    private long estimateInitialExecutionId() {
        long start = 0;
        long end = service.executionLatest().waitForTerminate().to().exact().id;
        long middle = (start + end) / 2;
        long previousEnd = end;

        while (true) {
            List<Execution> result = service.executionLatestAt(middle).skipError().waitForTerminate().toList();
            if (result.isEmpty()) {
                start = middle;
                middle = (start + end) / 2;
            } else {
                long id = result.get(0).id;
                if (id == previousEnd) {
                    return id;
                } else {
                    previousEnd = id;
                }
                end = result.get(0).id + 1;
                middle = (start + end) / 2;
            }

            if (end - start <= 10) {
                return start;
            }
        }
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
                cache.convertNormalToCompactAsync();
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
     * Read log from the specified id.
     * 
     * @param id
     */
    public final Signal<Execution> fromId(long id) {
        return network(id).effect(this::cache);
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
    final Cache cache(LocalDate date) {
        return new Cache(Chrono.utc(date));
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
     * 
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
            return existNormal() || existCompact();
        }

        /**
         * Check whether the cache file exist or not.
         * 
         * @return
         */
        private boolean existNormal() {
            return normal.isPresent() && normal.size() != 0;
        }

        /**
         * Check whether the cache file exist or not.
         * 
         * @return
         */
        private boolean existCompact() {
            return compact.isPresent() && compact.size() != 0;
        }

        /**
         * Start writing log automatically.
         * 
         * @return Chainable API
         */
        private Cache enableAutoSave() {
            if (task == NOOP) {
                task = scheduler.scheduleWithFixedDelay(this::write, 60, 240, TimeUnit.SECONDS);
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
        @SuppressWarnings("resource")
        private Signal<Execution> read(LogType... types) {
            LogType type = types == null || types.length == 0 ? LogType.Normal : types[0];
            Stopwatch stopwatch = Stopwatch.createUnstarted();

            try {
                CsvParserSettings setting = new CsvParserSettings();
                setting.getFormat().setDelimiter(' ');
                setting.getFormat().setComment('無');

                CsvParser parser = new CsvParser(setting);
                if (existCompact()) {
                    if (type == LogType.Fast) {
                        // read from fast log
                        writeFast();
                        return I.signal(parser.iterate(new ZstdInputStream(fast.newInputStream()), ISO_8859_1))
                                .scanWith(Market.BASE, logger::decode)
                                .effectOnComplete(parser::stopParsing)
                                .effectOnObserve(stopwatch::start)
                                .effectOnError(e -> log.error("Fail to read fast log. [" + fast + "]"))
                                .effectOnComplete(() -> {
                                    log.trace("Read fast log {} [{}] {}", service.id(), date, stopwatch.stop().elapsed());
                                });
                    } else {
                        // read from compact log
                        return I.signal(parser.iterate(new ZstdInputStream(compact.newInputStream()), ISO_8859_1))
                                .scanWith(Market.BASE, logger::decode)
                                .effectOnComplete(parser::stopParsing)
                                .effectOnObserve(stopwatch::start)
                                .effectOnError(e -> log.error("Fail to read compact log. [" + compact + "]"))
                                .effectOnComplete(() -> {
                                    log.trace("Read compact log {} [{}] {}", service.id(), date, stopwatch.stop().elapsed());
                                });
                    }
                } else if (existNormal()) {
                    // read from normal log
                    return I.signal(parser.iterate(normal.newInputStream(), ISO_8859_1))
                            .map(this::parse)
                            .effectOnComplete(parser::stopParsing)
                            .effectOnObserve(stopwatch::start)
                            .effectOnError(e -> log.error("Fail to read normal log. [" + normal + "]"))
                            .effectOnComplete(() -> {
                                log.trace("Read normal log {} [{}] {}", service.id(), date, stopwatch.stop().elapsed());
                            });
                } else {
                    // read from external repository
                    ExecutionLogRepository external = service.externalRepository();

                    if (external != null) {
                        return writeNormal(external.convert(date).effectOnObserve(stopwatch::start).effectOnComplete(() -> {
                            log.info("Donwload external log {} [{}] {}", service.id(), date, stopwatch.stop().elapsed());
                        }));
                    }

                    // read from server
                    ZonedDateTime start = Chrono.utc(date);
                    ZonedDateTime end = start.plusDays(1);

                    return service.executionLatest()
                            .flatMap(latest -> findNearest(start, latest))
                            .flatMap(e -> network(e.id))
                            .skipWhile(e -> e.isBefore(start))
                            .takeWhile(e -> e.isBefore(end))
                            .effectOnComplete(executions -> {
                                if (end.isBefore(Chrono.utcToday())) {
                                    writeCompact(I.signal(executions)).to();
                                } else {
                                    writeNormal(I.signal(executions)).to();
                                }
                            });
                }
            } catch (IOException e) {
                throw I.quiet(e);
            }
        }

        /**
         * Estimate the nearest {@link Execution} at the specified time.
         * 
         * @param target
         * @param latest
         * @return
         */
        private Signal<Execution> findNearest(ZonedDateTime target, Execution latest) {
            return service.executionLatestAt(latest.id - service.setting.acquirableExecutionSize).concatMap(previous -> {
                long timeDistance = latest.mills - previous.mills;
                long idDistance = latest.id - previous.id;
                long targetDistance = latest.mills - Chrono.utc(date).toInstant().toEpochMilli();
                long estimatedTargetId = latest.id - idDistance * (targetDistance / timeDistance);

                return service.executionLatestAt(estimatedTargetId).concatMap(estimated -> {
                    if (estimated.isBefore(target) && estimated.isAfter(target.minusMinutes(30))) {
                        return I.signal(estimated);
                    } else {
                        return findNearest(target, estimated);
                    }
                });
            }).first();
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

                    aggregateWritingLog.accept(service);
                    repository.updateLocal(date);
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
         * Write normal log.
         * 
         * @param executions
         * @return
         */
        private Signal<Execution> writeNormal(Signal<Execution> executions) {
            CsvWriter writer = buildCsvWriter(normal.newOutputStream());

            return executions.effect(e -> {
                writer.writeRow(e.toString());
            }).effectOnComplete(() -> {
                writer.close();
                repository.updateLocal(date);
            });
        }

        /**
         * Write compact log from the specified executions.
         */
        @VisibleForTesting
        Signal<Execution> writeCompact(Signal<Execution> executions) {
            try {
                CsvWriter writer = buildCsvWriter(new ZstdOutputStream(compact.newOutputStream(), 1));

                return executions.maps(Market.BASE, (prev, e) -> {
                    writer.writeRow(logger.encode(prev, e));
                    return e;
                }).effectOnComplete(() -> {
                    writer.close();
                    repository.updateLocal(date);
                });
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
                    int scale = service.setting.target.scale;
                    AtomicLong fastID = new AtomicLong();
                    TickerManager manager = new TickerManager().disableMemorySaving();
                    read().effect(e -> fastID.set(e.id)).to(manager::update);
                    fastID.updateAndGet(v -> v - 69120 /* 4x12x60x24 */);
                    Ticker ticker = manager.on(Span.Second5);
                    Execution[] prev = {Market.BASE};

                    CsvWriter writer = buildCsvWriter(new ZstdOutputStream(fast.newOutputStream(), 1));
                    ticker.ticks.each(tick -> {
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
                                    .id(fastID.getAndIncrement())
                                    .date(tick.openTime().plusSeconds(i))
                                    .consecutive(Execution.ConsecutiveDifference)
                                    .delay(Execution.DelayInestimable);

                            writer.writeRow(logger.encode(prev[0], e));
                            prev[0] = e;
                        }
                    });
                    writer.close();
                } catch (Throwable e) {
                    throw new Error("Failed writing the fast log. [" + fast + "]", e);
                }
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

        /**
         * Convert normal log to compact log asynchronously.
         */
        private void convertNormalToCompactAsync() {
            if (compact.isAbsent() && (!queue.isEmpty() || normal.isPresent())) {
                I.schedule(5, SECONDS).to(() -> {
                    writeCompact(read()).effectOnComplete(() -> normal.delete()).to(I.NoOP);
                });
            }
        }

        /**
         * Convert compact log to normal log.
         */
        private void convertCompactToNormal() {
            if (normal.isAbsent() && compact.isPresent()) {
                CsvWriter writer = buildCsvWriter(normal.newOutputStream());
                read().to(e -> writer.writeRow(e.toString()));
                writer.close();
            }
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
            // realtime buffer is empty
            Execution first = realtime.peekFirst();
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
            log.info(service.id() + " switch to Realtime API.");

            while (!realtime.isEmpty()) {
                ConcurrentLinkedDeque<Execution> buffer = realtime;
                realtime = new ConcurrentLinkedDeque();
                latestId = -1;
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
                return latestId = service.executionLatest().map(e -> e.id).waitForTerminate().to().or(-1L);
            }
        }
    }

    /**
     * Store for reepository related info.
     */
    private class Repository implements Storable<Repository> {

        private final ExecutionLogRepository external;

        /** The last scan date-time in local repository. */
        public LocalDate localScanLatest = LocalDate.of(1970, 1, 1);

        /** The oldest cache. */
        public LocalDate localFirst;

        /** The latest cache. */
        public LocalDate localLast;

        /** The last scan date-time in external repository. */
        public LocalDate externalScanLatest = LocalDate.of(1970, 1, 1);

        /** The oldest cache. */
        public LocalDate externalFirst;

        /** The latest cache. */
        public LocalDate externalLast;

        /**
         * Initialize.
         * 
         * @param external
         */
        private Repository(ExecutionLogRepository external) {
            this.external = external;

            restore();

            scanLocalRepository();
            scanExternalRepository();
        }

        /**
         * Scan logs in the local repository.
         */
        private void scanLocalRepository() {
            LocalDate now = LocalDate.now(Chrono.UTC);

            if (now.isAfter(localScanLatest)) {
                LocalDate[] dates = new LocalDate[2];

                root.walkFile("execution*.*og")
                        .map(file -> LocalDate.parse(file.name().subSequence(9, 17), FileNamePattern))
                        .effectOnce(date -> {
                            dates[0] = date;
                            dates[1] = date;
                        })
                        .to(date -> {
                            if (date.isBefore(dates[0])) {
                                dates[0] = date;
                            } else if (date.isAfter(dates[1])) {
                                dates[1] = date;
                            }
                        }, e -> {
                            // ignore
                        }, () -> {
                            localFirst = dates[0];
                            localLast = dates[1];
                            localScanLatest = now;
                            store();
                        });
            }
        }

        /**
         * Scan logs in the external repository.
         */
        private void scanExternalRepository() {
            if (external == null) {
                return;
            }

            LocalDate now = LocalDate.now(Chrono.UTC);

            if (now.isAfter(externalScanLatest)) {
                LocalDate[] dates = new LocalDate[2];

                external.collect().map(ZonedDateTime::toLocalDate).effectOnce(date -> {
                    dates[0] = date;
                    dates[1] = date;
                }).waitForTerminate().to(date -> {
                    if (date.isBefore(dates[0])) {
                        dates[0] = date;
                    } else if (date.isAfter(dates[1])) {
                        dates[1] = date;
                    }
                }, e -> {
                    // ignore
                }, () -> {
                    externalFirst = dates[0];
                    externalLast = dates[1];
                    externalScanLatest = now;
                    store();
                });
            }
        }

        /**
         * Compute the first cache.
         * 
         * @return
         */
        private LocalDate first() {
            return ObjectUtils.min(localFirst, externalFirst, LocalDate.now(Chrono.UTC));
        }

        /**
         * Compute the last cache.
         * 
         * @return
         */
        private LocalDate last() {
            return ObjectUtils.max(localLast, externalLast, first());
        }

        /**
         * Compute the first cache.
         * 
         * @return
         */
        private ZonedDateTime firstZDT() {
            return first().atTime(0, 0).atZone(Chrono.UTC);
        }

        /**
         * Compute the last cache.
         * 
         * @return
         */
        private ZonedDateTime lastZDT() {
            return last().atTime(0, 0).atZone(Chrono.UTC);
        }

        /**
         * Update the local resource.
         * 
         * @param date
         */
        private void updateLocal(LocalDate date) {
            if (localFirst == null || date.isBefore(localFirst)) {
                localFirst = date;
                store();
            } else if (localLast == null || date.isAfter(localLast)) {
                localLast = date;
                store();
            }
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public String locate() {
            return root.file("repository.json").toString();
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
        cache.convertCompactToNormal();
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
}