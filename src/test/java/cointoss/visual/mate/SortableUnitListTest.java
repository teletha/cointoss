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
import cointoss.visual.mate.MarketMakerBoard.SortableUnitList;

/**
 * @version 2017/11/14 15:54:27
 */
public class SortableUnitListTest {

    @Test
    public void ascending() throws Exception {
        SortableUnitList list = new SortableUnitList(true);
        list.add(unit(1000, 1));
        assert list.list.get(0).price.is(1000);

        list.add(unit(1002, 1));
        assert list.list.get(0).price.is(1000);
        assert list.list.get(1).price.is(1002);

        list.add(unit(1001, 1));
        assert list.list.get(0).price.is(1000);
        assert list.list.get(1).price.is(1001);
        assert list.list.get(1).size.is(1);
        assert list.list.get(2).price.is(1002);

        // replace
        list.add(unit(1001, 2));
        assert list.list.get(0).price.is(1000);
        assert list.list.get(1).price.is(1001);
        assert list.list.get(1).size.is(2);
        assert list.list.get(2).price.is(1002);

        // remove
        list.add(unit(1000, 0));
        assert list.list.size() == 2;
        assert list.list.get(0).price.is(1001);
        assert list.list.get(1).price.is(1002);
    }

    @Test
    public void descending() throws Exception {
        SortableUnitList list = new SortableUnitList(false);
        list.add(unit(1000, 1));
        assert list.list.get(0).price.is(1000);

        list.add(unit(1002, 1));
        assert list.list.get(0).price.is(1002);
        assert list.list.get(1).price.is(1000);

        list.add(unit(1001, 1));
        assert list.list.get(0).price.is(1002);
        assert list.list.get(1).price.is(1001);
        assert list.list.get(1).size.is(1);
        assert list.list.get(2).price.is(1000);

        // replace
        list.add(unit(1001, 2));
        assert list.list.get(0).price.is(1002);
        assert list.list.get(1).price.is(1001);
        assert list.list.get(1).size.is(2);
        assert list.list.get(2).price.is(1000);

        // remove
        list.add(unit(1000, 0));
        assert list.list.size() == 2;
        assert list.list.get(0).price.is(1002);
        assert list.list.get(1).price.is(1001);
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
