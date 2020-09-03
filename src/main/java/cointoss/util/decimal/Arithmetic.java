/*
 * Copyright (C) 2020 Nameless Production Committee
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          http://opensource.org/licenses/mit-license.php
 */
package cointoss.util.decimal;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.text.NumberFormat;

import cointoss.Directional;
import kiss.Variable;

/**
 * 
 */
public abstract class Arithmetic<Self extends Arithmetic> extends Number implements Comparable<Self> {

    /** The base context. */
    protected static final MathContext CONTEXT = new MathContext(15, RoundingMode.HALF_UP);

    /**
     * Build by the specified value.
     * 
     * @param value
     * @return
     */
    protected abstract Self create(int value);

    /**
     * Build by the specified value.
     * 
     * @param value
     * @return
     */
    protected abstract Self create(long value);

    /**
     * Build by the specified value.
     * 
     * @param value
     * @return
     */
    protected abstract Self create(double value);

    /**
     * Build by the specified value.
     * 
     * @param value
     * @return
     */
    protected abstract Self create(String value);

    /**
     * Build by the specified value.
     * 
     * @param value
     * @return
     */
    protected abstract Self create(BigDecimal value);

    protected abstract Self zero();

    /**
     * Returns a {@code Decimal} whose value is {@code (this + augend)}, with rounding according to
     * the context settings.
     * 
     * @param augend value to be added to this {@code Decimal}.
     * @return {@code this + augend}, rounded as necessary
     * @see BigDecimal#add(java.math.BigDecimal, java.math.MathContext)
     */
    public final Self plus(int augend) {
        return plus(create(augend));
    }

    /**
     * Returns a {@code Decimal} whose value is {@code (this + augend)}, with rounding according to
     * the context settings.
     * 
     * @param augend value to be added to this {@code Decimal}.
     * @return {@code this + augend}, rounded as necessary
     * @see BigDecimal#add(java.math.BigDecimal, java.math.MathContext)
     */
    public final Self plus(long augend) {
        return plus(create(augend));
    }

    /**
     * Returns a {@code Decimal} whose value is {@code (this + augend)}, with rounding according to
     * the context settings.
     * 
     * @param augend value to be added to this {@code Decimal}.
     * @return {@code this + augend}, rounded as necessary
     * @see BigDecimal#add(java.math.BigDecimal, java.math.MathContext)
     */
    public final Self plus(double augend) {
        return plus(create(augend));
    }

    /**
     * Returns a {@code Decimal} whose value is {@code (this + augend)}, with rounding according to
     * the context settings.
     * 
     * @param augend value to be added to this {@code Decimal}.
     * @return {@code this + augend}, rounded as necessary
     * @see BigDecimal#add(java.math.BigDecimal, java.math.MathContext)
     */
    public final Self plus(String augend) {
        return plus(create(augend));
    }

    /**
     * Returns a {@code Decimal} whose value is {@code (this + augend)}, with rounding according to
     * the context settings.
     * 
     * @param augend value to be added to this {@code Decimal}.
     * @return {@code this + augend}, rounded as necessary
     * @see BigDecimal#add(java.math.BigDecimal, java.math.MathContext)
     */
    public final Self plus(Variable<Self> augend) {
        return plus(augend.get());
    }

    /**
     * Increase amount by the specified {@link Directional}.
     * 
     * @param direction A current side.
     * @param size A increase size.
     */
    public final Self plus(Directional direction, int size) {
        return plus(direction, create(size));
    }

    /**
     * Increase amount by the specified {@link Directional}.
     * 
     * @param direction A current side.
     * @param size A increase size.
     */
    public final Self plus(Directional direction, Self size) {
        return direction.isBuy() ? plus(size) : minus(size);
    }

    /**
     * Increase amount by the specified {@link Directional}.
     * 
     * @param direction A current side.
     * @param size A increase size.
     */
    public final Self plus(Directional direction, Variable<Self> size) {
        return direction.isBuy() ? plus(size) : minus(size);
    }

