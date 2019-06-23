/*
 * Copyright (C) 2019 CoinToss Development Team
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          https://opensource.org/licenses/MIT
 */
package cointoss.util;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.util.Objects;
import java.util.stream.IntStream;

import org.apache.commons.lang3.RandomUtils;

import cointoss.Directional;
import cointoss.Market;
import kiss.Decoder;
import kiss.Encoder;
import kiss.I;
import kiss.Manageable;
import kiss.Signal;
import kiss.Singleton;
import kiss.Variable;

/**
 * @version 2018/07/14 13:48:54
 */
public class Num implements Comparable<Num> {

    // initialize
    static {
        I.load(Market.class);
    }

    /** The base context. */
    private static final MathContext CONTEXT = new MathContext(10, RoundingMode.HALF_UP);

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
    public final Num plus(long augend) {
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
    public final Num plus(double augend) {
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
    public final Num plus(Variable<Num> augend) {
        return plus(augend.get());
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
        return new Num(delegate.add(augend.delegate, CONTEXT));
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
    public final Num minus(long subtrahend) {
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
    public final Num minus(double subtrahend) {
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
    public final Num minus(Variable<Num> subtrahend) {
        return minus(subtrahend.get());
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
        return new Num(delegate.subtract(subtrahend.delegate, CONTEXT));
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
    public final Num multiply(long multiplicand) {
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
    public final Num multiply(double multiplicand) {
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
    public final Num multiply(Variable<Num> multiplicand) {
        return multiply(multiplicand.get());
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
        return new Num(delegate.multiply(multiplicand.delegate, CONTEXT));
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
    public final Num divide(long divisor) {
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
    public final Num divide(double divisor) {
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
    public final Num divide(Variable<Num> divisor) {
        return divide(divisor.get());
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
        return new Num(delegate.divide(divisor.delegate, CONTEXT));
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
    public final Num remainder(long divisor) {
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
    public final Num remainder(double divisor) {
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
    public final Num remainder(Variable<Num> divisor) {
        return remainder(divisor.get());
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
        return new Num(delegate.remainder(divisor.delegate, CONTEXT));
    }

    /**
     * Compute scale.
     */
    public int scale() {
        return delegate.stripTrailingZeros().scale();
    }

    /**
     * Change fractional portion.
     * 
     * @return
     */
    public final Num scale(int size) {
        return scale(size, RoundingMode.HALF_UP);
    }

    /**
     * Change fractional portion.
     * 
     * @return
     */
    public final Num scale(int size, RoundingMode mode) {
        return new Num(delegate.setScale(size, mode));
    }

    /**
     * Ceil up the specified digit.
     * 
     * @param size
     * @return
     */
    public final Num scaleDown(int size) {
        return scale(size, RoundingMode.DOWN);
    }

    /**
     * Ceil up the specified digit.
     * 
     * @param size
     * @return
     */
    public final Num scaleUp(int size) {
        return scale(size, RoundingMode.UP);
    }

    /**
     * @param scale
     */
    public Num scaleByPowerOfTen(int scale) {
        return new Num(delegate.scaleByPowerOfTen(scale));
    }

    /**
     * Returns a {@code Decimal} whose value is <tt>(this<sup>n</sup>)</tt>.
     * 
     * @param n power to raise this {@code Decimal} to.
     * @return <tt>this<sup>n</sup></tt>
     * @see BigDecimal#pow(int, java.math.MathContext)
     */
    public final Num pow(int n) {
        return new Num(delegate.pow(n, CONTEXT));
    }

    /**
     * Returns a {@code Decimal} whose value is <tt>(this<sup>n</sup>)</tt>.
     * 
     * @param n power to raise this {@code Decimal} to.
     * @return <tt>this<sup>n</sup></tt>
     * @see BigDecimal#pow(int, java.math.MathContext)
     */
    public final Num pow(double n) {
        return of(Math.pow(toDouble(), n));
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
        return of(StrictMath.sqrt(delegate.doubleValue()));
    }

    /**
     * Returns a {@code Decimal} whose value is the absolute value of this {@code Decimal}.
     * 
     * @return {@code abs(this)}
     */
    public final Num abs() {
        return new Num(delegate.abs());
    }

    /**
     * Returns a {@code Num} whose value is {@code (-this)}, and whose scale is
     * {@code this.scale()}.
     *
     * @return {@code -this}.
     */
    public Num negate() {
        return new Num(delegate.negate());
    }

    /**
     * Converts this {@code Num} to a {@code double}.
     * 
     * @return this {@code Num} converted to a {@code double}
     * @see BigDecimal#doubleValue()
     */
    public final double toDouble() {
        return delegate.doubleValue();
    }

    /**
     * Converts this {@code Num} to a {@code int}.
     * 
     * @return this {@code Num} converted to a {@code int}
     * @see BigDecimal#intValueExact();
     */
    public final int toInt() {
        return delegate.intValue();
    }

    /**
     * Converts this {@code Num} to a {@code long}.
     * 
     * @return this {@code Num} converted to a {@code long}
     * @see BigDecimal#intValueExact();
     */
    public final long toLong() {
        return delegate.longValue();
    }

    /**
     * @param other
     * @return
     */
    @Override
    public int compareTo(Num other) {
        return delegate.compareTo(other.delegate);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
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

        Num other = (Num) obj;
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
    public final boolean is(long other) {
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
     * Compare {@link Num}.
     * 
     * @param other
     * @return A result.
     */
    public final boolean is(Variable<Num> other) {
        return is(other.get());
    }

    /**
     * Checks if this value is equal to another.
     * 
     * @param other the other value, not null
     * @return true is this is greater than the specified value, false otherwise
     */
    public final boolean is(Num other) {
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
    public final boolean isNot(long other) {
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
    public final boolean isNot(Variable<Num> other) {
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
        return compareTo(ZERO) == 0;
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
        return compareTo(ZERO) > 0;
    }

    /**
     * Checks if the value is zero or greater.
     * 
     * @return true if the value is zero or greater, false otherwise
     */
    public final boolean isPositiveOrZero() {
        return compareTo(ZERO) >= 0;
    }

    /**
     * Checks value range.
     * 
     * @param position
     * @param errorRange
     * @return
     */
    public final boolean isNear(Num price, double errorRange) {
        return isNear(price, of(errorRange));
    }

    /**
     * Checks value range.
     * 
     * @param position
     * @param errorRange
     * @return
     */
    public final boolean isNear(Num price, Num errorRange) {
        return minus(price).abs().isLessThanOrEqual(errorRange.abs());
    }

    /**
     * Checks if the value is less than zero.
     * 
     * @return true if the value is less than zero, false otherwise
     */
    public final boolean isNegative() {
        return compareTo(ZERO) < 0;
    }

    /**
     * Checks if the value is zero or less.
     * 
     * @return true if the value is zero or less, false otherwise
     */
    public final boolean isNegativeOrZero() {
        return compareTo(ZERO) <= 0;
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
    public final boolean isGreaterThan(long other) {
        return isGreaterThan(of(other));
    }

    /**
     * Checks if this value is greater than another.
     * 
     * @param other the other value, not null
     * @return true is this is greater than the specified value, false otherwise
     */
    public final boolean isGreaterThan(double other) {
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
    public final boolean isGreaterThan(Variable<Num> other) {
        return isGreaterThan(other.get());
    }

    /**
     * Checks if this value is greater than another.
     * 
     * @param other the other value, not null
     * @return true is this is greater than the specified value, false otherwise
     */
    public final boolean isGreaterThan(Num other) {
        return compareTo(other) > 0;
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
    public final boolean isGreaterThanOrEqual(long other) {
        return isGreaterThanOrEqual(of(other));
    }

    /**
     * Checks if this value is greater than or equal to another.
     * 
     * @param other the other value, not null
     * @return true is this is greater than or equal to the specified value, false otherwise
     */
    public final boolean isGreaterThanOrEqual(double other) {
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
    public final boolean isGreaterThanOrEqual(Variable<Num> other) {
        return isGreaterThanOrEqual(other.get());
    }

    /**
     * Checks if this value is greater than or equal to another.
     * 
     * @param other the other value, not null
     * @return true is this is greater than or equal to the specified value, false otherwise
     */
    public final boolean isGreaterThanOrEqual(Num other) {
        return compareTo(other) > -1;
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
    public final boolean isLessThan(long other) {
        return isLessThan(of(other));
    }

    /**
     * Checks if this value is less than another.
     * 
     * @param other the other value, not null
     * @return true is this is less than the specified value, false otherwise
     */
    public final boolean isLessThan(double other) {
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
    public final boolean isLessThan(Variable<Num> other) {
        return isLessThan(other.get());
    }

    /**
     * Checks if this value is less than another.
     * 
     * @param other the other value, not null
     * @return true is this is less than the specified value, false otherwise
     */
    public final boolean isLessThan(Num other) {
        return compareTo(other) < 0;
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
    public final boolean isLessThanOrEqual(long other) {
        return isLessThanOrEqual(of(other));
    }

    /**
     * Checks if this value is less than or equal to another.
     * 
     * @param other the other value, not null
     * @return true is this is less than or equal to the specified value, false otherwise
     */
    public final boolean isLessThanOrEqual(double other) {
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
    public final boolean isLessThanOrEqual(Variable<Num> other) {
        return isLessThanOrEqual(other.get());
    }

    /**
     * Checks if this value is less than or equal to another.
     * 
     * @param other the other value, not null
     * @return true is this is less than or equal to the specified value, false otherwise
     */
    public final boolean isLessThanOrEqual(Num other) {
        return compareTo(other) < 1;
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
     * Decrease amount by the specified {@link Directional}.
     * 
     * @param direction A current side.
     * @param size A decrease size.
     */
    public final Num diff(Directional direction, int size) {
        return diff(direction, of(size));
    }

    /**
     * Decrease amount by the specified {@link Directional}.
     * 
     * @param direction A current side.
     * @param size A decrease size.
     */
    public final Num diff(Directional direction, Num size) {
        return direction.isSell() ? size.minus(this) : minus(size);
    }

    /**
     * Compare {@link Num}.
     * 
     * @param direction
     * @param other
     * @return
     */
    public final boolean isGreaterThan(Directional direction, Num price) {
        return direction.isBuy() ? isGreaterThan(price) : isLessThan(price);
    }

    /**
     * Compare {@link Num}.
     * 
     * @param direction
     * @param other
     * @return
     */
    public final boolean isGreaterThanOrEqual(Directional direction, Num other) {
        return direction.isBuy() ? isGreaterThanOrEqual(other) : isLessThanOrEqual(other);
    }

    /**
     * Compare {@link Num}.
     * 
     * @param direction
     * @param other
     * @return
     */
    public final boolean isLessThan(Directional direction, int other) {
        return isLessThan(direction, Num.of(other));
    }

    /**
     * Compare {@link Num}.
     * 
     * @param direction
     * @param other
     * @return
     */
    public final boolean isLessThan(Directional direction, Num other) {
        return direction.isBuy() ? isLessThan(other) : isGreaterThan(other);
    }

    /**
     * Compare {@link Num}.
     * 
     * @param direction
     * @param other
     * @return
     */
    public final boolean isLessThanOrEqual(Directional direction, Num other) {
        return direction.isBuy() ? isLessThanOrEqual(other) : isGreaterThanOrEqual(other);
    }

    public final String format(int scale) {
        Num num = scale(scale);

        return isPositiveOrZero() ? "+" + num : num.toString();
    }

    /**
     * @param format
     * @return
     */
    public String format(DecimalFormat format) {
        return format.format(delegate);
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

    /**
     * Detect max value.
     * 
     * @param decimals
     * @return
     */
    public static Num max(Num... decimals) {
        Num max = decimals[0];

        for (int i = 1; i < decimals.length; i++) {
            if (max == null || max.isLessThan(decimals[i])) {
                max = decimals[i];
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
        Num min = decimals[0];

        for (int i = 1; i < decimals.length; i++) {
            if (min == null || min.isGreaterThan(decimals[i])) {
                min = decimals[i];
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
    public static Num within(Num min, Num value, Num max) {
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
     * 
     */
    @Manageable(lifestyle = Singleton.class)
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
