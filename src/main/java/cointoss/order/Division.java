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
    Linear1, Linear2, Linear4, Linear5, Linear8, Linear10, Linear16, Linear20, Linear25, Curve2(0.4, 0.6), Curve4(0.1, 0.2, 0.3,
            0.4), Curve5(0.1, 0.1, 0.2, 0.3, 0.3), Curve8(0.05, 0.05, 0.1, 0.1, 0.15, 0.15, 0.2,
                    0.2), Curve10(0.02, 0.04, 0.06, 0.08, 0.1, 0.1, 0.12, 0.14, 0.16, 0.18);

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

    private Division(double... set) {
        size = Integer.parseInt(name().substring(5));
        weights = new Num[size];
        for (int i = 0; i < set.length; i++) {
            weights[i] = Num.of(set[i]);
        }
    }
}
