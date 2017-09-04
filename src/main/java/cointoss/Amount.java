/*
 * Copyright (C) 2017 Nameless Production Committee
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          http://opensource.org/licenses/mit-license.php
 */
package cointoss;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;

import kiss.Decoder;
import kiss.Encoder;

/**
 * @version 2017/07/24 15:31:51
 */
public class Amount {

    private static final int SCALE = 10;

    /** The base context. */
    private static final MathContext MC = MathContext.UNLIMITED;

    /** reuse */
    public static final Amount ZERO = new Amount(BigDecimal.ZERO);

    /** reuse */
    public static final Amount ONE = new Amount(BigDecimal.ONE);

    /** reuse */
    public static final Amount TWO = new Amount(new BigDecimal(2));

    /** reuse */
    public static final Amount TEN = new Amount(BigDecimal.TEN);

    /** reuse */
    public static final Amount HUNDRED = new Amount(new BigDecimal(100));

    /** reuse */
    public static final Amount MAX = new Amount(new BigDecimal(Long.MAX_VALUE));

    /** The actual value. */
    protected BigDecimal value;

    /**
     * @param value
     */
    public Amount(String value) {
        this.value = new BigDecimal(value, MC).setScale(SCALE, RoundingMode.HALF_UP);
    }

    /**
     * @param value
     */
    protected Amount(BigDecimal value) {
        this.value = value.setScale(SCALE, RoundingMode.HALF_UP);
    }

    /**
     * @return
     */
    public final long longValue() {
        return value.longValue();
    }

    /**
     * Compute absolute amount.
     * 
     * @return
     */
    public Amount abs() {
        return new Amount(value.abs());
    }

    /**
     * Increase amount.
     * 
     * @param size
     */
    public final Amount plus(int size) {
        return plus(Amount.of(size));
    }

    /**
     * Increase amount.
     * 
     * @param size
     */
    public Amount plus(Amount size) {
        return new Amount(value.add(size.value));
    }

    /**
     * Increase amount by the specified {@link Directional}.
     * 
     * @param direction A current side.
     * @param size A increase size.
     */
    public final Amount plus(Directional direction, int size) {
        return plus(direction, Amount.of(size));
    }

    /**
     * Increase amount by the specified {@link Directional}.
     * 
     * @param direction A current side.
     * @param size A increase size.
     */
    public final Amount plus(Directional direction, Amount size) {
        return direction.isBuy() ? plus(size) : minus(size);
    }

    /**
     * Decrease amount.
     * 
     * @param size
     */
    public final Amount minus(int size) {
        return minus(Amount.of(size));
    }

    /**
     * Decrease amount.
     * 
     * @param size
     */
    public Amount minus(Amount size) {
        return new Amount(value.subtract(size.value));
    }

    /**
     * Decrease amount by the specified {@link Directional}.
     * 
     * @param direction A current side.
     * @param size A decrease size.
     */
    public final Amount minus(Directional direction, int size) {
        return minus(direction, Amount.of(size));
    }

    /**
     * Decrease amount by the specified {@link Directional}.
     * 
     * @param direction A current side.
     * @param size A decrease size.
     */
    public final Amount minus(Directional direction, Amount size) {
        return direction.isSell() ? plus(size) : minus(size);
    }

    /**
     * Multiply amount.
     * 
     * @param size
     * @return
     */
    public final Amount multiply(int size) {
        return multiply(Amount.of(size));
    }

    /**
     * Multiply amount.
     * 
     * @param size
     * @return
     */
    public Amount multiply(Amount size) {
        return new Amount(value.multiply(size.value));
    }

    /**
     * Divide amount.
     * 
     * @param size
     * @return
     */
    public final Amount divide(int size) {
        return divide(Amount.of(size));
    }

    /**
     * Divide amount.
     * 
     * @param size
     * @return
     */
    public Amount divide(Amount size) {
        return new Amount(value.divide(size.value, 10, RoundingMode.HALF_UP));
    }

    /**
     * Multiply amount by the specified {@link Directional}.
     * 
     * @param size
     * @return
     */
    public Amount ratio(Directional direction, double size) {
        return ratio(direction, new Amount(new BigDecimal(String.valueOf(size))));
    }

    /**
     * Multiply amount by the specified {@link Directional}.
     * 
     * @param size
     * @return
     */
    public final Amount ratio(Directional direction, Amount size) {
        return direction.isBuy() ? multiply(HUNDRED.plus(size).divide(HUNDRED)) : multiply(HUNDRED.minus(size).divide(HUNDRED));
    }

    /**
     * Compare {@link Amount}.
     * 
     * @param amount
     * @return A result.
     */
    public final boolean isGreaterThan(int amount) {
        return isGreaterThan(Amount.of(amount));
    }

    /**
     * Compare {@link Amount}.
     * 
     * @param amount
     * @return A result.
     */
    public final boolean isGreaterThan(Amount amount) {
        return value.compareTo(amount.value) > 0;
    }

