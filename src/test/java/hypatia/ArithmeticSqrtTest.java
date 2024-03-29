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

import org.junit.jupiter.api.Assertions;

import hypatia.Num;

class ArithmeticSqrtTest extends ArithmeticTestSupport {

    @ArithmeticTest
    void sqrt(int one) {
        if (isPositive(one)) {
            assert equalityVaguely(Num.of(one).sqrt(), big(one).sqrt(Num.CONTEXT));
        } else {
            Assertions.assertThrows(ArithmeticException.class, () -> Num.of(one).sqrt());
        }
    }

    @ArithmeticTest
    void sqrt(long one) {
        if (isPositive(one)) {
            assert equalityVaguely(Num.of(one).sqrt(), big(one).sqrt(Num.CONTEXT));
        } else {
            Assertions.assertThrows(ArithmeticException.class, () -> Num.of(one).sqrt());
        }
    }

    @ArithmeticTest
    void sqrt(double one) {
        if (isPositive(one)) {
            assert equalityVaguely(Num.of(one).sqrt(), big(one).sqrt(Num.CONTEXT));
        } else {
            Assertions.assertThrows(ArithmeticException.class, () -> Num.of(one).sqrt());
        }
    }

    @ArithmeticTest
    void sqrt(String one) {
        if (isPositive(one)) {
            assert equalityVaguely(Num.of(one).sqrt(), big(one).sqrt(Num.CONTEXT));
        } else {
            Assertions.assertThrows(ArithmeticException.class, () -> Num.of(one).sqrt());
        }
    }
}