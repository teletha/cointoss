/*
 * Copyright (C) 2019 CoinToss Development Team
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          https://opensource.org/licenses/MIT
 */
package cointoss;

import java.time.ZonedDateTime;

import org.junit.jupiter.api.Test;

import cointoss.ticker.ExecutionFlow;
import cointoss.util.Num;

/**
 * @version 2018/04/02 16:47:20
 */
class ExecutionFlowTest {

    private ZonedDateTime base = ZonedDateTime.now().withSecond(0).withNano(0);

    @Test
    void price() {
        ExecutionFlow flow = new ExecutionFlow(3);
        assert flow.latest.price.is(0);

        flow.update(createBuy(0, 1, 1));
        assert flow.latest.price.is(1);

        flow.update(createBuy(0, 10, 1));
        assert flow.latest.price.is(10);

        flow.update(createSell(0, 5, 1));
        assert flow.latest.price.is(5);
    }

    @Test
    void longValues() {
        ExecutionFlow flow = new ExecutionFlow(3);
        flow.update(createBuy(0, 1, 1));
        assert flow.longVolume.is(1);

        flow.update(createBuy(0, 2, 1));
        assert flow.longVolume.is(2);

        flow.update(createBuy(0, 4, 2));
        assert flow.longVolume.is(4);

        flow.update(createBuy(0, 6, 1));
        assert flow.longVolume.is(4);
    }

    @Test
    void shortValues() {
        ExecutionFlow flow = new ExecutionFlow(3);
        flow.update(createSell(0, -1, 1));
        assert flow.shortVolume.is(1);

        flow.update(createSell(0, -2, 1));
        assert flow.shortVolume.is(2);

        flow.update(createSell(0, -4, 2));
        assert flow.shortVolume.is(4);

        flow.update(createSell(0, -6, 1));
        assert flow.shortVolume.is(4);
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
        e.date = base.plusSeconds(time);
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
        e.date = base.plusSeconds(time);
        e.side = Side.SELL;

        return e;
    }
}
