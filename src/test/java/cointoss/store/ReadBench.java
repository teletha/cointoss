/*
 * Copyright (C) 2021 Nameless Production Committee
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          http://opensource.org/licenses/mit-license.php
 */
package cointoss.store;

import antibug.profiler.Benchmark;
import cointoss.store.StoreBench.Orderbook;
import cointoss.ticker.Span;
import cointoss.ticker.FeatherStore;
import psychopath.Locator;

public class ReadBench {
    // public static void main2(String[] args) {
    // Benchmark benchmark = new Benchmark();
    //
    // MVStore store = new MVStore.Builder().fileName("test.db").compress().open();
    // MVMap<Long, Orderbook> map = store.openMap("test");
    //
    // benchmark.measure("Num", () -> {
    // long v = 10;
    // Cursor<Long, Orderbook> cursor = map.cursor(0L, 3600L * 100000, false);
    // while (cursor.hasNext()) {
    // v = +cursor.next();
    // }
    // return v;
    // });
    // store.commit();
    //
    // benchmark.perform();
    // }

    public static void main(String[] args) {
        Benchmark benchmark = new Benchmark();

        FeatherStore<Orderbook> store = FeatherStore.create(Orderbook.class, Span.Hour1).enableDiskStore(Locator.file("test.db"));
        System.out.println(store.at(0));

        benchmark.measure("Num", () -> {
            long v = 10;;
            for (int i = 0; i < 1000; i++) {
                v += store.at(3600L * i).time;
            }
            return v;
        });

        benchmark.perform();
    }
}
