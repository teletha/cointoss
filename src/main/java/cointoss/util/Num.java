/*
 * Copyright (C) 2017 Nameless Production Committee
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          http://opensource.org/licenses/mit-license.php
 */
package cointoss.util;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.util.Objects;

import cointoss.Directional;
import kiss.Decoder;
import kiss.Encoder;
import kiss.I;

/**
 * @version 2017/09/24 13:06:14
 */
public class Num implements Comparable<Num> {

    static {
        I.load(Codec.class, false);
    }

    public static final MathContext MATH_CONTEXT = new MathContext(16, RoundingMode.HALF_UP);

    /** Not-a-Number instance (infinite error) */
    public static final Num NaN = new Num();

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
    public static final Num MAX = new Num(new BigDecimal(Long.MAX_VALUE));

    /** reuse */
    public static final Num MIN = new Num(new BigDecimal(Long.MIN_VALUE));

    /** The actual value. */
    private final BigDecimal delegate;

    /**
     * Constructor. Only used for NaN instance.
     */
    private Num() {
        this.delegate = null;
    }

    /**
     * Constructor.
     * 
     * @param value primitive value
     */
    private Num(BigDecimal value) {
        this.delegate = value;
    }

    /**
     * Returns a {@code Decimal} whose value is {@code (this + augend)}, with rounding according to
     * the context settings.
     * 
     * @param augend value to be added to this {@code Decimal}.
     * @return {@code this + augend}, rounded as necessary
     * @see BigDecimal#add(java.math.BigDecimal, java.math.MathContext)
     */
    public final Num plus(int augend) {
        return plus(of(augend));
    }

    /**
     * Returns a {@code Decimal} whose value is {@code (this + augend)}, with rounding according to
     * the context settings.
     * 
     * @param augend value to be added to this {@code Decimal}.
     * @return {@code this + augend}, rounded as necessary
     * @see BigDecimal#add(java.math.BigDecimal, java.math.MathContext)
     */
    public final Num plus(String augend) {
        return plus(of(augend));
    }

    /**
     * Returns a {@code Decimal} whose value is {@code (this + augend)}, with rounding according to
     * the context settings.
     * 
     * @param augend value to be added to this {@code Decimal}.
     * @return {@code this + augend}, rounded as necessary
     * @see BigDecimal#add(java.math.BigDecimal, java.math.MathContext)
     */
    public final Num plus(Num augend) {
        if (augend == null || augend.isNaN()) {
            return NaN;
        }
        return new Num(delegate.add(augend.delegate, MATH_CONTEXT));
    }

    /**
     * Returns a {@code Decimal} whose value is {@code (this - augend)}, with rounding according to
     * the context settings.
     * 
     * @param subtrahend value to be subtracted from this {@code Decimal}.
     * @return {@code this - subtrahend}, rounded as necessary
     * @see BigDecimal#subtract(java.math.BigDecimal, java.math.MathContext)
     */
    public final Num minus(int subtrahend) {
        return minus(of(subtrahend));
    }

    /**
     * Returns a {@code Decimal} whose value is {@code (this - augend)}, with rounding according to
     * the context settings.
     * 
     * @param subtrahend value to be subtracted from this {@code Decimal}.
     * @return {@code this - subtrahend}, rounded as necessary
     * @see BigDecimal#subtract(java.math.BigDecimal, java.math.MathContext)
     */
    public final Num minus(String subtrahend) {
        return minus(of(subtrahend));
    }

    /**
     * Returns a {@code Decimal} whose value is {@code (this - augend)}, with rounding according to
     * the context settings.
     * 
     * @param subtrahend value to be subtracted from this {@code Decimal}.
     * @return {@code this - subtrahend}, rounded as necessary
     * @see BigDecimal#subtract(java.math.BigDecimal, java.math.MathContext)
     */
    public final Num minus(Num subtrahend) {
        if (subtrahend == null || subtrahend == NaN) {
            return NaN;
        }
        return new Num(delegate.subtract(subtrahend.delegate, MATH_CONTEXT));
    }

    /**
     * Returns a {@code Decimal} whose value is {@code this * multiplicand}, with rounding according
     * to the context settings.
     * 
     * @param multiplicand value to be multiplied by this {@code Decimal}.
     * @return {@code this * multiplicand}, rounded as necessary
     * @see BigDecimal#multiply(java.math.BigDecimal, java.math.MathContext)
     */
    public final Num multiply(int multiplicand) {
        return multiply(of(multiplicand));
    }

