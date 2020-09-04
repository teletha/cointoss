/*
 * Copyright (C) 2019 Nameless Production Committee
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          http://opensource.org/licenses/mit-license.php
 */
package cointoss.util.arithmeric;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.NumberFormat;
import java.util.Objects;

public abstract class JDK<Self extends JDK> extends Arithmetic<Self> {

    /** The actual value. */
    protected final BigDecimal delegate;

    /**
     * Constructor for ZERO.
     */
    protected JDK() {
        this(BigDecimal.ZERO);
    }

    /**
     * Constructor.
     * 
     * @param value primitive value
     */
    protected JDK(BigDecimal value) {
        this.delegate = value;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Self create(int value) {
        return create(new BigDecimal(value, CONTEXT));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Self create(long value) {
        return create(new BigDecimal(value, CONTEXT));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Self create(double value) {
        return create(new BigDecimal(value, CONTEXT));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Self create(String value) {
        return create(new BigDecimal(value, CONTEXT));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int compareTo(Self o) {
        return delegate.compareTo(o.delegate);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Self plus(Self augend) {
        return create(delegate.add(augend.delegate, CONTEXT));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Self minus(Self subtrahend) {
        return create(delegate.subtract(subtrahend.delegate, CONTEXT));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Self multiply(Self multiplicand) {
        return create(delegate.multiply(multiplicand.delegate, CONTEXT));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Self divide(Self divisor) {
        return create(delegate.divide(divisor.delegate, CONTEXT));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Self remainder(Self divisor) {
        return create(delegate.remainder(divisor.delegate, CONTEXT));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Self decuple(int n) {
        return create(delegate.scaleByPowerOfTen(n));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Self pow(int n) {
        return create(delegate.pow(n, CONTEXT));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Self pow(double n) {
        return create(Math.pow(doubleValue(), n));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Self sqrt() {
        return create(delegate.sqrt(CONTEXT));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Self abs() {
        return create(delegate.abs());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Self negate() {
        return create(delegate.negate());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int scale() {
        return delegate.stripTrailingZeros().scale();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Self scale(int size, RoundingMode mode) {
        return create(delegate.setScale(size, mode));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String format(NumberFormat format) {
        return format.format(delegate);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int intValue() {
        return delegate.intValue();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public long longValue() {
        return delegate.longValue();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public float floatValue() {
        return delegate.floatValue();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public double doubleValue() {
        return delegate.doubleValue();
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
        if (!(obj instanceof JDK)) {
            return false;
        }

        JDK other = (JDK) obj;
        if (this.delegate != other.delegate && (this.delegate == null || (this.delegate.compareTo(other.delegate) != 0))) {
            return false;
        }
        return true;
    }
}
