/*
 * Copyright (C) 2019 CoinToss Development Team
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          https://opensource.org/licenses/MIT
 */
package cointoss.order;

import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.List;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import cointoss.Direction;
import cointoss.execution.Execution;
import cointoss.util.Num;

/**
 * @version 2018/07/08 11:44:58
 */
public class OrderTest {

    @Test
    void isCanceled() {
        Order order = Order.with.buy(1).price(10);
        assert order.isNotCanceled();

        order.assignState(OrderState.CANCELED);
        assert order.isCanceled();
    }

    @Test
    void isCompleted() {
        Order order = Order.with.buy(1).price(10);
        assert order.isNotCompleted();

        order.assignState(OrderState.COMPLETED);
        assert order.isCompleted();
    }

    @Test
    void isExpired() {
        Order order = Order.with.buy(1).price(10);
        assert order.isNotExpired();

        order.assignState(OrderState.EXPIRED);
        assert order.isExpired();
    }

    @Test
    void attribute() {
        Order order = Order.with.buy(1).price(10);
        Attribute attribute = order.relation(Attribute.class);
        assert attribute != null;
        assert attribute.id == null;
    }

    @Test
    void attributeNull() {
        Order order = Order.with.buy(1).price(10);

        assertThrows(NullPointerException.class, () -> order.relation(null));
    }

    private static class Attribute {
        String id;
    }

    @Test
    void limit() {
        Order order = Order.with.direction(Direction.BUY, 1).price(20);
        assert order.direction == Direction.BUY;
        assert order.size.is(1);
        assert order.price.is(20);
    }

    @Test
    void market() {
        Order order = Order.with.direction(Direction.BUY, 1);
        assert order.direction == Direction.BUY;
        assert order.size.is(1);
        assert order.price.is(0);
    }

    @Test
    void observeTerminatingByCompleted() {
        Order order = Order.with.direction(Direction.BUY, 1);
        List<Order> result = order.observeTerminating().toList();
        assert result.isEmpty();
        order.assignState(OrderState.ACTIVE);
        assert result.isEmpty();
        order.assignState(OrderState.EXPIRED);
        assert result.isEmpty();
        order.assignState(OrderState.INIT);
        assert result.isEmpty();
        order.assignState(OrderState.REJECTED);
        assert result.isEmpty();
        order.assignState(OrderState.REQUESTING);
        assert result.isEmpty();

        order.assignState(OrderState.COMPLETED);
        assert result.size() == 1;
        order.assignState(OrderState.CANCELED);
        assert result.size() == 1;
        order.assignState(OrderState.COMPLETED);
        assert result.size() == 1;
    }

    @Test
    void observeTerminatingByCanceld() {
        Order order = Order.with.direction(Direction.BUY, 1);
        List<Order> result = order.observeTerminating().toList();
        assert result.isEmpty();
        order.assignState(OrderState.ACTIVE);
        assert result.isEmpty();
        order.assignState(OrderState.EXPIRED);
        assert result.isEmpty();
        order.assignState(OrderState.INIT);
        assert result.isEmpty();
        order.assignState(OrderState.REJECTED);
        assert result.isEmpty();
        order.assignState(OrderState.REQUESTING);
        assert result.isEmpty();

        order.assignState(OrderState.CANCELED);
        assert result.size() == 1;
        order.assignState(OrderState.CANCELED);
        assert result.size() == 1;
        order.assignState(OrderState.COMPLETED);
        assert result.size() == 1;
    }

    @Test
    void direction() {
        assert Order.with.buy(1).direction.isBuy();
        assert Order.with.sell(1).direction.isSell();
        assert Order.with.direction(Direction.BUY, 1).direction.isBuy();
        assert Order.with.direction(Direction.SELL, 1).direction.isSell();

        assertThrows(IllegalArgumentException.class, () -> Order.with.direction(null, 1));
    }

    @Test
    void size() {
        assert Order.with.buy(1).size.is(1);
        assert Order.with.buy(2.3).size.is(2.3);

        assertThrows(IllegalArgumentException.class, () -> Order.with.buy(0));
        assertThrows(IllegalArgumentException.class, () -> Order.with.buy(-1));
        assertThrows(IllegalArgumentException.class, () -> Order.with.buy(null));
    }

    @Test
    void price() {
        assert Order.with.buy(1).price(1L).price.is(1);
        assert Order.with.buy(1).price(2.3).price.is(2.3);
        assert Order.with.buy(1).price(Num.ONE).price.is(1);

        assert Order.with.buy(1).price(0).price.is(0);
        assert Order.with.buy(1).price(-1).price.is(0);
        assert Order.with.buy(1).price((Num) null).price.is(0);
    }

    @Test
    void condition() {
        assert Order.with.buy(1).quantityCondition == QuantityCondition.GoodTillCanceled;
        assert Order.with.buy(1).quantityCondition(QuantityCondition.FillOrKill).quantityCondition == QuantityCondition.FillOrKill;
        assert Order.with.buy(1).quantityCondition(null).quantityCondition == QuantityCondition.GoodTillCanceled;
    }

    @Test
    void updateSizeAtomically() {
        Order o = Order.with.buy(3);
        assert o.remainingSize.is(3);
        assert o.executedSize.is(0);

        o.observeRemainingSize().to(size -> {

        });
        List<Num> executedInUpdatingRemaining = o.observeRemainingSize().map(size -> o.executedSize).toList();
        List<Num> remainingInUpdatingExecuted = o.observeExecutedSize().map(size -> o.remainingSize).toList();

        o.executed(Execution.with.buy(1));
        o.executed(Execution.with.buy(1));
        o.executed(Execution.with.buy(1));

        Assertions.assertIterableEquals(executedInUpdatingRemaining, List.of(Num.of(1), Num.of(2), Num.of(3)));
        Assertions.assertIterableEquals(remainingInUpdatingExecuted, List.of(Num.of(2), Num.of(1), Num.of(0)));
    }
}
