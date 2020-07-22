/*
 * Copyright (C) 2019 Nameless Production Committee
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          http://opensource.org/licenses/mit-license.php
 */
package cointoss.market;

import org.junit.jupiter.api.Test;

import cointoss.MarketService;

public abstract class PublicServiceTestTemplate<S extends MarketService> extends MarketServiceTestTemplate<S> {

    @Test
    protected void executions() {
        notImplemented();
    }

    @Test
    protected void executionLatest() {
        notImplemented();
    }

    @Test
    protected void executionRealtimely() {
        notImplemented();
    }

    @Test
    protected void executionRealtimelyConsecutiveBuy() {
        notImplemented();
    }

    @Test
    protected void executionRealtimelyConsecutiveSell() {
        notImplemented();
    }

    @Test
    protected void executionRealtimelyWithMultipleChannels() {
        notImplemented();
    }
}
