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
    public void buyFix() throws Exception {
        OrderBookList list = new OrderBookList(Side.BUY, true);
        list.update(unit(1007, 1));
        list.update(unit(1006, 1));
        list.update(unit(1005, 1));
        list.update(unit(1004, 1));
        list.update(unit(1003, 1));
        list.update(unit(1000, 1));
        assert list.x1.size() == 6;

        // fix error
        list.fix(Num.of(1006));
        assertList(list.x1, 0, 1006, 1);
        assert list.x1.size() == 5;

        // fix error : multiple
        list.fix(Num.of(1004));
        assertList(list.x1, 0, 1004, 1);
        assert list.x1.size() == 3;

        // fix error : not exits
        list.fix(Num.of(1002));
        assertList(list.x1, 0, 1000, 1);
        assert list.x1.size() == 1;
    }

    @Test
    public void sellFix() throws Exception {
        OrderBookList list = new OrderBookList(Side.SELL, true);
        list.update(unit(1007, 1));
        list.update(unit(1004, 1));
        list.update(unit(1003, 1));
        list.update(unit(1002, 1));
        list.update(unit(1001, 1));
        list.update(unit(1000, 1));
        assert list.x1.size() == 6;

        // fix error
        list.fix(Num.of(1001));
        assert list.x1.size() == 5;

        // fix error : multiple
        list.fix(Num.of(1003));
        assert list.x1.size() == 3;

        // fix error : not exits
        list.fix(Num.of(1005));
        assert list.x1.size() == 1;
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
    public void buyGroupFix() throws Exception {
        OrderBookList list = new OrderBookList(Side.BUY, true);
        list.update(unit(1061, 1));
        list.update(unit(1060, 1));
        list.update(unit(1051, 1));
        list.update(unit(1050, 1));
        list.update(unit(1041, 1));
        list.update(unit(1040, 1));
        list.update(unit(1035, 1));
        list.update(unit(1034, 1));
        list.update(unit(1025, 1));
        list.update(unit(1024, 1));
        list.update(unit(1001, 1));
        list.update(unit(1000, 1));
        assert list.x10.size() == 6;

        // fix error
        list.fix(Num.of(1051));
        assert list.x10.size() == 5;

        // fix error : remaining
        list.fix(Num.of(1050));
        assert list.x10.size() == 5;

        // fix error : multiple
        list.fix(Num.of(1038));
        assert list.x10.size() == 3;

        // fix error : overlap
        list.fix(Num.of(1030));
        assert list.x10.size() == 2;

        // fix error : not exist
        list.fix(Num.of(1010));
        assert list.x10.size() == 1;
    }

    @Test
    public void sellGroupFix() throws Exception {
        OrderBookList list = new OrderBookList(Side.SELL, true);
        list.update(unit(1061, 1));
        list.update(unit(1060, 1));
        list.update(unit(1043, 1));
        list.update(unit(1044, 1));
        list.update(unit(1035, 1));
        list.update(unit(1034, 1));
        list.update(unit(1021, 1));
        list.update(unit(1020, 1));
        list.update(unit(1011, 1));
        list.update(unit(1010, 1));
        list.update(unit(1001, 1));
        list.update(unit(1000, 1));
        assert list.x10.size() == 6;

        // fix error
        list.fix(Num.of(1010));
        assert list.x10.size() == 5;

        // fix error : remaining
        list.fix(Num.of(1011));
        assert list.x10.size() == 5;

        // fix error : multiple
        list.fix(Num.of(1031));
        assert list.x10.size() == 3;

        // fix error : overlap
        list.fix(Num.of(1038));
        assert list.x10.size() == 2;

        // fix error : not exist
        list.fix(Num.of(1050));
        assert list.x10.size() == 1;
    }

    @Test
    public void buyBestPrice() throws Exception {
        OrderBookList list = new OrderBookList(Side.SELL, true);
        list.update(unit(1093, 1));
        list.update(unit(1077, 1));
        list.update(unit(1051, 2));
        list.update(unit(1035, 4));
        list.update(unit(1024, 1));
        list.update(unit(1013, 1));
        list.update(unit(1001, 7));
        list.update(unit(1000, 30)); // total 47
        assert list.computeBestPrice(Num.of(1000), Num.of(1999)).is(1002);
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
