/*
 * Copyright (C) 2018 Nameless Production Committee
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          http://opensource.org/licenses/mit-license.php
 */
package cointoss.order;

import org.junit.jupiter.api.Test;

/**
 * @version 2018/07/07 10:55:58
 */
public class OrderTest {

    @Test
    void isCanceled() {
        Order order = Order.limitLong(1, 10);
        assert order.isNotCanceled();

        order.state.set(OrderState.CANCELED);
        assert order.isCanceled();
    }

    @Test
    void isCompleted() {
        Order order = Order.limitLong(1, 10);
        assert order.isNotCompleted();

        order.state.set(OrderState.COMPLETED);
        assert order.isCompleted();
    }

    @Test
    void isExpired() {
        Order order = Order.limitLong(1, 10);
        assert order.isNotExpired();

        order.state.set(OrderState.EXPIRED);
        assert order.isExpired();
    }
}
