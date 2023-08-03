/*
 * Copyright (C) 2023 The COINTOSS Development Team
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          https://opensource.org/licenses/MIT
 */
package cointoss.market;

import org.junit.jupiter.api.Test;

import cointoss.MarketService;

public abstract class PublicServiceTestTemplate<S extends MarketService> extends MarketServiceTester<S> {

    @Test
    public void executions() {
        notImplemented();
    }

    @Test
    public void executionLatest() {
        notImplemented();
    }

    @Test
    public void executionRealtimely() {
        notImplemented();
    }

    @Test
    public void executionRealtimelyConsecutiveBuy() {
        notImplemented();
    }

    @Test
    public void executionRealtimelyConsecutiveSell() {
        notImplemented();
    }

    @Test
    public void executionRealtimelyWithMultipleChannels() {
        notImplemented();
    }
}