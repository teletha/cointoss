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

import java.lang.reflect.Constructor;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicReference;

import cointoss.chart.Chart;
import cointoss.util.Num;
import kiss.I;
import kiss.Observer;
import kiss.Signal;

/**
 * @version 2017/09/11 18:26:15
 */
public class Market {

    /** The market handler. */
    protected final MarketBackend backend;

    /** CHART */
    public final Chart day7 = new Chart(Duration.ofDays(7));

    /** CHART */
    public final Chart day3 = new Chart(Duration.ofDays(3));

    /** CHART */
    public final Chart day1 = new Chart(Duration.ofDays(1));

    /** CHART */
    public final Chart hour12 = new Chart(Duration.ofHours(12));

    /** CHART */
    public final Chart hour6 = new Chart(Duration.ofHours(6));

    /** CHART */
    public final Chart hour4 = new Chart(Duration.ofHours(4));

    /** CHART */
    public final Chart hour2 = new Chart(Duration.ofHours(2));

    /** CHART */
    public final Chart hour1 = new Chart(Duration.ofHours(1));

    /** CHART */
    public final Chart minute30 = new Chart(Duration.ofMinutes(30));

    /** CHART */
    public final Chart minute15 = new Chart(Duration.ofMinutes(15));

    /** CHART */
    public final Chart minute5 = new Chart(Duration.ofMinutes(5));

    /** CHART */
    public final Chart minute1 = new Chart(Duration.ofMinutes(1));

    /** CHART */
    public final Chart second30 = new Chart(Duration.ofSeconds(30));

    /** CHART */
    public final Chart second20 = new Chart(Duration.ofSeconds(20));

    /** CHART */
    public final Chart second10 = new Chart(Duration
            .ofSeconds(10), second20, second30, minute1, minute5, minute15, minute30, hour1, hour2, hour4, hour6, hour12, day1, day3, day7);

    /** The execution listeners. */
    private final CopyOnWriteArrayList<Observer<? super Execution>> timelines = new CopyOnWriteArrayList();

    /** The execution time line. */
    public final Signal<Execution> timeline = new Signal(timelines);

    /** The initial execution. */
    private Execution init;

    /** The latest execution. */
    private Execution latest;

    /** 基軸通貨量 */
    private Num base;

    /** 基軸通貨初期量 */
    private Num baseInit;

    /** 対象通貨量 */
    private Num target;

    /** 対象通貨初期量 */
    private Num targetInit;

    /** The current trading. */
    final List<Trading> tradings = new ArrayList<>();

    /**
     * @param backend
     * @param builder
     * @param strategy
     */
    public Market(MarketBackend backend, Signal<Execution> log, Trading strategy) {
        this.backend = Objects.requireNonNull(backend);

        // initialize price, balance and executions
        List<BalanceUnit> units = backend.getCurrency().toList();
        this.base = this.baseInit = units.get(0).amount;
        this.target = this.targetInit = units.get(1).amount;

        tradings.add(strategy);
        strategy.market = this;
        strategy.initialize();
        backend.initialize(this, log);
    }

    /**
     * Observe executions filtered by size.
     * 
     * @param threshold
     * @return
     */
    public final Signal<Execution> observeExecutionBySize(int threshold) {
        AtomicReference<Num> accumlated = new AtomicReference<>(Num.ZERO);

        return timeline.scan(new Execution(), (prev, next) -> {
            if ((next.side.isBuy() && prev.buy_child_order_acceptance_id.equals(next.buy_child_order_acceptance_id)) || (next.side
                    .isSell() && prev.sell_child_order_acceptance_id.equals(next.sell_child_order_acceptance_id))) {
                accumlated.updateAndGet(v -> v.plus(next.size));
            } else {
                prev.cumulativeSize = accumlated.getAndSet(next.size);
            }
            return next;
        }).delay(1).take(e -> e.cumulativeSize != null && e.cumulativeSize.isGreaterThanOrEqual(threshold));
    }

    /**
     * <p>
     * Request order.
     * </p>
     * 
     * @param position
     * @param init
     * @param size
     */
    public final Signal<Order> request(Order order) {
        return backend.request(order).flatMap(backend::getOrderBy).map(o -> {
            // copy backend property
            order.average_price = o.average_price;
            order.cancel_size = o.cancel_size;
            order.child_order_acceptance_id = o.child_order_acceptance_id;
            order.child_order_date = o.child_order_date;
            order.child_order_state = o.child_order_state;
            order.child_order_type = o.child_order_type;
            order.executed_size = o.executed_size;
            order.expire_date = o.expire_date;
            order.outstanding_size = o.outstanding_size;
            order.total_commission = o.total_commission;

            // store
            orders.add(order);

            return order;
        });
    }

    /**
     * Request order canceling.
     * 
     * @param acceptanceId
     */
    public final Signal<String> cancel(Order order) {
        orders.remove(order);
        order.child_order_state = OrderState.CANCELED;

        return backend.cancel(order.child_order_acceptance_id).effect(id -> {
            orders.remove(order);
            order.child_order_state = OrderState.CANCELED;

            for (Observer<? super Order> listener : order.cancelListeners) {
                listener.accept(order);
            }
        });
    }

