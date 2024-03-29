/*
 * Copyright (C) 2024 The COINTOSS Development Team
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          https://opensource.org/licenses/MIT
 */
package hypatia;

import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;

import com.google.common.math.DoubleMath;

public class Primitives {

    /** Fix decimal point(2). */
    public static final DecimalFormat DecimalScale2 = new DecimalFormat("#.#");

    /** Fix decimal point(4). */
    public static final DecimalFormat DecimalScale4 = new DecimalFormat("#.#");

    /** Fix decimal point(6). */
    public static final DecimalFormat DecimalScale6 = new DecimalFormat("#.#");

    /** Fix decimal point(340). */
    private static final DecimalFormat DecimalScale340 = new DecimalFormat("0", DecimalFormatSymbols.getInstance(Locale.ENGLISH));

    static {
        DecimalScale2.setMinimumFractionDigits(2);
        DecimalScale2.setMaximumFractionDigits(2);
        DecimalScale4.setMinimumFractionDigits(4);
        DecimalScale4.setMaximumFractionDigits(4);
        DecimalScale6.setMinimumFractionDigits(6);
        DecimalScale6.setMaximumFractionDigits(6);

        // 340 = DecimalFormat.DOUBLE_FRACTION_DIGITS
        DecimalScale340.setMaximumFractionDigits(340);
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
        return StrictMath.rint(value * s) / s;
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
        return DoubleMath.roundToLong(value * s, mode) / s;
    }

    /**
     * Round to the specified decimal place.
     * 
     * @param value A target value to round.
     * @param scale A required scale.
     * @return A rounded value as string expression.
     */
    public static String roundString(double value, int scale) {
        return DecimalScale340.format(roundDecimal(value, scale));
    }

    /**
     * Round to the specified decimal place.
     * 
     * @param value A target value to round.
     * @param scale A required scale.
     * @return A rounded value as string expression.
     */
    public static String roundString(double value, int scale, RoundingMode mode) {
        return DecimalScale340.format(roundDecimal(value, scale, mode));
    }

    /**
     * Check equality for primitive doubles.
     * 
     * @param value1
     * @param value2
     * @param delta
     * @return
     */
    public static boolean equals(double value1, double value2, double delta) {
        assertValidDelta(delta);
        return Double.doubleToLongBits(value1) == Double.doubleToLongBits(value2) || Math.abs(value1 - value2) <= delta;
    }

    private static void assertValidDelta(double delta) {
        if (Double.isNaN(delta) || delta < 0.0) {
            throw new IllegalArgumentException("Invalid delta " + delta);
        }
    }

    /**
     * Rounds the specified value to the range of valid values.
     * 
     * @param min A minimum value.
     * @param value A target value.
     * @param max A maximum value.
     */
    public static int round(int min, int value, int max) {
        if (value < min) {
            value = min;
        }

        if (max < value) {
            value = max;
        }
        return value;
    }

    /**
     * Rounds the specified value to the range of valid values.
     * 
     * @param min A minimum value.
     * @param value A target value.
     * @param max A maximum value.
     */
    public static long round(long min, long value, long max) {
        if (value < min) {
            value = min;
        }

        if (max < value) {
            value = max;
        }
        return value;
    }

    /**
     * Rounds the specified value to the range of valid values.
     * 
     * @param min A minimum value.
     * @param value A target value.
     * @param max A maximum value.
     */
    public static double round(double min, double value, double max) {
        if (value < min) {
            value = min;
        }

        if (max < value) {
            value = max;
        }
        return value;
    }

    /**
     * Checks if the specified value is in the range of valid values.
     * 
     * @param min A minimum value.
     * @param value A target value.
     * @param max A maximum value.
     */
    public static boolean within(int min, int value, int max) {
        if (value < min) {
            return false;
        }

        if (max < value) {
            return false;
        }
        return true;
    }

    /**
     * Checks if the specified value is in the range of valid values.
     * 
     * @param min A minimum value.
     * @param value A target value.
     * @param max A maximum value.
     */
    public static boolean within(long min, long value, long max) {
        if (value < min) {
            return false;
        }

        if (max < value) {
            return false;
        }
        return true;
    }

    /**
     * Checks if the specified value is in the range of valid values.
     * 
     * @param min A minimum value.
     * @param value A target value.
     * @param max A maximum value.
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

    /**
     * Checks if the specified value is in the range of valid values.
     * 
     * @param min A minimum value.
     * @param value A target value.
     * @param max A maximum value.
     */
    public static double between(double min, double value, double max) {
        if (value < min || value == Double.NEGATIVE_INFINITY) {
            value = min;
        }

        if (max < value || value == Double.POSITIVE_INFINITY) {
            value = max;
        }
        return value;
    }

    /**
     * Compute the maximum value.
     * 
     * @param values
     * @return
     */
    public static int max(int... values) {
        int max = Integer.MIN_VALUE;
        for (int value : values) {
            if (max < value) {
                max = value;
            }
        }
        return max;
    }

    /**
     * Compute the maximum value.
     * 
     * @param values
     * @return
     */
    public static long max(long... values) {
        long max = Long.MIN_VALUE;
        for (long value : values) {
            if (max < value) {
                max = value;
            }
        }
        return max;
    }

    /**
     * Compute the minimum value.
     * 
     * @param values
     * @return
     */
    public static int min(int... values) {
        int min = Integer.MAX_VALUE;
        for (int value : values) {
            if (value < min) {
                min = value;
            }
        }
        return min;
    }

    /**
     * Compute the minimum value.
     * 
     * @param values
     * @return
     */
    public static long min(long... values) {
        long min = Long.MAX_VALUE;
        for (long value : values) {
            if (value < min) {
                min = value;
            }
        }
        return min;
    }

    /**
     * Claculate ratio.
     * 
     * @param numerator
     * @param denominator
     * @return
     */
    public static double ratio(double numerator, double denominator) {
        return denominator == 0 ? 0 : roundDecimal(numerator / denominator, 3);
    }

    /**
     * Claculate ratio.
     * 
     * @param numerator
     * @param denominator
     * @return
     */
    public static String percent(double numerator, double denominator) {
        double ratio = denominator == 0 ? 0 : roundDecimal(numerator / denominator * 100, 1);
        long integer = (long) ratio;

        if (ratio == integer) {
            return String.valueOf(integer).concat("%");
        } else {
            return String.valueOf(ratio).concat("%");
        }
    }

    /**
     * Ensure that the specified value is long like value.
     * 
     * @param value
     * @return
     */
    public static long ensureLong(Object value) {
        if (value instanceof Long == false) {
            throw new IllegalArgumentException("Value type must be Long.");
        }
        return ((Long) value).longValue();
    }

    /**
     * Ensure that the specified value is long like value.
     * 
     * @param value
     * @return
     */
    public static long ensureLong(Long value) {
        if (value == null) {
            throw new IllegalArgumentException("Value must be Long, this is null.");
        }
        return value.longValue();
    }
}