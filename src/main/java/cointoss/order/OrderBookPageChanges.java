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

import static java.util.Collections.EMPTY_LIST;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import cointoss.util.arithmetic.Num;
import kiss.JSON;

public class OrderBookPageChanges {

    /**
     * The specific API does not tell you the information that the quantity has reached zero, so you
     * should erase any existing data that is within the range of the retrieved data.
     */
    public boolean clearInside = false;

    /** The list of long orders. */
    public final List<OrderBookPage> bids;

    /** The list of short orders. */
    public final List<OrderBookPage> asks;

    /**
     *  
     */
    public OrderBookPageChanges() {
        this(new ArrayList(4), new ArrayList(4));
    }

    /**
     * Initialization.
     * 
     * @param bids
     * @param asks
     */
    private OrderBookPageChanges(List<OrderBookPage> bids, List<OrderBookPage> asks) {
        this.bids = bids;
        this.asks = asks;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return "OrderBookChange [bids=" + bids + ", asks=" + asks + "]";
    }

    /**
     * Helper method to build the optimized {@link OrderBookPageChanges} from simple JSON data set.
     * 
     * @param bids A bid data.
     * @param asks An ask data.
     * @param priceKey The price key on json.
     * @param sizeKey The size key on json.
     * @return
     */
    public static OrderBookPageChanges byJSON(List<JSON> bids, List<JSON> asks, String priceKey, String sizeKey) {
        return byJSON(bids, asks, priceKey, sizeKey, -1);
    }

    /**
     * Helper method to build the optimized {@link OrderBookPageChanges} from simple JSON data set.
     * 
     * @param bids A bid data.
     * @param asks An ask data.
     * @param priceKey The price key on json.
     * @param sizeKey The size key on json.
     * @return
     */
    public static OrderBookPageChanges byJSON(List<JSON> bids, List<JSON> asks, String priceKey, String sizeKey, int scale) {
        int bidSize = bids.size();
        int askSize = asks.size();
        OrderBookPageChanges changes = byHint(bidSize, askSize);

        if (scale == -1) {
            for (int i = 0; i < bidSize; i++) {
                JSON e = bids.get(i);
                changes.bids.add(new OrderBookPage(e.get(Num.class, priceKey), Float.parseFloat(e.text(sizeKey))));
            }
            for (int i = 0; i < askSize; i++) {
                JSON e = asks.get(i);
                changes.asks.add(new OrderBookPage(e.get(Num.class, priceKey), Float.parseFloat(e.text(sizeKey))));
            }
        } else {
            for (int i = 0; i < bidSize; i++) {
                JSON e = bids.get(i);
                Num price = e.get(Num.class, priceKey);
                Num size = e.get(Num.class, sizeKey).divide(price).scale(scale);
                changes.bids.add(new OrderBookPage(price, size.floatValue()));
            }
            for (int i = 0; i < askSize; i++) {
                JSON e = asks.get(i);
                Num price = e.get(Num.class, priceKey);
                Num size = e.get(Num.class, sizeKey).divide(price).scale(scale);
                changes.asks.add(new OrderBookPage(price, size.floatValue()));
            }
        }

        return changes;
    }

    /**
     * Build the optimized {@link OrderBookPageChanges}.
     * 
     * @param bids An amount of bids.
     * @param asks An amount of asks.
     * @return
     */
    public static OrderBookPageChanges byHint(int bids, int asks) {
        return new OrderBookPageChanges(bids == 0 ? EMPTY_LIST : new ArrayList(bids), asks == 0 ? EMPTY_LIST : new ArrayList(asks));
    }

    /**
     * Build the optimized {@link OrderBookPageChanges} for bid.
     * 
     * @param price A requested price.
     * @param size A requested size.
     * @return
     */
    public static OrderBookPageChanges singleBuy(Num price, float size) {
        return new OrderBookPageChanges(Collections.singletonList(new OrderBookPage(price, size)), EMPTY_LIST);
    }

    /**
     * Build the optimized {@link OrderBookPageChanges} for bid.
     * 
     * @param price A requested price.
     * @param size A requested size.
     * @return
     */
    public static OrderBookPageChanges singleSell(Num price, float size) {
        return new OrderBookPageChanges(EMPTY_LIST, Collections.singletonList(new OrderBookPage(price, size)));
    }
}