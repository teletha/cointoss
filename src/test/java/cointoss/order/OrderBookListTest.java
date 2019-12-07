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

import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.Test;

import cointoss.Direction;
import cointoss.MarketSetting;
import cointoss.util.Num;

class OrderBookListTest {

    private MarketSetting setting = MarketSetting.with.baseCurrencyMinimumBidPrice(Num.ONE)
            .targetCurrencyMinimumBidSize(Num.ONE)
            .orderBookGroupRanges(Num.TEN);

    @Test
    void buy() {
        OrderBook list = new OrderBook(setting, Direction.BUY);

        // add
        list.update(unit(1000, 1));
        assert list.base.get(0).price.is(1000);
        // assert list.x1.get(0).total.is(1);

        list.update(unit(1002, 1));
        assert list.base.get(0).price.is(1002);
        // assert list.x1.get(0).total.is(1);
        assert list.base.get(1).price.is(1000);
        // assert list.x1.get(1).total.is(2);

        list.update(unit(1001, 1));
        assert list.base.get(0).price.is(1002);
        // assert list.x1.get(0).total.is(1);
        assert list.base.get(1).price.is(1001);
        assert list.base.get(1).size.is(1);
        // assert list.x1.get(1).total.is(2);
        assert list.base.get(2).price.is(1000);
        // assert list.x1.get(2).total.is(3);

        // replace
        list.update(unit(1001, 2));
        assert list.base.get(0).price.is(1002);
        // assert list.x1.get(0).total.is(1);
        assert list.base.get(1).price.is(1001);
        assert list.base.get(1).size.is(2);
        // assert list.x1.get(1).total.is(3);
        assert list.base.get(2).price.is(1000);
        // assert list.x1.get(2).total.is(4);

        // remove
        list.update(unit(1000, 0));
        assert list.base.size() == 2;
        assert list.base.get(0).price.is(1002);
        // assert list.x1.get(0).total.is(1);
        assert list.base.get(1).price.is(1001);
        // assert list.x1.get(1).total.is(3);

    }

    @Test
    void sell() {
        OrderBook list = new OrderBook(setting, Direction.SELL);
        list.update(unit(1000, 1));
        assert list.base.get(0).price.is(1000);
        // assert list.x1.get(0).total.is(1);

        list.update(unit(1002, 1));
        assert list.base.get(0).price.is(1002);
        // assert list.x1.get(0).total.is(2);
        assert list.base.get(1).price.is(1000);
        // assert list.x1.get(1).total.is(1);

        list.update(unit(1001, 1));
        assert list.base.get(0).price.is(1002);
        // assert list.x1.get(0).total.is(3);
        assert list.base.get(1).price.is(1001);
        assert list.base.get(1).size.is(1);
        // assert list.x1.get(1).total.is(2);
        assert list.base.get(2).price.is(1000);
        // assert list.x1.get(2).total.is(1);

        // replace
        list.update(unit(1001, 2));
        assert list.base.get(0).price.is(1002);
        // assert list.x1.get(0).total.is(4);
        assert list.base.get(1).price.is(1001);
        assert list.base.get(1).size.is(2);
        // assert list.x1.get(1).total.is(3);
        assert list.base.get(2).price.is(1000);
        // assert list.x1.get(2).total.is(1);

        // remove
        list.update(unit(1000, 0));
        assert list.base.size() == 2;
        assert list.base.get(0).price.is(1002);
        // assert list.x1.get(0).total.is(3);
        assert list.base.get(1).price.is(1001);
        // assert list.x1.get(1).total.is(2);
    }

    @Test
    void buyFix() {
        OrderBook list = new OrderBook(setting, Direction.BUY);
        list.update(unit(1007, 1));
        list.update(unit(1006, 1));
        list.update(unit(1005, 1));
        list.update(unit(1004, 1));
        list.update(unit(1003, 1));
        list.update(unit(1000, 1));
        assert list.base.size() == 6;

        // fix error
        list.fix(Num.of(1006));
        assertList(list.base, 0, 1006, 1, 1);
        assertList(list.base, 1, 1005, 1, 2);
        assertList(list.base, 2, 1004, 1, 3);
        assert list.base.size() == 5;

        // fix error : multiple
        list.fix(Num.of(1004));
        assertList(list.base, 0, 1004, 1, 1);
        assertList(list.base, 1, 1003, 1, 2);
        assertList(list.base, 2, 1000, 1, 3);
        assert list.base.size() == 3;

        // fix error : not exits
        list.fix(Num.of(1002));
        assertList(list.base, 0, 1000, 1, 1);
        assert list.base.size() == 1;
    }

