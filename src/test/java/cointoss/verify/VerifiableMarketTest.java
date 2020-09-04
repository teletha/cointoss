/*
 * Copyright (C) 2020 cointoss Development Team
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          https://opensource.org/licenses/MIT
 */
package cointoss.verify;

import static cointoss.order.OrderState.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import com.google.common.util.concurrent.AtomicDouble;

import cointoss.execution.Execution;
import cointoss.order.Order;
import cointoss.order.QuantityCondition;
import cointoss.util.arithmeric.Num;

class VerifiableMarketTest {

    VerifiableMarket market = new VerifiableMarket();

    @BeforeEach
    void initialize() {
        market.service.clear();
    }

    @Test
    void requestBuyLimitOrder() {
        market.request(Order.with.buy(1).price(1)).to(order -> {
            assert order.isBuy();
            assert order.executedSize.is(0);
            assert order.remainingSize.is(1);
        });
    }

    @Test
    void executeBuy() {
        market.request(Order.with.buy(1).price(10)).to(order -> {
            assert order.remainingSize.is(1);
            assert order.executedSize.is(0);

            market.perform(Execution.with.buy(1).price(10));
            assert order.remainingSize.is(1);
            assert order.executedSize.is(0);

            market.perform(Execution.with.buy(1).price(9));
            assert order.remainingSize.is(0);
            assert order.executedSize.is(1);
        });
    }

    @Test
    void executeBuyMinimumPrice() {
        Num min = market.service.setting.base.minimumSize;

        market.request(Order.with.buy(1).price(min)).to(order -> {
            assert order.remainingSize.is(1);
            assert order.executedSize.is(0);

            market.perform(Execution.with.buy(1).price(min));
            assert order.remainingSize.is(0);
            assert order.executedSize.is(1);
        });
    }

    @Test
    void executeSell() {
        market.request(Order.with.sell(1).price(10)).to(order -> {
            assert order.remainingSize.is(1);
            assert order.executedSize.is(0);

            market.perform(Execution.with.buy(1).price(10));
            assert order.remainingSize.is(1);
            assert order.executedSize.is(0);

            market.perform(Execution.with.buy(1).price(11));
            assert order.remainingSize.is(0);
            assert order.executedSize.is(1);
        });
    }

    @Test
    void executeDivided() {
        market.request(Order.with.buy(10).price(10)).to(order -> {
            assert order.remainingSize.is(10);
            assert order.executedSize.is(0);

            market.perform(Execution.with.buy(5).price(9));
            assert order.remainingSize.is(5);
            assert order.executedSize.is(5);
            assert order.state == ACTIVE;

            market.perform(Execution.with.buy(5).price(9));
            assert order.remainingSize.is(0);
            assert order.executedSize.is(10);
            assert order.state == COMPLETED;
        });
    }

    @Test
    void executeOverflow() {
        market.request(Order.with.buy(10).price(10)).to(order -> {
            assert order.remainingSize.is(10);
            assert order.executedSize.is(0);

            market.perform(Execution.with.buy(7).price(9));
            assert order.remainingSize.is(3);
            assert order.executedSize.is(7);
            assert order.state == ACTIVE;

            market.perform(Execution.with.buy(7).price(9));
            assert order.remainingSize.is(0);
            assert order.executedSize.is(10);
            assert order.state == COMPLETED;
        });
    }

    @Test
    void singleExecutionFillMultipleOders() {
        Order order1 = market.orders.requestNow(Order.with.buy(0.2).price(10));
        Order order2 = market.orders.requestNow(Order.with.buy(0.8).price(10));
        assert order1.executedSize.is(0);
        assert order2.executedSize.is(0);

        market.perform(Execution.with.buy(0.5).price(9));
        assert order1.executedSize.is(0.2);
        assert order2.executedSize.is(0.3);
    }

    @Test
    void executeExtra() {
        market.request(Order.with.buy(10).price(10)).to(order -> {
            assert order.remainingSize.is(10);
            assert order.executedSize.is(0);

            market.perform(Execution.with.buy(10).price(9));
            assert order.remainingSize.is(0);
            assert order.executedSize.is(10);

            market.perform(Execution.with.sell(1).price(9));
            assert order.remainingSize.is(0);
            assert order.executedSize.is(10);
        });
    }

