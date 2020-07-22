/*
 * Copyright (C) 2018 Nameless Production Committee
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          http://opensource.org/licenses/mit-license.php
 */
package cointoss.market.ftx;

import org.junit.jupiter.api.Test;

import cointoss.market.MarketServiceTestTemplate;
import cointoss.market.PublicServiceTestTemplate;

class FTXPublicServiceTest extends MarketServiceTestTemplate<FTXService> implements PublicServiceTestTemplate {

    /**
     * {@inheritDoc}
     */
    @Override
    protected FTXService constructMarketService() {
        return construct(FTXService::new, FTX.BTC_USD.marketName, FTX.BTC_USD.setting);
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
