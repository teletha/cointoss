/*
 * Copyright (C) 2019 CoinToss Development Team
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          https://opensource.org/licenses/MIT
 */
package cointoss.trader;

import static cointoss.Direction.BUY;
import static java.time.temporal.ChronoUnit.SECONDS;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import cointoss.Direction;
import cointoss.execution.Execution;
import cointoss.util.Num;
import kiss.Variable;

class TraderTest extends TraderTestSupport {

    @Test
    void entryMake() {
        when(now(), v -> {
            return new Entry(Direction.BUY) {

                @Override
                protected void order() {
                    order(1, s -> s.make(10));
                }
            };
        });

        Entry entry = latest();
        assert entry != null;
        assert entry.isBuy();
        assert entry.entrySize.is(1);
        assert entry.entryExecutedSize.is(0);
        assert entry.entryPrice.is(0);

        // execute entry order
        market.perform(Execution.with.buy(1).price(9));
        assert entry.entrySize.is(1);
        assert entry.entryExecutedSize.is(1);
        assert entry.entryPrice.is(10);
    }

    @Test
    void exitMakeAtPrice() {
        when(now(), v -> {
            return new Entry(Direction.BUY) {

                @Override
                protected void order() {
                    order(1, s -> s.make(10));
                }

                @Override
                protected void exit() {
                    exitWhen(now(), s -> s.make(20));
                }
            };
        });

        Entry entry = latest();

        // execute entry order
        market.perform(Execution.with.buy(1).price(9));
        assert entry.entrySize.is(1);
        assert entry.entryExecutedSize.is(1);
        assert entry.entryPrice.is(10);

        // exit order
        assert entry.exitSize.is(1);
        assert entry.exitExecutedSize.is(0);
        assert entry.exitPrice.is(0);

        // don't execute exit order
        market.perform(Execution.with.buy(1).price(15));
        assert entry.exitSize.is(1);
        assert entry.exitExecutedSize.is(0);
        assert entry.exitPrice.is(0);

        // execute exit order
        market.perform(Execution.with.buy(1).price(21));
        assert entry.exitSize.is(1);
        assert entry.exitExecutedSize.is(1);
        assert entry.exitPrice.is(20);

        // check profit
        assert entry.realizedProfit.is(10);
    }

    @Test
    void profit() {
        when(now(), v -> {
            return new Entry(Direction.BUY) {

                @Override
                protected void order() {
                    order(1, s -> s.make(10));
                }
            };
        });

        Entry e = latest();
        assert e.realizedProfit.is(0);

        // execute entry order
        market.perform(Execution.with.buy(1).price(9));
        assert e.realizedProfit.is(-1);
    }

    @Test
    @Disabled
    void entryLimitInvalidParameters() {
        // null side
        Entry entry = entryLimit(null, Num.ONE, Num.ONE, null);
        assert entry == null;

        // null size
        entry = entryLimit(Direction.BUY, null, Num.ONE, null);
        assert entry == null;

        // zero size
        entry = entryLimit(Direction.BUY, Num.ZERO, Num.ONE, null);
        assert entry == null;

        // negative size
        entry = entryLimit(Direction.BUY, Num.of(-1), Num.ONE, null);
        assert entry == null;

        // null price
        entry = entryLimit(Direction.BUY, Num.ONE, null, null);
        assert entry == null;

        // zero price
        entry = entryLimit(Direction.BUY, Num.ONE, Num.ZERO, null);
        assert entry == null;

        // negative price
        entry = entryLimit(Direction.BUY, Num.ONE, Num.of(-1), null);
        assert entry == null;
    }

    @Test
    @Disabled
    void entryMarket() {
        // try entry
        Entry entry = entryMarket(Direction.BUY, Num.ONE, null);
        assert entry.remaining().is(0);

        // execute
        market.perform(Execution.with.buy(1).price(10));
        assert entry.remaining().is(1);
    }

    @Test
    @Disabled
    void entryMarketInvalidParameters() {
        // null side
        Entry entry = entryMarket(null, Num.ONE, null);
        assert entry == null;

        // null size
        entry = entryMarket(Direction.BUY, null, null);
        assert entry == null;

        // zero size
        entry = entryMarket(Direction.BUY, Num.ZERO, null);
        assert entry == null;

        // negative size
        entry = entryMarket(Direction.BUY, Num.of(-1), null);
        assert entry == null;
    }

    @Test
    @Disabled
    void exitLimit() {
        // entry and execute
        Entry entry = entryLimit(Direction.BUY, Num.ONE, Num.TEN, null);
        market.perform(Execution.with.buy(1).price(9));
        assert entry.remaining().is(1);

        // try exit
        entry.exitLimit(Num.ONE, Num.TEN, null);
        assert entry.remaining().is(1);

        market.perform(Execution.with.buy(1).price(11));
        assert entry.remaining().is(0);
    }

