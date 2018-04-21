/*
 * Copyright (C) 2018 Nameless Production Committee
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          http://opensource.org/licenses/mit-license.php
 */
package cointoss.log;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;

import com.univocity.parsers.csv.CsvParser;
import com.univocity.parsers.csv.CsvParserSettings;
import com.univocity.parsers.csv.CsvWriter;
import com.univocity.parsers.csv.CsvWriterSettings;

import cointoss.Execution;
import cointoss.Side;
import cointoss.util.Num;
import filer.Filer;
import kiss.I;
import kiss.Signal;

/**
 * @version 2018/04/20 19:04:42
 */
public class Log {

    /**
     * Read {@link Execution} log.
     * 
     * @param file
     * @return
     */
    public Signal<Execution> read(Path file) {
        return new Signal<>((observer, disposer) -> {
            CsvParserSettings settings = new CsvParserSettings();
            settings.getFormat().setDelimiter(' ');

            CsvParser parser = new CsvParser(settings);
            parser.beginParsing(file.toFile());

            boolean isCompact = file.getFileName().toString().endsWith(".clog");

            String[] row;
            Execution previous = null;

            while ((row = parser.parseNext()) != null) {
                Execution current;

                if (previous == null || !isCompact) {
                    current = new Execution(row);
                } else {
                    current = new Execution();
                    current.id = previous.id + decode(row[0], 1);
                    current.exec_date = previous.exec_date.plus(decode(row[1], 0), ChronoUnit.MILLIS);
                    current.side = decode(row[2], previous.side);
                    current.price = decode(row[3], previous.price);
                    current.size = decodeSize(row[4], previous.size);
                    current.buy_child_order_acceptance_id = String.valueOf(decode(row[5], previous.buyer()));
                    current.sell_child_order_acceptance_id = String.valueOf(decode(row[6], previous.seller()));
                }
                observer.accept(previous = current);
            }

            parser.stopParsing();
            return disposer;
        });
    }

    private static long decode(String value, long defaults) {
        if (value == null) {
            return defaults;
        }
        return Long.parseLong(value);
    }

    private static Side decode(String value, Side defaults) {
        if (value == null) {
            return defaults;
        }
        return Side.parse(value);
    }

    private static Num decode(String value, Num defaults) {
        if (value == null) {
            return defaults;
        }
        return Num.of(value).plus(defaults);
    }

    private static Num decodeSize(String value, Num defaults) {
        if (value == null) {
            return defaults;
        }
        return Num.of(value).divide(Num.HUNDRED);
    }

    public void compact(Path file) {
        try {
            CsvParserSettings settings = new CsvParserSettings();
            settings.getFormat().setDelimiter(' ');

            CsvParser parser = new CsvParser(settings);
            parser.beginParsing(file.toFile());

            Path output = file.resolveSibling(file.getFileName().toString().replace(".log", ".clog"));
            CsvWriterSettings writerConfig = new CsvWriterSettings();
            writerConfig.getFormat().setDelimiter(' ');

            CsvWriter writer = new CsvWriter(output.toFile(), writerConfig);

            Execution previous = null;
            String[] row = null;
            while ((row = parser.parseNext()) != null) {
                if (previous == null) {
                    writer.writeRow(row);
                } else {
                    Execution exe = new Execution(row);

                    String id = encode(exe.id, previous.id, 1);
                    String time = encode(exe.exec_date, previous.exec_date);
                    String side = encode(exe.side.mark(), previous.side.mark());
                    String price = encode(exe.price, previous.price);
                    String size = exe.size.equals(previous.size) ? "" : exe.size.multiply(Num.HUNDRED).toString();
                    String buyer = encode(exe.buyer(), previous.buyer(), 0);
                    String seller = encode(exe.seller(), previous.seller(), 0);

                    writer.writeRow(id + " " + time + " " + side + " " + price + " " + size + " " + buyer + " " + seller);
                }
                previous = new Execution(row);
            }
            writer.close();
            parser.stopParsing();
        } catch (Exception e) {
            throw I.quiet(e);
        }
    }

    /**
     * Erase duplicated value.
     * 
     * @param current
     * @param previous
     * @param defaults
     * @return
     */
    private static String encode(Num current, Num previous) {
        if (current.equals(previous)) {
            return "";
        } else {
            return current.minus(previous).toString();
        }
    }

    /**
     * Erase duplicated value.
     * 
     * @param current
     * @param previous
     * @param defaults
     * @return
     */
    private static String encode(long current, long previous, long defaults) {
        long diff = current - previous;

        if (diff == defaults) {
            return "";
        } else {
            return String.valueOf(diff);
        }
    }

    /**
     * Erase duplicated value.
     * 
     * @param current
     * @param previous
     * @param defaults
     * @return
     */
    private static String encode(ZonedDateTime current, ZonedDateTime previous) {
        return encode(current.toInstant().toEpochMilli(), previous.toInstant().toEpochMilli(), 0);
    }

    /**
     * Erase duplicated sequence.
     * 
     * @param current
     * @param previous
     * @return
     */
    private static String encode(String current, String previous) {
        return current.equals(previous) ? "" : current;
    }

    public static void main(String[] args) {
        Path path = Paths.get("F:\\Development\\CoinToss\\.log\\bitflyer\\FX_BTC_JPY");
        Filer.walk(path, "execution20180404.log").to(file -> {
            Log log = new Log();
            log.compact(file);

            // long start = System.currentTimeMillis();
            // log.read(file).to(e -> {
            // });
            // long end = System.currentTimeMillis();
            // System.out.println(end - start);
        });
    }

}
