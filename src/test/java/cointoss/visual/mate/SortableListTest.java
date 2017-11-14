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
import cointoss.visual.mate.MarketBoard.SortableGroupList;
import cointoss.visual.mate.MarketBoard.SortableUnitList;

/**
 * @version 2017/11/14 15:54:27
 */
public class SortableListTest {

    @Test
    public void unitFromHead() throws Exception {
        SortableUnitList list = new SortableUnitList(true, true);
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

    @Test
    public void unitFromTail() throws Exception {
        SortableUnitList list = new SortableUnitList(false, true);
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

    @Test
    public void groupFromHead() throws Exception {
        SortableGroupList list = new SortableGroupList(true, -1, true);
        list.add(1000, 1);
        assert list.list.size() == 1;
        assert list.list.get(0).price.is(1000);
        assert list.list.get(0).size.is(1);

        // add
        list.add(1009, 1);
        assert list.list.size() == 1;
        assert list.list.get(0).price.is(1000);
        assert list.list.get(0).size.is(2);

        // minus
        list.add(1000, -1);
        assert list.list.size() == 1;
        assert list.list.get(0).price.is(1000);
        assert list.list.get(0).size.is(1);

        // next group
        list.add(1010, 1);
        assert list.list.size() == 2;
        assert list.list.get(0).price.is(1010);
        assert list.list.get(0).size.is(1);
        assert list.list.get(1).price.is(1000);
        assert list.list.get(1).size.is(1);

        // remove
        list.add(1009, -1);
        assert list.list.size() == 1;
        assert list.list.get(0).price.is(1010);
        assert list.list.get(0).size.is(1);
    }

    @Test
    public void groupFromTail() throws Exception {
        SortableGroupList list = new SortableGroupList(false, -1, true);
        list.add(1000, 1);
        assert list.list.size() == 1;
        assert list.list.get(0).price.is(1000);
        assert list.list.get(0).size.is(1);

        // add
        list.add(1009, 1);
        assert list.list.size() == 1;
        assert list.list.get(0).price.is(1000);
        assert list.list.get(0).size.is(2);

        // minus
        list.add(1000, -1);
        assert list.list.size() == 1;
        assert list.list.get(0).price.is(1000);
        assert list.list.get(0).size.is(1);

        // next group
        list.add(1010, 1);
        assert list.list.size() == 2;
        assert list.list.get(0).price.is(1010);
        assert list.list.get(0).size.is(1);
        assert list.list.get(1).price.is(1000);
        assert list.list.get(1).size.is(1);

        // remove
        list.add(1009, -1);
        assert list.list.size() == 1;
        assert list.list.get(0).price.is(1010);
        assert list.list.get(0).size.is(1);
    }

    /**
     * Helper.
     * 
     * @param price
     * @param size
     * @return
     */
    private Unit unit(int price, int size) {
        return new Unit(Num.of(price), Num.of(size));
    }
}