    /**
     * <p>
     * Check remaining orders.
     * </p>
     * 
     * @return
     */
    public final Signal<Order> getOrdersBy(OrderState state) {
        return backend.getOrders().take(o -> o.child_order_state == state);
    }

    /**
     * <p>
     * Check orders.
     * </p>
     * 
     * @return
     */
    public final List<Order> listOrders() {
        return backend.getOrders().toList();
    }

    /**
     * <p>
     * Check executions.
     * </p>
     */
    public final List<Execution> listExecutions() {
        return backend.getExecutions().toList();
    }

    /**
     * <p>
     * Check market state.
     * </p>
     * 
     * @return
     */
    public final boolean isEmpty() {
        return orders.isEmpty();
    }

    /**
     * Return the current amount of base currency.
     * 
     * @return
     */
    public Num getBase() {
        return base;
    }

    /**
     * Return the current amount of target currency.
     * 
     * @return
     */
    public Num getTarget() {
        return target;
    }

    /**
     * Return the current amount of base currency.
     * 
     * @return
     */
    public Num getBaseInit() {
        return baseInit;
    }

    /**
     * Return the current amount of target currency.
     * 
     * @return
     */
    public Num getTargetInit() {
        return targetInit;
    }

    /**
     * Return the current amount of base currency.
     * 
     * @return
     */
    public Execution getExecutionInit() {
        return init;
    }

    /**
     * Return the current amount of target currency.
     * 
     * @return
     */
    public Execution getExecutionLatest() {
        return latest;
    }

    /**
     * @return
     */
    public Num getLatestPrice() {
        return latest.price;
    }

    /**
     * <p>
     * Check market state.
     * </p>
     * 
     * @return
     */
    public boolean hasNoActiveOrder() {
        return orders.isEmpty();
    }

    /**
     * Calculate profit and loss.
     * 
     * @return
     */
    public Num calculateProfit() {
        Num baseProfit = base.minus(baseInit);
        Num targetProfit = target.multiply(latest.price).minus(targetInit.multiply(init.price));
        return baseProfit.plus(targetProfit);
    }

    // ===========================================================
    // Position Management
    // ===========================================================
    /** The side */
    public Side position;

    /** The remaining position price. */
    public Num price = Num.ZERO;

    /** The remaining position size. */
    public Num remaining = Num.ZERO;

    /** The related order identifiers. */
    private final CopyOnWriteArrayList<Order> orders = new CopyOnWriteArrayList();

    /**
     * <p>
     * Trade something.
     * </p>
     * 
     * @param exe
     */
    public final void tick(Execution exe) {
        if (init == null) {
            init = exe;
        }
        latest = exe;

        second10.tick(exe);

        for (Order order : orders) {
            if (order.id().equals(exe.buy_child_order_acceptance_id) || order.id().equals(exe.sell_child_order_acceptance_id)) {
                update(order, exe);

                for (Observer<? super Execution> listener : order.executeListeners) {
                    listener.accept(exe);
                }
            }
        }

        // observe executions
        for (Observer<? super Execution> listener : timelines) {
            listener.accept(exe);
        }
    }

    /**
     * Update local managed {@link Order}.
     * 
     * @param order
     * @param exe
     */
    private void update(Order order, Execution exe) {
        // update assets
        if (order.side().isBuy()) {
            base = base.minus(exe.size.multiply(exe.price));
            target = target.plus(exe.size);
        } else {
            base = base.plus(exe.size.multiply(exe.price));
            target = target.minus(exe.size);
        }

        // for order state
        Num executed = Num.min(order.outstanding_size, exe.size);

        if (order.child_order_type.isMarket() && executed.isNot(0)) {
            order.average_price = order.average_price.multiply(order.executed_size)
                    .plus(exe.price.multiply(executed))
                    .divide(order.executed_size.plus(executed));
        }

        order.outstanding_size = order.outstanding_size.minus(executed);
        order.executed_size = order.executed_size.plus(executed);

        if (order.outstanding_size.is(0)) {
            order.child_order_state = OrderState.COMPLETED;
            orders.remove(order); // complete order
        }

        // pairing order and execution
        exe.associated = order;
        order.executions.add(exe);

        // for trade state
        if (position == null) {
            // first
            position = order.side();
            remaining = executed;
            price = exe.price;
        } else if (position.isSame(order.side())) {
            // same position
            price = price.multiply(remaining).plus(exe.price.multiply(executed)).divide(remaining.plus(executed));
            remaining = remaining.plus(executed);
        } else {
            // diff position
            remaining = remaining.minus(executed);
            if (remaining.isGreaterThan(0)) {

            } else if (remaining.isLessThan(0)) {
                position = order.side();
                remaining = remaining.abs();
                price = exe.price;
            } else {
                initializePosition();
            }
        }
    }

    /**
     * Initialize position.
     */
    private void initializePosition() {
        position = null;
        price = remaining = Num.ZERO;
    }

    /**
     * Create new {@link Trading} instance.
     * 
     * @param type
     * @return
     */
    private Trading create(Class<? extends Trading> type) {
        try {
            Constructor<? extends Trading> constructor = type.getDeclaredConstructor(Market.class);
            constructor.setAccessible(true);
            return constructor.newInstance(this);
        } catch (Exception e) {
            throw I.quiet(e);
        }
    }
}