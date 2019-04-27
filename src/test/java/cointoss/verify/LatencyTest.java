/*
 * Copyright (C) 2019 Nameless Production Committee
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          http://opensource.org/licenses/mit-license.php
 */
package cointoss.verify;

import static java.time.temporal.ChronoUnit.SECONDS;

import org.junit.jupiter.api.Test;

import cointoss.execution.Executing;
import cointoss.order.Order;

class LatencyTest {

    @Test
    void request() {
        VerifiableMarket market = new VerifiableMarket();
        market.service.latency = Latency.fixed(3, SECONDS);

        market.request(Order.buy(1).price(10)).to(order -> {
            // after 0 sec, order isn't accepted
            market.perform(Executing.buy(1).price(9));
            assert order.isNotCompleted();

            // after 1 sec, order isn't accepted
            market.perform(Executing.buy(1).price(9), 1);
            assert order.isNotCompleted();

            // after 2 sec, order isn't accepted
            market.perform(Executing.buy(1).price(9), 1);
            assert order.isNotCompleted();

            // after 3 sec, order is accepted
            market.perform(Executing.buy(1).price(9), 1);
            assert order.isCompleted();
        });
    }

    @Test
    void cancel() {
        VerifiableMarket market = new VerifiableMarket();
        market.service.latency = Latency.fixed(3, SECONDS);

        market.request(Order.buy(1).price(10)).to(order -> {
            // after 0 sec, order isn't accepted
            market.perform(Executing.buy(1).price(11));
            assert order.isNotCanceled();

            // after 3 sec, order is accepted
            market.perform(Executing.buy(1).price(11), 3);
            assert order.isNotCanceled();

            market.cancel(order).to(() -> {
                // after 0 sec, order isn't canceled
                market.perform(Executing.buy(1).price(11));
                assert order.isNotCanceled();

                // after 1 sec, order isn't canceled
                market.perform(Executing.buy(1).price(11), 1);
                assert order.isNotCanceled();
                System.out.println(order.isNotCanceled());

                // after 3 sec, order is accepted
                market.perform(Executing.buy(1).price(11), 2);
                assert order.isCanceled();
            });
        });
    }
}