    @Test
    @Disabled
    void exitLimitInvalidParameters() {
        // entry and execute
        Entry entry = entryLimit(Direction.BUY, Num.ONE, Num.TEN, null);
        market.perform(Execution.with.buy(1).price(9));
        assert entry.remaining().is(1);

        // null size
        entry.exitLimit(null, Num.ONE, null);
        assert entry.remaining().is(1);

        // zero size
        entry.exitLimit(Num.ZERO, Num.ONE, null);
        assert entry.remaining().is(1);

        // negative size
        entry.exitLimit(Num.of(-1), Num.ONE, null);
        assert entry.remaining().is(1);

        // null price
        entry.exitLimit(Num.ONE, null, null);
        assert entry.remaining().is(1);

        // zero price
        entry.exitLimit(Num.ONE, Num.ZERO, null);
        assert entry.remaining().is(1);

        // negative price
        entry.exitLimit(Num.ONE, Num.of(-1), null);
        assert entry.remaining().is(1);
    }

    @Test
    @Disabled
    void exitMarket() {
        // entry and execute
        Entry entry = entryLimit(Direction.BUY, Num.ONE, Num.TEN, null);
        market.perform(Execution.with.buy(1).price(9));
        assert entry.remaining().is(1);

        // try exit
        entry.exitMarket(Num.ONE);
        assert entry.remaining().is(1);

        market.perform(Execution.with.buy(1).price(11));
        assert entry.remaining().is(0);
    }

    @Test
    @Disabled
    void exitMarketInvalidPrameters() {
        // entry and execute
        Entry entry = entryLimit(Direction.BUY, Num.ONE, Num.TEN, null);
        market.perform(Execution.with.buy(1).price(9));
        assert entry.remaining().is(1);

        // null size
        entry.exitMarket((Num) null);
        assert entry.remaining().is(1);

        // zero size
        entry.exitMarket(Num.ZERO);
        assert entry.remaining().is(1);

        // negative size
        entry.exitMarket(Num.of(-1));
        assert entry.remaining().is(1);
    }

    @Test
    @Disabled
    void completingEntry() {
        Variable<Boolean> completed = completingEntry.to();
        assert completed.isAbsent();

        // entry
        entryLimit(Direction.BUY, Num.ONE, Num.TEN, null);
        assert completed.isAbsent();

        // execute
        market.perform(Execution.with.buy(1).price(9));
        assert completed.is(true);
    }

    @Test
    @Disabled
    void completingExit() {
        Variable<Boolean> completed = completingExit.to();
        assert completed.isAbsent();

        // entry
        Entry entry = entryLimit(Direction.BUY, Num.ONE, Num.TEN, null);
        market.perform(Execution.with.buy(1).price(9));
        assert completed.isAbsent();

        // exit
        entry.exitLimit(Num.ONE, Num.TEN, null);
        assert completed.isAbsent();

        // execute
        market.perform(Execution.with.buy(1).price(11));
        assert completed.is(true);
    }

    @Test
    @Disabled
    void closingPosition() {
        Variable<Boolean> completed = closingPosition.to();
        assert completed.isAbsent();

        // entry
        Entry entry = entryLimit(Direction.BUY, Num.ONE, Num.TEN, null);
        market.perform(Execution.with.buy(1).price(9));
        assert completed.isAbsent();

        // exit
        entry.exitLimit(Num.ONE, Num.TEN, null);
        assert completed.isAbsent();

        // execute
        market.perform(Execution.with.buy(1).price(11));
        assert completed.is(true);
    }

    @Test
    void keep() {
        Variable<Execution> state = market.timeline.take(keep(5, SECONDS, e -> e.price.isLessThan(10))).to();
        assert state.isAbsent();

        // keep more than 10
        market.perform(Execution.with.buy(1).price(15), 2);
        market.perform(Execution.with.buy(1).price(15), 2);
        market.perform(Execution.with.buy(1).price(15), 2);
        assert state.isAbsent();

        // keep less than 10 during 3 seconds
        market.perform(Execution.with.buy(1).price(9), 1);
        market.perform(Execution.with.buy(1).price(9), 1);
        market.perform(Execution.with.buy(1).price(15), 3);
        market.perform(Execution.with.buy(1).price(15), 3);
        assert state.isAbsent();

        // keep less than 10 during 5 seconds
        market.perform(Execution.with.buy(1).price(9), 3);
        market.perform(Execution.with.buy(1).price(9), 3);
        market.perform(Execution.with.buy(1).price(9), 3);
        assert state.isPresent();
    }

    @Test
    @Disabled
    void testHasPosition() {
        assert hasPosition() == false;
        Exit exiter = entry(BUY, 1, 10);
        assert hasPosition() == true;
        exiter.exit(1, 10);
        assert hasPosition() == false;
    }

    @Test
    @Disabled
    void isWinAndLose() {
        entry(BUY, 1, 5).exit(1, 10);
        assert latest().isWin() == true;
        assert latest().isLose() == false;

        entry(BUY, 1, 5).exit(1, 3);
        assert latest().isWin() == false;
        assert latest().isLose() == true;

        entry(BUY, 1, 5).exit(1, 5);
        assert latest().isWin() == false;
        assert latest().isLose() == false;
    }
}
