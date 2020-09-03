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
import cointoss.util.decimal.JDK;
import kiss.Decoder;
import kiss.Encoder;
import kiss.I;
import kiss.Managed;
import kiss.Signal;
import kiss.Singleton;
import kiss.Variable;

@SuppressWarnings("serial")
public class Num extends JDK<Num> {

    // initialize
    static {
        I.load(Market.class);
    }

    /** reuse */
    public static final Num ZERO = of(0);

    /** reuse */
    public static final Num ONE = of(1);

    /** reuse */
    public static final Num TWO = of(2);

    /** reuse */
    public static final Num THREE = of(3);

    /** reuse */
    public static final Num TEN = of(10);

    /** reuse */
    public static final Num HUNDRED = of(100);

    /** reuse */
    public static final Num THOUSAND = of(1000);

    /** reuse */
    public static final Num MAX = of(Long.MAX_VALUE);

    /** reuse */
    public static final Num MIN = of(Long.MIN_VALUE);

    /** reuse */
    public static final Num NaN = new NaN();

    /**
     * Constructor. Only used for NaN instance.
     */
    private Num() {
    }

    /**
     * Constructor.
     * 
     * @param value primitive value
     */
    protected Num(BigDecimal value) {
        super(value);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected Num create(BigDecimal value) {
        return of(value);
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
        return new Num(new BigDecimal(value, CONTEXT));
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
        return new Num(new BigDecimal(value, CONTEXT));
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
        return new Num(new BigDecimal(value, CONTEXT));
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
        if (Double.isNaN(value)) {
            return NaN;
        }
        return new Num(new BigDecimal(value, CONTEXT));
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
        return new Num(new BigDecimal(value, CONTEXT));
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
        return new Num(value);
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

    /**
     */
    private static class NaN extends Num {

        /**
         * {@inheritDoc}
         */
        @Override
        public double doubleValue() {
            return Double.NaN;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public String toString() {
            return "NaN";
        }
    }
}