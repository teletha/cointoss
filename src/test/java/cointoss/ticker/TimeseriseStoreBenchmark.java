/*
 * Copyright (C) 2019 Nameless Production Committee
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          http://opensource.org/licenses/mit-license.php
 */
package cointoss.ticker;

import antibug.CleanRoom;
import antibug.profiler.Benchmark;
import cointoss.ticker.TimeseriesStoreTest.Value;

public class TimeseriseStoreBenchmark {

    public static void main(String[] args) {
        Benchmark benchmark = new Benchmark();
        Span span = Span.Minute1;
        CleanRoom room = new CleanRoom();

        // TimeseriesStore<Value> memory = TimeseriesStore.create(Value.class, span);
        // benchmark.measure("Store without persist", () -> {
        // for (int i = 0; i < 10000; i++) {
        // memory.store(new Value(i * span.seconds));
        // }
        // return memory.size();
        // });

        TimeseriesStore<Value> disk = TimeseriesStore.create(Value.class, span).enableDiskStore(room.locateDirectory("persist"));
        benchmark.measure("Store with persist", () -> {
            for (int i = 0; i < 10000; i++) {
                disk.store(new Value(i * span.seconds));
            }
            return disk.size();
        });

        // ConcurrentNavigableLongMap<Long> map = LongMap.createSortedMap();
        // List<Long> collect = LongStream.range(489600, 489600 +
        // 1000).boxed().collect(Collectors.toList());
        // List<Long> randomed = new ArrayList(collect);
        // Collections.shuffle(randomed);
        //
        // benchmark.measure("Remove", () -> {
        // for (Long i : randomed) {
        // map.put(i.longValue(), i);
        // }
        //
        // for (Long i : randomed) {
        // map.remove(i.longValue());
        // }
        // return 10;
        // });

        benchmark.perform();
    }
}
