/*
 * Copyright (C) 2017 Nameless Production Committee
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          http://opensource.org/licenses/mit-license.php
 */
package cointoss.market.bitflyer;

import java.util.ArrayList;
import java.util.List;

/**
 * @version 2017/06/18 17:04:28
 */
public class BoardInfo {

    public float mid_price;

    public List<Order> bids = new ArrayList();

    public List<Order> asks = new ArrayList();

    /**
     * @version 2017/06/18 17:05:21
     */
    public static class Order {

        public float price;

        public float size;

        /**
         * {@inheritDoc}
         */
        @Override
        public String toString() {
            return price + "   " + size;
        }
    }
}
