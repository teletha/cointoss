/*
 * Copyright (C) 2019 CoinToss Development Team
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          https://opensource.org/licenses/MIT
 */
package cointoss.verify;

import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import org.junit.jupiter.api.Test;

import cointoss.Direction;
import cointoss.execution.Executed;
import cointoss.order.Order;
import cointoss.order.OrderState;
import cointoss.order.QuantityCondition;
import cointoss.util.Num;

class VerifiableMarketTest {

    VerifiableMarket market = new VerifiableMarket();

    @Test
    void requestOrder() {
        Order order = market.request(Order.buy(1).price(10)).to().v;
        assert order.isBuy();
        assert order.executedSize.is(0);
        assert market.orders().size() == 1;
    }

    @Test
    void execute() {
        Order order = market.request(Order.buy(1).price(10)).to().v;
        assert order.remainingSize.is(1);
        assert order.executedSize.is(0);

        market.execute(Executed.buy(1).price(10));
        assert order.remainingSize.is(0);
        assert order.executedSize.is(1);
    }

    @Test
    void executeDivided() {
        Order order = market.request(Order.buy(10).price(10)).to().v;
        assert order.remainingSize.is(10);
        assert order.executedSize.is(0);

        market.execute(Direction.SELL, 5, 10);
        assert order.remainingSize.is(5);
        assert order.executedSize.is(5);

        market.execute(Direction.SELL, 5, 10);
        assert order.remainingSize.is(0);
        assert order.executedSize.is(10);
        assert market.validateExecutionState(2);
    }

    @Test
    void executeOverflow() {
        market.request(Order.buy(10).price(10)).to(order -> {
            assert order.remainingSize.is(10);
            assert order.executedSize.is(0);

            market.execute(Direction.BUY, 7, 10);
            assert order.remainingSize.is(3);
            assert order.executedSize.is(7);

            market.execute(Direction.SELL, 7, 10);
            assert order.remainingSize.is(0);
            assert order.executedSize.is(10);
        });

        List<Order> orders = market.orders();
        assert orders.size() == 1;
        assert orders.get(0).state.is(OrderState.COMPLETED);

        // List<Execution> executions = market.backend.executions().toList();
        // assert executions.size() == 2;
        // assert executions.get(0).size.is(7);
        // assert executions.get(1).size.is(3);
    }

    @Test
    void executeExtra() {
        market.request(Order.buy(10).price(10)).to(order -> {
            assert order.remainingSize.is(10);
            assert order.executedSize.is(0);

            market.execute(Direction.BUY, 10, 10);
            assert order.remainingSize.is(0);
            assert order.executedSize.is(10);

            market.execute(Direction.SELL, 1, 10);
            assert order.remainingSize.is(0);
            assert order.executedSize.is(10);
        });

        assert market.validateOrderState(0, 1, 0, 0, 0);
        assert market.validateExecutionState(1);
    }

    @Test
    void executeLongWithUpperPrice() {
        market.request(Order.buy(10).price(10)).to(order -> {
            market.execute(Direction.BUY, 5, 12);
            market.execute(Direction.SELL, 5, 13);
        });

        assert market.validateOrderState(1, 0, 0, 0, 0);
        assert market.validateExecutionState(0);
    }

    @Test
    void executeLongWithLowerPrice() {
        market.request(Order.buy(10).price(10)).to(order -> {
            market.execute(Direction.BUY, 5, 8);
            market.execute(Direction.SELL, 5, 7);
        });

        assert market.validateOrderState(0, 1, 0, 0, 0);
        assert market.validateExecutionState(2);
    }

    @Test
    void executeShortWithUpperPrice() {
        market.request(Order.sell(10).price(10)).to(order -> {
            market.execute(Direction.BUY, 5, 12);
            market.execute(Direction.SELL, 5, 13);
        });

        assert market.validateOrderState(0, 1, 0, 0, 0);
        assert market.validateExecutionState(2);
    }

    @Test
    void executeShortWithLowerPrice() {
        market.request(Order.sell(10).price(10)).to(order -> {
            market.execute(Direction.BUY, 5, 8);
            market.execute(Direction.SELL, 5, 7);
        });

        assert market.validateOrderState(1, 0, 0, 0, 0);
        assert market.validateExecutionState(0);
    }

    @Test
    void lag() {
        VerifiableMarket market = new VerifiableMarket();
        market.service.lag(5);

        market.request(Order.buy(10).price(10)).to();
        market.execute(Direction.BUY, 5, 10, new TimeLag(3));
        market.execute(Direction.BUY, 4, 10, new TimeLag(4));
        market.execute(Direction.BUY, 3, 10, new TimeLag(5));
        market.execute(Direction.BUY, 2, 10, new TimeLag(6));
        market.execute(Direction.BUY, 1, 10, new TimeLag(7));

        assert market.validateExecutionState(3);
        assert market.validateOrderState(1, 0, 0, 0, 0);
    }

