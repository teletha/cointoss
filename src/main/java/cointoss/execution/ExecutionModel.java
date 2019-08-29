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

import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Consumer;
import java.util.function.LongConsumer;

import cointoss.Direction;
import cointoss.Directional;
import cointoss.util.Chrono;
import cointoss.util.Num;
import icy.manipulator.Icy;
import kiss.Decoder;
import kiss.Encoder;
import kiss.Manageable;
import kiss.Singleton;

@Icy(grouping = 2)
public abstract class ExecutionModel implements Directional {

    /** The internal id counter. */
    private static final AtomicLong counter = new AtomicLong(1);

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

    /**
     * Execution {@link Direction}.
     * 
     * @return
     */
    @Override
    @Icy.Property
    public abstract Direction direction();

    /**
     * Size.
     * 
     * @return
     */
    @Icy.Property
    public abstract Num size();

    /**
     * Set executed size by value.
     * 
     * @param size An executed size.
     * @return Chainable API.
     */
    @Icy.Overload("size")
    private Num size(int size) {
        return Num.of(size);
    }

    /**
     * Set executed size by value.
     * 
     * @param size An executed size.
     * @return Chainable API.
     */
    @Icy.Overload("size")
    private Num size(float size) {
        return Num.of(size);
    }

    /**
     * Set executed size by value.
     * 
     * @param size An executed size.
     * @return Chainable API.
     */
    @Icy.Overload("size")
    private Num size(long size) {
        return Num.of(size);
    }

    /**
     * Set executed size by value.
     * 
     * @param size An executed size.
     * @return Chainable API.
     */
    @Icy.Overload("size")
    private Num size(double size) {
        return Num.of(size);
    }

    @Icy.Intercept("size")
    private Num assignWithAccumulative(Num size, Consumer<Num> accumulative) {
        accumulative.accept(size);
        return size;
    }

    /**
     * Execution id.
     * 
     * @return
     */
    @Icy.Property(mutable = true)
    public long id() {
        return counter.getAndIncrement();
    }

    /**
     * Exectution price.
     * 
     * @return
     */
    @Icy.Property
    public Num price() {
        return Num.ZERO;
    }

    /**
     * Set price by value.
     * 
     * @param price A price.
     * @return Chainable API.
     */
    @Icy.Overload("price")
    private Num price(int price) {
        return Num.of(price);
    }

    /**
     * Set price by value.
     * 
     * @param price A price.
     * @return Chainable API.
     */
    @Icy.Overload("price")
    private Num price(long price) {
        return Num.of(price);
    }

    /**
     * Set price by value.
     * 
     * @param price A price.
     * @return Chainable API.
     */
    @Icy.Overload("price")
    private Num price(float price) {
        return Num.of(price);
    }

    /**
     * Set price by value.
     * 
     * @param price A price.
     * @return Chainable API.
     */
    @Icy.Overload("price")
    private Num price(double price) {
        return Num.of(price);
    }

    /**
     * Size.
     * 
     * @return
     */
    @Icy.Property(mutable = true)
    public Num accumulative() {
        return Num.ZERO;
    }

    /**
     * Accessor for {@link #price}.
     * 
     * @return
     */
    @Icy.Property
    public ZonedDateTime date() {
        return Chrono.MIN;
    }

    /**
     * Assign executed date.
     * 
     * @param year Year.
     * @param month Month.
     * @param day Day of month.
     * @param hour Hour.
     * @param minute Minute.
     * @param second Second.
     * @param ms Mill second.
     * @return
     */
    @Icy.Overload("date")
    private ZonedDateTime date(int year, int month, int day, int hour, int minute, int second, int ms) {
        return ZonedDateTime.of(year, month, day, hour, minute, second, ms * 1000000, Chrono.UTC);
    }

    @Icy.Intercept("date")
    private ZonedDateTime assignWithMills(ZonedDateTime date, LongConsumer mills) {
        mills.accept(date.toInstant().toEpochMilli());
        return date;
    }

    /**
     * Accessor for {@link #price}.
     * 
     * @return
     */
    @Icy.Property
    public long mills() {
        return 0;
    }

    /**
     * Accessor for {@link #price}.
     * 
     * @return
     */
    @Icy.Property
    public int consecutive() {
        return ConsecutiveDifference;
    }

    /**
     * Accessor for {@link #price}.
     * 
     * @return
     */
    @Icy.Property
    public int delay() {
        return DelayInestimable;
    }

    /**
     * Accessor for {@link #info}.
     * 
     * @return
     */
    @Icy.Property
    public String info() {
        return null;
    }

    /**
     * Accessor for {@link #detail}.
     * 
     * @return
     */
    @Icy.Property
    public String detail() {
        return null;
    }

    /**
     * Helper method to compare date and time.
     * 
     * @param time
     * @return A result.
     */
    public final boolean isBefore(ZonedDateTime time) {
        return date().isBefore(time);
    }

    /**
     * Helper method to compare date and time.
     * 
     * @param time
     * @return A result.
     */
    public final boolean isAfter(ZonedDateTime time) {
        return date().isAfter(time);
    }

    /**
     * Calculate the after time.
     * 
     * @param seconds
     * @return
     */
    public ZonedDateTime after(long seconds) {
        return date().plusSeconds(seconds);
    }

    /**
     * Check each equality.
     * 
     * @param other
     * @return
     */
    public boolean equals(Execution other) {
        if (other.info == null) {
            return id() == other.id;
        } else {
            return info() == other.info;
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return id() + " " + date().toLocalDateTime() + " " + direction()
                .mark() + " " + price() + " " + size() + " " + consecutive() + " " + delay();
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
