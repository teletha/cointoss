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

import com.google.common.math.DoubleMath;

class ArithmeticPlusTest extends ArithmeticTestSupport {

    @ArithmeticTest
    void primitiveInt(int value) {
        assert Num.ZERO.plus(value).intValue() == value;
    }

    @ArithmeticTest
    void primitiveInt(long value) {
        assert Num.ZERO.plus(value).longValue() == value;
    }

    @ArithmeticTest
    void primitiveDouble(double value) {
        assert DoubleMath.fuzzyEquals(Num.ZERO.plus(value).doubleValue(), value, Fuzzy);
    }
}
