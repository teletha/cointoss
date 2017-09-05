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

import eu.verdelhan.ta4j.Decimal;

/**
 * @version 2017/08/31 19:11:31
 */
public class BitTrexTicker {

    public Decimal Bid;

    public Decimal Ask;

    public Decimal Last;

    public Decimal middle() {
        return Bid.plus(Ask).dividedBy(2);
    }

    public Decimal middleBid() {
        return Bid.plus(Bid).plus(Ask).dividedBy(3);
    }

    public Decimal middleAsk() {
        return Bid.plus(Ask).plus(Ask).dividedBy(3);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return "BitTrexTicker [Bid=" + Bid + ", Ask=" + Ask + ", Last=" + Last + "]";
    }
}
