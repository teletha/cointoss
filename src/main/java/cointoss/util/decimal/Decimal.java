/*
 * Copyright (C) 2018 Nameless Production Committee
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          http://opensource.org/licenses/mit-license.php
 */
package cointoss.util.decimal;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.NumberFormat;
import java.util.List;
import java.util.stream.IntStream;

import org.apache.commons.lang3.RandomUtils;

import com.google.common.math.DoubleMath;

import cointoss.Direction;
import cointoss.Directional;
import cointoss.util.Primitives;
import kiss.I;
import kiss.Signal;
import kiss.Variable;

public class Decimal extends Arithmetic<Decimal> {

    private static final double[] positives = {1, 10, 100, 1000, 10000, 100000, 1000000, 10000000, 100000000, 1000000000, 10000000000d,
            100000000000d, 1000000000000d, 10000000000000d, 100000000000000d, 1000000000000000d, 10000000000000000d, 100000000000000000d,
            1000000000000000000d, 10000000000000000000d, 100000000000000000000d, 1000000000000000000000d, 10000000000000000000000d,
            100000000000000000000000d, 1000000000000000000000000d, 10000000000000000000000000d, 100000000000000000000000000d,
            1000000000000000000000000000d, 10000000000000000000000000000d, 100000000000000000000000000000d};

    private static final double[] negatives = {1, 0.1, 0.01, 0.001, 0.0001, 0.00001, 0.000001, 0.0000001, 0.00000001, 0.000000001,
            0.0000000001, 0.00000000001, 0.000000000001, 0.0000000000001, 0.00000000000001, 0.000000000000001, 0.0000000000000001,
            0.00000000000000001, 0.000000000000000001, 0.0000000000000000001, 0.00000000000000000001, 0.000000000000000000001,
            0.0000000000000000000001, 0.00000000000000000000001, 0.000000000000000000000001, 0.0000000000000000000000001,
            0.00000000000000000000000001, 0.000000000000000000000000001, 0.0000000000000000000000000001, 0.00000000000000000000000000001};

    private static double pow10(int scale) {
        if (0 <= scale) {
            return positives[scale];
        } else {
            return negatives[-scale];
        }
    }

    private static final Decimal ZERO = of(0);

    /** reuse */
    public static final Decimal ONE = of(1);

    /** reuse */
    public static final Decimal TWO = of(2);

    /** reuse */
    public static final Decimal THREE = of(3);

    /** reuse */
    public static final Decimal TEN = of(10);

    /** reuse */
    public static final Decimal HUNDRED = of(100);

    /** reuse */
    public static final Decimal THOUSAND = of(1000);

    private long v;

    private int scale;