    @Test
    void executeLongWithUpperPrice() {
        market.request(Order.with.buy(10).price(10)).to(order -> {
            market.perform(Execution.with.buy(5).price(12));
            market.perform(Execution.with.sell(5).price(13));

            assert order.remainingSize.is(10);
            assert order.executedSize.is(0);
            assert order.state == ACTIVE;
        });
    }

    @Test
    void executeLongWithLowerPrice() {
        market.request(Order.with.buy(10).price(10)).to(order -> {
            market.perform(Execution.with.buy(5).price(8));
            market.perform(Execution.with.sell(5).price(7));

            assert order.remainingSize.is(0);
            assert order.executedSize.is(10);
            assert order.state == COMPLETED;
        });
    }

    @Test
    void executeShortWithUpperPrice() {
        market.request(Order.with.sell(10).price(10)).to(order -> {
            market.perform(Execution.with.buy(5).price(12));
            market.perform(Execution.with.sell(5).price(13));

            assert order.remainingSize.is(0);
            assert order.executedSize.is(10);
            assert order.state == COMPLETED;
        });
    }

    @Test
    void executeShortWithLowerPrice() {
        market.request(Order.with.sell(10).price(10)).to(order -> {
            market.perform(Execution.with.buy(5).price(8));
            market.perform(Execution.with.sell(5).price(7));

            assert order.remainingSize.is(10);
            assert order.executedSize.is(0);
            assert order.state == ACTIVE;
        });
    }

    @Test
    void fillOrKillLong() {
        // success
        market.request(Order.with.buy(10).price(10).quantityCondition(QuantityCondition.FillOrKill)).to(order -> {
            market.perform(Execution.with.buy(10).price(9));
            assert order.isCompleted();
        });

        // over price will success
        market.request(Order.with.buy(10).price(10).quantityCondition(QuantityCondition.FillOrKill)).to(order -> {
            market.perform(Execution.with.buy(10).price(5));
            assert order.isCompleted();
        });

        // over size will success
        market.request(Order.with.buy(10).price(10).quantityCondition(QuantityCondition.FillOrKill)).to(order -> {
            market.perform(Execution.with.buy(15).price(9));
            assert order.isCompleted();
        });

        // less size will be failed
        market.request(Order.with.buy(10).price(10).quantityCondition(QuantityCondition.FillOrKill)).to(order -> {
            market.perform(Execution.with.buy(4).price(5));
            assert order.isNotCompleted();
        });

        // less price will be failed
        market.request(Order.with.buy(10).price(10).quantityCondition(QuantityCondition.FillOrKill)).to(order -> {
            market.perform(Execution.with.buy(10).price(11));
            assert order.isNotCompleted();
        });
    }

    @Test
    void fillOrKillShort() {
        // success
        market.request(Order.with.sell(1).price(10).quantityCondition(QuantityCondition.FillOrKill)).to(order -> {
            market.perform(Execution.with.sell(1).price(11));
            assert order.isCompleted();
        });

        // over price will success
        market.request(Order.with.sell(1).price(10).quantityCondition(QuantityCondition.FillOrKill)).to(order -> {
            market.perform(Execution.with.sell(1).price(11));
            assert order.isCompleted();
        });

        // over size will success
        market.request(Order.with.sell(10).price(10).quantityCondition(QuantityCondition.FillOrKill)).to(order -> {
            market.perform(Execution.with.sell(15).price(11));
            assert order.isCompleted();
        });

        // less size will be failed
        market.request(Order.with.sell(10).price(10).quantityCondition(QuantityCondition.FillOrKill)).to(order -> {
            market.perform(Execution.with.sell(4).price(11));
            assert order.isNotCompleted();
        });

        // less price will be failed
        market.request(Order.with.sell(10).price(10).quantityCondition(QuantityCondition.FillOrKill)).to(order -> {
            market.perform(Execution.with.sell(10).price(9));
            assert order.isNotCompleted();
        });
    }

