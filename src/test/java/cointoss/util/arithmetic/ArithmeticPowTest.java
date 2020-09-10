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

class ArithmeticPowTest extends ArithmeticTestSupport {

    @ArithmeticTest
    void pow(int one, int other) {
        assert equality(Num.of(one).pow(other), big(one).pow(other));
    }

    @ArithmeticTest
    void pow(long one, long other) {
        assert equalityVaguely(Num.of(one).pow(other), Math.pow(one, other));
    }
}
