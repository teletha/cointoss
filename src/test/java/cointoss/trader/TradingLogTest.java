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

import org.junit.jupiter.api.Test;

import cointoss.trade.TradingLog;

/**
 * @version 2018/04/02 16:44:56
 */
public class TradingLogTest extends TraderTestSupport {

    @Test
    void initialized() {
        TradingLog log = log();
        assert log.terminated == 0;
        assert log.active == 0;
        assert log.cancel == 0;
        assert log.total == 0;
    }

    @Test
    void completeProfit() {
        entryAndExit(BUY, 1, 10).exit(1, 15);

        TradingLog log = createLog();
        assert log.terminated == 1;
        assert log.profit.max().is(5);
        assert log.profit.min().is(5);
        assert log.profit.size() == 1;
        assert log.profit.total().is(5);
    }

    @Test
    void completeLoss() {
        entryAndExit(SELL, 1, 10).exit(1, 15);

        TradingLog log = createLog();
        assert log.terminated == 1;
        assert log.loss.max().is(-5);
        assert log.loss.min().is(-5);
        assert log.loss.size() == 1;
        assert log.loss.total().is(-5);
    }

    @Test
    void completeProfitMultiExitOrders() {
        entryAndExit(BUY, 1, 10).exit(0.5, 15).exit(0.5, 20);

        TradingLog log = createLog();
        assert log.terminated == 1;
        assert log.profit.max().is("7.5");
        assert log.profit.min().is("7.5");
        assert log.profit.size() == 1;
        assert log.profit.total().is("7.5");
    }

    @Test
    void completeProfitMultiExitExecutions() {
        entryAndExit(BUY, 1, 10).exit(1, 15, 0.5, 0.5);

        TradingLog log = createLog();
        assert log.terminated == 1;
        assert log.profit.max().is("5");
        assert log.profit.min().is("5");
        assert log.profit.size() == 1;
        assert log.profit.total().is("5");
    }

    @Test
    void active() {
        entryAndExit(BUY, 1, 10).exit(0.5, 15);

        TradingLog log = createLog();
        assert log.terminated == 0;
        assert log.active == 1;
        assert log.cancel == 0;
        assert log.total == 1;
    }

    @Test
    void activeProfit() {
        entryAndExit(BUY, 1, 10).exit(0.5, 15);

        TradingLog log = createLog();
        assert log.active == 1;
        assert log.profit.max().is(5);
        assert log.profit.min().is(5);
        assert log.profit.size() == 1;
        assert log.profit.total().is(5);
    }

    @Test
    void activeLoss() {
        entryAndExit(SELL, 1, 10).exit(0.5, 15);

        TradingLog log = createLog();
        assert log.active == 1;
        assert log.loss.max().is(-5);
        assert log.loss.min().is(-5);
        assert log.loss.size() == 1;
        assert log.loss.total().is(-5);
    }

    @Test
    void activeProfitMultipleExitOrders() {
        entryAndExit(BUY, 1, 10).exit(0.5, 15).exit(0.2, 20);

        TradingLog log = createLog();
        assert log.active == 1;
        assert log.profit.max().is("7.5");
        assert log.profit.min().is("7.5");
        assert log.profit.size() == 1;
        assert log.profit.total().is("7.5");
    }

    @Test
    void activeProfitMultiExitExecutions() {
        entryAndExit(BUY, 1, 10).exit(1, 20, 0.2, 0.3);

        TradingLog log = createLog();
        assert log.active == 1;
        assert log.profit.max().is("10");
        assert log.profit.min().is("10");
        assert log.profit.size() == 1;
        assert log.profit.total().is("10");
    }

    @Test
    void entries() {
        entryAndExit(BUY, 1, 10).exit(1, 15);
        entryAndExit(BUY, 1, 15).exit(1, 25, 0.5);

        TradingLog log = createLog();
        assert log.terminated == 1;
        assert log.active == 1;
        assert log.cancel == 0;
        assert log.total == 2;
        assert log.profit.max().is(10);
        assert log.profit.min().is(5);
        assert log.profit.size() == 2;
        assert log.profit.total().is("15");
        assert log.profit.mean().is("7.5");
    }

    @Test
    void profitAndLoss() {
        entryAndExit(BUY, 1, 10).exit(1, 15);
        entryAndExit(BUY, 1, 15).exit(1, 25);

        entryAndExit(SELL, 1, 25).exit(1, 30);
        entryAndExit(SELL, 1, 30).exit(1, 40);

        TradingLog log = createLog();
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

        TradingLog log = createLog();
        assert log.winningRate().is(50);
    }

    @Test
    void winningRate2() {
        // win
        entryAndExit(BUY, 1, 10).exit(1, 15);
        entryAndExit(BUY, 1, 15).exit(1, 25);

        // lose
        entryAndExit(SELL, 1, 25).exit(1, 30);

        TradingLog log = createLog();
        assert log.winningRate().is("66.7");
    }

