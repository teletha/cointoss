/*
 * Copyright (C) 2023 Nameless Production Committee
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          http://opensource.org/licenses/mit-license.php
 */
package cointoss.util;

import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import cointoss.MarketService;
import kiss.Scheduler;
import kiss.WiseRunnable;

public class Mediator {

    public static final WithoutContext TickerGenerator = new WithoutContext("TickerGenerator");

    public static final WithContext<MarketService> ExecutionCollector = new WithContext("ExecutionCollector");

    public static final WithoutContext ExecutionWriter = new WithoutContext("ExecutionWriter");

    /**
     * Base task handler.
     */
    protected static class TaskType {

        /** The task type. */
        protected final String type;

        /** The thread pool manager. */
        private final ConcurrentHashMap<Object, Scheduler> executors = new ConcurrentHashMap();

        /**
         * Define your task type.
         * 
         * @param type
         */
        protected TaskType(String type) {
            this.type = type;
        }

        /**
         * Get the task executor for the specified key.
         * 
         * @return
         */
        @SuppressWarnings("resource")
        protected final ScheduledExecutorService findExecutor(Object key) {
            Objects.requireNonNull(key);

            return executors.computeIfAbsent(Objects.hash(type, key), hash -> new Scheduler(1));
        }

        /**
         * Schedule your task actually.
         * 
         * @param delay
         * @param interval
         * @param key
         * @param task
         * @return
         */
        protected final ScheduledFuture<?> scheduleTask(long delay, long interval, Object key, WiseRunnable task) {
            ScheduledExecutorService scheduler = findExecutor(key);

            if (interval <= 0) {
                return scheduler.schedule(task, delay, TimeUnit.MILLISECONDS);
            } else {
                return scheduler.scheduleWithFixedDelay(task, delay, interval, TimeUnit.MILLISECONDS);
            }
        }
    }

    /**
     * Task mediator on application context.
     */
    public static class WithoutContext extends TaskType {

        WithoutContext(String name) {
            super(name);
        }

        /**
         * Request your task which will be exected only once immediately.
         * 
         * @param task
         * @return
         */
        public ScheduledFuture<?> schedule(WiseRunnable task) {
            return schedule(0, 0, task);
        }

        /**
         * Request your task which will be executed only once after the specified delay.
         * 
         * @param delay
         * @param task
         * @return
         */
        public ScheduledFuture<?> schedule(long delay, WiseRunnable task) {
            return schedule(delay, 0, task);
        }

        /**
         * Request your task which will be executed repeatedly after the specified delay.
         * 
         * @param delay
         * @param interval
         * @param task
         * @return
         */
        public ScheduledFuture<?> schedule(long delay, long interval, WiseRunnable task) {
            return scheduleTask(delay, interval, Mediator.class, task);
        }
    }

    /**
     * Task mediator on your context.
     */
    public static class WithContext<C> extends TaskType {

        WithContext(String name) {
            super(name);
        }

        /**
         * Request your task which will be exected only once immediately.
         * 
         * @param task
         * @return
         */
        public ScheduledFuture<?> schedule(C context, WiseRunnable task) {
            return schedule(0, 0, context, task);
        }

        /**
         * Request your task which will be executed only once after the specified delay.
         * 
         * @param delay
         * @param task
         * @return
         */
        public ScheduledFuture<?> schedule(long delay, C context, WiseRunnable task) {
            return schedule(delay, 0, context, task);
        }

        /**
         * Request your task which will be executed repeatedly after the specified delay.
         * 
         * @param delay
         * @param interval
         * @param task
         * @return
         */
        public ScheduledFuture<?> schedule(long delay, long interval, C context, WiseRunnable task) {
            return scheduleTask(delay, interval, context, task);
        }
    }
}