    /**
     * Returns a {@code Decimal} whose value is {@code (this + augend)}, with rounding according to
     * the context settings.
     * 
     * @param augend value to be added to this {@code Decimal}.
     * @return {@code this + augend}, rounded as necessary
     * @see BigDecimal#add(java.math.BigDecimal, java.math.MathContext)
     */
    public abstract Self plus(Self augend);

    /**
     * Returns a {@code Decimal} whose value is {@code (this - augend)}, with rounding according to
     * the context settings.
     * 
     * @param subtrahend value to be subtracted from this {@code Decimal}.
     * @return {@code this - subtrahend}, rounded as necessary
     * @see BigDecimal#subtract(java.math.BigDecimal, java.math.MathContext)
     */
    public final Self minus(int subtrahend) {
        return minus(create(subtrahend));
    }

    /**
     * Returns a {@code Decimal} whose value is {@code (this - augend)}, with rounding according to
     * the context settings.
     * 
     * @param subtrahend value to be subtracted from this {@code Decimal}.
     * @return {@code this - subtrahend}, rounded as necessary
     * @see BigDecimal#subtract(java.math.BigDecimal, java.math.MathContext)
     */
    public final Self minus(long subtrahend) {
        return minus(create(subtrahend));
    }

    /**
     * Returns a {@code Decimal} whose value is {@code (this - augend)}, with rounding according to
     * the context settings.
     * 
     * @param subtrahend value to be subtracted from this {@code Decimal}.
     * @return {@code this - subtrahend}, rounded as necessary
     * @see BigDecimal#subtract(java.math.BigDecimal, java.math.MathContext)
     */
    public final Self minus(double subtrahend) {
        return minus(create(subtrahend));
    }

    /**
     * Returns a {@code Decimal} whose value is {@code (this - augend)}, with rounding according to
     * the context settings.
     * 
     * @param subtrahend value to be subtracted from this {@code Decimal}.
     * @return {@code this - subtrahend}, rounded as necessary
     * @see BigDecimal#subtract(java.math.BigDecimal, java.math.MathContext)
     */
    public final Self minus(String subtrahend) {
        return minus(create(subtrahend));
    }

    /**
     * Returns a {@code Decimal} whose value is {@code (this - augend)}, with rounding according to
     * the context settings.
     * 
     * @param subtrahend value to be subtracted from this {@code Decimal}.
     * @return {@code this - subtrahend}, rounded as necessary
     * @see BigDecimal#subtract(java.math.BigDecimal, java.math.MathContext)
     */
    public final Self minus(Variable<Self> subtrahend) {
        return minus(subtrahend.get());
    }

    /**
     * Decrease amount by the specified {@link Directional}.
     * 
     * @param direction A current side.
     * @param size A decrease size.
     */
    public final Self minus(Directional direction, int size) {
        return minus(direction, create(size));
    }

    /**
     * Decrease amount by the specified {@link Directional}.
     * 
     * @param direction A current side.
     * @param size A decrease size.
     */
    public final Self minus(Directional direction, Self size) {
        return direction.isSell() ? plus(size) : minus(size);
    }

    /**
     * Decrease amount by the specified {@link Directional}.
     * 
     * @param direction A current side.
     * @param size A decrease size.
     */
    public final Self minus(Directional direction, Variable<Self> size) {
        return direction.isSell() ? plus(size) : minus(size);
    }

    /**
     * Returns a {@code Decimal} whose value is {@code (this - augend)}, with rounding according to
     * the context settings.
     * 
     * @param subtrahend value to be subtracted from this {@code Decimal}.
     * @return {@code this - subtrahend}, rounded as necessary
     * @see BigDecimal#subtract(java.math.BigDecimal, java.math.MathContext)
     */
    public abstract Self minus(Self subtrahend);

    /**
     * Returns a {@code Decimal} whose value is {@code this * multiplicand}, with rounding according
     * to the context settings.
     * 
     * @param multiplicand value to be multiplied by this {@code Decimal}.
     * @return {@code this * multiplicand}, rounded as necessary
     * @see BigDecimal#multiply(java.math.BigDecimal, java.math.MathContext)
     */
    public final Self multiply(int multiplicand) {
        return multiply(create(multiplicand));
    }

