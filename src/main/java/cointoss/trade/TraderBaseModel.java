/*
 * Copyright (C) 2020 cointoss Development Team
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          https://opensource.org/licenses/MIT
 */
package cointoss.trade;

import cointoss.util.ObservableNumProperty;
import cointoss.util.arithmetic.Num;
import icy.manipulator.Icy;

@Icy
abstract class TraderBaseModel {

    /**
     * Return the current hold size of target currency. Positive number means long position,
     * negative number means short position. Zero means no position.
     * 
     * @return A current hold size.
     */
    @Icy.Property(setterModifier = "final", custom = ObservableNumProperty.class)
    public Num holdSize() {
        return Num.ZERO;
    }

    /**
     * Return the maximum hold size of target currency. (historical data)
     * 
     * @return A maximum hold size.
     */
    @Icy.Property(setterModifier = "final", custom = ObservableNumProperty.class)
    public Num holdMaxSize() {
        return Num.ZERO;
    }

    /**
     * Calculate the current profit and loss.
     * 
     * @return A current profit and loss.
     */
    @Icy.Property(setterModifier = "final", custom = ObservableNumProperty.class)
    public Num profit() {
        return Num.ZERO;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode() {
        return super.hashCode();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean equals(Object obj) {
        return super.equals(obj);
    }
}