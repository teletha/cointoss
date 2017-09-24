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

import cointoss.util.Num;

/**
 * @version 2017/08/31 19:11:31
 */
public class BitTrexTicker {

    public Num Bid;

    public Num Ask;

    public Num Last;

    public Num middle() {
        return Bid.plus(Ask).divide(2);
    }

    public Num middleBid() {
        return Bid.plus(Bid).plus(Ask).divide(3);
    }

    public Num middleAsk() {
        return Bid.plus(Ask).plus(Ask).divide(3);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return "BitTrexTicker [Bid=" + Bid + ", Ask=" + Ask + ", Last=" + Last + "]";
    }
}
