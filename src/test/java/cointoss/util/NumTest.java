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

import org.junit.Test;

import cointoss.Side;

/**
 * @version 2018/02/17 9:35:49
 */
public class NumTest {

    @Test
    public void max() throws Exception {
        assert Num.max(ONE).is(1);
        assert Num.max(ONE, TWO, TEN).is(10);
        assert Num.max(ONE, TWO, of(-10)).is(2);
        assert Num.max().isNaN();
        assert Num.max((Num[]) null).isNaN();
        assert Num.max(ONE, null, TWO).is(2);
        assert Num.max(null, null, null).isNaN();
    }

    @Test
    public void min() throws Exception {
        assert Num.min(ONE).is(1);
        assert Num.min(ONE, TWO, TEN).is(1);
        assert Num.min(ONE, TWO, of(-10)).is(-10);
        assert Num.min().isNaN();
        assert Num.min((Num[]) null).isNaN();
        assert Num.min(ONE, null, TWO).is(1);
        assert Num.min(null, null, null).isNaN();
    }

    @Test
    public void within() throws Exception {
        assert Num.within(ONE, TWO, TEN).is(TWO);
        assert Num.within(ONE, THOUSAND, TEN).is(TEN);
        assert Num.within(ONE, ZERO, TEN).is(ONE);
    }

    @Test
    public void plus() throws Exception {
        assert ONE.plus(TWO).is(3);
        assert ONE.plus(-1).is(0);
        assert ONE.plus("5.5").is(6.5);
        assert ONE.plus((Num) null).isNaN();
    }

    @Test
    public void minus() throws Exception {
        assert ONE.minus(TWO).is(-1);
        assert ONE.minus(-1).is(2);
        assert ONE.minus("5.5").is(-4.5);
        assert ONE.minus((Num) null).isNaN();
    }

    @Test
    public void minusDirectional() throws Exception {
        assert HUNDRED.minus(Side.BUY, 20).is(80);
        assert HUNDRED.minus(Side.SELL, 20).is(120);
    }

    @Test
    public void multiply() throws Exception {
        assert ONE.multiply(TWO).is(2);
        assert ONE.multiply(-1).is(-1);
        assert ONE.multiply("5.5").is(5.5);
        assert ONE.multiply((Num) null).isNaN();
    }

    @Test
    public void divide() throws Exception {
        assert ONE.divide(TWO).is(0.5);
        assert ONE.divide(-1).is(-1);
        assert ONE.divide("0.5").is(2);
        assert ONE.divide((Num) null).isNaN();
    }

    @Test
    public void remainder() throws Exception {
        assert TEN.remainder(TWO).is(0);
        assert TEN.remainder(-3).is(1);
        assert TEN.remainder("2.4").is(0.4);
        assert TEN.remainder((Num) null).isNaN();
    }

    @Test
    public void abs() throws Exception {
        assert ONE.abs().is(1);
        assert Num.of(-1).abs().is(1);
        assert Num.of(-0.5).abs().is(0.5);
        assert NaN.abs().isNaN();
    }

    @Test
    public void scale() throws Exception {
        assert Num.of(1).scale(0).is(1);
        assert Num.of(1).scale(1).is(1);
        assert Num.of(1).scale(2).is(1);

        assert Num.of(1.234).scale(0).is(1);
        assert Num.of(1.234).scale(1).is(1.2);
        assert Num.of(1.234).scale(2).is(1.23);

        assert NaN.scale(2).isNaN();
    }

    @Test
    public void scaleDown() throws Exception {
        assert Num.of(1).scaleDown(0).is(1);
        assert Num.of(12).scaleDown(0).is(12);
        assert Num.of(123).scaleDown(0).is(123);

        assert Num.of(1).scaleDown(1).is(1);
        assert Num.of(12).scaleDown(-1).is(10);
        assert Num.of(123).scaleDown(-1).is(120);
    }

