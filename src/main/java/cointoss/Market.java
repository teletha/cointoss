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

import static cointoss.order.Order.State.*;

import java.time.ZonedDateTime;
import java.util.EnumMap;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import cointoss.MarketBackend.Health;
import cointoss.order.Order;
import cointoss.order.Order.State;
import cointoss.order.OrderBook;
import cointoss.order.OrderBookChange;
import cointoss.ticker.ExecutionFlow;
import cointoss.ticker.TickSpan;
import cointoss.ticker.Ticker;
import cointoss.util.Listeners;
import cointoss.util.Num;
import kiss.Disposable;
import kiss.I;
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

    /** The initial execution. */
    public final Variable<Execution> init = Variable.empty();

    /** The latest execution. */
    public final Variable<Execution> latest = Variable.of(new Execution());

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

    /** The ticker manager. */
    private final EnumMap<TickSpan, Ticker> tickers = new EnumMap(TickSpan.class);

    /** The position manager. */
    private final List<Position> positions = new CopyOnWriteArrayList();

    /** The order manager. */
    private final OrderManager orders;

    /**
     * Market without {@link Trader}.
     * 
     * @param provider A market provider.
     * @param log A market execution log.
     */
    public Market(MarketProvider provider, Signal<Execution> log) {
        this(provider, log, null);
    }

    /**
     * Market with {@link Trader}.
     * 
     * @param provider A market backend.
     * @param log A market execution log.
     * @param trading A trading strategy.
     */
    public Market(MarketProvider provider, Signal<Execution> log, Trader trading) {
        if (provider == null) {
            throw new Error("Market is not found.");
        }

        if (log == null) {
            throw new Error("Market log is not found.");
        }

        // build tickers for each span
        for (TickSpan span : TickSpan.values()) {
            tickers.put(span, new Ticker(span, timeline));
        }

        this.backend = provider.service();
        this.orders = new OrderManager();

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
     * Get {@link Ticker} by span.
     * 
     * @param span
     * @return
     */
    public final Ticker tickerBy(TickSpan span) {
        return tickers.get(span);
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
        return backend.request(order).map(id -> {
            order.child_order_acceptance_id = id;
            order.state.set(State.REQUESTING);
            order.child_order_date.set(ZonedDateTime.now());
            order.average_price.set(order.price);
            order.outstanding_size.set(order.size);

            // store
            orders.add(order);

            // event
            holderForYourOrder.accept(order);
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
        Num targetProfit = target.multiply(latest.v.price).minus(targetInit.multiply(init.v.price));
        return baseProfit.plus(targetProfit);
    }

    /**
     * Create new price signal.
     * 
     * @param price
     * @return
     */
    public Signal<Execution> signalByPrice(Num price) {
        if (latest.v.price.isLessThan(price)) {
            return timeline.take(e -> e.price.isGreaterThanOrEqual(price)).take(1);
        } else {
            return timeline.take(e -> e.price.isLessThanOrEqual(price)).take(1);
        }
    }

    /**
     * <p>
     * Trade something.
     * </p>
     * 
     * @param exe
     */
    public final void tick(Execution exe) {
        if (init.isAbsent()) init.let(exe);
        latest.set(exe);

        flow.record(exe);
        flow75.record(exe);
        flow100.record(exe);
        flow200.record(exe);
        flow300.record(exe);

        for (Order order : orders.actives) {
            if (order.id().equals(exe.buy_child_order_acceptance_id) || order.id().equals(exe.sell_child_order_acceptance_id)) {
                update(order, exe);

                order.listeners.accept(exe);

                Position position = new Position();
                position.side = order.side;
                position.price = exe.price;
                position.size.set(exe.size);
                position.date = exe.exec_date;
                holderForYourExecution.accept(position);
            }
        }

        // observe executions
        holderForTimeline.accept(exe);
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
            order.average_price.set(v -> v.multiply(order.executed_size)
                    .plus(exe.price.multiply(executed))
                    .divide(executed.plus(order.executed_size)));
        }

        order.executed_size.set(v -> v.plus(executed));
        order.outstanding_size.set(v -> v.minus(executed));

        if (order.outstanding_size.is(Num.ZERO)) {
            order.state.set(State.COMPLETED);
            orders.remove(order); // complete order
        }

        // pairing order and execution
        exe.associated = order;
        order.executions.add(exe);
    }

    /**
     * @version 2018/02/14 15:55:43
     */
    private class OrderManager {

        /** The active orders. */
        private final List<Order> actives = new CopyOnWriteArrayList();

        /**
         * 
         */
        private OrderManager() {
            I.signal(0, 1, TimeUnit.SECONDS).map(v -> listOrders()).to(this::update);
        }

        /**
         * Add new order.
         * 
         * @param order
         */
        private void add(Order order) {
            actives.add(order);
        }

        /**
         * Remove the specified order.
         * 
         * @param order
         */
        private void remove(Order order) {
            actives.remove(order);
        }

        /**
         * Check size.
         */
        private boolean isEmpty() {
            return actives.isEmpty();
        }

        /**
         * Update managed orders.
         * 
         * @param updaters
         */
        private void update(List<Order> updaters) {
            System.out.println(updaters);

            for (Order updater : updaters) {
                Order active = findBy(updater.child_order_acceptance_id);

                if (active == null) {
                    // add order
                    add(active = updater);
                    System.out.println("add " + updater);
                } else {
                    if (active.state.is(State.REQUESTING)) {
                        // update order id
                        active.id = updater.id;
                        active.state.set(updater.state);
                        System.out.println("update " + active + "  " + updater.id + "  " + updater.state);
                    }
                }
            }
        }

        /**
         * Find by accept id.
         * 
         * @param acceptId
         * @return
         */
        private Order findBy(String acceptId) {
            for (Order order : actives) {
                if (order.child_order_acceptance_id.equals(acceptId)) {
                    return order;
                }
            }
            return null;
        }
    }
}