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

import static java.util.concurrent.TimeUnit.SECONDS;

import java.time.Duration;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;

import cointoss.market.MarketProvider;
import cointoss.order.Order;
import cointoss.order.OrderBook;
import cointoss.order.OrderBookManager;
import cointoss.order.OrderManager;
import cointoss.position.PositionManager;
import cointoss.ticker.TickerManager;
import cointoss.util.Num;
import cointoss.util.RetryPolicy;
import kiss.Disposable;
import kiss.Signal;
import kiss.Signaling;
import kiss.Variable;

/**
 * Facade for the market related operations.
 * 
 * @version 2018/08/14 1:04:19
 */
public class Market implements Disposable {

    private final AtomicReference<Execution> switcher = new AtomicReference<>(Execution.BASE);

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
            e.cumulativeSize = previous.cumulativeSize.plus(e.size);
            return null;
        }
        return previous;
    }).skip(e -> e == null || e == Execution.BASE);

    /** The position manager. */
    public final PositionManager positions;

    /** The amount of base currency. */
    public final Variable<Num> baseCurrency;

    /** The initial amount of base currency. */
    public final Num initialBaseCurrency;

    /** The amount of target currency. */
    public final Variable<Num> targetCurrency;

    /** The initial amount of target currency. */
    public final Num initialTargetCurrency;

    /** The retry policy. */
    private final RetryPolicy policy = new RetryPolicy().retryMaximum(100)
            .delayLinear(Duration.ofSeconds(1))
            .delayMaximum(Duration.ofMinutes(2));

    /** The tarader manager. */
    private final List<Trader> traders = new CopyOnWriteArrayList<>();

    /**
     * Build {@link Market} with the specified {@link MarketProvider}.
     * 
     * @param provider A market provider.
     */
    public Market(MarketService service) {
        this.service = Objects.requireNonNull(service, "Market is not found.");
        this.orders = new OrderManager(service);
        this.orderBook = new OrderBookManager(service);
        this.positions = new PositionManager(service, tickers.latest);

        // initialize currency data
        baseCurrency = service.baseCurrency().to();
        targetCurrency = service.targetCurrency().to();
        initialBaseCurrency = baseCurrency.v;
        initialTargetCurrency = targetCurrency.v;

        // build tickers for each span
        timeline.to(tickers::update);

        readOrderBook();

        service.add(service.executionsRealtimelyForMe()
                .to(e -> {
                    // update assets
                    if (e.isBuy()) {
                        baseCurrency.set(value -> value.minus(e.size.multiply(e.price)));
                        targetCurrency.set(value -> value.plus(e.size));
                    } else {
                        baseCurrency.set(value -> value.plus(e.size.multiply(e.price)));
                        targetCurrency.set(value -> value.minus(e.size));
                    }
                }));
    }

    /**
     * Start reading {@link OrderBook}.
     */
    protected void readOrderBook() {
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
     * Add market trader to this market.
     * 
     * @param trader A trader to add.
     */
    public final void addTrader(Trader trader) {
        if (trader != null) {
            traders.add(trader);
            trader.market = this;
            trader.initialize();
        }
    }

    /**
     * Remove market trader to this market.
     * 
     * @param trader A trader to remove.
     */
    public final void removeTrader(Trader trader) {
        if (trader != null) {
            traders.remove(trader);
            trader.dispose();
            trader.market = null;
        }
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
     * List up all orders.
     * 
     * @return A list of all orders.
     */
    public final List<Order> orders() {
        return service.orders().toList();
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
    public final Market readLog(Function<MarketLog, Signal<Execution>> log) {
        service.add(log.apply(service.log).to(timelineObservers));

        return this;
    }

    /**
     * Calculate profit and loss.
     * 
     * @return
     */
    public Num calculateProfit() {
        Num baseProfit = baseCurrency.v.minus(initialBaseCurrency);
        Num targetProfit = targetCurrency.v.multiply(tickers.latest.v.price).minus(initialTargetCurrency.multiply(tickers.initial.v.price));
        return baseProfit.plus(targetProfit);
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
