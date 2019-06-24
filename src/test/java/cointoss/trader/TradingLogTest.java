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
import static cointoss.execution.Execution.with;

import org.junit.jupiter.api.Test;

import cointoss.execution.Execution;
import cointoss.trade.TradingLog;

public class TradingLogTest extends TraderTestSupport {

    @Test
    void entry() {
        entryAndExit(Execution.with.buy(1).price(10), Execution.with.buy(1).price(20));

        TradingLog log = log();
        assert log.terminated == 1;
        assert log.active == 0;
        assert log.cancel == 0;
        assert log.total == 1;
    }

    @Test
    void entries() {
        entryAndExit(Execution.with.buy(1).price(10), Execution.with.buy(1).price(20));
        entryAndExit(Execution.with.buy(1).price(30), Execution.with.buy(1).price(50));
        entryAndExit(Execution.with.buy(1).price(30), Execution.with.buy(1).price(10));

        TradingLog log = log();
        System.out.println(log);
        assert log.terminated == 3;
        assert log.active == 0;
        assert log.cancel == 0;
        assert log.total == 3;
        assert log.profit.max().is(20);
        assert log.profit.min().is(-20);
        assert log.profit.total().is(10);
        assert log.profit.mean().is("3");
    }

    @Test
    void completeProfit() {
        entryAndExit(Execution.with.buy(1).price(10), Execution.with.buy(1).price(15));

        TradingLog log = log();
        assert log.terminated == 1;
        assert log.profit.max().is(5);
        assert log.profit.min().is(5);
        assert log.profit.size() == 1;
        assert log.profit.total().is(5);
    }

    @Test
    void completeLoss() {
        entryAndExit(Execution.with.sell(1).price(10), Execution.with.buy(1).price(15));

        TradingLog log = log();
        assert log.terminated == 1;
        assert log.loss.max().is(-5);
        assert log.loss.min().is(-5);
        assert log.loss.size() == 1;
        assert log.loss.total().is(-5);
    }

    @Test
    void activeEntry() {
        entry(Execution.with.buy(1).price(10));

        TradingLog log = log();
        assert log.terminated == 0;
        assert log.active == 1;
        assert log.cancel == 0;
        assert log.total == 1;
    }

    @Test
    void profitAndLoss() {
        entryAndExit(BUY, 1, 10).exit(1, 15);
        entryAndExit(BUY, 1, 15).exit(1, 25);

        entryAndExit(SELL, 1, 25).exit(1, 30);
        entryAndExit(SELL, 1, 30).exit(1, 40);

        TradingLog log = log();
        assert log.terminated == 4;
        assert log.total == 4;

        assert log.profit.size() == 2;
        assert log.profit.max().is(10);
        assert log.profit.min().is(5);
        assert log.profit.total().is(15);

        assert log.loss.size() == 2;
        assert log.loss.max().is(-5);
        assert log.loss.min().is(-10);
        assert log.loss.total().is(-15);

        assert log.profitAndLoss.size() == 4;
        assert log.profitAndLoss.max().is(10);
        assert log.profitAndLoss.min().is(-10);
        assert log.profitAndLoss.total().is(0);
    }

    @Test
    void winningRate1() {
        // win
        entryAndExit(BUY, 1, 10).exit(1, 15);
        entryAndExit(BUY, 1, 15).exit(1, 25);

        // lose
        entryAndExit(SELL, 1, 25).exit(1, 30);
        entryAndExit(SELL, 1, 30).exit(1, 40);

        TradingLog log = log();
        assert log.winningRate().is(50);
    }

    @Test
    void winningRate2() {
        // win
        entryAndExit(BUY, 1, 10).exit(1, 15);
        entryAndExit(BUY, 1, 15).exit(1, 25);

        // lose
        entryAndExit(SELL, 1, 25).exit(1, 30);

        TradingLog log = log();
        assert log.winningRate().is("66.7");
    }

    @Test
    void winningRateNoWin() {
        // lose
        entryAndExit(SELL, 1, 25).exit(1, 30);
        entryAndExit(SELL, 1, 25).exit(1, 30);

        TradingLog log = log();
        assert log.winningRate().is("0");
    }

