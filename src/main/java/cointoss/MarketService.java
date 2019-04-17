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
import java.util.Objects;

import cointoss.execution.Execution;
import cointoss.execution.ExecutionCodec;
import cointoss.execution.ExecutionLog;
import cointoss.order.Order;
import cointoss.order.OrderBookChange;
import cointoss.util.Chrono;
import cointoss.util.Network;
import cointoss.util.Num;
import kiss.Disposable;
import kiss.I;
import kiss.Signal;

/**
 * @version 2018/08/05 0:47:38
 */
public abstract class MarketService implements Disposable {

    /** The singleton. */
    private static final ExecutionCodec codec = I.make(ExecutionCodec.class);

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
    public abstract Signal<String> request(Order order);

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
    public abstract Signal<Execution> executionsRealtimelyForMe();

    /**
     * Acquier the latest execution log.
     * 
     * @return A latest execution log.
     */
    public abstract Signal<Execution> executionLatest();

    /**
     * Acquire the execution sequential key (default is {@link Execution#id}).
     * 
     * @param execution A target execution.
     * @return
     */
    protected long executionKey(Execution execution) {
        return execution.id;
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
     * Return {@link ExecutionCodec} of this market.
     * 
     * @return
     */
    public ExecutionCodec codec() {
        return codec;
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
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return marketIdentity();
    }
}
