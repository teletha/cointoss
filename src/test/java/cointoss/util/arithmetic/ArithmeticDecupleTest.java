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

class ArithmeticDecupleTest extends ArithmeticTestSupport {

    @ArithmeticTest
    void decuple(int one, int other) {
        assert equalityVaguely(Num.of(one).decuple(1), big(one).scaleByPowerOfTen(1));
        assert equalityVaguely(Num.of(one).decuple(10), big(one).scaleByPowerOfTen(10));
        assert equalityVaguely(Num.of(one).decuple(-1), big(one).scaleByPowerOfTen(-1));
        assert equalityVaguely(Num.of(one).decuple(-10), big(one).scaleByPowerOfTen(-10));
    }

    @ArithmeticTest
    void decuple(long one) {
        assert equalityVaguely(Num.of(one).decuple(1), big(one).scaleByPowerOfTen(1));
        assert equalityVaguely(Num.of(one).decuple(10), big(one).scaleByPowerOfTen(10));
        assert equalityVaguely(Num.of(one).decuple(-1), big(one).scaleByPowerOfTen(-1));
        assert equalityVaguely(Num.of(one).decuple(-10), big(one).scaleByPowerOfTen(-10));
    }

    @ArithmeticTest
    void decuple(double one) {
        assert equalityVaguely(Num.of(one).decuple(1), big(one).scaleByPowerOfTen(1));
        assert equalityVaguely(Num.of(one).decuple(10), big(one).scaleByPowerOfTen(10));
        assert equalityVaguely(Num.of(one).decuple(-1), big(one).scaleByPowerOfTen(-1));
        assert equalityVaguely(Num.of(one).decuple(-10), big(one).scaleByPowerOfTen(-10));
    }

    @ArithmeticTest
    void decuple(String one) {
        assert equalityVaguely(Num.of(one).decuple(1), big(one).scaleByPowerOfTen(1));
        assert equalityVaguely(Num.of(one).decuple(10), big(one).scaleByPowerOfTen(10));
        assert equalityVaguely(Num.of(one).decuple(-1), big(one).scaleByPowerOfTen(-1));
        assert equalityVaguely(Num.of(one).decuple(-10), big(one).scaleByPowerOfTen(-10));
    }
}
