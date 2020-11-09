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
import kiss.I;

public class FileDBTest {

    @Test
    void db() throws InterruptedException {
        FeatherDefinition<Hourly> definition = FeatherDB.define(Span.Hour1, Hourly.class, v -> v.seconds).maxDataFileSize(1024);

        FeatherDB<Hourly> db = definition.createTable("test");

        long base = Span.Hour1.seconds;

        I.signal(1000).recurse(i -> i + 1).take(1000).map(i -> new Hourly(i * base)).buffer(100).to(items -> {
            System.out.println(items.get(0));
            db.insert(items);
        });

        db.after(base * 0, 2000).effect(e -> System.out.println(e)).map(e -> e.high).scanWith(0f, (o, p) -> Math.max(o, p)).last().to(e -> {
            System.out.println(e);
        });
    }

    private static class Hourly {

        static Random R = new Random();

        public long seconds;

        public float open = R.nextFloat() * 100;

        public float close = R.nextFloat() * 100;

        public float high = R.nextFloat() * 100;

        public float low = R.nextFloat() * 100;

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
