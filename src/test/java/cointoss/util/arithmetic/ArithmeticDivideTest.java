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

import org.junit.jupiter.api.Disabled;

import kiss.Variable;

@Disabled
class ArithmeticDivideTest extends ArithmeticTestSupport {

    @ArithmeticTest
    void primitiveInt(int one, int other) {
        if (other == 0) {
            return;
        }
        assert equalityVaguely(Num.of(one).divide(other), big(one).divide(big(other), Num.CONTEXT));
    }

    @ArithmeticTest
    void primitiveLong(long one, long other) {
        assert equalityVaguely(Num.of(one).divide(other), big(one).divide(big(other)));
    }

    @ArithmeticTest
    void primitiveDouble(double one, double other) {
        assert equalityVaguely(Num.of(one).divide(other), big(one).divide(big(other)));
    }

    @ArithmeticTest
    void numeralString(String one, String other) {
        assert equalityVaguely(Num.of(one).divide(other), big(one).divide(big(other)));
    }

    @ArithmeticTest
    void number(Num value) {
        assert Num.ONE.divide(value).equals(value);
    }

    @ArithmeticTest
    void number(Num one, Num other) {
        assert equalityVaguely(one.divide(other), big(one).divide(big(other)));
    }

    @ArithmeticTest
    void numberVariable(Variable<Num> one, Variable<Num> other) {
        assert equalityVaguely(one.v.divide(other), big(one).divide(big(other)));
    }
}
