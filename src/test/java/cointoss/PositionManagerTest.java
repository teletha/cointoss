/*
 * Copyright (C) 2018 Nameless Production Committee
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          http://opensource.org/licenses/mit-license.php
 */
package cointoss;

import static cointoss.MarketTestSupport.*;

import org.junit.jupiter.api.Test;

import cointoss.util.Num;
import kiss.Variable;

/**
 * @version 2018/04/25 16:24:55
 */
public class PositionManagerTest {

    @Test
    void hasPosition() {
        PositionManager positions = new PositionManager(null);
        assert positions.hasPosition() == false;
        assert positions.hasNoPosition() == true;

        positions.add(position(Side.BUY, 1, 10));
        assert positions.hasPosition() == true;
        assert positions.hasNoPosition() == false;
    }

    @Test
    void isLong() {
        PositionManager positions = new PositionManager(null);
        assert positions.isLong() == false;
        assert positions.isShort() == false;

        positions.add(position(Side.BUY, 1, 10));
        assert positions.isLong() == true;
        assert positions.isShort() == false;
    }

    @Test
    void isShort() {
        PositionManager positions = new PositionManager(null);
        assert positions.isLong() == false;
        assert positions.isShort() == false;

        positions.add(position(Side.SELL, 1, 10));
        assert positions.isLong() == false;
        assert positions.isShort() == true;
    }

    @Test
    void size() {
        PositionManager positions = new PositionManager(null);
        assert positions.size.is(Num.ZERO);

        // long
        positions.add(position(Side.BUY, 1, 10));
        assert positions.size.is(Num.ONE);

        // same price long
        positions.add(position(Side.BUY, 1, 10));
        assert positions.size.is(Num.TWO);

        // different price long
        positions.add(position(Side.BUY, 1, 20));
        assert positions.size.is(Num.THREE);

        // short
        positions.add(position(Side.SELL, 2, 10));
        assert positions.size.is(Num.ONE);

        // turn over
        positions.add(position(Side.SELL, 2, 20));
        assert positions.size.is(Num.ONE);
    }

    @Test
    void price() {
        PositionManager positions = new PositionManager(null);
        assert positions.price.is(Num.ZERO);

        // long
        positions.add(position(Side.BUY, 1, 10));
        assert positions.price.is(Num.TEN);

        // same price long
        positions.add(position(Side.BUY, 1, 10));
        assert positions.price.is(Num.TEN);

        // different price long
        positions.add(position(Side.BUY, 2, 20));
        assert positions.price.is(Num.of(15));

        // short
        positions.add(position(Side.SELL, 2, 10));
        assert positions.price.is(Num.of(20));

        // turn over
        positions.add(position(Side.SELL, 2, 20));
        assert positions.price.is(Num.ZERO);
    }

    @Test
    void profit() {
        Variable<Execution> latest = Variable.of(buy(1, 20));
        PositionManager positions = new PositionManager(latest);
        assert positions.profit.is(Num.ZERO);

        // long
        positions.add(position(Side.BUY, 1, 10));
        assert positions.profit.is(Num.of(10));

        // same price long
        positions.add(position(Side.BUY, 1, 10));
        assert positions.profit.is(Num.of(20));

        // different price long
        positions.add(position(Side.BUY, 2, 20));
        assert positions.profit.is(Num.of(20));

        // short
        positions.add(position(Side.SELL, 2, 10));
        assert positions.profit.is(Num.ZERO);

        // turn over
        positions.add(position(Side.SELL, 2, 20));
        assert positions.profit.is(Num.ZERO);
    }

    @Test
    void zero() {
        PositionManager positions = new PositionManager(null);
        positions.add(position(Side.BUY, 1, 10));
        positions.add(position(Side.SELL, 1, 12));

        assert positions.hasNoPosition();
    }
}
