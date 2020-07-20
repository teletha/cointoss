/*
 * Copyright (C) 2018 Nameless Production Committee
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          http://opensource.org/licenses/mit-license.php
 */
package cointoss.market;

import java.lang.reflect.Method;
import java.time.ZonedDateTime;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;

import com.pgssoft.httpclient.HttpClientMock;
import com.pgssoft.httpclient.RecordableHttpClientMock;

import antibug.WebSocketServer;
import antibug.WebSocketServer.WebSocketClient;
import cointoss.MarketService;
import cointoss.execution.Execution;
import cointoss.order.Order;
import cointoss.order.OrderBookPageChanges;
import cointoss.order.OrderState;
import cointoss.util.EfficientWebSocket;
import cointoss.util.Num;
import cointoss.util.RetryPolicy;
import kiss.Disposable;
import kiss.I;
import kiss.Signal;
import kiss.WiseSupplier;

public class MarketServiceMock extends MarketService {

    /** The mocked http client interface. */
    public final HttpClientMock httpClient = RecordableHttpClientMock.build();

    /** The mocked websocket server. */
    public final WebSocketServer websocketServer = new WebSocketServer();

    /** The websocket client interface. */
    public final WebSocketClient websocketClient = websocketServer.websocketClient();

    /** The delegated service. */
    private final MarketService service;

    private final WiseSupplier<EfficientWebSocket> clientRealtimely;

    private final WiseSupplier<Signal<Execution>> connectExecutionRealtimely;

    private boolean debug;

    /**
     * 
     */
    public static MarketServiceMock mock(MarketService service) {
        return new MarketServiceMock(service);
    }

    /**
     * @param exchangeName
     * @param marketName
     * @param setting
     * @param service
     */
    public MarketServiceMock(MarketService service) {
        super(service.exchangeName, service.marketName, service.setting);
        this.service = service;
        this.clientRealtimely = method("clientRealtimely");
        this.connectExecutionRealtimely = method("connectExecutionRealtimely");
    }

    private <T> WiseSupplier<T> method(String methodName) {
        try {
            Method method = MarketService.class.getDeclaredMethod(methodName);
            method.setAccessible(true);

            return () -> (T) method.invoke(service);
        } catch (Exception e) {
            throw I.quiet(e);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected EfficientWebSocket clientRealtimely() {
        EfficientWebSocket ws = clientRealtimely.get();
        if (debug) ws.enableDebug();
        return ws;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected Signal<Execution> connectExecutionRealtimely() {
        return connectExecutionRealtimely.get();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected Signal<Order> connectOrdersRealtimely() {
        return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected Signal<OrderBookPageChanges> connectOrderBookRealtimely() {
        return null;
    }

    public MarketServiceMock enableDebug() {
        this.debug = true;
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void dispose() {
        service.dispose();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isDisposed() {
        return service.isDisposed();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isNotDisposed() {
        return service.isNotDisposed();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Disposable add(Disposable next) {
        return service.add(next);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Disposable add(Future next) {
        return service.add(next);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode() {
        return service.hashCode();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Disposable sub() {
        return service.sub();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Signal<Integer> delay() {
        return service.delay();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Signal<String> request(Order order) {
        return service.request(order);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Signal<Order> cancel(Order order) {
        return service.cancel(order);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Signal<Execution> executions(long start, long end) {
        return service.executions(start, end);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean equals(Object obj) {
        return service.equals(obj);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Signal<Execution> executionLatest() {
        return service.executionLatest();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean checkEquality(Execution one, Execution other) {
        return service.checkEquality(one, other);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public long estimateInitialExecutionId() {
        return service.estimateInitialExecutionId();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Signal<Order> orders() {
        return service.orders();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Signal<Order> orders(OrderState state) {
        return service.orders(state);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Signal<OrderBookPageChanges> orderBook() {
        return service.orderBook();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String calculateReadablePrice(double price) {
        return service.calculateReadablePrice(price);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String calculateReadableTime(double seconds) {
        return service.calculateReadableTime(seconds);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Signal<Num> baseCurrency() {
        return service.baseCurrency();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Signal<Num> targetCurrency() {
        return service.targetCurrency();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ZonedDateTime now() {
        return service.now();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public long nano() {
        return service.nano();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ScheduledExecutorService scheduler() {
        return service.scheduler();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public RetryPolicy retryPolicy(int max, String name) {
        return service.retryPolicy(max, name);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return service.toString();
    }
}
