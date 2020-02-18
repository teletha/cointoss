/*
 * Copyright (C) 2018 Nameless Production Committee
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          http://opensource.org/licenses/mit-license.php
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
    double[] size() default {2, 0.2};

    /**
     * Configure {@link PricePart}.
     * 
     * @return
     */
    double[] price() default {10, 20, 20, 10, 0.1, 0.2, 0.2, 0.1};

    /**
     * Configure {@link EntryExitGapPart}.
     * 
     * @return
     */
    int[] gap() default {0};
}
