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

import static cointoss.Order.State.*;

import java.time.Duration;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import cointoss.MarketBackend.Health;
import cointoss.Order.State;
import cointoss.chart.Chart;
import cointoss.order.OrderBook;
import cointoss.order.OrderBookChange;
import cointoss.util.Listeners;
import cointoss.util.Num;
import kiss.Disposable;
import kiss.Observer;
import kiss.Signal;
import kiss.Variable;

/**
 * @version 2018/01/23 14:36:32
 */
public class Market implements Disposable {

    /** The initial execution. */
    private static final Execution SEED = new Execution();

    static {
        SEED.side = Side.BUY;
    }

    private final AtomicReference<Execution> switcher = new AtomicReference<>(SEED);

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
    public final Chart second10 = new Chart(Duration.ofSeconds(10));

    /** CHART */
    public final Chart second5 = new Chart(Duration
            .ofSeconds(5), second10, second20, second30, minute1, minute5, minute15, minute30, hour1, hour2, hour4, hour6, hour12, day1, day3, day7);

    public final ExecutionFlow flow = new ExecutionFlow(100);

    public final ExecutionFlow flow75 = new ExecutionFlow(200);

    public final ExecutionFlow flow100 = new ExecutionFlow(400);

    public final ExecutionFlow flow200 = new ExecutionFlow(800);

    public final ExecutionFlow flow300 = new ExecutionFlow(1600);

    /** The execution listeners. */
    private final Listeners<Execution> holderForTimeline = new Listeners();

    /** The execution time line. */
    public final Signal<Execution> timeline = new Signal(holderForTimeline);

    /** The execution time line by taker. */
    public final Signal<Execution> timelineByTaker = timeline.map(e -> {
        Execution previous = switcher.getAndSet(e);

        if (e.side.isBuy() && previous.buy_child_order_acceptance_id.equals(e.buy_child_order_acceptance_id)) {
            // same long taker
            e.cumulativeSize = previous.cumulativeSize.plus(e.size);
            return null;
        } else if (e.side.isSell() && previous.sell_child_order_acceptance_id.equals(e.sell_child_order_acceptance_id)) {
            // same short taker
            e.cumulativeSize = previous.cumulativeSize.plus(e.size);
            return null;
        }
        return previous;
    }).skip(e -> e == null || e == SEED);

    /** The execution time line. */
    public final Signal<OrderBookChange> orderTimeline;

    /** Order Book. */
    public final OrderBook orderBook = new OrderBook();

    /** The holder. */
    private final Listeners<Order> holderForYourOrder = new Listeners();

    /** The event stream. */
    public final Signal<Order> yourOrder = new Signal(holderForYourOrder);

    /** The holder. */
    private final Listeners<Position> holderForYourExecution = new Listeners();

    /** The event stream. */
    public final Signal<Position> yourExecution = new Signal(holderForYourExecution);

    /** The market health. */
    public final Variable<Health> health = Variable.empty();

    /** The latest price. */
    public final Variable<Num> latestPrice = Variable.of(Num.ZERO);

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

    /** The list of traders. */
    final List<Trader> traders = new CopyOnWriteArrayList<>();

    /**
     * Market without {@link Trader}.
     * 
     * @param backend A market backend.
     * @param log A market execution log.
     */
    public Market(MarketBackend backend, Signal<Execution> log) {
        this(backend, log, null);
    }

    /**
     * Market with {@link Trader}.
     * 
     * @param backend A market backend.
     * @param log A market execution log.
     * @param trading A trading strategy.
     */
    public Market(MarketBackend backend, Signal<Execution> log, Trader trading) {
        if (backend == null) {
            throw new Error("Market is not found.");
        }

        if (log == null) {
            throw new Error("Market log is not found.");
        }

        this.backend = backend;

        // initialize price, balance and executions
        this.base = this.baseInit = backend.getBaseCurrency().to().v;
        this.target = this.targetInit = backend.getTargetCurrency().to().v;

        add(trading);
        backend.initialize(this, log);

        orderTimeline = backend.getOrderBook();
        backend.add(orderTimeline.to(board -> {
            orderBook.shorts.update(board.asks);
            orderBook.longs.update(board.bids);
        }));
        backend.add(backend.getHealth().to(health -> {
            this.health.set(health);
        }));
        backend.add(timeline.throttle(2, TimeUnit.SECONDS).to(e -> {
            // fix error board
            orderBook.shorts.fix(e.price);
            orderBook.longs.fix(e.price);
        }));
    }