    /**
     * Compare {@link Amount}.
     * 
     * @param amount
     * @return A result.
     */
    public final boolean isGreaterThan(Directional direction, Amount price) {
        return direction.isBuy() ? isGreaterThan(price) : isLessThan(price);
    }

    /**
     * @param i
     * @return
     */
    public final boolean isEqualOrGreaterThan(int amount) {
        return isEqualOrGreaterThan(Amount.of(amount));
    }

    /**
     * Compare {@link Amount}.
     * 
     * @param amount
     * @return A result.
     */
    public final boolean isEqualOrGreaterThan(Amount amount) {
        return value.compareTo(amount.value) >= 0;
    }

    /**
     * Compare {@link Amount}.
     * 
     * @param amount
     * @return A result.
     */
    public final boolean isLessThan(int amount) {
        return isLessThan(Amount.of(amount));
    }

    /**
     * Compare {@link Amount}.
     * 
     * @param amount
     * @return A result.
     */
    public final boolean isLessThan(Amount amount) {
        return value.compareTo(amount.value) < 0;
    }

    /**
     * Compare {@link Amount}.
     * 
     * @param amount
     * @return A result.
     */
    public final boolean isLessThan(Directional direction, Amount price) {
        return direction.isBuy() ? isLessThan(price) : isGreaterThan(price);
    }

    /**
     * @param amount
     * @return
     */
    public final boolean isEqualOrLessThan(int amount) {
        return isEqualOrLessThan(Amount.of(amount));
    }

    /**
     * Compare {@link Amount}.
     * 
     * @param amount
     * @return A result.
     */
    public final boolean isEqualOrLessThan(Amount amount) {
        return value.compareTo(amount.value) <= 0;
    }

    /**
     * Compare {@link Amount}.
     * 
     * @param amount
     * @return A result.
     */
    public final boolean isEqualTo(Amount amount) {
        return value.compareTo(amount.value) == 0;
    }

    /**
     * Compare {@link Amount}.
     * 
     * @param amount
     * @return A result.
     */
    public final boolean isNotEqualTo(Amount amount) {
        return value.compareTo(amount.value) != 0;
    }

    /**
     * Compare {@link Amount}.
     * 
     * @param amount
     * @return A result.
     */
    public final boolean is(int amount) {
        return isEqualTo(Amount.of(amount));
    }

    /**
     * Compare {@link Amount}.
     * 
     * @param amount
     * @return A result.
     */
    public final boolean is(String amount) {
        return isEqualTo(new Amount(amount));
    }

    /**
     * Compare {@link Amount}.
     * 
     * @param amount
     * @return A result.
     */
    public final boolean isNot(int amount) {
        return isNotEqualTo(Amount.of(amount));
    }

    /**
     * @return
     */
    public final boolean isZero() {
        return isEqualTo(ZERO);
    }

    /**
     * @return
     */
    public final boolean isPositive() {
        return isEqualOrGreaterThan(ZERO);
    }

    /**
     * @return
     */
    public final boolean isNegative() {
        return isLessThan(ZERO);
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
        return String.format("%," + size + ".0f円", value.doubleValue());
    }

    /**
     * Format as BTC.
     * 
     * @return
     */
    public final String asBTC() {
        return String.format("B%3.4f", value.doubleValue());
    }

    /**
     * Remove fractional portion.
     * 
     * @return
     */
    public Amount integral() {
        return new Amount(value.setScale(0, RoundingMode.HALF_UP));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final String toString() {
        return value.stripTrailingZeros().toPlainString();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final int hashCode() {
        return value.hashCode();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final boolean equals(Object obj) {
        if (obj instanceof Amount) {
            return value.equals(((Amount) obj).value);
        }
        return false;
    }

    /**
     * <p>
     * Return the maximum amount.
     * </p>
     * 
     * @param amounts
     * @return The maximum amount.
     */
    public static Amount max(Amount... amounts) {
        Amount max = amounts[0];

        for (int i = 1; i < amounts.length; i++) {
            if (amounts[i].isGreaterThan(max)) {
                max = amounts[i];
            }
        }
        return max;
    }

    /**
     * <p>
     * Return the minmum amount.
     * </p>
     * 
     * @param amounts
     * @return The minimum amount.
     */
    public static Amount min(Amount... amounts) {
        Amount min = amounts[0];

        for (int i = 1; i < amounts.length; i++) {
            if (amounts[i].isLessThan(min)) {
                min = amounts[i];
            }
        }
        return min;
    }

    public static Amount of(int value) {
        return new Amount(String.valueOf(value));
    }

    /**
     * @version 2017/07/26 9:08:16
     */
    public static class Codec implements Encoder<Amount>, Decoder<Amount> {

        /**
         * {@inheritDoc}
         */
        @Override
        public Amount decode(String value) {
            return new Amount(value);
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public String encode(Amount value) {
            return value.toString();
        }
    }
}
