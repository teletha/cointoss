/*
 * Copyright (C) 2018 CoinToss Development Team
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          https://opensource.org/licenses/MIT
 */
package cointoss;

import java.time.ZonedDateTime;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.ConcurrentLinkedQueue;

import cointoss.Time.Lag;
import cointoss.market.bitflyer.BitFlyer;
import cointoss.order.Order;
import cointoss.order.Order.Quantity;
import cointoss.order.Order.State;
import cointoss.order.OrderBookListChange;
import cointoss.order.OrderType;
import cointoss.util.Num;
import kiss.Disposable;
import kiss.I;
import kiss.Signal;

/**
 * @version 2017/09/08 19:09:35
 */
class TestableMarketBackend extends MarketBackend implements MarketProvider {

    /** The terminator. */
    private final Disposable diposer = Disposable.empty();

    /** The managed id. */
    private int id = 0;

    /** The order manager. */
    private final ConcurrentLinkedDeque<BackendOrder> orderActive = new ConcurrentLinkedDeque<>();

    /** The order manager. */
    private final ConcurrentLinkedQueue<BackendOrder> orderAll = new ConcurrentLinkedQueue<>();

    /** The order manager. */
    private final ConcurrentLinkedDeque<Position> positions = new ConcurrentLinkedDeque<>();

    /** The execution manager. */
    private final LinkedList<Execution> executeds = new LinkedList();

    /** The lag generator. */
    private final Lag lag;

    /** The current time. */
    private ZonedDateTime now = Time.BASE;

    /**
    * 
    */
    TestableMarketBackend(Lag lag) {
        this.lag = lag;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public MarketBackend service() {
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public MarketLog log() {
        return BitFlyer.FX_BTC_JPY.log();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String name() {
        return "TestableMarket";
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void initialize(Market market, Signal<Execution> log) {
        diposer.add(log.to(e -> market.tick(emulate(e))));
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
            child.child_order_type = order.price.is(0) ? OrderType.MARKET : OrderType.LIMIT;
            child.averagePrice.set(order.price);
            child.remainingSize.set(order.size);
            child.cancel_size = Num.ZERO;
            child.executed_size.set(Num.ZERO);

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
    public Signal<Execution> executions() {
        return I.signal(executeds);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Signal<Execution> executions(long id) {
        // If this exception will be thrown, it is bug of this program. So we must rethrow the
        // wrapped error in here.
        throw new Error();
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
    public Signal<Position> getPositions() {
        return I.signal(positions);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Signal<Num> getBaseCurrency() {
        return I.signal(Num.HUNDRED);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Signal<Num> getTargetCurrency() {
        return I.signal(Num.ZERO);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Signal<OrderBookListChange> getOrderBook() {
        return Signal.NEVER;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Signal<Health> health() {
        return I.signal(Health.Normal);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected Execution decode(String[] values, Execution previous) {
        // If this exception will be thrown, it is bug of this program. So we must rethrow the
        // wrapped error in here.
        throw new Error();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected String[] encode(Execution execution, Execution previous) {
        // If this exception will be thrown, it is bug of this program. So we must rethrow the
        // wrapped error in here.
        throw new Error();
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
            if (order.quantity() == Quantity.FillOrKill && !order.isTradableWith(e)) {
                iterator.remove();
                orderAll.remove(order);
                continue;
            }

            if (order.quantity() == Quantity.ImmediateOrCancel) {
                if (order.isTradablePriceWith(e)) {
                    Num min = Num.min(e.size, order.remainingSize.get());
                    order.remainingSize.set(min);
                } else {
                    iterator.remove();
                    orderAll.remove(order);
                    continue;
                }
            }

            if (order.isTradablePriceWith(e)) {
                Num executedSize = Num.min(e.size, order.remainingSize.get());
                if (order.child_order_type.isMarket() && executedSize.isNot(0)) {
                    order.marketMinPrice = order.isBuy() ? Num.max(order.marketMinPrice, e.price) : Num.min(order.marketMinPrice, e.price);
                    order.averagePrice.set(order.averagePrice.get()
                            .multiply(order.executed_size.get())
                            .plus(order.marketMinPrice.multiply(executedSize))
                            .divide(order.executed_size.get().plus(executedSize)));;
                }
                order.remainingSize.set(order.remainingSize.get().minus(executedSize));
                order.executed_size.set(order.executed_size.get().plus(executedSize));

                Execution exe = new Execution();
                exe.side = order.side();
                exe.size = exe.cumulativeSize = executedSize;
                exe.price = order.child_order_type.isMarket() ? order.marketMinPrice : order.averagePrice.get();
                exe.exec_date = e.exec_date;
                exe.buy_child_order_acceptance_id = order.isBuy() ? order.id : e.buy_child_order_acceptance_id;
                exe.sell_child_order_acceptance_id = order.isSell() ? order.id : e.sell_child_order_acceptance_id;
                executeds.add(exe);

                if (order.remainingSize.get().is(0)) {
                    order.state.set(State.COMPLETED);
                    iterator.remove();
                }

                // replace execution info
                e.side = exe.side;
                e.size = e.cumulativeSize = exe.size;
                e.price = exe.price;
                e.buy_child_order_acceptance_id = exe.buy_child_order_acceptance_id;
                e.sell_child_order_acceptance_id = exe.sell_child_order_acceptance_id;
                break;
            }
        }
        return e;
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
            super(o.side(), o.size, o.price, o.triggerPrice(), o.quantity());
        }
    }
}
