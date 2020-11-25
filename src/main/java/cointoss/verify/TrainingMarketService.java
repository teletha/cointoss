/*
 * Copyright (C) 2019 Nameless Production Committee
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          http://opensource.org/licenses/mit-license.php
 */
package cointoss.verify;

import java.lang.reflect.Method;

import org.apache.commons.lang3.RandomUtils;

import cointoss.Market;
import cointoss.MarketService;
import cointoss.execution.Execution;
import cointoss.order.Order;
import cointoss.order.OrderBookManager;
import cointoss.order.OrderBookPageChanges;
import cointoss.order.OrderManager;
import cointoss.order.OrderState;
import cointoss.util.EfficientWebSocket;
import cointoss.util.arithmetic.Num;
import kiss.I;
import kiss.Signal;

public class TrainingMarketService extends MarketService {

    private final MarketService backend;

    private final OrderBookManager orderbooks;

    final VerifiableMarketService frontend;

    /**
     * @param backend
     */
    public TrainingMarketService(Market backend) {
        super(backend.service.exchange, backend.service.marketName, backend.service.setting);
        this.backend = backend.service;
        this.orderbooks = backend.orderBook;
        this.frontend = new VerifiableMarketService(backend.service);
    }

    /**
     * Helper to delegate the internal method by reflection.
     * 
     * @param <T>
     * @param type
     * @param name
     * @return
     */
    private <T> T delegateInternal(Class<T> type, String name) {
        try {
            Method method = MarketService.class.getDeclaredMethod(name);
            method.setAccessible(true);
            return (T) method.invoke(backend);
        } catch (Exception e) {
            throw I.quiet(e);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected EfficientWebSocket clientRealtimely() {
        return delegateInternal(EfficientWebSocket.class, "clientRealtimely");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Signal<Integer> delay() {
        return backend.delay();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Signal<String> request(Order order) {
        System.out.println("request " + order);
        if (order.type.isTaker()) {
            String id = "ID" + RandomUtils.nextInt();
            return I.signal(id).effectOnComplete(() -> {
                frontend.orderUpdateRealtimely.accept(OrderManager.Update
                        .execute(id, order.size, orderbooks.by(order.inverse()).predictTakingPrice(order.size), Num.ZERO));
            });
        } else {
            return frontend.request(order);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Signal<Order> cancel(Order order) {
        System.out.println("cancel " + order);
        return frontend.cancel(order);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Signal<Execution> executions(long startId, long endId) {
        return backend.executions(startId, endId);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected Signal<Execution> connectExecutionRealtimely() {
        return delegateInternal(Signal.class, "connectExecutionRealtimely");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Signal<Execution> executionLatest() {
        return backend.executionLatest();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Signal<Execution> executionLatestAt(long id) {
        return backend.executionLatestAt(id);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Signal<Order> orders() {
        return frontend.orders();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Signal<Order> orders(OrderState state) {
        return frontend.orders(state);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected Signal<Order> connectOrdersRealtimely() {
        return frontend.connectOrdersRealtimely();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Signal<OrderBookPageChanges> orderBook() {
        return backend.orderBook();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected Signal<OrderBookPageChanges> connectOrderBookRealtimely() {
        return delegateInternal(Signal.class, "connectOrderBookRealtimely");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Signal<Num> baseCurrency() {
        return backend.baseCurrency();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Signal<Num> targetCurrency() {
        return backend.targetCurrency();
    }
}
