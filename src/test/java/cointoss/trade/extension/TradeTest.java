/*
 * Copyright (C) 2024 The COINTOSS Development Team
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          https://opensource.org/licenses/MIT
 */
package cointoss.trade.extension;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ArgumentsSource;

import cointoss.Direction;

@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@ParameterizedTest
@ArgumentsSource(TradePartProvider.class)
public @interface TradeTest {

    ScenePart[] scene() default {};

    /**
     * Configure {@link SidePart}.
     * 
     * @return
     */
    Direction[] side() default {Direction.BUY, Direction.SELL};

    /**
     * Configure {@link SizePart}.
     * 
     * @return
     */
    double[] size() default {5, 0.0004};

    /**
     * Configure {@link PricePart}.
     * 
     * @return
     */
    double[] price() default {0.01, 0.02, 2, 1};

    /**
     * Configure {@link HoldTimePart}.
     * 
     * @return
     */
    int[] gap() default {0};
}