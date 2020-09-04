/*
 * Copyright (C) 2020 cointoss Development Team
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          https://opensource.org/licenses/MIT
 */
package cointoss.util;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.IntStream;

import org.apache.commons.lang3.RandomUtils;

import cointoss.Direction;
import cointoss.Directional;
import cointoss.Market;
import cointoss.util.decimal.Decimal;
import kiss.Decoder;
import kiss.Encoder;
import kiss.I;
import kiss.Managed;
import kiss.Signal;
import kiss.Singleton;
import kiss.Variable;

@SuppressWarnings("serial")
public class Num extends Decimal<Num> {

    /** reuse */
    public static final Num ZERO = new Num(0, 0);

    /** reuse */
    public static final Num ONE = ZERO.create(1);

    /** reuse */
    public static final Num TWO = ZERO.create(2);

    /** reuse */
    public static final Num THREE = ZERO.create(3);

    /** reuse */
    public static final Num TEN = ZERO.create(10);

    /** reuse */
    public static final Num HUNDRED = ZERO.create(100);

    /** reuse */
    public static final Num THOUSAND = ZERO.create(1000);

    /** reuse */
    public static final Num MAX = ZERO.create(Long.MAX_VALUE);

    /** reuse */
    public static final Num MIN = ZERO.create(Long.MIN_VALUE);

    /**
     * @param value
     * @param scale
     */
    protected Num(long value, int scale) {
        super(value, scale);
    }

    /**
     * @param value
     */
    protected Num(BigDecimal value) {
        super(value);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected Num create(long value, int scale) {
        return new Num(value, scale);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected Num create(BigDecimal value) {
        return new Num(value);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected Num zero() {
        return ZERO;
    }

    /**
     * Convert to {@link Num}.
     * 
     * @param value
     * @return
     */
    public static Num of(int value) {
        return ZERO.create(value);
    }

    /**
     * Convert to {@link Num}.
     * 
     * @param values
     * @return
     */
    public static Num[] of(int... values) {
        Num[] decimals = new Num[values.length];

        for (int i = 0; i < decimals.length; i++) {
            decimals[i] = of(values[i]);
        }
        return decimals;
    }

    /**
     * Convert to {@link Num}.
     * 
     * @param value
     * @return
     */
    public static Num of(long value) {
        return ZERO.create(value);
    }

    /**
     * Convert to {@link Num}.
     * 
     * @param values
     * @return
     */
    public static Num[] of(long... values) {
        Num[] decimals = new Num[values.length];

        for (int i = 0; i < decimals.length; i++) {
            decimals[i] = of(values[i]);
        }
        return decimals;
    }

    /**
     * Convert to {@link Num}.
     * 
     * @param value
     * @return
     */
    public static Num of(float value) {
        return ZERO.create(value);
    }

    /**
     * Convert to {@link Num}.
     * 
     * @param values
     * @return
     */
    public static Num[] of(float... values) {
        Num[] decimals = new Num[values.length];

        for (int i = 0; i < decimals.length; i++) {
            decimals[i] = of(values[i]);
        }
        return decimals;
    }

    /**
     * Convert to {@link Num}.
     * 
     * @param value
     * @return
     */
    public static Num of(double value) {
        return ZERO.create(value);
    }

    /**
     * Convert to {@link Num}.
     * 
     * @param values
     * @return
     */
    public static Num[] of(double... values) {
        Num[] decimals = new Num[values.length];

        for (int i = 0; i < decimals.length; i++) {
            decimals[i] = of(values[i]);
        }
        return decimals;
    }

    /**
     * Convert to {@link Num}.
     * 
     * @param value
     * @return
     */
    public static Num of(String value) {
        return ZERO.create(value);
    }

    /**
     * Convert to {@link Num}.
     * 
     * @param values
     * @return
     */
    public static Num[] of(String... values) {
        Num[] decimals = new Num[values.length];

        for (int i = 0; i < decimals.length; i++) {
            decimals[i] = of(values[i]);
        }
        return decimals;
    }

    public static Num of(BigDecimal value) {
        return ZERO.create(value);
    }

    /**
     * Detect max value.
     * 
     * @param decimals
     * @return
     */
    public static Num max(Num... decimals) {
        return max(Direction.BUY, decimals);
    }

    /**
     * Detect max value.
     * 
     * @param decimals
     * @return
     */
    public static Num max(Directional direction, Num... decimals) {
        Num max = decimals[0];

        for (int i = 1; i < decimals.length; i++) {
            if (decimals[i] != null) {
                if (max == null || max.isLessThan(direction, decimals[i])) {
                    max = decimals[i];
                }
            }
        }
        return max;
    }

    /**
     * Detect min value.
     * 
     * @param one
     * @param other
     * @return
     */
    public static Num min(Variable<Num> one, Num other) {
        return min(one.v, other);
    }

    /**
     * Detect min value.
     * 
     * @param decimals
     * @return
     */
    public static Num min(Num... decimals) {
        return min(Direction.BUY, decimals);
    }

    /**
     * Detect min value.
     * 
     * @param decimals
     * @return
     */
    public static Num min(Directional direction, Num... decimals) {
        Num min = decimals[0];

        for (int i = 1; i < decimals.length; i++) {
            if (decimals[i] != null) {
                if (min == null || min.isGreaterThan(direction, decimals[i])) {
                    min = decimals[i];
                }
            }
        }
        return min;
    }

    /**
     * Check the value range.
     * 
     * @param min A minimum value.
     * @param value A target value to check.
     * @param max A maximum value.
     * @return A target value in range.
     */
    public static boolean within(Num min, Num value, Num max) {
        if (min.isGreaterThan(value)) {
            return false;
        }

        if (value.isGreaterThan(max)) {
            return false;
        }
        return true;
    }

    /**
     * Check the value range.
     * 
     * @param min A minimum value.
     * @param value A target value to check.
     * @param max A maximum value.
     * @return A target value in range.
     */
    public static Num between(Num min, Num value, Num max) {
        return min(max, max(min, value));
    }

    /**
     * @param start
     * @param end
     * @return
     */
    public static Signal<Num> range(int start, int end) {
        return I.signal(IntStream.rangeClosed(start, end).mapToObj(Num::of)::iterator);
    }

    /**
     * Create {@link Num} with random number between min and max.
     * 
     * @param minInclusive A minimum number (inclusive).
     * @param maxExclusive A maximum number (exclusive).
     * @return A random number.
     */
    public static Num random(double minInclusive, double maxExclusive) {
        return of(RandomUtils.nextDouble(minInclusive, maxExclusive));
    }

    /**
     * Calculate the sum of all numbers.
     * 
     * @param nums
     * @return
     */
    public static Num sum(Num... nums) {
        return sum(List.of(nums));
    }

    /**
     * Calculate the sum of all numbers.
     * 
     * @param nums
     * @return
     */
    public static Num sum(Iterable<Num> nums) {
        Num sum = Num.ZERO;
        for (Num num : nums) {
            sum = sum.plus(num);
        }
        return sum;
    }

    // initialize
    static {
        I.load(Market.class);
    }

    /**
     * 
     */
    @Managed(value = Singleton.class)
    private static class Codec implements Encoder<Num>, Decoder<Num> {

        /**
         * {@inheritDoc}
         */
        @Override
        public Num decode(String value) {
            return of(value);
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public String encode(Num value) {
            return value.toString();
        }
    }
}