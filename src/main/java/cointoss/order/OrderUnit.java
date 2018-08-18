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

import cointoss.util.Num;

/**
 * Immutable.
 * 
 * @version 2017/11/13 22:53:25
 */
public class OrderUnit implements Comparable<OrderUnit> {

    /** IMMUTABLE */
    public Num price;

    /** IMMUTABLE */
    public Num size;

    /**
     * @param price
     * @param size
     */
    public OrderUnit(Num price, Num size) {
        this.price = price;
        this.size = size;
    }

    public Num price() {
        return price;
    }

    public Num size() {
        return size;
    }

    /**
     * Create new Unit.
     * 
     * @param size
     * @return
     */
    public OrderUnit size(Num size) {
        return new OrderUnit(price, size);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return price + "  " + size.format(4);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int compareTo(OrderUnit o) {
        return price.compareTo(o.price);
    }
}
