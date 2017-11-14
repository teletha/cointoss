/*
 * Copyright (C) 2017 Nameless Production Committee
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          http://opensource.org/licenses/mit-license.php
 */
package cointoss.visual.mate;

import org.junit.Test;

import cointoss.Board.Unit;
import cointoss.util.Num;
import cointoss.visual.mate.MarketMakerBoard.Group;

/**
 * @version 2017/11/14 13:21:03
 */
public class BoardTest {

    @Test
    public void is() throws Exception {
        Group group = new Group(unit(1000, 1), 0, 1);
        assert group.is(Num.of(1000));
        assert group.is(Num.of(1001)) == false;
        assert group.is(Num.of(999)) == false;

        group = new Group(unit(1000, 1), -1, 10);
        assert group.is(Num.of(1000));
        assert group.is(Num.of(1001));
        assert group.is(Num.of(1009));
        assert group.is(Num.of(999)) == false;
        assert group.is(Num.of(1010)) == false;

        group = new Group(unit(1000, 1), -2, 100);
        assert group.is(Num.of(1000));
        assert group.is(Num.of(1001));
        assert group.is(Num.of(1099));
        assert group.is(Num.of(999)) == false;
        assert group.is(Num.of(1100)) == false;
    }

    @Test
    public void isGreaterThan() throws Exception {
        Group group = new Group(unit(1000, 1), 0, 1);
        assert group.isGreaterThan(Num.of(1000)) == false;
        assert group.isGreaterThan(Num.of(1001)) == false;
        assert group.isGreaterThan(Num.of(999));

        group = new Group(unit(1000, 1), -1, 10);
        assert group.isGreaterThan(Num.of(1000)) == false;
        assert group.isGreaterThan(Num.of(1001)) == false;
        assert group.isGreaterThan(Num.of(1009)) == false;
        assert group.isGreaterThan(Num.of(999));
        assert group.isGreaterThan(Num.of(1010)) == false;

        group = new Group(unit(1000, 1), -2, 100);
        assert group.isGreaterThan(Num.of(1000)) == false;
        assert group.isGreaterThan(Num.of(1001)) == false;
        assert group.isGreaterThan(Num.of(1099)) == false;
        assert group.isGreaterThan(Num.of(999));
        assert group.isGreaterThan(Num.of(1100)) == false;
    }

    @Test
    public void update() throws Exception {
        Group group = new Group(unit(1000, 1), 0, 1);
        assert group.size.is(1);
        assert group.update(unit(1000, 2)) == false;
        assert group.size.is(2);
        assert group.update(unit(1000, 4)) == false;
        assert group.size.is(4);
        assert group.update(unit(1000, 0)) == true;
        assert group.size.is(0);

        group = new Group(unit(1000, 1), -1, 10);
        assert group.size.is(1);
        assert group.update(unit(1000, 2)) == false;
        assert group.size.is(2);
        assert group.update(unit(1001, 4)) == false;
        assert group.size.is(6);
        assert group.update(unit(1001, 3)) == false;
        assert group.size.is(5);
        assert group.update(unit(1001, 0)) == false;
        assert group.size.is(2);
        assert group.update(unit(1000, 0)) == true;
        assert group.size.is(0);
    }

    /**
     * Helper.
     * 
     * @param price
     * @param size
     * @return
     */
    private Unit unit(int price, int size) {
        Unit unit = new Unit();
        unit.price = Num.of(price);
        unit.size = Num.of(size);

        return unit;
    }
}