    @Test
    void winningRateNoLose() {
        // win
        entryAndExit(BUY, 1, 25).exit(1, 30);
        entryAndExit(BUY, 1, 25).exit(1, 30);

        TradingLog log = log();
        assert log.winningRate().is("100");
    }

    @Test
    void profitFactorSame() {
        // win
        entryAndExit(BUY, 1, 10).exit(1, 15);
        entryAndExit(BUY, 1, 15).exit(1, 25);

        // lose
        entryAndExit(SELL, 1, 25).exit(1, 30);
        entryAndExit(SELL, 1, 30).exit(1, 40);

        TradingLog log = log();
        assert log.profitFactor().is(1);
    }

    @Test
    void profitFactorUp() {
        // win
        entryAndExit(BUY, 1, 10).exit(1, 15);
        entryAndExit(BUY, 1, 15).exit(1, 25);

        // lose
        entryAndExit(SELL, 1, 20).exit(1, 30);

        TradingLog log = log();
        assert log.profitFactor().is("1.5");
    }

    @Test
    void profitFactorDown() {
        // win
        entryAndExit(BUY, 1, 10).exit(1, 20);

        // lose
        entryAndExit(SELL, 1, 25).exit(1, 30);
        entryAndExit(SELL, 1, 30).exit(1, 40);

        TradingLog log = log();
        assert log.profitFactor().is("0.667");
    }

    @Test
    void profitFactorNoLose() {
        // win
        entryAndExit(BUY, 1, 10).exit(1, 20);

        TradingLog log = log();
        assert log.profitFactor().is("10");
    }

    @Test
    void profitFactorNoWin() {
        // lose
        entryAndExit(SELL, 1, 25).exit(1, 30);
        entryAndExit(SELL, 1, 30).exit(1, 40);

        TradingLog log = log();
        assert log.profitFactor().is("0");
    }

    @Test
    void drawDown1() {
        entryAndExit(with.buy(1).price(10), with.buy(1).price(15)); // win 5
        entryAndExit(with.buy(1).price(10), with.buy(1).price(15)); // win 5
        entryAndExit(with.sell(1).price(25), with.buy(1).price(30)); // lose -5

        TradingLog log = log();
        assert log.drawDownRatio.is("0.045");
    }

    @Test
    void drawDown2() {
        entryAndExit(with.buy(1).price(10), with.buy(1).price(30)); // win 20
        entryAndExit(with.buy(1).price(30), with.buy(1).price(25)); // lose -5
        entryAndExit(with.buy(1).price(10), with.buy(1).price(40)); // win 30
        entryAndExit(with.buy(1).price(50), with.buy(1).price(25)); // lose -25
        entryAndExit(with.buy(1).price(30), with.buy(1).price(25)); // lose -5
        entryAndExit(with.buy(1).price(35), with.buy(1).price(25)); // lose -10
        entryAndExit(with.buy(1).price(10), with.buy(1).price(40)); // win 30

        TradingLog log = log();
        assert log.drawDownRatio.is("0.276");
    }

    @Test
    void drawDown3() {
        entryAndExit(with.buy(1).price(10), with.buy(1).price(15)); // lose 5
        entryAndExit(with.sell(1).price(10), with.buy(1).price(35)); // lose -25
        entryAndExit(with.sell(1).price(10), with.buy(1).price(15)); // lose -5
        entryAndExit(with.sell(1).price(10), with.buy(1).price(20)); // lose -10

        TradingLog log = log();
        assert log.drawDownRatio.is("0.381");
    }

    @Test
    void drawDownWinAll() {
        entryAndExit(with.buy(1).price(10), with.buy(1).price(30)); // win 20
        entryAndExit(with.buy(1).price(10), with.buy(1).price(40)); // win 30
        entryAndExit(with.buy(1).price(10), with.buy(1).price(30)); // win 30

        TradingLog log = log();
        assert log.drawDownRatio.is(0);
    }

    @Test
    void drawDownLoseAll() {
        entryAndExit(with.sell(1).price(10), with.buy(1).price(15)); // lose -5
        entryAndExit(with.sell(1).price(10), with.buy(1).price(35)); // lose -25
        entryAndExit(with.sell(1).price(10), with.buy(1).price(15)); // lose -5
        entryAndExit(with.sell(1).price(10), with.buy(1).price(20)); // lose -10

        TradingLog log = log();
        assert log.drawDownRatio.is("0.45");
    }
}
