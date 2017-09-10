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

import java.time.ZonedDateTime;

import eu.verdelhan.ta4j.Decimal;

/**
 * @version 2017/08/19 18:48:34
 */
public class OrderAndExecution implements Directional {

    public final Order order;

    public final Execution e;

    /** The target market. */
    private final Market market;

    /**
     * @param order
     * @param exe
     */
    OrderAndExecution(Order order, Execution exe, Market market) {
        this.order = order;
        this.e = exe;
        this.market = market;
    }

    /**
     * Cancel the current order.
     * 
     * @param message
     */
    public void clear(String message) {
        market.cancel(order).to(id -> {
            order.entry.description(message);

            Order.market(order.side(), order.outstanding_size).with(order.entry).description(message).entryTo(market).to();
        });
    }

    /**
     * @param time
     * @return
     */
    public boolean isAfter(ZonedDateTime time) {
        return e.exec_date.isAfter(time);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Side side() {
        return order.side();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return order + "\t\t" + e;
    }

    /**
     * Calculate the upforward order price.
     * 
     * @param size
     * @return
     */
    public Decimal priceUp(int size) {
        return order.average_price.plus(order, size);
    }
}
