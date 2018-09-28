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

import static org.junit.jupiter.api.Assertions.*;

import java.util.List;

import org.junit.jupiter.api.Test;

import cointoss.Side;

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

    @Test
    void attributeNull() {
        Order order = Order.limitLong(1, 10);

        assertThrows(NullPointerException.class, () -> order.attribute(null));
    }

    private static class Attribute {
        String id;
    }

    @Test
    void limit() {
        Order order = Order.limit(Side.BUY, 1, 20);
        assert order.side == Side.BUY;
        assert order.size.is(1);
        assert order.price.v.is(20);
    }

    @Test
    void market() {
        Order order = Order.market(Side.BUY, 1);
        assert order.side == Side.BUY;
        assert order.size.is(1);
        assert order.price.v.is(0);
    }

    @Test
    void observeTerminatingByCompleted() {
        Order order = Order.market(Side.BUY, 1);
        List<Order> result = order.observeTerminating().toList();
        assert result.isEmpty();
        order.state.set(OrderState.ACTIVE);
        assert result.isEmpty();
        order.state.set(OrderState.EXPIRED);
        assert result.isEmpty();
        order.state.set(OrderState.INIT);
        assert result.isEmpty();
        order.state.set(OrderState.REJECTED);
        assert result.isEmpty();
        order.state.set(OrderState.REQUESTING);
        assert result.isEmpty();

        order.state.set(OrderState.COMPLETED);
        assert result.size() == 1;
        order.state.set(OrderState.CANCELED);
        assert result.size() == 1;
        order.state.set(OrderState.COMPLETED);
        assert result.size() == 1;
    }

    @Test
    void observeTerminatingByCanceld() {
        Order order = Order.market(Side.BUY, 1);
        List<Order> result = order.observeTerminating().toList();
        assert result.isEmpty();
        order.state.set(OrderState.ACTIVE);
        assert result.isEmpty();
        order.state.set(OrderState.EXPIRED);
        assert result.isEmpty();
        order.state.set(OrderState.INIT);
        assert result.isEmpty();
        order.state.set(OrderState.REJECTED);
        assert result.isEmpty();
        order.state.set(OrderState.REQUESTING);
        assert result.isEmpty();

        order.state.set(OrderState.CANCELED);
        assert result.size() == 1;
        order.state.set(OrderState.CANCELED);
        assert result.size() == 1;
        order.state.set(OrderState.COMPLETED);
        assert result.size() == 1;
    }
}
