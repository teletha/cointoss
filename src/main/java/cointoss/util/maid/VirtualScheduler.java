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

import static java.util.concurrent.Executors.*;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Callable;
import java.util.concurrent.CancellationException;
import java.util.concurrent.Delayed;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.PriorityBlockingQueue;
import java.util.concurrent.RunnableFuture;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicBoolean;

public class VirtualScheduler implements ScheduledExecutorService {

    /** The task queue. */
    protected final BlockingQueue<ScheduledFutureTask<?>> taskQueue = new PriorityBlockingQueue<>();

    /** The running state of task queue. */
    private volatile boolean running = true;

    /** The counter for the running tasks. */
    protected volatile long runningTask;

    /** The counter for the executed tasks. */
    protected volatile long executedTask;

    /**
     * Build simple task manager by virtual thread.
     */
    public VirtualScheduler() {
        Thread.ofVirtual().start(() -> {
            while (running) {
                try {
                    executeTask(taskQueue.take());
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                }
            }
        });
    }

    /**
     * Execute the task.
     * 
     * @param task
     */
    private void executeTask(ScheduledFutureTask task) {
        if (!task.isCancelled()) {
            runningTask++;

            Thread.ofVirtual().start(() -> {
                try {
                    Thread.sleep(Duration.ofNanos(task.getDelay(TimeUnit.NANOSECONDS)));

                    if (!task.isCancelled()) {
                        task.run();

                        if (task.period == 0) {
                            // one shot
                        } else {
                            // reschedule task
                            if (task.period > 0) {
                                // fixed rate
                                task.time += task.period;
                            } else {
                                // fixed delay
                                task.time = calculateNext(-task.period, TimeUnit.NANOSECONDS);
                            }
                            taskQueue.offer(task);
                        }
                    }
                } catch (InterruptedException e) {
                    // TODO: handle exception
                } finally {
                    runningTask--;
                    executedTask++;
                }
            });
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ScheduledFuture<?> schedule(Runnable command, long delay, TimeUnit unit) {
        ScheduledFutureTask task = new ScheduledFutureTask(callable(command), calculateNext(delay, unit), 0);
        taskQueue.offer(task);
        return task;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public <V> ScheduledFuture<V> schedule(Callable<V> command, long delay, TimeUnit unit) {
        ScheduledFutureTask<V> task = new ScheduledFutureTask(command, calculateNext(delay, unit), 0);
        taskQueue.offer(task);
        return task;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ScheduledFuture<?> scheduleAtFixedRate(Runnable command, long initialDelay, long period, TimeUnit unit) {
        ScheduledFutureTask task = new ScheduledFutureTask(callable(command), calculateNext(initialDelay, unit), unit.toNanos(period));
        taskQueue.offer(task);
        return task;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ScheduledFuture<?> scheduleWithFixedDelay(Runnable command, long initialDelay, long delay, TimeUnit unit) {
        ScheduledFutureTask task = new ScheduledFutureTask(callable(command), calculateNext(initialDelay, unit), unit.toNanos(-delay));
        taskQueue.offer(task);
        return task;
    }

    private long calculateNext(long delay, TimeUnit unit) {
        return System.nanoTime() + unit.toNanos(delay);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void shutdown() {
        running = false;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<Runnable> shutdownNow() {
        running = false;
        List<Runnable> remainingTasks = new ArrayList<>();
        taskQueue.drainTo(remainingTasks);
        return remainingTasks;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isShutdown() {
        return !running;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isTerminated() {
        return isShutdown() && taskQueue.isEmpty();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean awaitTermination(long timeout, TimeUnit unit) throws InterruptedException {
        long remainingNanos = unit.toNanos(timeout);
        long end = System.nanoTime() + remainingNanos;
        while (remainingNanos > 0) {
            if (isTerminated()) {
                return true;
            }
            Thread.sleep(Math.min(TimeUnit.NANOSECONDS.toMillis(remainingNanos) + 1, 100));
            remainingNanos = end - System.nanoTime();
        }
        return isTerminated();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void execute(Runnable command) {
        schedule(command, 0, TimeUnit.NANOSECONDS);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public <T> Future<T> submit(Callable<T> task) {
        return schedule(task, 0, TimeUnit.NANOSECONDS);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Future<?> submit(Runnable task) {
        return schedule(task, 0, TimeUnit.NANOSECONDS);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public <T> Future<T> submit(Runnable task, T result) {
        return schedule(Executors.callable(task, result), 0, TimeUnit.NANOSECONDS);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public <T> List<Future<T>> invokeAll(Collection<? extends Callable<T>> tasks) throws InterruptedException {
        List<Future<T>> futures = new ArrayList<>(tasks.size());
        for (Callable<T> task : tasks) {
            futures.add(submit(task));
        }
        for (Future<T> future : futures) {
            try {
                future.get();
            } catch (ExecutionException e) {
                // Ignore
            }
        }
        return futures;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public <T> List<Future<T>> invokeAll(Collection<? extends Callable<T>> tasks, long timeout, TimeUnit unit) throws InterruptedException {
        long end = System.nanoTime() + unit.toNanos(timeout);
        List<Future<T>> futures = new ArrayList<>(tasks.size());
        for (Callable<T> task : tasks) {
            futures.add(submit(task));
        }
        for (Future<T> future : futures) {
            long remainingTime = end - System.nanoTime();
            if (remainingTime <= 0) {
                break;
            }
            try {
                future.get(remainingTime, TimeUnit.NANOSECONDS);
            } catch (ExecutionException | TimeoutException e) {
                // Ignore
            }
        }
        return futures;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public <T> T invokeAny(Collection<? extends Callable<T>> tasks) throws InterruptedException, ExecutionException {
        throw new UnsupportedOperationException("invokeAny is not supported in this implementation");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public <T> T invokeAny(Collection<? extends Callable<T>> tasks, long timeout, TimeUnit unit)
            throws InterruptedException, ExecutionException, TimeoutException {
        throw new UnsupportedOperationException("invokeAny is not supported in this implementation");
    }

    private class ScheduledFutureTask<V> implements RunnableFuture<V>, ScheduledFuture<V> {

        /** The actual task, */
        private final Callable<V> task;

        /** The next trigger time. */
        private long time;

        /** The interval time. */
        private final long period;

        private V result;

        private Exception exception;

        private final AtomicBoolean cancelled = new AtomicBoolean(false);

        private final AtomicBoolean done = new AtomicBoolean(false);

        private ScheduledFutureTask(Callable<V> task, long time, long period) {
            this.task = Objects.requireNonNull(task);
            this.time = time;
            this.period = period;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public long getDelay(TimeUnit unit) {
            return unit.convert(time - System.nanoTime(), TimeUnit.NANOSECONDS);
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public int compareTo(Delayed other) {
            if (other == this) {
                return 0;
            }
            if (other instanceof ScheduledFutureTask task) {
                long diff = time - task.time;
                if (diff < 0) {
                    return -1;
                } else if (diff > 0) {
                    return 1;
                } else {
                    return 1;
                }
            }
            long d = (getDelay(TimeUnit.NANOSECONDS) - other.getDelay(TimeUnit.NANOSECONDS));
            return (d == 0) ? 0 : ((d < 0) ? -1 : 1);
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public void run() {
            if (!cancelled.get()) {
                try {
                    this.result = task.call();
                } catch (Exception e) {
                    this.exception = e;
                }
                done.set(true);
            }
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public V get() throws InterruptedException, ExecutionException {
            while (!isDone()) {
                if (Thread.interrupted()) {
                    throw new InterruptedException();
                }
                Thread.onSpinWait();
            }
            if (isCancelled()) {
                throw new CancellationException();
            }
            if (exception != null) {
                throw new ExecutionException(exception);
            }
            return result;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public V get(long timeout, TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
            long remainingNanos = unit.toNanos(timeout);
            long end = System.nanoTime() + remainingNanos;
            while (!isDone() && remainingNanos > 0) {
                if (Thread.interrupted()) {
                    throw new InterruptedException();
                }
                Thread.onSpinWait();
                remainingNanos = end - System.nanoTime();
            }
            if (!isDone()) {
                throw new TimeoutException();
            }
            if (isCancelled()) {
                throw new CancellationException();
            }
            if (exception != null) {
                throw new ExecutionException(exception);
            }
            return result;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public boolean cancel(boolean mayInterruptIfRunning) {
            boolean result = cancelled.compareAndSet(false, true);
            if (result) {
                done.set(true);
                taskQueue.remove(this);
            }
            return result;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public boolean isCancelled() {
            return cancelled.get();
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public boolean isDone() {
            return done.get();
        }
    }
}