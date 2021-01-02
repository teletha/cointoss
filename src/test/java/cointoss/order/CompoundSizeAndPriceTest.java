/*
 * Copyright (C) 2021 cointoss Development Team
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          https://opensource.org/licenses/MIT
 */
package cointoss.order;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import cointoss.trade.extension.PricePart;
import cointoss.trade.extension.SidePart;
import cointoss.trade.extension.SizePart;
import cointoss.trade.extension.TradeTest;
import cointoss.verify.VerifiableMarketService;

@Disabled
class CompoundSizeAndPriceTest {

    @TradeTest
    void addCompletelyExecuted(SidePart side, SizePart size, PricePart price) {
        OrderManager orders = new OrderManager(new VerifiableMarketService());

        orders.update(Order.with.direction(side, size).price(price.entry).id("A").executedSize(size));
        assert orders.compoundSize.v.is(side.sign * size.num);
        assert orders.compoundPrice.v.is(price.entry);
    }

    @TradeTest
    void addPartiallyExecuted(SidePart side, SizePart size, PricePart price) {
        OrderManager orders = new OrderManager(new VerifiableMarketService());

        orders.update(Order.with.direction(side, size).price(price.entry).id("A").executedSize(size.half));
        assert orders.compoundSize.v.is(side.sign * size.half);
        assert orders.compoundPrice.v.is(price.entry);
    }

    @TradeTest
    void addNoExecuted(SidePart side, SizePart size, PricePart price) {
        OrderManager orders = new OrderManager(new VerifiableMarketService());

        orders.update(Order.with.direction(side, size).price(price.entry).id("A"));
        assert orders.compoundSize.v.is(0);
        assert orders.compoundPrice.v.is(0);
    }

    @TradeTest
    void updateCompletelyExecuted(SidePart side, SizePart size, PricePart price) {
        OrderManager orders = new OrderManager(new VerifiableMarketService());

        orders.update(Order.with.direction(side, size).price(price.entry).id("A"));
        orders.update(Order.with.direction(side, size).price(price.entry).id("A").executedSize(size));
        assert orders.compoundSize.v.is(side.sign * size.num);
        assert orders.compoundPrice.v.is(price.entry);
    }

    @TradeTest
    void updatePartiallyExecuted(SidePart side, SizePart size, PricePart price) {
        OrderManager orders = new OrderManager(new VerifiableMarketService());

        orders.update(Order.with.direction(side, size).price(price.entry).id("A"));
        orders.update(Order.with.direction(side, size).price(price.entry).id("A").executedSize(size.half));
        assert orders.compoundSize.v.is(side.sign * size.half);
        assert orders.compoundPrice.v.is(price.entry);
    }

    @TradeTest
    void updatePartiallyCompletelyExecuted(SidePart side, SizePart size, PricePart price) {
        OrderManager orders = new OrderManager(new VerifiableMarketService());

        orders.update(Order.with.direction(side, size).price(price.entry).id("A").executedSize(size.half));
        orders.update(Order.with.direction(side, size).price(price.entry).id("A").executedSize(size));
        assert orders.compoundSize.v.is(side.sign * size.num);
        assert orders.compoundPrice.v.is(price.entry);
    }

    @TradeTest
    void addMultipleCompletelyExecuteds(SidePart side, SizePart size, PricePart price) {
        OrderManager orders = new OrderManager(new VerifiableMarketService());

        orders.update(Order.with.direction(side, size).price(price.entry).id("A").executedSize(size));
        orders.update(Order.with.direction(side, size).price(price.entry).id("B").executedSize(size));
        orders.update(Order.with.direction(side, size).price(price.entry).id("C").executedSize(size));
        assert orders.compoundSize.v.is(side.sign * size.num * 3);
        assert orders.compoundPrice.v.is(price.entry);
    }

    @TradeTest
    void addMultiplePartiallyExecuteds(SidePart side, SizePart size, PricePart price) {
        OrderManager orders = new OrderManager(new VerifiableMarketService());

        orders.update(Order.with.direction(side, size).price(price.entry).id("A").executedSize(size.half));
        orders.update(Order.with.direction(side, size).price(price.entry).id("B").executedSize(size.half));
        orders.update(Order.with.direction(side, size).price(price.entry).id("C").executedSize(size.half));
        assert orders.compoundSize.v.is(side.sign * size.half * 3);
        assert orders.compoundPrice.v.is(price.entry);
    }

    @TradeTest
    void addMultipleNoExecuteds(SidePart side, SizePart size, PricePart price) {
        OrderManager orders = new OrderManager(new VerifiableMarketService());

        orders.update(Order.with.direction(side, size).price(price.entry).id("A"));
        orders.update(Order.with.direction(side, size).price(price.entry).id("B"));
        orders.update(Order.with.direction(side, size).price(price.entry).id("C"));
        assert orders.compoundSize.v.is(0);
        assert orders.compoundPrice.v.is(0);
    }

    @TradeTest
    void variousPrice(SidePart side, SizePart size, PricePart price) {
        OrderManager orders = new OrderManager(new VerifiableMarketService());

        orders.update(Order.with.direction(side, size).price(price.entry * 1).id("A").executedSize(size));
        orders.update(Order.with.direction(side, size).price(price.entry * 2).id("B").executedSize(size));
        orders.update(Order.with.direction(side, size).price(price.entry * 3).id("C").executedSize(size));
        assert orders.compoundSize.v.is(side.sign * size.num * 3);
        assert orders.compoundPrice.v.is(price.entry * 2);
    }

