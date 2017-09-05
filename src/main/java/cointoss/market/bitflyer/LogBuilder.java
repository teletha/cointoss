/*
 * Copyright (C) 2017 Nameless Production Committee
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          http://opensource.org/licenses/mit-license.php
 */
package cointoss.market.bitflyer;

import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import cointoss.Execution;
import cointoss.Generator;
import cointoss.Side;
import eu.verdelhan.ta4j.Decimal;
import filer.Filer;
import kiss.I;

/**
 * @version 2017/08/23 14:48:43
 */
public class LogBuilder {

    /** date format for log */
    private static final DateTimeFormatter format = DateTimeFormatter.ofPattern("yyyyMMdd");

    /** The target type. */
    private final BitFlyerType type;

    /**
     * @param type
     */
    private LogBuilder(BitFlyerType type) {
        this.type = type;
    }

    /**
     * Build execution log for the specified market.
     * 
     * @param type
     */
    public void build() {
        List<Path> files = Filer.walk(Filer.locate(".log").resolve("bitflyer").resolve(type.name()), "execution2*.log");

        for (Path file : files) {
            fix(file);
        }

        LocalDate today = LocalDate.now();
        LocalDate latest = LocalDate.parse(files.get(files.size() - 1).getFileName().toString().substring(9, 17), format);

        while (latest.isBefore(today)) {
            latest = latest.plusDays(1);

            createLog(latest);
        }
    }

    /**
     * Create execution log file of the specified day.
     * 
     * @param target
     */
    private void createLog(LocalDate target) {
        Path path = file(target);
        long latest = -1;
        List<String> lines;

        if (Files.notExists(path)) {
            // read from previous log file
            lines = new ArrayList();
            latest = readLastId(readLines(target.minusDays(1)));
        } else {
            lines = readLines(target);
            latest = readLastId(lines);
        }

        final long initia = latest;

        try {
            root: for (int i = 0; i < 5000; i++) {
                URL url = new URL("https://api.bitflyer.jp/v1/executions?product_code=" + type
                        .name() + "&count=500&before=" + (initia + i * 500));
                Executions executions = I.json(url).to(Executions.class);
                System.out.println("Read " + latest + "   " + lines.size() + "   " + executions.get(executions.size() - 1).exec_date);

                for (int j = executions.size() - 1; 0 <= j; j--) {
                    Execution exe = executions.get(j);

                    if (latest < exe.id) {
                        if (exe.exec_date.toLocalDate().isEqual(target)) {
                            // target day
                            lines.add(exe.toString());
                            latest = exe.id;
                        } else {
                            // next day
                            break root;
                        }
                    }
                }
                Thread.sleep(700);
            }

            if (initia == latest) {
                // completed
            } else {
                // write
                Files.write(file(target), lines);

                // check
                // createLog(target);
            }
        } catch (Throwable e) {
            e.printStackTrace();
            throw I.quiet(e);
        }
    }

    /**
     * @param lines
     * @return
     */
    private long readLastId(List<String> lines) {
        return new Execution(lines.get(lines.size() - 1)).id;
    }

    /**
     * @param day
     * @return
     */
    private List<String> readLines(LocalDate day) {
        try {
            return Files.readAllLines(file(day));
        } catch (IOException e) {
            throw I.quiet(e);
        }
    }

    /**
     * @param day
     * @return
     */
    private Path file(LocalDate day) {
        return Filer.locate(".log").resolve("bitflyer").resolve(type.name()).resolve("execution" + format.format(day) + ".log");
    }

    /**
     * Fix incomplete log.
     * 
     * @param file
     */
    private void fix(Path file) {
        List<Execution> lines = Filer.read(file).map(Execution::new).toList();
        int initial = lines.size();

        for (int i = 0; i < lines.size() - 1; i++) {
            Execution e1 = lines.get(i);
            Execution e2 = lines.get(i + 1);

            // check id
            if (e2.id - e1.id < 5) {
                continue;
            }

            // check price
            if (e2.price.minus(e1.price).abs().isLessThanOrEqual(400)) {
                continue;
            }

            // check time
            long duration = Duration.between(e1.exec_date, e2.exec_date).getSeconds();

            if (duration < 30) {
                continue;
            }

            Execution complement = new Execution();
            complement.id = (e1.id + e2.id) / 2;
            complement.side = Side.random();
            complement.size = e1.size.plus(e2.size).dividedBy(2);
            complement.price = e1.price.plus(e2.price).dividedBy(2).integral();
            complement.exec_date = e1.exec_date.plusSeconds(duration / 2);
            complement.buy_child_order_acceptance_id = "Complement-" + format.format(e1.exec_date) + "-" + Generator
                    .randomInt(10000000, 99999999);
            complement.sell_child_order_acceptance_id = "Complement-" + format.format(e1.exec_date) + "-" + Generator
                    .randomInt(10000000, 99999999);

            System.out.println("Insert complement execution. " + complement);

            // insert complemented execution
            lines.add(++i, complement);
        }

        if (initial != lines.size()) {
            try {
                Files.write(file, lines.stream().map(Execution::toString).collect(Collectors.toList()));
            } catch (IOException e) {
                throw I.quiet(e);
            }
        }
    }

    public static void main(String[] args) {
        I.load(Decimal.Codec.class, false);

        new LogBuilder(BitFlyerType.FX_BTC_JPY).build();
    }
}
