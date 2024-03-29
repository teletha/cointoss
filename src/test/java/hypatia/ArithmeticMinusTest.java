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

class ArithmeticMinusTest extends ArithmeticTestSupport {

    @ArithmeticTest
    void primitiveInt(int value) {
        assert equality(Num.ZERO.minus(value), big(value).negate());
    }

    @ArithmeticTest
    void primitiveIntOrientational(int value, Orientational side) {
        if (side.isPositive()) {
            assert equality(Num.ZERO.minus(side, value), big(value).negate());
        } else {
            assert equality(Num.ZERO.minus(side, value), big(value));
        }
    }

    @ArithmeticTest
    void primitiveLong(long value) {
        assert equality(Num.ZERO.minus(value), big(value).negate());
    }

    @ArithmeticTest
    void primitiveLongOrientational(long value, Orientational side) {
        if (side.isPositive()) {
            assert equality(Num.ZERO.minus(side, value), big(value).negate());
        } else {
            assert equality(Num.ZERO.minus(side, value), big(value));
        }
    }

    @ArithmeticTest
    void primitiveDouble(double value) {
        assert equalityVaguely(Num.ZERO.minus(value), big(value).negate());
    }

    @ArithmeticTest
    void primitiveDoubleOrientational(double value, Orientational side) {
        if (side.isPositive()) {
            assert equalityVaguely(Num.ZERO.minus(side, value), big(value).negate());
        } else {
            assert equalityVaguely(Num.ZERO.minus(side, value), big(value));
        }
    }

    @ArithmeticTest
    void numeralString(String value) {
        assert equality(Num.ZERO.minus(value), big(value).negate());
    }

    @ArithmeticTest
    void numeralStringOrientational(String value, Orientational side) {
        if (side.isPositive()) {
            assert equality(Num.ZERO.minus(side, value), big(value).negate());
        } else {
            assert equality(Num.ZERO.minus(side, value), big(value));
        }
    }

    @ArithmeticTest
    void number(Num value) {
        assert equality(Num.ZERO.minus(value), big(value).negate());
    }

    @ArithmeticTest
    void numberOrientational(Num value, Orientational side) {
        if (side.isPositive()) {
            assert equality(Num.ZERO.minus(side, value), big(value).negate());
        } else {
            assert equality(Num.ZERO.minus(side, value), big(value));
        }
    }

    @ArithmeticTest
    void numberVariable(Variable<Num> value) {
        assert equality(Num.ZERO.minus(value), big(value).negate());
    }

    @ArithmeticTest
    void numberVariableWithOrientational(Variable<Num> value, Orientational side) {
        if (side.isPositive()) {
            assert equality(Num.ZERO.minus(side, value), big(value).negate());
        } else {
            assert equality(Num.ZERO.minus(side, value), big(value));
        }
    }
}