    /**
     * Returns a {@code Decimal} whose value is {@code this * multiplicand}, with rounding according
     * to the context settings.
     * 
     * @param multiplicand value to be multiplied by this {@code Decimal}.
     * @return {@code this * multiplicand}, rounded as necessary
     * @see BigDecimal#multiply(java.math.BigDecimal, java.math.MathContext)
     */
    public final Num multiply(String multiplicand) {
        return multiply(of(multiplicand));
    }

    /**
     * Returns a {@code Decimal} whose value is {@code this * multiplicand}, with rounding according
     * to the context settings.
     * 
     * @param multiplicand value to be multiplied by this {@code Decimal}.
     * @return {@code this * multiplicand}, rounded as necessary
     * @see BigDecimal#multiply(java.math.BigDecimal, java.math.MathContext)
     */
    public final Num multiply(Num multiplicand) {
        if (multiplicand == null || multiplicand == NaN) {
            return NaN;
        }
        return new Num(delegate.multiply(multiplicand.delegate, MATH_CONTEXT));
    }

    /**
     * Returns a {@code Decimal} whose value is {@code (this / divisor)}, with rounding according to
     * the context settings.
     * 
     * @param divisor value by which this {@code Decimal} is to be divided.
     * @return {@code this / divisor}, rounded as necessary
     * @see BigDecimal#divide(java.math.BigDecimal, java.math.MathContext)
     */
    public final Num divide(int divisor) {
        return divide(of(divisor));
    }

    /**
     * Returns a {@code Decimal} whose value is {@code (this / divisor)}, with rounding according to
     * the context settings.
     * 
     * @param divisor value by which this {@code Decimal} is to be divided.
     * @return {@code this / divisor}, rounded as necessary
     * @see BigDecimal#divide(java.math.BigDecimal, java.math.MathContext)
     */
    public final Num divide(String divisor) {
        return divide(of(divisor));
    }

    /**
     * Returns a {@code Decimal} whose value is {@code (this / divisor)}, with rounding according to
     * the context settings.
     * 
     * @param divisor value by which this {@code Decimal} is to be divided.
     * @return {@code this / divisor}, rounded as necessary
     * @see BigDecimal#divide(java.math.BigDecimal, java.math.MathContext)
     */
    public final Num divide(Num divisor) {
        if (this == NaN || divisor == null || divisor == NaN || divisor.isZero()) {
            return NaN;
        }
        return new Num(delegate.divide(divisor.delegate, MATH_CONTEXT));
    }

    /**
     * Returns a {@code Decimal} whose value is {@code (this % divisor)}, with rounding according to
     * the context settings.
     * 
     * @param divisor value by which this {@code Decimal} is to be divided.
     * @return {@code this % divisor}, rounded as necessary.
     * @see BigDecimal#remainder(java.math.BigDecimal, java.math.MathContext)
     */
    public final Num remainder(int divisor) {
        return remainder(of(divisor));
    }

    /**
     * Returns a {@code Decimal} whose value is {@code (this % divisor)}, with rounding according to
     * the context settings.
     * 
     * @param divisor value by which this {@code Decimal} is to be divided.
     * @return {@code this % divisor}, rounded as necessary.
     * @see BigDecimal#remainder(java.math.BigDecimal, java.math.MathContext)
     */
    public final Num remainder(String divisor) {
        return remainder(of(divisor));
    }

    /**
     * Returns a {@code Decimal} whose value is {@code (this % divisor)}, with rounding according to
     * the context settings.
     * 
     * @param divisor value by which this {@code Decimal} is to be divided.
     * @return {@code this % divisor}, rounded as necessary.
     * @see BigDecimal#remainder(java.math.BigDecimal, java.math.MathContext)
     */
    public final Num remainder(Num divisor) {
        if (this == NaN || divisor == null || divisor == NaN || divisor.isZero()) {
            return NaN;
        }
        return new Num(delegate.remainder(divisor.delegate, MATH_CONTEXT));
    }

    /**
     * Change fractional portion.
     * 
     * @return
     */
    public final Num scale(int size) {
        return this == NaN ? NaN : new Num(delegate.setScale(size, RoundingMode.HALF_UP));
    }

