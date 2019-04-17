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

import static cointoss.MarketTestSupport.*;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import cointoss.execution.Execution;
import cointoss.Direction;
import cointoss.position.Position;
import cointoss.position.PositionManager;
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

        positions.add(execution(Direction.BUY, 1, 10));
        assert positions.hasPosition() == true;
        assert positions.hasNoPosition() == false;
    }

    @Test
    void isLong() {
        assert positions.isLong() == false;
        assert positions.isShort() == false;

        positions.add(execution(Direction.BUY, 1, 10));
        assert positions.isLong() == true;
        assert positions.isShort() == false;
    }

    @Test
    void isShort() {
        assert positions.isLong() == false;
        assert positions.isShort() == false;

        positions.add(execution(Direction.SELL, 1, 10));
        assert positions.isLong() == false;
        assert positions.isShort() == true;
    }

    @Test
    void size() {
        assert positions.size.is(Num.ZERO);

        // long
        positions.add(execution(Direction.BUY, 1, 10));
        assert positions.size.is(Num.ONE);

        // same price long
        positions.add(execution(Direction.BUY, 1, 10));
        assert positions.size.is(Num.TWO);

        // different price long
        positions.add(execution(Direction.BUY, 1, 20));
        assert positions.size.is(Num.THREE);

        // short
        positions.add(execution(Direction.SELL, 2, 10));
        assert positions.size.is(Num.ONE);

        // turn over
        positions.add(execution(Direction.SELL, 2, 20));
        assert positions.size.is(Num.ONE);
    }

    @Test
    void price() {
        assert positions.price.is(Num.ZERO);

        // long
        positions.add(execution(Direction.BUY, 1, 10));
        assert positions.price.is(Num.TEN);

        // same price long
        positions.add(execution(Direction.BUY, 1, 10));
        assert positions.price.is(Num.TEN);

        // different price long
        positions.add(execution(Direction.BUY, 2, 20));
        assert positions.price.is(Num.of(15));

        // short
        positions.add(execution(Direction.SELL, 2, 10));
        assert positions.price.is(Num.of(20));

        // turn over
        positions.add(execution(Direction.SELL, 2, 20));
        assert positions.price.is(Num.ZERO);
    }

    @Test
    void profit() {
        latest.set(buy(1, 20));
        assert positions.profit.is(Num.ZERO);

        // long
        positions.add(execution(Direction.BUY, 1, 10));
        assert positions.profit.is(Num.of(10));

        // same price long
        positions.add(execution(Direction.BUY, 1, 10));
        assert positions.profit.is(Num.of(20));

        // different price long
        positions.add(execution(Direction.BUY, 2, 20));
        assert positions.profit.is(Num.of(20));

        // short
        positions.add(execution(Direction.SELL, 2, 10));
        assert positions.profit.is(Num.ZERO);

        // turn over
        positions.add(execution(Direction.SELL, 2, 20));
        assert positions.profit.is(Num.ZERO);
    }

    @Test
    void zero() {
        positions.add(execution(Direction.BUY, 1, 10));
        positions.add(execution(Direction.SELL, 1, 12));

        assert positions.hasNoPosition();
    }

    @Test
    void increase() {
        positions.add(sell(3, 10));
        assert positions.size.is(Num.of(3));
        positions.add(buy(1, 10));
        assert positions.size.is(Num.of(2));
        positions.add(buy(1, 10));
        assert positions.size.is(Num.of(1));
        positions.add(buy(1, 10));
        assert positions.size.is(Num.of(0));
        positions.add(buy(1, 10));
        assert positions.size.is(Num.of(1));
        positions.add(buy(1, 10));
        assert positions.size.is(Num.of(2));
    }

    @Test
    void decrease() {
        positions.add(buy(3, 10));
        assert positions.size.is(Num.of(3));
        positions.add(sell(1, 10));
        assert positions.size.is(Num.of(2));
        positions.add(sell(1, 10));
        assert positions.size.is(Num.of(1));
        positions.add(sell(1, 10));
        assert positions.size.is(Num.of(0));
        positions.add(sell(1, 10));
        assert positions.size.is(Num.of(1));
        positions.add(sell(1, 10));
        assert positions.size.is(Num.of(2));
    }

    @Test
    void added() {
        List<Position> added = positions.added.toList();
        assert added.size() == 0;

        positions.add(buy(1, 10));
        assert added.size() == 1;

        // same price
        positions.add(buy(1, 10));
        assert added.size() == 1;

        // difference price
        positions.add(buy(1, 30));
        assert added.size() == 2;

        // exit partially
        positions.add(sell(2, 40));
        assert added.size() == 2;

        // exit overflow
        positions.add(sell(2, 50));
        assert added.size() == 3;
    }

    @Test
    void removed() {
        List<Position> removed = positions.removed.toList();
        assert removed.size() == 0;

        positions.add(buy(1, 10));
        assert removed.size() == 0;

        // same price
        positions.add(buy(1, 10));
        assert removed.size() == 0;

        // difference price
        positions.add(buy(1, 30));
        assert removed.size() == 0;

        // exit partially
        positions.add(sell(2, 40));
        assert removed.size() == 1;

        // exit overflow
        positions.add(sell(2, 50));
        assert removed.size() == 2;
    }
}
