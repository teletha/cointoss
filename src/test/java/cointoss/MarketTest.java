/*
 * Copyright (C) 2018 CoinToss Development Team
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          https://opensource.org/licenses/MIT
 */
package cointoss;

import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import org.junit.jupiter.api.Test;

import cointoss.order.Order;
import cointoss.order.Order.Quantity;
import cointoss.order.Order.State;
import cointoss.util.Num;

/**
 * @version 2018/04/02 16:48:08
 */
class MarketTest {

    @Test
    void requestOrder() {
        TestableMarket market = new TestableMarket();

        assert market.orders().isEmpty();
        Order order = Order.limitLong(1, 10);
        market.requestSuccessfully(order);
        assert order.isBuy();
        assert order.executed_size.get().is(0);
        assert market.orders().size() == 1;
    }

    @Test
    void execute() {
        TestableMarket market = new TestableMarket();
        market.request(Order.limitLong(1, 10)).to(order -> {
            assert order.remainingSize.get().is(1);
            assert order.executed_size.get().is(0);

            market.execute(Side.BUY, 1, 10);
            assert order.remainingSize.get().is(0);
            assert order.executed_size.get().is(1);
        });
    }

    @Test
    void executeDivided() {
        TestableMarket market = new TestableMarket();
        market.request(Order.limitLong(10, 10)).to(order -> {
            assert order.remainingSize.get().is(10);
            assert order.executed_size.get().is(0);

            market.execute(Side.SELL, 5, 10);
            assert order.remainingSize.get().is(5);
            assert order.executed_size.get().is(5);

            market.execute(Side.SELL, 5, 10);
            assert order.remainingSize.get().is(0);
            assert order.executed_size.get().is(10);
        });
        assert market.validateExecutionState(2);
    }

    @Test
    void executeOverflow() {
        TestableMarket market = new TestableMarket();
        market.request(Order.limitLong(10, 10)).to(order -> {
            assert order.remainingSize.get().is(10);
            assert order.executed_size.get().is(0);

            market.execute(Side.BUY, 7, 10);
            assert order.remainingSize.get().is(3);
            assert order.executed_size.get().is(7);

            market.execute(Side.SELL, 7, 10);
            assert order.remainingSize.get().is(0);
            assert order.executed_size.get().is(10);
        });

        List<Order> orders = market.orders();
        assert orders.size() == 1;
        assert orders.get(0).state.is(State.COMPLETED);

        List<Execution> executions = market.executions();
        assert executions.size() == 2;
        assert executions.get(0).size.is(7);
        assert executions.get(1).size.is(3);
    }

    @Test
    void executeExtra() {
        TestableMarket market = new TestableMarket();
        market.request(Order.limitLong(10, 10)).to(order -> {
            assert order.remainingSize.get().is(10);
            assert order.executed_size.get().is(0);

            market.execute(Side.BUY, 10, 10);
            assert order.remainingSize.get().is(0);
            assert order.executed_size.get().is(10);

            market.execute(Side.SELL, 1, 10);
            assert order.remainingSize.get().is(0);
            assert order.executed_size.get().is(10);
        });

        assert market.validateOrderState(0, 1, 0, 0, 0);
        assert market.validateExecutionState(1);
    }

    @Test
    void executeLongWithUpperPrice() {
        TestableMarket market = new TestableMarket();
        market.request(Order.limitLong(10, 10)).to(order -> {
            market.execute(Side.BUY, 5, 12);
            market.execute(Side.SELL, 5, 13);
        });

        assert market.validateOrderState(1, 0, 0, 0, 0);
        assert market.validateExecutionState(0);
    }

    @Test
    void executeLongWithLowerPrice() {
        TestableMarket market = new TestableMarket();
        market.request(Order.limitLong(10, 10)).to(order -> {
            market.execute(Side.BUY, 5, 8);
            market.execute(Side.SELL, 5, 7);
        });

        assert market.validateOrderState(0, 1, 0, 0, 0);
        assert market.validateExecutionState(2);
    }

    @Test
    void executeShortWithUpperPrice() {
        TestableMarket market = new TestableMarket();
        market.request(Order.limitShort(10, 10)).to(order -> {
            market.execute(Side.BUY, 5, 12);
            market.execute(Side.SELL, 5, 13);
        });

        assert market.validateOrderState(0, 1, 0, 0, 0);
        assert market.validateExecutionState(2);
    }