    /**
     * Returns a {@code Decimal} whose value is <tt>(this<sup>n</sup>)</tt>.
     * 
     * @param n power to raise this {@code Decimal} to.
     * @return <tt>this<sup>n</sup></tt>
     * @see BigDecimal#pow(int, java.math.MathContext)
     */
    public final Num pow(int n) {
        if (this == NaN) {
            return NaN;
        }
        return new Num(delegate.pow(n, MATH_CONTEXT));
    }

    /**
     * Returns the correctly rounded natural logarithm (base e) of the <code>double</code> value of
     * this {@code Decimal}. /!\ Warning! Uses the {@code StrictMath#log(double)} method under the
     * hood.
     * 
     * @return the natural logarithm (base e) of {@code this}
     * @see StrictMath#log(double)
     */
    public final Num log() {
        if (this == NaN) {
            return NaN;
        }
        return of(StrictMath.log(delegate.doubleValue()));
    }

    /**
     * Returns the correctly rounded positive square root of the <code>double</code> value of this
     * {@code Decimal}. /!\ Warning! Uses the {@code StrictMath#sqrt(double)} method under the hood.
     * 
     * @return the positive square root of {@code this}
     * @see StrictMath#sqrt(double)
     */
    public final Num sqrt() {
        if (this == NaN) {
            return NaN;
        }
        return of(StrictMath.sqrt(delegate.doubleValue()));
    }

    /**
     * Returns a {@code Decimal} whose value is the absolute value of this {@code Decimal}.
     * 
     * @return {@code abs(this)}
     */
    public final Num abs() {
        if (this == NaN) {
            return NaN;
        }
        return new Num(delegate.abs());
    }

    /**
     * Converts this {@code Decimal} to a {@code double}.
     * 
     * @return this {@code Decimal} converted to a {@code double}
     * @see BigDecimal#doubleValue()
     */
    public final double toDouble() {
        if (this == NaN) {
            return Double.NaN;
        }
        return delegate.doubleValue();
    }

    /**
     * @param other
     * @return
     */
    @Override
    public int compareTo(Num other) {
        if (this == NaN || other == NaN || other == null) {
            return 0;
        }
        return delegate.compareTo(other.delegate);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        if (this == NaN) {
            return "NaN";
        }
        return delegate.stripTrailingZeros().toPlainString();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode() {
        return Objects.hash(delegate);
    }

    /**
     * {@inheritDoc} Warning: This method returns true if `this` and `obj` are both NaN.
     */
    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (!(obj instanceof Num)) {
            return false;
        }
        final Num other = (Num) obj;
        if (this.delegate != other.delegate && (this.delegate == null || (this.delegate.compareTo(other.delegate) != 0))) {
            return false;
        }
        return true;
    }

    /**
     * Compare {@link Num}.
     * 
     * @param other
     * @return A result.
     */
    public final boolean is(int other) {
        return is(of(other));
    }

    /**
     * Compare {@link Num}.
     * 
     * @param other
     * @return A result.
     */
    public final boolean is(double other) {
        return is(of(other));
    }

    /**
     * Compare {@link Num}.
     * 
     * @param other
     * @return A result.
     */
    public final boolean is(String other) {
        return is(of(other));
    }

    /**
     * Checks if this value is equal to another.
     * 
     * @param other the other value, not null
     * @return true is this is greater than the specified value, false otherwise
     */
    public final boolean is(Num other) {
        if (this == NaN || other == NaN) {
            return false;
        }
        return compareTo(other) == 0;
    }

    /**
     * Compare {@link Num}.
     * 
     * @param other the other value, not null
     * @return true is this is greater than the specified value, false otherwise
     */
    public final boolean isNot(int other) {
        return !is(other);
    }

    /**
     * Compare {@link Num}.
     * 
     * @param other the other value, not null
     * @return true is this is greater than the specified value, false otherwise
     */
    public final boolean isNot(double other) {
        return !is(other);
    }

    /**
     * Compare {@link Num}.
     * 
     * @param other the other value, not null
     * @return true is this is greater than the specified value, false otherwise
     */
    public final boolean isNot(String other) {
        return !is(other);
    }

    /**
     * Checks if this value is equal to another.
     * 
     * @param other the other value, not null
     * @return true is this is greater than the specified value, false otherwise
     */
    public final boolean isNot(Num other) {
        return !is(other);
    }

