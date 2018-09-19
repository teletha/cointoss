/*
 * Copyright (C) 2018 CoinToss Development Team
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          https://opensource.org/licenses/MIT
 */
package cointoss.backtest;

import static cointoss.util.Num.*;

import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import org.junit.jupiter.api.Test;

import cointoss.MarketTestSupport;
import cointoss.Side;
import cointoss.VerifiableMarket;
import cointoss.order.Order;
import cointoss.order.OrderState;
import cointoss.order.QuantityCondition;
import cointoss.util.Num;

/**
 * @version 2018/04/29 16:12:46
 */
class VerifiableMarketTest {

    static final Num FIVE = Num.of(5);

    VerifiableMarket market = new VerifiableMarket();

    @Test
    void requestOrder() {
        Order order = market.requestTo(Order.limitLong(1, 10));
        assert order.isBuy();
        assert order.executedSize.is(ZERO);
        assert market.orders().size() == 1;
    }

    @Test
    void execute() {
        Order order = market.requestTo(Order.limitLong(1, 10));
        assert order.remainingSize.is(ONE);
        assert order.executedSize.is(ZERO);

        market.execute(Side.BUY, 1, 10);
        assert order.remainingSize.is(ZERO);
        assert order.executedSize.is(ONE);
    }

    @Test
    void executeDivided() {
        Order order = market.requestTo(Order.limitLong(10, 10));
        assert order.remainingSize.is(TEN);
        assert order.executedSize.is(ZERO);

        market.execute(Side.SELL, 5, 10);
        assert order.remainingSize.is(FIVE);
        assert order.executedSize.is(FIVE);

        market.execute(Side.SELL, 5, 10);
        assert order.remainingSize.is(ZERO);
        assert order.executedSize.is(TEN);
        assert market.validateExecutionState(2);
    }

