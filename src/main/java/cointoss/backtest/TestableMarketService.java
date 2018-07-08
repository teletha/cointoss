/*
 * Copyright (C) 2018 CoinToss Development Team
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          https://opensource.org/licenses/MIT
 */
package cointoss.backtest;

import java.time.ZonedDateTime;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.ConcurrentLinkedQueue;

import cointoss.Execution;
import cointoss.MarketService;
import cointoss.order.Order;
import cointoss.order.Order.Quantity;
import cointoss.order.Order.State;
import cointoss.order.OrderBookListChange;
import cointoss.order.OrderType;
import cointoss.util.Num;
import kiss.Disposable;
import kiss.I;
import kiss.Signal;
import kiss.Signaling;

/**
 * @version 2018/05/23 18:36:07
 */
public class TestableMarketService extends MarketService {

    /** The terminator. */
    private final Disposable diposer = Disposable.empty();

    /** The managed id. */
    private int id = 0;

    /** The order manager. */
    private final ConcurrentLinkedDeque<BackendOrder> orderActive = new ConcurrentLinkedDeque<>();

    /** The order manager. */
    private final ConcurrentLinkedQueue<BackendOrder> orderAll = new ConcurrentLinkedQueue<>();

    /** The order manager. */
    private final Signaling<Execution> positions = new Signaling();

    /** The execution manager. */
    private final LinkedList<Execution> executeds = new LinkedList();

    /** The lag generator. */
    private final Time lag;

    /** The current time. */
    private ZonedDateTime now = Time.BASE;

    /**
     * 
     */
    public TestableMarketService() {
        this(Time.at(0));
    }

    /**
    * 
    */
    public TestableMarketService(Time lag) {
        super("TestableExchange", "TestableMarket");
        this.lag = lag;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Signal<Integer> delay() {
        return I.signal(0);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void vandalize() {
        diposer.dispose();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Signal<String> request(Order order) {
        return I.signal(order).map(o -> {
            BackendOrder child = new BackendOrder(order);
            child.id = "LOCAL-ACCEPTANCE-" + id++;
            child.state.set(State.ACTIVE);
            child.created.set(now.plusNanos(lag.generate()));
            child.sizeRemaining.set(order.size);
            child.sizeExecuted.set(Num.ZERO);

            orderAll.add(child);
            orderActive.add(child);
            return child.id;
        });
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Signal<Order> cancel(Order order) {
        return new Signal<>((observer, disposer) -> {
            orderActive.removeIf(o -> o.id.equals(order.id));
            I.signal(orderAll).take(o -> o.id.equals(order.id)).take(1).to(o -> {
                o.state.set(State.CANCELED);
                observer.accept(order);
                observer.complete();
            });
            return disposer;
        });
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Signal<Execution> executionsRealtimely() {
        return I.signal(executeds);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Execution exectutionLatest() {
        // If this exception will be thrown, it is bug of this program. So we must rethrow the
        // wrapped error
        // in here.
        throw new Error();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Signal<Execution> executions(long start, long end) {
        return I.signal(executeds);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Signal<Order> orders() {
        return I.signal(orderAll).as(Order.class);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Signal<Execution> positions() {
        return positions.expose;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Signal<Num> baseCurrency() {
        return I.signal(Num.HUNDRED);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Signal<Num> targetCurrency() {
        return I.signal(Num.ZERO);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Signal<OrderBookListChange> orderBook() {
        return Signal.NEVER;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected int executionMaxAcquirableSize() {
        return 0;
    }

    /**
     * Emulate {@link Execution}.
     * 
     * @param e
     * @return
     */
    Execution emulate(Execution e) {
        now = e.exec_date;

        // emulate market execution
        Iterator<BackendOrder> iterator = orderActive.iterator();

        while (iterator.hasNext()) {
            BackendOrder order = iterator.next();

            // time base filter
            if (e.exec_date.isBefore(order.created.get())) {
                continue;
            }

            // check trigger price
            if (order.triggerPrice() != null && order.triggerArchived == false) {
                if (order.isBuy()) {
                    if (e.price.isGreaterThanOrEqual(order.triggerPrice())) {
                        order.triggerArchived = true;
                    }
                } else {
                    if (e.price.isLessThanOrEqual(order.triggerPrice())) {
                        order.triggerArchived = true;
                    }
                }
                continue;
            }

            // check quantity condition
            if (order.quantity() == Quantity.FillOrKill && !validateTradable(order, e)) {
                iterator.remove();
                orderAll.remove(order);
                continue;
            }

            if (order.quantity() == Quantity.ImmediateOrCancel) {
                if (validateTradableByPrice(order, e)) {
                    Num min = Num.min(e.size, order.sizeRemaining.get());
                    order.sizeRemaining.set(min);
                } else {
                    iterator.remove();
                    orderAll.remove(order);
                    continue;
                }
            }

            if (validateTradableByPrice(order, e)) {
                Num executedSize = Num.min(e.size, order.sizeRemaining.get());
                if (order.type.isMarket() && executedSize.isNot(0)) {
                    order.marketMinPrice = order.isBuy() ? Num.max(order.marketMinPrice, e.price) : Num.min(order.marketMinPrice, e.price);
                    order.price.set(v -> v.multiply(order.sizeExecuted)
                            .plus(order.marketMinPrice.multiply(executedSize))
                            .divide(executedSize.plus(order.sizeExecuted)));
                }
                order.sizeRemaining.set(v -> v.minus(executedSize));
                order.sizeExecuted.set(v -> v.plus(executedSize));

                Execution exe = new Execution();
                exe.side = order.side();
                exe.size = exe.cumulativeSize = executedSize;
                exe.price = order.type.isMarket() ? order.marketMinPrice : order.price.get();
                exe.exec_date = e.exec_date;
                exe.yourOrder = order.id;
                executeds.add(exe);

                if (order.sizeRemaining.get().is(0)) {
                    order.state.set(State.COMPLETED);
                    iterator.remove();
                }
                positions.accept(exe);

                // replace execution info
                e.side = exe.side;
                e.size = e.cumulativeSize = exe.size;
                e.price = exe.price;
                break;
            }
        }
        return e;
    }

    /**
     * Test whether this order can trade with the specified {@link Execution}.
     * 
     * @param e A target {@link Execution}.
     * @return A result.
     */
    private boolean validateTradable(Order order, Execution e) {
        return validateTradableBySize(order, e) && validateTradableByPrice(order, e);
    }

    /**
     * Test whether this order price can trade with the specified {@link Execution}.
     * 
     * @param e A target {@link Execution}.
     * @return A result.
     */
    private boolean validateTradableByPrice(Order order, Execution e) {
        if (order.type == OrderType.MARKET) {
            return true;
        }

        if (order.isBuy()) {
            return order.price.v.isGreaterThanOrEqual(e.price);
        } else {
            return order.price.v.isLessThanOrEqual(e.price);
        }
    }

    /**
     * Test whether this order size can trade with the specified {@link Execution}.
     * 
     * @param e A target {@link Execution}.
     * @return A result.
     */
    private boolean validateTradableBySize(Order order, Execution e) {
        return order.size.isLessThanOrEqual(e.size);
    }

    /**
     * For test.
     */
    private static class BackendOrder extends Order {

        /** The trigger state. */
        private boolean triggerArchived;

        /** The minimum price for market order. */
        private Num marketMinPrice = isBuy() ? Num.ZERO : Num.MAX;

        /**
         * @param o
         */
        private BackendOrder(Order o) {
            super(o.side(), o.size, o.price.v, o.triggerPrice(), o.quantity());
        }
    }
}
