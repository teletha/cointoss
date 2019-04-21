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

import static cointoss.Direction.*;
import static java.time.temporal.ChronoUnit.*;

import org.junit.jupiter.api.Test;

import cointoss.Direction;
import cointoss.execution.Execution;
import cointoss.util.Num;
import kiss.Variable;

/**
 * @version 2018/04/02 16:48:42
 */
class TraderTest extends TraderTestSupport {

    @Test
    void entryLimit() {
        assert hasPosition() == false;

        // try entry
        Entry entry = entryLimit(Direction.BUY, Num.ONE, Num.TEN, null);
        assert entry.remaining().is(0);

        // execute
        market.execute(1, 10);
        entry.remaining().is(1);
    }

    @Test
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
    void entryMarket() {
        // try entry
        Entry entry = entryMarket(Direction.BUY, Num.ONE, null);
        assert entry.remaining().is(0);

        // execute
        market.execute(1, 10);
        assert entry.remaining().is(1);
    }

    @Test
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
    void exitLimit() {
        // entry and execute
        Entry entry = entryLimit(Direction.BUY, Num.ONE, Num.TEN, null);
        market.execute(1, 9);
        assert entry.remaining().is(1);

        // try exit
        entry.exitLimit(Num.ONE, Num.TEN, null);
        assert entry.remaining().is(1);

        market.execute(1, 11);
        assert entry.remaining().is(0);
    }

    @Test
    void exitLimitInvalidParameters() {
        // entry and execute
        Entry entry = entryLimit(Direction.BUY, Num.ONE, Num.TEN, null);
        market.execute(1, 9);
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
    void exitMarket() {
        // entry and execute
        Entry entry = entryLimit(Direction.BUY, Num.ONE, Num.TEN, null);
        market.execute(1, 9);
        assert entry.remaining().is(1);

        // try exit
        entry.exitMarket(Num.ONE);
        assert entry.remaining().is(1);

        market.execute(1, 11);
        assert entry.remaining().is(0);
    }

    @Test
    void exitMarketInvalidPrameters() {
        // entry and execute
        Entry entry = entryLimit(Direction.BUY, Num.ONE, Num.TEN, null);
        market.execute(1, 9);
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
    void completingEntry() {
        Variable<Boolean> completed = completingEntry.to();
        assert completed.isAbsent();

        // entry
        entryLimit(Direction.BUY, Num.ONE, Num.TEN, null);
        assert completed.isAbsent();

        // execute
        market.execute(1, 9);
        assert completed.is(true);
    }

    @Test
    void completingExit() {
        Variable<Boolean> completed = completingExit.to();
        assert completed.isAbsent();

        // entry
        Entry entry = entryLimit(Direction.BUY, Num.ONE, Num.TEN, null);
        market.execute(1, 9);
        assert completed.isAbsent();

        // exit
        entry.exitLimit(Num.ONE, Num.TEN, null);
        assert completed.isAbsent();

        // execute
        market.execute(1, 11);
        assert completed.is(true);
    }

    @Test
    void closingPosition() {
        Variable<Boolean> completed = closingPosition.to();
        assert completed.isAbsent();

        // entry
        Entry entry = entryLimit(Direction.BUY, Num.ONE, Num.TEN, null);
        market.execute(1, 9);
        assert completed.isAbsent();

        // exit
        entry.exitLimit(Num.ONE, Num.TEN, null);
        assert completed.isAbsent();

        // execute
        market.execute(1, 11);
        assert completed.is(true);
    }

    @Test
    void keep() {
        Variable<Execution> state = market.timeline.take(keep(5, SECONDS, e -> e.price.isLessThan(10))).to();
        assert state.isAbsent();

        // keep more than 10
        market.execute(1, 15, 1);
        market.execute(1, 15, 4);
        market.execute(1, 15, 8);
        assert state.isAbsent();

        // keep less than 10 during 3 seconds
        market.execute(1, 9, 10);
        market.execute(1, 9, 12);
        market.execute(1, 15, 13);
        market.execute(1, 15, 18);
        assert state.isAbsent();

        // keep less than 10 during 5 seconds
        market.execute(1, 9, 10);
        market.execute(1, 9, 12);
        market.execute(1, 9, 15);
        assert state.isPresent();
    }

    @Test
    void testHasPosition() {
        assert hasPosition() == false;
        Exit exiter = entry(BUY, 1, 10);
        assert hasPosition() == true;
        exiter.exit(1, 10);
        assert hasPosition() == false;
    }

    @Test
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