    /**
     * Returns a {@code Decimal} whose value is {@code this * multiplicand}, with rounding according
     * to the context settings.
     * 
     * @param multiplicand value to be multiplied by this {@code Decimal}.
     * @return {@code this * multiplicand}, rounded as necessary
     * @see BigDecimal#multiply(java.math.BigDecimal, java.math.MathContext)
     */
    public final Self multiply(long multiplicand) {
        return multiply(create(multiplicand));
    }

    /**
     * Returns a {@code Decimal} whose value is {@code this * multiplicand}, with rounding according
     * to the context settings.
     * 
     * @param multiplicand value to be multiplied by this {@code Decimal}.
     * @return {@code this * multiplicand}, rounded as necessary
     * @see BigDecimal#multiply(java.math.BigDecimal, java.math.MathContext)
     */
    public final Self multiply(double multiplicand) {
        return multiply(create(multiplicand));
    }

    /**
     * Returns a {@code Decimal} whose value is {@code this * multiplicand}, with rounding according
     * to the context settings.
     * 
     * @param multiplicand value to be multiplied by this {@code Decimal}.
     * @return {@code this * multiplicand}, rounded as necessary
     * @see BigDecimal#multiply(java.math.BigDecimal, java.math.MathContext)
     */
    public final Self multiply(String multiplicand) {
        return multiply(create(multiplicand));
    }

