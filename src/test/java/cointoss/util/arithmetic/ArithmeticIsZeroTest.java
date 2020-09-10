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

class ArithmeticIsZeroTest extends ArithmeticTestSupport {

    @ArithmeticTest
    void isZero(int one) {
        assert Num.of(one).isZero() == (0 == BigDecimal.ZERO.compareTo(big(one)));
    }
}
