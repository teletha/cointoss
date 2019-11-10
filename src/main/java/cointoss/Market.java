/*
 * Copyright (C) 2019 CoinToss Development Team
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          https://opensource.org/licenses/MIT
 */
package cointoss;

import java.time.temporal.ChronoUnit;
import java.util.LinkedList;
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
import cointoss.ticker.TickerManager;
import cointoss.util.Chrono;
import cointoss.util.Num;
import cointoss.util.PentaConsumer;
import cointoss.util.Retry;
import kiss.Disposable;
import kiss.I;
import kiss.Observer;
import kiss.Signal;
import kiss.Signaling;
import kiss.WiseConsumer;

/**
 * Facade for the market related operations.
 * 
 * @version 2018/08/14 1:04:19
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

    /** The ticker manager. */
    public final TickerManager tickers = new TickerManager();

    /** The execution time line. */
    public final Signal<Execution> timeline = timelineObservers.expose.skipComplete();

    /** The execution time line with current value. */
    public final Signal<Execution> timelineNow = timeline.startWith(tickers.latest);

    /** The execution time line by taker. */
    public final Signal<Execution> timelineByTaker = timeline.map(e -> {
        Execution previous = switcher.getAndSet(e);

        if (e.consecutive == Execution.ConsecutiveSameBuyer || e.consecutive == Execution.ConsecutiveSameSeller) {
            // same taker
            e.assignAccumulative(v -> previous.accumulative.plus(v));
            return null;
        }
        return previous;
    }).skip(e -> e == null || e == Market.BASE);

    /**
     * Build {@link Market} with the specified {@link MarketServiceProvider}.
     * 
     * @param provider A market provider.
     */
    public Market(MarketService service) {
        this.service = Objects.requireNonNull(service, "Market is not found.");
        this.orders = new OrderManager(service);
        this.orderBook = new OrderBookManager(service);

        // build tickers for each span
        timeline.to(tickers::update);

        readOrderBook();
    }

    /**
     * Start reading {@link OrderBook}.
     */
    protected void readOrderBook() {
        Retry policy = service.setting.retryPolicy();

        // orderbook management
        service.add(service.orderBook().retryWhen(policy).to(board -> {
            orderBook.shorts.update(board.asks);
            orderBook.longs.update(board.bids);
            policy.reset();
        }));
        service.add(timeline.throttle(2, TimeUnit.SECONDS).to(e -> {
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
        service.add(log.apply(service.log).to(timelineObservers));

        return this;
    }

    /**
     * 
     */
    private class MarketOrderStrategy implements Orderable, Takable, Makable, Cancellable {

        /** The action sequence. */
        private final LinkedList<PentaConsumer<Market, Direction, Num, Order, Observer<? super Order>>> actions = new LinkedList();

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
                        .computeBestPrice(market.service.setting.baseCurrencyMinimumBidPrice), market, direction, size, previous, orders);
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
                        .computeBestPrice(market.service.setting.baseCurrencyMinimumBidPrice), market, direction, size, previous, orders);
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
            return cancelWhen(I.signal(time, 0, TimeUnit.of(unit), service.scheduler()));
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
            PentaConsumer<Market, Direction, Num, Order, Observer<? super Order>> action = actions.pollFirst();

            if (action == null) {
                observer.complete();
            } else {
                action.accept(market, direction, size, previous, observer);
            }
        }
    }
}
