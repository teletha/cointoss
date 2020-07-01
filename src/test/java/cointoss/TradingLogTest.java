/*
 * Copyright (C) 2020 cointoss Development Team
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          https://opensource.org/licenses/MIT
 */
package cointoss;

import org.junit.jupiter.api.Test;

import cointoss.analyze.TradingStats;
import cointoss.execution.Execution;
import cointoss.trade.TraderTestSupport;

public class TradingLogTest extends TraderTestSupport {

    @Test
    void entryTerminated() {
        entryAndExit(Execution.with.buy(1).price(10), Execution.with.buy(1).price(20));

        TradingStats log = statistics();
        assert log.terminated == 1;
        assert log.active == 0;
        assert log.total == 1;
    }

    @Test
    void entryActive() {
        entry(Execution.with.buy(1).price(10));

        TradingStats log = statistics();
        assert log.terminated == 0;
        assert log.active == 1;
        assert log.total == 1;
    }

    @Test
    void profitStatistics() {
        entryAndExit(Execution.with.buy(1).price(10), Execution.with.buy(1).price(20)); // profit
        entryAndExit(Execution.with.buy(1).price(30), Execution.with.buy(1).price(50)); // profit
        entryAndExit(Execution.with.buy(1).price(30), Execution.with.buy(1).price(10)); // loss

        TradingStats log = statistics();
        assert log.profit.size() == 2;
        assert log.profit.max().is(20);
        assert log.profit.min().is(10);
        assert log.profit.total().is(30);
        assert log.profit.mean().is(15);
    }

    @Test
    void lossStatistics() {
        entryAndExit(Execution.with.buy(1).price(10), Execution.with.buy(1).price(20)); // profit
        entryAndExit(Execution.with.buy(1).price(30), Execution.with.buy(1).price(20)); // loss
        entryAndExit(Execution.with.buy(1).price(30), Execution.with.buy(1).price(10)); // loss

        TradingStats log = statistics();
        assert log.loss.size() == 2;
        assert log.loss.max().is(-20);
        assert log.loss.min().is(-10);
        assert log.loss.total().is(-30);
        assert log.loss.mean().is(-15);
    }

    @Test
    void profitAndLoss() {
        entryAndExit(Execution.with.buy(1).price(10), Execution.with.buy(1).price(30)); // profit
        entryAndExit(Execution.with.buy(1).price(10), Execution.with.buy(1).price(30)); // profit
        entryAndExit(Execution.with.buy(1).price(30), Execution.with.buy(1).price(20)); // loss
        entryAndExit(Execution.with.buy(1).price(30), Execution.with.buy(1).price(10)); // loss

        TradingStats log = statistics();
        assert log.profitAndLoss.size() == 4;
        assert log.profitAndLoss.max().is(20);
        assert log.profitAndLoss.min().is(-20);
        assert log.profitAndLoss.total().is(10);
        assert log.profitAndLoss.mean().is(2.5);
    }

    @Test
    void completeProfit() {
        entryAndExit(Execution.with.buy(1).price(10), Execution.with.buy(1).price(15));

        TradingStats log = statistics();
        assert log.terminated == 1;
        assert log.profit.max().is(5);
        assert log.profit.min().is(5);
        assert log.profit.size() == 1;
        assert log.profit.total().is(5);
    }

    @Test
    void completeLoss() {
        entryAndExit(Execution.with.sell(1).price(10), Execution.with.buy(1).price(15));

        TradingStats log = statistics();
        assert log.terminated == 1;
        assert log.loss.max().is(-5);
        assert log.loss.min().is(-5);
        assert log.loss.size() == 1;
        assert log.loss.total().is(-5);
    }

    @Test
    void activeEntry() {
        entry(Execution.with.buy(1).price(10));

        TradingStats log = statistics();
        assert log.terminated == 0;
        assert log.active == 1;
        assert log.total == 1;
    }

    @Test
    void winningRate1() {
        // win
        entryAndExit(Execution.with.buy(1).price(10), Execution.with.buy(1).price(15));
        entryAndExit(Execution.with.buy(1).price(10), Execution.with.buy(1).price(15));

        // lose
        entryAndExit(Execution.with.sell(1).price(10), Execution.with.buy(1).price(15));
        entryAndExit(Execution.with.sell(1).price(10), Execution.with.buy(1).price(15));

        TradingStats log = statistics();
        assert log.winningRate().is(50);
    }

    @Test
    void winningRate2() {
        // win
        entryAndExit(Execution.with.buy(1).price(10), Execution.with.buy(1).price(15));
        entryAndExit(Execution.with.buy(1).price(10), Execution.with.buy(1).price(15));

        // lose
        entryAndExit(Execution.with.sell(1).price(10), Execution.with.buy(1).price(15));

        TradingStats log = statistics();
        assert log.winningRate().is(66.7);
    }

    @Test
    void winningRateNoWin() {
        // lose
        entryAndExit(Execution.with.sell(1).price(10), Execution.with.buy(1).price(15));
        entryAndExit(Execution.with.sell(1).price(10), Execution.with.buy(1).price(15));

        TradingStats log = statistics();
        assert log.winningRate().is(0);
    }

