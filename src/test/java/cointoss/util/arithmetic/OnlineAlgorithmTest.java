/*
 * Copyright (C) 2020 Nameless Production Committee
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          http://opensource.org/licenses/mit-license.php
 */
package cointoss.util.arithmetic;

import java.util.List;
import java.util.stream.Collector;

import org.junit.jupiter.api.Test;

import kiss.I;

class OnlineAlgorithmTest {

    @Test
    void sum() {
        Collector<Double, ?, Double> sum = OnlineAlgorithm.sumDouble(4);

        List<Double> list = I.signal(1d, 2d, 3d, 4d, 5d, 6d, 7d, 8d, 9d, 10d).scan(sum).toList();
        System.out.println(list);
    }
}
