/*
 * Copyright (C) 2019 Nameless Production Committee
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          http://opensource.org/licenses/mit-license.php
 */
package cointoss.util.feather;

import java.nio.ByteBuffer;

import antibug.CleanRoom;
import antibug.profiler.Benchmark;
import cointoss.ticker.Span;
import cointoss.util.feather.FeatherStoreTest.Value;

public class FeatherStoreBenchmark {

    public static void main(String[] args) {
        Benchmark benchmark = new Benchmark();
        Span span = Span.Minute1;
        CleanRoom room = new CleanRoom();

        FeatherStore<Value> disk = FeatherStore.create(Value.class, span).enableDiskStore(room.locateFile("persist.db"), optimized);
        benchmark.measure("Store on disk", () -> {
            for (int i = 0; i < 10000; i++) {
                disk.store(new Value(i * span.seconds));
            }
            return disk.size();
        });

        FeatherStore<Value> memory = FeatherStore.create(Value.class, span);
        benchmark.measure("Store on memory", () -> {
            for (int i = 0; i < 10000; i++) {
                memory.store(new Value(i * span.seconds));
            }
            return memory.size();
        });

        benchmark.perform();
    }

    private static final DataType<Value> optimized = new DataType<Value>() {

        @Override
        public int size() {
            return 4;
        }

        @Override
        public void write(Value item, ByteBuffer buffer) {
            buffer.putInt(item.value);
        }

        @Override
        public Value read(ByteBuffer buffer) {
            return new Value(buffer.getInt());
        }
    };
}
