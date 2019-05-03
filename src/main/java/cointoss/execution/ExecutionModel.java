/*
 * Copyright (C) 2019 CoinToss Development Team
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          https://opensource.org/licenses/MIT
 */
package cointoss.execution;

import java.time.ZonedDateTime;

import cointoss.Direction;
import cointoss.Directional;
import cointoss.util.Chrono;
import cointoss.util.Num;
import icy.manipulator.Icy;
import icy.manipulator.Initializer;

@Icy
public class ExecutionModel implements Directional {

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

    /** The identifier. */
    public long id = 0;

    /** The side */
    public Direction side = Direction.BUY;

    /** The executed price */
    public Num price = Num.ZERO;

    /** The executed size. */
    public Num size = Num.ZERO;

    /** The executed comulative size. */
    public Num cumulativeSize = Num.ZERO;

    /** The executed date-time. */
    public ZonedDateTime date = null;

    /** The epoch millseconds of executed date-time. */
    public final long mills = Initializer.Long();

    /** Optional Attribute : The consecutive type. */
    public int consecutive = ConsecutiveDifference;

    /**
     * Optional Attribute : The rough estimated delay time (unit : second). The negative value means
     * special info.
     */
    public int delay = DelayInestimable;

    /**
     * {@inheritDoc}
     */
    @Override
    public Direction direction() {
        return side;
    }

    /**
     * 
     */
    @Icy.Derive(by = "date", to = "mills")
    void date(Execution model) {
        model.mills(Chrono.epochMills(date));
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
        if (obj instanceof ExecutionModel == false) {
            return false;
        }

        ExecutionModel other = (ExecutionModel) obj;

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
}