    @Test
    void winningRateNoLose() {
        // win
        entryAndExit(Execution.with.buy(1).price(10), Execution.with.buy(1).price(15));
        entryAndExit(Execution.with.buy(1).price(10), Execution.with.buy(1).price(15));

        TradingStats log = statistics();
        assert log.winningRate().is(100);
    }

    @Test
    void profitFactorSame() {
        // win
        entryAndExit(Execution.with.buy(1).price(10), Execution.with.buy(1).price(15));
        entryAndExit(Execution.with.buy(1).price(10), Execution.with.buy(1).price(15));

        // lose
        entryAndExit(Execution.with.sell(1).price(10), Execution.with.buy(1).price(15));
        entryAndExit(Execution.with.sell(1).price(10), Execution.with.buy(1).price(15));

        TradingStats log = statistics();
        assert log.profitFactor().is(1);
    }

    @Test
    void profitFactorUp() {
        // win
        entryAndExit(Execution.with.buy(1).price(10), Execution.with.buy(1).price(15));
        entryAndExit(Execution.with.buy(1).price(10), Execution.with.buy(1).price(15));

        // lose
        entryAndExit(Execution.with.sell(1).price(10), Execution.with.buy(1).price(15));

        TradingStats log = statistics();
        assert log.profitFactor().is("2");
    }

    @Test
    void profitFactorDown() {
        // win
        entryAndExit(Execution.with.buy(1).price(10), Execution.with.buy(1).price(15));

        // lose
        entryAndExit(Execution.with.sell(1).price(10), Execution.with.buy(1).price(15));
        entryAndExit(Execution.with.sell(1).price(10), Execution.with.buy(1).price(15));

        TradingStats log = statistics();
        assert log.profitFactor().is("0.5");
    }

    @Test
    void profitFactorNoLose() {
        // win
        entryAndExit(Execution.with.buy(1).price(10), Execution.with.buy(1).price(15));
        entryAndExit(Execution.with.buy(1).price(10), Execution.with.buy(1).price(15));

        TradingStats log = statistics();
        assert log.profitFactor().is("10");
    }

    @Test
    void profitFactorNoWin() {
        // lose
        entryAndExit(Execution.with.sell(1).price(10), Execution.with.buy(1).price(15));
        entryAndExit(Execution.with.sell(1).price(10), Execution.with.buy(1).price(15));

        TradingStats log = statistics();
        assert log.profitFactor().is("0");
    }

    @Test
    void drawDown1() {
        entryAndExit(Execution.with.buy(1).price(10), Execution.with.buy(1).price(15)); // win 5
        entryAndExit(Execution.with.buy(1).price(10), Execution.with.buy(1).price(15)); // win 5
        entryAndExit(Execution.with.sell(1).price(25), Execution.with.buy(1).price(30)); // lose -5

        TradingStats log = statistics();
        assert log.drawDownRatio.is("0.045");
    }

    @Test
    void drawDown2() {
        entryAndExit(Execution.with.buy(1).price(10), Execution.with.buy(1).price(30)); // win 20
        entryAndExit(Execution.with.buy(1).price(30), Execution.with.buy(1).price(25)); // lose -5
        entryAndExit(Execution.with.buy(1).price(10), Execution.with.buy(1).price(40)); // win 30
        entryAndExit(Execution.with.buy(1).price(50), Execution.with.buy(1).price(25)); // lose -25
        entryAndExit(Execution.with.buy(1).price(30), Execution.with.buy(1).price(25)); // lose -5
        entryAndExit(Execution.with.buy(1).price(35), Execution.with.buy(1).price(25)); // lose -10
        entryAndExit(Execution.with.buy(1).price(10), Execution.with.buy(1).price(40)); // win 30

        TradingStats log = statistics();
        assert log.drawDownRatio.is("0.276");
    }

    @Test
    void drawDown3() {
        entryAndExit(Execution.with.buy(1).price(10), Execution.with.buy(1).price(15)); // lose 5
        entryAndExit(Execution.with.sell(1).price(10), Execution.with.buy(1).price(35)); // lose -25
        entryAndExit(Execution.with.sell(1).price(10), Execution.with.buy(1).price(15)); // lose -5
        entryAndExit(Execution.with.sell(1).price(10), Execution.with.buy(1).price(20)); // lose -10

        TradingStats log = statistics();
        assert log.drawDownRatio.is("0.381");
    }

    @Test
    void drawDownWinAll() {
        entryAndExit(Execution.with.buy(1).price(10), Execution.with.buy(1).price(30)); // win 20
        entryAndExit(Execution.with.buy(1).price(10), Execution.with.buy(1).price(40)); // win 30
        entryAndExit(Execution.with.buy(1).price(10), Execution.with.buy(1).price(30)); // win 30

        TradingStats log = statistics();
        assert log.drawDownRatio.is(0);
    }

    @Test
    void drawDownLoseAll() {
        entryAndExit(Execution.with.sell(1).price(10), Execution.with.buy(1).price(15)); // lose -5
        entryAndExit(Execution.with.sell(1).price(10), Execution.with.buy(1).price(35)); // lose -25
        entryAndExit(Execution.with.sell(1).price(10), Execution.with.buy(1).price(15)); // lose -5
        entryAndExit(Execution.with.sell(1).price(10), Execution.with.buy(1).price(20)); // lose -10

        TradingStats log = statistics();
        assert log.drawDownRatio.is("0.45");
    }
}