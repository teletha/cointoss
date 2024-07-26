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
        I.env("typewriter.sqlite", "jdbc:duckdb::test");

        ZonedDateTime s = Chrono.utc(2024, 6, 1);
        ZonedDateTime ee = Chrono.utc(2024, 6, 30);

        Market market = Market.of(BinanceFuture.FUTURE_BTC_USDT);

        AtomicInteger count = new AtomicInteger();

        Ticker ticker = market.tickers.on(Span.Hour1);

        market.tickers.add(market.log.range(s, ee, LogType.Fast));

        System.out.println(ticker.ticks.size());

        long start = System.currentTimeMillis();
        ticker.ticks.query(s.toEpochSecond(), ee.toEpochSecond()).to(x -> {
            count.incrementAndGet();
        });
        long end = System.currentTimeMillis();

        System.out.println("Feather " + (end - start) + "   " + count.get());
        count.set(0);

        RDB<TickerDBTick> db = RDB.of(TickerDBTick.class);
        ticker.ticks.query(s.toEpochSecond(), ee.toEpochSecond()).map(e -> {
            TickerDBTick tick = new TickerDBTick();
            tick.time = e.openTime;
            tick.start = e.openPrice;
            tick.end = e.closePrice;
            tick.high = e.highPrice;
            tick.low = e.lowPrice;

            return tick;
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
}
