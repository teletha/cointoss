/*
 * Copyright (C) 2024 The COINTOSS Development Team
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          https://opensource.org/licenses/MIT
 */
package cointoss.order;

import static cointoss.Direction.*;
import static hypatia.Num.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import cointoss.TestableMarket;
import cointoss.execution.Execution;
import cointoss.util.Chrono;
import kiss.I;

class OrderManagerTest {

    private TestableMarket market;

    private OrderManager orders;

    @BeforeEach
    void setup() {
        market = new TestableMarket();
        orders = market.orders;
    }

    @Test
    void request() {
        assert orders.items.size() == 0;
        orders.requestNow(Order.with.buy(1).price(10));
        assert orders.items.size() == 1;
    }

    @Test
    void cancel() {
        Order order = orders.requestNow(Order.with.buy(1).price(10));
        assert orders.items.size() == 1;
        assert orders.cancelNow(order) == order;
        assert orders.items.size() == 0;
        assert orders.cancelNow(order) == order;
        assert orders.items.size() == 0;
    }

    @Test
    void updateRemovedByCancel() {
        Order order1 = orders.requestNow(Order.with.buy(1).price(10));
        Order order2 = orders.requestNow(Order.with.buy(1).price(10));
        assert orders.items.size() == 2;

        orders.update(OrderManager.Update.cancel(order1.id));
        assert orders.items.size() == 1;
        orders.update(OrderManager.Update.cancel(order2.id));
        assert orders.items.size() == 0;
    }

    @Test
    void requestedOrderHaveCreationTime() {
        Order order = Order.with.buy(1).price(10);
        assert order.creationTime == Chrono.MIN;

        orders.request(order).to(o -> {
            assert o.creationTime.isEqual(market.service.now());
        });
    }

    @Test
    void recieveExecutionsBeforeOrderResponse() {
        market.service.emulateExecutionBeforeOrderResponse(Execution.with.buy(0.2).price(9));
        market.service.emulateExecutionBeforeOrderResponse(Execution.with.buy(0.3).price(9));

        Order o = orders.requestNow(Order.with.buy(1).price(10));
        assert o.remainingSize().is(0.5);
        assert o.executedSize.is(0.5);

        market.perform(Execution.with.buy(0.5).price(9));
        assert o.remainingSize().is(0);
        assert o.executedSize.is(1);
    }

    @Test
    void recieveExecutionsAfterCancelResponse() {
        Order o = Order.with.buy(1).price(10);
        orders.requestNow(o);
        assert o.remainingSize().is(1);
        assert o.executedSize.is(0);

        market.service.emulateExecutionAfterOrderCancelResponse(Execution.with.buy(0.2).price(9));
        market.service.emulateExecutionAfterOrderCancelResponse(Execution.with.buy(0.3).price(9));
        orders.cancelNow(o);
        assert o.remainingSize().is(0.5);
        assert o.executedSize.is(0.5);
    }

    @Test
    void updateAddDiffOrder() {
        orders.update(Order.with.buy(1).price(10).id("one"));
        assert orders.items.size() == 1;
        orders.update(Order.with.buy(1).price(10).id("other"));
        assert orders.items.size() == 2;
    }

    @Test
    void updateAddSameOrder() {
        orders.update(Order.with.buy(1).price(10).id("same"));
        assert orders.items.size() == 1;
        orders.update(Order.with.buy(1).price(10).id("same"));
        assert orders.items.size() == 1;
    }

    @Test
    void updateExecutedOrder() {
        orders.update(Order.with.buy(1).price(10).id("A"));
        assert orders.items.size() == 1;
        Order order = orders.items.get("A");
        assert order.size.is(1);
        assert order.executedSize.is(0);

        orders.update(Order.with.buy(1).price(10).id("A").executedSize(1).state(OrderState.ACTIVE));
        assert orders.items.size() == 0;
        assert order.size.is(1);
        assert order.executedSize.is(1);
    }

    @Test
    void updatePartialExecutedOrder() {
        orders.update(Order.with.buy(1).price(10).id("A"));
        Order order = orders.items.get("A");
        assert orders.items.size() == 1;
        assert order.size.is(1);
        assert order.executedSize.is(0);

        orders.update(Order.with.buy(0.5).price(10).id("A").state(OrderState.ACTIVE_PARTIAL));
        assert orders.items.size() == 1;
        assert order.size.is(1);
        assert order.executedSize.is(0.5);

        orders.update(Order.with.buy(0.5).price(10).id("A").state(OrderState.ACTIVE_PARTIAL));
        assert orders.items.size() == 0;
        assert order.size.is(1);
        assert order.executedSize.is(1);
    }

    @Test
    void acceptOrderOnSever() {
        String id = market.service.request(OrderManager.Update.create("A", BUY, ONE, ONE)).to().exact();
        assert orders.items.size() == 1;

        Order order = orders.items.get(id);
        assert order.state == OrderState.ACTIVE;
    }

    @Test
    void acceptCancelOnSever() {
        String id = market.service.request(OrderManager.Update.create("A", BUY, ONE, ONE)).to().exact();
        assert orders.items.size() == 1;

        Order order = orders.items.get(id);
        assert order.state == OrderState.ACTIVE;

        market.service.cancel(OrderManager.Update.create(id, BUY, ONE, ONE)).to(I.NoOP);
        assert orders.items.size() == 0;
        assert order.state == OrderState.CANCELED;
    }
}