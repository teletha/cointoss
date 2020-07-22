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

public abstract class PrivateServiceTestTemplate<S extends MarketService> extends MarketServiceTestTemplate<S> {

    @Test
    protected void orders() {
        notImplemented();
    }

    @Test
    protected void ordersEmpty() {
        notImplemented();
    }

    @Test
    protected void orderActive() {
        notImplemented();
    }

    @Test
    protected void orderActiveEmpty() {
        notImplemented();
    }

    @Test
    protected void orderCanceled() {
        notImplemented();
    }

    @Test
    protected void orderCanceledEmpty() {
        notImplemented();
    }

    @Test
    protected void orderCompleted() {
        notImplemented();
    }

    @Test
    protected void orderCompletedEmpty() {
        notImplemented();
    }
}
