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

import static cointoss.order.OrderState.*;

import java.util.concurrent.atomic.AtomicReference;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import cointoss.execution.Executed;
import cointoss.order.Order;
import cointoss.order.QuantityCondition;
import cointoss.util.Num;

class VerifiableMarketTest {

    VerifiableMarket market = new VerifiableMarket();

    @Test
    void requestBuyLimitOrder() {
        assert market.orders.hasNoActiveOrder();

        market.request(Order.buy(1).price(1)).to(order -> {
            assert order.isBuy();
            assert order.executedSize.is(0);
            assert order.remainingSize.is(1);
            assert market.orders.hasActiveOrder();
        });
    }

    @Test
    void executeBuy() {
        market.request(Order.buy(1).price(10)).to(order -> {
            assert order.remainingSize.is(1);
            assert order.executedSize.is(0);

            market.execute(Executed.buy(1).price(10));
            assert order.remainingSize.is(1);
            assert order.executedSize.is(0);

            market.execute(Executed.buy(1).price(9));
            assert order.remainingSize.is(0);
            assert order.executedSize.is(1);
        });
    }

    @Test
    void executeBuyMinimumPrice() {
        Num min = market.service.setting.baseCurrencyMinimumBidPrice();

        market.request(Order.buy(1).price(min)).to(order -> {
            assert order.remainingSize.is(1);
            assert order.executedSize.is(0);

            market.execute(Executed.buy(1).price(min));
            assert order.remainingSize.is(0);
            assert order.executedSize.is(1);
        });
    }

    @Test
    void executeSell() {
        market.request(Order.sell(1).price(10)).to(order -> {
            assert order.remainingSize.is(1);
            assert order.executedSize.is(0);

            market.execute(Executed.buy(1).price(10));
            assert order.remainingSize.is(1);
            assert order.executedSize.is(0);

            market.execute(Executed.buy(1).price(11));
            assert order.remainingSize.is(0);
            assert order.executedSize.is(1);
        });
    }

    @Test
    void executeDivided() {
        market.request(Order.buy(10).price(10)).to(order -> {
            assert order.remainingSize.is(10);
            assert order.executedSize.is(0);

            market.execute(Executed.buy(5).price(9));
            assert order.remainingSize.is(5);
            assert order.executedSize.is(5);
            assert order.state.is(ACTIVE);

            market.execute(Executed.buy(5).price(9));
            assert order.remainingSize.is(0);
            assert order.executedSize.is(10);
            assert order.state.is(COMPLETED);
        });
    }

    @Test
    void executeOverflow() {
        market.request(Order.buy(10).price(10)).to(order -> {
            assert order.remainingSize.is(10);
            assert order.executedSize.is(0);

            market.execute(Executed.buy(7).price(9));
            assert order.remainingSize.is(3);
            assert order.executedSize.is(7);
            assert order.state.is(ACTIVE);

            market.execute(Executed.buy(7).price(9));
            assert order.remainingSize.is(0);
            assert order.executedSize.is(10);
            assert order.state.is(COMPLETED);
        });
    }

    @Test
    void executeExtra() {
        market.request(Order.buy(10).price(10)).to(order -> {
            assert order.remainingSize.is(10);
            assert order.executedSize.is(0);

            market.execute(Executed.buy(10).price(9));
            assert order.remainingSize.is(0);
            assert order.executedSize.is(10);

            market.execute(Executed.sell(1).price(9));
            assert order.remainingSize.is(0);
            assert order.executedSize.is(10);
        });
    }

    @Test
    void executeLongWithUpperPrice() {
        market.request(Order.buy(10).price(10)).to(order -> {
            market.execute(Executed.buy(5).price(12));
            market.execute(Executed.sell(5).price(13));

            assert order.remainingSize.is(10);
            assert order.executedSize.is(0);
            assert order.state.is(ACTIVE);
        });
    }

    @Test
    void executeLongWithLowerPrice() {
        market.request(Order.buy(10).price(10)).to(order -> {
            market.execute(Executed.buy(5).price(8));
            market.execute(Executed.sell(5).price(7));

            assert order.remainingSize.is(0);
            assert order.executedSize.is(10);
            assert order.state.is(COMPLETED);
        });
    }

    @Test
    void executeShortWithUpperPrice() {
        market.request(Order.sell(10).price(10)).to(order -> {
            market.execute(Executed.buy(5).price(12));
            market.execute(Executed.sell(5).price(13));

            assert order.remainingSize.is(0);
            assert order.executedSize.is(10);
            assert order.state.is(COMPLETED);
        });
    }

    @Test
    void executeShortWithLowerPrice() {
        market.request(Order.sell(10).price(10)).to(order -> {
            market.execute(Executed.buy(5).price(8));
            market.execute(Executed.sell(5).price(7));

            assert order.remainingSize.is(10);
            assert order.executedSize.is(0);
            assert order.state.is(ACTIVE);
        });
    }

