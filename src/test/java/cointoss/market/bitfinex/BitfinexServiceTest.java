/*
 * Copyright (C) 2019 Nameless Production Committee
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          http://opensource.org/licenses/mit-license.php
 */
package cointoss.market.bitfinex;

import org.junit.jupiter.api.Test;

import cointoss.market.MarketServiceTestTemplate;

class BitfinexServiceTest extends MarketServiceTestTemplate<BitfinexService> {

    /**
     * {@inheritDoc}
     */
    @Override
    protected BitfinexService constructMarketService() {
        return construct(BitfinexService::new, Bitfinex.BTC_USDT.marketName, Bitfinex.BTC_USDT.setting);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Test
    protected void orderActive() {
        super.orderActive();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Test
    protected void orderActiveEmpty() {
        super.orderActiveEmpty();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Test
    protected void orderCanceled() {
        super.orderCanceled();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Test
    protected void orderCanceledEmpty() {
        super.orderCanceledEmpty();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Test
    protected void orderCompleted() {
        super.orderCompleted();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Test
    protected void orderCompletedEmpty() {
        super.orderCompletedEmpty();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Test
    protected void orders() {
        super.orders();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Test
    protected void ordersEmpty() {
        super.ordersEmpty();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Test
    protected void executions() {
        super.executions();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Test
    protected void executionLatest() {
        super.executionLatest();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Test
    protected void executionRealtimely() {
        super.executionRealtimely();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Test
    protected void executionRealtimelyConsecutiveBuy() {
        super.executionRealtimelyConsecutiveBuy();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Test
    protected void executionRealtimelyConsecutiveSell() {
        super.executionRealtimelyConsecutiveSell();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Test
    protected void executionRealtimelyWithMultipleChannels() {
        super.executionRealtimelyWithMultipleChannels();
    }
}
