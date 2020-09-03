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

import java.math.BigDecimal;

public class DecimalNum extends Decimal<DecimalNum> {

    /** reuse */
    public static final DecimalNum ZERO = new DecimalNum(0, 0);

    /** reuse */
    public static final DecimalNum ONE = ZERO.create(1);

    /** reuse */
    public static final DecimalNum TWO = ZERO.create(2);

    /** reuse */
    public static final DecimalNum THREE = ZERO.create(3);

    /** reuse */
    public static final DecimalNum TEN = ZERO.create(10);

    /** reuse */
    public static final DecimalNum HUNDRED = ZERO.create(100);

    /** reuse */
    public static final DecimalNum THOUSAND = ZERO.create(1000);

    /** reuse */
    public static final DecimalNum MAX = ZERO.create(Long.MAX_VALUE);

    /** reuse */
    public static final DecimalNum MIN = ZERO.create(Long.MIN_VALUE);

    /**
     * @param value
     * @param scale
     */
    private DecimalNum(long value, int scale) {
        super(value, scale);
    }

    /**
     * @param value
     */
    public DecimalNum(BigDecimal value) {
        super(value);
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
    public static DecimalNum of(int value) {
        return ZERO.create(value);
    }

    /**
     * Construct {@link Decimal} by the specified value.
     * 
     * @param value Your value.
     * @return Immutable {@link Decimal}.
     */
    public static DecimalNum of(long value) {
        return ZERO.create(value);
    }

    /**
     * Construct {@link Decimal} by the specified value.
     * 
     * @param value Your value.
     * @return Immutable {@link Decimal}.
     */
    public static DecimalNum of(float value) {
        return ZERO.create(value);
    }

    /**
     * Construct {@link Decimal} by the specified value.
     * 
     * @param value Your value.
     * @return Immutable {@link Decimal}.
     */
    public static DecimalNum of(double value) {
        return ZERO.create(value);
    }

    /**
     * Construct {@link Decimal} by the specified value.
     * 
     * @param value Your value.
     * @return Immutable {@link Decimal}.
     */
    public static DecimalNum of(String value) {
        return ZERO.create(value);
    }
}
