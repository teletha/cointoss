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
import cointoss.market.PublicServiceTestTemplate;

class BitfinexPublicServiceTest extends MarketServiceTestTemplate<BitfinexService> implements PublicServiceTestTemplate {

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
    public void executions() {
        PublicServiceTestTemplate.super.executions();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Test
    public void executionLatest() {
        PublicServiceTestTemplate.super.executionLatest();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Test
    public void executionRealtimely() {
        PublicServiceTestTemplate.super.executionRealtimely();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Test
    public void executionRealtimelyConsecutiveBuy() {
        PublicServiceTestTemplate.super.executionRealtimelyConsecutiveBuy();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Test
    public void executionRealtimelyConsecutiveSell() {
        PublicServiceTestTemplate.super.executionRealtimelyConsecutiveSell();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Test
    public void executionRealtimelyWithMultipleChannels() {
        PublicServiceTestTemplate.super.executionRealtimelyWithMultipleChannels();
    }
}
