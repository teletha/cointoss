/*
 * Copyright (C) 2021 cointoss Development Team
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          https://opensource.org/licenses/MIT
 */
package cointoss.orderbook;

import com.google.common.annotations.VisibleForTesting;

import cointoss.Direction;
import cointoss.Directional;
import cointoss.MarketService;
import cointoss.util.arithmetic.Num;
import kiss.Disposable;
import kiss.Signal;

public class OrderBookManager implements Disposable {

    /** ASK */
    public final OrderBook shorts;

    /** BID */
    public final OrderBook longs;

    /** The initial price range. */
    private final float initialRange;

    /** The price range for group. */
    private float range;

    /**
     * Expose to test.
     * 
     * @param service
     */
    @VisibleForTesting
    OrderBookManager(MarketService service) {
        this(service, Signal.never());
    }

    /**
     * 
     */
    public OrderBookManager(MarketService service, Signal<Num> fixPageByPrice) {
        this.shorts = new OrderBook(service.setting, Direction.SELL);
        this.longs = new OrderBook(service.setting, Direction.BUY);
        this.initialRange = this.range = service.setting.base.minimumSize.floatValue();

        // orderbook management
        service.add(service.orderBookRealtimely().to(board -> {
            if (service.supportOrderBookFix()) {
                shorts.fix(board.bestAsk());
                longs.fix(board.bestBid());
            }
            shorts.update(board);
            longs.update(board);
        }));
        service.add(fixPageByPrice.to(price -> {
            shorts.fix(price.doubleValue());
            longs.fix(price.doubleValue());
        }));
    }

    /**
     * Retrieve the {@link OrderBook} by {@link Direction}.
     * 
     * @param side A target direction.
     * @return The associated {@link OrderBook}.
     */
    public OrderBook by(Directional side) {
        return side.isBuy() ? longs : shorts;
    }

    /**
     * @param range
     */
    public void groupBy(float range) {
        this.range = range <= initialRange ? initialRange : range;
        shorts.groupBy(this.range);
        longs.groupBy(this.range);
    }

    /**
     * Get the terminal price if you are representing a price range. In the buyboard, it represents
     * the lowest price in the price range, and in the sellboard, it represents the highest price in
     * the price range.
     * 
     * @return
     */
    public double ranged() {
        return range;
    }

    /**
     * Compute the current spread.
     * 
     * @return
     */
    public double spread() {
        if (shorts.best.isAbsent() || longs.best.isAbsent()) {
            return 0;
        } else {
            return shorts.best.v.price - longs.best.v.price;
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

    /**
     * {@inheritDoc}
     */
    @Override
    public void vandalize() {
    }
}