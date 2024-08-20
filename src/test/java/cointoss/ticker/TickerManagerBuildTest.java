/*
 * Copyright (C) 2024 Nameless Production Committee
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          http://opensource.org/licenses/mit-license.php
 */
package cointoss.ticker;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import cointoss.market.TestableMarketService;
import cointoss.util.Chrono;

public class TickerManagerBuildTest {

    @RegisterExtension
    TestableMarketService service = new TestableMarketService();

    @Test
    void build() {
        TickerManager manager = new TickerManager(service);
        manager.build(Chrono.utc(2020, 1, 1), Chrono.utc(2020, 1, 2), false);
    }
}
