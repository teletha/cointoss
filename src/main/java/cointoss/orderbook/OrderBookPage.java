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

public final class OrderBookPage {

    /** The board price. */
    public double price;

    /** The board size. */
    public float size;

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
    OrderBookPage(double price, float size) {
        this.price = price;
        this.size = size;
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