    @TradeTest
    void variousPriceAndSize(SidePart side, SizePart size, PricePart price) {
        OrderManager orders = new OrderManager(new VerifiableMarketService());

        orders.update(Order.with.direction(side, size.num * 1).price(price.entry * 1).id("A").executedSize(size.num * 1));
        orders.update(Order.with.direction(side, size.num * 2).price(price.entry * 2).id("B").executedSize(size.num * 2));
        orders.update(Order.with.direction(side, size.num * 4).price(price.entry * 4).id("C").executedSize(size.num * 4));
        assert orders.compoundSize.v.is(side.sign * size.num * 7);
        assert orders.compoundPrice.v.is(price.entry * 3);
    }

    @TradeTest
    void variousSide(SidePart side, SizePart size, PricePart price) {
        OrderManager orders = new OrderManager(new VerifiableMarketService());

        orders.update(Order.with.direction(side, size).price(price.entry).id("A").executedSize(size));
        orders.update(Order.with.direction(side.inverse(), size.half).price(price.entry).id("B").executedSize(size.half));
        assert orders.compoundSize.v.is(side.sign * size.half);
        assert orders.compoundPrice.v.is(price.entry);
    }

    @TradeTest
    void variousSideSizePrice(SidePart side, SizePart size, PricePart price) {
        OrderManager orders = new OrderManager(new VerifiableMarketService());

        orders.update(Order.with.direction(side, size).price(price.entry).id("A").executedSize(size));
        orders.update(Order.with.direction(side.inverse(), size.half).price(price.entry * 2).id("B").executedSize(size.half));
        assert orders.compoundSize.v.is(side.sign * size.half);
        assert orders.compoundPrice.v.is(price.entry);
    }

    @TradeTest
    void variousSideSameSize(SidePart side, SizePart size, PricePart price) {
        OrderManager orders = new OrderManager(new VerifiableMarketService());

        orders.update(Order.with.direction(side, size).price(price.entry).id("A").executedSize(size));
        orders.update(Order.with.direction(side.inverse(), size).price(price.entry).id("B").executedSize(size));
        assert orders.compoundSize.v.is(0);
        assert orders.compoundPrice.v.is(0);
    }

    @Test
    void complex01() {
        OrderManager orders = new OrderManager(new VerifiableMarketService());

        orders.update(Order.with.buy(2).price(10).id("A").executedSize(2));
        assert orders.compoundSize.v.is(2);
        assert orders.compoundPrice.v.is(10);

        orders.update(Order.with.sell(1).price(20).id("B").executedSize(1));
        assert orders.compoundSize.v.is(1);
        assert orders.compoundPrice.v.is(10);

        orders.update(Order.with.buy(1).price(30).id("C").executedSize(1));
        assert orders.compoundSize.v.is(2);
        assert orders.compoundPrice.v.is(20);

        orders.update(Order.with.sell(3).price(10).id("D").executedSize(3));
        assert orders.compoundSize.v.is(-1);
        assert orders.compoundPrice.v.is(10);

        orders.update(Order.with.sell(1).price(2).id("E").executedSize(1));
        assert orders.compoundSize.v.is(-2);
        assert orders.compoundPrice.v.is(6);

        orders.update(Order.with.buy(2).price(10).id("F").executedSize(2));
        assert orders.compoundSize.v.is(0);
        assert orders.compoundPrice.v.is(0);
    }

    @TradeTest
    void complex02(SidePart side) {
        OrderManager orders = new OrderManager(new VerifiableMarketService());

        orders.update(Order.with.direction(side, 2).price(10).id("A").executedSize(1));
        assert orders.compoundSize.v.is(side.sign * 1);
        assert orders.compoundPrice.v.is(10);

        orders.update(Order.with.direction(side.inverse(), 1).price(20).id("B").executedSize(1));
        assert orders.compoundSize.v.is(0);
        assert orders.compoundPrice.v.is(0);

        orders.update(Order.with.direction(side, 2).price(10).id("A").executedSize(2));
        assert orders.compoundSize.v.is(side.sign * 1);
        assert orders.compoundPrice.v.is(10);

        orders.update(Order.with.direction(side.inverse(), 4).price(20).id("C").executedSize(3));
        assert orders.compoundSize.v.is(-side.sign * 2);
        assert orders.compoundPrice.v.is(20);

        orders.update(Order.with.direction(side.inverse(), 4).price(20).id("C").executedSize(4));
        assert orders.compoundSize.v.is(-side.sign * 3);
        assert orders.compoundPrice.v.is(20);
    }

    @Disabled
    @TradeTest
    void complex03(SidePart side) {
        OrderManager orders = new OrderManager(new VerifiableMarketService());

        orders.update(Order.with.direction(side, 2).price(10).id("A"));
        assert orders.compoundSize.v.is(0);
        assert orders.compoundPrice.v.is(0);

        orders.update(Order.with.direction(side, 2).price(10).id("A").executedSize(1));
        assert orders.compoundSize.v.is(side.sign * 1);
        assert orders.compoundPrice.v.is(10);

        orders.update(Order.with.direction(side, 2).price(12).id("A").executedSize(2));
        assert orders.compoundSize.v.is(side.sign * 2);
        assert orders.compoundPrice.v.is(11);
    }
}