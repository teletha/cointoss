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

    public static final WithoutContext LogCompacter = new WithoutContext("LogCompacter");

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

            // Scheduler(1) limits the number of concurrent executions to a maximum of 1, so this
            // effectively functions as a task queue.
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
        protected final ScheduledFuture<?> scheduleTask(long delay, long interval, TimeUnit unit, Object key, WiseRunnable task) {
            ScheduledExecutorService scheduler = findExecutor(key);

            if (interval <= 0) {
                return scheduler.schedule(task, delay, unit);
            } else {
                return scheduler.scheduleWithFixedDelay(task, delay, interval, unit);
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
            return schedule(0, 0, TimeUnit.SECONDS, task);
        }

        /**
         * Request your task which will be executed only once after the specified delay.
         * 
         * @param delay
         * @param task
         * @return
         */
        public ScheduledFuture<?> schedule(long delay, TimeUnit unit, WiseRunnable task) {
            return schedule(delay, 0, unit, task);
        }

        /**
         * Request your task which will be executed repeatedly after the specified delay.
         * 
         * @param delay
         * @param interval
         * @param task
         * @return
         */
        public ScheduledFuture<?> schedule(long delay, long interval, TimeUnit unit, WiseRunnable task) {
            return scheduleTask(delay, interval, unit, Mediator.class, task);
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
            return schedule(0, 0, TimeUnit.SECONDS, context, task);
        }

        /**
         * Request your task which will be executed only once after the specified delay.
         * 
         * @param delay
         * @param task
         * @return
         */
        public ScheduledFuture<?> schedule(long delay, TimeUnit unit, C context, WiseRunnable task) {
            return schedule(delay, 0, unit, context, task);
        }

        /**
         * Request your task which will be executed repeatedly after the specified delay.
         * 
         * @param delay
         * @param interval
         * @param task
         * @return
         */
        public ScheduledFuture<?> schedule(long delay, long interval, TimeUnit unit, C context, WiseRunnable task) {
            return scheduleTask(delay, interval, unit, context, task);
        }
    }
}