    /**
     * Returns a {@code Decimal} whose value is {@code this * multiplicand}, with rounding according
     * to the context settings.
     * 
     * @param multiplicand value to be multiplied by this {@code Decimal}.
     * @return {@code this * multiplicand}, rounded as necessary
     * @see BigDecimal#multiply(java.math.BigDecimal, java.math.MathContext)
     */
    public final Self multiply(Variable<Self> multiplicand) {
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
    public abstract Self multiply(Self multiplicand);

    /**
     * Returns a {@code Decimal} whose value is {@code (this / divisor)}, with rounding according to
     * the context settings.
     * 
     * @param divisor value by which this {@code Decimal} is to be divided.
     * @return {@code this / divisor}, rounded as necessary
     * @see BigDecimal#divide(java.math.BigDecimal, java.math.MathContext)
     */
    public final Self divide(int divisor) {
        return divide(create(divisor));
    }

    /**
     * Returns a {@code Decimal} whose value is {@code (this / divisor)}, with rounding according to
     * the context settings.
     * 
     * @param divisor value by which this {@code Decimal} is to be divided.
     * @return {@code this / divisor}, rounded as necessary
     * @see BigDecimal#divide(java.math.BigDecimal, java.math.MathContext)
     */
    public final Self divide(long divisor) {
        return divide(create(divisor));
    }

    /**
     * Returns a {@code Decimal} whose value is {@code (this / divisor)}, with rounding according to
     * the context settings.
     * 
     * @param divisor value by which this {@code Decimal} is to be divided.
     * @return {@code this / divisor}, rounded as necessary
     * @see BigDecimal#divide(java.math.BigDecimal, java.math.MathContext)
     */
    public final Self divide(double divisor) {
        return divide(create(divisor));
    }

    /**
     * Returns a {@code Decimal} whose value is {@code (this / divisor)}, with rounding according to
     * the context settings.
     * 
     * @param divisor value by which this {@code Decimal} is to be divided.
     * @return {@code this / divisor}, rounded as necessary
     * @see BigDecimal#divide(java.math.BigDecimal, java.math.MathContext)
     */
    public final Self divide(String divisor) {
        return divide(create(divisor));
    }

    /**
     * Returns a {@code Decimal} whose value is {@code (this / divisor)}, with rounding according to
     * the context settings.
     * 
     * @param divisor value by which this {@code Decimal} is to be divided.
     * @return {@code this / divisor}, rounded as necessary
     * @see BigDecimal#divide(java.math.BigDecimal, java.math.MathContext)
     */
    public final Self divide(Variable<Self> divisor) {
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
    public abstract Self divide(Self divisor);

    /**
     * Returns a {@code Decimal} whose value is {@code (this % divisor)}, with rounding according to
     * the context settings.
     * 
     * @param divisor value by which this {@code Decimal} is to be divided.
     * @return {@code this % divisor}, rounded as necessary.
     * @see BigDecimal#remainder(java.math.BigDecimal, java.math.MathContext)
     */
    public final Self remainder(int divisor) {
        return remainder(create(divisor));
    }

    /**
     * Returns a {@code Decimal} whose value is {@code (this % divisor)}, with rounding according to
     * the context settings.
     * 
     * @param divisor value by which this {@code Decimal} is to be divided.
     * @return {@code this % divisor}, rounded as necessary.
     * @see BigDecimal#remainder(java.math.BigDecimal, java.math.MathContext)
     */
    public final Self remainder(long divisor) {
        return remainder(create(divisor));
    }

    /**
     * Returns a {@code Decimal} whose value is {@code (this % divisor)}, with rounding according to
     * the context settings.
     * 
     * @param divisor value by which this {@code Decimal} is to be divided.
     * @return {@code this % divisor}, rounded as necessary.
     * @see BigDecimal#remainder(java.math.BigDecimal, java.math.MathContext)
     */
    public final Self remainder(double divisor) {
        return remainder(create(divisor));
    }

    /**
     * Returns a {@code Decimal} whose value is {@code (this % divisor)}, with rounding according to
     * the context settings.
     * 
     * @param divisor value by which this {@code Decimal} is to be divided.
     * @return {@code this % divisor}, rounded as necessary.
     * @see BigDecimal#remainder(java.math.BigDecimal, java.math.MathContext)
     */
    public final Self remainder(String divisor) {
        return remainder(create(divisor));
    }

    /**
     * Returns a {@code Decimal} whose value is {@code (this % divisor)}, with rounding according to
     * the context settings.
     * 
     * @param divisor value by which this {@code Decimal} is to be divided.
     * @return {@code this % divisor}, rounded as necessary.
     * @see BigDecimal#remainder(java.math.BigDecimal, java.math.MathContext)
     */
    public final Self remainder(Variable<Self> divisor) {
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
    public abstract Self remainder(Self divisor);

    /**
     * Checks if this value is greater than another.
     * 
     * @param other the other value, not null
     * @return true is this is greater than the specified value, false otherwise
     */
    public final boolean isGreaterThan(int other) {
        return isGreaterThan(create(other));
    }

    /**
     * Checks if this value is greater than another.
     * 
     * @param other the other value, not null
     * @return true is this is greater than the specified value, false otherwise
     */
    public final boolean isGreaterThan(long other) {
        return isGreaterThan(create(other));
    }

    /**
     * Checks if this value is greater than another.
     * 
     * @param other the other value, not null
     * @return true is this is greater than the specified value, false otherwise
     */
    public final boolean isGreaterThan(double other) {
        return isGreaterThan(create(other));
    }

    /**
     * Checks if this value is greater than another.
     * 
     * @param other the other value, not null
     * @return true is this is greater than the specified value, false otherwise
     */
    public final boolean isGreaterThan(String other) {
        return isGreaterThan(create(other));
    }

    /**
     * Checks if this value is greater than another.
     * 
     * @param other the other value, not null
     * @return true is this is greater than the specified value, false otherwise
     */
    public final boolean isGreaterThan(Variable<Self> other) {
        return isGreaterThan(other.get());
    }

    /**
     * Checks if this value is greater than another.
     * 
     * @param other the other value, not null
     * @return true is this is greater than the specified value, false otherwise
     */
    public final boolean isGreaterThan(Self other) {
        return compareTo(other) > 0;
    }

    /**
     * Compare {@link Self}.
     * 
     * @param direction
     * @param other
     * @return
     */
    public final boolean isGreaterThan(Directional direction, Self price) {
        return direction.isBuy() ? isGreaterThan(price) : isLessThan(price);
    }

    /**
     * Compare {@link Self}.
     * 
     * @param direction
     * @param other
     * @return
     */
    public final boolean isGreaterThan(Directional direction, Variable<Self> price) {
        return direction.isBuy() ? isGreaterThan(price) : isLessThan(price);
    }

    /**
     * Checks if this value is greater than or equal to another.
     * 
     * @param other the other value, not null
     * @return true is this is greater than or equal to the specified value, false otherwise
     */
    public final boolean isGreaterThanOrEqual(int other) {
        return isGreaterThanOrEqual(create(other));
    }

    /**
     * Checks if this value is greater than or equal to another.
     * 
     * @param other the other value, not null
     * @return true is this is greater than or equal to the specified value, false otherwise
     */
    public final boolean isGreaterThanOrEqual(long other) {
        return isGreaterThanOrEqual(create(other));
    }

    /**
     * Checks if this value is greater than or equal to another.
     * 
     * @param other the other value, not null
     * @return true is this is greater than or equal to the specified value, false otherwise
     */
    public final boolean isGreaterThanOrEqual(double other) {
        return isGreaterThanOrEqual(create(other));
    }

    /**
     * Checks if this value is greater than or equal to another.
     * 
     * @param other the other value, not null
     * @return true is this is greater than or equal to the specified value, false otherwise
     */
    public final boolean isGreaterThanOrEqual(String other) {
        return isGreaterThanOrEqual(create(other));
    }

    /**
     * Checks if this value is greater than or equal to another.
     * 
     * @param other the other value, not null
     * @return true is this is greater than or equal to the specified value, false otherwise
     */
    public final boolean isGreaterThanOrEqual(Variable<Self> other) {
        return isGreaterThanOrEqual(other.get());
    }

    /**
     * Checks if this value is greater than or equal to another.
     * 
     * @param other the other value, not null
     * @return true is this is greater than or equal to the specified value, false otherwise
     */
    public final boolean isGreaterThanOrEqual(Self other) {
        return compareTo(other) > -1;
    }

    /**
     * Compare {@link Self}.
     * 
     * @param direction
     * @param other
     * @return
     */
    public final boolean isGreaterThanOrEqual(Directional direction, Self other) {
        return direction.isBuy() ? isGreaterThanOrEqual(other) : isLessThanOrEqual(other);
    }

    /**
     * Compare {@link Self}.
     * 
     * @param direction
     * @param other
     * @return
     */
    public final boolean isGreaterThanOrEqual(Directional direction, Variable<Self> other) {
        return direction.isBuy() ? isGreaterThanOrEqual(other) : isLessThanOrEqual(other);
    }

    /**
     * Checks if this value is less than another.
     * 
     * @param other the other value, not null
     * @return true is this is less than the specified value, false otherwise
     */
    public final boolean isLessThan(int other) {
        return isLessThan(create(other));
    }

    /**
     * Checks if this value is less than another.
     * 
     * @param other the other value, not null
     * @return true is this is less than the specified value, false otherwise
     */
    public final boolean isLessThan(long other) {
        return isLessThan(create(other));
    }

    /**
     * Checks if this value is less than another.
     * 
     * @param other the other value, not null
     * @return true is this is less than the specified value, false otherwise
     */
    public final boolean isLessThan(double other) {
        return isLessThan(create(other));
    }

    /**
     * Checks if this value is less than another.
     * 
     * @param other the other value, not null
     * @return true is this is less than the specified value, false otherwise
     */
    public final boolean isLessThan(String other) {
        return isLessThan(create(other));
    }

    /**
     * Checks if this value is less than another.
     * 
     * @param other the other value, not null
     * @return true is this is less than the specified value, false otherwise
     */
    public final boolean isLessThan(Variable<Self> other) {
        return isLessThan(other.get());
    }

    /**
     * Checks if this value is less than another.
     * 
     * @param other the other value, not null
     * @return true is this is less than the specified value, false otherwise
     */
    public final boolean isLessThan(Self other) {
        return compareTo(other) < 0;
    }

    /**
     * Compare {@link Self}.
     * 
     * @param direction
     * @param other
     * @return
     */
    public final boolean isLessThan(Directional direction, int other) {
        return isLessThan(direction, create(other));
    }

    /**
     * Compare {@link Self}.
     * 
     * @param direction
     * @param other
     * @return
     */
    public final boolean isLessThan(Directional direction, Self other) {
        return direction.isBuy() ? isLessThan(other) : isGreaterThan(other);
    }

    /**
     * Compare {@link Self}.
     * 
     * @param direction
     * @param other
     * @return
     */
    public final boolean isLessThan(Directional direction, Variable<Self> other) {
        return direction.isBuy() ? isLessThan(other) : isGreaterThan(other);
    }

    /**
     * Checks if this value is less than or equal to another.
     * 
     * @param other the other value, not null
     * @return true is this is less than or equal to the specified value, false otherwise
     */
    public final boolean isLessThanOrEqual(int other) {
        return isLessThanOrEqual(create(other));
    }

    /**
     * Checks if this value is less than or equal to another.
     * 
     * @param other the other value, not null
     * @return true is this is less than or equal to the specified value, false otherwise
     */
    public final boolean isLessThanOrEqual(long other) {
        return isLessThanOrEqual(create(other));
    }

    /**
     * Checks if this value is less than or equal to another.
     * 
     * @param other the other value, not null
     * @return true is this is less than or equal to the specified value, false otherwise
     */
    public final boolean isLessThanOrEqual(double other) {
        return isLessThanOrEqual(create(other));
    }

    /**
     * Checks if this value is less than or equal to another.
     * 
     * @param other the other value, not null
     * @return true is this is less than or equal to the specified value, false otherwise
     */
    public final boolean isLessThanOrEqual(String other) {
        return isLessThanOrEqual(create(other));
    }

    /**
     * Checks if this value is less than or equal to another.
     * 
     * @param other the other value, not null
     * @return true is this is less than or equal to the specified value, false otherwise
     */
    public final boolean isLessThanOrEqual(Variable<Self> other) {
        return isLessThanOrEqual(other.get());
    }

    /**
     * Checks if this value is less than or equal to another.
     * 
     * @param other the other value, not null
     * @return true is this is less than or equal to the specified value, false otherwise
     */
    public final boolean isLessThanOrEqual(Self other) {
        return compareTo(other) < 1;
    }

    /**
     * Compare {@link Self}.
     * 
     * @param direction
     * @param other
     * @return
     */
    public final boolean isLessThanOrEqual(Directional direction, Self other) {
        return direction.isBuy() ? isLessThanOrEqual(other) : isGreaterThanOrEqual(other);
    }

    /**
     * Compare {@link Self}.
     * 
     * @param direction
     * @param other
     * @return
     */
    public final boolean isLessThanOrEqual(Directional direction, Variable<Self> other) {
        return direction.isBuy() ? isLessThanOrEqual(other) : isGreaterThanOrEqual(other);
    }

    /**
     * Decrease amount by the specified {@link Directional}.
     * 
     * @param direction A current side.
     * @param size A decrease size.
     */
    public final Self diff(Directional direction, int size) {
        return diff(direction, create(size));
    }

    /**
     * Decrease amount by the specified {@link Directional}.
     * 
     * @param direction A current side.
     * @param size A decrease size.
     */
    public final Self diff(Directional direction, Self size) {
        return direction.isSell() ? (Self) size.minus(this) : minus(size);
    }

    /**
     * Compare {@link Self}.
     * 
     * @param other
     * @return A result.
     */
    public final boolean is(int other) {
        return is(create(other));
    }

    /**
     * Compare {@link Self}.
     * 
     * @param other
     * @return A result.
     */
    public final boolean is(long other) {
        return is(create(other));
    }

    /**
     * Compare {@link Self}.
     * 
     * @param other
     * @return A result.
     */
    public final boolean is(double other) {
        return is(create(other));
    }

    /**
     * Compare {@link Self}.
     * 
     * @param other
     * @return A result.
     */
    public final boolean is(String other) {
        return is(create(other));
    }

    /**
     * Compare {@link Self}.
     * 
     * @param other
     * @return A result.
     */
    public final boolean is(Variable<Self> other) {
        return is(other.get());
    }

    /**
     * Checks if this value is equal to another.
     * 
     * @param other the other value, not null
     * @return true is this is greater than the specified value, false otherwise
     */
    public final boolean is(Self other) {
        return compareTo(other) == 0;
    }

    /**
     * Compare {@link Self}.
     * 
     * @param other the other value, not null
     * @return true is this is greater than the specified value, false otherwise
     */
    public final boolean isNot(int other) {
        return !is(other);
    }

    /**
     * Compare {@link Self}.
     * 
     * @param other the other value, not null
     * @return true is this is greater than the specified value, false otherwise
     */
    public final boolean isNot(long other) {
        return !is(other);
    }

    /**
     * Compare {@link Self}.
     * 
     * @param other the other value, not null
     * @return true is this is greater than the specified value, false otherwise
     */
    public final boolean isNot(double other) {
        return !is(other);
    }

    /**
     * Compare {@link Self}.
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
    public final boolean isNot(Variable<Self> other) {
        return !is(other);
    }

    /**
     * Checks if this value is equal to another.
     * 
     * @param other the other value, not null
     * @return true is this is greater than the specified value, false otherwise
     */
    public final boolean isNot(Self other) {
        return !is(other);
    }

    /**
     * Checks if the value is zero.
     * 
     * @return true if the value is zero, false otherwise
     */
    public final boolean isZero() {
        return compareTo(zero()) == 0;
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
        return compareTo(zero()) > 0;
    }

    /**
     * Checks if the value is zero or greater.
     * 
     * @return true if the value is zero or greater, false otherwise
     */
    public final boolean isPositiveOrZero() {
        return compareTo(zero()) >= 0;
    }

    /**
     * Checks if the value is less than zero.
     * 
     * @return true if the value is less than zero, false otherwise
     */
    public final boolean isNegative() {
        return compareTo(zero()) < 0;
    }

    /**
     * Checks if the value is zero or less.
     * 
     * @return true if the value is zero or less, false otherwise
     */
    public final boolean isNegativeOrZero() {
        return compareTo(zero()) <= 0;
    }

    /**
     * Returns a {@link Self} whose Selferical value is equal to ({@code this} * 10<sup>n</sup>).
     * The scale of the result is {@code (this.scale() - n)}.
     *
     * @param n the exponent power of ten to scale by
     * @return a {@link Self} whose Selferical value is equal to ({@code this} * 10<sup>n</sup>)
     * @throws ArithmeticException if the scale would be outside the range of a 32-bit integer.
     */
    public abstract Self decuple(int n);

    /**
     * Returns a {@code Self} whose value is <code>(this<sup>n</sup>)</code>. The current
     * implementation uses the core algorithm defined in ANSI standard X3.274-1996 with rounding
     * according to the context settings. In general, the returned Selferical value is within two
     * ulps of the exact Selferical value for the chosen precision. Note that future releases may
     * use a different algorithm with a decreased allowable error bound and increased allowable
     * exponent range.
     *
     * @param n power to raise this {@code Self} to.
     * @param mc the context to use.
     * @return <code>this<sup>n</sup></code> using the ANSI standard X3.274-1996 algorithm
     * @throws ArithmeticException if the result is inexact but the rounding mode is
     *             {@code UNNECESSARY}, or {@code n} is out of range.
     */
    public abstract Self pow(int n);

    /**
     * Returns a {@code Self} whose value is <code>(this<sup>n</sup>)</code>. The current
     * implementation uses the core algorithm defined in ANSI standard X3.274-1996 with rounding
     * according to the context settings. In general, the returned Selferical value is within two
     * ulps of the exact Selferical value for the chosen precision. Note that future releases may
     * use a different algorithm with a decreased allowable error bound and increased allowable
     * exponent range.
     *
     * @param n power to raise this {@code Self} to.
     * @param mc the context to use.
     * @return <code>this<sup>n</sup></code> using the ANSI standard X3.274-1996 algorithm
     * @throws ArithmeticException if the result is inexact but the rounding mode is
     *             {@code UNNECESSARY}, or {@code n} is out of range.
     */
    public abstract Self pow(double n);

    /**
     * Returns an approximation to the square root of {@code this} with rounding according to the
     * context settings.
     * <p>
     * The preferred scale of the returned result is equal to {@code this.scale()/2}. The value of
     * the returned result is always within one ulp of the exact decimal value for the precision in
     * question. If the rounding mode is {@link RoundingMode#HALF_UP HALF_UP},
     * {@link RoundingMode#HALF_DOWN HALF_DOWN}, or {@link RoundingMode#HALF_EVEN HALF_EVEN}, the
     * result is within one half an ulp of the exact decimal value.
     *
     * @return the square root of {@code this}.
     * @throws ArithmeticException if {@code this} is less than zero.
     * @throws ArithmeticException if an exact result is requested ({@code mc.getPrecision()==0})
     *             and there is no finite decimal expansion of the exact result
     * @throws ArithmeticException if {@code (mc.getRoundingMode()==RoundingMode.UNNECESSARY}) and
     *             the exact result cannot fit in {@code mc.getPrecision()} digits.
     */
    public abstract Self sqrt();

    /**
     * Returns a {@code Self} whose value is the absolute value of this {@code Self}, and whose
     * scale is {@code this.scale()}.
     *
     * @return {@code abs(this)}
     */
    public abstract Self abs();

    /**
     * Returns a {@code Self} whose value is {@code (-this)}, and whose scale is
     * {@code this.scale()}.
     *
     * @return {@code -this}.
     */
    public abstract Self negate();

    /**
     * Compute scale.
     */
    public abstract int scale();

    /**
     * Returns a {@code Self} whose scale is the specified value, and whose unscaled value is
     * determined by multiplying or dividing this {@code Self}'s unscaled value by the appropriate
     * power of ten to maintain its overall value. If the scale is reduced by the operation, the
     * unscaled value must be divided (rather than multiplied), and the value may be changed; in
     * this case, the specified rounding mode is applied to the division.
     *
     * @param size scale of the {@code Self} value to be returned.
     * @return a {@code Self} whose scale is the specified value, and whose unscaled value is
     *         determined by multiplying or dividing this {@code Self}'s unscaled value by the
     *         appropriate power of ten to maintain its overall value.
     * @throws ArithmeticException if {@code roundingMode==UNNECESSARY} and the specified scaling
     *             operation would require rounding.
     */
    public final Self scale(int size) {
        return scale(size, CONTEXT.getRoundingMode());
    }

    /**
     * Returns a {@code Self} whose scale is the specified value, and whose unscaled value is
     * determined by multiplying or dividing this {@code Self}'s unscaled value by the appropriate
     * power of ten to maintain its overall value. If the scale is reduced by the operation, the
     * unscaled value must be divided (rather than multiplied), and the value may be changed; in
     * this case, the specified rounding mode is applied to the division.
     *
     * @param size scale of the {@code Self} value to be returned.
     * @param mode The rounding mode to apply.
     * @return a {@code Self} whose scale is the specified value, and whose unscaled value is
     *         determined by multiplying or dividing this {@code Self}'s unscaled value by the
     *         appropriate power of ten to maintain its overall value.
     * @throws ArithmeticException if {@code roundingMode==UNNECESSARY} and the specified scaling
     *             operation would require rounding.
     */
    public abstract Self scale(int size, RoundingMode mode);

    /**
     * Format this Selfber.
     * 
     * @param format Your Selfber format.
     * @return A formatted value.
     */
    public abstract String format(NumberFormat format);
}