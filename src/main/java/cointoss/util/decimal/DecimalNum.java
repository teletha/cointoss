/*
 * Copyright (C) 2019 Nameless Production Committee
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          http://opensource.org/licenses/mit-license.php
 */
package cointoss.util.decimal;

public class DecimalNum extends Decimal<DecimalNum> {

    public static final DecimalNum ZERO = new DecimalNum(0, 0);

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

    /**
     * @param value
     * @param scale
     */
    private DecimalNum(long value, int scale) {
        super(value, scale);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected DecimalNum create(long value, int scale) {
        return new DecimalNum(value, scale);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected DecimalNum zero() {
        return ZERO;
    }

    /**
     * Construct {@link Decimal} by the specified value.
     * 
     * @param value Your value.
     * @return Immutable {@link Decimal}.
     */
    public static Decimal of(int value) {
        return ZERO.create(value);
    }

    /**
     * Construct {@link Decimal} by the specified value.
     * 
     * @param value Your value.
     * @return Immutable {@link Decimal}.
     */
    public static Decimal of(long value) {
        return ZERO.create(value);
    }

    /**
     * Construct {@link Decimal} by the specified value.
     * 
     * @param value Your value.
     * @return Immutable {@link Decimal}.
     */
    public static Decimal of(float value) {
        return ZERO.create(value);
    }

    /**
     * Construct {@link Decimal} by the specified value.
     * 
     * @param value Your value.
     * @return Immutable {@link Decimal}.
     */
    public static Decimal of(double value) {
        return ZERO.create(value);
    }

    /**
     * Construct {@link Decimal} by the specified value.
     * 
     * @param value Your value.
     * @return Immutable {@link Decimal}.
     */
    public static Decimal of(String value) {
        return ZERO.create(value);
    }
}
