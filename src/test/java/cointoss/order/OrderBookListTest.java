/*
 * Copyright (C) 2017 Nameless Production Committee
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          http://opensource.org/licenses/mit-license.php
 */
package cointoss.order;

import javafx.collections.ObservableList;

import org.junit.Test;

import cointoss.Side;
import cointoss.util.Num;

/**
 * @version 2017/12/01 2:30:34
 */
public class OrderBookListTest {

    @Test
    public void buy() throws Exception {
        OrderBookList list = new OrderBookList(Side.BUY, true);

        // add
        list.update(unit(1000, 1));
        assert list.x1.get(0).price.is(1000);

        list.update(unit(1002, 1));
        assert list.x1.get(0).price.is(1002);
        assert list.x1.get(1).price.is(1000);

        list.update(unit(1001, 1));
        assert list.x1.get(0).price.is(1002);
        assert list.x1.get(1).price.is(1001);
        assert list.x1.get(1).size.is(1);
        assert list.x1.get(2).price.is(1000);

        // replace
        list.update(unit(1001, 2));
        assert list.x1.get(0).price.is(1002);
        assert list.x1.get(1).price.is(1001);
        assert list.x1.get(1).size.is(2);
        assert list.x1.get(2).price.is(1000);

        // remove
        list.update(unit(1000, 0));
        assert list.x1.size() == 2;
        assert list.x1.get(0).price.is(1002);
        assert list.x1.get(1).price.is(1001);
    }

    @Test
    public void sell() throws Exception {
        OrderBookList list = new OrderBookList(Side.SELL, true);
        list.update(unit(1000, 1));
        assert list.x1.get(0).price.is(1000);

        list.update(unit(1002, 1));
        assert list.x1.get(0).price.is(1002);
        assert list.x1.get(1).price.is(1000);

        list.update(unit(1001, 1));
        assert list.x1.get(0).price.is(1002);
        assert list.x1.get(1).price.is(1001);
        assert list.x1.get(1).size.is(1);
        assert list.x1.get(2).price.is(1000);

        // replace
        list.update(unit(1001, 2));
        assert list.x1.get(0).price.is(1002);
        assert list.x1.get(1).price.is(1001);
        assert list.x1.get(1).size.is(2);
        assert list.x1.get(2).price.is(1000);

        // remove
        list.update(unit(1000, 0));
        assert list.x1.size() == 2;
        assert list.x1.get(0).price.is(1002);
        assert list.x1.get(1).price.is(1001);
    }

    @Test
    public void buyGroup() throws Exception {
        OrderBookList list = new OrderBookList(Side.BUY, true);
        list.update(unit(1000, 1));
        assertList(list.x10, 0, 1000, 1);

        // add
        list.update(unit(1009, 1));
        assertList(list.x10, 0, 1000, 2);

        // minus
        list.update(unit(1000, 0));
        assertList(list.x10, 0, 1000, 1);

        // next group
        list.update(unit(1010, 1));
        assertList(list.x10, 0, 1010, 1);
        assertList(list.x10, 1, 1000, 1);

        // remove
        list.update(unit(1009, 0));
        assertList(list.x10, 0, 1010, 1);
    }

    @Test
    public void sellGroup() throws Exception {
        OrderBookList list = new OrderBookList(Side.SELL, true);
        list.update(unit(1000, 1));
        assertList(list.x10, 0, 1000, 1);

        // add
        list.update(unit(1009, 1));
        assertList(list.x10, 0, 1000, 2);

        // minus
        list.update(unit(1000, 0));
        assertList(list.x10, 0, 1000, 1);

        // next group
        list.update(unit(1010, 1));
        assertList(list.x10, 0, 1010, 1);
        assertList(list.x10, 1, 1000, 1);

        // remove
        list.update(unit(1009, 0));
        assertList(list.x10, 0, 1010, 1);
    }

    @Test
    public void buyFix() throws Exception {
        OrderBookList list = new OrderBookList(Side.BUY, true);
        list.update(unit(1003, 1));
        list.update(unit(1002, 1));
        list.update(unit(1001, 1));
        list.update(unit(1000, 1));
        assert list.x1.size() == 4;

        // fix error
        list.fix(Num.of(1002));
        assertList(list.x1, 0, 1002, 1);
        assert list.x1.size() == 3;

        // fix error
        list.fix(Num.of(1000));
        assertList(list.x1, 0, 1000, 1);
        assert list.x1.size() == 1;
    }

    @Test
    public void sellFix() throws Exception {
        OrderBookList list = new OrderBookList(Side.SELL, true);
        list.update(unit(1003, 1));
        list.update(unit(1002, 1));
        list.update(unit(1001, 1));
        list.update(unit(1000, 1));
        assert list.x1.size() == 4;

        // fix error
        list.fix(Num.of(1001));
        assertList(list.x1, 2, 1001, 1);
        assert list.x1.size() == 3;

        // fix error
        list.fix(Num.of(1003));
        assertList(list.x1, 0, 1003, 1);
        assert list.x1.size() == 1;
    }

    /**
     * Helper method to assert.
     * 
     * @param list
     * @param index
     * @param size
     * @param price
     */
    private void assertList(ObservableList<OrderUnit> list, int index, int price, int size) {
        OrderUnit unit = list.get(index);
        assert unit.size.is(size);
        assert unit.price.is(price);
    }

    /**
     * Helper.
     * 
     * @param price
     * @param size
     * @return
     */
    private OrderUnit unit(int price, int size) {
        return new OrderUnit(Num.of(price), Num.of(size));
    }
}
