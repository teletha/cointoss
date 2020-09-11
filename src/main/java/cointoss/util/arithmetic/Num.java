/*
 * Copyright (C) 2020 cointoss Development Team
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          https://opensource.org/licenses/MIT
 */
package cointoss.util.arithmetic;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.text.NumberFormat;
import java.util.List;
import java.util.stream.IntStream;

import org.apache.commons.lang3.RandomUtils;

import com.google.common.math.DoubleMath;

import ch.obermuhlner.math.big.BigDecimalMath;
import cointoss.Direction;
import cointoss.Directional;
import cointoss.Market;
import kiss.Decoder;
import kiss.Encoder;
import kiss.I;
import kiss.Managed;
import kiss.Signal;
import kiss.Singleton;
import kiss.Variable;

/**
 * A signed real number with arbitrary precision that cannot be changed.
 */
@SuppressWarnings("serial")
public class Num extends Arithmetic<Num> {

    /** The acceptable decimal difference. */
    static final double Fuzzy = 1e-14;

    /** The base context. */
    static final MathContext CONTEXT = new MathContext(19, RoundingMode.HALF_UP);

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
    public static final Num MAX = ZERO.create(Long.MAX_VALUE);

    /** reuse */
    public static final Num MIN = ZERO.create(Long.MIN_VALUE);

    /** Express a real number as the product of an integer N and a power of 10. */
    private long v;

    /** Express a real number as the product of an integer N and a power of 10. */
    private int scale;

    /**
     * Construct the number as a binary format with dynamic fixed precision.
     * 
     * @param value
     * @param scale
     */
    protected Num(long value, int scale) {
        this.v = value;
        this.scale = scale;
    }

    /**
     * Use an arbitrary double-precision decimal point for real numbers that do not fit in the range
     * of Long.
     */
    private BigDecimal big;

