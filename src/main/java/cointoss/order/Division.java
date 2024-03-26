/*
 * Copyright (C) 2024 Nameless Production Committee
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          http://opensource.org/licenses/mit-license.php
 */
package cointoss.order;

import java.util.Arrays;

import cointoss.util.arithmetic.Num;

public enum Division {
    Linear1, Linear2, Linear4, Linear5, Linear8, Linear10, Linear16, Linear20, Linear25;

    /** The division size. */
    public final int size;

    /** The weight ratio. */
    public final Num[] weights;

    /**
     * For linear type.
     */
    private Division() {
        size = Integer.parseInt(name().substring(6));
        weights = new Num[size];
        Arrays.fill(weights, Num.ONE.divide(size));
    }
}
