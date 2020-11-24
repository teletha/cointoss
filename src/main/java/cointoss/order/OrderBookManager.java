/*
 * Copyright (C) 2020 cointoss Development Team
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          https://opensource.org/licenses/MIT
 */
package cointoss.order;

import cointoss.Direction;
import cointoss.Directional;
import cointoss.MarketService;
import cointoss.util.arithmetic.Num;
import kiss.Variable;

/**
 * @version 2018/01/23 14:12:16
 */
public class OrderBookManager {

    /** ASK */
    public final OrderBook shorts;

    /** BID */
    public final OrderBook longs;

    /** The current spread. */
    public final Variable<Num> spread = Variable.of(Num.ZERO);

    /**
     * 
     */
    public OrderBookManager(MarketService service) {
        this.shorts = new OrderBook(service.setting, Direction.SELL);
        this.longs = new OrderBook(service.setting, Direction.BUY);
        shorts.best.observe().combineLatest(longs.best.observe()).to(v -> spread.set(v.ⅰ.price.minus(v.ⅱ.price)));
    }

    /**
     * Retrieve the {@link OrderBook} for {@link Direction}.
     * 
     * @param side
     * @return
     */
    public OrderBook bookFor(Directional side) {
        return side.isBuy() ? longs : shorts;
    }

    /**
     * Compute the current spread.
     * 
     * @return
     */
    public Num spread() {
        if (shorts.best.isAbsent() || longs.best.isAbsent()) {
            return Num.ZERO;
        } else {
            return shorts.best.v.price.minus(longs.best.v.price);
        }
    }

    /**
     * @param side
     * @param price
     * @param threshold
     * @param diff
     * @return
     */
    public Num computeBestPrice(Directional side, Num price, Num threshold, Num diff) {
        if (threshold.isZero()) {
            return price;
        }
        return side.isBuy() ? longs.computeBestPrice(price, threshold, diff) : shorts.computeBestPrice(price, threshold, diff);
    }

    /**
     * It finds the largest order in the currently selected OrderBook within the specified price
     * range.
     * 
     * @param lowerPrice
     * @param upperPrice
     * @return
     */
    public OrderBookPage findLargestOrder(double lowerPrice, double upperPrice) {
        return findLargestOrder(Num.of(lowerPrice), Num.of(upperPrice));
    }

    /**
     * It finds the largest order in the currently selected OrderBook within the specified price
     * range.
     * 
     * @param lowerPrice
     * @param upperPrice
     * @return
     */
    public OrderBookPage findLargestOrder(Num lowerPrice, Num upperPrice) {
        OrderBookPage inLong = longs.findLargestOrder(lowerPrice, upperPrice);
        OrderBookPage inShort = shorts.findLargestOrder(lowerPrice, upperPrice);

        return inLong.size < inShort.size ? inShort : inLong;
    }
}