/*
 * Copyright (C) 2018 CoinToss Development Team
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          https://opensource.org/licenses/MIT
 */
package cointoss.util;

import static cointoss.util.Num.*;

import org.junit.jupiter.api.Test;

import cointoss.Side;

/**
 * @version 2018/04/02 16:46:50
 */
class NumTest {

    @Test
    void max() {
        assert Num.max(ONE).is(1);
        assert Num.max(ONE, TWO, TEN).is(10);
        assert Num.max(ONE, TWO, of(-10)).is(2);
    }

    @Test
    void min() {
        assert Num.min(ONE).is(1);
        assert Num.min(ONE, TWO, TEN).is(1);
        assert Num.min(ONE, TWO, of(-10)).is(-10);
    }

    @Test
    void within() {
        assert Num.within(ONE, TWO, TEN).is(TWO);
        assert Num.within(ONE, THOUSAND, TEN).is(TEN);
        assert Num.within(ONE, ZERO, TEN).is(ONE);
    }

    @Test
    void plus() {
        assert ONE.plus(TWO).is(3);
        assert ONE.plus(-1).is(0);
        assert ONE.plus("5.5").is(6.5);
    }

    @Test
    void minus() {
        assert ONE.minus(TWO).is(-1);
        assert ONE.minus(-1).is(2);
        assert ONE.minus("5.5").is(-4.5);
    }

    @Test
    void minusDirectional() {
        assert HUNDRED.minus(Side.BUY, 20).is(80);
        assert HUNDRED.minus(Side.SELL, 20).is(120);
    }

    @Test
    void multiply() {
        assert ONE.multiply(TWO).is(2);
        assert ONE.multiply(-1).is(-1);
        assert ONE.multiply("5.5").is(5.5);
    }

    @Test
    void divide() {
        assert ONE.divide(TWO).is(0.5);
        assert ONE.divide(-1).is(-1);
        assert ONE.divide("0.5").is(2);
    }

    @Test
    void remainder() {
        assert TEN.remainder(TWO).is(0);
        assert TEN.remainder(-3).is(1);
        assert TEN.remainder("2.4").is(0.4);
    }

    @Test
    void abs() {
        assert ONE.abs().is(1);
        assert Num.of(-1).abs().is(1);
        assert Num.of(-0.5).abs().is(0.5);
    }

    @Test
    void scale() {
        assert Num.of(1).scale(0).is(1);
        assert Num.of(1).scale(1).is(1);
        assert Num.of(1).scale(2).is(1);

        assert Num.of(1.234).scale(0).is(1);
        assert Num.of(1.234).scale(1).is(1.2);
        assert Num.of(1.234).scale(2).is(1.23);
        assert Num.of(0.05).scale(1).is(0.1);
    }

    @Test
    void scaleDown() {
        assert Num.of(1).scaleDown(0).is(1);
        assert Num.of(12).scaleDown(0).is(12);
        assert Num.of(123).scaleDown(0).is(123);

        assert Num.of(1).scaleDown(1).is(1);
        assert Num.of(12).scaleDown(-1).is(10);
        assert Num.of(123).scaleDown(-1).is(120);
        assert Num.of(0.05).scaleDown(1).is(0);
    }

    @Test
    void pow() {
        assert Num.of(2).pow(0).is(1);
        assert Num.of(2).pow(1).is(2);
        assert Num.of(2).pow(2).is(4);

        assert Num.of(-2.5).pow(0).is(1);
        assert Num.of(-2.5).pow(1).is(-2.5);
        assert Num.of(-2.5).pow(2).is(6.25);
    }

    @Test
    void sqrt() {
        assert Num.of(0).sqrt().is(0);
        assert Num.of(1).sqrt().is(1);
        assert Num.of(4).sqrt().is(2);
    }

    @Test
    void isLessThan() {
        assert ONE.isLessThan(2);
        assert ONE.isLessThan(1) == false;
        assert ONE.isLessThan("3");
        assert ONE.isLessThan(TEN);
    }

    @Test
    void isLessThanOrEqual() {
        assert ONE.isLessThanOrEqual(2);
        assert ONE.isLessThanOrEqual(1);
        assert ONE.isLessThanOrEqual("3");
        assert ONE.isLessThanOrEqual(TEN);
    }

    @Test
    void isLessThanDirectional() {
        assert HUNDRED.isLessThan(Side.BUY, 120) == true;
        assert HUNDRED.isLessThan(Side.BUY, 80) == false;
        assert HUNDRED.isLessThan(Side.SELL, 120) == false;
        assert HUNDRED.isLessThan(Side.SELL, 80) == true;
    }

    @Test
    void isGreaterThan() {
        assert ONE.isGreaterThan(2) == false;
        assert ONE.isGreaterThan(1) == false;
        assert ONE.isGreaterThan("0");
        assert ONE.isGreaterThan(TEN) == false;
    }

    @Test
    void isGreaterThanOrEqual() {
        assert ONE.isGreaterThanOrEqual(2) == false;;
        assert ONE.isGreaterThanOrEqual(1);
        assert ONE.isGreaterThanOrEqual("0");
        assert ONE.isGreaterThanOrEqual(TEN) == false;
    }

    @Test
    void isZero() {
        assert Num.of(1).isZero() == false;
        assert Num.of(0).isZero();
    }

    @Test
    void isNotZero() {
        assert Num.of(1).isNotZero();
        assert Num.of(0).isNotZero() == false;
    }

    @Test
    void isPositive() {
        assert Num.of(1).isPositive() == true;
        assert Num.of(0).isPositive() == false;
        assert Num.of(-1).isPositive() == false;
    }

    @Test
    void isPositiveOrZero() {
        assert Num.of(1).isPositiveOrZero() == true;
        assert Num.of(0).isPositiveOrZero() == true;
        assert Num.of(-1).isPositiveOrZero() == false;
    }

    @Test
    void isNegative() {
        assert Num.of(1).isNegative() == false;
        assert Num.of(0).isNegative() == false;
        assert Num.of(-1).isNegative() == true;
    }

    @Test
    void isNegativeOrZero() {
        assert Num.of(1).isNegativeOrZero() == false;
        assert Num.of(0).isNegativeOrZero() == true;
        assert Num.of(-1).isNegativeOrZero() == true;
    }
}
