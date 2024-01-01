/*
 * Copyright (C) 2024 The COINTOSS Development Team
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          https://opensource.org/licenses/MIT
 */
package cointoss.orderbook;

import static cointoss.orderbook.OrderBookChanges.*;
import static cointoss.util.arithmetic.Num.ONE;

import java.util.Collection;

import org.junit.jupiter.api.Test;

import com.google.common.collect.Iterables;

import cointoss.Currency;
import cointoss.Direction;
import cointoss.MarketSetting;
import cointoss.util.arithmetic.Num;

class OrderBookTest {

    private MarketSetting setting = MarketSetting.with.spot().target(Currency.UNKNOWN.minimumSize(1)).base(Currency.UNKNOWN.minimumSize(1));

    private OrderBookPage at(int index, Collection<OrderBookPage> pages) {
        return Iterables.get(pages, index);
    }

    @Test
    void buy() {
        OrderBook book = new OrderBook(setting, Direction.BUY);
        Collection<OrderBookPage> list = book.groupBy(1);

        // add
        book.update(singleBid(1000, 1));
        assert at(0, list).price == 1000;

        book.update(singleBid(1002, 1));
        assert at(0, list).price == 1002;
        assert at(1, list).price == 1000;

        book.update(singleBid(1001, 1));
        assert at(0, list).price == 1002;
        assert at(1, list).price == 1001;
        assert at(1, list).size == 1d;
        assert at(2, list).price == 1000;

        // replace
        book.update(singleBid(1001, 2));
        assert at(0, list).price == 1002;
        assert at(1, list).price == 1001;
        assert at(1, list).size == 2d;
        assert at(2, list).price == 1000;

        // remove
        book.update(singleBid(1000, 0));
        assert list.size() == 2;
        assert at(0, list).price == 1002;
        assert at(1, list).price == 1001;
    }

    @Test
    void sell() {
        OrderBook book = new OrderBook(setting, Direction.SELL);
        Collection<OrderBookPage> list = book.groupBy(1);

        book.update(singleAsk(1000, 1));
        assert at(0, list).price == 1000;

        book.update(singleAsk(1002, 1));
        assert at(0, list).price == 1000;
        assert at(1, list).price == 1002;

        book.update(singleAsk(1001, 1));
        assert at(0, list).price == 1000;
        assert at(1, list).price == 1001;
        assert at(1, list).size == 1d;
        assert at(2, list).price == 1002;

        // replace
        book.update(singleAsk(1001, 2));
        assert at(0, list).price == 1000;
        assert at(1, list).price == 1001;
        assert at(1, list).size == 2d;
        assert at(2, list).price == 1002;

        // remove
        book.update(singleAsk(1000, 0));
        assert list.size() == 2;
        assert at(0, list).price == 1001;
        assert at(1, list).price == 1002;
    }

    @Test
    void buyFix() {
        OrderBook book = new OrderBook(setting, Direction.BUY);
        Collection<OrderBookPage> list = book.groupBy(1);

        book.update(singleBid(1007, 1));
        book.update(singleBid(1006, 1));
        book.update(singleBid(1005, 1));
        book.update(singleBid(1004, 1));
        book.update(singleBid(1003, 1));
        book.update(singleBid(1000, 1));
        assert list.size() == 6;

        // fix error
        book.fix(1006);
        assertList(list, 0, 1006, 1, 1);
        assertList(list, 1, 1005, 1, 2);
        assertList(list, 2, 1004, 1, 3);
        assert list.size() == 5;

        // fix error : multiple
        book.fix(1004);
        assertList(list, 0, 1004, 1, 1);
        assertList(list, 1, 1003, 1, 2);
        assertList(list, 2, 1000, 1, 3);
        assert list.size() == 3;

        // fix error : not exits
        book.fix(1002);
        assertList(list, 0, 1000, 1, 1);
        assert list.size() == 1;
    }

    @Test
    void sellFix() {
        OrderBook book = new OrderBook(setting, Direction.SELL);
        Collection<OrderBookPage> list = book.groupBy(1);

        book.update(singleAsk(1007, 1));
        book.update(singleAsk(1004, 1));
        book.update(singleAsk(1003, 1));
        book.update(singleAsk(1002, 1));
        book.update(singleAsk(1001, 1));
        book.update(singleAsk(1000, 1));
        assert list.size() == 6;

        // fix error
        book.fix(1001);
        assert list.size() == 5;

        // fix error : multiple
        book.fix(1003);
        assert list.size() == 3;

        // fix error : not exits
        book.fix(1005);
        assert list.size() == 1;
    }