    @Test
    @Disabled
    void immediateOrCancelLong() {
        // success
        market.request(Order.with.buy(1).price(10).quantityCondition(QuantityCondition.ImmediateOrCancel)).to(order -> {
            market.perform(Execution.with.buy(1).price(9));
            assert order.isCompleted();
            assert order.isNotCanceled();
        });

        // over price will success
        market.request(Order.with.buy(1).price(10).quantityCondition(QuantityCondition.ImmediateOrCancel)).to(order -> {
            market.perform(Execution.with.buy(1).price(9));
            assert order.isCompleted();
            assert order.isNotCanceled();
        });

        // over size will success
        market.request(Order.with.buy(1).price(10).quantityCondition(QuantityCondition.ImmediateOrCancel)).to(order -> {
            market.perform(Execution.with.buy(5).price(9));
            assert order.isCompleted();
            assert order.isNotCanceled();
        });

        // less size will success
        market.request(Order.with.buy(10).price(10).quantityCondition(QuantityCondition.ImmediateOrCancel)).to(order -> {
            market.perform(Execution.with.buy(4).price(9));
            assert order.isCompleted();
            assert order.isNotCanceled();
        });

        // less price will be failed
        market.request(Order.with.buy(1).price(10).quantityCondition(QuantityCondition.ImmediateOrCancel)).to(order -> {
            market.perform(Execution.with.buy(1).price(11));
            assert order.isNotCompleted();
            assert order.isCanceled();
        });
    }

    @Test
    @Disabled
    void immediateOrCancelShort() {
        // success
        market.request(Order.with.sell(1).price(10).quantityCondition(QuantityCondition.ImmediateOrCancel)).to(order -> {
            market.perform(Execution.with.sell(1).price(11));
            assert order.isCompleted();
            assert order.isNotCanceled();
        });

        // over price will success
        market.request(Order.with.sell(1).price(10).quantityCondition(QuantityCondition.ImmediateOrCancel)).to(order -> {
            market.perform(Execution.with.sell(1).price(12));
            assert order.isCompleted();
            assert order.isNotCanceled();
        });

        // over size will success
        market.request(Order.with.sell(10).price(10).quantityCondition(QuantityCondition.ImmediateOrCancel)).to(order -> {
            market.perform(Execution.with.sell(15).price(11));
            assert order.isCompleted();
            assert order.isNotCanceled();
        });

        // less size will success
        market.request(Order.with.sell(10).price(10).quantityCondition(QuantityCondition.ImmediateOrCancel)).to(order -> {
            market.perform(Execution.with.sell(4).price(11));
            assert order.isNotCompleted();
            assert order.isCanceled();
        });

        // less price will be failed
        market.request(Order.with.sell(1).price(10).quantityCondition(QuantityCondition.ImmediateOrCancel)).to(order -> {
            market.perform(Execution.with.sell(1).price(9));
            assert order.isNotCompleted();
            assert order.isCanceled();
        });
    }

    @Test
    void cancel() {
        market.request(Order.with.sell(1).price(12)).to(order -> {
            market.perform(Execution.with.buy(1).price(11));
            assert order.isNotCanceled();
            assert order.isNotCompleted();

            market.cancel(order).to();
            assert order.isCanceled();
            assert order.isNotCompleted();

            market.perform(Execution.with.buy(1).price(13));
            assert order.isCanceled();
            assert order.isNotCompleted();
        });
    }

    @Test
    void observeSequencialExecutionsBySellSize() {
        AtomicDouble size = new AtomicDouble();

        market.timelineByTaker.to(e -> {
            size.set(e.accumulative);
        });

        market.performSequencially(4, Execution.with.sell(5).price(10));
        market.perform(Execution.with.sell(5).price(10));
        assert size.get() == 20d;
    }

    @Test
    void observeSequencialExecutionsByBuySize() {
        AtomicDouble size = new AtomicDouble();

        market.timelineByTaker.to(e -> {
            size.set(e.accumulative);
        });

        market.performSequencially(4, Execution.with.buy(5).price(10));
        market.perform(Execution.with.buy(5).price(10));
        assert size.get() == 20d;
    }
}