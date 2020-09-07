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

import java.math.BigDecimal;

import com.google.common.math.DoubleMath;

import cointoss.Direction;
import kiss.Variable;

class ArithmeticPlusTest extends ArithmeticTestSupport {

    @ArithmeticTest
    void primitiveInt(int value) {
        assert Num.ZERO.plus(value).intValue() == value;
    }

    @ArithmeticTest
    void primitiveIntDirection(int value, Direction side) {
        if (side.isBuy()) {
            assert Num.ZERO.plus(side, value).equals(Num.of(value));
        } else {
            assert Num.ZERO.plus(side, value).equals(Num.of(value * -1L));
        }
    }

    @ArithmeticTest
    void primitiveLong(long value) {
        assert Num.ZERO.plus(value).longValue() == value;
    }

    @ArithmeticTest
    void primitiveLongDirection(long value, Direction side) {
        if (side.isBuy()) {
            assert Num.ZERO.plus(side, value).equals(Num.of(value));
        } else {
            assert Num.ZERO.plus(side, value).equals(Num.of(value * -1L));
        }
    }

    @ArithmeticTest
    void primitiveDouble(double value) {
        assert DoubleMath.fuzzyEquals(Num.ZERO.plus(value).doubleValue(), value, Fuzzy);
    }

    @ArithmeticTest
    void primitiveDoubleDirection(double value, Direction side) {
        if (side.isBuy()) {
            assert DoubleMath.fuzzyEquals(Num.ZERO.plus(side, value).doubleValue(), value, Fuzzy);
        } else {
            assert DoubleMath.fuzzyEquals(Num.ZERO.plus(side, value).doubleValue(), value * -1.0D, Fuzzy);
        }
    }

    @ArithmeticTest
    void numeralString(String value) {
        assert Num.ZERO.plus(value).toString().equals(new BigDecimal(value, Num.CONTEXT).toPlainString());
    }

    @ArithmeticTest
    void numeralStringDirection(String value, Direction side) {
        if (side.isBuy()) {
            assert Num.ZERO.plus(side, value).toString().equals(new BigDecimal(value, Num.CONTEXT).toPlainString());
        } else {
            assert Num.ZERO.plus(side, value).toString().equals(new BigDecimal(value, Num.CONTEXT).negate().toPlainString());
        }
    }

    @ArithmeticTest
    void number(Num value) {
        assert Num.ZERO.plus(value).equals(value);
    }

    @ArithmeticTest
    void numberDirection(Num value, Direction side) {
        if (side.isBuy()) {
            assert Num.ZERO.plus(side, value).equals(value);
        } else {
            assert Num.ZERO.plus(side, value).equals(value.negate());
        }
    }

    @ArithmeticTest
    void numberVariable(Variable<Num> value) {
        assert Num.ZERO.plus(value).longValue() == value.v.longValue();
    }

    @ArithmeticTest
    void numberVariableWithDirection(Variable<Num> value, Direction side) {
        if (side.isBuy()) {
            assert Num.ZERO.plus(side, value).longValue() == value.v.longValue();
        } else {
            assert Num.ZERO.plus(side, value).longValue() == -value.v.longValue();
        }
    }
}
