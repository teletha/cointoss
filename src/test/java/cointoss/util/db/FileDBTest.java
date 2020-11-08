/*
 * Copyright (C) 2019 Nameless Production Committee
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          http://opensource.org/licenses/mit-license.php
 */
package cointoss.util.db;

import java.util.Random;

import org.junit.jupiter.api.Test;

import cointoss.ticker.Span;

public class FileDBTest {

    @Test
    void db() throws InterruptedException {
        TableDefinition<Hourly> definition = new TableDefinition<>(Span.Hour1, Hourly.class, v -> v.seconds);
        FileDB<Hourly> db = definition.build("test");

        long base = Span.Hour1.seconds;

        for (int i = 1; i < 100000000; i++) {
            db.insert(new Hourly(base * i));
        }

        // db.range(base * 0, 10000000).map(e -> e.high).scanWith(0d, (o, p) -> Math.max(o,
        // p)).last().to(e -> {
        // System.out.println(e);
        // });
    }

    private static class Hourly {

        static Random R = new Random();

        public long seconds;

        public double open = R.nextDouble();

        public double close = R.nextDouble();

        public double high = R.nextDouble();

        public double low = R.nextDouble();

        public Hourly(long start) {
            this.seconds = start;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public String toString() {
            return "Hourly [seconds=" + seconds + ", open=" + open + ", close=" + close + ", high=" + high + ", low=" + low + "]";
        }
    }
}
