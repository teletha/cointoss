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

import java.time.LocalDateTime;

/**
 * @version 2017/08/19 18:48:34
 */
public class OrderAndExecution implements Directional {

    public final Order o;

    public final Execution e;

    /** The target market. */
    private final Market market;

    /**
     * @param order
     * @param exe
     */
    OrderAndExecution(Order order, Execution exe, Market market) {
        this.o = order;
        this.e = exe;
        this.market = market;
    }

    /**
     * Cancel the current order.
     * 
     * @param message
     */
    public void clear(String message) {
        market.cancel(o).to(id -> {
            o.entry.description(message);
    
            Order.market(o.side(), o.outstanding_size).with(o.entry).description(message).entryTo(market).to();
        });
    }

    /**
     * @param time
     * @return
     */
    public boolean isAfter(LocalDateTime time) {
        return e.exec_date.isAfter(time);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Side side() {
        return o.side();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return o + "\t\t" + e;
    }
}
