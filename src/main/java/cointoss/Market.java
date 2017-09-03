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

import java.util.List;
import java.util.Objects;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicReference;

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

    /** The tick manager. */
    public final TimeSeries ticks = new TimeSeries();

    /** The initial execution. */
    private Execution init;

    /** The latest execution. */
    private Execution latest;

    /** 基軸通貨量 */
    private Amount base;

    /** 基軸通貨初期量 */
    private Amount baseInit;

    /** 対象通貨量 */
    private Amount target;

    /** 対象通貨初期量 */
    private Amount targetInit;

    /** The current trading. */
    private Trade trade = new NOP();

    /**
     * @param backend
     * @param builder
     * @param strategy
     */
    protected Market(MarketBackend backend, MarketBuilder builder, Class<? extends Trade> trade) {
        this.backend = Objects.requireNonNull(backend);
        with(trade);

        // initialize price, balance and executions
        this.backend.getCurrency().to(amounts -> {
            this.base = this.baseInit = amounts.ⅰ;
            this.target = this.targetInit = amounts.ⅱ;
        });

        backend.initialize(this, builder);
    }

    /**
     * Set trading strategy.
     * 
     * @param tradeType
     * @return
     */
    public final Market with(Class<? extends Trade> tradeType) {
        if (tradeType != null) {
            this.trade = I.make(tradeType);
            this.trade.initialize(this);
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
    public Amount getBase() {
        return base;
    }

    /**
     * Return the current amount of target currency.
     * 
     * @return
     */
    public Amount getTarget() {
        return target;
    }

    /**
     * Return the current amount of base currency.
     * 
     * @return
     */
    public Amount getBaseInit() {
        return baseInit;
    }

    /**
     * Return the current amount of target currency.
     * 
     * @return
     */
    public Amount getTargetInit() {
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
    public Amount getLatestPrice() {
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
    public Amount calculateProfit() {
        Amount baseProfit = base.minus(baseInit);
        Amount targetProfit = target.multiply(latest.price).minus(targetInit.multiply(init.price));
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
    public Amount price = Amount.ZERO;

    /** The remaining position size. */
    public Amount remaining = Amount.ZERO;

    /** The related order identifiers. */
    private final CopyOnWriteArrayList<Order> orders = new CopyOnWriteArrayList();

    /**
     * <p>
     * Trade something.
     * </p>
     * 
     * @param exe
     */
    final void trade(Execution exe) {
        if (init == null) {
            init = exe;
        }
        latest = exe;

        ticks.tick(exe);

        for (Order order : orders) {
            if (order.id().equals(exe.buy_child_order_acceptance_id) || order.id().equals(exe.sell_child_order_acceptance_id)) {
                update(order, exe);
            }

            OrderAndExecution oae = new OrderAndExecution(order, exe, this);

            for (Observer<? super OrderAndExecution> listener : order.executionListeners) {
                listener.accept(oae);
            }
        }
        trade.onNoPosition(this, exe);
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
        Amount executed = Amount.min(order.outstanding_size, exe.size);
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
        price = remaining = Amount.ZERO;
    }

    /**
     * @version 2017/08/24 20:13:13
     */
    private static class NOP extends Trade {

        /**
         * {@inheritDoc}
         */
        @Override
        public void initialize(Market market) {
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public void onNoPosition(Market market, Execution exe) {
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