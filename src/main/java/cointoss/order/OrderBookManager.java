/*
 * Copyright (C) 2018 CoinToss Development Team
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          https://opensource.org/licenses/MIT
 */
package cointoss.order;

import cointoss.Directional;
import cointoss.MarketService;
import cointoss.Side;
import cointoss.util.Num;
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
        this.shorts = new OrderBook(Side.SELL, service.baseCurrencyMinimumBidPrice(), service.orderBookGroupRanges());
        this.longs = new OrderBook(Side.BUY, service.baseCurrencyMinimumBidPrice(), service.orderBookGroupRanges());
        shorts.best.observe().combineLatest(longs.best.observe()).to(v -> spread.set(v.ⅰ.price.minus(v.ⅱ.price)));
    }

    /**
     * Retrieve the {@link OrderBook} for {@link Side}.
     * 
     * @param side
     * @return
     */
    public OrderBook bookFor(Side side) {
        return side.isBuy() ? longs : shorts;
    }

    /**
     * Compute the current spread.
     * 
     * @return
     */
    public Num spread() {
        return shorts.best.v.price.minus(longs.best.v.price);
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
}
