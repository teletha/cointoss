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

import static java.nio.charset.StandardCharsets.*;
import static java.nio.file.StandardOpenOption.*;
import static java.util.concurrent.TimeUnit.*;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.nio.channels.OverlappingFileLockException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayDeque;
import java.util.ArrayList;
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

import org.apache.commons.lang3.RandomUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.github.luben.zstd.ZstdInputStream;
import com.github.luben.zstd.ZstdOutputStream;
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
import cointoss.ticker.Span;
import cointoss.ticker.Ticker;
import cointoss.ticker.TickerManager;
import cointoss.util.Chrono;
import cointoss.util.arithmetic.Num;
import kiss.I;
import kiss.Observer;
import kiss.Signal;
import kiss.Signaling;
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

    /** The market provider. */
    private final MarketService service;

    /** The root directory of logs. */
    private final Directory root;

    /** The repository. */
    private final Repository repository;

    /** The active cache. */
    private Cache cache;

    /** The latest cached id. */
    private long cacheId;

    /** The latest stored id. */
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
        this.logger = I.make(service.setting.executionLogger());
        this.repository = new Repository(root, service);

        this.cache = new Cache(repository.firstZDT());
    }

    /**
     * Get a physical checkup on logs.
     */
    public final void checkup() {
        repository.collectLocals(false, false).map(this::cache).to(Cache::repair);
    }

    /**
     * Return the first cached date.
     * 
     * @return
     */
    public final ZonedDateTime firstCacheDate() {
        return repository.firstZDT();
    }

    /**
     * Return the last cached date.
     * 
     * @return
     */
    public final ZonedDateTime lastCacheDate() {
        return repository.lastZDT();
    }

    /**
     * Clear all fast log.
     */
    public final void clearFastCache() {
        root.directory("flog").delete();
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
            long startId = fromId != -1 ? fromId
                    : cacheId != 0 ? cacheId
                            : service.searchNearestExecution(Chrono.utcNow().minusDays(2)).map(e -> e.id).waitForTerminate().to().exact();
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
        return range(repository.firstZDT(), repository.lastZDT(), type);
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
        ZonedDateTime first = repository.firstZDT();
        long range = ChronoUnit.DAYS.between(first, repository.lastZDT().minusDays(days + 1));
        long offset = RandomUtils.nextLong(0, range);
        return range(first.plusDays(offset), first.plusDays(offset + days), type);
    }

    /**
     * Read all caches.
     * 
     * @return
     */
    final Signal<Cache> caches() {
        ZonedDateTime first = repository.firstZDT();
        ZonedDateTime last = repository.lastZDT();

        return I.signal(first).recurse(day -> day.plusDays(1)).takeWhile(day -> day.isBefore(last) || day.isEqual(last)).map(Cache::new);
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
     * 
     */
    class Cache {

        /** The end date */
        final LocalDate date;

        /** The log file. */
        final File normal;

        /** The log writing task. */
        private ScheduledFuture task = NOOP;

        /** The writing execution queue. */
        private LinkedList<Execution> queue = new LinkedList();

        /**
         * @param date
         */
        private Cache(ZonedDateTime date) {
            this.date = date.toLocalDate();
            this.normal = root.file("execution" + Chrono.DateCompact.format(date) + ".log");
        }

        /**
         * Locate compressed execution log.
         * 
         * @param date A date time.
         * @return A file location.
         */
        final File compactLog() {
            return normal.extension("clog");
        }

        /**
         * Locate fast execution log.
         * 
         * @param date A date time.
         * @return A file location.
         */
        final File fastLog() {
            return normal.extension("flog");
        }

        /**
         * Check whether the cache file exist or not.
         * 
         * @return
         */
        boolean exist() {
            return existNormal() || existCompact();
        }

        /**
         * Check whether the cache file exist or not.
         * 
         * @return
         */
        boolean existNormal() {
            return normal.isPresent() && normal.size() != 0;
        }

        /**
         * Check whether the cache file exist or not.
         * 
         * @return
         */
        boolean existCompletedNormal() {
            if (normal.isAbsent() && normal.size() == 0) {
                return false;
            }

            try (NormalLog log = new NormalLog(normal)) {
                long lastID = log.lastID();
                return service.executions(lastID, lastID + 1).first().waitForTerminate().to().exact().date.toLocalDate().isAfter(date);
            } catch (Exception e) {
                throw I.quiet(e);
            }
        }

        /**
         * Check whether the cache file exist or not.
         * 
         * @return
         */
        boolean existCompact() {
            File compact = compactLog();
            return compact.isPresent() && compact.size() != 0;
        }

        /**
         * Check whether the cache file exist or not.
         * 
         * @return
         */
        boolean existFast() {
            File fast = fastLog();
            return fast.isPresent() && fast.size() != 0;
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
        Signal<Execution> read(LogType... types) {
            LogType type = types == null || types.length == 0 ? LogType.Normal : types[0];

            if (existCompact()) {
                if (type == LogType.Fast) {
                    convertCompactToFast();
                    return readFast();
                } else {
                    return readCompact();
                }
            } else if (existNormal()) {
                return readNormal();
            } else {
                ExecutionLogRepository external = service.externalRepository();

                if (external != null && Chrono.within(repository.externalFirst, date, repository.externalLast)) {
                    return readExternalRepository(external);
                }

                return I.signal();
            }
        }

        /**
         * Read normal log.
         * 
         * @return
         */
        Signal<Execution> readNormal() {
            CsvParser parser = buildCsvParser();
            Stopwatch stopwatch = Stopwatch.createUnstarted();

            return I.signal(parser.iterate(normal.newInputStream(), ISO_8859_1))
                    .map(values -> (Execution) Execution.with.direction(Direction.parse(values[2]), Num.of(values[4]))
                            .price(Num.of(values[3]))
                            .id(Long.parseLong(values[0]))
                            .date(LocalDateTime.parse(values[1]).atZone(Chrono.UTC))
                            .consecutive(Integer.parseInt(values[5]))
                            .delay(Integer.parseInt(values[6])))
                    .effectOnComplete(parser::stopParsing)
                    .effectOnObserve(stopwatch::start)
                    .effectOnError(e -> log.error("Fail to read normal log. [" + normal + "]"))
                    .effectOnComplete(() -> {
                        log.trace("Read normal log {} [{}] {}", service.id(), date, stopwatch.stop().elapsed());
                    });
        }

        /**
         * Read compact log.
         * 
         * @return
         */
        Signal<Execution> readCompact() {
            CsvParser parser = buildCsvParser();
            Stopwatch stopwatch = Stopwatch.createUnstarted();
            File compact = compactLog();

            try {
                return I.signal(parser.iterate(new ZstdInputStream(compact.newInputStream()), ISO_8859_1))
                        .scanWith(Market.BASE, logger::decode)
                        .effectOnComplete(parser::stopParsing)
                        .effectOnObserve(stopwatch::start)
                        .effectOnError(e -> log.error("Fail to read compact log. [" + compact + "]"))
                        .effectOnComplete(() -> {
                            log.trace("Read compact log {} [{}] {}", service.id(), date, stopwatch.stop().elapsed());
                        });
            } catch (IOException e) {
                throw I.quiet(e);
            }
        }

        /**
         * Read fast log.
         * 
         * @return
         */
        Signal<Execution> readFast() {
            CsvParser parser = buildCsvParser();
            Stopwatch stopwatch = Stopwatch.createUnstarted();
            File fast = fastLog();

            try {
                return I.signal(parser.iterate(new ZstdInputStream(fast.newInputStream()), ISO_8859_1))
                        .scanWith(Market.BASE, logger::decode)
                        .effectOnComplete(parser::stopParsing)
                        .effectOnObserve(stopwatch::start)
                        .effectOnError(e -> log.error("Fail to read fast log. [" + fast + "]"))
                        .effectOnComplete(() -> {
                            log.trace("Read fast log {} [{}] {}", service.id(), date, stopwatch.stop().elapsed());
                        });
            } catch (IOException e) {
                throw I.quiet(e);
            }
        }

        /**
         * Read log from the external repository.
         * 
         * @return
         */
        Signal<Execution> readExternalRepository(ExecutionLogRepository external) {
            Stopwatch stopwatch = Stopwatch.createUnstarted();

            return writeNormal(external.convert(date)
                    .effectOnError(e -> log.error("Fail to download external log {} [{}].", service, date))
                    .effectOnObserve(stopwatch::start)
                    .effectOnComplete(() -> {
                        log.info("Donwload external log {} [{}] {}", service, date, stopwatch.stop().elapsed());
                    }));
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
                    storedId = remaining.getLast().id;

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
        private long readStoredId() {
            try (NormalLog reader = new NormalLog(normal)) {
                long id = reader.lastID();
                if (0 <= id) {
                    storedId = id;
                }
                return id;
            } catch (Exception e) {
                throw I.quiet(e);
            }
        }

        /**
         * Write the execution log to the normal log.
         * 
         * @param executions A list of executions to write.
         */
        void writeNormal(Execution... executions) {
            writeNormal(I.signal(executions)).to(I.NoOP);
        }

        /**
         * Write the execution log to the normal log.
         * 
         * @param executions A stream of executions to write.
         * @return Wrapped {@link Signal}.
         */
        Signal<Execution> writeNormal(Signal<Execution> executions) {
            int[] count = {1};
            CsvWriter writer = buildCsvWriter(normal.newOutputStream());

            return executions.effect(e -> {
                writer.writeRow(e.toString());

                // write out constantly
                if (count[0]++ % 2000 == 0) {
                    writer.flush();
                }
            }).effectOnComplete(() -> {
                writer.close();
                repository.updateLocal(date);
            });
        }

        /**
         * Write the execution log to the compact log.
         * 
         * @param executions A list of executions to write.
         */
        void writeCompact(Execution... executions) {
            writeCompact(I.signal(executions)).to(I.NoOP);
        }

        /**
         * Write the execution log to the compact log.
         * 
         * @param executions A stream of executions to write.
         * @return Wrapped {@link Signal}.
         */
        Signal<Execution> writeCompact(Signal<Execution> executions) {
            File compact = compactLog();

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
         * Create new CSV writer.
         * 
         * @param out
         * @return
         */
        private CsvParser buildCsvParser() {
            CsvParserSettings setting = new CsvParserSettings();
            setting.getFormat().setDelimiter(' ');
            setting.getFormat().setComment('無');
            return new CsvParser(setting);
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
         * Convert normal log to compact log.
         */
        void convertNormalToCompact() {
            if (existNormal() && !existCompact()) {
                writeCompact(readNormal()).to(I.NoOP, e -> {
                    log.error("{} fails to compact the normal log. [{}]", service, date, e);
                }, () -> {
                    normal.delete();
                });
            }
        }

        /**
         * Convert normal log to compact log asynchronously.
         */
        void convertNormalToCompactAsync() {
            if (!existCompact() && (!queue.isEmpty() || normal.isPresent())) {
                I.schedule(5, SECONDS).to(() -> {
                    writeCompact(readNormal()).effectOnComplete(() -> normal.delete()).to(I.NoOP);
                });
            }
        }

        /**
         * Convert compact log to normal log.
         */
        void convertCompactToNormal() {
            if (!existNormal() && existCompact()) {
                CsvWriter writer = buildCsvWriter(normal.newOutputStream());
                readCompact().to(e -> writer.writeRow(e.toString()));
                writer.close();
            }
        }

        /**
         * Convert compact log to fast log synchronously.
         */
        void convertCompactToFast() {
            File fast = fastLog();

            if (!existFast() && existCompact()) {
                try {
                    int scale = service.setting.target.scale;
                    AtomicLong fastID = new AtomicLong();
                    TickerManager manager = new TickerManager().disableMemorySaving();
                    readCompact().effect(e -> fastID.set(e.id)).to(manager::update);
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

        long estimateFirstID() {
            if (existCompact()) {
                return readCompact().first().to().v.id;
            }

            if (existNormal()) {
                return readNormal().first().to().v.id;
            }

            Cache yesterday = cache(date.minusDays(1));

            if (yesterday.existCompact()) {
                return yesterday.readCompact().last().to().v.id;
            }
            return service.searchNearestExecution(Chrono.utc(date)).waitForTerminate().map(Execution::id).to().or(-1L);
        }

        /**
         * Attempt to create a complete compact log by any means necessary.
         * <p>
         * Writes the specified execution log to the normal log of this Cache. It will not write the
         * log that is older than the history that has already been written. Also, it will not write
         * the log that does not correspond to the date of this Cache.
         * 
         * @return true if the compact log exists, false otherwise.
         */
        boolean repair() {
            // confirm the completed compact log
            if (existCompact()) {
                normal.delete();
                return true;
            }

            // // comfirm the completed normal log
            // if (existCompletedNormal()) {
            // convertNormalToCompact();
            // return true;
            // }

            // imcompleted or no normal log
            ExecutionLogRepository external = service.externalRepository();
            if (external != null && external.has(date)) {
                readExternalRepository(external).waitForTerminate().to(I.NoOP, log::error, this::convertNormalToCompact);
                return true;
            }

            boolean completed = false;
            try (NormalLog normalLog = new NormalLog(normal)) {
                long id = normalLog.lastID();
                if (id == -1) {
                    id = estimateFirstID();
                    if (id == -1) {
                        return false;
                    }
                }

                long startTime = Chrono.utc(date).toInstant().toEpochMilli();
                long endTime = startTime + 24 * 60 * 60 * 1000;
                List<Execution> executions = new ArrayList(service.setting.acquirableExecutionSize);

                while (!completed) {
                    // retrive the execution log from server
                    service.executions(id, id + service.setting.acquirableExecutionSize).waitForTerminate().toCollection(executions);

                    // Since the execution log after the specified ID does not exist on the server,
                    // it is not possible to create the completed normal log.
                    if (executions.isEmpty()) {
                        return false;
                    }

                    int startIndex = 0;
                    int endIndex = executions.size();
                    for (int i = 0; i < executions.size(); i++) {
                        Execution e = executions.get(i);

                        // Ignore any execution log that has an ID earlier than the current last ID.
                        if (e.id <= id) {
                            startIndex = i + 1;
                            continue;
                        }

                        // Ignore any execution log that has a date earlier than the date of this
                        // cache.
                        if (e.mills < startTime) {
                            startIndex = i + 1;
                            continue;
                        }

                        // If there is an execution log with a date later than the date of this
                        // cache, it is considered that a complete log has been obtained,
                        // so we should create it.
                        if (endTime <= e.mills) {
                            endIndex = i;
                            completed = true;
                            break;
                        }
                    }

                    List<Execution> valids = executions.subList(startIndex, endIndex);
                    if (!valids.isEmpty()) {
                        // Expand all valid log in memory for batch writing.
                        StringBuilder text = new StringBuilder();
                        for (Execution e : valids) {
                            text.append(e).append("\r\n");
                        }
                        normalLog.append(text.toString());

                        // update the last ID
                        id = valids.get(valids.size() - 1).id;

                        log.info("{} acquires the execution log from {} ({}).", service, valids.get(0).date, valids.size());
                    }

                    // clear all data to reuse the container
                    executions.clear();
                }
            } catch (Exception e) {
                throw I.quiet(e);
            }

            if (completed) {
                convertNormalToCompact();
                return true;
            } else {
                return false;
            }
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public String toString() {
            return service + " Log[" + date + "]";
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

    public static void main(String[] args) {

    }
}