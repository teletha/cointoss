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
import kiss.Variable;

class ArithmeticRemainderTest extends ArithmeticTestSupport {

    @ArithmeticTest
    void primitiveInt(int one, int other) {
        if (!zeroIsEqualTo(other)) {
            assert equalityVaguely(Num.of(one).remainder(other), big(one).remainder(big(other)));
        } else {
            Assertions.assertThrows(ArithmeticException.class, () -> Num.of(one).remainder(other));
        }
    }

    @ArithmeticTest
    void primitiveLong(long one, long other) {
        if (!zeroIsEqualTo(other)) {
            assert equalityVaguely(Num.of(one).remainder(other), big(one).remainder(big(other)));
        } else {
            Assertions.assertThrows(ArithmeticException.class, () -> Num.of(one).remainder(other));
        }
    }

    @ArithmeticTest
    void primitiveDouble(double one, double other) {
        if (!zeroIsEqualTo(other)) {
            assert equalityVaguely(Num.of(one).remainder(other), big(one).remainder(big(other)));
        } else {
            Assertions.assertThrows(ArithmeticException.class, () -> Num.of(one).remainder(other));
        }
    }

    @ArithmeticTest
    void numeralString(String one, String other) {
        if (!zeroIsEqualTo(other)) {
            assert equalityVaguely(Num.of(one).remainder(other), big(one).remainder(big(other)));
        } else {
            Assertions.assertThrows(ArithmeticException.class, () -> Num.of(one).remainder(other));
        }
    }

    @ArithmeticTest
    void number(Num one, Num other) {
        if (!zeroIsEqualTo(other)) {
            assert equalityVaguely(one.remainder(other), big(one).remainder(big(other)));
        } else {
            Assertions.assertThrows(ArithmeticException.class, () -> one.remainder(other));
        }
    }

    @ArithmeticTest
    void numberVariable(Variable<Num> one, Variable<Num> other) {
        if (!zeroIsEqualTo(other)) {
            assert equalityVaguely(one.v.remainder(other), big(one).remainder(big(other)));
        } else {
            Assertions.assertThrows(ArithmeticException.class, () -> one.v.remainder(other));
        }
    }
}