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

import cointoss.execution.Execution;
import cointoss.order.Order;
import cointoss.verify.VerifiableMarket;

class EntryTest {

    VerifiableMarket market;

    @BeforeEach
    void setup() {
        market = new VerifiableMarket();
    }

    @Test
    void entrySize() {
        market.orders.requestEntry(Order.buy(10).price(10)).to(e -> {
            // execute partially
            market.perform(Execution.with.buy(1).price(9));
            assert e.entrySize.is(10);
            assert e.entryRemainingSize.is(9);
            assert e.entryExecutedSize.is(1);

            // execute additionally
            market.perform(Execution.with.buy(2).price(9));
            assert e.entrySize.is(10);
            assert e.entryRemainingSize.is(7);
            assert e.entryExecutedSize.is(3);

            // execute completely
            market.perform(Execution.with.buy(10).price(9));
            assert e.entrySize.is(10);
            assert e.entryRemainingSize.is(0);
            assert e.entryExecutedSize.is(10);

            // no effect
            market.perform(Execution.with.buy(3).price(9));
            assert e.entrySize.is(10);
            assert e.entryRemainingSize.is(0);
            assert e.entryExecutedSize.is(10);
        });
    }

    @Test
    void entryPrice() {
        market.orders.requestEntry(Order.buy(10).price(10)).to(e -> {
            // execute partially
            market.perform(Execution.with.buy(1).price(9));
            assert e.entryPice.is(10);

            // execute additionally
            market.perform(Execution.with.buy(2).price(9));
            assert e.entryPice.is(10);

            // execute completely
            market.perform(Execution.with.buy(10).price(9));
            assert e.entryPice.is(10);

            // no effect
            market.perform(Execution.with.buy(3).price(9));
            assert e.entryPice.is(10);
        });
    }

    @Test
    void multiEntryPrice() {
        market.orders.requestEntry(Order.buy(10).price(10)).to(e -> {
            // execute completely
            market.perform(Execution.with.buy(10).price(9));
            assert e.entryPice.is(10);

            // second entry
            e.requestEntry(Order.buy(10).price(20)).to(() -> {
                // execute completely
                market.perform(Execution.with.buy(10).price(19));
                assert e.entryPice.is(15);
            });
        });
    }

    @Test
    void exitSize() {
        market.performEntry(Order.buy(10).price(10), Order.sell(10).price(12)).to(e -> {
            // execute exit partially
            market.perform(Execution.with.sell(1).price(13));
            assert e.exitSize.is(10);
            assert e.exitRemainingSize.is(9);
            assert e.exitExecutedSize.is(1);

            // execute exit additionally
            market.perform(Execution.with.sell(2).price(13));
            assert e.exitSize.is(10);
            assert e.exitRemainingSize.is(7);
            assert e.exitExecutedSize.is(3);

            // execute exit completely
            market.perform(Execution.with.sell(10).price(13));
            assert e.exitSize.is(10);
            assert e.exitRemainingSize.is(0);
            assert e.exitExecutedSize.is(10);

            // no effect
            market.perform(Execution.with.sell(3).price(13));
            assert e.exitSize.is(10);
            assert e.exitRemainingSize.is(0);
            assert e.exitExecutedSize.is(10);
        });
    }

    @Test
    void cancelInitialEntry() {
        market.orders.requestEntry(Order.buy(10).price(10)).to(e -> {
            // cancel
            e.cancelEntry();
            assert e.entrySize.is(0);
            assert e.entryRemainingSize.is(0);
            assert e.entryExecutedSize.is(0);

            // no effect
            market.perform(Execution.with.buy(3).price(9));
            assert e.entrySize.is(0);
            assert e.entryRemainingSize.is(0);
            assert e.entryExecutedSize.is(0);
        });
    }

    @Test
    void cancelPartialEntry() {
        market.orders.requestEntry(Order.buy(10).price(10)).to(e -> {
            // execute partially
            market.perform(Execution.with.buy(3).price(9));
            assert e.entrySize.is(10);
            assert e.entryRemainingSize.is(7);
            assert e.entryExecutedSize.is(3);

            // cancel
            e.cancelEntry();
            assert e.entrySize.is(3);
            assert e.entryRemainingSize.is(0);
            assert e.entryExecutedSize.is(3);

            // no effect
            market.perform(Execution.with.buy(3).price(9));
            assert e.entrySize.is(3);
            assert e.entryRemainingSize.is(0);
            assert e.entryExecutedSize.is(3);
        });
    }

    @Test
    void cancelCompleteEntry() {
        market.orders.requestEntry(Order.buy(10).price(10)).to(e -> {
            // execute partially
            market.perform(Execution.with.buy(10).price(9));
            assert e.entrySize.is(10);
            assert e.entryRemainingSize.is(0);
            assert e.entryExecutedSize.is(10);

            // cancel
            e.cancelEntry();
            assert e.entrySize.is(10);
            assert e.entryRemainingSize.is(0);
            assert e.entryExecutedSize.is(10);

            // no effect
            market.perform(Execution.with.buy(3).price(9));
            assert e.entrySize.is(10);
            assert e.entryRemainingSize.is(0);
            assert e.entryExecutedSize.is(10);
        });
    }

    @Test
    void positionSize() {
        market.orders.requestEntry(Order.buy(10).price(10)).to(e -> {
            // execute entry partially
            market.perform(Execution.with.buy(1).price(9));
            assert e.positionSize.is(1);

            // execute entry additionally
            market.perform(Execution.with.buy(2).price(9));
            assert e.positionSize.is(3);

            // execute entry completely
            market.perform(Execution.with.buy(10).price(9));
            assert e.positionSize.is(10);

            // no effect
            market.perform(Execution.with.buy(3).price(9));
            assert e.positionSize.is(10);

            e.requestExit(Order.sell(10).price(12)).to(() -> {
                // execute exit partially
                market.perform(Execution.with.sell(1).price(13));
                assert e.positionSize.is(9);

                // execute exit additionally
                market.perform(Execution.with.buy(2).price(13));
                assert e.positionSize.is(7);

                // execute exit completely
                market.perform(Execution.with.buy(10).price(13));
                assert e.positionSize.is(0);

                // no effect
                market.perform(Execution.with.buy(3).price(13));
                assert e.positionSize.is(0);
            });
        });
    }
}
