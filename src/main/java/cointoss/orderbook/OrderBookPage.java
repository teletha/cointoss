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

public final class OrderBookPage implements Comparable<OrderBookPage> {

    /** The board price. */
    public double price;

    /** The board size. */
    public float size;

    /** The price range. */
    private final float range;

    /**
     * For I#make.
     */
    OrderBookPage() {
        range = 0;
    }

    /**
     * Simple Builder.
     * 
     * @param price A price.
     * @param size A total size.
     */
    public OrderBookPage(double price, float size) {
        this(price, size, 0);
    }

    /**
     * Simple Builder.
     * 
     * @param price A price.
     * @param size A total size.
     */
    OrderBookPage(double price, float size, float range) {
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
    public double rangedPrice() {
        return price + range;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int compareTo(OrderBookPage o) {
        return Double.compare(price, o.price);
    }

    /**
     * Expose to test.
     */
    boolean is(double price, double size) {
        return this.price == price && size == size;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return "OrderBookPage [price=" + price + ", size=" + size + "]";
    }
}