    @Test
    void lag() {
        VerifiableMarket market = new VerifiableMarket();
        market.service.lag(5);

        market.request(Order.buy(10).price(10)).to(order -> {
            market.execute(Executed.buy(5).price(7), new TimeLag(3));
            assert order.remainingSize.is(10);
            market.execute(Executed.buy(4).price(7), new TimeLag(4));
            assert order.remainingSize.is(10);
            market.execute(Executed.buy(3).price(7), new TimeLag(5));
            assert order.remainingSize.is(7);
            market.execute(Executed.buy(2).price(7), new TimeLag(6));
            assert order.remainingSize.is(5);
            market.execute(Executed.buy(1).price(7), new TimeLag(7));
            assert order.remainingSize.is(4);
        });
    }

    @Test
    void fillOrKillLong() {
        // success
        market.request(Order.buy(10).price(10).type(QuantityCondition.FillOrKill)).to(order -> {
            market.execute(Executed.buy(10).price(9));
            assert order.isCompleted();
        });

        // over price will success
        market.request(Order.buy(10).price(10).type(QuantityCondition.FillOrKill)).to(order -> {
            market.execute(Executed.buy(10).price(5));
            assert order.isCompleted();
        });

        // over size will success
        market.request(Order.buy(10).price(10).type(QuantityCondition.FillOrKill)).to(order -> {
            market.execute(Executed.buy(15).price(9));
            assert order.isCompleted();
        });

        // less size will be failed
        market.request(Order.buy(10).price(10).type(QuantityCondition.FillOrKill)).to(order -> {
            market.execute(Executed.buy(4).price(5));
            assert order.isNotCompleted();
        });

        // less price will be failed
        market.request(Order.buy(10).price(10).type(QuantityCondition.FillOrKill)).to(order -> {
            market.execute(Executed.buy(10).price(11));
            assert order.isNotCompleted();
        });
    }

    @Test
    void fillOrKillShort() {
        // success
        market.request(Order.sell(1).price(10).type(QuantityCondition.FillOrKill)).to(order -> {
            market.execute(Executed.sell(1).price(11));
            assert order.isCompleted();
        });

        // over price will success
        market.request(Order.sell(1).price(10).type(QuantityCondition.FillOrKill)).to(order -> {
            market.execute(Executed.sell(1).price(11));
            assert order.isCompleted();
        });

        // over size will success
        market.request(Order.sell(10).price(10).type(QuantityCondition.FillOrKill)).to(order -> {
            market.execute(Executed.sell(15).price(11));
            assert order.isCompleted();
        });

        // less size will be failed
        market.request(Order.sell(10).price(10).type(QuantityCondition.FillOrKill)).to(order -> {
            market.execute(Executed.sell(4).price(11));
            assert order.isNotCompleted();
        });

        // less price will be failed
        market.request(Order.sell(10).price(10).type(QuantityCondition.FillOrKill)).to(order -> {
            market.execute(Executed.sell(10).price(9));
            assert order.isNotCompleted();
        });
    }

    @Test
    @Disabled
    void immediateOrCancelLong() {
        // success
        market.request(Order.buy(1).price(10).type(QuantityCondition.ImmediateOrCancel)).to(order -> {
            market.execute(Executed.buy(1).price(9));
            assert order.isCompleted();
            assert order.isNotCanceled();
        });

        // over price will success
        market.request(Order.buy(1).price(10).type(QuantityCondition.ImmediateOrCancel)).to(order -> {
            market.execute(Executed.buy(1).price(9));
            assert order.isCompleted();
            assert order.isNotCanceled();
        });

        // over size will success
        market.request(Order.buy(1).price(10).type(QuantityCondition.ImmediateOrCancel)).to(order -> {
            market.execute(Executed.buy(5).price(9));
            assert order.isCompleted();
            assert order.isNotCanceled();
        });

        // less size will success
        market.request(Order.buy(10).price(10).type(QuantityCondition.ImmediateOrCancel)).to(order -> {
            market.execute(Executed.buy(4).price(9));
            assert order.isCompleted();
            assert order.isNotCanceled();
        });

        // less price will be failed
        market.request(Order.buy(1).price(10).type(QuantityCondition.ImmediateOrCancel)).to(order -> {
            market.execute(Executed.buy(1).price(11));
            assert order.isNotCompleted();
            assert order.isCanceled();
        });
    }

    @Test
    @Disabled
    void immediateOrCancelShort() {
        // success
        market.request(Order.sell(1).price(10).type(QuantityCondition.ImmediateOrCancel)).to();
        market.execute(Executed.sell(1).price(10));
        assert market.validateOrderState(0, 1, 0, 0, 0);

        // large price will success
        market.request(Order.sell(1).price(10).type(QuantityCondition.ImmediateOrCancel)).to();
        market.execute(Executed.sell(1).price(11));
        assert market.validateOrderState(0, 2, 0, 0, 0);

        // large size will success
        market.request(Order.sell(10).price(10).type(QuantityCondition.ImmediateOrCancel)).to();
        market.execute(Executed.sell(15).price(10));
        assert market.validateOrderState(0, 3, 0, 0, 0);

        // less size will success
        market.request(Order.sell(10).price(10).type(QuantityCondition.ImmediateOrCancel)).to();
        market.execute(Executed.sell(4).price(10));
        assert market.validateOrderState(0, 4, 0, 0, 0);
        assert market.orders().get(3).executedSize.is(4);

        // less price will be failed
        market.request(Order.sell(1).price(10).type(QuantityCondition.ImmediateOrCancel)).to();
        market.execute(Executed.sell(1).price(9));
        assert market.validateOrderState(0, 4, 0, 0, 0);
    }

