/*
 * Copyright (C) 2024 Nameless Production Committee
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          http://opensource.org/licenses/mit-license.php
 */
package cointoss.ticker;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.ZonedDateTime;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import cointoss.Market;
import cointoss.execution.Execution;
import cointoss.execution.LogType;
import cointoss.market.binance.BinanceFuture;
import cointoss.util.Chrono;
import hypatia.Num;
import kiss.I;
import kiss.Signal;
import typewriter.rdb.RDB;
import typewriter.rdb.RDBCodec;

public class TickerDB {

    public static void main(String[] args) {
        I.load(TickerDB.class);
        I.env("typewriter.sqlite", "jdbc:duckdb::test");

        ZonedDateTime s = Chrono.utc(2024, 6, 1);
        ZonedDateTime ee = Chrono.utc(2024, 6, 3);

        Market market = Market.of(BinanceFuture.FUTURE_BTC_USDT);

        AtomicInteger count = new AtomicInteger();

        Signal<Execution> exes = market.log.range(s, ee, LogType.Normal);
        long start = System.currentTimeMillis();
        exes.to(e -> {
            count.incrementAndGet();
        });
        long end = System.currentTimeMillis();

        System.out.println("NORMAL " + (end - start) + " " + count.get());
        count.set(0);

        exes = market.log.range(s, ee, LogType.Fast);
        start = System.currentTimeMillis();
        exes.to(e -> {
            count.incrementAndGet();
        });
        end = System.currentTimeMillis();

        System.out.println("FAST " + (end - start) + "  " + count.get());
        count.set(0);

        RDB<TickerDBExecution> db = RDB.of(TickerDBExecution.class);
        exes = market.log.range(s, ee, LogType.Normal);
        exes.map(e -> {
            TickerDBExecution exe = new TickerDBExecution();
            exe.id = e.id;
            exe.time = e.mills;
            exe.price = e.price;
            exe.size = e.size;
            exe.delay = e.delay;
            exe.direction = e.orientation;

            return exe;
        }).buffer(10000).to(e -> {
            db.updateAll(e);
        });

        ZonedDateTime utc = Chrono.utc(2024, 6, 1);

        Signal<TickerDBExecution> query = db.findAll();
        start = System.currentTimeMillis();
        query.to(e -> {
            count.incrementAndGet();
        });
        end = System.currentTimeMillis();

        System.out.println("QUERY " + (end - start) + "  " + count.get());
    }

    private static class C extends RDBCodec<Num> {

        public C() {
            super(double.class);
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public void encode(Map<String, Object> result, String name, Num value) {
            result.put(name, value.doubleValue());
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public Num decode(ResultSet result, String name) throws SQLException {
            return Num.of(result.getDouble(name));
        }
    }
}
