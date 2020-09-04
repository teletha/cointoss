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

public class Num extends Decimal<Num> {

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
    public static final Num THOUSAND = ZERO.create(1000);

    /** reuse */
    public static final Num MAX = ZERO.create(Long.MAX_VALUE);

    /** reuse */
    public static final Num MIN = ZERO.create(Long.MIN_VALUE);

    /**
     * @param value
     * @param scale
     */
    private Num(long value, int scale) {
        super(value, scale);
    }

    /**
     * @param value
     */
    private Num(BigDecimal value) {
        super(value);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected Num create(long value, int scale) {
        return new Num(value, scale);
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
     * Construct {@link Decimal} by the specified value.
     * 
     * @param value Your value.
     * @return Immutable {@link Decimal}.
     */
    public static Num of(int value) {
        return ZERO.create(value);
    }

    /**
     * Construct {@link Decimal} by the specified value.
     * 
     * @param value Your value.
     * @return Immutable {@link Decimal}.
     */
    public static Num of(long value) {
        return ZERO.create(value);
    }

    /**
     * Construct {@link Decimal} by the specified value.
     * 
     * @param value Your value.
     * @return Immutable {@link Decimal}.
     */
    public static Num of(float value) {
        return ZERO.create(value);
    }

    /**
     * Construct {@link Decimal} by the specified value.
     * 
     * @param value Your value.
     * @return Immutable {@link Decimal}.
     */
    public static Num of(double value) {
        return ZERO.create(value);
    }

    /**
     * Construct {@link Decimal} by the specified value.
     * 
     * @param value Your value.
     * @return Immutable {@link Decimal}.
     */
    public static Num of(String value) {
        return ZERO.create(value);
    }
}
