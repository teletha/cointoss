/*
 * Copyright (C) 2020 cointoss Development Team
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          https://opensource.org/licenses/MIT
 */
package cointoss;

import java.net.http.HttpClient;
import java.time.Duration;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import cointoss.execution.Execution;
import cointoss.execution.ExecutionLog;
import cointoss.market.MarketServiceProvider;
import cointoss.order.Order;
import cointoss.order.OrderBookPageChanges;
import cointoss.order.OrderState;
import cointoss.util.Chrono;
import cointoss.util.EfficientWebSocket;
import cointoss.util.Num;
import cointoss.util.RetryPolicy;
import kiss.Decoder;
import kiss.Disposable;
import kiss.Encoder;
import kiss.Signal;
import kiss.Variable;

public abstract class MarketService implements Disposable {

    /** The logging system. */
    protected static final Logger logger = LogManager.getLogger(MarketService.class);

    /** The exchange name. */
    public final String exchangeName;

    /** The market name. */
    public final String marketName;

    /** The execution log. */
    public final ExecutionLog log;

    /** The service disposer. */
    protected final Disposable disposer = Disposable.empty();

    /** The market configuration. */
    public final MarketSetting setting;

    /** The market specific scheduler. */
    private final ScheduledThreadPoolExecutor scheduler;

    /** The shared real-time execution log. */
    private Signal<Execution> executions;

    /** The shared real-time order book. */
    private Signal<OrderBookPageChanges> orderBooks;

    /** The realtime user order state. */
    private Variable<Order> orderStream;

    /**
     * @param exchangeName
     * @param marketName
     */
    protected MarketService(String exchangeName, String marketName, MarketSetting setting) {
        this.exchangeName = Objects.requireNonNull(exchangeName);
        this.marketName = Objects.requireNonNull(marketName);
        this.setting = setting;
        this.scheduler = new ScheduledThreadPoolExecutor(2, task -> {
            Thread thread = new Thread(task);
            thread.setName(marketIdentity() + " Scheduler");
            thread.setDaemon(true);
            return thread;
        });
        this.scheduler.allowCoreThreadTimeOut(true);
        this.scheduler.setKeepAliveTime(30, TimeUnit.SECONDS);

        this.log = new ExecutionLog(this);
    }

    /**
     * Returns the identity of market.
     * 
     * @return A market identity.
     */
    public final String marketIdentity() {
        return exchangeName + " " + marketName;
    }

    /**
     * Returns the human-readable name of market.
     * 
     * @return A human-readable name.
     */
    public final String marketReadableName() {
        return marketIdentity().replaceAll("_", "");
    }

    /**
     * Return the http communicator.
     * 
     * @return
     */
    protected HttpClient client() {
        return null;
    }

    /**
     * Return the realtime communicator.
     * 
     * @return
     */
    protected abstract EfficientWebSocket clientRealtimely();

    /**
     * Estimate the curernt order delay (second).
     * 
     * @return
     */
    public abstract Signal<Integer> delay();

    /**
     * Request order actually.
     * 
     * @param order A order to request.
     * @return A requested order.
     */
    public abstract Signal<String> request(Order order);

    /**
     * Request canceling order actually.
     * 
     * @param order A order to cancel.
     * @return A cancelled order result (state, remainingSize, executedSize).
     */
    public abstract Signal<Order> cancel(Order order);

    /**
     * Acquire the execution log between start (exclusive) and end (exclusive) key.
     * 
     * @param key An execution sequencial key (i.e. ID, datetime etc).
     * @return This {@link Signal} will be completed immediately.
     */
    public abstract Signal<Execution> executions(long startId, long endId);

    /**
     * Acquire execution log in realtime. This is infinitely.
     * 
     * @return A shared realtime execution logs.
     */
    public final synchronized Signal<Execution> executionsRealtimely() {
        if (executions == null) {
            executions = connectExecutionRealtimely().effectOnObserve(disposer::add).share();
        }
        return executions;
    }

    /**
     * Connect to the realtime execution log stream.
     * 
     * @return A realtime execution logs.
     */
    protected abstract Signal<Execution> connectExecutionRealtimely();

    /**
     * Acquier the latest execution log.
     * 
     * @return A latest execution log.
     */
    public abstract Signal<Execution> executionLatest();

    /**
     * Checks whether the specified {@link Execution}s are the same.
     * 
     * @param one Non-null target.
     * @param other Non-null target.
     * @return Result.
     */
    public boolean checkEquality(Execution one, Execution other) {
        return one.id == other.id;
    }