    @Test
    void sellFix() {
        OrderBook list = new OrderBook(setting, Direction.SELL);
        list.update(unit(1007, 1));
        list.update(unit(1004, 1));
        list.update(unit(1003, 1));
        list.update(unit(1002, 1));
        list.update(unit(1001, 1));
        list.update(unit(1000, 1));
        assert list.base.size() == 6;

        // fix error
        list.fix(Num.of(1001));
        assert list.base.size() == 5;

        // fix error : multiple
        list.fix(Num.of(1003));
        assert list.base.size() == 3;

        // fix error : not exits
        list.fix(Num.of(1005));
        assert list.base.size() == 1;
    }

    @Test
    void buyGroup() {
        OrderBook list = new OrderBook(setting, Direction.BUY);
        list.update(unit(1000, 1));
        assertList(list.selectBy(Num.TEN), 0, 1000, 1, 1);

        // add
        list.update(unit(1009, 1));
        assertList(list.selectBy(Num.TEN), 0, 1000, 2, 2);

        // minus
        list.update(unit(1000, 0));
        assertList(list.selectBy(Num.TEN), 0, 1000, 1, 1);

        // next group
        list.update(unit(1010, 1));
        assertList(list.selectBy(Num.TEN), 0, 1010, 1, 1);
        assertList(list.selectBy(Num.TEN), 1, 1000, 1, 2);

        // remove
        list.update(unit(1009, 0));
        assertList(list.selectBy(Num.TEN), 0, 1010, 1, 1);
    }

    @Test
    void sellGroup() {
        OrderBook list = new OrderBook(setting, Direction.SELL);
        list.update(unit(1000, 1));
        assertList(list.selectBy(Num.TEN), 0, 1000, 1, 1);

        // add
        list.update(unit(1009, 1));
        assertList(list.selectBy(Num.TEN), 0, 1000, 2, 2);

        // minus
        list.update(unit(1000, 0));
        assertList(list.selectBy(Num.TEN), 0, 1000, 1, 1);

        // next group
        list.update(unit(1010, 1));
        assertList(list.selectBy(Num.TEN), 0, 1010, 1, 2);
        assertList(list.selectBy(Num.TEN), 1, 1000, 1, 1);

        // remove
        list.update(unit(1009, 0));
        assertList(list.selectBy(Num.TEN), 0, 1010, 1, 1);
    }

    @Test
    void buyGroupFix() {
        OrderBook list = new OrderBook(setting, Direction.BUY);
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
        assert list.selectBy(Num.TEN).size() == 6;

        // fix error
        list.fix(Num.of(1051));
        assert list.selectBy(Num.TEN).size() == 5;

        // fix error : remaining
        list.fix(Num.of(1050));
        assert list.selectBy(Num.TEN).size() == 5;

        // fix error : multiple
        list.fix(Num.of(1038));
        assert list.selectBy(Num.TEN).size() == 3;

        // fix error : overlap
        list.fix(Num.of(1030));
        assert list.selectBy(Num.TEN).size() == 2;

        // fix error : not exist
        list.fix(Num.of(1010));
        assert list.selectBy(Num.TEN).size() == 1;
    }

    @Test
    void sellGroupFix() {
        OrderBook list = new OrderBook(setting, Direction.SELL);
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
        assert list.selectBy(Num.TEN).size() == 6;

        // fix error
        list.fix(Num.of(1010));
        assert list.selectBy(Num.TEN).size() == 5;

        // fix error : remaining
        list.fix(Num.of(1011));
        assert list.selectBy(Num.TEN).size() == 5;

        // fix error : multiple
        list.fix(Num.of(1031));
        assert list.selectBy(Num.TEN).size() == 3;

        // fix error : overlap
        list.fix(Num.of(1038));
        assert list.selectBy(Num.TEN).size() == 2;

        // fix error : not exist
        list.fix(Num.of(1050));
        assert list.selectBy(Num.TEN).size() == 1;
    }

