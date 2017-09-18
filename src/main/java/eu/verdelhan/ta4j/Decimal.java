/*
 * Copyright (C) 2017 Nameless Production Committee
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          http://opensource.org/licenses/mit-license.php
 */
package eu.verdelhan.ta4j;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.util.Objects;

import cointoss.Directional;
import kiss.Decoder;
import kiss.Encoder;

/**
 * @version 2017/07/24 15:31:51
 */
public class Decimal implements Comparable<Decimal> {

    private static final long serialVersionUID = 2225130444465033658L;

    public static final MathContext MATH_CONTEXT = new MathContext(16, RoundingMode.HALF_UP);

    /** Not-a-Number instance (infinite error) */
    public static final Decimal NaN = new Decimal();

    /** reuse */
    public static final Decimal ZERO = valueOf(0);

    /** reuse */
    public static final Decimal ONE = valueOf(1);

    /** reuse */
    public static final Decimal TWO = valueOf(2);

    /** reuse */
    public static final Decimal THREE = valueOf(3);

    /** reuse */
    public static final Decimal TEN = valueOf(10);

    /** reuse */
    public static final Decimal HUNDRED = valueOf(100);

    /** reuse */
    public static final Decimal THOUSAND = valueOf(1000);

    /** reuse */
    public static final Decimal MAX = new Decimal(new BigDecimal(Long.MAX_VALUE));

    private final BigDecimal delegate;

    /**
     * Constructor. Only used for NaN instance.
     */
    private Decimal() {
        delegate = null;
    }

    /**
     * Constructor.
     * 
     * @param val the string representation of the decimal value
     */
    private Decimal(String val) {
        delegate = new BigDecimal(val, MATH_CONTEXT);
    }

    /**
     * Constructor.
     * 
     * @param val the double value
     */
    private Decimal(double val) {
        delegate = new BigDecimal(val, MATH_CONTEXT);
    }

    private Decimal(int val) {
        delegate = new BigDecimal(val, MATH_CONTEXT);
    }

    private Decimal(long val) {
        delegate = new BigDecimal(val, MATH_CONTEXT);
    }

    private Decimal(BigDecimal val) {
        delegate = val;
    }

    /**
     * Returns a {@code Decimal} whose value is {@code (this + augend)}, with rounding according to
     * the context settings.
     * 
     * @param augend value to be added to this {@code Decimal}.
     * @return {@code this + augend}, rounded as necessary
     * @see BigDecimal#add(java.math.BigDecimal, java.math.MathContext)
     */
    public Decimal plus(Decimal augend) {
        return new Decimal(delegate.add(augend.delegate, MATH_CONTEXT));
    }

    /**
     * Returns a {@code Decimal} whose value is {@code (this - augend)}, with rounding according to
     * the context settings.
     * 
     * @param subtrahend value to be subtracted from this {@code Decimal}.
     * @return {@code this - subtrahend}, rounded as necessary
     * @see BigDecimal#subtract(java.math.BigDecimal, java.math.MathContext)
     */
    public Decimal minus(Decimal subtrahend) {
        return new Decimal(delegate.subtract(subtrahend.delegate, MATH_CONTEXT));
    }

    /**
     * Returns a {@code Decimal} whose value is {@code this * multiplicand}, with rounding according
     * to the context settings.
     * 
     * @param multiplicand value to be multiplied by this {@code Decimal}.
     * @return {@code this * multiplicand}, rounded as necessary
     * @see BigDecimal#multiply(java.math.BigDecimal, java.math.MathContext)
     */
    public Decimal multipliedBy(Decimal multiplicand) {
        return new Decimal(delegate.multiply(multiplicand.delegate, MATH_CONTEXT));
    }

    /**
     * Returns a {@code Decimal} whose value is {@code (this / divisor)}, with rounding according to
     * the context settings.
     * 
     * @param divisor value by which this {@code Decimal} is to be divided.
     * @return {@code this / divisor}, rounded as necessary
     * @see BigDecimal#divide(java.math.BigDecimal, java.math.MathContext)
     */
    public Decimal dividedBy(Decimal divisor) {
        if ((this == NaN) || (divisor == NaN) || divisor.isZero()) {
            return NaN;
        }
        return new Decimal(delegate.divide(divisor.delegate, MATH_CONTEXT));
    }