    @Test
    void winningRateNoWin() {
        // lose
        entryAndExit(SELL, 1, 25).exit(1, 30);
        entryAndExit(SELL, 1, 25).exit(1, 30);

        TradingLog log = createLog();
        assert log.winningRate().is("0");
    }

    @Test
    void winningRateNoLose() {
        // win
        entryAndExit(BUY, 1, 25).exit(1, 30);
        entryAndExit(BUY, 1, 25).exit(1, 30);

        TradingLog log = createLog();
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

        TradingLog log = createLog();
        assert log.profitFactor().is(1);
    }

    @Test
    void profitFactorUp() {
        // win
        entryAndExit(BUY, 1, 10).exit(1, 15);
        entryAndExit(BUY, 1, 15).exit(1, 25);

        // lose
        entryAndExit(SELL, 1, 20).exit(1, 30);

        TradingLog log = createLog();
        assert log.profitFactor().is("1.5");
    }

    @Test
    void profitFactorDown() {
        // win
        entryAndExit(BUY, 1, 10).exit(1, 20);

        // lose
        entryAndExit(SELL, 1, 25).exit(1, 30);
        entryAndExit(SELL, 1, 30).exit(1, 40);

        TradingLog log = createLog();
        assert log.profitFactor().is("0.667");
    }

    @Test
    void profitFactorNoLose() {
        // win
        entryAndExit(BUY, 1, 10).exit(1, 20);

        TradingLog log = createLog();
        assert log.profitFactor().is("10");
    }

    @Test
    void profitFactorNoWin() {
        // lose
        entryAndExit(SELL, 1, 25).exit(1, 30);
        entryAndExit(SELL, 1, 30).exit(1, 40);

        TradingLog log = createLog();
        assert log.profitFactor().is("0");
    }

    @Test
    void drawDown1() {
        // win
        entryAndExit(BUY, 1, 10).exit(1, 15);
        entryAndExit(BUY, 1, 10).exit(1, 15);

        // lose
        entryAndExit(SELL, 1, 25).exit(1, 30);

        TradingLog log = createLog();
        assert log.drawDown.is(5);
        assert log.drawDownRate.is("4.5");
    }

    @Test
    void drawDown2() {
        entryAndExit(BUY, 1, 10).exit(1, 30); // win 20
        entryAndExit(SELL, 1, 25).exit(1, 30); // lose -5
        entryAndExit(BUY, 1, 10).exit(1, 40); // win 30
        entryAndExit(SELL, 1, 25).exit(1, 50); // lose -25
        entryAndExit(SELL, 1, 25).exit(1, 30); // lose -5
        entryAndExit(SELL, 1, 25).exit(1, 35); // lose -10
        entryAndExit(BUY, 1, 10).exit(1, 40); // win 30

        TradingLog log = createLog();
        assert log.drawDown.is(40);
        assert log.drawDownRate.is("27.6");
    }

    @Test
    void drawDown3() {
        entryAndExit(BUY, 1, 10).exit(1, 30); // win 20
        entryAndExit(BUY, 1, 10).exit(1, 40); // win 30
        entryAndExit(BUY, 1, 10).exit(1, 40); // win 30

        TradingLog log = createLog();
        assert log.drawDown.is(0);
        assert log.drawDownRate.is("0");
    }

    @Test
    void drawDown4() {
        entryAndExit(SELL, 1, 25).exit(1, 30); // lose -5
        entryAndExit(SELL, 1, 25).exit(1, 50); // lose -25
        entryAndExit(SELL, 1, 25).exit(1, 30); // lose -5
        entryAndExit(SELL, 1, 25).exit(1, 35); // lose -10

        TradingLog log = createLog();
        assert log.drawDown.is(45);
        assert log.drawDownRate.is("45");
    }

    @Test
    void asset() {
        entryAndExit(BUY, 1, 10).exit(1, 30); // win 20
        assert createLog().asset().is(120);
        entryAndExit(SELL, 1, 25).exit(1, 30); // lose -5
        assert createLog().asset().is(115);
        entryAndExit(BUY, 1, 10).exit(1, 40); // win 30
        assert createLog().asset().is(145);
        entryAndExit(SELL, 1, 25).exit(1, 50); // lose -25
        assert createLog().asset().is(120);
        entryAndExit(SELL, 1, 25).exit(1, 30); // lose -5
        assert createLog().asset().is(115);
        entryAndExit(SELL, 1, 25).exit(1, 35); // lose -10
        assert createLog().asset().is(105);
        entryAndExit(BUY, 1, 10).exit(1, 40); // win 30
        assert createLog().asset().is(135);
    }
}
