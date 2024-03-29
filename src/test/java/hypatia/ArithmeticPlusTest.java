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

import kiss.Variable;

class ArithmeticPlusTest extends ArithmeticTestSupport {

    @ArithmeticTest
    void primitiveInt(int value) {
        assert equality(Num.ZERO.plus(value), big(value));
    }

    @ArithmeticTest
    void primitiveIntOrientational(int value, Orientational side) {
        if (side.isPositive()) {
            assert equality(Num.ZERO.plus(side, value), big(value));
        } else {
            assert equality(Num.ZERO.plus(side, value), big(value).negate());
        }
    }

    @ArithmeticTest
    void primitiveLong(long value) {
        assert equality(Num.ZERO.plus(value), big(value));
    }

    @ArithmeticTest
    void primitiveLongOrientational(long value, Orientational side) {
        if (side.isPositive()) {
            assert equality(Num.ZERO.plus(side, value), big(value));
        } else {
            assert equality(Num.ZERO.plus(side, value), big(value).negate());
        }
    }

    @ArithmeticTest
    void primitiveDouble(double value) {
        assert equalityVaguely(Num.ZERO.plus(value), big(value));
    }

    @ArithmeticTest
    void primitiveDoubleOrientational(double value, Orientational side) {
        if (side.isPositive()) {
            assert equalityVaguely(Num.ZERO.plus(side, value), big(value));
        } else {
            assert equalityVaguely(Num.ZERO.plus(side, value), big(value).negate());
        }
    }

    @ArithmeticTest
    void numeralString(String value) {
        assert equality(Num.ZERO.plus(value), big(value));
    }

    @ArithmeticTest
    void numeralStringOrientational(String value, Orientational side) {
        if (side.isPositive()) {
            assert equality(Num.ZERO.plus(side, value), big(value));
        } else {
            assert equality(Num.ZERO.plus(side, value), big(value).negate());
        }
    }

    @ArithmeticTest
    void number(Num value) {
        assert equality(Num.ZERO.plus(value), big(value));
    }

    @ArithmeticTest
    void numberOrientational(Num value, Orientational side) {
        if (side.isPositive()) {
            assert equality(Num.ZERO.plus(side, value), big(value));
        } else {
            assert equality(Num.ZERO.plus(side, value), big(value).negate());
        }
    }

    @ArithmeticTest
    void numberVariable(Variable<Num> value) {
        assert equality(Num.ZERO.plus(value), big(value));
    }

    @ArithmeticTest
    void numberVariableWithOrientational(Variable<Num> value, Orientational side) {
        if (side.isPositive()) {
            assert equality(Num.ZERO.plus(side, value), big(value));
        } else {
            assert equality(Num.ZERO.plus(side, value), big(value).negate());
        }
    }
}