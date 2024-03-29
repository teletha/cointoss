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

class ArithmeticIsSomethingTest extends ArithmeticTestSupport {

    @ArithmeticTest
    void isZero(int one) {
        assert Num.of(one).isZero() == (0 == BigDecimal.ZERO.compareTo(big(one)));
    }

    @ArithmeticTest
    void isNotZero(int one) {
        assert Num.of(one).isNotZero() == (0 != big(one).compareTo(BigDecimal.ZERO));
    }

    @ArithmeticTest
    void isPositiveZ(int one) {
        assert Num.of(one).isPositive() == (0 < big(one).compareTo(BigDecimal.ZERO));
    }

    @ArithmeticTest
    void isPositiveOrZero(int one) {
        assert Num.of(one).isPositiveOrZero() == (0 <= big(one).compareTo(BigDecimal.ZERO));
    }

    @ArithmeticTest
    void isNegative(int one) {
        assert Num.of(one).isNegative() == (0 > big(one).compareTo(BigDecimal.ZERO));
    }

    @ArithmeticTest
    void isNegativeOrZero(int one) {
        assert Num.of(one).isNegativeOrZero() == (big(one).compareTo(BigDecimal.ZERO) <= 0);
    }
}