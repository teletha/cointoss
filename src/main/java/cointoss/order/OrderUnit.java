/*
 * Copyright (C) 2017 Nameless Production Committee
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          http://opensource.org/licenses/mit-license.php
 */
package cointoss.order;

import cointoss.util.Num;

/**
 * Immutable.
 * 
 * @version 2017/11/13 22:53:25
 */
public class OrderUnit {

    /** IMMUTABLE */
    public Num price;

    /** IMMUTABLE */
    public Num size;

    /** IMMUTABLE */
    public Num total;

    private OrderUnit() {

    }

    /**
     * @param price
     * @param size
     */
    public OrderUnit(Num price, Num size) {
        this(price, size, Num.ZERO);
    }

    /**
     * @param price
     * @param size
     */
    private OrderUnit(Num price, Num size, Num total) {
        this.price = price;
        this.size = size;
        this.total = total;
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
     * @param total2
     * @return
     */
    public OrderUnit total(Num total) {
        return new OrderUnit(price, size, total);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return price + "  " + size.format(4);
    }
}