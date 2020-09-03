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

public class Decimal {

    private final long v;

    private final int scale;

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
        return new Decimal(v / value.v, scale - value.scale);
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

    static int computeScale(double value) {
        return 1;
    }
}
