/*
 * Copyright (C) 2019 CoinToss Development Team
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          https://opensource.org/licenses/MIT
 */
package cointoss.verify;

import java.time.ZonedDateTime;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.ConcurrentLinkedQueue;

import cointoss.MarketService;
import cointoss.MarketSetting;
import cointoss.backtest.Time;
import cointoss.execution.Execution;
import cointoss.order.Order;
import cointoss.order.OrderBookChange;
import cointoss.order.OrderState;
import cointoss.order.OrderType;
import cointoss.order.QuantityCondition;
import cointoss.util.Num;
import kiss.Disposable;
import kiss.I;
import kiss.Signal;
import kiss.Signaling;

/**
 * @version 2018/09/18 20:37:42
 */
public class VerifiableMarketService extends MarketService {

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
    private Time lag = Time.at(0);

    /** The initial base currency. */
    private final Num baseCurrency = Num.HUNDRED;

    /** The initial target currency. */
    private final Num targetCurrency = Num.ZERO;

    /** The current time. */
    private ZonedDateTime now = Time.Base;

    /**
     * 
     */
    public VerifiableMarketService() {
        super("TestableExchange", "TestableMarket", MarketSetting.builder()
                .baseCurrencyMinimumBidPrice(Num.ONE)
                .targetCurrencyMinimumBidSize(Num.ONE)
                .orderBookGroupRanges(Num.of(1))
                .build());
    }

    /**
     * 
     */
    public VerifiableMarketService(MarketService delegation) {
        super(delegation.exchangeName, delegation.marketName, delegation.setting);
    }

    /**
     * Configure fixed lag.
     * 
     * @param lag
     * @return
     */
    public final VerifiableMarketService lag(int lag) {
        this.lag = Time.at(lag);

        return this;
    }

    /**
     * Configure random lag.
     * 
     * @param lag
     * @return
     */
    public final VerifiableMarketService lag(int start, int end) {
        this.lag = Time.lag(start, end);

        return this;
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
            child.id.let("LOCAL-ACCEPTANCE-" + id++);
            child.state.set(OrderState.ACTIVE);
            child.created.set(now.plusNanos(lag.generate()));
            child.remainingSize = order.size;

            orderAll.add(child);
            orderActive.add(child);
            return child.id.v;
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
                o.state.set(OrderState.CANCELED);
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
    public Signal<Execution> executionsRealtimelyForMe() {
        return positions.expose;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Signal<Execution> executionLatest() {
        // If this exception will be thrown, it is bug of this program. So we must rethrow the
        // wrapped error in here.
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
    public Signal<Num> baseCurrency() {
        return I.signal(baseCurrency);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Signal<Num> targetCurrency() {
        return I.signal(targetCurrency);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Signal<OrderBookChange> orderBook() {
        return Signal.never();
    }

    /**
     * Emulate {@link Execution}.
     * 
     * @param e
     * @return
     */
    public Execution emulate(Execution e) {
        now = e.date;

        // emulate market execution
        Iterator<BackendOrder> iterator = orderActive.iterator();

        while (iterator.hasNext()) {
            BackendOrder order = iterator.next();

            // time base filter
            if (e.date.isBefore(order.created.get())) {
                continue;
            }

            // check trigger price
            if (order.stopPrice() != null && order.triggerArchived == false) {
                if (order.isBuy()) {
                    if (e.price.isGreaterThanOrEqual(order.stopPrice())) {
                        order.triggerArchived = true;
                    }
                } else {
                    if (e.price.isLessThanOrEqual(order.stopPrice())) {
                        order.triggerArchived = true;
                    }
                }
                continue;
            }

            // check quantity condition
            if (order.quantityCondition() == QuantityCondition.FillOrKill && !validateTradable(order, e)) {
                iterator.remove();
                orderAll.remove(order);
                continue;
            }

            if (order.quantityCondition() == QuantityCondition.ImmediateOrCancel) {
                if (validateTradableByPrice(order, e)) {
                    order.remainingSize = Num.min(e.size, order.remainingSize);
                } else {
                    iterator.remove();
                    orderAll.remove(order);
                    continue;
                }
            }

            if (validateTradableByPrice(order, e)) {
                Num executedSize = Num.min(e.size, order.remainingSize);
                if (order.type.isMarket() && executedSize.isNot(0)) {
                    order.marketMinPrice = order.isBuy() ? Num.max(order.marketMinPrice, e.price) : Num.min(order.marketMinPrice, e.price);
                    order.price.set(v -> v.multiply(order.executedSize)
                            .plus(order.marketMinPrice.multiply(executedSize))
                            .divide(executedSize.plus(order.executedSize)));
                }
                order.executedSize = order.executedSize.plus(executedSize);
                order.remainingSize = order.remainingSize.minus(executedSize);

                Execution exe = new Execution();
                exe.side = order.direction();
                exe.size = exe.cumulativeSize = executedSize;
                exe.price = order.type.isMarket() ? order.marketMinPrice : order.price.get();
                exe.date = e.date;
                exe.yourOrder = order.id.v;
                executeds.add(exe);

                if (order.remainingSize.isZero()) {
                    order.state.set(OrderState.COMPLETED);
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
            super(o.direction(), o.size, o.price.v);

            stopAt(o.stopPrice());
            type(o.quantityCondition());
        }
    }
}
