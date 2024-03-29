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
import org.junit.jupiter.api.Test;

import hypatia.Num;

class ArithmeticFloorTest extends ArithmeticTestSupport {

    @Test
    void floor() {
        assert Num.of(100).floor(Num.of(30)).is(90);
        assert Num.of(100).floor(Num.of(1)).is(100);
        assert Num.of(100).floor(Num.of(0.1)).is(100);
        assert Num.of(100).floor(Num.of(0.3)).is(99.9);
    }

    @ArithmeticTest
    void floor(int value) {
        if (!zeroIsEqualTo(value)) {
            assert equality(Num.of(100).floor(Num.of(value)), big(100).subtract(big(100).remainder(big(value))));
        } else {
            Assertions.assertThrows(ArithmeticException.class, () -> Num.of(100).floor(Num.of(value)));
        }
    }

    @ArithmeticTest
    void floor(double value) {
        if (!zeroIsEqualTo(value)) {
            assert equalityVaguely(Num.of(100).floor(Num.of(value)), big(100).subtract(big(100).remainder(big(value))));
        } else {
            Assertions.assertThrows(ArithmeticException.class, () -> Num.of(100).floor(Num.of(value)));
        }
    }
}