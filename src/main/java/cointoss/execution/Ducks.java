/*
 * Copyright (C) 2024 Nameless Production Committee
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          http://opensource.org/licenses/mit-license.php
 */
package cointoss.execution;

import java.time.ZonedDateTime;

import cointoss.ticker.Tick;
import kiss.I;
import psychopath.Locator;
import typewriter.duck.DuckModel;
import typewriter.rdb.RDB;

public class Ducks {

    public static void main(String[] args) {
        I.load(Ducks.class);

        I.env("typewriter.duckdb", "jdbc:duckdb:" + Locator.file("test.db").absolutize());
        // I.env("typewriter.sqlite", "jdbc:sqlite:" + Locator.file("sqlite.db").absolutize());

        RDB<FastTick> db = RDB.of(FastTick.class);

        // Market market = Market.of(BitFlyer.FX_BTC_JPY);
        // market.tickers.on(Span.Minute1).close.to(tick -> {
        // FastTick fast = new FastTick(tick);
        // System.out.println(fast);
        // db.update(fast);
        // });
        // market.readLog(log -> log.from(Chrono.utc(2023, 1, 1)));

        // long start = System.currentTimeMillis();
        // Double max = db.max(FastTick::getHight);
        // Double min = db.min(FastTick::getLow);
        // System.out.println(System.currentTimeMillis() - start + " " + String.format("%.10f", max)
        // + " " + min);
        db.findBy("time, close, avg(close) over order by date rows between 4 precending and current row from ");
    }

    private static class FastTick extends DuckModel<FastTick> {

        public ZonedDateTime time;

        public double open;

        public double close;

        public double hight;

        public double low;

        public double buy;

        public double sell;

        public FastTick(Tick tick) {
            setId(tick.date().toEpochSecond());
            this.time = tick.date();
            this.open = tick.openPrice;
            this.close = tick.closePrice();
            this.hight = tick.highPrice();
            this.low = tick.lowPrice();
            this.buy = tick.longVolume();
            this.sell = tick.shortVolume();
        }

        /**
         * Get the hight property of this {@link Ducks.FastTick}.
         * 
         * @return The hight property.
         */
        public double getHight() {
            return hight;
        }

        /**
         * Set the hight property of this {@link Ducks.FastTick}.
         * 
         * @param hight The hight value to set.
         */
        public void setHight(double hight) {
            this.hight = hight;
        }

        /**
         * Get the low property of this {@link Ducks.FastTick}.
         * 
         * @return The low property.
         */
        public double getLow() {
            return low;
        }

        /**
         * Set the low property of this {@link Ducks.FastTick}.
         * 
         * @param low The low value to set.
         */
        public void setLow(double low) {
            this.low = low;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public String toString() {
            return "FastTick [time=" + time + ", open=" + open + ", close=" + close + ", hight=" + hight + ", low=" + low + ", buy=" + buy + ", sell=" + sell + "]";
        }
    }
}