    /**
     * Returns a {@code Decimal} whose value is {@code (this % divisor)}, with rounding according to
     * the context settings.
     * 
     * @param divisor value by which this {@code Decimal} is to be divided.
     * @return {@code this % divisor}, rounded as necessary.
     * @see BigDecimal#remainder(java.math.BigDecimal, java.math.MathContext)
     */
    public Decimal remainder(Decimal divisor) {
        if ((this == NaN) || (divisor == NaN) || divisor.isZero()) {
            return NaN;
        }
        return new Decimal(delegate.remainder(divisor.delegate, MATH_CONTEXT));
    }

    /**
     * Returns a {@code Decimal} whose value is <tt>(this<sup>n</sup>)</tt>.
     * 
     * @param n power to raise this {@code Decimal} to.
     * @return <tt>this<sup>n</sup></tt>
     * @see BigDecimal#pow(int, java.math.MathContext)
     */
    public Decimal pow(int n) {
        if (this == NaN) {
            return NaN;
        }
        return new Decimal(delegate.pow(n, MATH_CONTEXT));
    }

    /**
     * Returns the correctly rounded natural logarithm (base e) of the <code>double</code> value of
     * this {@code Decimal}. /!\ Warning! Uses the {@code StrictMath#log(double)} method under the
     * hood.
     * 
     * @return the natural logarithm (base e) of {@code this}
     * @see StrictMath#log(double)
     */
    public Decimal log() {
        if (this == NaN) {
            return NaN;
        }
        return new Decimal(StrictMath.log(delegate.doubleValue()));
    }

    /**
     * Returns the correctly rounded positive square root of the <code>double</code> value of this
     * {@code Decimal}. /!\ Warning! Uses the {@code StrictMath#sqrt(double)} method under the hood.
     * 
     * @return the positive square root of {@code this}
     * @see StrictMath#sqrt(double)
     */
    public Decimal sqrt() {
        if (this == NaN) {
            return NaN;
        }
        return new Decimal(StrictMath.sqrt(delegate.doubleValue()));
    }

    /**
     * Returns a {@code Decimal} whose value is the absolute value of this {@code Decimal}.
     * 
     * @return {@code abs(this)}
     */
    public Decimal abs() {
        if (this == NaN) {
            return NaN;
        }
        return new Decimal(delegate.abs());
    }

    /**
     * Checks if the value is zero.
     * 
     * @return true if the value is zero, false otherwise
     */
    public boolean isZero() {
        if (this == NaN) {
            return false;
        }
        return compareTo(ZERO) == 0;
    }

    /**
     * Checks if the value is greater than zero.
     * 
     * @return true if the value is greater than zero, false otherwise
     */
    public boolean isPositive() {
        if (this == NaN) {
            return false;
        }
        return compareTo(ZERO) > 0;
    }

    /**
     * Checks if the value is zero or greater.
     * 
     * @return true if the value is zero or greater, false otherwise
     */
    public boolean isPositiveOrZero() {
        if (this == NaN) {
            return false;
        }
        return compareTo(ZERO) >= 0;
    }

    /**
     * Checks if the value is Not-a-Number.
     * 
     * @return true if the value is Not-a-Number (NaN), false otherwise
     */
    public boolean isNaN() {
        return this == NaN;
    }

    /**
     * Checks if the value is less than zero.
     * 
     * @return true if the value is less than zero, false otherwise
     */
    public boolean isNegative() {
        if (this == NaN) {
            return false;
        }
        return compareTo(ZERO) < 0;
    }

    /**
     * Checks if the value is zero or less.
     * 
     * @return true if the value is zero or less, false otherwise
     */
    public boolean isNegativeOrZero() {
        if (this == NaN) {
            return false;
        }
        return compareTo(ZERO) <= 0;
    }

    /**
     * Checks if this value is equal to another.
     * 
     * @param other the other value, not null
     * @return true is this is greater than the specified value, false otherwise
     */
    public boolean isEqual(Decimal other) {
        if ((this == NaN) || (other == NaN)) {
            return false;
        }
        return compareTo(other) == 0;
    }