    /**
     * Estimate the inital execution id of the {@link Market}.
     * 
     * @return
     */
    public long estimateInitialExecutionId() {
        long start = 0;
        long end = executionLatest().waitForTerminate().to().exact().id;
        long middle = (start + end) / 2;

        while (true) {
            List<Execution> result = executions(start, middle).skipError().waitForTerminate().toList();

            if (result.isEmpty()) {
                start = middle;
                middle = (start + end) / 2;
            } else {
                end = result.get(0).id + 1;
                middle = (start + end) / 2;
            }

            if (end - start <= 10) {
                return start;
            }
        }
    }

    /**
     * Return {@link ExecutionLog} of this market.
     * 
     * @return
     */
    public final ExecutionLog log() {
        return log;
    }

    /**
     * Request all orders.
     * 
     * @return
     */
    public abstract Signal<Order> orders();

    /**
     * Request all orders with the specified state.
     * 
     * @return
     */
    public abstract Signal<Order> orders(OrderState state);

    /**
     * Acquire the order state in realtime. This is infinitely.
     * 
     * @return A event stream of order state.
     */
    public final synchronized Signal<Order> ordersRealtimely() {
        if (orderStream == null) {
            orderStream = Variable.empty();
            disposer.add(connectOrdersRealtimely().to(orderStream::set));
        }
        return orderStream.observe();
    }

    /**
     * Connect to the realtime order stream.
     * 
     * @return
     */
    protected abstract Signal<Order> connectOrdersRealtimely();

    /**
     * <p>
     * Get amount of the base and target currency.
     * </p>
     * 
     * @return
     */
    public abstract Signal<OrderBookPageChanges> orderBook();

    /**
     * Acquire order book in realtime. This is infinitely.
     * 
     * @return A shared realtime order books.
     */
    public final synchronized Signal<OrderBookPageChanges> orderBookRealtimely() {
        if (orderBooks == null) {
            orderBooks = orderBook().concat(connectOrderBookRealtimely()).effectOnObserve(disposer::add).share();
        }
        return orderBooks;
    }

    /**
     * Connect to the realtime order book stream.
     * 
     * @return A realtime order books.
     */
    protected abstract Signal<OrderBookPageChanges> connectOrderBookRealtimely();

    /**
     * Calculate human-readable price for display.
     * 
     * @param price A target price.
     * @return
     */
    public String calculateReadablePrice(double price) {
        return Num.of(price).scale(setting.baseCurrencyScaleSize).toString();
    }

    /**
     * Calculate human-readable data-time for display.
     * 
     * @param seconds A target time. (second)
     * @return
     */
    public String calculateReadableTime(double seconds) {
        ZonedDateTime time = Chrono.systemBySeconds((long) seconds);

        if (time.getMinute() == 0 && time.getHour() % 6 == 0) {
            return time.format(Chrono.DateTimeWithoutSec);
        } else {
            return time.format(Chrono.TimeWithoutSec);
        }
    }

    /**
     * Get amount of the base currency.
     */
    public abstract Signal<Num> baseCurrency();

    /**
     * Get amount of the target currency.
     */
    public abstract Signal<Num> targetCurrency();

    /**
     * Get the current time.
     * 
     * @return The current time.
     */
    public ZonedDateTime now() {
        return Chrono.utcNow();
    }

    /**
     * Get the current nano-time.
     * 
     * @return The current time.
     */
    public long nano() {
        return System.nanoTime();
    }

    /**
     * Get the market scheduler.
     * 
     * @return A scheduler.
     */
    public ScheduledExecutorService scheduler() {
        return scheduler;
    }

    /**
     * Create new {@link RetryPolicy}.
     * 
     * @param max The maximum number to retry.
     * @return
     */
    public final RetryPolicy retryPolicy(int max) {
        return retryPolicy(max, null);
    }

    /**
     * Create new {@link RetryPolicy}.
     * 
     * @param max The maximum number to retry.
     * @return
     */
    public RetryPolicy retryPolicy(int max, String name) {
        return RetryPolicy.with.limit(max)
                .delayLinear(Duration.ofSeconds(2))
                .delayMaximum(Duration.ofMinutes(2))
                .scheduler(scheduler())
                .debug(name == null || name.length() == 0 ? null : marketIdentity() + " : " + name);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final void vandalize() {
        disposer.dispose();
        scheduler.shutdown();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return marketIdentity();
    }

    /**
     * Codec.
     */
    @SuppressWarnings("unused")
    private static class Codec implements Decoder<MarketService>, Encoder<MarketService> {

        /**
         * {@inheritDoc}
         */
        @Override
        public String encode(MarketService value) {
            return value.toString();
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public MarketService decode(String value) {
            return MarketServiceProvider.by(value).or((MarketService) null);
        }
    }
}