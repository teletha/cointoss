/*
 * Copyright (C) 2017 Nameless Production Committee
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          http://opensource.org/licenses/mit-license.php
 */
package cointoss;

import static cointoss.Side.*;
import static java.time.temporal.ChronoUnit.*;

import org.junit.Test;

import cointoss.util.Num;
import kiss.Variable;

/**
 * @version 2017/09/11 13:24:46
 */
public class TradingTest extends TradingTestSupport {

    @Test
    public void entryLimit() throws Exception {
        assert hasPosition() == false;

        // try entry
        Entry entry = entryLimit(Side.BUY, Num.ONE, Num.TEN, null);
        assert entry.remaining().is(0);

        // execute
        market.execute(1, 10);
        entry.remaining().is(1);
    }

    @Test
    public void entryLimitInvalidParameters() throws Exception {
        // null side
        Entry entry = entryLimit(null, Num.ONE, Num.ONE, null);
        assert entry == null;

        // null size
        entry = entryLimit(Side.BUY, null, Num.ONE, null);
        assert entry == null;

        // zero size
        entry = entryLimit(Side.BUY, Num.ZERO, Num.ONE, null);
        assert entry == null;

        // negative size
        entry = entryLimit(Side.BUY, Num.of(-1), Num.ONE, null);
        assert entry == null;

        // null price
        entry = entryLimit(Side.BUY, Num.ONE, null, null);
        assert entry == null;

        // zero price
        entry = entryLimit(Side.BUY, Num.ONE, Num.ZERO, null);
        assert entry == null;

        // negative price
        entry = entryLimit(Side.BUY, Num.ONE, Num.of(-1), null);
        assert entry == null;
    }

    @Test
    public void entryMarket() throws Exception {
        // try entry
        Entry entry = entryMarket(Side.BUY, Num.ONE, null);
        assert entry.remaining().is(0);

        // execute
        market.execute(1, 10);
        assert entry.remaining().is(1);
    }

    @Test
    public void entryMarketInvalidParameters() throws Exception {
        // null side
        Entry entry = entryMarket(null, Num.ONE, null);
        assert entry == null;

        // null size
        entry = entryMarket(Side.BUY, null, null);
        assert entry == null;

        // zero size
        entry = entryMarket(Side.BUY, Num.ZERO, null);
        assert entry == null;

        // negative size
        entry = entryMarket(Side.BUY, Num.of(-1), null);
        assert entry == null;
    }

    @Test
    public void exitLimit() throws Exception {
        // entry and execute
        Entry entry = entryLimit(Side.BUY, Num.ONE, Num.TEN, null);
        market.execute(1, 10);
        assert entry.remaining().is(1);

        // try exit
        entry.exitLimit(Num.ONE, Num.TEN, null);
        assert entry.remaining().is(1);

        market.execute(1, 10);
        assert entry.remaining().is(0);
    }

    @Test
    public void exitLimitInvalidParameters() throws Exception {
        // entry and execute
        Entry entry = entryLimit(Side.BUY, Num.ONE, Num.TEN, null);
        market.execute(1, 10);
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
    public void exitMarket() throws Exception {
        // entry and execute
        Entry entry = entryLimit(Side.BUY, Num.ONE, Num.TEN, null);
        market.execute(1, 10);
        assert entry.remaining().is(1);

        // try exit
        entry.exitMarket(Num.ONE);
        assert entry.remaining().is(1);

        market.execute(1, 10);
        assert entry.remaining().is(0);
    }

    @Test
    public void exitMarketInvalidPrameters() throws Exception {
        // entry and execute
        Entry entry = entryLimit(Side.BUY, Num.ONE, Num.TEN, null);
        market.execute(1, 10);
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
    public void completingEntry() throws Exception {
        Variable<Boolean> completed = completingEntry.to();
        assert completed.isAbsent();

        // entry
        entryLimit(Side.BUY, Num.ONE, Num.TEN, null);
        assert completed.isAbsent();

        // execute
        market.execute(1, 10);
        assert completed.is(true);
    }

    @Test
    public void completingExit() throws Exception {
        Variable<Boolean> completed = completingExit.to();
        assert completed.isAbsent();

        // entry
        Entry entry = entryLimit(Side.BUY, Num.ONE, Num.TEN, null);
        market.execute(1, 10);
        assert completed.isAbsent();

        // exit
        entry.exitLimit(Num.ONE, Num.TEN, null);
        assert completed.isAbsent();

        // execute
        market.execute(1, 10);
        assert completed.is(true);
    }

    @Test
    public void closingPosition() throws Exception {
        Variable<Boolean> completed = closingPosition.to();
        assert completed.isAbsent();

        // entry
        Entry entry = entryLimit(Side.BUY, Num.ONE, Num.TEN, null);
        market.execute(1, 10);
        assert completed.isAbsent();

        // exit
        entry.exitLimit(Num.ONE, Num.TEN, null);
        assert completed.isAbsent();

        // execute
        market.execute(1, 10);
        assert completed.is(true);
    }

    @Test
    public void keep() throws Exception {
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
    public void testHasPosition() throws Exception {
        assert hasPosition() == false;
        Exit exiter = entry(BUY, 1, 10);
        assert hasPosition() == true;
        exiter.exit(1, 10);
        assert hasPosition() == false;
    }

    @Test
    public void isWinAndLose() throws Exception {
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
