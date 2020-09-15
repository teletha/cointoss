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

class ArithmeticCalculateTest extends ArithmeticTestSupport {

    @ArithmeticTest
    void calculate1(int value) {
        assert equality(Num.TEN.calculate(Num.of(value), (ten, v) -> ten + v), big(10).add(big(value)));
    }

    @ArithmeticTest(doubles = {0, 0.1, -0.1})
    void calculateMultiple(double value) {
        assert equalityVaguely(Num.TEN.calculate(Num.of(value), (ten, v) -> ten * v), big(10).multiply(big(value)));
    }

    @ArithmeticTest
    void calculate2(int value) {
        assert equality(Num.TEN.calculate(Num.of(value), Num.of(value), (ten, v1, v2) -> ten + v1 + v2), big(10).add(big(value))
                .add(big(value)));
    }

    @ArithmeticTest(ints = {0, 1, 10, -1, -10})
    void calculate3(int value) {
        assert equality(Num.TEN.calculate(Num.of(value), Num.of(value), Num.of(value), (ten, v1, v2, v3) -> (ten + v1 + v2) * v3), big(10)
                .add(big(value))
                .add(big(value))
                .multiply(big(value)));
    }

    @ArithmeticTest(doubles = {0, 1, 10, 0.01, -0.0045})
    void calculate4(double value) {
        assert equalityVaguely(Num.TEN.calculate(8, Num.of(value), Num.of(value), Num.of(value), Num
                .of(value), (ten, v1, v2, v3, v4) -> ((ten + v1 + v2) * v3) - v4), big(10).add(big(value))
                        .add(big(value))
                        .multiply(big(value))
                        .subtract(big(value)));
    }
}
