/*
 * Copyright (C) 2023 The COINTOSS Development Team
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          https://opensource.org/licenses/MIT
 */
package cointoss.util.feather;

import java.nio.ByteBuffer;

import antibug.CleanRoom;
import antibug.profiler.Benchmark;
import cointoss.ticker.Span;
import cointoss.util.feather.FeatherStoreTest.Value;
import psychopath.Locator;

public class FeatherStoreBenchmark {

    public static void main(String[] args) {
        Benchmark benchmark = new Benchmark();
        Span span = Span.Minute1;
        CleanRoom room = new CleanRoom();

        FeatherStore<Value> disk = FeatherStore.create(Value.class, span)
                .enableDiskStore(Locator.file(room.locateFile("persist.db")), optimized);
        benchmark.measure("Store on disk", () -> {
            for (int i = 0; i < 10000; i++) {
                disk.store(new Value(i * span.seconds));
            }
            return disk.size();
        });

        benchmark.measure("Read from disk", () -> {
            for (int i = 0; i < 10000; i++) {
                disk.at(i * span.seconds);
            }
            return 10;
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

    private static final DataCodec<Value> optimized = new DataCodec<Value>() {

        @Override
        public int size() {
            return 8;
        }

        @Override
        public void write(Value item, ByteBuffer buffer) {
            buffer.putLong(item.value);
        }

        @Override
        public Value read(long time, ByteBuffer buffer) {
            return new Value(buffer.getLong());
        }
    };
}