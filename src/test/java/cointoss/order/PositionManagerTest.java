/*
 * Copyright (C) 2019 CoinToss Development Team
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          https://opensource.org/licenses/MIT
 */
package cointoss.order;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

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

    OrderManager positions;

    @BeforeEach
    void init() {
        service = new VerifiableMarketService();
        positions = new OrderManager(service);
    }

    @Test
    void hasPosition() {
        assert positions.hasPosition() == false;
        assert positions.hasNoPosition() == true;

        positions.add(Execution.with.buy(1).price(10));
        assert positions.hasPosition() == true;
        assert positions.hasNoPosition() == false;
    }

    @Test
    void isLong() {
        assert positions.hasLongPosition() == false;
        assert positions.hasShortPosition() == false;

        positions.add(Execution.with.buy(1).price(10));
        assert positions.hasLongPosition() == true;
        assert positions.hasShortPosition() == false;
    }

    @Test
    void isShort() {
        assert positions.hasLongPosition() == false;
        assert positions.hasShortPosition() == false;

        positions.add(Execution.with.sell(1).price(10));
        assert positions.hasLongPosition() == false;
        assert positions.hasShortPosition() == true;
    }

    @Test
    void size() {
        assert positions.positionSize.is(Num.ZERO);

        // long
        positions.add(Execution.with.buy(1).price(10));
        assert positions.positionSize.is(Num.ONE);

        // same price long
        positions.add(Execution.with.buy(1).price(10));
        assert positions.positionSize.is(Num.TWO);

        // different price long
        positions.add(Execution.with.buy(1).price(20));
        assert positions.positionSize.is(Num.THREE);

        // short
        positions.add(Execution.with.sell(2).price(10));
        assert positions.positionSize.is(Num.ONE);

        // turn over
        positions.add(Execution.with.sell(2).price(20));
        assert positions.positionSize.is(Num.ONE);
    }

    @Test
    void price() {
        assert positions.positionPrice.is(Num.ZERO);

        // long
        positions.add(Execution.with.buy(1).price(10));
        assert positions.positionPrice.is(Num.TEN);

        // same price long
        positions.add(Execution.with.buy(1).price(10));
        assert positions.positionPrice.is(Num.TEN);

        // different price long
        positions.add(Execution.with.buy(2).price(20));
        assert positions.positionPrice.is(Num.of(15));

        // short
        positions.add(Execution.with.sell(2).price(10));
        assert positions.positionPrice.is(Num.of(20));

        // turn over
        positions.add(Execution.with.sell(2).price(20));
        assert positions.positionPrice.is(Num.ZERO);
    }

    @Test
    void profit() {
        assert positions.profit(Num.of(20)).is(Num.ZERO);

        // long
        positions.add(Execution.with.buy(1).price(10));
        assert positions.profit(Num.of(20)).is(Num.of(10));

        // same price long
        positions.add(Execution.with.buy(1).price(10));
        assert positions.profit(Num.of(20)).is(Num.of(20));

        // different price long
        positions.add(Execution.with.buy(2).price(20));
        assert positions.profit(Num.of(20)).is(Num.of(20));

        // short
        positions.add(Execution.with.sell(2).price(10));
        assert positions.profit(Num.of(20)).is(Num.ZERO);

        // turn over
        positions.add(Execution.with.sell(2).price(20));
        assert positions.profit(Num.of(20)).is(Num.ZERO);
    }

    @Test
    void zero() {
        positions.add(Execution.with.buy(1).price(10));
        positions.add(Execution.with.sell(1).price(12));

        assert positions.hasNoPosition();
    }

    @Test
    void increase() {
        positions.add(Execution.with.sell(3).price(10));
        assert positions.positionSize.is(Num.of(3));
        positions.add(Execution.with.buy(1).price(10));
        assert positions.positionSize.is(Num.of(2));
        positions.add(Execution.with.buy(1).price(10));
        assert positions.positionSize.is(Num.of(1));
        positions.add(Execution.with.buy(1).price(10));
        assert positions.positionSize.is(Num.of(0));
        positions.add(Execution.with.buy(1).price(10));
        assert positions.positionSize.is(Num.of(1));
        positions.add(Execution.with.buy(1).price(10));
        assert positions.positionSize.is(Num.of(2));
    }

    @Test
    void decrease() {
        positions.add(Execution.with.buy(3).price(10));
        assert positions.positionSize.is(Num.of(3));
        positions.add(Execution.with.sell(1).price(10));
        assert positions.positionSize.is(Num.of(2));
        positions.add(Execution.with.sell(1).price(10));
        assert positions.positionSize.is(Num.of(1));
        positions.add(Execution.with.sell(1).price(10));
        assert positions.positionSize.is(Num.of(0));
        positions.add(Execution.with.sell(1).price(10));
        assert positions.positionSize.is(Num.of(1));
        positions.add(Execution.with.sell(1).price(10));
        assert positions.positionSize.is(Num.of(2));
    }

    @Test
    void added() {
        List<Position> added = positions.positionAdded.toList();
        assert added.size() == 0;

        positions.add(Execution.with.buy(1).price(10));
        assert added.size() == 1;

        // same price
        positions.add(Execution.with.buy(1).price(10));
        assert added.size() == 1;

        // difference price
        positions.add(Execution.with.buy(1).price(30));
        assert added.size() == 2;

        // exit partially
        positions.add(Execution.with.sell(2).price(40));
        assert added.size() == 2;

        // exit overflow
        positions.add(Execution.with.sell(2).price(50));
        assert added.size() == 3;
    }

    @Test
    void removed() {
        List<Position> removed = positions.positionRemoved.toList();
        assert removed.size() == 0;

        positions.add(Execution.with.buy(1).price(10));
        assert removed.size() == 0;

        // same price
        positions.add(Execution.with.buy(1).price(10));
        assert removed.size() == 0;

        // difference price
        positions.add(Execution.with.buy(1).price(30));
        assert removed.size() == 0;

        // exit partially
        positions.add(Execution.with.sell(2).price(40));
        assert removed.size() == 1;

        // exit overflow
        positions.add(Execution.with.sell(2).price(50));
        assert removed.size() == 2;
    }
}
