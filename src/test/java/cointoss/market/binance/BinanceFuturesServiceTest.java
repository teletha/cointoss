/*
 * Copyright (C) 2018 Nameless Production Committee
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          http://opensource.org/licenses/mit-license.php
 */
package cointoss.market.binance;

import org.junit.jupiter.api.Test;

import cointoss.market.MarketServiceTestTemplate;

public class BinanceFuturesServiceTest extends MarketServiceTestTemplate<BinanceService> {

    /**
     * {@inheritDoc}
     */
    @Override
    protected BinanceService constructMarketService() {
        return construct(BinanceService::new, Binance.FUTURE_BTC_USDT.marketName, true, Binance.FUTURE_BTC_USDT.setting);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Test
    protected void orderActive() {
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Test
    protected void orderActiveEmpty() {
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Test
    protected void orderCanceled() {
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Test
    protected void orderCanceledEmpty() {
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Test
    protected void orderCompleted() {
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Test
    protected void orderCompletedEmpty() {
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Test
    protected void orders() {
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Test
    protected void ordersEmpty() {
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Test
    protected void executions() {
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Test
    protected void executionLatest() {
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Test
    protected void executionRealtimely() {
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Test
    protected void executionRealtimelyConsecutiveBuy() {
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Test
    protected void executionRealtimelyConsecutiveSell() {
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Test
    protected void executionRealtimelyWithMultipleChannels() {
    }
}