    @Test
    void executeShortWithLowerPrice() {
        TestableMarket market = new TestableMarket();
        market.request(Order.limitShort(10, 10)).to(order -> {
            market.execute(Side.BUY, 5, 8);
            market.execute(Side.SELL, 5, 7);
        });

        assert market.validateOrderState(1, 0, 0, 0, 0);
        assert market.validateExecutionState(0);
    }

    @Test
    void lag() {
        TestableMarket market = new TestableMarket(5);

        market.requestSuccessfully(Order.limitLong(10, 10));
        market.execute(Side.BUY, 5, 10, Time.at(3));
        market.execute(Side.BUY, 4, 10, Time.at(4));
        market.execute(Side.BUY, 3, 10, Time.at(5));
        market.execute(Side.BUY, 2, 10, Time.at(6));
        market.execute(Side.BUY, 1, 10, Time.at(7));

        assert market.validateExecutionState(3);
        assert market.validateOrderState(1, 0, 0, 0, 0);
    }

    @Test
    void shortWithTrigger() {
        TestableMarket market = new TestableMarket();

        market.requestSuccessfully(Order.limitShort(1, 7).when(8));
        market.execute(Side.BUY, 1, 9);
        assert market.validateOrderState(1, 0, 0, 0, 0);
        market.execute(Side.BUY, 1, 8);
        assert market.validateOrderState(1, 0, 0, 0, 0);
        market.execute(Side.BUY, 1, 7);
        assert market.validateOrderState(0, 1, 0, 0, 0);
    }

    @Test
    void shortWithTriggerSamePrice() {
        TestableMarket market = new TestableMarket();

        market.requestSuccessfully(Order.limitShort(1, 8).when(8));
        market.execute(Side.BUY, 1, 9);
        assert market.validateOrderState(1, 0, 0, 0, 0);
        market.execute(Side.BUY, 1, 8);
        assert market.validateOrderState(1, 0, 0, 0, 0);
        market.execute(Side.BUY, 1, 7);
        assert market.validateOrderState(1, 0, 0, 0, 0);
        market.execute(Side.BUY, 1, 8);
        assert market.validateOrderState(0, 1, 0, 0, 0);
    }

    @Test
    void shortMarketWithTrigger() {
        TestableMarket market = new TestableMarket();

        Order order = Order.marketShort(1).when(8);
        market.requestSuccessfully(order);
        market.execute(Side.BUY, 1, 9);
        assert market.validateOrderState(1, 0, 0, 0, 0);
        market.execute(Side.BUY, 1, 8);
        assert market.validateOrderState(1, 0, 0, 0, 0);
        market.execute(Side.BUY, 1, 7);
        assert market.validateOrderState(0, 1, 0, 0, 0);
        assert order.averagePrice.get().is(7);

        order = Order.marketShort(1).when(8);
        market.requestSuccessfully(order);
        market.execute(Side.BUY, 1, 9);
        assert market.validateOrderState(1, 1, 0, 0, 0);
        market.execute(Side.BUY, 1, 8);
        assert market.validateOrderState(1, 1, 0, 0, 0);
        market.execute(Side.BUY, 1, 9);
        assert market.validateOrderState(0, 2, 0, 0, 0);
        assert order.averagePrice.get().is(9);
    }

    @Test
    void longWithTrigger() {
        TestableMarket market = new TestableMarket();

        market.requestSuccessfully(Order.limitLong(1, 13).when(12));
        market.execute(Side.BUY, 1, 11);
        assert market.validateOrderState(1, 0, 0, 0, 0);
        market.execute(Side.BUY, 1, 12);
        assert market.validateOrderState(1, 0, 0, 0, 0);
        market.execute(Side.BUY, 1, 13);
        assert market.validateOrderState(0, 1, 0, 0, 0);
    }

