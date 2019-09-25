/*
 * Copyright (C) 2019 CoinToss Development Team
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          https://opensource.org/licenses/MIT
 */
package cointoss;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.function.Consumer;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import cointoss.execution.Execution;
import cointoss.execution.ExecutionLog;
import cointoss.market.MarketServiceProvider;
import cointoss.order.Order;
import cointoss.order.OrderBookChange;
import cointoss.order.OrderState;
import cointoss.util.Chrono;
import cointoss.util.Network;
import cointoss.util.Num;
import kiss.Decoder;
import kiss.Disposable;
import kiss.Encoder;
import kiss.Signal;
import kiss.Ⅲ;

public abstract class MarketService implements Disposable {

    /** The logging system. */
    protected static final Logger logger = LogManager.getLogger(MarketService.class);

    /** The exchange name. */
    public final String exchangeName;

    /** The market name. */
    public final String marketName;

    /** The execution log. */
    public final ExecutionLog log;

    /** The network accessor. */
    protected Network network = new Network();

    /** The market configuration. */
    public final MarketSetting setting;

    /** The market specific scheduler. */
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(8, task -> {
        Thread thread = new Thread(task);
        thread.setName(marketIdentity());
        thread.setDaemon(true);
        return thread;
    });

    /**
     * @param exchangeName
     * @param marketName
     */
    protected MarketService(String exchangeName, String marketName, MarketSetting setting) {
        this.exchangeName = Objects.requireNonNull(exchangeName);
        this.marketName = Objects.requireNonNull(marketName);
        this.setting = setting;
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
     * Estimate the curernt order delay (second).
     * 
     * @return
     */
    public abstract Signal<Integer> delay();

    /**
     * <p>
     * Request order actually.
     * </p>
     * 
     * @param order A order to request.
     * @return A requested order.
     */
    public abstract Signal<String> request(Order order, Consumer<OrderState> state);

    /**
     * <p>
     * Request order canceling.
     * </p>
     * 
     * @param order A order to cancel.
     */
    public abstract Signal<Order> cancel(Order order);

    /**
     * Acquire the execution log between start (exclusive) and end (exclusive) key.
     * 
     * @param key An execution sequencial key (i.e. ID, datetime etc).
     * @return This {@link Signal} will be completed immediately.
     */
    public abstract Signal<Execution> executions(long start, long end);

    /**
     * Acquire the execution log in realtime. This is infinitely.
     * 
     * @return A event stream of execution log.
     */
    public abstract Signal<Execution> executionsRealtimely();

    /**
     * Acquire the my execution log (positions) in realtime. This is infinitely.
     * 
     * @return A event stream of execution log related to my orders.
     */
    public abstract Signal<Ⅲ<Direction, String, Execution>> executionsRealtimelyForMe();

    /**
     * Acquier the latest execution log.
     * 
     * @return A latest execution log.
     */
    public abstract Signal<Execution> executionLatest();

    /**
     * Estimate the inital execution id of the {@link Market}.
     * 
     * @return
     */
    public long estimateInitialExecutionId() {
        long start = 0;
        long end = executionLatest().to().v.id;
        long middle = (start + end) / 2;

        while (true) {
            List<Execution> result = executions(start, middle).skipError().toList();

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
     * <p>
     * Request all orders.
     * </p>
     * 
     * @return
     */
    public abstract Signal<Order> orders();

    /**
     * Acquire the order state in realtime. This is infinitely.
     * 
     * @return A event stream of order state.
     */
    public abstract Signal<Order> ordersRealtimely();

    /**
     * <p>
     * Get amount of the base and target currency.
     * </p>
     * 
     * @return
     */
    public abstract Signal<OrderBookChange> orderBook();

    /**
     * Calculate human-readable price for display.
     * 
     * @param price A target price.
     * @return
     */
    public String calculateReadablePrice(double price) {
        return Num.of(price).scale(0).toString();
    }

    /**
     * Calculate human-readable price for display.
     * 
     * @param seconds A target time. (second)
     * @return
     */
    public String calculateReadableTime(double seconds) {
        ZonedDateTime time = Chrono.systemBySeconds(seconds);

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
     * Get the market scheduler.
     * 
     * @return A scheduler.
     */
    public ScheduledExecutorService scheduler() {
        return scheduler;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void vandalize() {
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
