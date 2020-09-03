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

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.NumberFormat;
import java.util.Objects;

import com.google.common.math.DoubleMath;

import cointoss.util.Primitives;

public abstract class Decimal<Self extends Decimal<Self>> extends Arithmetic<Self> {

    private static final double[] positives = {1, 10, 100, 1000, 10000, 100000, 1000000, 10000000, 100000000, 1000000000, 10000000000d,
            100000000000d, 1000000000000d, 10000000000000d, 100000000000000d, 1000000000000000d, 10000000000000000d, 100000000000000000d,
            1000000000000000000d, 10000000000000000000d, 100000000000000000000d, 1000000000000000000000d, 10000000000000000000000d,
            100000000000000000000000d, 1000000000000000000000000d, 10000000000000000000000000d, 100000000000000000000000000d,
            1000000000000000000000000000d, 10000000000000000000000000000d, 100000000000000000000000000000d};

    private static final double[] negatives = {1, 0.1, 0.01, 0.001, 0.0001, 0.00001, 0.000001, 0.0000001, 0.00000001, 0.000000001,
            0.0000000001, 0.00000000001, 0.000000000001, 0.0000000000001, 0.00000000000001, 0.000000000000001, 0.0000000000000001,
            0.00000000000000001, 0.000000000000000001, 0.0000000000000000001, 0.00000000000000000001, 0.000000000000000000001,
            0.0000000000000000000001, 0.00000000000000000000001, 0.000000000000000000000001, 0.0000000000000000000000001,
            0.00000000000000000000000001, 0.000000000000000000000000001, 0.0000000000000000000000000001, 0.00000000000000000000000000001};

    private static double pow10(int scale) {
        if (0 <= scale) {
            return positives[scale];
        } else {
            return negatives[-scale];
        }
    }

    long v;

    int scale;

    /**
     * @param value
     * @param scale
     */
    protected Decimal(long value, int scale) {
        this.v = value;
        this.scale = scale;
    }

    /**
     * @param value
     * @param scale
     */
    protected Decimal(BigDecimal value) {
        int scale = Math.max(0, value.scale());
        this.v = value.scaleByPowerOfTen(scale).longValue();
        this.scale = scale;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected Self create(int value) {
        return create(value, 0);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected Self create(long value) {
        return create(value, 0);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected Self create(double value) {
        int scale = computeScale(value);
        return create((long) (value * pow10(scale)), scale);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected Self create(String value) {
        BigDecimal big = new BigDecimal(value);
        int scale = Math.max(0, big.scale());
        return create(big.scaleByPowerOfTen(scale).longValue(), scale);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected Self create(BigDecimal value) {
        int scale = Math.max(0, value.scale());
        return create(value.scaleByPowerOfTen(scale).longValue(), scale);
    }

    protected abstract Self create(long value, int scale);

    @Override
    public Self plus(Self value) {
        if (scale == value.scale) {
            return create(v + value.v, scale);
        } else if (scale < value.scale) {
            return create((long) (v * pow10(value.scale - scale) + value.v), value.scale);
        } else {
            return create(v + (long) (value.v * pow10(scale - value.scale)), scale);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Self minus(Self value) {
        if (scale == value.scale) {
            return create(v - value.v, scale);
        } else if (scale < value.scale) {
            return create((long) (v * pow10(value.scale - scale) - value.v), value.scale);
        } else {
            return create(v - (long) (value.v * pow10(scale - value.scale)), scale);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Self multiply(Self value) {
        return create(v * value.v, scale + value.scale);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Self divide(Self value) {
        Self result = create((double) v / value.v);
        result.scale = scale - value.scale + result.scale;
        return result;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Self remainder(Self value) {
        if (scale == value.scale) {
            return create(v % value.v, scale);
        } else if (scale < value.scale) {
            return create((long) (v * pow10(value.scale - scale)) % value.v, value.scale);
        } else {
            return create(v % (long) (value.v * pow10(scale - value.scale)), scale);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int compareTo(Self o) {
        if (scale == o.scale) {
            return Long.compare(v, o.v);
        } else if (scale < o.scale) {
            return Long.compare((long) (v * pow10(o.scale - scale)), o.v);
        } else {
            return Long.compare(v, (long) (o.v * pow10(scale - o.scale)));
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Self decuple(int n) {
        return create(v, scale - n);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Self pow(int n) {
        Self result = create(Math.pow(v, n));
        result.scale += scale * n;
        return result;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Self pow(double n) {
        Self result = create(Math.pow(v, n));
        result.scale += scale * n;
        return result;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Self sqrt() {
        Self result = create(Math.sqrt(v));
        result.scale += scale / 2;
        return result;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Self abs() {
        return create(Math.abs(v), scale);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Self negate() {
        return create(-v, scale);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int scale() {
        return scale;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Self scale(int size, RoundingMode mode) {
        if (scale == size) {
            return (Self) this;
        } else if (scale < size) {
            return create((long) (v * pow10(size - scale)), size);
        } else {
            return create(DoubleMath.roundToLong(v * pow10(size - scale), mode), size);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String format(NumberFormat format) {
        return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int intValue() {
        return (int) (v * pow10(-scale));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public long longValue() {
        return (long) (v * pow10(-scale));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public float floatValue() {
        return (float) Primitives.roundDecimal(v * pow10(-scale), scale);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public double doubleValue() {
        return Primitives.roundDecimal(v * pow10(-scale), scale);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return Primitives.roundString(v * pow10(-scale), scale);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode() {
        return Objects.hash(v, scale);
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

        Decimal other = (Decimal) obj;
        return this.scale == other.scale && this.v == other.v;
    }

    protected static int computeScale(double value) {
        for (int i = 0; i < 30; i++) {
            double fixer = pow10(i);
            double fixed = ((long) (value * fixer)) / fixer;
            if (DoubleMath.fuzzyEquals(value, fixed, 0.000000000001)) {
                return i;
            }
        }
        throw new Error("Overflow Value : " + value);
    }
}
