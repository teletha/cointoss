/*
 * Copyright (C) 2021 cointoss Development Team
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          https://opensource.org/licenses/MIT
 */
package cointoss;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;
import java.util.function.Function;

import cointoss.execution.Execution;
import cointoss.execution.ExecutionLog;
import cointoss.market.MarketServiceProvider;
import cointoss.order.Order;
import cointoss.order.OrderBookManager;
import cointoss.order.OrderManager;
import cointoss.order.OrderStrategy.Makable;
import cointoss.order.OrderStrategy.Orderable;
import cointoss.order.OrderStrategy.Takable;
import cointoss.ticker.Span;
import cointoss.ticker.Tick;
import cointoss.ticker.TickerManager;
import cointoss.trade.Funds;
import cointoss.trade.Trader;
import cointoss.util.Chrono;
import cointoss.util.arithmetic.Num;
import cointoss.verify.TrainingMarket;
import cointoss.volume.PriceRangedVolumeManager;
import kiss.Disposable;
import kiss.Signal;
import kiss.Signaling;

/**
 * Facade for the market related operations.
 */
public class Market implements Disposable {

    /** The cache. */
    private static final Map<MarketService, Market> cachedMarket = new ConcurrentHashMap();

    /** The cache. */
    private static final Map<MarketService, Market> cachedTrainingMarket = new ConcurrentHashMap();

    /** The empty object. */
    public static final Execution BASE = Execution.with.buy(0).date(Chrono.utc(2000, 1, 1));

    /** The target market servicce. */
    public final MarketService service;

    /** The order manager. */
    public final OrderManager orders;

    /** The order books. */
    public final OrderBookManager orderBook;

    /** The price volume manager. */
    public final PriceRangedVolumeManager priceVolume;

    /** The ticker manager. */
    public final TickerManager tickers;

    /** The execution observers. */
    protected final Signaling<Execution> timelineObservers = new Signaling();

    /** The execution timeline. */
    public final Signal<Execution> timeline = timelineObservers.expose.skipComplete();

    private final AtomicReference<Execution> switcher = new AtomicReference<>(Market.BASE);

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

    /** The base trader. */
    private Trader trader;

    /** The managed {@link Trader}. */
    public final List<Trader> traders = Collections.unmodifiableList(managedTraders);

    public static Market of(MarketService service) {
        return cachedMarket.computeIfAbsent(service, Market::new);
    }

    public static Market trainingOf(MarketService service) {
        return cachedTrainingMarket.computeIfAbsent(service, key -> new TrainingMarket(of(key)));
    }

    /**
     * Build {@link Market} with the specified {@link MarketServiceProvider}.
     * 
     * @param provider A market provider.
     */
    protected Market(MarketService service) {
        this.service = Objects.requireNonNull(service, "Market is not found.");
        this.orders = createOrderManager();
        this.orderBook = createOrderBookManager();
        this.priceVolume = createPriceRangedVolumeManager();
        this.tickers = createTickerManager();

        // build tickers for each span
        timeline.to(e -> {
            tickers.update(e);
            priceVolume.update(e);
        });
        tickers.on(Span.Hour8).open.to(priceVolume::start);

        // manage disposer
        add(orderBook);
    }

    /**
     * Create a new {@link OrderManager} for this {@link Market}.
     * <p>
     * The method is defined for smooth delegation. Therefore, it is usually not possible to call or
     * override this method from outside the cointoss.verify package.
     * 
     * @return
     */
    protected OrderManager createOrderManager() {
        return new OrderManager(service);
    }

    /**
     * Create a new {@link OrderBookManager} for this {@link Market}.
     * <p>
     * The method is defined for smooth delegation. Therefore, it is usually not possible to call or
     * override this method from outside the cointoss.verify package.
     * 
     * @return
     */
    protected OrderBookManager createOrderBookManager() {
        return new OrderBookManager(service, service.executionsRealtimely().throttle(1, TimeUnit.SECONDS).map(Execution::price));
    }

    /**
     * Create a new {@link PriceRangedVolumeManager} for this {@link Market}.
     * <p>
     * The method is defined for smooth delegation. Therefore, it is usually not possible to call or
     * override this method from outside the cointoss.verify package.
     * 
     * @return
     */
    protected PriceRangedVolumeManager createPriceRangedVolumeManager() {
        return new PriceRangedVolumeManager(service.setting.recommendedPriceRange());
    }

    /**
     * Create a new {@link TickerManager} for this {@link Market}.
     * <p>
     * The method is defined for smooth delegation. Therefore, it is usually not possible to call or
     * override this method from outside the cointoss.verify package.
     * 
     * @return
     */
    protected TickerManager createTickerManager() {
        return new TickerManager();
    }

    public synchronized Trader trader() {
        if (trader == null) {
            trader = new Trader() {
                @Override
                protected void declareStrategy(Market market, Funds fund) {
                }
            };
            register(trader);
        }
        return trader;
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
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return service.marketReadableName;
    }
}