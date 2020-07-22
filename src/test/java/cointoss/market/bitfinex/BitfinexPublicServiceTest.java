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

import cointoss.market.MarketServiceTestBase;
import cointoss.market.PublicServiceTemplate;

class BitfinexPublicServiceTest extends MarketServiceTestBase<BitfinexService> implements PublicServiceTemplate {

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
        PublicServiceTemplate.super.executions();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Test
    public void executionLatest() {
        PublicServiceTemplate.super.executionLatest();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Test
    public void executionRealtimely() {
        PublicServiceTemplate.super.executionRealtimely();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Test
    public void executionRealtimelyConsecutiveBuy() {
        PublicServiceTemplate.super.executionRealtimelyConsecutiveBuy();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Test
    public void executionRealtimelyConsecutiveSell() {
        PublicServiceTemplate.super.executionRealtimelyConsecutiveSell();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Test
    public void executionRealtimelyWithMultipleChannels() {
        PublicServiceTemplate.super.executionRealtimelyWithMultipleChannels();
    }
}
