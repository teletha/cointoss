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

import com.univocity.parsers.csv.CsvParser;
import com.univocity.parsers.csv.CsvParserSettings;
import com.univocity.parsers.csv.CsvWriter;
import com.univocity.parsers.csv.CsvWriterSettings;

import cointoss.Execution;
import cointoss.util.Num;
import filer.Filer;
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

            String[] row;
            while ((row = parser.parseNext()) != null) {
                observer.accept(new Execution(row));
            }

            parser.stopParsing();
            return disposer;
        });
    }

    public void compact(Path file) {
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

        parser.stopParsing();
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
        });
    }

}
