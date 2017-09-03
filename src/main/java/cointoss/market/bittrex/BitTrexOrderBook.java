/*
 * Copyright (C) 2017 Nameless Production Committee
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          http://opensource.org/licenses/mit-license.php
 */
package cointoss.market.bittrex;

import java.util.ArrayList;
import java.util.List;

import cointoss.Amount;

/**
 * @version 2017/09/01 11:32:57
 */
public class BitTrexOrderBook {

    public List<Book> sell = new ArrayList();

    public List<Book> buy = new ArrayList();

    public Book bid() {
        return buy.get(0);
    }

    public Book ask() {
        return sell.get(0);
    }

    public Amount middleBid() {
        return bid().Rate.multiply(4).plus(ask().Rate).divide(5);
    }

    public Amount middleAsk() {
        return ask().Rate.multiply(4).plus(bid().Rate).divide(5);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return "BitTrexOrderBook [sell=" + sell + ", buy=" + buy + "]";
    }

    /**
     * @version 2017/09/01 11:33:17
     */
    public static class Book {

        public Amount Quantity;

        public Amount Rate;

        /**
         * {@inheritDoc}
         */
        @Override
        public String toString() {
            return "Book [Quantity=" + Quantity + ", Rate=" + Rate + "]";
        }
    }
}
