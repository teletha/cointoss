/*
 * Copyright (C) 2025 The COINTOSS Development Team
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

public abstract class PrivateServiceTestTemplate<S extends MarketService> extends MarketServiceTester<S> {

    @Test
    public void orders() {
        notImplemented();
    }

    @Test
    public void ordersEmpty() {
        notImplemented();
    }

    @Test
    public void orderActive() {
        notImplemented();
    }

    @Test
    public void orderActiveEmpty() {
        notImplemented();
    }

    @Test
    public void orderCanceled() {
        notImplemented();
    }

    @Test
    public void orderCanceledEmpty() {
        notImplemented();
    }

    @Test
    public void orderCompleted() {
        notImplemented();
    }

    @Test
    public void orderCompletedEmpty() {
        notImplemented();
    }
}