    @Test
    void shortWithTrigger() {
        market.request(Order.sell(1).price(7).stopAt(8)).to();
        market.execute(Direction.BUY, 1, 9);
        assert market.validateOrderState(1, 0, 0, 0, 0);
        market.execute(Direction.BUY, 1, 8);
        assert market.validateOrderState(1, 0, 0, 0, 0);
        market.execute(Direction.BUY, 1, 7);
        assert market.validateOrderState(0, 1, 0, 0, 0);
    }

    @Test
    void shortWithTriggerSamePrice() {
        market.request(Order.sell(1).price(8).stopAt(8)).to();
        market.execute(Direction.BUY, 1, 9);
        assert market.validateOrderState(1, 0, 0, 0, 0);
        market.execute(Direction.BUY, 1, 8);
        assert market.validateOrderState(1, 0, 0, 0, 0);
        market.execute(Direction.BUY, 1, 7);
        assert market.validateOrderState(1, 0, 0, 0, 0);
        market.execute(Direction.BUY, 1, 8);
        assert market.validateOrderState(0, 1, 0, 0, 0);
    }

    @Test
    void shortMarketWithTrigger() {
        Order order = Order.sell(1).stopAt(8);
        market.request(order).to();
        market.execute(Direction.BUY, 1, 9);
        assert market.validateOrderState(1, 0, 0, 0, 0);
        market.execute(Direction.BUY, 1, 8);
        assert market.validateOrderState(1, 0, 0, 0, 0);
        market.execute(Direction.BUY, 1, 7);
        assert market.validateOrderState(0, 1, 0, 0, 0);
        assert order.price.v.is(7);

        order = Order.sell(1).stopAt(8);
        market.request(order).to();
        market.execute(Direction.BUY, 1, 9);
        assert market.validateOrderState(1, 1, 0, 0, 0);
        market.execute(Direction.BUY, 1, 8);
        assert market.validateOrderState(1, 1, 0, 0, 0);
        market.execute(Direction.BUY, 1, 9);
        assert market.validateOrderState(0, 2, 0, 0, 0);
        assert order.price.v.is(9);
    }

    @Test
    void longWithTrigger() {
        market.request(Order.buy(1).price(13).stopAt(12)).to();
        market.execute(Direction.BUY, 1, 11);
        assert market.validateOrderState(1, 0, 0, 0, 0);
        market.execute(Direction.BUY, 1, 12);
        assert market.validateOrderState(1, 0, 0, 0, 0);
        market.execute(Direction.BUY, 1, 13);
        assert market.validateOrderState(0, 1, 0, 0, 0);
    }

    @Test
    void longWithTriggerSamePrice() {
        market.request(Order.buy(1).price(12).stopAt(12)).to();
        market.execute(Direction.BUY, 1, 11);
        assert market.validateOrderState(1, 0, 0, 0, 0);
        market.execute(Direction.BUY, 1, 12);
        assert market.validateOrderState(1, 0, 0, 0, 0);
        market.execute(Direction.BUY, 1, 13);
        assert market.validateOrderState(1, 0, 0, 0, 0);
        market.execute(Direction.BUY, 1, 12);
        assert market.validateOrderState(0, 1, 0, 0, 0);
    }

    @Test
    void longMarketWithTrigger() {
        Order order = Order.buy(1).stopAt(12);
        market.request(order).to();
        market.execute(Direction.BUY, 1, 11);
        assert market.validateOrderState(1, 0, 0, 0, 0);
        market.execute(Direction.BUY, 1, 12);
        assert market.validateOrderState(1, 0, 0, 0, 0);
        market.execute(Direction.BUY, 1, 13);
        assert market.validateOrderState(0, 1, 0, 0, 0);
        assert order.price.v.is(13);

        order = Order.buy(1).stopAt(12);
        market.request(order).to();
        market.execute(Direction.BUY, 1, 11);
        assert market.validateOrderState(1, 1, 0, 0, 0);
        market.execute(Direction.BUY, 1, 12);
        assert market.validateOrderState(1, 1, 0, 0, 0);
        market.execute(Direction.BUY, 1, 11);
        assert market.validateOrderState(0, 2, 0, 0, 0);
        assert order.price.v.is(11);
    }