    @Test
    void longWithTriggerSamePrice() {
        TestableMarket market = new TestableMarket();

        market.requestSuccessfully(Order.limitLong(1, 12).when(12));
        market.execute(Side.BUY, 1, 11);
        assert market.validateOrderState(1, 0, 0, 0, 0);
        market.execute(Side.BUY, 1, 12);
        assert market.validateOrderState(1, 0, 0, 0, 0);
        market.execute(Side.BUY, 1, 13);
        assert market.validateOrderState(1, 0, 0, 0, 0);
        market.execute(Side.BUY, 1, 12);
        assert market.validateOrderState(0, 1, 0, 0, 0);
    }

    @Test
    void longMarketWithTrigger() {
        TestableMarket market = new TestableMarket();

        Order order = Order.marketLong(1).when(12);
        market.requestSuccessfully(order);
        market.execute(Side.BUY, 1, 11);
        assert market.validateOrderState(1, 0, 0, 0, 0);
        market.execute(Side.BUY, 1, 12);
        assert market.validateOrderState(1, 0, 0, 0, 0);
        market.execute(Side.BUY, 1, 13);
        assert market.validateOrderState(0, 1, 0, 0, 0);
        assert order.averagePrice.get().is(13);

        order = Order.marketLong(1).when(12);
        market.requestSuccessfully(order);
        market.execute(Side.BUY, 1, 11);
        assert market.validateOrderState(1, 1, 0, 0, 0);
        market.execute(Side.BUY, 1, 12);
        assert market.validateOrderState(1, 1, 0, 0, 0);
        market.execute(Side.BUY, 1, 11);
        assert market.validateOrderState(0, 2, 0, 0, 0);
        assert order.averagePrice.get().is(11);
    }

    @Test
    void fillOrKillLong() {
        TestableMarket market = new TestableMarket();

        // success
        market.requestSuccessfully(Order.limitLong(10, 10).type(Quantity.FillOrKill));
        market.execute(Side.BUY, 10, 10);
        assert market.validateOrderState(0, 1, 0, 0, 0);

        // large price will success
        market.requestSuccessfully(Order.limitLong(10, 10).type(Quantity.FillOrKill));
        market.execute(Side.BUY, 10, 9);
        assert market.validateOrderState(0, 2, 0, 0, 0);

        // large size will success
        market.requestSuccessfully(Order.limitLong(10, 10).type(Quantity.FillOrKill));
        market.execute(Side.BUY, 15, 10);
        assert market.validateOrderState(0, 3, 0, 0, 0);

        // less size will be failed
        market.requestSuccessfully(Order.limitLong(10, 10).type(Quantity.FillOrKill));
        market.execute(Side.BUY, 4, 10);
        assert market.validateOrderState(0, 3, 0, 0, 0);

        // less price will be failed
        market.requestSuccessfully(Order.limitLong(10, 10).type(Quantity.FillOrKill));
        market.execute(Side.BUY, 10, 11);
        assert market.validateOrderState(0, 3, 0, 0, 0);
    }

    @Test
    void fillOrKillShort() {
        TestableMarket market = new TestableMarket();

        // success
        market.requestSuccessfully(Order.limitShort(1, 10).type(Quantity.FillOrKill));
        market.execute(Side.SELL, 1, 10);
        assert market.validateOrderState(0, 1, 0, 0, 0);

        // large price will success
        market.requestSuccessfully(Order.limitShort(1, 10).type(Quantity.FillOrKill));
        market.execute(Side.SELL, 1, 11);
        assert market.validateOrderState(0, 2, 0, 0, 0);

        // large size will success
        market.requestSuccessfully(Order.limitShort(10, 10).type(Quantity.FillOrKill));
        market.execute(Side.SELL, 15, 10);
        assert market.validateOrderState(0, 3, 0, 0, 0);

        // less size will be failed
        market.requestSuccessfully(Order.limitShort(10, 10).type(Quantity.FillOrKill));
        market.execute(Side.SELL, 4, 10);
        assert market.validateOrderState(0, 3, 0, 0, 0);

        // less price will be failed
        market.requestSuccessfully(Order.limitShort(10, 10).type(Quantity.FillOrKill));
        market.execute(Side.SELL, 10, 9);
        assert market.validateOrderState(0, 3, 0, 0, 0);
    }

