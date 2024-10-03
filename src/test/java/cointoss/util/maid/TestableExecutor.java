/*
 * Copyright (C) 2023 Nameless Production Committee
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          http://opensource.org/licenses/mit-license.php
 */
package cointoss.util.maid;

import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import kiss.I;

public class TestableExecutor extends VirtualScheduler {

    private Map<Object, Future> futures = new ConcurrentHashMap();

    private long awaitingLimit = 1000;

    /**
     * {@inheritDoc}
     */
    @Override
    public ScheduledFuture<?> schedule(Runnable command, long delay, TimeUnit unit) {
        ScheduledFuture<?> schedule = super.schedule(command, delay, unit);
        futures.put(command, schedule);
        return schedule;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public <V> ScheduledFuture<V> schedule(Callable<V> callable, long delay, TimeUnit unit) {
        ScheduledFuture<V> schedule = super.schedule(callable, delay, unit);
        futures.put(callable, schedule);
        return schedule;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ScheduledFuture<?> scheduleAtFixedRate(Runnable command, long initialDelay, long period, TimeUnit unit) {
        ScheduledFuture<?> future = super.scheduleAtFixedRate(command, initialDelay, period, unit);
        futures.put(command, future);
        return future;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ScheduledFuture<?> scheduleWithFixedDelay(Runnable command, long initialDelay, long delay, TimeUnit unit) {
        ScheduledFuture<?> future = super.scheduleWithFixedDelay(command, initialDelay, delay, unit);
        futures.put(command, future);
        return future;
    }

    protected TestableExecutor limitAwaitTime(long millis) {
        awaitingLimit = millis;
        return this;
    }

    /**
     * Await all tasks are executed.
     */
    protected boolean awaitIdling() {
        long start = System.currentTimeMillis();

        while (!taskQueue.isEmpty() || runningTask.get() != 0) {
            try {
                Thread.sleep(10);
            } catch (InterruptedException e) {
                throw I.quiet(e);
            }

            if (awaitingLimit <= System.currentTimeMillis() - start) {
                throw new Error("Too long task is active. TaskQueue:" + taskQueue.size() + " RunningTask:" + runningTask
                        .get() + "  ExecutedTask:" + executedTask);
            }
        }
        return true;
    }

    /**
     * Await the required tasks are executed.
     * 
     * @param required
     * @return
     */
    protected boolean awaitExecutions(long required) {
        long start = System.currentTimeMillis();

        while (executedTask.get() < required) {
            try {
                Thread.sleep(10);
            } catch (InterruptedException e) {
                throw I.quiet(e);
            }

            if (awaitingLimit <= System.currentTimeMillis() - start) {
                throw new Error("Too long task is active.");
            }
        }
        return true;
    }

    protected void cancel(Object command) {
        Future future = futures.get(command);
        if (future != null) {
            future.cancel(false);
        }
    }
}