    @Test
    void buyGroup() {
        OrderBook book = new OrderBook(setting, Direction.BUY);

        book.update(singleBid(1000, 1));
        assertList(book.groupBy(10), 0, 1000, 1, 1);

        // add
        book.update(singleBid(1009, 1));
        assertList(book.groupBy(10), 0, 1000, 2, 2);

        // minus
        book.update(singleBid(1000, 0));
        assertList(book.groupBy(10), 0, 1000, 1, 1);

        // next group
        book.update(singleBid(1010, 1));
        assertList(book.groupBy(10), 0, 1010, 1, 1);
        assertList(book.groupBy(10), 1, 1000, 1, 2);

        // remove
        book.update(singleBid(1009, 0));
        assertList(book.groupBy(10), 0, 1010, 1, 1);
    }

    @Test
    void sellGroup() {
        OrderBook book = new OrderBook(setting, Direction.SELL);
        book.update(singleAsk(1000, 1));
        assertList(book.groupBy(10), 0, 1000, 1, 1);

        // add
        book.update(singleAsk(1009, 1));
        assertList(book.groupBy(10), 0, 1000, 2, 2);

        // minus
        book.update(singleAsk(1000, 0));
        assertList(book.groupBy(10), 0, 1000, 1, 1);

        // next group
        book.update(singleAsk(1010, 1));
        assertList(book.groupBy(10), 1, 1010, 1, 2);
        assertList(book.groupBy(10), 0, 1000, 1, 1);

        // remove
        book.update(singleAsk(1009, 0));
        assertList(book.groupBy(10), 0, 1010, 1, 1);
    }

    @Test
    void buyGroupFix() {
        OrderBook book = new OrderBook(setting, Direction.BUY);
        book.update(singleBid(1061, 1));
        book.update(singleBid(1060, 1));
        book.update(singleBid(1051, 1));
        book.update(singleBid(1050, 1));
        book.update(singleBid(1041, 1));
        book.update(singleBid(1040, 1));
        book.update(singleBid(1035, 1));
        book.update(singleBid(1034, 1));
        book.update(singleBid(1025, 1));
        book.update(singleBid(1024, 1));
        book.update(singleBid(1001, 1));
        book.update(singleBid(1000, 1));
        assert book.groupBy(10).size() == 6;

        // fix error
        book.fix(1051);
        assert book.groupBy(10).size() == 5;

        // fix error : remaining
        book.fix(1050);
        assert book.groupBy(10).size() == 5;

        // fix error : multiple
        book.fix(1038);
        assert book.groupBy(10).size() == 3;

        // fix error : overlap
        book.fix(1030);
        assert book.groupBy(10).size() == 2;

        // fix error : not exist
        book.fix(1010);
        assert book.groupBy(10).size() == 1;
    }

    @Test
    void sellGroupFix() {
        OrderBook book = new OrderBook(setting, Direction.SELL);
        book.update(singleAsk(1061, 1));
        book.update(singleAsk(1060, 1));
        book.update(singleAsk(1043, 1));
        book.update(singleAsk(1044, 1));
        book.update(singleAsk(1035, 1));
        book.update(singleAsk(1034, 1));
        book.update(singleAsk(1021, 1));
        book.update(singleAsk(1020, 1));
        book.update(singleAsk(1011, 1));
        book.update(singleAsk(1010, 1));
        book.update(singleAsk(1001, 1));
        book.update(singleAsk(1000, 1));
        assert book.groupBy(10).size() == 6;

        // fix error
        book.fix(1010);
        assert book.groupBy(10).size() == 5;

        // fix error : remaining
        book.fix(1011);
        assert book.groupBy(10).size() == 5;

        // fix error : multiple
        book.fix(1031);
        assert book.groupBy(10).size() == 3;

        // fix error : overlap
        book.fix(1038);
        assert book.groupBy(10).size() == 2;

        // fix error : not exist
        book.fix(1050);
        assert book.groupBy(10).size() == 1;
    }

