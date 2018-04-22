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
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;

import com.univocity.parsers.csv.CsvParser;
import com.univocity.parsers.csv.CsvParserSettings;
import com.univocity.parsers.csv.CsvWriter;
import com.univocity.parsers.csv.CsvWriterSettings;

import cointoss.util.Chrono;
import cointoss.util.Span;
import kiss.I;
import kiss.Signal;

/**
 * @version 2017/09/08 18:20:48
 */
public abstract class MarketLog {

    /**
     * Get the starting day of cache.
     * 
     * @return
     */
    public abstract ZonedDateTime getCacheStart();

    /**
     * Get the ending day of cache.
     * 
     * @return
     */
    public abstract ZonedDateTime getCacheEnd();

    /**
     * Locate cache directory.
     * 
     * @return
     */
    public abstract Path cacheRoot();

    /**
     * Read date from the specified date.
     * 
     * @param start
     * @return
     */
    public abstract Signal<Execution> from(ZonedDateTime start);

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
