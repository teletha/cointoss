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

import static cointoss.order.Order.State.*;
import static java.util.concurrent.TimeUnit.*;

import java.time.ZonedDateTime;
import java.util.EnumMap;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import cointoss.order.Order;
import cointoss.order.Order.State;
import cointoss.order.OrderBook;
import cointoss.order.OrderBookListChange;
import cointoss.order.OrderManager;
import cointoss.ticker.ExecutionFlow;
import cointoss.ticker.TickSpan;
import cointoss.ticker.Ticker;
import cointoss.util.Num;
import kiss.Disposable;
import kiss.I;
import kiss.Signal;
import kiss.Signaling;
import kiss.Variable;
import viewtify.Viewtify;

/**
 * @version 2018/02/25 18:36:30
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
    private final Signaling<Execution> holderForTimeline = new Signaling();

    /** The execution time line. */
    public final Signal<Execution> timeline = holderForTimeline.expose;

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
    public final Signal<OrderBookListChange> orderTimeline;

    /** Order Book. */
    public final OrderBook orderBook = new OrderBook();

    /** The holder. */
    private final Signaling<Order> holderForYourOrder = new Signaling();

    /** The event stream. */
    public final Signal<Order> yourOrder = holderForYourOrder.expose;

    /** The initial execution. */
    public final Variable<Execution> init = Variable.empty();

    /** The latest execution. */
    public final Variable<Execution> latest = Variable.of(Execution.NONE);

    /** The latest price. */
    public final Variable<Num> price = latest.observeNow().map(e -> e.price).diff().to();

    /** The order manager. */
    public final OrderManager orders = new OrderManager();

    /** The position manager. */
    public final PositionManager positions = new PositionManager(price);

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

    /** The order manager. */
    private final List<Order> orderItems = new CopyOnWriteArrayList();

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

        // initialize price, balance and executions
        this.base = this.baseInit = backend.getBaseCurrency().to().v;
        this.target = this.targetInit = backend.getTargetCurrency().to().v;

        add(trading);

        backend.initialize(this, log);

        orderTimeline = backend.getOrderBook();
        backend.add(orderTimeline.on(Viewtify.UIThread).to(board -> {
            orderBook.shorts.update(board.asks);
            orderBook.longs.update(board.bids);
        }));
        backend.add(timeline.throttle(2, TimeUnit.SECONDS).to(e -> {
            // fix error board
            orderBook.shorts.fix(e.price);
            orderBook.longs.fix(e.price);
        }));
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
     * Request order actually.
     * 
     * @param order A order to request.
     * @return A requested order.
     */
    public final Signal<Order> request(Order order) {
        order.state.set(REQUESTING);

        return backend.request(order).retryWhen(fail -> fail.take(40).delay(100, MILLISECONDS)).map(id -> {
            order.id = id;
            order.created.set(ZonedDateTime.now());
            order.averagePrice.set(order.price);
            order.remainingSize.set(order.size);
            order.state.set(ACTIVE);

            orderItems.add(order);

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
     * @param order A order to cancel.
     * @return A canceled order.
     */
    public final Signal<Order> cancel(Order order) {
        if (order.state.is(ACTIVE) || order.state.is(State.REQUESTING)) {
            State previous = order.state.set(REQUESTING);

            return backend.cancel(order).effect(o -> {
                orderItems.remove(o);
                o.state.set(CANCELED);
            }).effectOnError(e -> {
                order.state.set(previous);
            });
        } else {
            return I.signal(order);
        }
    }

    /**
     * List up all orders.
     * 
     * @return A list of all orders.
     */
    public final List<Order> orders() {
        return backend.orders().toList();
    }

    /**
     * Create new price signal.
     * 
     * @param price
     * @return
     */
    public final Signal<Execution> signalByPrice(Num price) {
        if (latest.v.price.isLessThan(price)) {
            return timeline.take(e -> e.price.isGreaterThanOrEqual(price)).take(1);
        } else {
            return timeline.take(e -> e.price.isLessThanOrEqual(price)).take(1);
        }
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

        for (Order order : orderItems) {
            if (order.id().equals(exe.buy_child_order_acceptance_id) || order.id().equals(exe.sell_child_order_acceptance_id)) {
                update(order, exe);

                order.listeners.accept(exe);

                Position position = new Position();
                position.side = order.side;
                position.price = exe.price;
                position.size.set(exe.size);
                position.date = exe.exec_date;
                positions.add(position);
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
        Num executed = Num.min(order.remainingSize, exe.size);

        if (order.child_order_type.isMarket() && executed.isNot(0)) {
            order.averagePrice.set(v -> v.multiply(order.executed_size)
                    .plus(exe.price.multiply(executed))
                    .divide(executed.plus(order.executed_size)));
        }

        order.executed_size.set(v -> v.plus(executed));
        order.remainingSize.set(v -> v.minus(executed));

        if (order.remainingSize.is(Num.ZERO)) {
            order.state.set(State.COMPLETED);
            orderItems.remove(order); // complete order
        }

        // pairing order and execution
        order.executions.add(exe);
    }
}
