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

import antibug.ExpectThrow;

/**
 * @version 2018/07/08 11:44:58
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

    @Test
    void attribute() {
        Order order = Order.limitLong(1, 10);
        Attribute attribute = order.attribute(Attribute.class);
        assert attribute != null;
        assert attribute.id == null;
    }

    @ExpectThrow(NullPointerException.class)
    void attributeNull() {
        Order order = Order.limitLong(1, 10);
        order.attribute(null);
    }

    @Test
    void copyAttributeFrom() {
        Order order = Order.limitLong(1, 10);
        order.attribute(Attribute.class).id = "base";

        Order notExist = Order.limitLong(1, 10);
        notExist.copyAttributeFrom(order);
        assert notExist.attribute(Attribute.class).id.equals("base");

        Order exist = Order.limitLong(1, 10);
        Attribute attribute = exist.attribute(Attribute.class);
        assert attribute.id == null;
        exist.copyAttributeFrom(order);
        assert exist.attribute(Attribute.class).id.equals("base");
    }

    private static class Attribute {
        String id;
    }
}
