/*
 * Copyright (C) 2025 The COINTOSS Development Team
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          https://opensource.org/licenses/MIT
 */
package cointoss;

import hypatia.Num;

public class CurrencySetting {

    /** The currency. */
    public final Currency currency;

    /** The minimum size. */
    public final Num minimumSize;

    /** The decimal scale. */
    public final int scale;

    CurrencySetting(Currency currency, Num minimumSize) {
        this(currency, minimumSize, countDecimalPlaces(minimumSize.doubleValue()));
    }

    private static int countDecimalPlaces(double value) {
        if (value == (long) value) {
            return 0;
        }

        int count = 0;
        while (value != Math.round(value)) {
            value *= 10;
            count++;
        }
        return count;
    }

    CurrencySetting(Currency currency, Num minimumSize, int scale) {
        this.currency = currency;
        this.minimumSize = minimumSize;
        this.scale = Math.max(0, scale);
    }

    /**
     * Configure the scale of this {@link Currency}.
     * 
     * @param scale
     * @return
     */
    public CurrencySetting scale(int scale) {
        return new CurrencySetting(currency, minimumSize, scale);
    }
}