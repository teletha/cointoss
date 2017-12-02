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

import cointoss.Side;
import cointoss.util.Num;

/**
 * @version 2017/12/01 1:10:57
 */
public class OrderBook {

    /** ASK */
    public final OrderBookList shorts = new OrderBookList(Side.SELL);

    /** BID */
    public final OrderBookList longs = new OrderBookList(Side.BUY);

    /**
     * @param side
     * @param price
     * @param threshold
     * @param diff
     * @return
     */
    public Num computeBestPrice(Side side, Num price, Num threshold, Num diff) {
        return side.isBuy() ? longs.computeBestPrice(price, threshold, diff) : shorts.computeBestPrice(price, threshold, diff);
    }
}
