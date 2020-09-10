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

class ArithmeticAbsTest extends ArithmeticTestSupport {

    @ArithmeticTest
    void abs(int one) {
        assert equality(Num.of(one).abs(), big(one).abs());
    }

    @ArithmeticTest
    void abs(long one) {
        assert equality(Num.of(one).abs(), big(one).abs());
    }

    @ArithmeticTest
    void abs(double one) {
        assert equalityVaguely(Num.of(one).abs(), big(one).abs());
    }

    @ArithmeticTest
    void abs(String one) {
        assert equalityVaguely(Num.of(one).abs(), big(one).abs());
    }
}
