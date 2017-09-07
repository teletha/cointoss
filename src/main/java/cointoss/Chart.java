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

import java.time.Duration;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Consumer;

import eu.verdelhan.ta4j.Decimal;
import eu.verdelhan.ta4j.Tick;
import eu.verdelhan.ta4j.TimeSeries;

/**
 * @version 2017/09/07 22:12:13
 */
@SuppressWarnings("serial")
public class Chart extends TimeSeries {

    /** The chart duration. */
    private final Duration duration;

    /** The child delegator. */
    private final Chart child;

    /** The current tick */
    private MutableTick current;

    /** The tick listeners. */
    private final List<Consumer<Tick>> listeners = new CopyOnWriteArrayList();

    /**
     * 
     */
    Chart(Duration duration, Chart child) {
        super(duration.toString());

        this.duration = duration;
        this.child = child;
    }

    /**
     * Record executions.
     */
    void tick(Execution exe) {
        if (current == null) {
            current = new MutableTick(exe);
        }

        if (!exe.exec_date.isBefore(current.endTime)) {
            // notify
            for (Consumer<Tick> listener : listeners) {
                listener.accept(current);
            }

            // update
            current = new MutableTick(exe);
        }
        current.tick(exe);

        // propagate
        if (child != null) {
            child.tick(exe);
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
     * @version 2017/09/07 21:53:44
     */
    private class MutableTick implements Tick {

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
        * 
        */
        private MutableTick(Execution exe) {
            beginTime = exe.exec_date.withSecond(0).withNano(0);
            endTime = beginTime.plus(duration);
            openPrice = exe.price;

            addTick(this);
        }

        /**
        * 
        */
        private MutableTick(MutableTick exe) {
            beginTime = exe.beginTime;
            endTime = beginTime.plus(duration);
            openPrice = exe.openPrice;

            addTick(this);
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
            return duration;
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
    }

}
