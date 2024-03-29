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

import hypatia.Num;
import kiss.Variable;

class ArithmeticIsNotTest extends ArithmeticTestSupport {

    @ArithmeticTest
    void primitiveInt(int one) {
        assert Num.ZERO.isNot(one) == (0 != BigDecimal.ZERO.compareTo(big(one)));
    }

    @ArithmeticTest
    void primitiveLong(long one) {
        assert Num.ZERO.isNot(one) == (0 != BigDecimal.ZERO.compareTo(big(one)));
    }

    @ArithmeticTest
    void primitiveDouble(double one) {
        assert Num.ZERO.isNot(one) == (0 != BigDecimal.ZERO.compareTo(big(one)));
    }

    @ArithmeticTest
    void numeralString(String one) {
        assert Num.ZERO.isNot(one) == (0 != BigDecimal.ZERO.compareTo(big(one)));
    }

    @ArithmeticTest
    void number(Num one) {
        assert Num.ZERO.isNot(one) == (0 != BigDecimal.ZERO.compareTo(big(one)));
    }

    @ArithmeticTest
    void numberVariable(Variable<Num> one) {
        assert Num.ZERO.isNot(one) == (0 != BigDecimal.ZERO.compareTo(big(one)));
    }
}