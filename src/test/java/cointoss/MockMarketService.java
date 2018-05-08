/*
 * Copyright (C) 2018 Nameless Production Committee
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          http://opensource.org/licenses/mit-license.php
 */
package cointoss;

import cointoss.order.Order;
import cointoss.order.OrderBookListChange;
import cointoss.util.Num;
import kiss.Signal;

/**
 * @version 2018/05/08 18:04:18
 */
public class MockMarketService extends MarketService {

    /**
     * 
     */
    public MockMarketService() {
        super("MockMarket", "MockCoin");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void vandalize() {
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Signal<Integer> delay() {
        return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Signal<String> request(Order order) {
        return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Signal<Order> cancel(Order order) {
        return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Signal<Execution> executions() {
        return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Signal<Execution> executions(long key) {
        return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Signal<Order> orders() {
        return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Signal<Execution> positions() {
        return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Signal<Num> baseCurrency() {
        return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Signal<Num> targetCurrency() {
        return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Signal<OrderBookListChange> orderBook() {
        return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected Execution decode(String[] values, Execution previous) {
        return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected String[] encode(Execution execution, Execution previous) {
        return null;
    }

}
