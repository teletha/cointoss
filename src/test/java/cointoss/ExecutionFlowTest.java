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

import java.time.ZonedDateTime;

import org.junit.Test;

import cointoss.util.Num;

/**
 * @version 2017/11/09 11:34:34
 */
public class ExecutionFlowTest {

    private ZonedDateTime base = ZonedDateTime.now().withSecond(0).withNano(0);

    @Test
    public void price() throws Exception {
        ExecutionFlow flow = new ExecutionFlow(3);
        System.out.println(flow);
        System.out.println(flow.latest == null);
        System.out.println(flow.latest.price == null);
        assert flow.latest.price.is(0);

        flow.record(createBuy(0, 1, 1));
        assert flow.latest.price.is(1);

        flow.record(createBuy(0, 10, 1));
        assert flow.latest.price.is(10);

        flow.record(createSell(0, 5, 1));
        assert flow.latest.price.is(5);
    }

    @Test
    public void longValues() throws Exception {
        ExecutionFlow flow = new ExecutionFlow(3);
        flow.record(createBuy(0, 1, 1));
        assert flow.longVolume.is(1);

        flow.record(createBuy(0, 2, 1));
        assert flow.longVolume.is(2);

        flow.record(createBuy(0, 4, 2));
        assert flow.longVolume.is(4);

        flow.record(createBuy(0, 6, 1));
        assert flow.longVolume.is(4);
    }

    @Test
    public void shortValues() throws Exception {
        ExecutionFlow flow = new ExecutionFlow(3);
        flow.record(createSell(0, -1, 1));
        assert flow.shortVolume.is(1);

        flow.record(createSell(0, -2, 1));
        assert flow.shortVolume.is(2);

        flow.record(createSell(0, -4, 2));
        assert flow.shortVolume.is(4);

        flow.record(createSell(0, -6, 1));
        assert flow.shortVolume.is(4);
    }

    @Test
    public void history() throws Exception {
        ExecutionFlow flow = new ExecutionFlow(3);
        flow.record(createBuy(0, 1, 1));
        assert flow.history.latest().latest.price.is(0);

        flow.record(createBuy(1, 2, 1));
        assert flow.history.latest().latest.price.is(1);

        flow.record(createBuy(2, 3, 1));
        assert flow.history.latest().latest.price.is(2);
    }

    /**
     * Create executon.
     * 
     * @param time
     * @param price
     * @param size
     * @return
     */
    private Execution createBuy(int time, int price, int size) {
        Execution e = new Execution();
        e.price = Num.of(price);
        e.size = Num.of(size);
        e.exec_date = base.plusSeconds(time);
        e.side = Side.BUY;

        return e;
    }

    /**
     * Create executon.
     * 
     * @param time
     * @param price
     * @param size
     * @return
     */
    private Execution createSell(int time, int price, int size) {
        Execution e = new Execution();
        e.price = Num.of(price);
        e.size = Num.of(size);
        e.exec_date = base.plusSeconds(time);
        e.side = Side.SELL;

        return e;
    }
}
