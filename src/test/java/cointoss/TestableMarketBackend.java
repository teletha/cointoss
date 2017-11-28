/*
 * Copyright (C) 2017 Nameless Production Committee
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          http://opensource.org/licenses/mit-license.php
 */
package cointoss;

import java.time.ZonedDateTime;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.ConcurrentLinkedQueue;

import javafx.beans.property.SimpleObjectProperty;

import cointoss.Order.Quantity;
import cointoss.Time.Lag;
import cointoss.util.Num;
import kiss.I;
import kiss.Signal;

/**
 * @version 2017/09/08 19:09:35
 */
class TestableMarketBackend implements MarketBackend {

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
    public void initialize(Market market, Signal<Execution> log) {
        log.to(e -> market.tick(emulate(e)));
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
    public Signal<String> request(Order order) {
        return I.signal(order).map(o -> {
            BackendOrder child = new BackendOrder(order);
            child.child_order_acceptance_id = "LOCAL-ACCEPTANCE-" + id++;
            child.child_order_state.set(OrderState.ACTIVE);
            child.child_order_date = new SimpleObjectProperty(now.plusNanos(lag.generate()));
            child.child_order_type = order.price.v.is(0) ? OrderType.MARKET : OrderType.LIMIT;
            child.average_price.set(order.price.v);
            child.outstanding_size.set(order.size.v);
            child.cancel_size = Num.ZERO;
            child.executed_size = Num.ZERO;

            orderAll.add(child);
            orderActive.add(child);

            return child.child_order_acceptance_id;
        });
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Signal<String> cancel(String childOrderId) {
        return new Signal<>((observer, disposer) -> {
            orderActive.removeIf(o -> o.child_order_acceptance_id.equals(childOrderId));
            I.signal(orderAll).take(o -> o.child_order_acceptance_id.equals(childOrderId)).take(1).to(o -> {
                o.child_order_state.set(OrderState.CANCELED);
                observer.accept(childOrderId);
                observer.complete();
            });
            return disposer;
        });
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Signal<Order> getOrderBy(String id) {
        return I.signal(orderAll).take(o -> o.child_order_acceptance_id.equals(id)).take(1).as(Order.class);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Signal<Order> getOrders() {
        return I.signal(orderAll).map(o -> o);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Signal<Order> getOrdersBy(OrderState state) {
        return I.signal(orderAll).take(o -> o.child_order_state.is(state)).as(Order.class);
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
    public Signal<Execution> getExecutions() {
        return I.signal(executeds);
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
    public Signal<Board> getBoard() {
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
            if (e.exec_date.isBefore(order.child_order_date.get())) {
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
                    Num min = Num.min(e.size, order.outstanding_size.get());
                    order.outstanding_size.set(min);
                } else {
                    iterator.remove();
                    orderAll.remove(order);
                    continue;
                }
            }

            if (order.isTradablePriceWith(e)) {
                Num executedSize = Num.min(e.size, order.outstanding_size.get());
                if (order.child_order_type.isMarket() && executedSize.isNot(0)) {
                    order.marketMinPrice = order.isBuy() ? Num.max(order.marketMinPrice, e.price) : Num.min(order.marketMinPrice, e.price);
                    order.average_price.set(order.average_price.get()
                            .multiply(order.executed_size)
                            .plus(order.marketMinPrice.multiply(executedSize))
                            .divide(order.executed_size.plus(executedSize)));;
                }
                order.outstanding_size.set(order.outstanding_size.get().minus(executedSize));
                order.executed_size = order.executed_size.plus(executedSize);

                Execution exe = new Execution();
                exe.side = order.side();
                exe.size = exe.cumulativeSize = executedSize;
                exe.price = order.child_order_type.isMarket() ? order.marketMinPrice : order.average_price.get();
                exe.exec_date = e.exec_date;
                exe.buy_child_order_acceptance_id = order.isBuy() ? order.child_order_acceptance_id : e.buy_child_order_acceptance_id;
                exe.sell_child_order_acceptance_id = order.isSell() ? order.child_order_acceptance_id : e.sell_child_order_acceptance_id;

                executeds.add(exe);

                if (order.outstanding_size.get().is(0)) {
                    order.child_order_state.set(OrderState.COMPLETED);
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
            super(o.side(), o.size.v, o.price.v, o.triggerPrice(), o.quantity());
        }
    }
}