    @Test
    public void pow() throws Exception {
        assert Num.of(2).pow(0).is(1);
        assert Num.of(2).pow(1).is(2);
        assert Num.of(2).pow(2).is(4);

        assert Num.of(-2.5).pow(0).is(1);
        assert Num.of(-2.5).pow(1).is(-2.5);
        assert Num.of(-2.5).pow(2).is(6.25);

        assert NaN.pow(1).isNaN();
    }

    @Test
    public void sqrt() throws Exception {
        assert Num.of(0).sqrt().is(0);
        assert Num.of(1).sqrt().is(1);
        assert Num.of(4).sqrt().is(2);
        assert NaN.sqrt().isNaN();
    }

    @Test
    public void isLessThan() throws Exception {
        assert ONE.isLessThan(2);
        assert ONE.isLessThan(1) == false;
        assert ONE.isLessThan("3");
        assert ONE.isLessThan(TEN);
        assert ONE.isLessThan((Num) null) == false;
    }

    @Test
    public void isLessThanOrEqual() throws Exception {
        assert ONE.isLessThanOrEqual(2);
        assert ONE.isLessThanOrEqual(1);
        assert ONE.isLessThanOrEqual("3");
        assert ONE.isLessThanOrEqual(TEN);
        assert ONE.isLessThanOrEqual((Num) null) == false;
    }

    @Test
    public void isLessThanDirectional() throws Exception {
        assert HUNDRED.isLessThan(Side.BUY, 120) == true;
        assert HUNDRED.isLessThan(Side.BUY, 80) == false;
        assert HUNDRED.isLessThan(Side.SELL, 120) == false;
        assert HUNDRED.isLessThan(Side.SELL, 80) == true;
    }

    @Test
    public void isGreaterThan() throws Exception {
        assert ONE.isGreaterThan(2) == false;
        assert ONE.isGreaterThan(1) == false;
        assert ONE.isGreaterThan("0");
        assert ONE.isGreaterThan(TEN) == false;
        assert ONE.isGreaterThan((Num) null) == false;
    }

    @Test
    public void isGreaterThanOrEqual() throws Exception {
        assert ONE.isGreaterThanOrEqual(2) == false;;
        assert ONE.isGreaterThanOrEqual(1);
        assert ONE.isGreaterThanOrEqual("0");
        assert ONE.isGreaterThanOrEqual(TEN) == false;
        assert ONE.isGreaterThanOrEqual((Num) null) == false;
    }

    @Test
    public void isZero() throws Exception {
        assert Num.of(1).isZero() == false;
        assert Num.of(0).isZero();
        assert Num.NaN.isZero() == false;
    }

    @Test
    public void isNotZero() throws Exception {
        assert Num.of(1).isNotZero();
        assert Num.of(0).isNotZero() == false;
        assert Num.NaN.isNotZero();
    }

    @Test
    public void isPositive() throws Exception {
        assert Num.of(1).isPositive() == true;
        assert Num.of(0).isPositive() == false;
        assert Num.of(-1).isPositive() == false;
        assert Num.NaN.isPositive() == false;
    }

    @Test
    public void isPositiveOrZero() throws Exception {
        assert Num.of(1).isPositiveOrZero() == true;
        assert Num.of(0).isPositiveOrZero() == true;
        assert Num.of(-1).isPositiveOrZero() == false;
        assert Num.NaN.isPositiveOrZero() == false;
    }

    @Test
    public void isNegative() throws Exception {
        assert Num.of(1).isNegative() == false;
        assert Num.of(0).isNegative() == false;
        assert Num.of(-1).isNegative() == true;
        assert Num.NaN.isNegative() == false;
    }

    @Test
    public void isNegativeOrZero() throws Exception {
        assert Num.of(1).isNegativeOrZero() == false;
        assert Num.of(0).isNegativeOrZero() == true;
        assert Num.of(-1).isNegativeOrZero() == true;
        assert Num.NaN.isNegativeOrZero() == false;
    }
}