    /**
     * Constructs the number as a signed decimal number with arbitrary precision.
     * 
     * @param value
     */
    protected Num(BigDecimal value) {
        this.big = value;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected Num create(long value) {
        return new Num(value, 0);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected Num create(double value) {
        try {
            int scale = computeScale(value);
            double longed = value * pow10(scale);
            if (Long.MIN_VALUE < longed && longed < Long.MAX_VALUE) {
                return new Num((long) longed, scale);
            } else {
                // don't use BigDecimal constructor
                return create(BigDecimal.valueOf(value));
            }
        } catch (ArithmeticException e) {
            // don't use BigDecimal constructor
            return create(BigDecimal.valueOf(value));
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected Num create(String value) {
        if (value.indexOf('.') == -1 && value.length() < 18) {
            return create(Long.parseLong(value));
        } else {
            return create(new BigDecimal(value, CONTEXT));
        }
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
     * Convert to {@link BigDecimal}.
     * 
     * @return
     */
    private BigDecimal big() {
        if (big != null) {
            return big;
        } else if (scale == 0) {
            return new BigDecimal(v, CONTEXT);
        } else {
            return new BigDecimal(v, CONTEXT).scaleByPowerOfTen(-scale);
        }
    }

    /**
     * Convert to {@link BigDecimal}.
     * 
     * @return
     */
    Num small() {
        if (big != null) {
            scale = Math.max(0, big.scale());
            v = (long) (big.doubleValue() * pow10(scale));
        }
        return this;
    }

    public Num primitive() {
        if (big == null) {
            return this;
        } else {
            int scale = Math.max(0, big.scale());
            return new Num((long) (big.doubleValue() * pow10(scale)), scale);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Num plus(Num value) {
        if (big != null) {
            return create(big.add(value.big(), CONTEXT));
        } else if (value.big != null) {
            return create(big().add(value.big, CONTEXT));
        } else {
            try {
                if (scale == value.scale) {
                    return new Num(Math.addExact(v, value.v), scale);
                } else if (scale < value.scale) {
                    return new Num(Math.addExact((long) (v * pow10(value.scale - scale)), value.v), value.scale);
                } else {
                    return new Num(Math.addExact(v, (long) (value.v * pow10(scale - value.scale))), scale);
                }
            } catch (ArithmeticException e) {
                return create(big().add(value.big()));
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Num minus(Num value) {
        if (big != null) {
            return create(big.subtract(value.big(), CONTEXT));
        } else if (value.big != null) {
            return create(big().subtract(value.big, CONTEXT));
        } else {
            try {
                if (scale == value.scale) {
                    return new Num(Math.subtractExact(v, value.v), scale);
                } else if (scale < value.scale) {
                    return new Num(Math.subtractExact((long) (v * pow10(value.scale - scale)), value.v), value.scale);
                } else {
                    return new Num(Math.subtractExact(v, (long) (value.v * pow10(scale - value.scale))), scale);
                }
            } catch (ArithmeticException e) {
                return create(big().subtract(value.big()));
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Num multiply(Num value) {
        if (big != null) {
            return create(big.multiply(value.big(), CONTEXT));
        } else if (value.big != null) {
            return create(big().multiply(value.big, CONTEXT));
        } else {
            try {
                return new Num(Math.multiplyExact(v, value.v), scale + value.scale);
            } catch (ArithmeticException e) {
                return create(big().multiply(value.big()));
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Num divide(Num value) {
        if (big != null) {
            return create(big.divide(value.big(), CONTEXT));
        } else if (value.big != null) {
            return create(big().divide(value.big, CONTEXT));
        } else {
            if (value.v == 0) throw new ArithmeticException("Trying to divide " + this + " by 0.");

            Num result = create((double) v / value.v);
            result.scale = scale - value.scale + result.scale;
            return result;
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Num remainder(Num value) {
        if (big != null) {
            return create(big.remainder(value.big()));
        } else if (value.big != null) {
            return create(big().remainder(value.big));
        } else {
            try {
                if (scale == value.scale) {
                    return new Num(v % value.v, scale);
                } else if (scale < value.scale) {
                    return new Num((Math.multiplyExact(v, (long) pow10(value.scale - scale))) % value.v, value.scale);
                } else {
                    return new Num(v % (long) (value.v * pow10(scale - value.scale)), scale);
                }
            } catch (ArithmeticException e) {
                return create(big().remainder(value.big()));
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Num modulo(Num value) {
        if (big != null) {
            small();
        }

        if (value.big != null) {
            value.small();
        }

        if (scale == value.scale) {
            return new Num(v % value.v, scale);
        } else if (scale < value.scale) {
            return new Num((long) (v * pow10(value.scale - scale)) % value.v, value.scale);
        } else {
            return new Num(v % (long) (value.v * pow10(scale - value.scale)), scale);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int compareTo(Num o) {
        if (big != null) {
            return big.compareTo(o.big());
        } else if (o.big != null) {
            return big().compareTo(o.big);
        } else {
            if (scale == o.scale) {
                return Long.compare(v, o.v);
            } else if (scale < o.scale) {
                return Long.compare((long) (v * pow10(o.scale - scale)), o.v);
            } else {
                return Long.compare(v, (long) (o.v * pow10(scale - o.scale)));
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Num decuple(int n) {
        if (big != null) {
            return create(big.scaleByPowerOfTen(n));
        } else {
            return new Num(v, scale - n);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Num pow(int n) {
        if (big != null) {
            return create(big.pow(n));
        } else if (n == 0) {
            return Num.ONE; // by definition
        } else if (n == 1) {
            return this; // shortcut
        } else if (v == 0) {
            return Num.ZERO; // cache
        } else {
            try {
                double result = Math.pow(v, n);
                DoubleMath.roundToLong(result, RoundingMode.HALF_DOWN);
                Num self = create(result);
                self.scale += scale * n;
                return self;
            } catch (ArithmeticException e) {
                return create(big().pow(n));
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Num pow(double n) {
        if (big != null) {
            return create(BigDecimal.valueOf(Math.pow(big.doubleValue(), n)));
        } else if (n == 0) {
            return Num.ONE; // by definition
        } else if (n == 1) {
            return this; // shortcut
        } else if (v == 0) {
            return Num.ZERO; // cache
        } else {
            try {
                double result = Math.pow(v, n);
                DoubleMath.roundToLong(result, RoundingMode.HALF_DOWN);
                Num self = create(result);
                self.scale += scale * n;
                return self;
            } catch (ArithmeticException e) {
                return create(BigDecimalMath.pow(big(), BigDecimal.valueOf(n), MathContext.DECIMAL128));
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Num sqrt() {
        if (big != null) {
            return create(big.sqrt(CONTEXT));
        } else if (v < 0) {
            throw new ArithmeticException("Cannot calculate the square root of a negative number.");
        } else if (scale % 2 == 0) {
            Num result = create(Math.sqrt(v));
            result.scale += scale / 2;
            return result;
        } else {
            return create(big().sqrt(CONTEXT));
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Num abs() {
        if (big != null) {
            return create(big.abs());
        } else if (v == Long.MIN_VALUE) {
            return create(big().abs());
        } else {
            return new Num(Math.abs(v), scale);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Num negate() {
        if (big != null) {
            return create(big.negate());
        } else {
            try {
                return new Num(Math.negateExact(v), scale);
            } catch (ArithmeticException e) {
                return create(big().negate());
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int scale() {
        if (big != null) {
            return big.stripTrailingZeros().scale();
        } else {
            return scale;
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Num scale(int size, RoundingMode mode) {
        if (big != null) {
            return create(big.setScale(size, mode));
        } else {
            if (scale == size) {
                return this;
            } else if (scale < size) {
                return this;
            } else {
                return new Num(DoubleMath.roundToLong(v * pow10(size - scale), mode), size);
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String format(NumberFormat format) {
        if (big != null) {
            return format.format(big);
        } else {
            return format.format(doubleValue());
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int intValue() {
        if (big != null) {
            return big.intValue();
        } else {
            return (int) (v * pow10(-scale));
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public long longValue() {
        if (big != null) {
            return big.longValue();
        } else {
            return (long) (v * pow10(-scale));
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public float floatValue() {
        if (big != null) {
            return big.floatValue();
        } else {
            return (float) Primitives.roundDecimal(v * pow10(-scale), scale);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public double doubleValue() {
        if (big != null) {
            return big.doubleValue();
        } else {
            return Primitives.roundDecimal(v * pow10(-scale), scale);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        if (big != null) {
            return big.stripTrailingZeros().toPlainString();
        } else if (scale == 0) {
            return Long.toString(v);
        } else {
            return Primitives.roundString(v * pow10(-scale), scale);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode() {
        if (big != null) {
            return big.hashCode();
        } else {
            return (int) (v ^ ((scale + 1) >>> 32));
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean equals(Object obj) {
        if (obj instanceof Num == false) {
            return false;
        }

        Num other = (Num) obj;
        if (big != null) {
            if (other.big != null) {
                return big.compareTo(other.big) == 0;
            } else {
                return checkEqualityBetweenPrimitiveAndBig(other, this);
            }
        } else {
            if (other.big != null) {
                return checkEqualityBetweenPrimitiveAndBig(this, other);
            } else {
                return this.scale == other.scale && this.v == other.v;
            }
        }
    }

    /**
     * Test equality between the primive type and wrapped type.
     * 
     * @param primitive
     * @param big
     * @return
     */
    private boolean checkEqualityBetweenPrimitiveAndBig(Num primitive, Num big) {
        if (primitive.scale == 0) {
            return primitive.v == big.big.longValue();
        } else {
            return big.big.compareTo(primitive.big()) == 0;
        }
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

    static {
        I.load(Market.class);
    }

    /** The value of the power of 10 is calculated and cached in advance. (For 18 digits.) */
    private static final double[] positives = {1, 10, 100, 1000, 10000, 100000, 1000000, 10000000, 100000000, 1000000000, 10000000000d,
            100000000000d, 1000000000000d, 10000000000000d, 100000000000000d, 1000000000000000d, 10000000000000000d, 100000000000000000d,
            1000000000000000000d, 10000000000000000000d, 100000000000000000000d, 1000000000000000000000d, 10000000000000000000000d,
            100000000000000000000000d, 1000000000000000000000000d, 10000000000000000000000000d, 100000000000000000000000000d};

    /** The value of the power of 10 is calculated and cached in advance. (For 18 digits.) */
    private static final double[] negatives = {1, 0.1, 0.01, 0.001, 0.0001, 0.00001, 0.000001, 0.0000001, 0.00000001, 0.000000001,
            0.0000000001, 0.00000000001, 0.000000000001, 0.0000000000001, 0.00000000000001, 0.000000000000001, 0.0000000000000001,
            0.00000000000000001, 0.000000000000000001, 0.0000000000000000001, 0.00000000000000000001, 0.000000000000000000001};

    /**
     * Fast cached power of ten.
     * 
     * @param scale
     * @return
     */
    private static double pow10(int scale) {
        if (0 <= scale) {
            return positives[scale];
        } else {
            return negatives[-scale];
        }
    }

    /**
     * Estimate scale of the target double value.
     * 
     * @param value
     * @return
     */
    static int computeScale(double value) {
        if (value != 0 && -Fuzzy <= value && value <= Fuzzy) {
            throw new ArithmeticException("Too small.");
        }

        for (int i = 0; i < 18; i++) {
            double fixer = pow10(i);
            double fixed = ((long) (value * fixer)) / fixer;
            if (DoubleMath.fuzzyEquals(value, fixed, Fuzzy)) {
                return i;
            }
        }
        return 14;
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