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

import java.math.BigDecimal;

import kiss.Variable;

class ArithmeticIsLessThanTest extends ArithmeticTestSupport {

    @ArithmeticTest
    void primitiveInt(int one) {
        assert Num.ZERO.isLessThan(one) == BigDecimal.ZERO.compareTo(big(one)) < 0;
    }

    @ArithmeticTest
    void primitiveInt(int one, Orientational side) {
        if (side.isPositive()) {
            assert Num.ZERO.isLessThan(side, one) == BigDecimal.ZERO.compareTo(big(one)) < 0;
        } else {
            assert Num.ZERO.isLessThan(side, one) == 0 < BigDecimal.ZERO.compareTo(big(one));
        }
    }

    @ArithmeticTest
    void primitiveLong(long one) {
        assert Num.ZERO.isLessThan(one) == BigDecimal.ZERO.compareTo(big(one)) < 0;
    }

    @ArithmeticTest
    void primitiveLong(long one, Orientational side) {
        if (side.isPositive()) {
            assert Num.ZERO.isLessThan(side, one) == BigDecimal.ZERO.compareTo(big(one)) < 0;
        } else {
            assert Num.ZERO.isLessThan(side, one) == 0 < BigDecimal.ZERO.compareTo(big(one));
        }
    }

    @ArithmeticTest
    void primitiveDouble(double one) {
        assert Num.ZERO.isLessThan(one) == BigDecimal.ZERO.compareTo(big(one)) < 0;
    }

    @ArithmeticTest
    void primitiveDouble(double one, Orientational side) {
        if (side.isPositive()) {
            assert Num.ZERO.isLessThan(side, one) == BigDecimal.ZERO.compareTo(big(one)) < 0;
        } else {
            assert Num.ZERO.isLessThan(side, one) == 0 < BigDecimal.ZERO.compareTo(big(one));
        }
    }

    @ArithmeticTest
    void numeralString(String one) {
        assert Num.ZERO.isLessThan(one) == BigDecimal.ZERO.compareTo(big(one)) < 0;
    }

    @ArithmeticTest
    void numeralString(String one, Orientational side) {
        if (side.isPositive()) {
            assert Num.ZERO.isLessThan(side, one) == BigDecimal.ZERO.compareTo(big(one)) < 0;
        } else {
            assert Num.ZERO.isLessThan(side, one) == 0 < BigDecimal.ZERO.compareTo(big(one));
        }
    }

    @ArithmeticTest
    void number(Num one) {
        assert Num.ZERO.isLessThan(one) == BigDecimal.ZERO.compareTo(big(one)) < 0;
    }

    @ArithmeticTest
    void number(Num one, Orientational side) {
        if (side.isPositive()) {
            assert Num.ZERO.isLessThan(side, one) == BigDecimal.ZERO.compareTo(big(one)) < 0;
        } else {
            assert Num.ZERO.isLessThan(side, one) == 0 < BigDecimal.ZERO.compareTo(big(one));
        }
    }

    @ArithmeticTest
    void numberVariable(Variable<Num> one) {
        assert Num.ZERO.isLessThan(one) == BigDecimal.ZERO.compareTo(big(one)) < 0;
    }

    @ArithmeticTest
    void numberVariable(Variable<Num> one, Orientational side) {
        if (side.isPositive()) {
            assert Num.ZERO.isLessThan(side, one) == BigDecimal.ZERO.compareTo(big(one)) < 0;
        } else {
            assert Num.ZERO.isLessThan(side, one) == 0 < BigDecimal.ZERO.compareTo(big(one));
        }
    }
}