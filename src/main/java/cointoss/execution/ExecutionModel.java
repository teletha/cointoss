/*
 * Copyright (C) 2024 The COINTOSS Development Team
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          https://opensource.org/licenses/MIT
 */
package cointoss.execution;

import java.time.ZonedDateTime;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.DoubleConsumer;
import java.util.function.LongConsumer;

import cointoss.Direction;
import cointoss.util.Chrono;
import cointoss.util.feather.Timelinable;
import hypatia.Num;
import hypatia.Orientational;
import icy.manipulator.Icy;

@Icy(grouping = 2)
abstract class ExecutionModel implements Orientational<Direction>, Timelinable {

    /** The internal id counter. */
    private static final AtomicLong counter = new AtomicLong(1);

    /** The consecutive type. (DEFAULT) */
    public static final int ConsecutiveDifference = 0;

    /** The consecutive type. */
    public static final int ConsecutiveSameBuyer = 1;

    /** The consecutive type. */
    public static final int ConsecutiveSameSeller = 2;

    /** The consecutive type. */
    public static final int ConsecutivePseudoDifference = 3;

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
    public abstract Direction orientation();

    @Icy.Overload("orientation")
    private Direction direction(Orientational<Direction> direction) {
        return direction.orientation();
    }

    /**
     * Size.
     * 
     * @return
     */
    @Icy.Property(copiable = true)
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
    private Num assignWithAccumulative(Num size, DoubleConsumer accumulative) {
        accumulative.accept(size.doubleValue());
        return size;
    }

    /**
     * Execution id.
     * 
     * @return
     */
    @Icy.Property(getterModifier = "public")
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
    public double accumulative() {
        return 0;
    }

    /**
     * Executed date-time.
     * 
     * @return
     */
    @Override
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
     * {@inheritDoc}
     */
    @Override
    @Icy.Property
    public long mills() {
        return 0;
    }

    /**
     * Consecutive state.
     * 
     * @return
     */
    @Icy.Property
    public int consecutive() {
        return ConsecutiveDifference;
    }

    /**
     * Delay time.
     * 
     * @return
     */
    @Icy.Property(mutable = true)
    public int delay() {
        return DelayInestimable;
    }

    /**
     * Accessor for {@link Execution} related info.
     * 
     * @return
     */
    @Icy.Property(mutable = true)
    public String info() {
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
     * Helper method to compare date and time.
     * 
     * @return A result.
     */
    public final boolean isAfterSeconds(long second) {
        return second * 1000 < mills();
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
    public boolean equals(cointoss.execution.Execution other) {
        return id() == other.id;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return id() + " " + date().toLocalDateTime() + " " + orientation()
                .mark() + " " + price() + " " + size() + " " + consecutive() + " " + delay();
    }
}