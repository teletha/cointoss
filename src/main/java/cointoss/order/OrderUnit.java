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
 * @version 2017/11/13 22:53:25
 */
public class OrderUnit {

    public Num price;

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
     * Create price scaled unit.
     * 
     * @param scale
     * @return
     */
    public OrderUnit scale(int scale) {
        return new OrderUnit(price.scaleDown(scale), size);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return price + "  " + size.format(4);
    }
}