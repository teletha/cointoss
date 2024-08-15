/*
 * Copyright (C) 2024 Nameless Production Committee
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          http://opensource.org/licenses/mit-license.php
 */
package cointoss.util;

import java.util.Collection;
import java.util.Map.Entry;
import java.util.concurrent.Delayed;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import com.google.common.collect.TreeMultimap;

import cointoss.MarketService;
import cointoss.market.Exchange;
import kiss.I;
import kiss.Signaling;

public enum JobType {

    ExecutionLogWriter(1, 3, "Saved execution log"),

    TickerWriter(5, 5, "Saved ticker");

    /** NOOP TASK */
    public static final ScheduledFuture<Object> NOOP = new ScheduledFuture<Object>() {

        /**
         * {@inheritDoc}
         */
        @Override
        public int compareTo(Delayed o) {
            return 0;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public boolean cancel(boolean mayInterruptIfRunning) {
            return false;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public boolean isCancelled() {
            return false;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public boolean isDone() {
            return false;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public Object get() throws InterruptedException, ExecutionException {
            return null;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public Object get(long timeout, TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
            return null;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public long getDelay(TimeUnit unit) {
            return 0;
        }
    };

    /** The initial delay. */
    final long initialDelayMinutes;

    /** The scheduling interval. */
    final long intervalDelayMinutes;

    /** The actual scheduler. */
    final ScheduledExecutorService scheduler;

    /** The message aggregator. */
    final Signaling<MarketService> aggregator = new Signaling();

    /**
     * @param initialDelayMinutes
     * @param intervalDelayMinutes
     */
    JobType(long initialDelayMinutes, long intervalDelayMinutes, String log) {
        this.initialDelayMinutes = initialDelayMinutes;
        this.intervalDelayMinutes = intervalDelayMinutes;
        this.scheduler = Executors.newSingleThreadScheduledExecutor(run -> {
            Thread thread = new Thread(run);
            thread.setName(toString());
            thread.setDaemon(true);
            return thread;
        });
        this.aggregator.expose.debounceAll(10, TimeUnit.SECONDS).to(services -> {
            TreeMultimap<Exchange, String> map = TreeMultimap.create();

            for (MarketService service : services) {
                map.put(service.exchange, service.marketName);
            }

            for (Entry<Exchange, Collection<String>> entry : map.asMap().entrySet()) {
                I.info(log + " for " + entry.getValue().size() + " markets in " + entry.getKey() + ". " + entry.getValue());
            }
        });
    }

    public ScheduledFuture<?> schedule(MarketService service, Runnable job) {
        return scheduler.scheduleWithFixedDelay(() -> {
            try {
                job.run();
            } finally {
                aggregator.accept(service);
            }
        }, initialDelayMinutes, intervalDelayMinutes, TimeUnit.MINUTES);
    }
}