    @Test
    void buyBestPrice() {
        Num min = Num.of(1099);

        OrderBook book = new OrderBook(setting, Direction.BUY);
        book.update(singleBid(1093, 1)); // total 1
        book.update(singleBid(1077, 1)); // total 2
        book.update(singleBid(1051, 2)); // total 4
        book.update(singleBid(1035, 4)); // total 8
        book.update(singleBid(1024, 1)); // total 9
        book.update(singleBid(1013, 1)); // total 10
        book.update(singleBid(1001, 7)); // total 17
        book.update(singleBid(1000, 30)); // total 47
        assert book.computeBestPrice(min, Num.of(10), ONE).is(1014);
        assert book.computeBestPrice(min, Num.of(30), ONE).is(1001);
        assert book.computeBestPrice(min, Num.of(4), ONE).is(1052);
    }

    @Test
    void buyBestPriceWithDiffOnly() {
        OrderBook book = new OrderBook(setting, Direction.BUY);
        book.update(singleBid(1000, 1));
        assert book.computeBestPrice(Num.of(1)).is(1001);
        assert book.computeBestPrice(Num.of(2)).is(1002);
        assert book.computeBestPrice(Num.of(20)).is(1020);
        assert book.computeBestPrice(Num.of(100)).is(1100);
    }

    @Test
    void sellBestPrice() {
        Num min = Num.of(1000);

        OrderBook book = new OrderBook(setting, Direction.SELL);
        book.update(singleAsk(1093, 30)); // total 47
        book.update(singleAsk(1077, 7)); // total 17
        book.update(singleAsk(1051, 1)); // total 10
        book.update(singleAsk(1035, 1)); // total 9
        book.update(singleAsk(1024, 4)); // total 8
        book.update(singleAsk(1013, 2)); // total 4
        book.update(singleAsk(1001, 1)); // total 2
        book.update(singleAsk(1000, 1)); // total 1
        assert book.computeBestPrice(min, Num.of(10), ONE).is(1050);
        assert book.computeBestPrice(min, Num.of(30), ONE).is(1092);
        assert book.computeBestPrice(min, Num.of(4), ONE).is(1012);
    }

    @Test
    void sellBestPriceWithDiffOnly() {
        OrderBook book = new OrderBook(setting, Direction.SELL);
        book.update(singleAsk(1000, 1));
        assert book.computeBestPrice(Num.of(1)).is(999);
        assert book.computeBestPrice(Num.of(2)).is(998);
        assert book.computeBestPrice(Num.of(20)).is(980);
        assert book.computeBestPrice(Num.of(100)).is(900);
    }

    @Test
    void predictTakingPriceOnBuy() {
        OrderBook book = new OrderBook(setting, Direction.BUY);
        book.update(singleBid(10, 1));
        book.update(singleBid(12, 1));
        book.update(singleBid(14, 1));
        book.update(singleBid(16, 1));
        book.update(singleBid(18, 1));
        book.update(singleBid(20, 1));

        assert book.predictTakingPrice(1).is(20);
        assert book.predictTakingPrice(2).is(19);
        assert book.predictTakingPrice(3).is(18);
        assert book.predictTakingPrice(4).is(17);
        assert book.predictTakingPrice(5).is(16);
        assert book.predictTakingPrice(6).is(15);
    }

    @Test
    void predictTakingPriceOnSell() {
        OrderBook book = new OrderBook(setting, Direction.SELL);
        book.update(singleAsk(10, 1));
        book.update(singleAsk(12, 1));
        book.update(singleAsk(14, 1));
        book.update(singleAsk(16, 1));
        book.update(singleAsk(18, 1));
        book.update(singleAsk(20, 1));

        assert book.predictTakingPrice(1).is(10);
        assert book.predictTakingPrice(2).is(11);
        assert book.predictTakingPrice(3).is(12);
        assert book.predictTakingPrice(4).is(13);
        assert book.predictTakingPrice(5).is(14);
        assert book.predictTakingPrice(6).is(15);
    }

    /**
     * Helper method to assert.
     * 
     * @param list
     * @param index
     * @param size
     * @param price
     */
    private void assertList(Collection<OrderBookPage> list, int index, int price, int size, int total) {
        OrderBookPage unit = at(index, list);
        assert unit.size == size;
        assert unit.price == price;
    }
}