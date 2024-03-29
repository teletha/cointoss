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

import hypatia.Num;
import kiss.Variable;

class ArithmeticCompareToTest extends ArithmeticTestSupport {

    @ArithmeticTest
    void primitiveInt(int one, int other) {
        assert Num.of(one).compareTo(Num.of(other)) == big(one).compareTo(big(other));
    }

    @ArithmeticTest
    void primitiveLong(long one, long other) {
        assert Num.of(one).compareTo(Num.of(other)) == big(one).compareTo(big(other));
    }

    @ArithmeticTest
    void primitiveDouble(double one, double other) {
        assert Num.of(one).compareTo(Num.of(other)) == big(one).compareTo(big(other));
    }

    @ArithmeticTest
    void numeralString(String one, String other) {
        assert Num.of(one).compareTo(Num.of(other)) == big(one).compareTo(big(other));
    }

    @ArithmeticTest
    void number(Num one, Num other) {
        assert one.compareTo(other) == big(one).compareTo(big(other));
    }

    @ArithmeticTest
    void numberVariable(Variable<Num> one, Variable<Num> other) {
        assert one.v.compareTo(other.v) == big(one).compareTo(big(other));
    }
}