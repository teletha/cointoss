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

class ArithmeticMultiplyTest extends ArithmeticTestSupport {

    @ArithmeticTest
    void primitiveInt(int one, int other) {
        assert equality(Num.of(one).multiply(other), big(one).multiply(big(other)));
    }

    @ArithmeticTest
    void primitiveLong(long one, long other) {
        assert equality(Num.of(one).multiply(other), big(one).multiply(big(other)));
    }

    @ArithmeticTest
    void primitiveDouble(double one, double other) {
        assert equalityVaguely(Num.of(one).multiply(other), big(one).multiply(big(other)));
    }

    @ArithmeticTest
    void numeralString(String one, String other) {
        assert equalityVaguely(Num.of(one).multiply(other), big(one).multiply(big(other)));
    }

    @ArithmeticTest
    void number(Num value) {
        assert Num.ONE.multiply(value).equals(value);
    }

    @ArithmeticTest
    void number(Num one, Num other) {
        assert equalityVaguely(one.multiply(other), big(one).multiply(big(other)));
    }

    @ArithmeticTest
    void numberVariable(Variable<Num> one, Variable<Num> other) {
        assert equalityVaguely(one.v.multiply(other), big(one).multiply(big(other)));
    }
}