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

import java.io.Serializable;
import java.nio.ByteBuffer;
import java.time.ZonedDateTime;

import org.h2.mvstore.MVMap;
import org.h2.mvstore.MVStore;
import org.h2.mvstore.WriteBuffer;
import org.h2.mvstore.type.BasicDataType;
import org.h2.mvstore.type.LongDataType;

import antibug.profiler.Benchmark;
import cointoss.ticker.Span;
import cointoss.ticker.TimeseriesStore;
import cointoss.ticker.data.TimeseriesData;
import cointoss.util.Chrono;
import psychopath.Locator;

public class StoreBench {
    public static void main(String[] args) {
        Benchmark benchmark = new Benchmark();

        long[] time = {0};
        MVStore store = new MVStore.Builder().fileName("test.db").compress().open();
        MVMap<Long, Orderbook> map = store.openMap("test", new MVMap.Builder().keyType(new LongDataType()).valueType(new BeanType()));

        benchmark.measure("Num", () -> {
            Orderbook book = new Orderbook();
            book.price = 10;
            book.size = 1.0f;
            book.time = time[0];
            time[0] += 3600;

            map.put(book.time, book);

            return book;
        });
        store.commit();

        benchmark.perform();
        System.out.println(map.sizeAsLong());
        System.out.println(map.get(0L).time);
        System.out.println(map.get(3600L).time);
    }

    public static void main2(String[] args) {
        Benchmark benchmark = new Benchmark();

        long[] time = {0};
        TimeseriesStore<Orderbook> store = TimeseriesStore.create(Orderbook.class, Span.Hour1).enableDiskStore(Locator.directory("test"));

        benchmark.measure("Num", () -> {
            Orderbook book = new Orderbook();
            book.price = 10;
            book.size = 1.0f;
            book.time = time[0];
            time[0] += 3600;

            store.store(book);

            return book;
        });

        benchmark.perform();
        System.out.println(store.size());
        System.out.println(store.at(0).time);
        System.out.println(store.at(3600).time);
    }

    static class Orderbook implements TimeseriesData, Serializable {
        public float price;

        public float size;

        public long time;

        /**
         * {@inheritDoc}
         */
        @Override
        public ZonedDateTime date() {
            return Chrono.utcBySeconds(time);
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public long epochSeconds() {
            return time;
        }
    }

    static class BeanType extends BasicDataType<Orderbook> {

        /**
         * {@inheritDoc}
         */
        @Override
        public Orderbook[] createStorage(int size) {
            return new Orderbook[size];
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public int getMemory(Orderbook obj) {
            return 16;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public void write(WriteBuffer buff, Orderbook o) {
            buff.putFloat(o.price).putFloat(o.size).putLong(o.time);
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public Orderbook read(ByteBuffer buff) {
            Orderbook o = new Orderbook();
            o.price = buff.getFloat();
            o.size = buff.getFloat();
            o.time = buff.getLong();
            return o;
        }
    }
}
