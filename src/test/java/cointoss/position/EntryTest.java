/*
 * Copyright (C) 2019 Nameless Production Committee
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          http://opensource.org/licenses/mit-license.php
 */
package cointoss.position;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import cointoss.execution.Executing;
import cointoss.order.Order;
import cointoss.verify.VerifiableMarket;

class EntryTest {

    VerifiableMarket m;

    @BeforeEach
    void setup() {
        m = new VerifiableMarket();
    }

    @Test
    void entrySize() {
        m.orders.requestEntry(Order.buy(10).price(10)).to(e -> {
            // execute partially
            m.perform(Executing.buy(1).price(9));
            assert e.entrySize.is(10);
            assert e.entryRemainingSize.is(9);
            assert e.entryExecutedSize.is(1);

            // execute additionally
            m.perform(Executing.buy(2).price(9));
            assert e.entrySize.is(10);
            assert e.entryRemainingSize.is(7);
            assert e.entryExecutedSize.is(3);

            // execute completely
            m.perform(Executing.buy(10).price(9));
            assert e.entrySize.is(10);
            assert e.entryRemainingSize.is(0);
            assert e.entryExecutedSize.is(10);

            // no effect
            m.perform(Executing.buy(3).price(9));
            assert e.entrySize.is(10);
            assert e.entryRemainingSize.is(0);
            assert e.entryExecutedSize.is(10);
        });
    }

    @Test
    void cancelInitialEntry() {
        m.orders.requestEntry(Order.buy(10).price(10)).to(e -> {
            // cancel
            e.cancelEntry();
            assert e.entrySize.is(0);
            assert e.entryRemainingSize.is(0);
            assert e.entryExecutedSize.is(0);

            // no effect
            m.perform(Executing.buy(3).price(9));
            assert e.entrySize.is(0);
            assert e.entryRemainingSize.is(0);
            assert e.entryExecutedSize.is(0);
        });
    }

    @Test
    void cancelPartialEntry() {
        m.orders.requestEntry(Order.buy(10).price(10)).to(e -> {
            // execute partially
            m.perform(Executing.buy(3).price(9));
            assert e.entrySize.is(10);
            assert e.entryRemainingSize.is(7);
            assert e.entryExecutedSize.is(3);

            // cancel
            e.cancelEntry();
            assert e.entrySize.is(3);
            assert e.entryRemainingSize.is(0);
            assert e.entryExecutedSize.is(3);

            // no effect
            m.perform(Executing.buy(3).price(9));
            assert e.entrySize.is(3);
            assert e.entryRemainingSize.is(0);
            assert e.entryExecutedSize.is(3);
        });
    }

    @Test
    void cancelCompleteEntry() {
        m.orders.requestEntry(Order.buy(10).price(10)).to(e -> {
            // execute partially
            m.perform(Executing.buy(10).price(9));
            assert e.entrySize.is(10);
            assert e.entryRemainingSize.is(0);
            assert e.entryExecutedSize.is(10);

            // cancel
            e.cancelEntry();
            assert e.entrySize.is(10);
            assert e.entryRemainingSize.is(0);
            assert e.entryExecutedSize.is(10);

            // no effect
            m.perform(Executing.buy(3).price(9));
            assert e.entrySize.is(10);
            assert e.entryRemainingSize.is(0);
            assert e.entryExecutedSize.is(10);
        });
    }

    @Test
    void positionSize() {
        m.orders.requestEntry(Order.buy(10).price(10)).to(e -> {
            // execute entry partially
            m.perform(Executing.buy(1).price(9));
            assert e.positionSize.is(1);

            // execute entry additionally
            m.perform(Executing.buy(2).price(9));
            assert e.positionSize.is(3);

            // execute entry completely
            m.perform(Executing.buy(10).price(9));
            assert e.positionSize.is(10);

            // no effect
            m.perform(Executing.buy(3).price(9));
            assert e.positionSize.is(10);

            e.requestExit(Order.sell(10).price(12)).to(() -> {
                // execute exit partially
                m.perform(Executing.sell(1).price(13));
                assert e.positionSize.is(9);

                // execute exit additionally
                m.perform(Executing.buy(2).price(13));
                assert e.positionSize.is(7);

                // execute exit completely
                m.perform(Executing.buy(10).price(13));
                assert e.positionSize.is(0);

                // no effect
                m.perform(Executing.buy(3).price(13));
                assert e.positionSize.is(0);
            });
        });
    }
}
