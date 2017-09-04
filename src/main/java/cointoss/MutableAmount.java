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

/**
 * @version 2017/09/04 9:20:05
 */
public class MutableAmount extends Amount {

    /**
     * @param value
     */
    public MutableAmount(String value) {
        super(value);
    }

    /**
     * @param value
     */
    public MutableAmount(Amount value) {
        super(value.value);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Amount abs() {
        value = super.abs().value;
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Amount plus(Amount size) {
        value = super.plus(size).value;
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Amount minus(Amount size) {
        value = super.minus(size).value;
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Amount multiply(Amount size) {
        value = super.multiply(size).value;
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Amount divide(Amount size) {
        value = super.divide(size).value;
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Amount ratio(Directional direction, double size) {
        value = super.ratio(direction, size).value;
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Amount integral() {
        value = super.integral().value;
        return this;
    }
}
