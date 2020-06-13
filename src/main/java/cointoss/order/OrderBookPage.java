/*
 * Copyright (C) 2019 CoinToss Development Team
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          https://opensource.org/licenses/MIT
 */
package cointoss.order;

import cointoss.util.Num;

public class OrderBookPage implements Comparable<OrderBookPage> {

    /** The board price. */
    public Num price;

    /** The board size. */
    public double size;

    /**
     * For I#make.
     */
    OrderBookPage() {
    }

    /**
     * Simple Builder.
     * 
     * @param price A price.
     * @param size A total size.
     */
    public OrderBookPage(Num price, double size) {
        this.price = price;
        this.size = size;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int compareTo(OrderBookPage o) {
        return price.compareTo(o.price);
    }

    /**
     * Test method.
     * 
     * @param price
     * @param size
     * @return
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
