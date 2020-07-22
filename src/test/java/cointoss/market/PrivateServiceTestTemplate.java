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

public interface PrivateServiceTestTemplate {

    @Test
    default void orders() {
        notImplemented();
    }

    @Test
    default void ordersEmpty() {
        notImplemented();
    }

    @Test
    default void orderActive() {
        notImplemented();
    }

    @Test
    default void orderActiveEmpty() {
        notImplemented();
    }

    @Test
    default void orderCanceled() {
        notImplemented();
    }

    @Test
    default void orderCanceledEmpty() {
        notImplemented();
    }

    @Test
    default void orderCompleted() {
        notImplemented();
    }

    @Test
    default void orderCompletedEmpty() {
        notImplemented();
    }
}
