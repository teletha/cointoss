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
import psychopath.Locator;
import typewriter.rdb.RDB;

public class TickerCompare {

    public static void main(String[] args) {
        I.load(TickerDB.class);
        I.env("typewriter.duckdb", "jdbc:duckdb:duck.db");

        ZonedDateTime starting = Chrono.utc(2023, 6, 1);
        ZonedDateTime ending = Chrono.utc(2024, 7, 30);

        save(starting, ending);
        //
        // Market market = Market.of(BinanceFuture.FUTURE_BTC_USDT);
        //
        // AtomicInteger count = new AtomicInteger();
        //
        // Ticker ticker = market.tickers.on(Span.Hour1);
        //
        // market.tickers.add(market.log.range(starting, ending, LogType.Fast));
        //
        // System.out.println(ticker.ticks.size());
        //
        // long start = System.currentTimeMillis();
        // ticker.ticks.query(starting.toEpochSecond(), ending.toEpochSecond()).to(x -> {
        // count.incrementAndGet();
        // });
        // long end = System.currentTimeMillis();
        //
        // System.out.println("Feather " + (end - start) + " " + count.get());
        // count.set(0);
        //
        // RDB<TickerDBTick> db = RDB.of(TickerDBTick.class);
        // ticker.ticks.query(starting.toEpochSecond(), ending.toEpochSecond()).map(e -> {
        // TickerDBTick tick = new TickerDBTick();
        // tick.id = e.openTime;
        // tick.start = e.openPrice;
        // tick.close = e.closePrice;
        // tick.high = e.highPrice;
        // tick.low = e.lowPrice;
        // return tick;
        // }).buffer(24 * 2).to(e -> {
        // db.updateAll(e);
        // });
        //
        // Signal<TickerDBTick> query = db.findBy(TickerDBTick::getId, x ->
        // x.isLessThan(ending.toEpochSecond() - 60 * 60 * 24 * 30));
        // start = System.currentTimeMillis();
        // query.to(e -> {
        // count.incrementAndGet();
        // });
        // end = System.currentTimeMillis();
        //
        // System.out.println("QUERY " + (end - start) + " " + count.get());
    }

    private static void save(ZonedDateTime starting, ZonedDateTime ending) {
        Market market = Market.of(BinanceFuture.FUTURE_BTC_USDT);

        Ticker ticker = market.tickers.on(Span.Hour1);
        ticker.ticks.enableDiskStore(Locator.file("feather.db"));

        market.log.range(starting, ending, LogType.Fast).to(e -> {
            market.tickers.update(e);
        });
        ticker.ticks.commit();

        RDB<TickerDBTick> db = RDB.of(TickerDBTick.class);
        ticker.ticks.query(starting.toEpochSecond(), ending.toEpochSecond()).map(e -> {
            TickerDBTick tick = new TickerDBTick();
            tick.id = e.openTime;
            tick.start = e.openPrice;
            tick.close = e.closePrice;
            tick.high = e.highPrice;
            tick.low = e.lowPrice;
            return tick;
        }).buffer(24).to(e -> {
            db.updateAll(e);
        });
    }

    private static void read(ZonedDateTime starting, ZonedDateTime ending) {
        Market market = Market.of(BinanceFuture.FUTURE_BTC_USDT);

        Ticker ticker = market.tickers.on(Span.Hour1);
        ticker.ticks.enableDiskStore(Locator.file("feather.db"));

        AtomicInteger count = new AtomicInteger();
        long start = System.currentTimeMillis();

        ticker.ticks.query(starting.toEpochSecond(), ending.toEpochSecond()).to(e -> {
            count.incrementAndGet();
        });
        long end = System.currentTimeMillis();

        System.out.println("Feather " + (end - start) + "  " + count.get());
        count.set(0);

        RDB<TickerDBTick> db = RDB.of(TickerDBTick.class);
        Signal<TickerDBTick> query = db.findAll();
        start = System.currentTimeMillis();
        query.to(e -> {
            count.incrementAndGet();
        });
        end = System.currentTimeMillis();

        System.out.println("QUERY " + (end - start) + " " + count.get());
    }
}
