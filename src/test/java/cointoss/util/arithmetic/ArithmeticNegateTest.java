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

class ArithmeticNegateTest extends ArithmeticTestSupport {

    @ArithmeticTest
    void negate(int one) {
        assert equality(Num.of(one).negate(), big(one).negate());
    }

    @ArithmeticTest
    void negate(long one) {
        assert equality(Num.of(one).negate(), big(one).negate());
    }

    @ArithmeticTest
    void negate(double one) {
        assert equalityVaguely(Num.of(one).negate(), big(one).negate());
    }

    @ArithmeticTest
    void negate(String one) {
        assert equality(Num.of(one).negate(), big(one).negate());
    }
}