    /**
     * Checks if the value is zero.
     * 
     * @return true if the value is zero, false otherwise
     */
    public final boolean isZero() {
        return this != NaN && compareTo(ZERO) == 0;
    }

    /**
     * Checks if the value is NOT zero.
     * 
     * @return true if the value is NOT zero, false otherwise
     */
    public final boolean isNotZero() {
        return isZero() == false;
    }

    /**
     * Checks if the value is greater than zero.
     * 
     * @return true if the value is greater than zero, false otherwise
     */
    public final boolean isPositive() {
        return this != NaN && compareTo(ZERO) > 0;
    }

    /**
     * Checks if the value is zero or greater.
     * 
     * @return true if the value is zero or greater, false otherwise
     */
    public final boolean isPositiveOrZero() {
        return this != NaN && compareTo(ZERO) >= 0;
    }

    /**
     * Checks if the value is Not-a-Number.
     * 
     * @return true if the value is Not-a-Number (NaN), false otherwise
     */
    public final boolean isNaN() {
        return this == NaN;
    }

    /**
     * Checks if the value is less than zero.
     * 
     * @return true if the value is less than zero, false otherwise
     */
    public final boolean isNegative() {
        return this != NaN && compareTo(ZERO) < 0;
    }

    /**
     * Checks if the value is zero or less.
     * 
     * @return true if the value is zero or less, false otherwise
     */
    public final boolean isNegativeOrZero() {
        return this != NaN && compareTo(ZERO) <= 0;
    }

    /**
     * Checks if this value is greater than another.
     * 
     * @param other the other value, not null
     * @return true is this is greater than the specified value, false otherwise
     */
    public final boolean isGreaterThan(int other) {
        return isGreaterThan(of(other));
    }

    /**
     * Checks if this value is greater than another.
     * 
     * @param other the other value, not null
     * @return true is this is greater than the specified value, false otherwise
     */
    public final boolean isGreaterThan(String other) {
        return isGreaterThan(of(other));
    }

    /**
     * Checks if this value is greater than another.
     * 
     * @param other the other value, not null
     * @return true is this is greater than the specified value, false otherwise
     */
    public final boolean isGreaterThan(Num other) {
        return this != NaN && other != NaN && compareTo(other) > 0;
    }

    /**
     * Checks if this value is greater than or equal to another.
     * 
     * @param other the other value, not null
     * @return true is this is greater than or equal to the specified value, false otherwise
     */
    public final boolean isGreaterThanOrEqual(int other) {
        return isGreaterThanOrEqual(of(other));
    }

    /**
     * Checks if this value is greater than or equal to another.
     * 
     * @param other the other value, not null
     * @return true is this is greater than or equal to the specified value, false otherwise
     */
    public final boolean isGreaterThanOrEqual(String other) {
        return isGreaterThanOrEqual(of(other));
    }

    /**
     * Checks if this value is greater than or equal to another.
     * 
     * @param other the other value, not null
     * @return true is this is greater than or equal to the specified value, false otherwise
     */
    public final boolean isGreaterThanOrEqual(Num other) {
        return other != null && this != NaN && other != NaN && compareTo(other) > -1;
    }

    /**
     * Checks if this value is less than another.
     * 
     * @param other the other value, not null
     * @return true is this is less than the specified value, false otherwise
     */
    public final boolean isLessThan(int other) {
        return isLessThan(of(other));
    }

    /**
     * Checks if this value is less than another.
     * 
     * @param other the other value, not null
     * @return true is this is less than the specified value, false otherwise
     */
    public final boolean isLessThan(String other) {
        return isLessThan(of(other));
    }

    /**
     * Checks if this value is less than another.
     * 
     * @param other the other value, not null
     * @return true is this is less than the specified value, false otherwise
     */
    public final boolean isLessThan(Num other) {
        return this != NaN && other != NaN && compareTo(other) < 0;
    }

    /**
     * Checks if this value is less than or equal to another.
     * 
     * @param other the other value, not null
     * @return true is this is less than or equal to the specified value, false otherwise
     */
    public final boolean isLessThanOrEqual(int other) {
        return isLessThanOrEqual(of(other));
    }

