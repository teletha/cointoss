/*
 * Copyright (C) 2019 CoinToss Development Team
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          https://opensource.org/licenses/MIT
 */
package cointoss.position;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import cointoss.execution.Executed;
import cointoss.execution.Execution;
import cointoss.util.Num;
import cointoss.verify.VerifiableMarketService;
import kiss.Variable;

/**
 * @version 2018/07/10 17:08:50
 */
class PositionManagerTest {

    Variable<Execution> latest;

    VerifiableMarketService service;

    PositionManager positions;

    @BeforeEach
    void init() {
        latest = Variable.of(Execution.BASE);
        service = new VerifiableMarketService();
        positions = new PositionManager(service, latest);
    }

    @Test
    void hasPosition() {
        assert positions.hasPosition() == false;
        assert positions.hasNoPosition() == true;

        positions.add(Executed.buy(1).price(10));
        assert positions.hasPosition() == true;
        assert positions.hasNoPosition() == false;
    }

    @Test
    void isLong() {
        assert positions.isLong() == false;
        assert positions.isShort() == false;

        positions.add(Executed.buy(1).price(10));
        assert positions.isLong() == true;
        assert positions.isShort() == false;
    }

    @Test
    void isShort() {
        assert positions.isLong() == false;
        assert positions.isShort() == false;

        positions.add(Executed.sell(1).price(10));
        assert positions.isLong() == false;
        assert positions.isShort() == true;
    }

    @Test
    void size() {
        assert positions.size.is(Num.ZERO);

        // long
        positions.add(Executed.buy(1).price(10));
        assert positions.size.is(Num.ONE);

        // same price long
        positions.add(Executed.buy(1).price(10));
        assert positions.size.is(Num.TWO);

        // different price long
        positions.add(Executed.buy(1).price(20));
        assert positions.size.is(Num.THREE);

        // short
        positions.add(Executed.sell(2).price(10));
        assert positions.size.is(Num.ONE);

        // turn over
        positions.add(Executed.sell(2).price(20));
        assert positions.size.is(Num.ONE);
    }

    @Test
    void price() {
        assert positions.price.is(Num.ZERO);

        // long
        positions.add(Executed.buy(1).price(10));
        assert positions.price.is(Num.TEN);

        // same price long
        positions.add(Executed.buy(1).price(10));
        assert positions.price.is(Num.TEN);

        // different price long
        positions.add(Executed.buy(2).price(20));
        assert positions.price.is(Num.of(15));

        // short
        positions.add(Executed.sell(2).price(10));
        assert positions.price.is(Num.of(20));

        // turn over
        positions.add(Executed.sell(2).price(20));
        assert positions.price.is(Num.ZERO);
    }

    @Test
    void profit() {
        latest.set(Executed.buy(1).price(20));
        assert positions.profit.is(Num.ZERO);

        // long
        positions.add(Executed.buy(1).price(10));
        assert positions.profit.is(Num.of(10));

        // same price long
        positions.add(Executed.buy(1).price(10));
        assert positions.profit.is(Num.of(20));

        // different price long
        positions.add(Executed.buy(2).price(20));
        assert positions.profit.is(Num.of(20));

        // short
        positions.add(Executed.sell(2).price(10));
        assert positions.profit.is(Num.ZERO);

        // turn over
        positions.add(Executed.sell(2).price(20));
        assert positions.profit.is(Num.ZERO);
    }

    @Test
    void zero() {
        positions.add(Executed.buy(1).price(10));
        positions.add(Executed.sell(1).price(12));

        assert positions.hasNoPosition();
    }

    @Test
    void increase() {
        positions.add(Executed.sell(3).price(10));
        assert positions.size.is(Num.of(3));
        positions.add(Executed.buy(1).price(10));
        assert positions.size.is(Num.of(2));
        positions.add(Executed.buy(1).price(10));
        assert positions.size.is(Num.of(1));
        positions.add(Executed.buy(1).price(10));
        assert positions.size.is(Num.of(0));
        positions.add(Executed.buy(1).price(10));
        assert positions.size.is(Num.of(1));
        positions.add(Executed.buy(1).price(10));
        assert positions.size.is(Num.of(2));
    }

    @Test
    void decrease() {
        positions.add(Executed.buy(3).price(10));
        assert positions.size.is(Num.of(3));
        positions.add(Executed.sell(1).price(10));
        assert positions.size.is(Num.of(2));
        positions.add(Executed.sell(1).price(10));
        assert positions.size.is(Num.of(1));
        positions.add(Executed.sell(1).price(10));
        assert positions.size.is(Num.of(0));
        positions.add(Executed.sell(1).price(10));
        assert positions.size.is(Num.of(1));
        positions.add(Executed.sell(1).price(10));
        assert positions.size.is(Num.of(2));
    }
}
