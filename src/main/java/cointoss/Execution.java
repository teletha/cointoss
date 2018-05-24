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

import cointoss.util.Num;
import kiss.Decoder;
import kiss.Encoder;

/**
 * @version 2018/05/01 16:47:20
 */
public class Execution {

    /** The consecutive type. (DEFAULT) */
    public static final int ConsecutiveDifference = 0;

    /** The consecutive type. */
    public static final int ConsecutiveSameBuyer = 1;

    /** The consecutive type. */
    public static final int ConsecutiveSameSeller = 2;

    /** The order delay type. (DEFAULT) */
    public static final int DelayInestimable = 0;

    /** The order delay type (over 180s). */
    public static final int DelayHuge = -1;

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

    /** Optional Attribute : The consecutive type. */
    public int consecutive;

    /**
     * Optional Attribute : The rough estimated delay time (unit : second). The negative value means
     * special info.
     */
    public int delay;

    /** Optional : The associated execution id. */
    public String yourOrder;

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
        consecutive = Integer.parseInt(values[5]);
        delay = Integer.parseInt(values[6]);
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
        return id + " " + exec_date.toLocalDateTime() + " " + side.mark() + " " + price + " " + size + " " + consecutive + " " + delay;
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

        if (consecutive != other.consecutive) {
            return false;
        }

        if (delay != other.delay) {
            return false;
        }
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
