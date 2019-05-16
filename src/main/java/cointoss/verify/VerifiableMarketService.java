/*
 * Copyright (C) 2019 CoinToss Development Team
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          https://opensource.org/licenses/MIT
 */
package cointoss.verify;

import java.time.ZonedDateTime;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.ConcurrentLinkedQueue;

import cointoss.Direction;
import cointoss.Directional;
import cointoss.MarketService;
import cointoss.MarketSetting;
import cointoss.execution.Execution;
import cointoss.order.Order;
import cointoss.order.OrderBookChange;
import cointoss.order.OrderState;
import cointoss.order.OrderType;
import cointoss.order.QuantityCondition;
import cointoss.util.Chrono;
import cointoss.util.Num;
import cointoss.util.RetryPolicy;
import kiss.Disposable;
import kiss.I;
import kiss.Signal;
import kiss.Signaling;
import kiss.Ⅲ;

public class VerifiableMarketService extends MarketService {

    /** The terminator. */
    private final Disposable diposer = Disposable.empty();

    /** The managed id. */
    private int id = 0;

    /** The order manager. */
    private final ConcurrentLinkedDeque<BackendOrder> orderActive = new ConcurrentLinkedDeque<>();

    /** The order manager. */
    private final ConcurrentLinkedQueue<BackendOrder> orderAll = new ConcurrentLinkedQueue<>();

    /** The order manager. */
    private final Signaling<Ⅲ<Direction, String, Execution>> positions = new Signaling();

    /** The execution manager. */
    private final LinkedList<Execution> executeds = new LinkedList();

    /** The initial base currency. */
    private final Num baseCurrency = Num.HUNDRED;

    /** The initial target currency. */
    private final Num targetCurrency = Num.ZERO;

    /** The latest execution time. */
    private ZonedDateTime now = Chrono.MIN;

    /** The lag emulator. */
    public Latency latency = Latency.zero();

    /**
     * 
     */
    public VerifiableMarketService() {
        super("TestableExchange", "TestableMarket", MarketSetting.builder()
                .baseCurrencyMinimumBidPrice(Num.ONE)
                .targetCurrencyMinimumBidSize(Num.ONE)
                .orderBookGroupRanges(Num.of(1))
                .retryPolicy(new RetryPolicy().retryMaximum(0))
                .build());
    }

