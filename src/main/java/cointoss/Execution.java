/*
 * Copyright (C) 2019 CoinToss Development Team
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          https://opensource.org/licenses/MIT
 */
package cointoss;

import java.time.LocalDateTime;
import java.time.ZonedDateTime;

import cointoss.util.Chrono;
import cointoss.util.Num;
import kiss.Decoder;
import kiss.Encoder;
import kiss.Manageable;
import kiss.Singleton;

/**
 * @version 2018/07/12 9:52:41
 */
public class Execution implements Directional {

    /** The consecutive type. (DEFAULT) */
    public static final int ConsecutiveDifference = 0;

    /** The consecutive type. */
    public static final int ConsecutiveSameBuyer = 1;

    /** The consecutive type. */
    public static final int ConsecutiveSameSeller = 2;

    /** The consecutive type. */
    public static final int ConsecutiveSameBoth = 3;

    /** The order delay type. (DEFAULT) */
    public static final int DelayInestimable = 0;

    /** The order delay type (over 180s). */
    public static final int DelayHuge = -1;

    /** The empty object. */
    public static final Execution BASE = new Execution();

    static {
        // don't modify these initial values
        BASE.date = Chrono.utc(2000, 1, 1);
        BASE.side = Side.BUY;
        BASE.price = Num.ZERO;
        BASE.size = Num.ZERO;
    }

    public long id;

    /** The side */
    public Side side;

    /** The executed price */
    public Num price;

    /** The executed size. */
    public Num size;

    /** The executed comulative size. */
    public Num cumulativeSize = Num.ZERO;

    /** The executed date-time. */
    public ZonedDateTime date;

    /** Optional Attribute : The consecutive type. */
    public int consecutive;

    /**
     * Optional Attribute : The rough estimated delay time (unit : second). The negative value means
     * special info.
     */
    public int delay;

    /** Optional : The associated execution id. */
    public String yourOrder;

    /** The epoch millseconds of executed date-time. */
    public long mills;

    /**
     * Create empty {@link Execution}.
     */
    public Execution() {
    }

    /**
     * Create {@link Execution} with the specified values.
     * 
     * @param values A list of values.
     */
    Execution(String... values) {
        id = Long.parseLong(values[0]);
        date = LocalDateTime.parse(values[1]).atZone(Chrono.UTC);
        mills = Chrono.epochMills(date);
        side = Side.parse(values[2]);
        price = Num.of(values[3]);
        size = cumulativeSize = Num.of(values[4]);
        consecutive = Integer.parseInt(values[5]);
        delay = Integer.parseInt(values[6]);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Side side() {
        return side;
    }

    /**
     * Accessor for {@link #price}.
     * 
     * @return
     */
    public final Num price() {
        return price;
    }

    /**
     * Helper method to compare date and time.
     * 
     * @param time
     * @return A result.
     */
    public final boolean isBefore(ZonedDateTime time) {
        return date.isBefore(time);
    }

    /**
     * Helper method to compare date and time.
     * 
     * @param time
     * @return A result.
     */
    public final boolean isAfter(ZonedDateTime time) {
        return date.isAfter(time);
    }

    /**
     * Calculate the after time.
     * 
     * @param seconds
     * @return
     */
    public ZonedDateTime after(long seconds) {
        return date.plusSeconds(seconds);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return id + " " + date.toLocalDateTime() + " " + side.mark() + " " + price + " " + size + " " + consecutive + " " + delay;
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

        if (date.isEqual(other.date) == false) {
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
     * 
     */
    @Manageable(lifestyle = Singleton.class)
    private static class Codec implements Decoder<ZonedDateTime>, Encoder<ZonedDateTime> {

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
            return LocalDateTime.parse(value).atZone(Chrono.UTC);
        }
    }

}
