/*
 * Copyright (C) 2020 cointoss Development Team
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          https://opensource.org/licenses/MIT
 */
package cointoss;

import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;
import java.util.function.Function;

import cointoss.execution.Execution;
import cointoss.execution.ExecutionLog;
import cointoss.market.MarketServiceProvider;
import cointoss.order.Order;
import cointoss.order.OrderBook;
import cointoss.order.OrderBookManager;
import cointoss.order.OrderManager;
import cointoss.order.OrderStrategy;
import cointoss.order.OrderStrategy.Cancellable;
import cointoss.order.OrderStrategy.Makable;
import cointoss.order.OrderStrategy.Orderable;
import cointoss.order.OrderStrategy.Takable;
import cointoss.ticker.PriceRangedVolumeManager;
import cointoss.ticker.Span;
import cointoss.ticker.Tick;
import cointoss.ticker.TickerManager;
import cointoss.trade.Trader;
import cointoss.util.Chrono;
import cointoss.util.Num;
import cointoss.util.RetryPolicy;
import kiss.Disposable;
import kiss.I;
import kiss.Observer;
import kiss.Signal;
import kiss.Signaling;
import kiss.WiseConsumer;

/**
 * Facade for the market related operations.
 */
public class Market implements Disposable {

    /** The empty object. */
    public static final Execution BASE = Execution.with.buy(0).date(Chrono.utc(2000, 1, 1));

    private final AtomicReference<Execution> switcher = new AtomicReference<>(Market.BASE);

    /** The target market servicce. */
    public final MarketService service;

    /** The execution observers. */
    protected final Signaling<Execution> timelineObservers = new Signaling();

    /** The order manager. */
    public final OrderManager orders;

    /** The order books. */
    public final OrderBookManager orderBook;

    /** The price volume manager. */
    public final PriceRangedVolumeManager priceVolume;

    /** The ticker manager. */
    public final TickerManager tickers = new TickerManager();

    /** The execution time line. */
    public final Signal<Execution> timeline = timelineObservers.expose.skipComplete();

    /** The execution time line by taker. */
    public final Signal<Execution> timelineByTaker = timeline.map(e -> {
        Execution previous = switcher.getAndSet(e);

        if (previous.direction == e.direction && (e.consecutive == Execution.ConsecutiveSameBuyer || e.consecutive == Execution.ConsecutiveSameSeller)) {
            // same taker
            e.assignAccumulative(v -> previous.accumulative + v);
            return null;
        }
        return previous;
    }).skip(e -> e == null || e == Market.BASE);

    /** The managed {@link Trader}. */
    private final List<Trader> managedTraders = new ArrayList();

    /** The managed {@link Trader}. */
    public final List<Trader> traders = Collections.unmodifiableList(managedTraders);

    /** Flag */
    private boolean readingLog = false;

    /**
     * Build {@link Market} with the specified {@link MarketServiceProvider}.
     * 
     * @param provider A market provider.
     */
    public Market(MarketService service) {
        this.service = Objects.requireNonNull(service, "Market is not found.");
        this.orders = new OrderManager(service);
        this.orderBook = new OrderBookManager(service);
        this.priceVolume = new PriceRangedVolumeManager(service.setting.base.minimumSize.multiply(10), service.setting.base.scale);

        // build tickers for each span
        timeline.to(e -> {
            tickers.update(e);
            priceVolume.update(e);
        });

        tickers.on(Span.Day3).open.to(priceVolume::update);

        readOrderBook();
    }

    public void register(Trader... traders) {
        register(List.of(traders));
    }

    public void register(List<Trader> traders) {
        for (Trader trader : traders) {
            trader.initialize(this);
            this.managedTraders.add(trader);
        }
    }