    /**
     * Checks if this value is less than or equal to another.
     * 
     * @param other the other value, not null
     * @return true is this is less than or equal to the specified value, false otherwise
     */
    public final boolean isLessThanOrEqual(String other) {
        return isLessThanOrEqual(of(other));
    }

    /**
     * Checks if this value is less than or equal to another.
     * 
     * @param other the other value, not null
     * @return true is this is less than or equal to the specified value, false otherwise
     */
    public final boolean isLessThanOrEqual(Num other) {
        return other != null && this != NaN && other != NaN && compareTo(other) < 1;
    }

    /**
     * Format as JPY.
     * 
     * @return
     */
    public final String asJPY() {
        return asJPY(10);
    }

    /**
     * Format as JPY.
     * 
     * @return
     */
    public final String asJPY(int size) {
        return String.format("%," + size + ".0f円", delegate.doubleValue());
    }

    /**
     * Format as BTC.
     * 
     * @return
     */
    public final String asBTC() {
        return String.format("B%3.4f", delegate.doubleValue());
    }

    /**
     * Increase amount by the specified {@link Directional}.
     * 
     * @param direction A current side.
     * @param size A increase size.
     */
    public final Num plus(Directional direction, int size) {
        return plus(direction, of(size));
    }

    /**
     * Increase amount by the specified {@link Directional}.
     * 
     * @param direction A current side.
     * @param size A increase size.
     */
    public final Num plus(Directional direction, Num size) {
        return direction.isBuy() ? plus(size) : minus(size);
    }

    /**
     * Decrease amount by the specified {@link Directional}.
     * 
     * @param direction A current side.
     * @param size A decrease size.
     */
    public final Num minus(Directional direction, int size) {
        return minus(direction, of(size));
    }

    /**
     * Decrease amount by the specified {@link Directional}.
     * 
     * @param direction A current side.
     * @param size A decrease size.
     */
    public final Num minus(Directional direction, Num size) {
        return direction.isSell() ? plus(size) : minus(size);
    }

    /**
     * Compare {@link Num}.
     * 
     * @param amount
     * @return A result.
     */
    public final boolean isGreaterThan(Directional direction, Num price) {
        return direction.isBuy() ? isGreaterThan(price) : isLessThan(price);
    }

    /**
     * Compare {@link Num}.
     * 
     * @param amount
     * @return A result.
     */
    public final boolean isLessThan(Directional direction, int price) {
        return isLessThan(direction, Num.of(price));
    }

    /**
     * Compare {@link Num}.
     * 
     * @param amount
     * @return A result.
     */
    public final boolean isLessThan(Directional direction, Num price) {
        return direction.isBuy() ? isLessThan(price) : isGreaterThan(price);
    }

    public final String format(int scale) {
        Num num = scale(scale);

        return isPositiveOrZero() ? "+" + num : num.toString();
    }

    /**
     * Convert to {@link Num}.
     * 
     * @param value
     * @return
     */
    public static Num of(int value) {
        return new Num(new BigDecimal(value, MATH_CONTEXT));
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
        return new Num(new BigDecimal(value, MATH_CONTEXT));
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
        return Float.isNaN(value) ? NaN : new Num(new BigDecimal(value, MATH_CONTEXT));
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
        return Double.isNaN(value) ? NaN : new Num(new BigDecimal(value, MATH_CONTEXT));
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
        return "NaN".equals(value) ? NaN : new Num(new BigDecimal(value, MATH_CONTEXT));
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

    /**
     * Detect max value.
     * 
     * @param decimals
     * @return
     */
    public static Num max(Num... decimals) {
        if (decimals == null || decimals.length == 0) {
            return Num.NaN;
        }

        Num max = decimals[0];

        for (int i = 1; i < decimals.length; i++) {
            if (max == null || max.isLessThan(decimals[i])) {
                max = decimals[i];
            }
        }
        return max == null ? NaN : max;
    }

    /**
     * Detect min value.
     * 
     * @param decimals
     * @return
     */
    public static Num min(Num... decimals) {
        if (decimals == null || decimals.length == 0) {
            return Num.NaN;
        }

        Num min = decimals[0];

        for (int i = 1; i < decimals.length; i++) {
            if (min == null || min.isGreaterThan(decimals[i])) {
                min = decimals[i];
            }
        }
        return min == null ? NaN : min;
    }

    /**
     * @version 2017/07/26 9:08:16
     */
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