    @Test
    void buyBestPrice() {
        Num min = Num.of(1099);

        OrderBook list = new OrderBook(setting, Direction.BUY);
        list.update(unit(1093, 1)); // total 1
        list.update(unit(1077, 1)); // total 2
        list.update(unit(1051, 2)); // total 4
        list.update(unit(1035, 4)); // total 8
        list.update(unit(1024, 1)); // total 9
        list.update(unit(1013, 1)); // total 10
        list.update(unit(1001, 7)); // total 17
        list.update(unit(1000, 30)); // total 47
        assert list.computeBestPrice(min, Num.of(10), Num.ONE).is(1014);
        assert list.computeBestPrice(min, Num.of(30), Num.ONE).is(1001);
        assert list.computeBestPrice(min, Num.of(4), Num.ONE).is(1052);
    }

    @Test
    void buyBestPriceWithDiffOnly() {
        OrderBook list = new OrderBook(setting, Direction.BUY);
        list.update(unit(1000, 1));
        assert list.computeBestPrice(Num.of(1)).is(1001);
        assert list.computeBestPrice(Num.of(2)).is(1002);
        assert list.computeBestPrice(Num.of(20)).is(1020);
        assert list.computeBestPrice(Num.of(100)).is(1100);
    }

    @Test
    void sellBestPrice() {
        Num min = Num.of(1000);

        OrderBook list = new OrderBook(setting, Direction.SELL);
        list.update(unit(1093, 30)); // total 47
        list.update(unit(1077, 7)); // total 17
        list.update(unit(1051, 1)); // total 10
        list.update(unit(1035, 1)); // total 9
        list.update(unit(1024, 4)); // total 8
        list.update(unit(1013, 2)); // total 4
        list.update(unit(1001, 1)); // total 2
        list.update(unit(1000, 1)); // total 1
        assert list.computeBestPrice(min, Num.of(10), Num.ONE).is(1050);
        assert list.computeBestPrice(min, Num.of(30), Num.ONE).is(1092);
        assert list.computeBestPrice(min, Num.of(4), Num.ONE).is(1012);
    }

    @Test
    void sellBestPriceWithDiffOnly() {
        OrderBook list = new OrderBook(setting, Direction.SELL);
        list.update(unit(1000, 1));
        assert list.computeBestPrice(Num.of(1)).is(999);
        assert list.computeBestPrice(Num.of(2)).is(998);
        assert list.computeBestPrice(Num.of(20)).is(980);
        assert list.computeBestPrice(Num.of(100)).is(900);
    }

    /**
     * Helper method to assert.
     * 
     * @param list
     * @param index
     * @param size
     * @param price
     */
    private void assertList(List<OrderUnit> list, int index, int price, int size, int total) {
        OrderUnit unit = list.get(index);
        assert unit.size.is(size);
        assert unit.price.is(price);
        // assert unit.total.is(total);
    }

    /**
     * Helper.
     * 
     * @param price
     * @param size
     * @return
     */
    private List<OrderUnit> unit(int price, int size) {
        return Collections.singletonList(new OrderUnit(Num.of(price), Num.of(size)));
    }

    @Test
    void calculateGroupedPrice() {
        assert OrderBook.calculateGroupedPrice(Num.of(100), Num.of(500)).is(Num.of(0));
        assert OrderBook.calculateGroupedPrice(Num.of(300), Num.of(500)).is(Num.of(0));
        assert OrderBook.calculateGroupedPrice(Num.of(500), Num.of(500)).is(Num.of(500));
        assert OrderBook.calculateGroupedPrice(Num.of(700), Num.of(500)).is(Num.of(500));
        assert OrderBook.calculateGroupedPrice(Num.of(900), Num.of(500)).is(Num.of(500));
        assert OrderBook.calculateGroupedPrice(Num.of(1100), Num.of(500)).is(Num.of(1000));
    }
}
