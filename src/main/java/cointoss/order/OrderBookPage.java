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

import cointoss.util.arithmeric.Num;

public final class OrderBookPage implements Comparable<OrderBookPage> {

    /** The board price. */
    public Num price;

    /** The board size. */
    public double size;

    /** The price range. */
    private final Num range;

    /**
     * For I#make.
     */
    OrderBookPage() {
        range = Num.ZERO;
    }

    /**
     * Simple Builder.
     * 
     * @param price A price.
     * @param size A total size.
     */
    public OrderBookPage(Num price, double size) {
        this(price, size, Num.ZERO);
    }

    /**
     * Simple Builder.
     * 
     * @param price A price.
     * @param size A total size.
     */
    OrderBookPage(Num price, double size, Num range) {
        this.price = price;
        this.size = size;
        this.range = range;
    }

    /**
     * Get the terminal price if you are representing a price range. In the buyboard, it represents
     * the lowest price in the price range, and in the sellboard, it represents the highest price in
     * the price range.
     * 
     * @return
     */
    public Num rangedPrice() {
        return price.plus(range);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int compareTo(OrderBookPage o) {
        return price.compareTo(o.price);
    }

    /**
     * Expose to test.
     */
    boolean is(double price, double size) {
        return this.price.is(price) && size == size;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return "OrderBookPage [price=" + price + ", size=" + size + "]";
    }
}