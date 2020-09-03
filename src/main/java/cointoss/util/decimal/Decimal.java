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

import cointoss.util.math.BigDecimal;

public class Decimal extends Number {

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

    public Decimal add(Decimal value) {
        if (scale == value.scale) {
            return new Decimal(v + value.v, scale);
        } else if (scale < value.scale) {

        }
        return new Decimal(v / value.v, scale - value.scale);
    }

    public Decimal multiply(Decimal value) {
        return new Decimal(v * value.v, scale + value.scale);
    }

    public Decimal divide(Decimal value) {
        Decimal result = Decimal.of((double) v / value.v);
        result.scale = scale - value.scale + result.scale;
        return result;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int intValue() {
        return (int) (v * pow(-scale));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public long longValue() {
        return 0;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public float floatValue() {
        return 0;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public double doubleValue() {
        return v * pow(-scale);
    }

    private double pow(int scale) {
        switch (scale) {
        case 0:
            return 1;
        case 1:
            return 10;
        case 2:
            return 100;
        case 3:
            return 1000;
        case 4:
            return 10000;
        case 5:
            return 100000;
        case 6:
            return 1000000;
        case 7:
            return 10000000;
        case 8:
            return 100000000;
        case 9:
            return 1000000000;
        case 10:
            return 10000000000d;
        case 11:
            return 100000000000d;
        case 12:
            return 1000000000000d;
        case 13:
            return 10000000000000d;
        case 14:
            return 100000000000000d;
        case 15:
            return 1000000000000000d;
        case 16:
            return 10000000000000000d;
        case 17:
            return 100000000000000000d;
        case 18:
            return 1000000000000000000d;
        case 19:
            return 10000000000000000000d;
        case 20:
            return 100000000000000000000d;
        case 21:
            return 1000000000000000000000d;
        case 22:
            return 10000000000000000000000d;
        case 23:
            return 100000000000000000000000d;
        case 24:
            return 1000000000000000000000000d;
        case 25:
            return 10000000000000000000000000d;
        case 26:
            return 100000000000000000000000000d;
        case 27:
            return 1000000000000000000000000000d;
        case 28:
            return 10000000000000000000000000000d;
        case 29:
            return 100000000000000000000000000000d;
        case 30:
            return 1000000000000000000000000000000d;

        case -1:
            return 0.1;
        case -2:
            return 0.01;
        case -3:
            return 0.001;
        case -4:
            return 0.0001;
        case -5:
            return 0.00001;
        case -6:
            return 0.000001;
        case -7:
            return 0.0000001;
        case -8:
            return 0.00000001;
        case -9:
            return 0.000000001;
        case -10:
            return 0.0000000001;
        case -11:
            return 0.00000000001;
        case -12:
            return 0.000000000001;
        case -13:
            return 0.0000000000001;
        case -14:
            return 0.00000000000001;
        case -15:
            return 0.000000000000001;
        case -16:
            return 0.0000000000000001;
        case -17:
            return 0.00000000000000001;
        case -18:
            return 0.000000000000000001;
        case -19:
            return 0.0000000000000000001;
        case -20:
            return 0.00000000000000000001;
        case -21:
            return 0.000000000000000000001;
        case -22:
            return 0.0000000000000000000001;
        case -23:
            return 0.00000000000000000000001;
        case -24:
            return 0.000000000000000000000001;
        case -25:
            return 0.0000000000000000000000001;
        case -26:
            return 0.00000000000000000000000001;
        case -27:
            return 0.000000000000000000000000001;
        case -28:
            return 0.0000000000000000000000000001;
        case -29:
            return 0.00000000000000000000000000001;
        case -30:
            return 0.000000000000000000000000000001;
        default:
            throw new Error("Overflow");
        }
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
        return new Decimal((long) (value * Math.pow(10, scale)), scale);
    }

    /**
     * Construct {@link Decimal} by the specified value.
     * 
     * @param value Your value.
     * @return Immutable {@link Decimal}.
     */
    public static Decimal of(double value) {
        int scale = computeScale(value);
        return new Decimal((long) (value * Math.pow(10, scale)), scale);
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
        return Math.max(0, BigDecimal.valueOf(value).scale());
    }
}
