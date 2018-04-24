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

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.Objects;

import com.univocity.parsers.csv.CsvParser;
import com.univocity.parsers.csv.CsvParserSettings;
import com.univocity.parsers.csv.CsvWriter;
import com.univocity.parsers.csv.CsvWriterSettings;

import cointoss.util.Chrono;
import cointoss.util.Span;
import filer.Filer;
import kiss.I;
import kiss.Signal;

/**
 * @version 2017/09/08 18:20:48
 */
public abstract class MarketLog {

    /** The file data format */
    private static final DateTimeFormatter fileName = DateTimeFormatter.ofPattern("yyyyMMdd");

    /** The market provider. */
    protected final MarketProvider provider;

    /** The root directory of logs. */
    protected final Path root;

    /** The first day. */
    protected ZonedDateTime cacheFirst;

    /** The last day. */
    protected ZonedDateTime cacheLast;

    /**
     * Create log manager.
     * 
     * @param provider
     */
    protected MarketLog(MarketProvider provider) {
        this.provider = Objects.requireNonNull(provider);
        this.root = Paths.get(".log").resolve(provider.orgnizationName()).resolve(provider.name());

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
     * Get the starting day of cache.
     * 
     * @return
     */
    public final ZonedDateTime getCacheStart() {
        return cacheFirst;
    }

    /**
     * Get the ending day of cache.
     * 
     * @return
     */
    public final ZonedDateTime getCacheEnd() {
        return cacheLast;
    }

    /**
     * Read date from the specified date.
     * 
     * @param start
     * @return
     */
    public abstract Signal<Execution> from(ZonedDateTime start);

    /**
     * Read data in realtime.
     * 
     * @return
     */
    public abstract Signal<Execution> realtime();

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
        return range(getCacheStart(), getCacheEnd());
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
        return range(Span.random(getCacheStart(), getCacheEnd().minusDays(1), days));
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
                observer.accept(previous = decode(row, hasCompact ? previous : null));
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
    protected final Path localCacheFile(ZonedDateTime date) {
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
                writer.writeRow(encode(current, previous));
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
        return file.resolveSibling(file.getFileName().toString().replace(".log", ".clog"));
    }

    /**
     * Build execution from log.
     * 
     * @param values
     * @return
     */
    protected abstract Execution decode(String[] values, Execution previous);

    /**
     * Build log from execution.
     * 
     * @param execution
     * @return
     */
    protected abstract String[] encode(Execution execution, Execution previous);
}