    @Test
    void fillOrKillLong() {
        // success
        market.request(Order.buy(10).price(10).type(QuantityCondition.FillOrKill)).to();
        market.execute(Direction.BUY, 10, 10);
        assert market.validateOrderState(0, 1, 0, 0, 0);

        // large price will success
        market.request(Order.buy(10).price(10).type(QuantityCondition.FillOrKill)).to();
        market.execute(Direction.BUY, 10, 9);
        assert market.validateOrderState(0, 2, 0, 0, 0);

        // large size will success
        market.request(Order.buy(10).price(10).type(QuantityCondition.FillOrKill)).to();
        market.execute(Direction.BUY, 15, 10);
        assert market.validateOrderState(0, 3, 0, 0, 0);

        // less size will be failed
        market.request(Order.buy(10).price(10).type(QuantityCondition.FillOrKill)).to();
        market.execute(Direction.BUY, 4, 10);
        assert market.validateOrderState(0, 3, 0, 0, 0);

        // less price will be failed
        market.request(Order.buy(10).price(10).type(QuantityCondition.FillOrKill)).to();
        market.execute(Direction.BUY, 10, 11);
        assert market.validateOrderState(0, 3, 0, 0, 0);
    }

    @Test
    void fillOrKillShort() {
        // success
        market.request(Order.sell(1).price(10).type(QuantityCondition.FillOrKill)).to();
        market.execute(Direction.SELL, 1, 10);
        assert market.validateOrderState(0, 1, 0, 0, 0);

        // large price will success
        market.request(Order.sell(1).price(10).type(QuantityCondition.FillOrKill)).to();
        market.execute(Direction.SELL, 1, 11);
        assert market.validateOrderState(0, 2, 0, 0, 0);

        // large size will success
        market.request(Order.sell(10).price(10).type(QuantityCondition.FillOrKill)).to();
        market.execute(Direction.SELL, 15, 10);
        assert market.validateOrderState(0, 3, 0, 0, 0);

        // less size will be failed
        market.request(Order.sell(10).price(10).type(QuantityCondition.FillOrKill)).to();
        market.execute(Direction.SELL, 4, 10);
        assert market.validateOrderState(0, 3, 0, 0, 0);

        // less price will be failed
        market.request(Order.sell(10).price(10).type(QuantityCondition.FillOrKill)).to();
        market.execute(Direction.SELL, 10, 9);
        assert market.validateOrderState(0, 3, 0, 0, 0);
    }

    @Test
    void immediateOrCancelLong() {
        // success
        market.request(Order.buy(1).price(10).type(QuantityCondition.ImmediateOrCancel)).to();
        market.execute(Direction.BUY, 1, 10);
        assert market.validateOrderState(0, 1, 0, 0, 0);

        // large price will success
        market.request(Order.buy(1).price(10).type(QuantityCondition.ImmediateOrCancel)).to();
        market.execute(Direction.BUY, 1, 9);
        assert market.validateOrderState(0, 2, 0, 0, 0);

        // large size will success
        market.request(Order.buy(10).price(10).type(QuantityCondition.ImmediateOrCancel)).to();
        market.execute(Direction.BUY, 5, 10);
        assert market.validateOrderState(0, 3, 0, 0, 0);

        // less size will success
        market.request(Order.buy(10).price(10).type(QuantityCondition.ImmediateOrCancel)).to();
        market.execute(Direction.BUY, 4, 10);
        assert market.validateOrderState(0, 4, 0, 0, 0);
        assert market.orders().get(3).executedSize.is(4);

        // less price will be failed
        market.request(Order.buy(1).price(10).type(QuantityCondition.ImmediateOrCancel)).to();
        market.execute(Direction.BUY, 1, 11);
        assert market.validateOrderState(0, 4, 0, 0, 0);
    }

    @Test
    void immediateOrCancelShort() {
        // success
        market.request(Order.sell(1).price(10).type(QuantityCondition.ImmediateOrCancel)).to();
        market.execute(Direction.SELL, 1, 10);
        assert market.validateOrderState(0, 1, 0, 0, 0);

        // large price will success
        market.request(Order.sell(1).price(10).type(QuantityCondition.ImmediateOrCancel)).to();
        market.execute(Direction.SELL, 1, 11);
        assert market.validateOrderState(0, 2, 0, 0, 0);

        // large size will success
        market.request(Order.sell(10).price(10).type(QuantityCondition.ImmediateOrCancel)).to();
        market.execute(Direction.SELL, 15, 10);
        assert market.validateOrderState(0, 3, 0, 0, 0);

        // less size will success
        market.request(Order.sell(10).price(10).type(QuantityCondition.ImmediateOrCancel)).to();
        market.execute(Direction.SELL, 4, 10);
        assert market.validateOrderState(0, 4, 0, 0, 0);
        assert market.orders().get(3).executedSize.is(4);

        // less price will be failed
        market.request(Order.sell(1).price(10).type(QuantityCondition.ImmediateOrCancel)).to();
        market.execute(Direction.SELL, 1, 9);
        assert market.validateOrderState(0, 4, 0, 0, 0);
    }