    /**
     * Start reading {@link OrderBook}.
     */
    protected void readOrderBook() {
        RetryPolicy policy = service.retryPolicy(500, "OrderBook");

        // orderbook management
        service.add(service.orderBookRealtimely().retryWhen(policy).to(board -> {
            orderBook.shorts.update(board.asks);
            orderBook.longs.update(board.bids);
        }));
        service.add(timeline.skipWhile(e -> readingLog).throttle(1, TimeUnit.SECONDS).to(e -> {
            // fix error board
            orderBook.shorts.fix(e.price);
            orderBook.longs.fix(e.price);
        }));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void vandalize() {
        service.dispose();
        tickers.dispose();
        for (Trader trader : managedTraders) {
            trader.dispose();
        }
    }

    /**
     * Shorthand for {@link OrderManager#request(Order)}.
     * 
     * @param order A order to request.
     * @return A order request process.
     */
    public final Signal<Order> request(Order order) {
        return orders.request(order);
    }

    /**
     * Order with specified direction and quantity. You can also declaratively describe the ordering
     * strategy. If you do not describe the ordering strategy, it will be processed as a market
     * order.
     * 
     * @param <S> {@link Takable} or {@link Makable}.
     * @param directional A dirction of the order.
     * @param size A quantity of the order.
     * @param declaration Your order strategy.
     * @return Returns all the {@link Order}s used in this strategy. Complete event occurs when all
     *         strategies are performed. (Note: not when all orders are completed but when the
     *         strategy is completed)
     */
    public final Signal<Order> request(Directional directional, long size, Consumer<Orderable> declaration) {
        return request(directional, Num.of(size), declaration);
    }

    /**
     * Order with specified direction and quantity. You can also declaratively describe the ordering
     * strategy. If you do not describe the ordering strategy, it will be processed as a market
     * order.
     * 
     * @param <S> {@link Takable} or {@link Makable}.
     * @param directional A dirction of the order.
     * @param size A quantity of the order.
     * @param declaration Your order strategy.
     * @return Returns all the {@link Order}s used in this strategy. Complete event occurs when all
     *         strategies are performed. (Note: not when all orders are completed but when the
     *         strategy is completed)
     */
    public final Signal<Order> request(Directional directional, double size, Consumer<Orderable> declaration) {
        return request(directional, Num.of(size), declaration);
    }

    /**
     * Order with specified direction and quantity. You can also declaratively describe the ordering
     * strategy. If you do not describe the ordering strategy, it will be processed as a market
     * order.
     * 
     * @param <S> {@link Takable} or {@link Makable}.
     * @param directional A dirction of the order.
     * @param size A quantity of the order.
     * @param declaration Your order strategy.
     * @return Returns all the {@link Order}s used in this strategy. Complete event occurs when all
     *         strategies are performed. (Note: not when all orders are completed but when the
     *         strategy is completed)
     */
    public final Signal<Order> request(Directional directional, Num size, Consumer<Orderable> declaration) {
        return new Signal<>((observer, disposer) -> {
            MarketOrderStrategy strategy = new MarketOrderStrategy();
            declaration.accept(strategy);

            if (strategy.actions.isEmpty()) {
                strategy.take();
            }

            strategy.execute(this, directional.direction(), size, null, observer);

            return disposer;
        });
    }

    /**
     * Shorthand for {@link OrderManager#cancel(Order)}.
     * 
     * @param order A order to cancel.
     * @return A order cancel process.
     */
    public final Signal<Order> cancel(Order order) {
        return orders.cancel(order);
    }

    /**
     * Stop all positions.
     */
    public final Signal<Order> stop() {
        return stop(null);
    }

    /**
     * Stop all positions with {@link Orderable}.
     */
    public final Signal<Order> stop(WiseConsumer<Orderable> strategy) {
        return I.signal();
        // if (orders.hasNoPosition()) {
        // return I.signal();
        // }
        //
        // if (strategy == null) {
        // strategy = I.recurse((self, s) -> {
        // if (orders.positionPrice.v.isLessThan(orders.positionDirection(), latestPrice())) {
        // // loss
        // s.makeBestPrice(orders.positionDirection()).cancelAfter(2, ChronoUnit.SECONDS).take();
        // } else {
        // // profit
        // s.makeBestPrice(orders.positionDirection().inverse()).cancelAfter(5,
        // ChronoUnit.SECONDS).next(self);
        // }
        // });
        // }
        // return request(orders.positionDirection().inverse(), orders.positionSize.v, strategy);
    }

    /**
     * Stop and reverse all positions.
     */
    public final Signal<Order> reverse() {
        return request(Direction.BUY, 0.01, s -> s.makeBestBuyPrice()).merge(request(Direction.SELL, 0.01, s -> s.makeBestSellPrice()));
    }

    /**
     * Create new price signal.
     * 
     * @param price
     * @return
     */
    public final Signal<Execution> signalByPrice(Num price) {
        if (tickers.latest.v.price.isLessThan(price)) {
            return timeline.take(e -> e.price.isGreaterThanOrEqual(price)).take(1);
        } else {
            return timeline.take(e -> e.price.isLessThanOrEqual(price)).take(1);
        }
    }

    /**
     * Read {@link Execution} log.
     * 
     * @param log
     * @return
     */
    public final Market readLog(Function<ExecutionLog, Signal<Execution>> log) {
        readingLog = true;
        service.add(log.apply(service.log).to(timelineObservers));
        readingLog = false;

        return this;
    }

    /**
     * Shortcut method for {@link Tick} open timing.
     * 
     * @param span A target time span.
     * @return A tick stream.
     */
    public final Signal<Tick> open(Span span) {
        return tickers.on(span).open;
    }

    /**
     * Shortcut method for {@link Tick} close timing.
     * 
     * @param span A target time span.
     * @return A tick stream.
     */
    public final Signal<Tick> close(Span span) {
        return tickers.on(span).close;
    }

    /**
     * 
     */
    private class MarketOrderStrategy implements Orderable, Takable, Makable, Cancellable {

        /** The action sequence. */
        private final LinkedList<OrderAction> actions = new LinkedList();

        /**
         * {@inheritDoc}
         */
        @Override
        public OrderStrategy next(Consumer<Orderable> strategy) {
            if (strategy != null) {
                actions.add((market, direction, size, previous, orders) -> {
                    strategy.accept(this);
                    execute(market, direction, size, previous, orders);
                });
            }
            return this;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public OrderStrategy take() {
            actions.add((market, direction, size, previous, orders) -> {
                Order order = Order.with.direction(direction, size);
                orders.accept(order);

                market.orders.request(order).to(() -> {
                    execute(market, direction, size, order, orders);
                });
            });
            return this;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public Cancellable make(Num price) {
            actions.add((market, direction, size, previous, orders) -> {
                make(price, market, direction, size, previous, orders);
            });
            return this;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public Cancellable makeBestPrice() {
            actions.add((market, direction, size, previous, orders) -> {
                make(market.orderBook.bookFor(direction)
                        .computeBestPrice(market.service.setting.base.minimumSize), market, direction, size, previous, orders);
            });
            return this;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public Cancellable makeBestPrice(Direction directionForBestPrice) {
            actions.add((market, direction, size, previous, orders) -> {
                make(market.orderBook.bookFor(directionForBestPrice)
                        .computeBestPrice(market.service.setting.base.minimumSize), market, direction, size, previous, orders);
            });
            return this;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public Cancellable makePositionPrice() {
            throw new Error("FIX ME");
            // actions.add((market, direction, size, previous, orders) -> {
            // make(market.orders.positionPrice.v
            // .scale(market.service.setting.baseCurrencyScaleSize), market, direction, size,
            // previous, orders);
            // });
            // return this;
        }

        /**
         * Request make order.
         * 
         * @param price
         * @param market
         * @param direction
         * @param size
         * @param previous
         * @param orders
         */
        private void make(Num price, Market market, Direction direction, Num size, Order previous, Observer<? super Order> orders) {
            Order order = Order.with.direction(direction, size).price(price);
            orders.accept(order);

            market.orders.request(order).to(() -> {
                execute(market, direction, size, order, orders);
            });
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public Orderable cancelAfter(long time, ChronoUnit unit) {
            return cancelWhen(I.schedule(time, TimeUnit.of(unit), service.scheduler()));
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public Orderable cancelWhen(Signal<?> timing) {
            actions.add((market, direction, size, previous, orders) -> {
                if (previous != null && previous.isNotCompleted()) {
                    timing.first().to(() -> {
                        if (previous.isNotCompleted()) {
                            market.orders.cancel(previous).to(() -> {
                                if (previous.remainingSize.isPositive()) {
                                    execute(market, direction, previous.remainingSize, null, orders);
                                }
                            });
                        }
                    });
                }
            });
            return this;
        }

        /**
         * Execute next order action.
         * 
         * @param market
         * @param direction
         * @param size
         */
        private void execute(Market market, Direction direction, Num size, Order previous, Observer<? super Order> observer) {
            OrderAction action = actions.pollFirst();

            if (action == null) {
                observer.complete();
            } else {
                action.execute(market, direction, size, previous, observer);
            }
        }
    }

    /**
     * 
     */
    private interface OrderAction {

        void execute(Market market, Direction direction, Num num, Order order, Observer<? super Order> observer);
    }
}