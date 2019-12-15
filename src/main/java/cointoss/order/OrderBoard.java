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

public class OrderBoard implements Comparable<OrderBoard> {

    /** The board price. */
    public Num price;

    /** The board size. */
    public double size;

    /**
     * For I#make.
     */
    OrderBoard() {
    }

    /**
     * Simple Builder.
     * 
     * @param price A price.
     * @param size A total size.
     */
    public OrderBoard(Num price, double size) {
        this.price = price;
        this.size = size;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int compareTo(OrderBoard o) {
        return price.compareTo(o.price);
    }
}
