/*
 * Copyright (C) 2019 Nameless Production Committee
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          http://opensource.org/licenses/mit-license.php
 */
package cointoss.util;

public class Primitives {

    /**
     * Round to the specified decimal place.
     * 
     * @param value
     * @param scale
     * @return
     */
    public static double roundDecimal(double value, int scale) {
        double s = Math.pow(10, scale);
        return Math.round(value * s) / s;
    }
}