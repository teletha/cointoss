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

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ArgumentsSource;

@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@ParameterizedTest
@ArgumentsSource(ArithmeticValueProvider.class)
public @interface ArithmeticTest {

    /**
     * Provide values.
     * 
     * @return
     */
    int[] ints() default {0, 1, -1, 1000, -1000, Integer.MAX_VALUE, Integer.MIN_VALUE};

    /**
     * Provide values.
     * 
     * @return
     */
    long[] longs() default {0, 1, -1, Integer.MAX_VALUE, Integer.MIN_VALUE, Long.MAX_VALUE, Long.MIN_VALUE};

    /**
     * Provide values.
     * 
     * @return
     */
    double[] doubles() default {0, 0.1, -0.1, Long.MAX_VALUE, Long.MIN_VALUE, Double.MIN_VALUE, -Double.MIN_VALUE};

    /**
     * Provide values.
     * 
     * @return
     */
    String[] strings() default {"0", "0.1", "-0.1", "9223372036854775807", "-9223372036854775808", "123456789012345678901234567890",
            "-123456789012345678901234567890", "0.123456789012345678901234567890123456789", "-0.123456789012345678901234567890123456789"};
}