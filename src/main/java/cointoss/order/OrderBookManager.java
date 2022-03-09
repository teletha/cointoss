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

        // orderbook management
        service.add(service.orderBookRealtimely().to(board -> {
            if (board.clearInside) {
                shorts.fix(board.asks.get(board.asks.size() - 1).price);
                longs.fix(board.bids.get(board.bids.size() - 1).price);
            }
            shorts.update(board.asks);
            longs.update(board.bids);
        }));
        service.add(fixPageByPrice.to(price -> {
            shorts.fix(price);
            longs.fix(price);
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
     * Compute the current spread.
     * 
     * @return
     */
    public double spread() {
        if (shorts.best.isAbsent() || longs.best.isAbsent()) {
            return 0;
        } else {
            return shorts.best.v.price.doubleValue() - longs.best.v.price.doubleValue();
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