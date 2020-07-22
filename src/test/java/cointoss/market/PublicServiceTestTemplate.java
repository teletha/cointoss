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

import static cointoss.market.MarketServiceTestTemplate.notImplemented;

import org.junit.jupiter.api.Test;

public interface PublicServiceTestTemplate {

    @Test
    default void executions() {
        notImplemented();
    }

    @Test
    default void executionLatest() {
        notImplemented();
    }

    @Test
    default void executionRealtimely() {
        notImplemented();
    }

    @Test
    default void executionRealtimelyConsecutiveBuy() {
        notImplemented();
    }

    @Test
    default void executionRealtimelyConsecutiveSell() {
        notImplemented();
    }

    @Test
    default void executionRealtimelyWithMultipleChannels() {
        notImplemented();
    }
}
