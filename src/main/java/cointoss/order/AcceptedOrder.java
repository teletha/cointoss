/*
 * Copyright (C) 2018 Nameless Production Committee
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          http://opensource.org/licenses/mit-license.php
 */
package cointoss.order;

import java.time.ZonedDateTime;

import cointoss.Side;
import cointoss.util.Num;

/**
 * @version 2018/07/07 15:00:31
 */
public class AcceptedOrder {

    /** The order identifier. */
    public final String id;

    /** The order {@link Side}. */
    public final Side side;

    /** The average price. */
    public final Num price;

    /** The total size. */
    public final Num size;

    /** The {@link OrderType}. */
    public final OrderType type;

    /** The order accepted date-time. */
    public final ZonedDateTime accepted;

    /** The order expired date-time. */
    public final ZonedDateTime expired;

    /**
     * New {@link AcceptedOrder}.
     * 
     * @param id
     * @param side
     * @param price
     * @param size
     * @param type
     * @param accepted
     * @param expired
     */
    public AcceptedOrder(String id, Side side, Num price, Num size, OrderType type, ZonedDateTime accepted, ZonedDateTime expired) {
        this.id = id;
        this.side = side;
        this.price = price;
        this.size = size;
        this.type = type;
        this.accepted = accepted;
        this.expired = expired;
    }
}