    /**
     * @param value
     * @param scale
     */
    private Decimal(long value, int scale) {
        this.v = value;
        this.scale = scale;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected Decimal create(int value) {
        return of(value);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected Decimal create(long value) {
        return of(value);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected Decimal create(double value) {
        return of(value);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected Decimal create(String value) {
        return of(value);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected Decimal zero() {
        return ZERO;
    }

    @Override
    public Decimal plus(Decimal value) {
        if (scale == value.scale) {
            return new Decimal(v + value.v, scale);
        } else if (scale < value.scale) {
            return new Decimal((long) (v * pow10(value.scale - scale) + value.v), value.scale);
        } else {
            return new Decimal(v + (long) (value.v * pow10(scale - value.scale)), scale);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Decimal minus(Decimal value) {
        if (scale == value.scale) {
            return new Decimal(v - value.v, scale);
        } else if (scale < value.scale) {
            return new Decimal((long) (v * pow10(value.scale - scale) - value.v), value.scale);
        } else {
            return new Decimal(v - (long) (value.v * pow10(scale - value.scale)), scale);
        }
    }

    @Override
    public Decimal multiply(Decimal value) {
        return new Decimal(v * value.v, scale + value.scale);
    }

    @Override
    public Decimal divide(Decimal value) {
        Decimal result = Decimal.of((double) v / value.v);
        result.scale = scale - value.scale + result.scale;
        return result;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Decimal remainder(Decimal value) {
        if (scale == value.scale) {
            return new Decimal(v % value.v, scale);
        } else if (scale < value.scale) {
            return new Decimal((long) (v * pow10(value.scale - scale)) % value.v, value.scale);
        } else {
            return new Decimal(v % (long) (value.v * pow10(scale - value.scale)), scale);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int compareTo(Decimal o) {
        if (scale == o.scale) {
            return Long.compare(v, o.v);
        } else if (scale < o.scale) {
            return Long.compare((long) (v * pow10(o.scale - scale)), o.v);
        } else {
            return Long.compare(v, (long) (o.v * pow10(scale - o.scale)));
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Decimal decuple(int n) {
        return new Decimal(v, scale - n);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Decimal pow(int n) {
        Decimal result = of(Math.pow(v, n));
        result.scale += scale * n;
        return result;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Decimal pow(double n) {
        Decimal result = of(Math.pow(v, n));
        result.scale += scale * n;
        return result;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Decimal sqrt() {
        Decimal result = of(Math.sqrt(v));
        result.scale += scale / 2;
        return result;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Decimal abs() {
        return new Decimal(Math.abs(v), scale);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Decimal negate() {
        return new Decimal(-v, scale);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int scale() {
        return scale;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Decimal scale(int size, RoundingMode mode) {
        if (scale == size) {
            return this;
        } else if (scale < size) {
            return new Decimal((long) (v * pow10(size - scale)), size);
        } else {
            return new Decimal(DoubleMath.roundToLong(v * pow10(size - scale), mode), size);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String format(NumberFormat format) {
        return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int intValue() {
        return (int) (v * pow10(-scale));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public long longValue() {
        return (long) (v * pow10(-scale));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public float floatValue() {
        return (float) Primitives.roundDecimal(v * pow10(-scale), scale);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public double doubleValue() {
        return Primitives.roundDecimal(v * pow10(-scale), scale);
    }

    /**
     * Construct {@link Decimal} by the specified value.
     * 
     * @param value Your value.
     * @return Immutable {@link Decimal}.
     */
    public static Decimal of(int value) {
        return new Decimal(value, 0);
    }

    /**
     * Construct {@link Decimal} by the specified value.
     * 
     * @param value Your value.
     * @return Immutable {@link Decimal}.
     */
    public static Decimal of(long value) {
        return new Decimal(value, 0);
    }

    /**
     * Construct {@link Decimal} by the specified value.
     * 
     * @param value Your value.
     * @return Immutable {@link Decimal}.
     */
    public static Decimal of(float value) {
        int scale = computeScale(value);
        return new Decimal((long) (value * pow10(scale)), scale);
    }

    /**
     * Construct {@link Decimal} by the specified value.
     * 
     * @param value Your value.
     * @return Immutable {@link Decimal}.
     */
    public static Decimal of(double value) {
        int scale = computeScale(value);
        return new Decimal((long) (value * pow10(scale)), scale);
    }

    /**
     * Construct {@link Decimal} by the specified value.
     * 
     * @param value Your value.
     * @return Immutable {@link Decimal}.
     */
    public static Decimal of(String value) {
        BigDecimal big = new BigDecimal(value);
        int scale = Math.max(0, big.scale());
        return new Decimal(big.scaleByPowerOfTen(scale).longValue(), scale);
    }

    static int computeScale(double value) {
        for (int i = 0; i < 30; i++) {
            double fixer = pow10(i);
            double fixed = ((long) (value * fixer)) / fixer;
            if (value == fixed) {
                return i;
            }
        }
        throw new Error();
    }

    /**
     * Convert to {@link Decimal}.
     * 
     * @param values
     * @return
     */
    public static Decimal[] of(int... values) {
        Decimal[] decimals = new Decimal[values.length];

        for (int i = 0; i < decimals.length; i++) {
            decimals[i] = of(values[i]);
        }
        return decimals;
    }

    /**
     * Convert to {@link Decimal}.
     * 
     * @param values
     * @return
     */
    public static Decimal[] of(long... values) {
        Decimal[] decimals = new Decimal[values.length];

        for (int i = 0; i < decimals.length; i++) {
            decimals[i] = of(values[i]);
        }
        return decimals;
    }

    /**
     * Convert to {@link Decimal}.
     * 
     * @param values
     * @return
     */
    public static Decimal[] of(float... values) {
        Decimal[] decimals = new Decimal[values.length];

        for (int i = 0; i < decimals.length; i++) {
            decimals[i] = of(values[i]);
        }
        return decimals;
    }

    /**
     * Convert to {@link Decimal}.
     * 
     * @param values
     * @return
     */
    public static Decimal[] of(double... values) {
        Decimal[] decimals = new Decimal[values.length];

        for (int i = 0; i < decimals.length; i++) {
            decimals[i] = of(values[i]);
        }
        return decimals;
    }

    /**
     * Convert to {@link Decimal}.
     * 
     * @param values
     * @return
     */
    public static Decimal[] of(String... values) {
        Decimal[] decimals = new Decimal[values.length];

        for (int i = 0; i < decimals.length; i++) {
            decimals[i] = of(values[i]);
        }
        return decimals;
    }

    /**
     * Detect max value.
     * 
     * @param decimals
     * @return
     */
    public static Decimal max(Decimal... decimals) {
        return max(Direction.BUY, decimals);
    }

    /**
     * Detect max value.
     * 
     * @param decimals
     * @return
     */
    public static Decimal max(Directional direction, Decimal... decimals) {
        Decimal max = decimals[0];

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
    public static Decimal min(Variable<Decimal> one, Decimal other) {
        return min(one.v, other);
    }

    /**
     * Detect min value.
     * 
     * @param decimals
     * @return
     */
    public static Decimal min(Decimal... decimals) {
        return min(Direction.BUY, decimals);
    }

    /**
     * Detect min value.
     * 
     * @param decimals
     * @return
     */
    public static Decimal min(Directional direction, Decimal... decimals) {
        Decimal min = decimals[0];

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
    public static boolean within(Decimal min, Decimal value, Decimal max) {
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
    public static Decimal between(Decimal min, Decimal value, Decimal max) {
        return min(max, max(min, value));
    }

    /**
     * @param start
     * @param end
     * @return
     */
    public static Signal<Decimal> range(int start, int end) {
        return I.signal(IntStream.rangeClosed(start, end).mapToObj(Decimal::of)::iterator);
    }

    /**
     * Create {@link Decimal} with random Decimalber between min and max.
     * 
     * @param minInclusive A minimum Decimalber (inclusive).
     * @param maxExclusive A maximum Decimalber (exclusive).
     * @return A random Decimalber.
     */
    public static Decimal random(double minInclusive, double maxExclusive) {
        return of(RandomUtils.nextDouble(minInclusive, maxExclusive));
    }

    /**
     * Calculate the sum of all Decimalbers.
     * 
     * @param Decimals
     * @return
     */
    public static Decimal sum(Decimal... Decimals) {
        return sum(List.of(Decimals));
    }

    /**
     * Calculate the sum of all Decimalbers.
     * 
     * @param Decimals
     * @return
     */
    public static Decimal sum(Iterable<Decimal> Decimals) {
        Decimal sum = Decimal.ZERO;
        for (Decimal Decimal : Decimals) {
            sum = sum.plus(Decimal);
        }
        return sum;
    }
}
