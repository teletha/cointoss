/*
 * Copyright (C) 2021 cointoss Development Team
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          https://opensource.org/licenses/MIT
 */
package cointoss.trade;

import static java.time.temporal.ChronoUnit.SECONDS;

import org.junit.jupiter.api.Test;

import cointoss.Direction;
import cointoss.execution.Execution;
import kiss.I;
import kiss.Variable;

class TraderTest extends TraderTestSupport {

    @Test
    void entryBuy() {
        when(now(), v -> trade(new Scenario() {

            @Override
            protected void entry() {
                entry(Direction.BUY, 1, s -> s.make(10));
            }

            @Override
            protected void exit() {
            }
        }));

        Scenario s = last();
        assert s.isBuy();
        assert s.entrySize.is(1);
        assert s.entryExecutedSize.is(0);
        assert s.entryPrice.is(10);

        // execute entry
        market.perform(Execution.with.buy(0.5).price(9));
        assert s.entrySize.is(1);
        assert s.entryExecutedSize.is(0.5);
        assert s.entryPrice.is(10);

        // execute entry
        market.perform(Execution.with.buy(0.5).price(9));
        assert s.entrySize.is(1);
        assert s.entryExecutedSize.is(1);
        assert s.entryPrice.is(10);
    }

    @Test
    void entrySell() {
        when(now(), v -> trade(new Scenario() {

            @Override
            protected void entry() {
                entry(Direction.SELL, 1, s -> s.make(10));
            }

            @Override
            protected void exit() {
            }
        }));

        Scenario s = last();
        assert s.isSell();
        assert s.entrySize.is(1);
        assert s.entryExecutedSize.is(0);
        assert s.entryPrice.is(10);

        // execute entry
        market.perform(Execution.with.buy(0.5).price(11));
        assert s.entrySize.is(1);
        assert s.entryExecutedSize.is(0.5);
        assert s.entryPrice.is(10);

        // execute entry
        market.perform(Execution.with.buy(0.5).price(11));
        assert s.entrySize.is(1);
        assert s.entryExecutedSize.is(1);
        assert s.entryPrice.is(10);
    }

    @Test
    void exitMakeAtPrice() {
        when(now(), v -> trade(new Scenario() {

            @Override
            protected void entry() {
                entry(Direction.BUY, 1, s -> s.make(10));
            }

            @Override
            protected void exit() {
                exitAt(20);
            }
        }));

        Scenario s = last();

        // execute entry
        market.perform(Execution.with.buy(1).price(9));
        market.elapse(1, SECONDS);
        assert s.entrySize.is(1);
        assert s.entryExecutedSize.is(1);
        assert s.entryPrice.is(10);

        // exit entry
        assert s.exitSize.is(1);
        assert s.exitExecutedSize.is(0);
        assert s.exitPrice.is(0);

        // don't execute exit entry
        market.perform(Execution.with.buy(1).price(15));
        assert s.exitSize.is(1);
        assert s.exitExecutedSize.is(0);
        assert s.exitPrice.is(0);

        // execute exit entry
        market.perform(Execution.with.buy(1).price(21));
        assert s.exitSize.is(1);
        assert s.exitExecutedSize.is(1);
        assert s.exitPrice.is(20);
    }

    @Test
    void exitWillStopAllEntries() {
        when(now(), v -> trade(new Scenario() {

            @Override
            protected void entry() {
                entry(Direction.BUY, 3, s -> s.make(10));
            }

            @Override
            protected void exit() {
                exitAt(20);
            }
        }));

        Scenario s = last();

        // entry partially
        market.perform(Execution.with.buy(2).price(9));
        market.elapse(1, SECONDS);
        assert s.isEntryTerminated() == false;
        assert s.isExitTerminated() == false;

        // exit pertially
        market.perform(Execution.with.buy(1).price(21));
        assert s.isEntryTerminated();
        assert s.isExitTerminated() == false;

        // exit all
        market.perform(Execution.with.buy(1).price(21));
        assert s.isEntryTerminated();
        assert s.isExitTerminated();
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
    void holdSizeAndHoldMaxSize() {
        entry(Execution.with.buy(1).price(10));
        assert holdSize().is(1);
        assert holdMaxSize.is(1);

        entry(Execution.with.buy(2).price(10));
        assert holdSize().is(3);
        assert holdMaxSize.is(3);

        entry(Execution.with.sell(1).price(20));
        assert holdSize().is(2);
        assert holdMaxSize.is(3);

        entry(Execution.with.sell(3).price(20));
        assert holdSize().is(-1);
        assert holdMaxSize.is(3);

        entry(Execution.with.sell(3).price(20));
        assert holdSize().is(-4);
        assert holdMaxSize.is(4);
    }

    @Test
    void disableAlways() {
        disableWhile(I.signal(true));

        // don't executed because disabled
        entry(Execution.with.buy(1).price(10));

        Scenario s = last();
        assert s == null;
    }

    @Test
    void disableWhile() {
        Variable<Boolean> disable = Variable.of(false);
        disableWhile(disable.observe());

        // enable
        entryAndExit(Execution.with.buy(1).price(10), Execution.with.sell(1).price(20));
        Scenario s = last();
        assert s.entrySize.is(1);
        assert s.exitSize.is(1);

        // disable
        disable.set(true);
        entryAndExit(Execution.with.buy(1).price(20), Execution.with.sell(1).price(30));
        Scenario previous = last();
        assert previous == s;

        // enable
        disable.set(false);
        entryAndExit(Execution.with.buy(2).price(30), Execution.with.sell(2).price(40));
        s = last();
        assert previous != s;
        assert s.entrySize.is(2);
        assert s.exitSize.is(2);
    }

    @Test
    void disableWhileMultiple() {
        Variable<Boolean> disable1 = Variable.of(false);
        disableWhile(disable1.observe());

        Variable<Boolean> disable2 = Variable.of(false);
        disableWhile(disable2.observe());

        // disable one and other
        disable1.set(true);
        disable2.set(true);
        entry(Execution.with.buy(1).price(10));
        Scenario s = last();
        assert s == null;

        // disable one and enabe other
        disable1.set(true);
        disable2.set(false);
        entry(Execution.with.buy(1).price(10));
        s = last();
        assert s == null;

        // enable one and disable other
        disable1.set(false);
        disable2.set(true);
        entry(Execution.with.buy(1).price(10));
        s = last();
        assert s == null;

        // enable one and other
        disable1.set(false);
        disable2.set(false);
        entry(Execution.with.buy(1).price(10));
        s = last();
        assert s != null;
    }

    @Test
    void isEnableAndIsDisable() {
        Variable<Boolean> disable = Variable.of(false);
        disableWhile(disable.observe());

        // enable
        assert isEnable() == true;
        assert isDisable() == false;

        // disable
        disable.set(true);
        assert isEnable() == false;
        assert isDisable() == true;
        entry(Execution.with.buy(1).price(10));
        Scenario s = last();
        assert s == null;
    }

    @Test
    void enableAndDisable() {
        // enable
        assert isEnable() == true;
        assert isDisable() == false;

        // disable
        disable();
        assert isEnable() == false;
        assert isDisable() == true;
        entry(Execution.with.buy(1).price(10));
        Scenario s = last();
        assert s == null;

        // enable
        enable();
        assert isEnable() == true;
        assert isDisable() == false;
    }

    @Test
    void callingDeclareOnlyOnce() {

    }
}