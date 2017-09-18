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

import org.junit.Test;

/**
 * @version 2017/09/18 9:19:37
 */
public class TradingLogTest extends TradingTestSupport {

    @Test
    public void complete() throws Exception {
        entry(BUY, 1, 10).exit(1, 15);

        TradingLog log = createLog();
        assert log.complete == 1;
        assert log.active == 0;
        assert log.cancel == 0;
        assert log.total == 1;
    }

    @Test
    public void completeProfit() throws Exception {
        entry(BUY, 1, 10).exit(1, 15);

        TradingLog log = createLog();
        assert log.complete == 1;
        assert log.profit.max.is(5);
        assert log.profit.min.is(5);
        assert log.profit.size == 1;
        assert log.profit.total.is(5);
    }

    @Test
    public void completeProfitMultiExitOrders() throws Exception {
        entry(BUY, 1, 10).exit(0.5, 15).exit(0.5, 20);

        TradingLog log = createLog();
        assert log.complete == 1;
        assert log.profit.max.is("7.5");
        assert log.profit.min.is("7.5");
        assert log.profit.size == 1;
        assert log.profit.total.is("7.5");
    }

    @Test
    public void completeProfitMultiExitExecutions() throws Exception {
        entry(BUY, 1, 10).exit(1, 15, 0.5, 0.5);

        TradingLog log = createLog();
        assert log.complete == 1;
        assert log.profit.max.is("5");
        assert log.profit.min.is("5");
        assert log.profit.size == 1;
        assert log.profit.total.is("5");
    }

    @Test
    public void active() throws Exception {
        entry(BUY, 1, 10).exit(0.5, 15);

        TradingLog log = createLog();
        assert log.complete == 0;
        assert log.active == 1;
        assert log.cancel == 0;
        assert log.total == 1;
    }

    @Test
    public void activeProfit() throws Exception {
        entry(BUY, 1, 10).exit(0.5, 15);

        TradingLog log = createLog();
        assert log.active == 1;
        assert log.profit.max.is(5);
        assert log.profit.min.is(5);
        assert log.profit.size == 1;
        assert log.profit.total.is(5);
    }

    @Test
    public void activeLoss() throws Exception {
        entry(SELL, 1, 10).exit(0.5, 15);

        TradingLog log = createLog();
        assert log.active == 1;
        assert log.loss.max.is(-5);
        assert log.loss.min.is(-5);
        assert log.loss.size == 1;
        assert log.loss.total.is(-5);
    }

    @Test
    public void activeProfitMultipleExitOrders() throws Exception {
        entry(BUY, 1, 10).exit(0.5, 15).exit(0.2, 20);

        TradingLog log = createLog();
        assert log.active == 1;
        assert log.profit.max.is("7.5");
        assert log.profit.min.is("7.5");
        assert log.profit.size == 1;
        assert log.profit.total.is("7.5");
    }

    @Test
    public void activeProfitMultiExitExecutions() throws Exception {
        entry(BUY, 1, 10).exit(1, 20, 0.2, 0.3);

        TradingLog log = createLog();
        assert log.active == 1;
        assert log.profit.max.is("10");
        assert log.profit.min.is("10");
        assert log.profit.size == 1;
        assert log.profit.total.is("10");
    }

    @Test
    public void entries() throws Exception {
        entry(BUY, 1, 10).exit(1, 15);
        entry(BUY, 1, 15).exit(1, 25, 0.5);

        TradingLog log = createLog();
        assert log.complete == 1;
        assert log.active == 1;
        assert log.cancel == 0;
        assert log.total == 2;
        assert log.profit.max.is(10);
        assert log.profit.min.is(5);
        assert log.profit.size == 2;
        assert log.profit.total.is("15");
        assert log.profit.mean().is("7.5");
    }
}
