/*
 * Copyright (C) 2024 The COINTOSS Development Team
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
import static psychopath.Option.*;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousFileChannel;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.nio.channels.OverlappingFileLockException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

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
import cointoss.ticker.TickerBuilder;
import cointoss.util.Chrono;
import cointoss.util.JobType;
import hypatia.Num;
import kiss.I;
import kiss.Signal;
import kiss.Variable;
import psychopath.Directory;
import psychopath.File;

/**
 * {@link Execution} Log Manager.
 */
public class ExecutionLog {

    /** The market provider. */
    public final MarketService service;

    /** The root directory of logs. */
    private final Directory root;

    /** The repository. */
    private final Repository repository;

    /** The active cache. */
    private Cache cache;

    /** The latest cached id. */
    private long cacheId;

    /** The log parser. */
    private final ExecutionLogger logger;

    /**
     * Create log manager.
     * 
     * @param service
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

    public final long estimateLastID() {
        return lastCache().estimateLastID();
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
     * Return the first cache.
     * 
     * @return
     */
    final Cache firstCache() {
        return cache(firstCacheDate());
    }

    /**
     * Return the last cache.
     * 
     * @return
     */
    final Cache lastCache() {
        return cache(lastCacheDate());
    }

    /**
     * Create the specified date cache for TEST.
     */
    final Cache cache(int year, int month, int day) {
        return new Cache(Chrono.utc(year, month, day));
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
     * Clear all fast log.
     */
    public final void clearRepositoryInfo() {
        root.file("repository.json").delete();
    }

    /**
     * Store the {@link Execution} to local cache.
     * 
     * @param e An {@link Execution} to store.
     */
    public final void store(Execution e) {
        if (cacheId < e.id) {
            cacheId = e.id;

            if (e.mills < cache.startTime || cache.endTime <= e.mills) {
                cache.disableAutoSave();
                if (service.supportStableExecutionQuery()) cache.convertNormalToCompact(true);
                cache = new Cache(e.date).enableAutoSave();
            }
            cache.queue.add(e);
        }
    }

    /**
     * Read date from the specified date.
     * 
     * @param start The start date.
     * @return
     */
    public final Signal<Execution> from(ZonedDateTime start, LogType... type) {
        return range(start, lastCacheDate(), type);
    }

    /**
     * Read log from the specified date.
     * 
     * @return
     */
    public final Signal<Execution> fromLast(int days, LogType... type) {
        return from(Chrono.utcNow().minusDays(days), type);
    }

    /**
     * Read log from the specified start to end.
     * 
     * @param start
     * @param end
     * @return
     */
    public final Signal<Execution> range(ZonedDateTime start, ZonedDateTime end, LogType... type) {
        List<ZonedDateTime> days = I.signal(start)
                .recurse(day -> day.plusDays(1))
                .takeUntil(day -> day.isEqual(end) || day.isAfter(end))
                .toList();

        return I.signal(days).concatMap(day -> at(day, type));
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

    public final void repair(ZonedDateTime date) {
        cache(date).repair(false);
    }

    /**
     * 
     */
    class Cache {

        /** The target date */
        final LocalDate date;

        /** The start time. */
        final long startTime;

        /** The end time. */
        final long endTime;

        /** The log file. */
        final File normal;

        /** The log writing task. */
        private ScheduledFuture task = JobType.NOOP;

        /** The writing execution queue. */
        private LinkedList<Execution> queue = new LinkedList();

        /** The lock file. */
        private AsynchronousFileChannel lockChannel;

        /**
         * @param date
         */
        private Cache(ZonedDateTime date) {
            this.date = date.toLocalDate();
            this.startTime = date.truncatedTo(ChronoUnit.DAYS).toInstant().toEpochMilli();
            this.endTime = startTime + 24 * 60 * 60 * 1000;

            this.normal = root.file("executions/" + Chrono.DateFolderFormatter.format(date) + "/execution" + Chrono.DateCompact
                    .format(date) + ".log");
        }

        /**
         * Locate compressed execution log.
         * 
         * @return A file location.
         */
        final File compactLog() {
            return normal.extension("clog");
        }

        /**
         * Locate fast execution log.
         * 
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
                Variable<Execution> latest = service.executions(lastID, lastID + 1).first().waitForTerminate().to();
                return latest.isPresent() ? latest.v.date.toLocalDate().isAfter(date) : false;
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
            if (task == JobType.NOOP) {
                task = JobType.ExecutionLogWriter.schedule(service, process -> {
                    write();
                });
            }
            return this;
        }

        /**
         * Stop writing log automatically.
         * 
         * @return
         */
        private Cache disableAutoSave() {
            if (task != JobType.NOOP) {
                task.cancel(false);
                task = JobType.NOOP;
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

            if (existCompact() || existFast()) {
                if (type == LogType.Fast) {
                    return readFast();
                } else {
                    return readCompact();
                }
            } else if (existNormal()) {
                repair(true);

                return readNormal();
            } else if (service.loghouse().isValid()) {
                return readExternal(service.loghouse()).effectOnComplete(() -> convertNormalToCompact(true));
            } else {
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
                    .effectOnError(e -> I.error("Fail to read normal log. [" + normal + "]"))
                    .effectOnComplete(() -> {
                        I.trace("Read normal log " + service.id + " [" + date + "] " + stopwatch.stop().elapsed());
                    });
        }

        /**
         * Read compact log.
         * 
         * @return
         */
        Signal<Execution> readCompact() {
            return readCompact(compactLog());
        }

        private Signal<Execution> readCompact(File file) {
            CsvParser parser = buildCsvParser();
            Stopwatch stopwatch = Stopwatch.createUnstarted();

            try {
                return I.signal(parser.iterate(new ZstdInputStream(file.newInputStream()), ISO_8859_1))
                        .scan(() -> Market.BASE, logger::decode)
                        .effectOnComplete(parser::stopParsing)
                        .effectOnObserve(stopwatch::start)
                        .effectOnError(e -> {
                            I.error("Fail to read compact log. [" + file + "]");
                            if (existNormal()) {
                                file.delete();
                            }
                        })
                        .effectOnComplete(() -> {
                            I.trace("Read compact log " + service.id + " [" + date + "] " + stopwatch.stop().elapsed());
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
                if (!existFast()) {
                    return this.writeFast(readCompact().plug(new FastLog(service.setting.target.scale)))
                            .effectOnObserve(stopwatch::start)
                            .effectOnError(e -> I.error("Fail to read fast log. [" + fast + "]"))
                            .effectOnComplete(() -> {
                                I.trace("Read fast log " + service.id + " [" + date + "] " + stopwatch.stop().elapsed());
                            });
                } else {
                    return I.signal(parser.iterate(new ZstdInputStream(fast.newInputStream()), ISO_8859_1))
                            .scan(() -> Market.BASE, logger::decode)
                            .effectOnComplete(parser::stopParsing)
                            .effectOnObserve(stopwatch::start)
                            .effectOnError(e -> {
                                I.error("Fail to read fast log. [" + fast + "]");
                                I.error(e);
                            })
                            .effectOnComplete(() -> {
                                I.trace("Read fast log " + service.id + " [" + date + "] " + stopwatch.stop().elapsed());
                            });
                }
            } catch (IOException e) {
                throw I.quiet(e);
            }
        }

        /**
         * Read log from the external {@link LogHouse}.
         * 
         * @return
         */
        Signal<Execution> readExternal(LogHouse external) {
            Stopwatch stopwatch = Stopwatch.createUnstarted();

            return writeNormal(external.convert(date)) //
                    .timeout(10, TimeUnit.SECONDS)
                    .effectOnObserve(() -> stopwatch.reset().start())
                    .effectOnError(e -> I.error("Fail to download external log " + service + " [" + date + "]."))
                    .retry(e -> e.effect(x -> System.out.println(x)).take(3).delay(500, TimeUnit.MILLISECONDS))
                    .effectOnComplete(() -> {
                        I.info("Donwload external log " + service + " [" + date + "] in " + Chrono
                                .formatAsDuration(stopwatch.stop().elapsed()));
                    })
                    .waitForTerminate();
        }

        /**
         * Write all queued executions to log file.
         */
        private void write() {
            if (queue.isEmpty()) {
                return;
            }

            // The first thing to do is to find out if you have already acquired the right to write
            // logs. If FileLock is acquired every time the log is written, a memory leak will
            // occur. You may think that you can just release the lock each time, but this program
            // must hold the lock until it exits, and for the reasons described in the FileLock
            // Javadoc, it cannot release the lock.
            //
            // ======================= From FileLock Javadoc =======================
            // On some systems, closing a channel releases all locks held by the Java virtual
            // machine on the underlying file regardless of whether the locks were acquired via that
            // channel or via another channel open on the same file. It is strongly recommended
            // that, within a program, a unique channel be used to acquire all locks on any given
            // file.
            // =============================================================
            if (lockChannel != null) {
                writeActually();
                return;
            }

            try {
                lockChannel = AsynchronousFileChannel.open(root.file(".lock").asJavaPath(), CREATE, WRITE);
                FileLock lock = lockChannel.tryLock();

                if (lock == null) {
                    // another program has already acquired the right to write to the log

                    // remove older execution from memory cache
                    long lastID = estimateLastID();
                    Iterator<Execution> iterator = queue.iterator();
                    while (iterator.hasNext()) {
                        Execution e = iterator.next();

                        if (e.id <= lastID) {
                            iterator.remove();
                        }
                    }
                } else {
                    writeActually();
                }
            } catch (OverlappingFileLockException e) {
                writeActually();
            } catch (IOException e) {
                throw I.quiet(e);
            }
        }

        /**
         * Write all queued executions to log file.
         */
        private void writeActually() {
            long lastID = estimateLastID();

            // switch buffer
            LinkedList<Execution> remaining = queue;
            queue = new LinkedList();

            // build text
            StringBuilder text = new StringBuilder();

            for (Execution e : remaining) {
                if (lastID < e.id) {
                    text.append(e).append("\r\n");
                }
            }
            remaining.clear(); // immediately

            if (text.isEmpty()) {
                return;
            }

            // write normal log
            try (FileChannel channel = FileChannel.open(normal.create().asJavaPath(), CREATE, APPEND)) {
                channel.write(ByteBuffer.wrap(text.toString().getBytes(ISO_8859_1)));
                repository.updateLocal(date);
            } catch (Throwable e) {
                throw I.quiet(e);
            }
        }

        /**
         * Write the execution log to the normal log.
         * 
         * @param executions A list of executions to write.
         */
        Cache writeNormal(Execution... executions) {
            writeNormal(I.signal(executions)).to(I.NoOP);

            return this;
        }

        /**
         * Write the execution log to the normal log.
         * 
         * @param executions A list of executions to write.
         */
        Cache writeNormal(Iterable<Execution> executions) {
            writeNormal(I.signal(executions)).to(I.NoOP);

            return this;
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
        Cache writeCompact(Execution... executions) {
            writeCompact(I.signal(executions)).to(I.NoOP);

            return this;
        }

        /**
         * Write the execution log to the compact log.
         * 
         * @param executions A list of executions to write.
         */
        Cache writeCompact(Iterable<Execution> executions) {
            writeCompact(I.signal(executions)).to(I.NoOP);

            return this;
        }

        /**
         * Write the execution log to the fast log.
         * 
         * @param executions A stream of executions to write.
         * @return Wrapped {@link Signal}.
         */
        Signal<Execution> writeCompact(Signal<Execution> executions) {
            return writeCompact(executions, 7);
        }

        /**
         * Write the execution log to the fast log.
         * 
         * @param executions A stream of executions to write.
         * @return Wrapped {@link Signal}.
         */
        private Signal<Execution> writeCompact(Signal<Execution> executions, int level) {
            File compact = compactLog();

            try {
                Execution[] prev = {Market.BASE};
                CsvWriter writer = buildCsvWriter(new ZstdOutputStream(compact.newOutputStream(ATOMIC_WRITE), level));

                return executions.plug(new CompactLog()).effect(e -> {
                    writer.writeRow(logger.encode(prev[0], e));
                    prev[0] = e;
                }).effectOnComplete(() -> {
                    writer.close();
                    repository.updateLocal(date);
                });
            } catch (IOException e) {
                throw I.quiet(e);
            }
        }

        /**
         * Write the execution log to the fast log.
         * 
         * @param executions A list of executions to write.
         */
        Cache writeFast(Execution... executions) {
            writeFast(I.signal(executions)).to(I.NoOP);

            return this;
        }

        /**
         * Write the execution log to the fast log.
         * 
         * @param executions A list of executions to write.
         */
        Cache writeFast(Iterable<Execution> executions) {
            writeFast(I.signal(executions)).to(I.NoOP);

            return this;
        }

        File createCompact(int level) {
            File file = normal.extension("log" + level);

            try {
                Execution[] prev = {Market.BASE};
                CsvWriter writer = buildCsvWriter(new ZstdOutputStream(file.newOutputStream(ATOMIC_WRITE), level));

                readCompact().plug(new CompactLog()).effect(e -> {
                    writer.writeRow(logger.encode(prev[0], e));
                    prev[0] = e;
                }).effectOnComplete(() -> {
                    writer.close();
                }).to(I.NoOP);

                return file;
            } catch (IOException e) {
                throw I.quiet(e);
            }
        }

        Signal<Execution> read(File file) {
            CsvParser parser = buildCsvParser();
            Stopwatch stopwatch = Stopwatch.createUnstarted();

            try {
                return I.signal(parser.iterate(new ZstdInputStream(file.newInputStream()), ISO_8859_1))
                        .scan(() -> Market.BASE, logger::decode)
                        .effectOnComplete(parser::stopParsing)
                        .effectOnObserve(stopwatch::start)
                        .effectOnError(e -> {
                            I.error("Fail to read compact log. [" + file + "]");
                        })
                        .effectOnComplete(() -> {
                            I.trace("Read compact log " + service.id + " [" + date + "] " + stopwatch.stop().elapsed());
                        });
            } catch (IOException e) {
                throw I.quiet(e);
            }
        }

        /**
         * Write the execution log to the fast log.
         * 
         * @param executions A stream of executions to write.
         * @return Wrapped {@link Signal}.
         */
        Signal<Execution> writeFast(Signal<Execution> executions) {
            File fast = fastLog();

            try {
                Execution[] prev = {Market.BASE};
                CsvWriter writer = buildCsvWriter(new ZstdOutputStream(fast.newOutputStream(ATOMIC_WRITE), 1));

                return executions.plug(new FastLog(service.setting.target.scale)).effect(e -> {
                    writer.writeRow(logger.encode(prev[0], e));
                    prev[0] = e;
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
            setting.getFormat().setLineSeparator("\r\n");
            return new CsvWriter(out, ISO_8859_1, setting);
        }

        /**
         * Convert normal log to compact log.
         */
        Cache convertNormalToCompact(boolean async) {
            if (!existCompact() && (!queue.isEmpty() || existNormal())) {
                if (async) {
                    I.schedule(5, TimeUnit.SECONDS).to(() -> convertNormalToCompact(false));
                } else {
                    writeFast(writeCompact(readNormal())).effectOnLifecycle(new TickerBuilder(service)).to(I.NoOP, e -> {
                        I.error(service + " fails to compact the normal log. [" + date + "]");
                        I.error(e);
                    }, () -> {
                        normal.delete();
                    });
                }
            }
            return this;
        }

        /**
         * Convert compact log to normal log.
         */
        Cache convertCompactToNormal() {
            if (!existNormal() && existCompact()) {
                CsvWriter writer = buildCsvWriter(normal.newOutputStream(ATOMIC_WRITE));
                readCompact().to(e -> {
                    writer.writeRow(e.toString());
                }, e -> {
                    I.error(service + " fails to restore the normal log. [" + date + "]");
                    I.error(e);
                }, () -> {
                    writer.close();
                });
            }
            return this;
        }

        /**
         * Convert compression level of compact log.
         * 
         * @param level
         */
        void convertCompactLevel(int level) {
            if (existCompact()) {
                File compact = compactLog();
                File temp = compact.renameTo(compact.base() + ".temp");
                writeCompact(readCompact(temp)).effectOnComplete(temp::delete).to(I.NoOP);
            }
        }

        /**
         * Build fast log.
         */
        Cache buildFast() {
            if (!existFast()) {
                if (existCompact()) {
                    writeFast(readCompact()).to(I.NoOP);
                } else if (existNormal()) {
                    writeFast(writeCompact(readNormal())).to(I.NoOP);
                }
            }
            return this;
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

        long estimateLastID() {
            if (existCompact()) {
                return readCompact().last().to().exact().id;
            } else if (existNormal()) {
                try (NormalLog reader = new NormalLog(normal)) {
                    return reader.lastID();
                } catch (Exception e) {
                    throw I.quiet(e);
                }
            } else {
                return -1;
            }
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
        boolean repair(boolean async) {
            // ignore today's data
            if (Chrono.utcToday().toLocalDate().isEqual(date)) {
                return false;
            }

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
            LogHouse external = service.loghouse();
            if (external.has(date)) {
                readExternal(external).waitForTerminate().to(I.NoOP, I::error, () -> convertNormalToCompact(async));
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
                List<Execution> executions = new ArrayList(service.executionRequestLimit());

                while (!completed) {
                    // retrive the execution log from server
                    service.executions(id, id + service.executionRequestLimit()).waitForTerminate().toCollection(executions);

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

                        I.info(service + " repairs the execution log from " + valids.get(0).date + " (" + valids.size() + ").");
                    }

                    // clear all data to reuse the container
                    executions.clear();
                }
            } catch (Exception e) {
                throw I.quiet(e);
            }

            if (completed) {
                convertNormalToCompact(async);
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
}