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

import static java.util.concurrent.TimeUnit.*;

import java.util.Objects;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;

import cointoss.execution.Execution;
import cointoss.execution.ExecutionLog;
import cointoss.market.MarketServiceProvider;
import cointoss.order.Order;
import cointoss.order.OrderBook;
import cointoss.order.OrderBookManager;
import cointoss.order.OrderManager;
import cointoss.order.PositionManager;
import cointoss.ticker.TickerManager;
import cointoss.util.Chrono;
import cointoss.util.Num;
import cointoss.util.RetryPolicy;
import kiss.Disposable;
import kiss.Signal;
import kiss.Signaling;

/**
 * Facade for the market related operations.
 * 
 * @version 2018/08/14 1:04:19
 */
public class Market implements Disposable {

    /** The empty object. */
    public static final Execution BASE = Execution.with.buy(0).date(Chrono.utc(2000, 1, 1));

    private final AtomicReference<Execution> switcher = new AtomicReference<>(Market.BASE);

    /** The target market servicce. */
    public final MarketService service;

    /** The execution observers. */
    protected final Signaling<Execution> timelineObservers = new Signaling();

    /** The order manager. */
    public final OrderManager orders;

    /** The order books. */
    public final OrderBookManager orderBook;

    /** The ticker manager. */
    public final TickerManager tickers = new TickerManager();

    /** The execution time line. */
    public final Signal<Execution> timeline = timelineObservers.expose.skipComplete();

    /** The execution time line with current value. */
    public final Signal<Execution> timelineNow = timeline.startWith(tickers.latest);

    /** The execution time line by taker. */
    public final Signal<Execution> timelineByTaker = timeline.map(e -> {
        Execution previous = switcher.getAndSet(e);

        if (e.consecutive == Execution.ConsecutiveSameBuyer || e.consecutive == Execution.ConsecutiveSameSeller) {
            // same taker
            e.assignAccumulative(v -> previous.accumulative.plus(v));
            return null;
        }
        return previous;
    }).skip(e -> e == null || e == Market.BASE);

    /** The position manager. */
    public final PositionManager positions;

    /**
     * Build {@link Market} with the specified {@link MarketServiceProvider}.
     * 
     * @param provider A market provider.
     */
    public Market(MarketService service) {
        this.service = Objects.requireNonNull(service, "Market is not found.");
        this.orders = new OrderManager(service);
        this.orderBook = new OrderBookManager(service);
        this.positions = new PositionManager(service, tickers.latest);

        // build tickers for each span
        timeline.to(tickers::update);

        readOrderBook();
    }

    /**
     * Start reading {@link OrderBook}.
     */
    protected void readOrderBook() {
        RetryPolicy policy = service.setting.retryPolicy();

        // orderbook management
        service.add(service.orderBook().retryWhen(policy).to(board -> {
            orderBook.shorts.update(board.asks);
            orderBook.longs.update(board.bids);
            policy.reset();
        }));
        service.add(timeline.throttle(2, SECONDS).to(e -> {
            // fix error board
            orderBook.shorts.fix(e.price);
            orderBook.longs.fix(e.price);
        }));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void vandalize() {
        service.dispose();
    }

    /**
     * Shorthand for {@link OrderManager#request(Order)}.
     * 
     * @param order A order to request.
     * @return A order request process.
     */
    public final Signal<Order> request(Order order) {
        return orders.request(order);
    }

    /**
     * Shorthand for {@link OrderManager#cancel(Order)}.
     * 
     * @param order A order to cancel.
     * @return A order cancel process.
     */
    public final Signal<Order> cancel(Order order) {
        return orders.cancel(order);
    }

    /**
     * Shorthand accessor to the latest price.
     * 
     * @return
     */
    public final Signal<Num> latestPrice() {
        return timelineNow.map(Execution::price).diff();
    }

    /**
     * Create new price signal.
     * 
     * @param price
     * @return
     */
    public final Signal<Execution> signalByPrice(Num price) {
        if (tickers.latest.v.price.isLessThan(price)) {
            return timeline.take(e -> e.price.isGreaterThanOrEqual(price)).take(1);
        } else {
            return timeline.take(e -> e.price.isLessThanOrEqual(price)).take(1);
        }
    }

    /**
     * Read {@link Execution} log.
     * 
     * @param log
     * @return
     */
    public final Market readLog(Function<ExecutionLog, Signal<Execution>> log) {
        service.add(log.apply(service.log).to(timelineObservers));

        return this;
    }

    /**
     * Stop all positions.
     */
    public void stop() {
    }

    /**
     * Cancel all orders.
     */
    public void cancel() {
        orders.cancelNowAll();
    }

    /**
     * Stop and reverse all positions.
     */
    public void reverse() {
    }
}
