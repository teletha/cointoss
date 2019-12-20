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

import java.math.RoundingMode;
import java.text.DecimalFormat;

import com.google.common.math.DoubleMath;

public class Primitives {

    /** Fix decimal point(2). */
    public static final DecimalFormat DecimalScale2 = new DecimalFormat("#.#");

    /** Fix decimal point(4). */
    public static final DecimalFormat DecimalScale4 = new DecimalFormat("#.#");

    /** Fix decimal point(6). */
    public static final DecimalFormat DecimalScale6 = new DecimalFormat("#.#");

    static {
        DecimalScale2.setMinimumFractionDigits(2);
        DecimalScale2.setMaximumFractionDigits(2);
        DecimalScale4.setMinimumFractionDigits(4);
        DecimalScale4.setMaximumFractionDigits(4);
        DecimalScale6.setMinimumFractionDigits(6);
        DecimalScale6.setMaximumFractionDigits(6);
    }

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

    /**
     * Round to the specified decimal place.
     * 
     * @param value
     * @param scale
     * @return
     */
    public static double roundDecimal(double value, int scale, RoundingMode mode) {
        double s = Math.pow(10, scale);
        return DoubleMath.roundToInt(value * s, mode) / s;
    }

    /**
     * @param min
     * @param value
     * @param max
     */
    public static double between(double min, double value, double max) {
        if (value < min) {
            value = min;
        }

        if (max < value) {
            value = max;
        }
        return value;
    }

    /**
     * @param min
     * @param value
     * @param max
     */
    public static boolean within(double min, double value, double max) {
        if (value < min) {
            return false;
        }

        if (max < value) {
            return false;
        }
        return true;
    }
}
