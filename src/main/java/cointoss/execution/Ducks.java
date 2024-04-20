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
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
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
        // market.tickers.on(Span.Minute1).close.map(FastTick::new).buffer(60 * 24).to(list -> {
        // System.out.println(list.stream().map(x -> x.time).toList());
        // });
        // market.readLog(log -> log.from(Chrono.utc(2024, 1, 1)));

        long start = System.currentTimeMillis();
        // db.findAll().buffer(60 * 5 * 23, 1).to(ticks -> {
        // double total = 0;
        // for (FastTick tick : ticks) {
        // total += tick.close;
        // }
        // });
        // db.avg(FastTick::getClose, o -> o.frame(-60 * 5 * 23, 0)).to(avg -> {
        // System.out.println(avg);
        // });

        // db.query().select("id/14400").fromCurrentTable().groupBy("id / 14400").qurey().to(x -> {
        // try {
        // System.out.println(x.getLong(1));
        // } catch (Exception e) {
        // // TODO: handle exception
        // }
        // });
        db.writer(w -> {
            w.select(m -> m.getId());
        });

        System.out.println(System.currentTimeMillis() - start);
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
         * Get the time property of this {@link Ducks.FastTick}.
         * 
         * @return The time property.
         */
        public ZonedDateTime getTime() {
            return time;
        }

        /**
         * Set the time property of this {@link Ducks.FastTick}.
         * 
         * @param time The time value to set.
         */
        public void setTime(ZonedDateTime time) {
            this.time = time;
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
         * Get the open property of this {@link Ducks.FastTick}.
         * 
         * @return The open property.
         */
        public double getOpen() {
            return open;
        }

        /**
         * Set the open property of this {@link Ducks.FastTick}.
         * 
         * @param open The open value to set.
         */
        public void setOpen(double open) {
            this.open = open;
        }

        /**
         * Get the close property of this {@link Ducks.FastTick}.
         * 
         * @return The close property.
         */
        public double getClose() {
            return close;
        }

        /**
         * Set the close property of this {@link Ducks.FastTick}.
         * 
         * @param close The close value to set.
         */
        public void setClose(double close) {
            this.close = close;
        }

        /**
         * Get the buy property of this {@link Ducks.FastTick}.
         * 
         * @return The buy property.
         */
        public double getBuy() {
            return buy;
        }

        /**
         * Set the buy property of this {@link Ducks.FastTick}.
         * 
         * @param buy The buy value to set.
         */
        public void setBuy(double buy) {
            this.buy = buy;
        }

        /**
         * Get the sell property of this {@link Ducks.FastTick}.
         * 
         * @return The sell property.
         */
        public double getSell() {
            return sell;
        }

        /**
         * Set the sell property of this {@link Ducks.FastTick}.
         * 
         * @param sell The sell value to set.
         */
        public void setSell(double sell) {
            this.sell = sell;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public String toString() {
            return "FastTick [time=" + time + ", open=" + open + ", close=" + close + ", hight=" + hight + ", low=" + low + ", buy=" + buy + ", sell=" + sell + "]";
        }
    }

    @Entity
    private static class SpringTick {

        @Id
        public long epochSeconds;

        public double open;

        public double close;

        public double hight;

        public double low;

        public double buy;

        public double sell;
    }
}
