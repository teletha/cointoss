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
    void calculate(int value) {
        assert equality(Num.TEN.calculate(Num.of(value), (ten, v) -> ten + v), big(10).add(big(value)));
    }
}
