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
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicReference;

import eu.verdelhan.ta4j.Decimal;
import kiss.I;
import kiss.Observer;
import kiss.Signal;

/**
 * @version 2017/08/16 9:22:14
 */
public class Market {

    /** The market handler. */
    protected final MarketBackend backend;

    /** The trading logger. */
    public final TradingLog logger = new TradingLog(this);

    /** CHART */
    public final Chart hour256 = new Chart(Duration.ofHours(256), null);

    /** CHART */
    public final Chart hour128 = new Chart(Duration.ofHours(128), hour256);

    /** CHART */
    public final Chart hour64 = new Chart(Duration.ofHours(64), hour128);

    /** CHART */
    public final Chart hour32 = new Chart(Duration.ofHours(32), hour64);

    /** CHART */
    public final Chart hour16 = new Chart(Duration.ofHours(16), hour32);

    /** CHART */
    public final Chart hour8 = new Chart(Duration.ofHours(8), hour16);

    /** CHART */
    public final Chart hour4 = new Chart(Duration.ofHours(4), hour8);

    /** CHART */
    public final Chart hour2 = new Chart(Duration.ofHours(2), hour4);

    /** CHART */
    public final Chart hour1 = new Chart(Duration.ofHours(1), hour2);

    /** CHART */
    public final Chart minute30 = new Chart(Duration.ofMinutes(30), hour1);

    /** CHART */
    public final Chart minute15 = new Chart(Duration.ofMinutes(15), minute30);

    /** CHART */
    public final Chart minute5 = new Chart(Duration.ofMinutes(5), minute15);

    /** CHART */
    public final Chart minute1 = new Chart(Duration.ofMinutes(1), minute5);

    /** The event listeners. */
    private final CopyOnWriteArrayList<Observer<? super Execution>> executionListeners = new CopyOnWriteArrayList();

    /** The initial execution. */
    private Execution init;

    /** The latest execution. */
    private Execution latest;

    /** 基軸通貨量 */
    private Decimal base;

    /** 基軸通貨初期量 */
    private Decimal baseInit;

    /** 対象通貨量 */
    private Decimal target;

    /** 対象通貨初期量 */
    private Decimal targetInit;

    /** The current trading. */
    private Trading trade = create(NOP.class);

    /**
     * @param backend
     * @param builder
     * @param strategy
     */
    public Market(MarketBackend backend, Signal<Execution> log, Class<? extends Trading> trade) {
        this.backend = Objects.requireNonNull(backend);
        with(trade);

        // initialize price, balance and executions
        List<BalanceUnit> units = backend.getCurrency().toList();
        this.base = this.baseInit = units.get(0).amount;
        this.target = this.targetInit = units.get(1).amount;

        backend.initialize(this, log);
    }

    /**
     * Observe executions.
     * 
     * @param threshold
     * @return
     */
    public final Signal<Execution> observeExecution() {
        return new Signal<Execution>((observer, disposer) -> {
            executionListeners.add(observer);
            return disposer.add(() -> {
                executionListeners.remove(observer);
            });
        });
    }

    /**
     * Observe executions filtered by size.
     * 
     * @param threshold
     * @return
     */
    public final Signal<Execution> observeExecutionBySize(int threshold) {
        AtomicReference<Decimal> accumlated = new AtomicReference<>(Decimal.ZERO);

        return observeExecution().scan(new Execution(), (prev, next) -> {
            if (prev.buy_child_order_acceptance_id.equals(next.buy_child_order_acceptance_id) || prev.sell_child_order_acceptance_id
                    .equals(next.sell_child_order_acceptance_id)) {
                accumlated.updateAndGet(v -> v.plus(next.size));
            } else {
                prev.cumulativeSize = accumlated.getAndSet(next.size);
            }
            return next;
        }).delay(1).take(e -> e.cumulativeSize != null && e.cumulativeSize.isGreaterThanOrEqual(threshold));
    }

    /**
     * Set trading strategy.
     * 
     * @param tradeType
     * @return
     */
    public final Market with(Class<? extends Trading> tradeType) {
        if (tradeType != null) {
            this.trade = create(tradeType);
        }
        return this;
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

            return logger.log(order);
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
    public Decimal getBase() {
        return base;
    }

    /**
     * Return the current amount of target currency.
     * 
     * @return
     */
    public Decimal getTarget() {
        return target;
    }

    /**
     * Return the current amount of base currency.
     * 
     * @return
     */
    public Decimal getBaseInit() {
        return baseInit;
    }

    /**
     * Return the current amount of target currency.
     * 
     * @return
     */
    public Decimal getTargetInit() {
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
    public Decimal getLatestPrice() {
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
    public Decimal calculateProfit() {
        Decimal baseProfit = base.minus(baseInit);
        Decimal targetProfit = target.multipliedBy(latest.price).minus(targetInit.multipliedBy(init.price));
        return baseProfit.plus(targetProfit);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return logger.toString();
    }

    // ===========================================================
    // Position Management
    // ===========================================================
    /** The side */
    public Side position;

    /** The remaining position price. */
    public Decimal price = Decimal.ZERO;

    /** The remaining position size. */
    public Decimal remaining = Decimal.ZERO;

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

        minute1.tick(exe);

        for (Order order : orders) {
            if (order.id().equals(exe.buy_child_order_acceptance_id) || order.id().equals(exe.sell_child_order_acceptance_id)) {
                update(order, exe);
            }

            OrderAndExecution oae = new OrderAndExecution(order, exe, this);

            for (Observer<? super OrderAndExecution> listener : order.executionListeners) {
                listener.accept(oae);
            }
        }

        // observe executions
        for (Observer<? super Execution> listener : executionListeners) {
            listener.accept(exe);
        }

        trade.tick(exe);
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
            base = base.minus(exe.size.multipliedBy(exe.price));
            target = target.plus(exe.size);
        } else {
            base = base.plus(exe.size.multipliedBy(exe.price));
            target = target.minus(exe.size);
        }

        // for order state
        Decimal executed = order.outstanding_size.min(exe.size);
        if (order.child_order_type.isMarket() && executed.isNot(0)) {
            order.average_price = order.average_price.multipliedBy(order.executed_size)
                    .plus(exe.price.multipliedBy(executed))
                    .dividedBy(order.executed_size.plus(executed));
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
            price = price.multipliedBy(remaining).plus(exe.price.multipliedBy(executed)).dividedBy(remaining.plus(executed));
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
        price = remaining = Decimal.ZERO;
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

    /**
     * @version 2017/08/24 20:13:13
     */
    private static class NOP extends Trading {

        /**
         * @param market
         */
        private NOP(Market market) {
            super(market);
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public void tryEntry(Execution exe) {
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public void tryExit(Execution exe) {
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public void timeline(Execution exe) {
        }
    }

    /**
     * @param profit
     * @param loss
     */
    public Signal<OrderAndExecution> entry(Order profit, Order loss) {
        AtomicReference<Order> ref = new AtomicReference();

        return profit.entryTo(this).merge(loss.entryTo(this)).take(o -> {
            if (ref.compareAndSet(null, o.o)) {
                cancel(o.o == profit ? loss : profit).to();
            }
            return ref.get() == o.o;
        });
    }
}