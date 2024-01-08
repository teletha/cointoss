/*
 * Copyright (C) 2024 The COINTOSS Development Team
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          https://opensource.org/licenses/MIT
 */
package cointoss.util.arithmetic;

import cointoss.Direction;
import kiss.Variable;

class ArithmeticPlusTest extends ArithmeticTestSupport {

    @ArithmeticTest
    void primitiveInt(int value) {
        assert equality(Num.ZERO.plus(value), big(value));
    }

    @ArithmeticTest
    void primitiveIntDirection(int value, Direction side) {
        if (side.isBuy()) {
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
    void primitiveLongDirection(long value, Direction side) {
        if (side.isBuy()) {
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
    void primitiveDoubleDirection(double value, Direction side) {
        if (side.isBuy()) {
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
    void numeralStringDirection(String value, Direction side) {
        if (side.isBuy()) {
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
    void numberDirection(Num value, Direction side) {
        if (side.isBuy()) {
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
    void numberVariableWithDirection(Variable<Num> value, Direction side) {
        if (side.isBuy()) {
            assert equality(Num.ZERO.plus(side, value), big(value));
        } else {
            assert equality(Num.ZERO.plus(side, value), big(value).negate());
        }
    }
}