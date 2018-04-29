/*
 * Copyright (C) 2018 CoinToss Development Team
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          https://opensource.org/licenses/MIT
 */
package cointoss;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.zip.CRC32;

import cointoss.util.Num;
import kiss.Decoder;
import kiss.Encoder;

/**
 * @version 2018/04/28 11:35:38
 */
public class Execution {

    /** The order delay type. (DEFAULT) */
    public static final int DelayUnknown = 0;

    /** The order delay type. */
    public static final int DelayInestimable = -1;

    /** The order delay type. */
    public static final int DelayServerOrder = -2;

    /** The consecutive type. (DEFAULT) */
    public static final int ConsecutiveUnknown = 0;

    /** The consecutive type. */
    public static final int ConsecutiveDifference = 1;

    /** The consecutive type. */
    public static final int ConsecutiveSameBuyer = 2;

    /** The consecutive type. */
    public static final int ConsecutiveSameSeller = 3;

    /** The empty object. */
    public static final Execution NONE = new Execution();

    static {
        NONE.id = 0;
        NONE.exec_date = ZonedDateTime.now();
        NONE.side = Side.BUY;
        NONE.price = Num.ZERO;
        NONE.size = Num.ZERO;
    }

    public long id;

    /** Buyer id of this execution. */
    public String buy_child_order_acceptance_id = "";

    /** Seller id of this execution. */
    public String sell_child_order_acceptance_id = "";

    /** The side */
    public Side side;

    /** price */
    public Num price;

    /** size */
    public Num size;

    /** size */
    public Num cumulativeSize = Num.ZERO;

    /** date */
    public ZonedDateTime exec_date;

    /**
     * Optional Attribute : The rough estimated delay time (unit : second). The negative value means
     * special info.
     */
    public long delay;

    /** Optional Attribute : The consecutive type. */
    public int consecutive;

    /**
     * 
     */
    public Execution() {
    }

    /**
     * @param line
     */
    public Execution(String line) {
        this(line.split(" "));
    }

    /**
     * @param line
     */
    public Execution(String... values) {
        id = Long.parseLong(values[0]);
        exec_date = LocalDateTime.parse(values[1]).atZone(cointoss.util.Chrono.UTC);
        side = Side.parse(values[2]);
        price = Num.of(values[3]);
        size = cumulativeSize = Num.of(values[4]);

        if (5 < values.length) {
            buy_child_order_acceptance_id = values[5];
            sell_child_order_acceptance_id = values[6];
        }
    }

    /**
     * Helper method to compare date and time.
     * 
     * @param time
     * @return A result.
     */
    public final boolean isBefore(ZonedDateTime time) {
        return exec_date.isBefore(time);
    }

    /**
     * Helper method to compare date and time.
     * 
     * @param time
     * @return A result.
     */
    public final boolean isAfter(ZonedDateTime time) {
        return exec_date.isAfter(time);
    }

    /**
     * Compute buyer id.
     * 
     * @return
     */
    public final long buyer() {
        return id(buy_child_order_acceptance_id);
    }

    /**
     * Compute seller id.
     * 
     * @return
     */
    public final long seller() {
        return id(sell_child_order_acceptance_id);
    }

    private long id(String value) {
        if (value.startsWith("JRF20") && 11 < value.length()) {
            return Long.parseLong(value.substring(11).replaceAll("\\D", ""));
        } else {
            CRC32 crc = new CRC32();
            crc.update(value.getBytes());
            return crc.getValue();
        }
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
        return id + " " + exec_date.toLocalDateTime() + " " + side
                .mark() + " " + price + " " + size + " " + buy_child_order_acceptance_id + " " + sell_child_order_acceptance_id;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean equals(Object obj) {
        if (obj instanceof Execution == false) {
            return false;
        }

        Execution other = (Execution) obj;

        if (id != other.id) {
            return false;
        }

        if (side != other.side) {
            return false;
        }

        if (price.isNot(other.price)) {
            return false;
        }

        if (size.isNot(other.size)) {
            return false;
        }

        if (exec_date.isEqual(other.exec_date) == false) {
            return false;
        }

        if (buyer() != other.buyer()) {
            return false;
        }

        // if (seller() != other.seller()) {
        // return false;
        // }
        return true;
    }

    /**
     * @version 2017/09/07 23:25:44
     */
    @SuppressWarnings("unused")
    private static class Codec implements Decoder<ZonedDateTime>, Encoder<ZonedDateTime> {

        private static final ZoneId zone = ZoneId.of("UTC");

        /**
         * {@inheritDoc}
         */
        @Override
        public String encode(ZonedDateTime value) {
            return value.toLocalDate().toString();
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public ZonedDateTime decode(String value) {
            return LocalDateTime.parse(value).atZone(zone);
        }
    }

}
