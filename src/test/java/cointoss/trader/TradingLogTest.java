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
    void complete() {
        entry(BUY, 1, 10).exit(1, 15);

        TradingLog log = createLog();
        assert log.complete == 1;
        assert log.active == 0;
        assert log.cancel == 0;
        assert log.total == 1;
    }

    @Test
    void completeProfit() {
        entry(BUY, 1, 10).exit(1, 15);

        TradingLog log = createLog();
        assert log.complete == 1;
        assert log.profit.max().is(5);
        assert log.profit.min().is(5);
        assert log.profit.size() == 1;
        assert log.profit.total().is(5);
    }

    @Test
    void completeLoss() {
        entry(SELL, 1, 10).exit(1, 15);

        TradingLog log = createLog();
        assert log.complete == 1;
        assert log.loss.max().is(-5);
        assert log.loss.min().is(-5);
        assert log.loss.size() == 1;
        assert log.loss.total().is(-5);
    }

    @Test
    void completeProfitMultiExitOrders() {
        entry(BUY, 1, 10).exit(0.5, 15).exit(0.5, 20);

        TradingLog log = createLog();
        assert log.complete == 1;
        assert log.profit.max().is("7.5");
        assert log.profit.min().is("7.5");
        assert log.profit.size() == 1;
        assert log.profit.total().is("7.5");
    }

    @Test
    void completeProfitMultiExitExecutions() {
        entry(BUY, 1, 10).exit(1, 15, 0.5, 0.5);

        TradingLog log = createLog();
        assert log.complete == 1;
        assert log.profit.max().is("5");
        assert log.profit.min().is("5");
        assert log.profit.size() == 1;
        assert log.profit.total().is("5");
    }

    @Test
    void active() {
        entry(BUY, 1, 10).exit(0.5, 15);

        TradingLog log = createLog();
        assert log.complete == 0;
        assert log.active == 1;
        assert log.cancel == 0;
        assert log.total == 1;
    }

    @Test
    void activeProfit() {
        entry(BUY, 1, 10).exit(0.5, 15);

        TradingLog log = createLog();
        assert log.active == 1;
        assert log.profit.max().is(5);
        assert log.profit.min().is(5);
        assert log.profit.size() == 1;
        assert log.profit.total().is(5);
    }

    @Test
    void activeLoss() {
        entry(SELL, 1, 10).exit(0.5, 15);

        TradingLog log = createLog();
        assert log.active == 1;
        assert log.loss.max().is(-5);
        assert log.loss.min().is(-5);
        assert log.loss.size() == 1;
        assert log.loss.total().is(-5);
    }

    @Test
    void activeProfitMultipleExitOrders() {
        entry(BUY, 1, 10).exit(0.5, 15).exit(0.2, 20);

        TradingLog log = createLog();
        assert log.active == 1;
        assert log.profit.max().is("7.5");
        assert log.profit.min().is("7.5");
        assert log.profit.size() == 1;
        assert log.profit.total().is("7.5");
    }

    @Test
    void activeProfitMultiExitExecutions() {
        entry(BUY, 1, 10).exit(1, 20, 0.2, 0.3);

        TradingLog log = createLog();
        assert log.active == 1;
        assert log.profit.max().is("10");
        assert log.profit.min().is("10");
        assert log.profit.size() == 1;
        assert log.profit.total().is("10");
    }

    @Test
    void entries() {
        entry(BUY, 1, 10).exit(1, 15);
        entry(BUY, 1, 15).exit(1, 25, 0.5);

        TradingLog log = createLog();
        assert log.complete == 1;
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
        entry(BUY, 1, 10).exit(1, 15);
        entry(BUY, 1, 15).exit(1, 25);

        entry(SELL, 1, 25).exit(1, 30);
        entry(SELL, 1, 30).exit(1, 40);

        TradingLog log = createLog();
        assert log.complete == 4;
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
        entry(BUY, 1, 10).exit(1, 15);
        entry(BUY, 1, 15).exit(1, 25);

        // lose
        entry(SELL, 1, 25).exit(1, 30);
        entry(SELL, 1, 30).exit(1, 40);

        TradingLog log = createLog();
        assert log.winningRate().is(50);
    }

    @Test
    void winningRate2() {
        // win
        entry(BUY, 1, 10).exit(1, 15);
        entry(BUY, 1, 15).exit(1, 25);

        // lose
        entry(SELL, 1, 25).exit(1, 30);

        TradingLog log = createLog();
        assert log.winningRate().is("66.7");
    }

    @Test
    void winningRateNoWin() {
        // lose
        entry(SELL, 1, 25).exit(1, 30);
        entry(SELL, 1, 25).exit(1, 30);

        TradingLog log = createLog();
        assert log.winningRate().is("0");
    }

    @Test
    void winningRateNoLose() {
        // win
        entry(BUY, 1, 25).exit(1, 30);
        entry(BUY, 1, 25).exit(1, 30);

        TradingLog log = createLog();
        assert log.winningRate().is("100");
    }

    @Test
    void profitFactorSame() {
        // win
        entry(BUY, 1, 10).exit(1, 15);
        entry(BUY, 1, 15).exit(1, 25);

        // lose
        entry(SELL, 1, 25).exit(1, 30);
        entry(SELL, 1, 30).exit(1, 40);

        TradingLog log = createLog();
        assert log.profitFactor().is(1);
    }

    @Test
    void profitFactorUp() {
        // win
        entry(BUY, 1, 10).exit(1, 15);
        entry(BUY, 1, 15).exit(1, 25);

        // lose
        entry(SELL, 1, 20).exit(1, 30);

        TradingLog log = createLog();
        assert log.profitFactor().is("1.5");
    }

    @Test
    void profitFactorDown() {
        // win
        entry(BUY, 1, 10).exit(1, 20);

        // lose
        entry(SELL, 1, 25).exit(1, 30);
        entry(SELL, 1, 30).exit(1, 40);

        TradingLog log = createLog();
        assert log.profitFactor().is("0.667");
    }

    @Test
    void profitFactorNoLose() {
        // win
        entry(BUY, 1, 10).exit(1, 20);

        TradingLog log = createLog();
        assert log.profitFactor().is("10");
    }

    @Test
    void profitFactorNoWin() {
        // lose
        entry(SELL, 1, 25).exit(1, 30);
        entry(SELL, 1, 30).exit(1, 40);

        TradingLog log = createLog();
        assert log.profitFactor().is("0");
    }

    @Test
    void drawDown1() {
        // win
        entry(BUY, 1, 10).exit(1, 15);
        entry(BUY, 1, 10).exit(1, 15);

        // lose
        entry(SELL, 1, 25).exit(1, 30);

        TradingLog log = createLog();
        assert log.drawDown.is(5);
        assert log.drawDownRate.is("4.5");
    }

    @Test
    void drawDown2() {
        entry(BUY, 1, 10).exit(1, 30); // win 20
        entry(SELL, 1, 25).exit(1, 30); // lose -5
        entry(BUY, 1, 10).exit(1, 40); // win 30
        entry(SELL, 1, 25).exit(1, 50); // lose -25
        entry(SELL, 1, 25).exit(1, 30); // lose -5
        entry(SELL, 1, 25).exit(1, 35); // lose -10
        entry(BUY, 1, 10).exit(1, 40); // win 30

        TradingLog log = createLog();
        assert log.drawDown.is(40);
        assert log.drawDownRate.is("27.6");
    }

    @Test
    void drawDown3() {
        entry(BUY, 1, 10).exit(1, 30); // win 20
        entry(BUY, 1, 10).exit(1, 40); // win 30
        entry(BUY, 1, 10).exit(1, 40); // win 30

        TradingLog log = createLog();
        assert log.drawDown.is(0);
        assert log.drawDownRate.is("0");
    }

    @Test
    void drawDown4() {
        entry(SELL, 1, 25).exit(1, 30); // lose -5
        entry(SELL, 1, 25).exit(1, 50); // lose -25
        entry(SELL, 1, 25).exit(1, 30); // lose -5
        entry(SELL, 1, 25).exit(1, 35); // lose -10

        TradingLog log = createLog();
        assert log.drawDown.is(45);
        assert log.drawDownRate.is("45");
    }

    @Test
    void asset() {
        entry(BUY, 1, 10).exit(1, 30); // win 20
        assert createLog().asset().is(120);
        entry(SELL, 1, 25).exit(1, 30); // lose -5
        assert createLog().asset().is(115);
        entry(BUY, 1, 10).exit(1, 40); // win 30
        assert createLog().asset().is(145);
        entry(SELL, 1, 25).exit(1, 50); // lose -25
        assert createLog().asset().is(120);
        entry(SELL, 1, 25).exit(1, 30); // lose -5
        assert createLog().asset().is(115);
        entry(SELL, 1, 25).exit(1, 35); // lose -10
        assert createLog().asset().is(105);
        entry(BUY, 1, 10).exit(1, 40); // win 30
        assert createLog().asset().is(135);
    }
}
