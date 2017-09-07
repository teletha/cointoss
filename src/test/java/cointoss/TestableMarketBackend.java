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

import cointoss.Order.Quantity;
import cointoss.Time.Lag;
import eu.verdelhan.ta4j.Decimal;
import kiss.I;
import kiss.Signal;

/**
 * @version 2017/08/15 23:38:25
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
    public void initialize(Market market, MarketBuilder builder) {
        builder.initialize().to(e -> {
            market.tick(emulate(e));
        });
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Signal<String> request(Order order) {
        return I.signal(order).map(o -> {
            BackendOrder child = new BackendOrder(order);
            child.child_order_acceptance_id = "LOCAL-ACCEPTANCE-" + id++;
            child.child_order_state = OrderState.ACTIVE;
            child.child_order_date = now.plusNanos(lag.generate());
            child.child_order_type = order.price().is(0) ? OrderType.MARKET : OrderType.LIMIT;
            child.average_price = order.price();
            child.outstanding_size = order.size();
            child.cancel_size = Decimal.ZERO;
            child.executed_size = Decimal.ZERO;

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
                o.child_order_state = OrderState.CANCELED;
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
        return I.signal(orderAll).take(o -> o.child_order_state == state).as(Order.class);
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
    public Signal<BalanceUnit> getCurrency() {
        BalanceUnit base = new BalanceUnit();
        base.currency_code = "JPY";
        base.amount = base.available = Decimal.HUNDRED;

        BalanceUnit target = new BalanceUnit();
        target.currency_code = "BTC";
        target.amount = target.available = Decimal.ZERO;

        return I.signal(base, target);
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
            if (e.exec_date.isBefore(order.child_order_date)) {
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
                    Decimal min = Decimal.min(e.size, order.outstanding_size);
                    order.outstanding_size = min;
                } else {
                    iterator.remove();
                    orderAll.remove(order);
                    continue;
                }
            }

            if (order.isTradablePriceWith(e)) {
                Decimal executedSize = Decimal.min(e.size, order.outstanding_size);
                if (order.child_order_type.isMarket() && executedSize.isNot(0)) {
                    order.average_price = order.average_price.multipliedBy(order.executed_size)
                            .plus(e.price.multipliedBy(executedSize))
                            .dividedBy(order.executed_size.plus(executedSize));
                }
                order.outstanding_size = order.outstanding_size.minus(executedSize);
                order.executed_size = order.executed_size.plus(executedSize);

                Execution exe = new Execution();
                exe.side = order.side();
                exe.size = executedSize;
                exe.price = order.child_order_type.isMarket() ? e.price : order.average_price;
                exe.exec_date = e.exec_date;
                exe.buy_child_order_acceptance_id = order.isBuy() ? order.child_order_acceptance_id : e.buy_child_order_acceptance_id;
                exe.sell_child_order_acceptance_id = order.isSell() ? order.child_order_acceptance_id : e.sell_child_order_acceptance_id;

                executeds.add(exe);

                if (order.outstanding_size.is(0)) {
                    order.child_order_state = OrderState.COMPLETED;
                    iterator.remove();
                }

                // replace execution info
                e.side = exe.side;
                e.size = exe.size;
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

        /**
         * @param o
         */
        private BackendOrder(Order o) {
            super(o.side(), o.size(), o.price(), o.triggerPrice(), o.quantity());
        }
    }
}