    @Test
    void immediateOrCancelLong() {
        TestableMarket market = new TestableMarket();

        // success
        market.requestSuccessfully(Order.limitLong(1, 10).type(Quantity.ImmediateOrCancel));
        market.execute(Side.BUY, 1, 10);
        assert market.validateOrderState(0, 1, 0, 0, 0);

        // large price will success
        market.requestSuccessfully(Order.limitLong(1, 10).type(Quantity.ImmediateOrCancel));
        market.execute(Side.BUY, 1, 9);
        assert market.validateOrderState(0, 2, 0, 0, 0);

        // large size will success
        market.requestSuccessfully(Order.limitLong(10, 10).type(Quantity.ImmediateOrCancel));
        market.execute(Side.BUY, 5, 10);
        assert market.validateOrderState(0, 3, 0, 0, 0);

        // less size will success
        market.requestSuccessfully(Order.limitLong(10, 10).type(Quantity.ImmediateOrCancel));
        market.execute(Side.BUY, 4, 10);
        assert market.validateOrderState(0, 4, 0, 0, 0);
        assert market.orders().get(3).executed_size.get().is(4);

        // less price will be failed
        market.requestSuccessfully(Order.limitLong(1, 10).type(Quantity.ImmediateOrCancel));
        market.execute(Side.BUY, 1, 11);
        assert market.validateOrderState(0, 4, 0, 0, 0);
    }

    @Test
    void immediateOrCancelShort() {
        TestableMarket market = new TestableMarket();

        // success
        market.requestSuccessfully(Order.limitShort(1, 10).type(Quantity.ImmediateOrCancel));
        market.execute(Side.SELL, 1, 10);
        assert market.validateOrderState(0, 1, 0, 0, 0);

        // large price will success
        market.requestSuccessfully(Order.limitShort(1, 10).type(Quantity.ImmediateOrCancel));
        market.execute(Side.SELL, 1, 11);
        assert market.validateOrderState(0, 2, 0, 0, 0);

        // large size will success
        market.requestSuccessfully(Order.limitShort(10, 10).type(Quantity.ImmediateOrCancel));
        market.execute(Side.SELL, 15, 10);
        assert market.validateOrderState(0, 3, 0, 0, 0);

        // less size will success
        market.requestSuccessfully(Order.limitShort(10, 10).type(Quantity.ImmediateOrCancel));
        market.execute(Side.SELL, 4, 10);
        assert market.validateOrderState(0, 4, 0, 0, 0);
        assert market.orders().get(3).executed_size.get().is(4);

        // less price will be failed
        market.requestSuccessfully(Order.limitShort(1, 10).type(Quantity.ImmediateOrCancel));
        market.execute(Side.SELL, 1, 9);
        assert market.validateOrderState(0, 4, 0, 0, 0);
    }

    @Test
    void marketLong() {
        TestableMarket market = new TestableMarket();

        market.requestSuccessfully(Order.marketLong(1));
        market.execute(Side.SELL, 1, 10);
        assert market.orders().get(0).averagePrice.get().is(10);
        assert market.orders().get(0).executed_size.get().is(1);

        // divide
        market.requestSuccessfully(Order.marketLong(10));
        market.execute(Side.BUY, 5, 10);
        market.execute(Side.BUY, 5, 20);
        assert market.orders().get(1).averagePrice.get().is(15);
        assert market.orders().get(1).executed_size.get().is(10);

        // divide overflow
        market.requestSuccessfully(Order.marketLong(10));
        market.execute(Side.BUY, 5, 10);
        market.execute(Side.BUY, 14, 20);
        assert market.orders().get(2).averagePrice.get().is(15);
        assert market.orders().get(2).executed_size.get().is(10);

        // divide underflow
        market.requestSuccessfully(Order.marketLong(10));
        market.execute(Side.BUY, 5, 10);
        market.execute(Side.BUY, 3, 20);
        assert market.orders().get(3).averagePrice.get().is("13.75");
        assert market.orders().get(3).executed_size.get().is(8);
        market.execute(Side.BUY, 2, 20);
        assert market.orders().get(3).averagePrice.get().is("15");

        // down price
        market.requestSuccessfully(Order.marketLong(10));
        market.execute(Side.BUY, 5, 10);
        market.execute(Side.BUY, 5, 5);
        assert market.orders().get(4).averagePrice.get().is("10");
        assert market.orders().get(4).executed_size.get().is(10);

        // up price
        market.requestSuccessfully(Order.marketLong(10));
        market.execute(Side.BUY, 5, 10);
        market.execute(Side.BUY, 5, 20);
        assert market.orders().get(5).averagePrice.get().is("15");
        assert market.orders().get(5).executed_size.get().is(10);
    }

