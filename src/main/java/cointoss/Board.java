/*
 * Copyright (C) 2017 Nameless Production Committee
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          http://opensource.org/licenses/mit-license.php
 */
package cointoss;

import java.util.ArrayList;
import java.util.List;

import cointoss.util.Num;

/**
 * @version 2017/11/13 22:52:44
 */
public class Board {

    public Num mid_price;

    public List<Unit> bids = new ArrayList();

    public List<Unit> asks = new ArrayList();

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return bids.size() + "  " + asks.size();
    }

    /**
     * @version 2017/11/13 22:53:25
     */
    public static class Unit {

        public Num price;

        public Num size;

        /**
         * {@inheritDoc}
         */
        @Override
        public String toString() {
            return price + "  " + size.format(4);
        }
    }
}
