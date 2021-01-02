/*
 * Copyright (C) 2021 cointoss Development Team
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          https://opensource.org/licenses/MIT
 */
package cointoss.order;

import java.util.List;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import cointoss.execution.Execution;
import cointoss.util.Chrono;
import cointoss.verify.VerifiableMarket;

class OrderManagerTest {

    private VerifiableMarket market = new VerifiableMarket();

    private OrderManager orders = market.orders;

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
    void added() {
        List<Order> added = orders.added.toList();
        assert added.size() == 0;

        orders.requestNow(Order.with.buy(1).price(10));
        assert added.size() == 1;
        orders.requestNow(Order.with.buy(1).price(10));
        assert added.size() == 2;
    }

    @Test
    void removedByCancel() {
        Order order1 = orders.requestNow(Order.with.buy(1).price(10));
        Order order2 = orders.requestNow(Order.with.buy(1).price(10));

        List<Order> removed = orders.removed.toList();
        assert removed.size() == 0;
        orders.cancelNow(order1);
        assert removed.size() == 1;
        orders.cancelNow(order2);
        assert removed.size() == 2;
    }

    @Test
    void removedByExecute() {
        orders.requestNow(Order.with.buy(1).price(10));

        List<Order> removed = orders.removed.toList();
        assert removed.size() == 0;
        market.perform(Execution.with.sell(1).price(9));
        assert removed.size() == 1;
    }

    @Test
    void removedByExecuteDividedly() {
        orders.requestNow(Order.with.buy(2).price(10));

        List<Order> removed = orders.removed.toList();
        assert removed.size() == 0;
        market.perform(Execution.with.sell(1).price(9));
        assert removed.size() == 0;
        market.perform(Execution.with.sell(1).price(9));
        assert removed.size() == 1;
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
    void manages() {
        List<Order> list = orders.manages().toList();
        assert list.size() == 0;

        orders.requestNow(Order.with.buy(1).price(1));
        assert list.size() == 1;

        List<Order> other = orders.manages().toList();
        assert other.size() == 1;

        orders.requestNow(Order.with.sell(1).price(1));
        assert other.size() == 2;
        assert list.size() == 2;
    }

    @Disabled
    @Test
    void recieveExecutionsBeforeOrderResponse() {
        market.service.emulateExecutionBeforeOrderResponse(Execution.with.buy(0.2).price(9));
        market.service.emulateExecutionBeforeOrderResponse(Execution.with.buy(0.3).price(9));

        Order o = Order.with.buy(1).price(10);
        orders.requestNow(o);
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
    void updateNew() {
        orders.update(Order.with.buy(1).price(10).id("A"));

        assert orders.items.size() == 1;
        assert orders.items.get(0).id.equals("A");
    }

    @Test
    void updateChange() {
        orders.update(Order.with.buy(1).price(10).id("A"));
        orders.update(Order.with.buy(1).price(10).id("A").executedSize(1).state(OrderState.ACTIVE));

        assert orders.items.size() == 1;
        assert orders.items.get(0).id.equals("A");
        assert orders.items.get(0).executedSize.is(1);
    }
}