    /**
     * Checks if this value is greater than another.
     * 
     * @param other the other value, not null
     * @return true is this is greater than the specified value, false otherwise
     */
    public boolean isGreaterThan(Decimal other) {
        if ((this == NaN) || (other == NaN)) {
            return false;
        }
        return compareTo(other) > 0;
    }

    /**
     * Checks if this value is greater than or equal to another.
     * 
     * @param other the other value, not null
     * @return true is this is greater than or equal to the specified value, false otherwise
     */
    public boolean isGreaterThanOrEqual(Decimal other) {
        if ((this == NaN) || (other == NaN)) {
            return false;
        }
        return compareTo(other) > -1;
    }

    /**
     * Checks if this value is less than another.
     * 
     * @param other the other value, not null
     * @return true is this is less than the specified value, false otherwise
     */
    public boolean isLessThan(Decimal other) {
        if ((this == NaN) || (other == NaN)) {
            return false;
        }
        return compareTo(other) < 0;
    }

    /**
     * Checks if this value is less than or equal to another.
     * 
     * @param other the other value, not null
     * @return true is this is less than or equal to the specified value, false otherwise
     */
    public boolean isLessThanOrEqual(Decimal other) {
        if ((this == NaN) || (other == NaN)) {
            return false;
        }
        return compareTo(other) < 1;
    }

    /**
     * @param other
     * @return
     */
    @Override
    public int compareTo(Decimal other) {
        if ((this == NaN) || (other == NaN)) {
            return 0;
        }
        return delegate.compareTo(other.delegate);
    }

    /**
     * Returns the minimum of this {@code Decimal} and {@code other}.
     * 
     * @param other value with which the minimum is to be computed
     * @return the {@code Decimal} whose value is the lesser of this {@code Decimal} and
     *         {@code other}. If they are equal, as defined by the {@link #compareTo(Decimal)
     *         compareTo} method, {@code this} is returned.
     */
    public Decimal min(Decimal other) {
        if ((this == NaN) || (other == NaN)) {
            return NaN;
        }
        return (compareTo(other) <= 0 ? this : other);
    }

    /**
     * Returns the maximum of this {@code Decimal} and {@code other}.
     * 
     * @param other value with which the maximum is to be computed
     * @return the {@code Decimal} whose value is the greater of this {@code Decimal} and
     *         {@code other}. If they are equal, as defined by the {@link #compareTo(Decimal)
     *         compareTo} method, {@code this} is returned.
     */
    public Decimal max(Decimal other) {
        if ((this == NaN) || (other == NaN)) {
            return NaN;
        }
        return (compareTo(other) >= 0 ? this : other);
    }

    /**
     * Converts this {@code Decimal} to a {@code double}.
     * 
     * @return this {@code Decimal} converted to a {@code double}
     * @see BigDecimal#doubleValue()
     */
    public double toDouble() {
        if (this == NaN) {
            return Double.NaN;
        }
        return delegate.doubleValue();
    }

