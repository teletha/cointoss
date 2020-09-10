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

class ArithmeticIsNotZeroTest extends ArithmeticTestSupport {

    @ArithmeticTest
    void isNotZero(int one) {
        assert Num.of(one).isNotZero() == (0 != big(one).compareTo(BigDecimal.ZERO));
    }
}