    @Test
    void marketLong() {
        market.request(Order.buy(1)).to();
        market.execute(Direction.SELL, 1, 10);
        assert market.orders().get(0).price.v.is(10);
        assert market.orders().get(0).executedSize.is(1);

        // divide
        market.request(Order.buy(10)).to();
        market.execute(Direction.BUY, 5, 10);
        market.execute(Direction.BUY, 5, 20);
        assert market.orders().get(1).price.v.is(15);
        assert market.orders().get(1).executedSize.is(10);

        // divide overflow
        market.request(Order.buy(10)).to();
        market.execute(Direction.BUY, 5, 10);
        market.execute(Direction.BUY, 14, 20);
        assert market.orders().get(2).price.v.is(15);
        assert market.orders().get(2).executedSize.is(10);

        // divide underflow
        market.request(Order.buy(10)).to();
        market.execute(Direction.BUY, 5, 10);
        market.execute(Direction.BUY, 3, 20);
        assert market.orders().get(3).price.v.is("13.75");
        assert market.orders().get(3).executedSize.is(8);
        market.execute(Direction.BUY, 2, 20);
        assert market.orders().get(3).price.v.is("15");

        // down price
        market.request(Order.buy(10)).to();
        market.execute(Direction.BUY, 5, 10);
        market.execute(Direction.BUY, 5, 5);
        assert market.orders().get(4).price.v.is("10");
        assert market.orders().get(4).executedSize.is(10);

        // up price
        market.request(Order.buy(10)).to();
        market.execute(Direction.BUY, 5, 10);
        market.execute(Direction.BUY, 5, 20);
        assert market.orders().get(5).price.v.is("15");
        assert market.orders().get(5).executedSize.is(10);
    }

    @Test
    void marketShort() {
        Order order = Order.sell(1);
        market.request(order).to();
        market.execute(Direction.SELL, 1, 10);
        assert order.price.v.is(10);
        assert order.executedSize.is(1);

        // divide
        order = Order.sell(10);
        market.request(order).to();
        market.execute(Direction.BUY, 5, 10);
        market.execute(Direction.BUY, 5, 5);
        assert order.price.v.is("7.5");
        assert order.executedSize.is(10);

        // divide overflow
        order = Order.sell(10);
        market.request(order).to();
        market.execute(Direction.BUY, 5, 10);
        market.execute(Direction.BUY, 14, 5);
        assert order.price.v.is("7.5");
        assert order.executedSize.is(10);

        // divide underflow
        order = Order.sell(10);
        market.request(order).to();
        market.execute(Direction.BUY, 5, 20);
        market.execute(Direction.BUY, 3, 15);
        assert order.price.v.is("18.125");
        assert order.executedSize.is(8);
        market.execute(Direction.BUY, 2, 10);
        assert order.price.v.is("16.5");

        // down price
        order = Order.sell(10);
        market.request(order).to();
        market.execute(Direction.BUY, 5, 10);
        market.execute(Direction.BUY, 5, 5);
        assert order.price.v.is("7.5");
        assert order.executedSize.is(10);

        // up price
        order = Order.sell(10);
        market.request(order).to();
        market.execute(Direction.BUY, 5, 10);
        market.execute(Direction.BUY, 5, 20);
        assert order.price.v.is("10");
        assert order.executedSize.is(10);
    }

    @Test
    void cancel() {
        Order order = market.request(Order.sell(1).price(12)).to().v;
        market.execute(Direction.BUY, 1, 11);
        assert market.validateOrderState(1, 0, 0, 0, 0);
        market.cancel(order).to();
        assert market.validateOrderState(0, 0, 1, 0, 0);
        market.execute(Direction.BUY, 1, 12);
        assert market.validateOrderState(0, 0, 1, 0, 0);
    }

    @Test
    void observeSequencialExecutionsBySellSize() {
        AtomicReference<Num> size = new AtomicReference<>();

        market.timelineByTaker.to(e -> {
            size.set(e.cumulativeSize);
        });

        market.executeSequencially(4, Executed.sell(5).price(10));
        market.execute(Direction.SELL, 5, 10);
        assert size.get().is(20);
    }

    @Test
    void observeSequencialExecutionsByBuySize() {
        AtomicReference<Num> size = new AtomicReference<>();

        market.timelineByTaker.to(e -> {
            size.set(e.cumulativeSize);
        });

        market.executeSequencially(4, Executed.buy(5).price(10));
        market.execute(Direction.BUY, 5, 10);
        assert size.get().is(20);
    }
}