    @Test
    void marketLong() {
        market.request(Order.buy(1)).to();
        market.execute(Executed.sell(1).price(10));
        assert market.orders().get(0).price.v.is(10);
        assert market.orders().get(0).executedSize.is(1);

        // divide
        market.request(Order.buy(10)).to();
        market.execute(Executed.buy(5).price(10));
        market.execute(Executed.buy(5).price(20));
        assert market.orders().get(1).price.v.is(15);
        assert market.orders().get(1).executedSize.is(10);

        // divide overflow
        market.request(Order.buy(10)).to();
        market.execute(Executed.buy(5).price(10));
        market.execute(Executed.buy(14).price(20));
        assert market.orders().get(2).price.v.is(15);
        assert market.orders().get(2).executedSize.is(10);

        // divide underflow
        market.request(Order.buy(10)).to();
        market.execute(Executed.buy(5).price(10));
        market.execute(Executed.buy(3).price(20));
        assert market.orders().get(3).price.v.is("13.75");
        assert market.orders().get(3).executedSize.is(8);
        market.execute(Executed.buy(2).price(20));
        assert market.orders().get(3).price.v.is("15");

        // down price
        market.request(Order.buy(10)).to();
        market.execute(Executed.buy(5).price(10));
        market.execute(Executed.buy(5).price(5));
        assert market.orders().get(4).price.v.is("10");
        assert market.orders().get(4).executedSize.is(10);

        // up price
        market.request(Order.buy(10)).to();
        market.execute(Executed.buy(5).price(10));
        market.execute(Executed.buy(5).price(20));
        assert market.orders().get(5).price.v.is("15");
        assert market.orders().get(5).executedSize.is(10);
    }

    @Test
    void marketShort() {
        Order order = Order.sell(1);
        market.request(order).to();
        market.execute(Executed.sell(1).price(10));
        assert order.price.v.is(10);
        assert order.executedSize.is(1);

        // divide
        order = Order.sell(10);
        market.request(order).to();
        market.execute(Executed.buy(5).price(10));
        market.execute(Executed.buy(5).price(5));
        assert order.price.v.is("7.5");
        assert order.executedSize.is(10);

        // divide overflow
        order = Order.sell(10);
        market.request(order).to();
        market.execute(Executed.buy(5).price(10));
        market.execute(Executed.buy(14).price(5));
        assert order.price.v.is("7.5");
        assert order.executedSize.is(10);

        // divide underflow
        order = Order.sell(10);
        market.request(order).to();
        market.execute(Executed.buy(5).price(20));
        market.execute(Executed.buy(3).price(15));
        assert order.price.v.is("18.125");
        assert order.executedSize.is(8);
        market.execute(Executed.buy(2).price(10));
        assert order.price.v.is("16.5");

        // down price
        order = Order.sell(10);
        market.request(order).to();
        market.execute(Executed.buy(5).price(10));
        market.execute(Executed.buy(5).price(5));
        assert order.price.v.is("7.5");
        assert order.executedSize.is(10);

        // up price
        order = Order.sell(10);
        market.request(order).to();
        market.execute(Executed.buy(5).price(10));
        market.execute(Executed.buy(5).price(20));
        assert order.price.v.is("10");
        assert order.executedSize.is(10);
    }

    @Test
    void cancel() {
        Order order = market.request(Order.sell(1).price(12)).to().v;
        market.execute(Executed.buy(1).price(11));
        assert market.validateOrderState(1, 0, 0, 0, 0);
        market.cancel(order).to();
        assert market.validateOrderState(0, 0, 1, 0, 0);
        market.execute(Executed.buy(1).price(12));
        assert market.validateOrderState(0, 0, 1, 0, 0);
    }

    @Test
    void observeSequencialExecutionsBySellSize() {
        AtomicReference<Num> size = new AtomicReference<>();

        market.timelineByTaker.to(e -> {
            size.set(e.cumulativeSize);
        });

        market.executeSequencially(4, Executed.sell(5).price(10));
        market.execute(Executed.sell(5).price(10));
        assert size.get().is(20);
    }

    @Test
    void observeSequencialExecutionsByBuySize() {
        AtomicReference<Num> size = new AtomicReference<>();

        market.timelineByTaker.to(e -> {
            size.set(e.cumulativeSize);
        });

        market.executeSequencially(4, Executed.buy(5).price(10));
        market.execute(Executed.buy(5).price(10));
        assert size.get().is(20);
    }
}
