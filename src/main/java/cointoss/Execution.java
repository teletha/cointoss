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
import java.time.ZoneId;
import java.time.ZonedDateTime;

import eu.verdelhan.ta4j.Decimal;

/**
 * @version 2017/08/23 8:47:20
 */
public class Execution {

    public long id;

    /** Buyer id of this execution. */
    public String buy_child_order_acceptance_id = "";

    /** Seller id of this execution. */
    public String sell_child_order_acceptance_id = "";

    /** The side */
    public Side side;

    /** price */
    public Decimal price;

    /** size */
    public Decimal size;

    /** size */
    public Decimal cumulativeSize;

    /** date */
    public ZonedDateTime exec_date;

    /** INTERNAL USAGE */
    Order associated;

    /**
     * 
     */
    public Execution() {
    }

    /**
     * @param line
     */
    public Execution(String line) {
        String[] values = line.split(" ");
        id = Long.parseLong(values[0]);
        exec_date = LocalDateTime.parse(values[1]).atZone(ZoneId.systemDefault());
        side = Side.parse(values[2]);
        price = Decimal.valueOf(values[3]);
        size = Decimal.valueOf(values[4]);

        if (5 < values.length) {
            buy_child_order_acceptance_id = values[5];
            sell_child_order_acceptance_id = values[6];
        }
    }

    /**
     * @return
     */
    public boolean isMine() {
        return associated != null;
    }

    /**
     * Calculate the after time.
     * 
     * @param seconds
     * @return
     */
    public ZonedDateTime after(long seconds) {
        return exec_date.plusSeconds(seconds);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return id + " " + exec_date + " " + side
                .mark() + " " + price + " " + size + " " + buy_child_order_acceptance_id + " " + sell_child_order_acceptance_id;
    }
}
