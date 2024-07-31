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

import java.time.ZonedDateTime;
import java.util.concurrent.atomic.AtomicInteger;

import cointoss.Market;
import cointoss.execution.LogType;
import cointoss.market.binance.BinanceFuture;
import cointoss.util.Chrono;
import kiss.I;
import kiss.Signal;
import typewriter.rdb.RDB;

public class TickerCompare {

    public static void main(String[] args) {
        I.load(TickerDB.class);
        I.env("typewriter.sqlite", "jdbc:sqlite:market.sqlite");

        ZonedDateTime starting = Chrono.utc(2020, 6, 1);
        ZonedDateTime ending = Chrono.utc(2024, 7, 27);

        save(starting, ending, null);
    }

    private static void save(ZonedDateTime starting, ZonedDateTime ending, RDB<Tick> db) {
        Market market = Market.of(BinanceFuture.FUTURE_BTC_USDT);

        market.readLog(x -> x.range(starting, ending, LogType.Fast));
    }

    private static void read(ZonedDateTime starting, ZonedDateTime ending, RDB<Tick> db) {
        AtomicInteger count = new AtomicInteger();

        Signal<Tick> query = db.findAll();
        long start = System.currentTimeMillis();
        query.to(e -> {
            System.out.println(e);
            count.incrementAndGet();
        });
        long end = System.currentTimeMillis();

        System.out.println("QUERY " + (end - start) + " " + count.get());
    }
}
