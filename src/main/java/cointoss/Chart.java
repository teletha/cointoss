/*
 * Copyright (C) 2017 Nameless Production Committee
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          http://opensource.org/licenses/mit-license.php
 */
package cointoss;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Consumer;
import java.util.stream.IntStream;

import eu.verdelhan.ta4j.BaseTimeSeries;
import eu.verdelhan.ta4j.Decimal;
import eu.verdelhan.ta4j.Tick;
import filer.Filer;
import kiss.Decoder;
import kiss.Encoder;
import kiss.I;

/**
 * @version 2017/09/07 22:12:13
 */
@SuppressWarnings("serial")
public class Chart extends BaseTimeSeries {

    static {
        I.load(TickCodec.class, false);
    }

    /** The chart duration. */
    private final Duration duration;

    /** The child delegator. */
    private final Chart[] children;

    /** The current tick */
    private MutableTick current;

    /** The tick listeners. */
    private final List<Consumer<Tick>> listeners = new CopyOnWriteArrayList();

    /**
     * 
     */
    public Chart(Duration duration, Chart... children) {
        super(duration.toString());

        this.duration = duration;
        this.children = children;
        setMaximumTickCount(60 * 60 * 24 * 3);
    }

    /**
     * Detect trend.
     * 
     * @return
     */
    public boolean isUpTrend() {
        return false;
    }

    /**
     * Detect trend.
     * 
     * @return
     */
    public boolean isDownTrend() {
        return false;
    }

    /**
     * Detect trend.
     * 
     * @return
     */
    public boolean isRange() {
        return false;
    }

    /**
     * Record executions.
     */
    public void tick(Execution exe) {
        if (current == null) {
            current = new MutableTick(exe, duration);
            addTick(current);
        }

        if (!exe.exec_date.isBefore(current.endTime)) {
            // notify
            for (Consumer<Tick> listener : listeners) {
                listener.accept(current);
            }

            // update
            current = new MutableTick(exe, duration);
            addTick(current);
        }
        current.tick(exe);

        // propagate
        if (children != null) {
            for (Chart child : children) {
                child.tick(exe);
            }
        }
    }

    /**
     * Observe tick.
     * 
     * @param object
     */
    public void to(Consumer<Tick> listener) {
        listeners.add(listener);
    }

    /**
     * <p>
     * Write out the current tick log to the specified file
     * </p>
     * 
     * @param file
     */
    public void writeTo(Path file) {
        List<String> ticks = I.signal(IntStream.range(0, getTickCount())).map(this::getTick).map(tick -> tick.toString()).toList();

        try {
            Files.createDirectories(file.getParent());
            Files.write(file, ticks);
        } catch (IOException e) {
            throw I.quiet(e);
        }
    }

    /**
     * <p>
     * Read tick log from the specified file.
     * </p>
     * 
     * @param file
     */
    public void readFrom(Path file) {
        Filer.read(file).map(line -> new MutableTick(line)).to(this::addTick);
        System.out.println(getTickCount());
    }

    /**
     * @version 2017/09/07 21:53:44
     */
    private static class MutableTick implements Tick {

        /** Begin time of the tick */
        public final ZonedDateTime beginTime;

        /** End time of the tick */
        public final ZonedDateTime endTime;

        /** Open price of the period */
        public final Decimal openPrice;

        /** Close price of the period */
        public Decimal closePrice = null;

        /** Max price of the period */
        public Decimal maxPrice = Decimal.ZERO;

        /** Min price of the period */
        public Decimal minPrice = Decimal.MAX;

        /** Traded amount during the period */
        public Decimal amount = Decimal.ZERO;

        /** Volume of the period */
        public Decimal volume = Decimal.ZERO;

        /** Trade count */
        protected int trades = 0;

        /**
         * Decode.
         * 
         * @param value
         */
        private MutableTick(String value) {
            String[] values = value.split(" ");

            beginTime = ZonedDateTime.parse(values[0]);
            endTime = ZonedDateTime.parse(values[1]);
            openPrice = Decimal.valueOf(values[2]);
            closePrice = Decimal.valueOf(values[3]);
            maxPrice = Decimal.valueOf(values[4]);
            minPrice = Decimal.valueOf(values[5]);
            volume = Decimal.valueOf(values[6]);
        }

        /**
        * 
        */
        private MutableTick(Execution exe, Duration duration) {
            beginTime = exe.exec_date.withSecond(0).withNano(0);
            endTime = beginTime.plus(duration);
            openPrice = exe.price;
        }

        /**
        * 
        */
        private MutableTick(MutableTick exe, Duration duration) {
            beginTime = exe.beginTime;
            endTime = beginTime.plus(duration);
            openPrice = exe.openPrice;
        }

        /**
         * Assign date.
         * 
         * @param exe
         */
        private void tick(Execution exe) {
            closePrice = exe.price;
            maxPrice = maxPrice.max(exe.price);
            minPrice = minPrice.min(exe.price);
            volume = volume.plus(exe.size);
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public Decimal getOpenPrice() {
            return openPrice;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public Decimal getMinPrice() {
            return minPrice;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public Decimal getMaxPrice() {
            return maxPrice;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public Decimal getClosePrice() {
            return closePrice;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public Decimal getVolume() {
            return volume;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public int getTrades() {
            return trades;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public Decimal getAmount() {
            return amount;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public Duration getTimePeriod() {
            return Duration.between(beginTime, endTime);
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public ZonedDateTime getBeginTime() {
            return beginTime;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public ZonedDateTime getEndTime() {
            return endTime;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public void addTrade(Decimal tradeVolume, Decimal tradePrice) {
            // If this exception will be thrown, it is bug of this program. So we must rethrow the
            // wrapped error in here.
            throw new Error();
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public String toString() {
            StringBuilder builder = new StringBuilder();
            builder.append(beginTime)
                    .append(" ")
                    .append(endTime)
                    .append(" ")
                    .append(openPrice)
                    .append(" ")
                    .append(closePrice)
                    .append(" ")
                    .append(maxPrice)
                    .append(" ")
                    .append(minPrice)
                    .append(" ")
                    .append(volume);

            return builder.toString();
        }
    }

    /**
     * @version 2017/09/09 11:31:55
     */
    private static class TickCodec implements Encoder<Tick>, Decoder<Tick> {

        /**
         * {@inheritDoc}
         */
        @Override
        public String encode(Tick value) {
            return value.toString();
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public Tick decode(String value) {
            return new MutableTick(value);
        }
    }
}