    @Test
    void executeOverflow() {
        market.request(Order.limitLong(10, 10)).to(order -> {
            assert order.remainingSize.is(10);
            assert order.executedSize.is(0);

            market.execute(Side.BUY, 7, 10);
            assert order.remainingSize.is(3);
            assert order.executedSize.is(7);

            market.execute(Side.SELL, 7, 10);
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
        market.request(Order.limitLong(10, 10)).to(order -> {
            assert order.remainingSize.is(10);
            assert order.executedSize.is(0);

            market.execute(Side.BUY, 10, 10);
            assert order.remainingSize.is(0);
            assert order.executedSize.is(10);

            market.execute(Side.SELL, 1, 10);
            assert order.remainingSize.is(0);
            assert order.executedSize.is(10);
        });

        assert market.validateOrderState(0, 1, 0, 0, 0);
        assert market.validateExecutionState(1);
    }

    @Test
    void executeLongWithUpperPrice() {
        market.request(Order.limitLong(10, 10)).to(order -> {
            market.execute(Side.BUY, 5, 12);
            market.execute(Side.SELL, 5, 13);
        });

        assert market.validateOrderState(1, 0, 0, 0, 0);
        assert market.validateExecutionState(0);
    }

    @Test
    void executeLongWithLowerPrice() {
        market.request(Order.limitLong(10, 10)).to(order -> {
            market.execute(Side.BUY, 5, 8);
            market.execute(Side.SELL, 5, 7);
        });

        assert market.validateOrderState(0, 1, 0, 0, 0);
        assert market.validateExecutionState(2);
    }

    @Test
    void executeShortWithUpperPrice() {
        market.request(Order.limitShort(10, 10)).to(order -> {
            market.execute(Side.BUY, 5, 12);
            market.execute(Side.SELL, 5, 13);
        });

        assert market.validateOrderState(0, 1, 0, 0, 0);
        assert market.validateExecutionState(2);
    }

    @Test
    void executeShortWithLowerPrice() {
        market.request(Order.limitShort(10, 10)).to(order -> {
            market.execute(Side.BUY, 5, 8);
            market.execute(Side.SELL, 5, 7);
        });

        assert market.validateOrderState(1, 0, 0, 0, 0);
        assert market.validateExecutionState(0);
    }

    @Test
    void lag() {
        VerifiableMarket market = new VerifiableMarket();
        market.service.lag(5);

        market.requestTo(Order.limitLong(10, 10));
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
        market.requestTo(Order.limitShort(1, 7).stopAt(8));
        market.execute(Side.BUY, 1, 9);
        assert market.validateOrderState(1, 0, 0, 0, 0);
        market.execute(Side.BUY, 1, 8);
        assert market.validateOrderState(1, 0, 0, 0, 0);
        market.execute(Side.BUY, 1, 7);
        assert market.validateOrderState(0, 1, 0, 0, 0);
    }

    @Test
    void shortWithTriggerSamePrice() {
        market.requestTo(Order.limitShort(1, 8).stopAt(8));
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
        Order order = Order.marketShort(1).stopAt(8);
        market.requestTo(order);
        market.execute(Side.BUY, 1, 9);
        assert market.validateOrderState(1, 0, 0, 0, 0);
        market.execute(Side.BUY, 1, 8);
        assert market.validateOrderState(1, 0, 0, 0, 0);
        market.execute(Side.BUY, 1, 7);
        assert market.validateOrderState(0, 1, 0, 0, 0);
        assert order.price.v.is(7);

        order = Order.marketShort(1).stopAt(8);
        market.requestTo(order);
        market.execute(Side.BUY, 1, 9);
        assert market.validateOrderState(1, 1, 0, 0, 0);
        market.execute(Side.BUY, 1, 8);
        assert market.validateOrderState(1, 1, 0, 0, 0);
        market.execute(Side.BUY, 1, 9);
        assert market.validateOrderState(0, 2, 0, 0, 0);
        assert order.price.v.is(9);
    }

    @Test
    void longWithTrigger() {
        market.requestTo(Order.limitLong(1, 13).stopAt(12));
        market.execute(Side.BUY, 1, 11);
        assert market.validateOrderState(1, 0, 0, 0, 0);
        market.execute(Side.BUY, 1, 12);
        assert market.validateOrderState(1, 0, 0, 0, 0);
        market.execute(Side.BUY, 1, 13);
        assert market.validateOrderState(0, 1, 0, 0, 0);
    }

    @Test
    void longWithTriggerSamePrice() {
        market.requestTo(Order.limitLong(1, 12).stopAt(12));
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
        Order order = Order.marketLong(1).stopAt(12);
        market.requestTo(order);
        market.execute(Side.BUY, 1, 11);
        assert market.validateOrderState(1, 0, 0, 0, 0);
        market.execute(Side.BUY, 1, 12);
        assert market.validateOrderState(1, 0, 0, 0, 0);
        market.execute(Side.BUY, 1, 13);
        assert market.validateOrderState(0, 1, 0, 0, 0);
        assert order.price.v.is(13);

        order = Order.marketLong(1).stopAt(12);
        market.requestTo(order);
        market.execute(Side.BUY, 1, 11);
        assert market.validateOrderState(1, 1, 0, 0, 0);
        market.execute(Side.BUY, 1, 12);
        assert market.validateOrderState(1, 1, 0, 0, 0);
        market.execute(Side.BUY, 1, 11);
        assert market.validateOrderState(0, 2, 0, 0, 0);
        assert order.price.v.is(11);
    }

    @Test
    void fillOrKillLong() {
        // success
        market.requestTo(Order.limitLong(10, 10).type(QuantityCondition.FillOrKill));
        market.execute(Side.BUY, 10, 10);
        assert market.validateOrderState(0, 1, 0, 0, 0);

        // large price will success
        market.requestTo(Order.limitLong(10, 10).type(QuantityCondition.FillOrKill));
        market.execute(Side.BUY, 10, 9);
        assert market.validateOrderState(0, 2, 0, 0, 0);

        // large size will success
        market.requestTo(Order.limitLong(10, 10).type(QuantityCondition.FillOrKill));
        market.execute(Side.BUY, 15, 10);
        assert market.validateOrderState(0, 3, 0, 0, 0);

        // less size will be failed
        market.requestTo(Order.limitLong(10, 10).type(QuantityCondition.FillOrKill));
        market.execute(Side.BUY, 4, 10);
        assert market.validateOrderState(0, 3, 0, 0, 0);

        // less price will be failed
        market.requestTo(Order.limitLong(10, 10).type(QuantityCondition.FillOrKill));
        market.execute(Side.BUY, 10, 11);
        assert market.validateOrderState(0, 3, 0, 0, 0);
    }

    @Test
    void fillOrKillShort() {
        // success
        market.requestTo(Order.limitShort(1, 10).type(QuantityCondition.FillOrKill));
        market.execute(Side.SELL, 1, 10);
        assert market.validateOrderState(0, 1, 0, 0, 0);

        // large price will success
        market.requestTo(Order.limitShort(1, 10).type(QuantityCondition.FillOrKill));
        market.execute(Side.SELL, 1, 11);
        assert market.validateOrderState(0, 2, 0, 0, 0);

        // large size will success
        market.requestTo(Order.limitShort(10, 10).type(QuantityCondition.FillOrKill));
        market.execute(Side.SELL, 15, 10);
        assert market.validateOrderState(0, 3, 0, 0, 0);

        // less size will be failed
        market.requestTo(Order.limitShort(10, 10).type(QuantityCondition.FillOrKill));
        market.execute(Side.SELL, 4, 10);
        assert market.validateOrderState(0, 3, 0, 0, 0);

        // less price will be failed
        market.requestTo(Order.limitShort(10, 10).type(QuantityCondition.FillOrKill));
        market.execute(Side.SELL, 10, 9);
        assert market.validateOrderState(0, 3, 0, 0, 0);
    }

    @Test
    void immediateOrCancelLong() {
        // success
        market.requestTo(Order.limitLong(1, 10).type(QuantityCondition.ImmediateOrCancel));
        market.execute(Side.BUY, 1, 10);
        assert market.validateOrderState(0, 1, 0, 0, 0);

        // large price will success
        market.requestTo(Order.limitLong(1, 10).type(QuantityCondition.ImmediateOrCancel));
        market.execute(Side.BUY, 1, 9);
        assert market.validateOrderState(0, 2, 0, 0, 0);

        // large size will success
        market.requestTo(Order.limitLong(10, 10).type(QuantityCondition.ImmediateOrCancel));
        market.execute(Side.BUY, 5, 10);
        assert market.validateOrderState(0, 3, 0, 0, 0);

        // less size will success
        market.requestTo(Order.limitLong(10, 10).type(QuantityCondition.ImmediateOrCancel));
        market.execute(Side.BUY, 4, 10);
        assert market.validateOrderState(0, 4, 0, 0, 0);
        assert market.orders().get(3).executedSize.is(4);

        // less price will be failed
        market.requestTo(Order.limitLong(1, 10).type(QuantityCondition.ImmediateOrCancel));
        market.execute(Side.BUY, 1, 11);
        assert market.validateOrderState(0, 4, 0, 0, 0);
    }

    @Test
    void immediateOrCancelShort() {
        // success
        market.requestTo(Order.limitShort(1, 10).type(QuantityCondition.ImmediateOrCancel));
        market.execute(Side.SELL, 1, 10);
        assert market.validateOrderState(0, 1, 0, 0, 0);

        // large price will success
        market.requestTo(Order.limitShort(1, 10).type(QuantityCondition.ImmediateOrCancel));
        market.execute(Side.SELL, 1, 11);
        assert market.validateOrderState(0, 2, 0, 0, 0);

        // large size will success
        market.requestTo(Order.limitShort(10, 10).type(QuantityCondition.ImmediateOrCancel));
        market.execute(Side.SELL, 15, 10);
        assert market.validateOrderState(0, 3, 0, 0, 0);

        // less size will success
        market.requestTo(Order.limitShort(10, 10).type(QuantityCondition.ImmediateOrCancel));
        market.execute(Side.SELL, 4, 10);
        assert market.validateOrderState(0, 4, 0, 0, 0);
        assert market.orders().get(3).executedSize.is(4);

        // less price will be failed
        market.requestTo(Order.limitShort(1, 10).type(QuantityCondition.ImmediateOrCancel));
        market.execute(Side.SELL, 1, 9);
        assert market.validateOrderState(0, 4, 0, 0, 0);
    }

    @Test
    void marketLong() {
        market.requestTo(Order.marketLong(1));
        market.execute(Side.SELL, 1, 10);
        assert market.orders().get(0).price.v.is(10);
        assert market.orders().get(0).executedSize.is(1);

        // divide
        market.requestTo(Order.marketLong(10));
        market.execute(Side.BUY, 5, 10);
        market.execute(Side.BUY, 5, 20);
        assert market.orders().get(1).price.v.is(15);
        assert market.orders().get(1).executedSize.is(10);

        // divide overflow
        market.requestTo(Order.marketLong(10));
        market.execute(Side.BUY, 5, 10);
        market.execute(Side.BUY, 14, 20);
        assert market.orders().get(2).price.v.is(15);
        assert market.orders().get(2).executedSize.is(10);

        // divide underflow
        market.requestTo(Order.marketLong(10));
        market.execute(Side.BUY, 5, 10);
        market.execute(Side.BUY, 3, 20);
        assert market.orders().get(3).price.v.is("13.75");
        assert market.orders().get(3).executedSize.is(8);
        market.execute(Side.BUY, 2, 20);
        assert market.orders().get(3).price.v.is("15");

        // down price
        market.requestTo(Order.marketLong(10));
        market.execute(Side.BUY, 5, 10);
        market.execute(Side.BUY, 5, 5);
        assert market.orders().get(4).price.v.is("10");
        assert market.orders().get(4).executedSize.is(10);

        // up price
        market.requestTo(Order.marketLong(10));
        market.execute(Side.BUY, 5, 10);
        market.execute(Side.BUY, 5, 20);
        assert market.orders().get(5).price.v.is("15");
        assert market.orders().get(5).executedSize.is(10);
    }

    @Test
    void marketShort() {
        Order order = Order.marketShort(1);
        market.requestTo(order);
        market.execute(Side.SELL, 1, 10);
        assert order.price.v.is(10);
        assert order.executedSize.is(1);

        // divide
        order = Order.marketShort(10);
        market.requestTo(order);
        market.execute(Side.BUY, 5, 10);
        market.execute(Side.BUY, 5, 5);
        assert order.price.v.is("7.5");
        assert order.executedSize.is(10);

        // divide overflow
        order = Order.marketShort(10);
        market.requestTo(order);
        market.execute(Side.BUY, 5, 10);
        market.execute(Side.BUY, 14, 5);
        assert order.price.v.is("7.5");
        assert order.executedSize.is(10);

        // divide underflow
        order = Order.marketShort(10);
        market.requestTo(order);
        market.execute(Side.BUY, 5, 20);
        market.execute(Side.BUY, 3, 15);
        assert order.price.v.is("18.125");
        assert order.executedSize.is(8);
        market.execute(Side.BUY, 2, 10);
        assert order.price.v.is("16.5");

        // down price
        order = Order.marketShort(10);
        market.requestTo(order);
        market.execute(Side.BUY, 5, 10);
        market.execute(Side.BUY, 5, 5);
        assert order.price.v.is("7.5");
        assert order.executedSize.is(10);

        // up price
        order = Order.marketShort(10);
        market.requestTo(order);
        market.execute(Side.BUY, 5, 10);
        market.execute(Side.BUY, 5, 20);
        assert order.price.v.is("10");
        assert order.executedSize.is(10);
    }

    @Test
    void cancel() {
        Order order = market.requestTo(Order.limitShort(1, 12));
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

        market.timelineByTaker.to(e -> {
            size.set(e.cumulativeSize);
        });

        MarketTestSupport.executionSerially(4, Side.SELL, 5, 10).forEach(market::execute);
        market.execute(Side.SELL, 5, 10);
        assert size.get().is(20);
    }

    @Test
    void observeSequencialExecutionsByBuySize() {
        AtomicReference<Num> size = new AtomicReference<>();

        market.timelineByTaker.to(e -> {
            size.set(e.cumulativeSize);
        });

        MarketTestSupport.executionSerially(4, Side.BUY, 5, 10).forEach(market::execute);
        market.execute(Side.BUY, 5, 10);
        assert size.get().is(20);
    }
}