    /**
     * 
     */
    public VerifiableMarketService(MarketService delegation) {
        super(delegation.exchangeName, delegation.marketName, delegation.setting.withRetryPolicy(new RetryPolicy().retryMaximum(0)));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Signal<Integer> delay() {
        return I.signal(0);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void vandalize() {
        diposer.dispose();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Signal<String> request(Order order) {
        return I.signal(order).map(o -> {
            BackendOrder child = new BackendOrder(order);
            child.id = "LOCAL-ACCEPTANCE-" + id++;
            child.state = OrderState.ACTIVE;
            child.creationTime = latency.emulate(now());
            child.remainingSize = order.size;

            orderAll.add(child);
            orderActive.add(child);
            return child.id;
        });
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Signal<Order> cancel(Order order) {
        BackendOrder backend = findBy(order);

        // associated backend order is not found, do nothing
        if (backend == null) {
            return I.signal();
        }

        // when latency is zero, cancel order immediately
        ZonedDateTime delay = latency.emulate(now);

        if (delay == now) {
            backend.cancel();
            return I.signal(order);
        }

        // backend order will be canceled in the specified delay
        backend.cancelTimeMills = Chrono.epochMills(delay);

        return backend.canceling.expose;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Signal<Execution> executionsRealtimely() {
        return I.signal(executeds);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Signal<Ⅲ<Direction, String, Execution>> executionsRealtimelyForMe() {
        return positions.expose;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Signal<Execution> executionLatest() {
        // If this exception will be thrown, it is bug of this program. So we must rethrow the
        // wrapped error in here.
        throw new Error();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Signal<Execution> executions(long start, long end) {
        return I.signal(executeds);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Signal<Order> orders() {
        return I.signal(orderAll).map(o -> {
            Order order = Order.with.direction(o.direction, o.size)
                    .price(o.price)
                    .quantityCondition(o.condition)
                    .remainingSize(o.remainingSize);
            order.id.set(o.id);
            order.state.set(o.state);
            order.executedSize.set(o.executedSize);

            return order;
        });
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Signal<Num> baseCurrency() {
        return I.signal(baseCurrency);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Signal<Num> targetCurrency() {
        return I.signal(targetCurrency);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Signal<OrderBookChange> orderBook() {
        return Signal.never();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ZonedDateTime now() {
        return now;
    };

    /**
     * Emulate {@link Execution}.
     * 
     * @param e
     * @return
     */
    public Execution emulate(Execution e) {
        now = e.date;

        // emulate market execution
        Iterator<BackendOrder> iterator = orderActive.iterator();

        while (iterator.hasNext()) {
            BackendOrder order = iterator.next();

            // time base filter
            if (e.date.isBefore(order.creationTime)) {
                continue;
            }

            // check canceling time
            if (order.cancelTimeMills != 0 && order.cancelTimeMills <= e.mills) {
                order.cancel();
                continue;
            }

            // check quantity condition
            if (order.condition == QuantityCondition.FillOrKill && !validateTradable(order, e)) {
                iterator.remove();
                orderAll.remove(order);
                continue;
            }

            if (order.condition == QuantityCondition.ImmediateOrCancel) {
                if (validateTradableByPrice(order, e)) {
                    order.remainingSize = Num.min(e.size, order.remainingSize);
                } else {
                    iterator.remove();
                    orderAll.remove(order);
                    continue;
                }
            }

            if (validateTradableByPrice(order, e)) {
                Num executedSize = Num.min(e.size, order.remainingSize);
                if (order.type.isMarket() && executedSize.isNot(0)) {
                    order.marketMinPrice = order.isBuy() ? Num.max(order.marketMinPrice, e.price) : Num.min(order.marketMinPrice, e.price);
                    order.price = order.price.multiply(order.executedSize)
                            .plus(order.marketMinPrice.multiply(executedSize))
                            .divide(executedSize.plus(order.executedSize));
                }
                order.executedSize = order.executedSize.plus(executedSize);
                order.remainingSize = order.remainingSize.minus(executedSize);

                Execution exe = Execution.with.direction(order.direction(), executedSize)
                        .price(order.type.isMarket() ? order.marketMinPrice : order.price)
                        .date(e.date);
                executeds.add(exe);

                if (order.remainingSize.isZero()) {
                    order.state = OrderState.COMPLETED;
                    iterator.remove();
                }
                positions.accept(I.pair(exe.direction, order.id, exe));

                // replace execution info
                return Execution.with.direction(e.direction, exe.size)
                        .price(exe.price)
                        .date(e.date)
                        .id(e.id)
                        .consecutive(e.consecutive)
                        .delay(e.delay);
            }
        }
        return e;
    }

    /**
     * Test whether this order can trade with the specified {@link Execution}.
     * 
     * @param e A target {@link Execution}.
     * @return A result.
     */
    private boolean validateTradable(BackendOrder order, Execution e) {
        return validateTradableBySize(order, e) && validateTradableByPrice(order, e);
    }

    /**
     * Test whether this order price can trade with the specified {@link Execution}.
     * 
     * @param e A target {@link Execution}.
     * @return A result.
     */
    private boolean validateTradableByPrice(BackendOrder order, Execution e) {
        if (order.type == OrderType.MARKET) {
            return true;
        }

        if (order.isBuy()) {
            Num price = order.price;

            return price.isGreaterThan(e.price) || price.is(setting.baseCurrencyMinimumBidPrice());
        } else {
            return order.price.isLessThan(e.price);
        }
    }

    /**
     * Test whether this order size can trade with the specified {@link Execution}.
     * 
     * @param e A target {@link Execution}.
     * @return A result.
     */
    private boolean validateTradableBySize(BackendOrder order, Execution e) {
        return order.size.isLessThanOrEqual(e.size);
    }

    /**
     * Find {@link BackendOrder} by fronend {@link Order}.
     * 
     * @param order
     * @return
     */
    private BackendOrder findBy(Order order) {
        for (BackendOrder back : orderActive) {
            if (back.front == order) {
                return back;
            }
        }
        return null;
    }

    /**
     * For test.
     */
    private class BackendOrder implements Directional {

        /** The frontend order. */
        private final Order front;

        /** The order direction. */
        private final Direction direction;

        /** The order size. */
        private final Num size;

        /** The order id. */
        private String id;

        /** The order price. */
        private Num price;

        /** The order type. */
        private OrderType type;

        /** The order type. */
        private QuantityCondition condition;

        /** The created time. */
        private ZonedDateTime creationTime;

        /** The order state. */
        private OrderState state;

        /** The order state. */
        private Num remainingSize;

        /** The order state. */
        private Num executedSize;

        /** The minimum price for market order. */
        private Num marketMinPrice;

        /**
         * The time which this order is created, Using epoch mills to make time-related calculation
         * faster.
         */
        private long createTimeMills;

        /**
         * The time which this order will be canceled completely. Using epoch mills to make
         * time-related calculation faster.
         */
        private long cancelTimeMills;

        /** The cancel event emitter. */
        private final Signaling<Order> canceling = new Signaling();

        /**
         * Create backend managed order.
         * 
         * @param o
         */
        private BackendOrder(Order o) {
            this.front = o;
            this.direction = o.direction;
            this.size = o.size;
            this.remainingSize = o.remainingSize;
            this.executedSize = o.executedSize.v;
            this.price = o.price;
            this.type = o.type;
            this.condition = o.quantityCondition;
            this.creationTime = o.creationTime.v;
            this.marketMinPrice = isBuy() ? Num.ZERO : Num.MAX;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public Direction direction() {
            return direction;
        }

        /**
         * Cancel this order actually.
         */
        private void cancel() {
            orderActive.removeIf(o -> o.id.equals(id));
            I.signal(orderAll).take(o -> o.id.equals(id)).take(1).to(o -> {
                o.state = OrderState.CANCELED;
                canceling.accept(front);
                canceling.complete();
            });
        }
    }
}