    /**
     * Market name.
     * 
     * @return
     */
    public String name() {
        return backend.name();
    }

    /**
     * Add market trader to this market.
     * 
     * @param trading
     */
    public void add(Trader trading) {
        if (trading != null) {
            trading.market = this;
            trading.initialize();

            traders.add(trading);
        }
    }

    /**
     * Remove market trader to this market.
     * 
     * @param trading
     */
    public void remove(Trader trading) {
        if (trading != null) {
            traders.remove(trading);
            trading.dispose();
            trading.market = null;
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void vandalize() {
        backend.dispose();
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
        return backend.request(order).effectOnError(System.out::println).map(id -> {
            order.child_order_acceptance_id = id;
            order.state.set(State.ACTIVE);
            order.child_order_date.set(ZonedDateTime.now());
            order.average_price.set(order.price);
            order.outstanding_size.set(order.size);

            // store
            orders.add(order);

            // event
            holderForYourOrder.omit(order);

            return order;
        }).effectOnError(e -> {
            order.state.set(State.CANCELED);
        });
    }

    /**
     * Request order canceling.
     * 
     * @param acceptanceId
     */
    public final Signal<String> cancel(Order order) {
        if (order.state.is(ACTIVE)) {
            State previous = order.state.set(REQUESTING);
            orders.remove(order);

            return backend.cancel(order.child_order_acceptance_id).effect(id -> {
                orders.remove(order);
                order.state.set(CANCELED);
            }).effectOnError(e -> {
                order.state.set(previous);
            });
        } else {
            return Signal.EMPTY;
        }
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

    /**
     * Create new price signal.
     * 
     * @param price
     * @return
     */
    public Signal<Execution> signalByPrice(Num price) {
        if (latest.price.isLessThan(price)) {
            return timeline.take(e -> e.price.isGreaterThanOrEqual(price)).take(1);
        } else {
            return timeline.take(e -> e.price.isLessThanOrEqual(price)).take(1);
        }
    }

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
        latestPrice.set(exe.price);

        flow.record(exe);
        flow75.record(exe);
        flow100.record(exe);
        flow200.record(exe);
        flow300.record(exe);
        second5.tick(exe);

        for (Order order : orders) {
            if (order.id().equals(exe.buy_child_order_acceptance_id) || order.id().equals(exe.sell_child_order_acceptance_id)) {
                update(order, exe);

                for (Observer<? super Execution> listener : order.executeListeners) {
                    listener.accept(exe);
                }

                Position position = new Position();
                position.side = order.side;
                position.price = exe.price;
                position.size.set(exe.size);
                position.open_date = exe.exec_date;
                holderForYourExecution.omit(position);
            }
        }

        // observe executions
        holderForTimeline.omit(exe);
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
        Num executed = Num.min(order.outstanding_size.v, exe.size);

        if (order.child_order_type.isMarket() && executed.isNot(0)) {
            order.average_price.set(order.average_price.v.multiply(order.executed_size.get())
                    .plus(exe.price.multiply(executed))
                    .divide(order.executed_size.get().plus(executed)));
        }

        order.outstanding_size.set(order.outstanding_size.v.minus(executed));
        order.executed_size.set(order.executed_size.get().plus(executed));

        if (order.outstanding_size.v.is(0)) {
            order.state.set(State.COMPLETED);
            orders.remove(order); // complete order
        }

        // pairing order and execution
        exe.associated = order;
        order.executions.add(exe);
    }
}