    @Test
    void marketShort() {
        TestableMarket market = new TestableMarket();

        Order order = Order.marketShort(1);
        market.requestSuccessfully(order);
        market.execute(Side.SELL, 1, 10);
        assert order.averagePrice.get().is(10);
        assert order.executed_size.get().is(1);

        // divide
        order = Order.marketShort(10);
        market.requestSuccessfully(order);
        market.execute(Side.BUY, 5, 10);
        market.execute(Side.BUY, 5, 5);
        assert order.averagePrice.get().is("7.5");
        assert order.executed_size.get().is(10);

        // divide overflow
        order = Order.marketShort(10);
        market.requestSuccessfully(order);
        market.execute(Side.BUY, 5, 10);
        market.execute(Side.BUY, 14, 5);
        assert order.averagePrice.get().is("7.5");
        assert order.executed_size.get().is(10);

        // divide underflow
        order = Order.marketShort(10);
        market.requestSuccessfully(order);
        market.execute(Side.BUY, 5, 20);
        market.execute(Side.BUY, 3, 15);
        assert order.averagePrice.get().is("18.125");
        assert order.executed_size.get().is(8);
        market.execute(Side.BUY, 2, 10);
        assert order.averagePrice.get().is("16.5");

        // down price
        order = Order.marketShort(10);
        market.requestSuccessfully(order);
        market.execute(Side.BUY, 5, 10);
        market.execute(Side.BUY, 5, 5);
        assert order.averagePrice.get().is("7.5");
        assert order.executed_size.get().is(10);

        // up price
        order = Order.marketShort(10);
        market.requestSuccessfully(order);
        market.execute(Side.BUY, 5, 10);
        market.execute(Side.BUY, 5, 20);
        assert order.averagePrice.get().is("10");
        assert order.executed_size.get().is(10);
    }

    @Test
    void cancel() {
        TestableMarket market = new TestableMarket();

        Order order = market.requestSuccessfully(Order.limitShort(1, 12));
        market.execute(Side.BUY, 1, 11);
        assert market.validateOrderState(1, 0, 0, 0, 0);
        market.cancel(order).to();
        assert market.validateOrderState(0, 0, 1, 0, 0);
        market.execute(Side.BUY, 1, 12);
        assert market.validateOrderState(0, 0, 1, 0, 0);
    }

    @Test
    void observeSequencialExecutionsBySellSize() {
        AtomicReference<Num> size = new AtomicReference<>();

        TestableMarket market = new TestableMarket();
        market.timelineByTaker.to(e -> {
            size.set(e.cumulativeSize);
        });

        market.execute(Side.SELL, 5, 10, "Buy-1", "Sell-1");
        assert size.get() == null;
        market.execute(Side.SELL, 5, 10, "Buy-2", "Sell-1");
        assert size.get() == null;
        market.execute(Side.SELL, 5, 10, "Buy-3", "Sell-1");
        assert size.get() == null;
        market.execute(Side.SELL, 5, 10, "Buy-4", "Sell-1");
        market.execute(Side.SELL, 5, 10, "Buy-5", "Sell-2");
        assert size.get().is(20);
    }

    @Test
    void observeSequencialExecutionsByBuySize() {
        AtomicReference<Num> size = new AtomicReference<>();

        TestableMarket market = new TestableMarket();
        market.timelineByTaker.to(e -> {
            size.set(e.cumulativeSize);
        });

        market.execute(Side.BUY, 5, 10, "Buy-1", "Sell-1");
        assert size.get() == null;
        market.execute(Side.BUY, 5, 10, "Buy-1", "Sell-2");
        assert size.get() == null;
        market.execute(Side.BUY, 5, 10, "Buy-1", "Sell-3");
        assert size.get() == null;
        market.execute(Side.BUY, 5, 10, "Buy-1", "Sell-4");
        market.execute(Side.BUY, 5, 10, "Buy-2", "Sell-5");
        assert size.get().is(20);
    }
}
