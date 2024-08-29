/*
 * Copyright (C) 2024 The COINTOSS Development Team
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          https://opensource.org/licenses/MIT
 */
package cointoss.ticker;

import org.junit.jupiter.api.BeforeEach;

import cointoss.TestableMarketService;
import cointoss.util.TimebaseSupport;

public class TickerTestSupport implements TimebaseSupport {

    protected TestableMarketService service;

    protected TestableTickerManager manager;

    @BeforeEach
    void setup() {
        service = new TestableMarketService();
        manager = new TestableTickerManager(service);
    }
}