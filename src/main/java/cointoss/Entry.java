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

import eu.verdelhan.ta4j.Decimal;

/**
 * @version 2017/09/11 2:26:42
 */
public class Entry implements Directional {

    /** The entry side. */
    public final Side side;

    /** The entry size. */
    public final Decimal size;

    /** The entry average price. */
    public final Decimal price;

    /** The target market. */
    private final Market market;

    /** The entry order. */
    private final Order entry;

    /** The remaining position size. */
    private Decimal remaining;

    /**
     * @param side
     * @param size
     * @param price
     */
    public Entry(Market market, Side side, Decimal size, Decimal price) {
        this.market = market;
        this.side = side;
        this.size = size;
        this.price = price;
        this.entry = Order.limit(side, size, price);
        this.remaining = Decimal.ZERO;
    }

    /**
     * Return remaining position size.
     * 
     * @return
     */
    public Decimal remaining() {
        return remaining;
    }

    /**
     * Request exit order.
     * 
     * @param size
     */
    public void exitMarket(Decimal size) {
        Order.market(side.inverse(), size).with(entry).entryTo(market);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Side side() {
        return side;
    }
}