    @Override
    public String toString() {
        if (this == NaN) {
            return "NaN";
        }
        return delegate.stripTrailingZeros().toPlainString();
    }

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
        if (!(obj instanceof Decimal)) {
            return false;
        }
        final Decimal other = (Decimal) obj;
        if (this.delegate != other.delegate && (this.delegate == null || (this.delegate.compareTo(other.delegate) != 0))) {
            return false;
        }
        return true;
    }

    public static Decimal valueOf(String val) {
        if ("NaN".equals(val)) {
            return NaN;
        }
        return new Decimal(val);
    }

    public static Decimal valueOf(double val) {
        if (Double.isNaN(val)) {
            return NaN;
        }
        return new Decimal(val);
    }

    public static Decimal valueOf(int val) {
        return new Decimal(val);
    }

    public static Decimal valueOf(long val) {
        return new Decimal(val);
    }

    // =====================================================
    // ENHANCED METHOD
    // =====================================================

    public static Decimal of(int value) {
        return valueOf(value);
    }

    /**
     * Divide amount.
     * 
     * @param size
     * @return
     */
    public final Decimal dividedBy(int size) {
        return dividedBy(valueOf(size));
    }

    /**
     * Multiply amount.
     * 
     * @param size
     * @return
     */
    public final Decimal multiply(int size) {
        return multipliedBy(valueOf(size));
    }

    /**
     * @param i
     * @return
     */
    public final boolean isGreaterThanOrEqual(int amount) {
        return isGreaterThanOrEqual(valueOf(amount));
    }

    /**
     * Compare {@link Decimal3}.
     * 
     * @param amount
     * @return A result.
     */
    public final boolean isGreaterThan(int amount) {
        return isGreaterThan(valueOf(amount));
    }

    /**
     * Compare {@link Decimal3}.
     * 
     * @param amount
     * @return A result.
     */
    public final boolean isLessThan(int amount) {
        return isLessThan(valueOf(amount));
    }

    /**
     * @param amount
     * @return
     */
    public final boolean isLessThanOrEqual(int amount) {
        return isLessThanOrEqual(valueOf(amount));
    }

    /**
     * Compare {@link Decimal3}.
     * 
     * @param amount
     * @return A result.
     */
    public final boolean is(int amount) {
        return isEqual(valueOf(amount));
    }

    /**
     * Compare {@link Decimal3}.
     * 
     * @param amount
     * @return A result.
     */
    public final boolean is(String amount) {
        return isEqual(valueOf(amount));
    }

    /**
     * Compare {@link Decimal3}.
     * 
     * @param amount
     * @return A result.
     */
    public final boolean isNot(int amount) {
        return !isEqual(valueOf(amount));
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
     * Remove fractional portion.
     * 
     * @return
     */
    public Decimal integral() {
        return scale(0);
    }

    /**
     * Change fractional portion.
     * 
     * @return
     */
    public Decimal scale(int size) {
        return new Decimal(delegate.setScale(size, RoundingMode.HALF_UP));
    }

    /**
     * Increase amount by the specified {@link Directional}.
     * 
     * @param direction A current side.
     * @param size A increase size.
     */
    public final Decimal plus(int size) {
        return plus(new Decimal(size));
    }

    /**
     * Increase amount by the specified {@link Directional}.
     * 
     * @param direction A current side.
     * @param size A increase size.
     */
    public final Decimal plus(Directional direction, int size) {
        return plus(direction, new Decimal(size));
    }

    /**
     * Increase amount by the specified {@link Directional}.
     * 
     * @param direction A current side.
     * @param size A increase size.
     */
    public final Decimal plus(Directional direction, Decimal size) {
        return direction.isBuy() ? plus(size) : minus(size);
    }

    /**
     * Decrease amount by the specified {@link Directional}.
     * 
     * @param direction A current side.
     * @param size A decrease size.
     */
    public final Decimal minus(int size) {
        return minus(new Decimal(size));
    }

    /**
     * Decrease amount by the specified {@link Directional}.
     * 
     * @param direction A current side.
     * @param size A decrease size.
     */
    public final Decimal minus(Directional direction, int size) {
        return minus(direction, new Decimal(size));
    }

    /**
     * Decrease amount by the specified {@link Directional}.
     * 
     * @param direction A current side.
     * @param size A decrease size.
     */
    public final Decimal minus(Directional direction, Decimal size) {
        return direction.isSell() ? plus(size) : minus(size);
    }

    /**
     * Compare {@link Decimal3}.
     * 
     * @param amount
     * @return A result.
     */
    public final boolean isGreaterThan(Directional direction, Decimal price) {
        return direction.isBuy() ? isGreaterThan(price) : isLessThan(price);
    }

    /**
     * Compare {@link Decimal3}.
     * 
     * @param amount
     * @return A result.
     */
    public final boolean isLessThan(Directional direction, int price) {
        return isLessThan(direction, Decimal.valueOf(price));
    }

    /**
     * Compare {@link Decimal3}.
     * 
     * @param amount
     * @return A result.
     */
    public final boolean isLessThan(Directional direction, Decimal price) {
        return direction.isBuy() ? isLessThan(price) : isGreaterThan(price);
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
            decimals[i] = valueOf(values[i]);
        }
        return decimals;
    }

    /**
     * @version 2017/07/26 9:08:16
     */
    public static class Codec implements Encoder<Decimal>, Decoder<Decimal> {

        /**
         * {@inheritDoc}
         */
        @Override
        public Decimal decode(String value) {
            return new Decimal(value);
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public String encode(Decimal value) {
            return value.toString();
        }
    }
}
