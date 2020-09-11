/*
 * Copyright (C) 2020 cointoss Development Team
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          https://opensource.org/licenses/MIT
 */
package cointoss.util.arithmetic;

import static cointoss.util.arithmetic.Num.*;

import java.math.RoundingMode;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import cointoss.Direction;

class NumTest {

    @Test
    void parseString() {
        for (int i = 1; i <= Num.CONTEXT.getPrecision(); i++) {
            String value = "1".repeat(i);
            assert Num.of(value).toString().equals(value);
        }
    }

    @Test
    void parseLong() {
        for (int i = 0; i <= 18; i++) {
            long value = (long) Math.pow(10, i);
            assert Num.of(value).longValue() == value;
        }

        assert Num.of(0L).longValue() == 0L;
        assert Num.of(Long.MAX_VALUE).longValue() == Long.MAX_VALUE;
        assert Num.of(Long.MIN_VALUE).longValue() == Long.MIN_VALUE;
    }

    @Test
    void max() {
        assert Num.max(ONE).is(1);
        assert Num.max(ONE, TWO, TEN).is(10);
        assert Num.max(ONE, TWO, of(-10)).is(2);
    }

    @Test
    void maxNull() {
        assert Num.max((Num) null, TWO, null).is(2);
    }

    @Test
    void maxOnSell() {
        assert Num.max(Direction.SELL, ONE).is(1);
        assert Num.max(Direction.SELL, ONE, TWO, TEN).is(1);
        assert Num.max(Direction.SELL, ONE, TWO, of(-10)).is(-10);
    }

    @Test
    void min() {
        assert Num.min(ONE).is(1);
        assert Num.min(ONE, TWO, TEN).is(1);
        assert Num.min(ONE, TWO, of(-10)).is(-10);
    }

    @Test
    void minNull() {
        assert Num.min((Num) null, TWO, null).is(2);
    }

    @Test
    void minOnSell() {
        assert Num.min(Direction.SELL, ONE).is(1);
        assert Num.min(Direction.SELL, ONE, TWO, TEN).is(10);
        assert Num.min(Direction.SELL, ONE, TWO, of(-10)).is(2);
    }

    @Test
    void between() {
        assert Num.between(ONE, TWO, TEN).is(TWO);
        assert Num.between(ONE, HUNDRED, TEN).is(TEN);
        assert Num.between(ONE, ZERO, TEN).is(ONE);
    }

    @Test
    void within() {
        assert Num.within(ONE, TWO, TEN) == true;
        assert Num.within(ONE, HUNDRED, TEN) == false;
        assert Num.within(ONE, ZERO, TEN) == false;
    }

    @Test
    void plus() {
        assert ONE.plus(TWO).is(3);
        assert ONE.plus(-1).is(0);
        assert ONE.plus("5.5").is(6.5);

        assert Num.of(10).plus(Num.of(20)).doubleValue() == 30;
        assert Num.of(10).plus(Num.of(20000)).doubleValue() == 20010;
        assert Num.of(10).plus(Num.of(0.1)).doubleValue() == 10.1;
        assert Num.of(10).plus(Num.of(0.123456)).doubleValue() == 10.123456;
    }

    @Test
    void minus() {
        assert ONE.minus(TWO).is(-1);
        assert ONE.minus(-1).is(2);
        assert ONE.minus("5.5").is(-4.5);

        assert Num.of(10).minus(Num.of(20)).doubleValue() == -10;
        assert Num.of(10).minus(Num.of(20000)).doubleValue() == -19990;
        assert Num.of(10).minus(Num.of(0.1)).doubleValue() == 9.9;
        assert Num.of(10).minus(Num.of(0.123456)).doubleValue() == 9.876544;
    }

    @Test
    void minusDirectional() {
        assert HUNDRED.minus(Direction.BUY, 20).is(80);
        assert HUNDRED.minus(Direction.SELL, 20).is(120);
    }

    @Test
    void multiply() {
        assert ONE.multiply(TWO).is(2);
        assert ONE.multiply(-1).is(-1);
        assert ONE.multiply("5.5").is(5.5);

        assert Num.of(10).multiply(Num.of(20)).doubleValue() == 200;
        assert Num.of(10).multiply(Num.of(12345678)).doubleValue() == 123456780;
        assert Num.of(10).multiply(Num.of(0.1)).doubleValue() == 1;
        assert Num.of(10).multiply(Num.of(0.00002)).doubleValue() == 0.0002;
        assert Num.of(10).multiply(Num.of(0.1234567)).doubleValue() == 1.234567;
    }

    @Test
    void divide() {
        assert ONE.divide(TWO).is(0.5);
        assert ONE.divide(-1).is(-1);
        assert ONE.divide("0.5").is(2);
        assert ZERO.divide(ONE).is(0);

        assert Num.of(10).divide(Num.of(20)).doubleValue() == 0.5;
        assert Num.of(10).divide(Num.of(12345679)).doubleValue() == 0.00000081;
        assert Num.of(10).divide(Num.of(0.1)).doubleValue() == 100;
        assert Num.of(10).divide(Num.of(0.00002)).intValue() == 500000;
        assert Num.of(10).divide(Num.of(0.1234567)).doubleValue() == 81.0000591;
    }

    @Test
    void remainder() {
        assert TEN.remainder(TWO).is(0);
        assert TEN.remainder(-3).is(1);
        assert TEN.remainder("2.4").is(0.4);

        assert Num.of(10).remainder(Num.of(3)).doubleValue() == 1;
        assert Num.of(10).remainder(Num.of(345)).doubleValue() == 10;
        assert Num.of(10).remainder(Num.of(0.1)).doubleValue() == 0;
        assert Num.of(10).remainder(Num.of(0.00002)).intValue() == 0;
    }

    @Test
    void modulo() {
        assert TEN.modulo(TWO).is(0);
        assert TEN.modulo(-3).is(1);
        assert TEN.modulo("2.4").is(0.4);

        assert Num.of(10).modulo(Num.of(3)).doubleValue() == 1;
        assert Num.of(10).modulo(Num.of(345)).doubleValue() == 10;
        assert Num.of(10).modulo(Num.of(0.1)).doubleValue() == 0;
        assert Num.of(10).modulo(Num.of(0.00002)).intValue() == 0;
    }

    @Test
    void abs() {
        assert ONE.abs().is(1);
        assert Num.of(-1).abs().is(1);
        assert Num.of(-0.5).abs().is(0.5);
        assert Num.of(100).abs().doubleValue() == 100;
        assert Num.of(-100).abs().doubleValue() == 100;
    }

    @Test
    void scaleSize() {
        assert Num.of(1).scale(1).scale() == 0;
        assert Num.of(13).scale(2).scale() == 0;
        assert Num.of(123).scale(3).scale() == 0;

        assert Num.of(1.10).scale(3).scale() == 1;
        assert Num.of(10.220).scale(3).scale() == 2;
        assert Num.of(123.456).scale(3).scale() == 3;
    }

    @Test
    void scale() {
        assert Num.of("10.1").scale(1).is("10.1");
        assert Num.of(1).scale(2).is(1);

        assert Num.of("1.234").scale(1).is("1.2");
        assert Num.of("1.234").scale(2).is("1.23");
        assert Num.of("0.05").scale(1).is("0.1");

        assert Num.of(21.234).scale(-1).is(20);
        assert Num.of(321.234).scale(-2).is(300);
    }

    @Test
    void scaleDown() {
        assert Num.of(1).scale(0, RoundingMode.DOWN).is(1);
        assert Num.of(12).scale(0, RoundingMode.DOWN).is(12);
        assert Num.of(123).scale(0, RoundingMode.DOWN).is(123);

        assert Num.of(1).scale(1, RoundingMode.DOWN).is(1);
        assert Num.of(12).scale(-1, RoundingMode.DOWN).is(10);
        assert Num.of(123).scale(-1, RoundingMode.DOWN).is(120);
        assert Num.of(0.05).scale(1, RoundingMode.DOWN).is(0);
    }

    @Test
    void pow() {
        assert Num.of(2).pow(0).is(1);
        assert Num.of(2).pow(1).is(2);
        assert Num.of(2).pow(2).is(4);

        assert Num.of(-2.5).pow(0).is(1);
        assert Num.of(-2.5).pow(1).is(-2.5);
        assert Num.of(-2.5).pow(2).is(6.25);

        assert Num.of(10).pow(2).doubleValue() == 100;
        assert Num.of(10).pow(-2).doubleValue() == 0.01;
        assert Num.of(10).pow(2.5).doubleValue() == 316.22776601683796;
        assert Num.of(10).pow(-2.5).doubleValue() == 0.00316227766016;
    }

    @Test
    void sqrt() {
        assert Num.of(0).sqrt().is(0);
        assert Num.of(1).sqrt().is(1);
        assert Num.of(4).sqrt().is(2);

        assert Num.of(100).sqrt().doubleValue() == 10;
        assert Num.of(3).sqrt().doubleValue() == 1.73205080756887;
    }

    @Test
    void isLessThan() {
        assert ONE.isLessThan(2);
        assert ONE.isLessThan(1) == false;
        assert ONE.isLessThan("3");
        assert ONE.isLessThan(TEN);
        assert ONE.isLessThan(5.3);
    }

    @Test
    void isLessThanOrEqual() {
        assert ONE.isLessThanOrEqual(2);
        assert ONE.isLessThanOrEqual(1);
        assert ONE.isLessThanOrEqual("3");
        assert ONE.isLessThanOrEqual(TEN);
        assert ONE.isLessThanOrEqual(5.345);
    }

    @Test
    void isLessThanDirectional() {
        assert HUNDRED.isLessThan(Direction.BUY, 120) == true;
        assert HUNDRED.isLessThan(Direction.BUY, 80) == false;
        assert HUNDRED.isLessThan(Direction.SELL, 120) == false;
        assert HUNDRED.isLessThan(Direction.SELL, 80) == true;
    }

    @Test
    void isGreaterThan() {
        assert ONE.isGreaterThan(2) == false;
        assert ONE.isGreaterThan(1) == false;
        assert ONE.isGreaterThan("0");
        assert ONE.isGreaterThan(TEN) == false;
        assert ONE.isGreaterThan(0.052);
    }

    @Test
    void isGreaterThanOrEqual() {
        assert ONE.isGreaterThanOrEqual(2) == false;;
        assert ONE.isGreaterThanOrEqual(1);
        assert ONE.isGreaterThanOrEqual("0");
        assert ONE.isGreaterThanOrEqual(TEN) == false;
        assert ONE.isGreaterThanOrEqual(1.355) == false;
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

    @Test
    void computeScale() {
        assert Num.computeScale(0.1) == 1;
        assert Num.computeScale(0.12) == 2;
        assert Num.computeScale(0.123) == 3;
        assert Num.computeScale(0.1234) == 4;
        assert Num.computeScale(0.100) == 1;
        assert Num.computeScale(0.02) == 2;
        assert Num.computeScale(0.02040) == 4;

        for (int i = 1; i < 10000; i++) {
            if (i % 1000 == 0) {
                assert Num.computeScale(i * 0.0001) == 1;
            } else if (i % 100 == 0) {
                assert Num.computeScale(i * 0.0001) == 2;
            } else if (i % 10 == 0) {
                assert Num.computeScale(i * 0.0001) == 3;
            } else {
                assert Num.computeScale(i * 0.0001) == 4;
            }
        }

        assert Num.computeScale(1e+1) == 0;
        assert Num.computeScale(1e+2) == 0;
        assert Num.computeScale(1e+3) == 0;
        assert Num.computeScale(1e+4) == 0;
        assert Num.computeScale(1e+5) == 0;
        assert Num.computeScale(1e+6) == 0;
        assert Num.computeScale(1e+7) == 0;

        assert Num.computeScale(1e-1) == 1;
        assert Num.computeScale(1e-2) == 2;
        assert Num.computeScale(1e-3) == 3;
        assert Num.computeScale(1e-4) == 4;
        assert Num.computeScale(1e-5) == 5;
        assert Num.computeScale(1e-6) == 6;
        assert Num.computeScale(1e-7) == 7;
        assert Num.computeScale(1e-8) == 8;
        assert Num.computeScale(1e-9) == 9;
        assert Num.computeScale(1e-10) == 10;
        // assert Num.computeScale(1e-11) == 11;
        assert Num.computeScale(1e-12) == 12;
        assert Num.computeScale(1e-13) == 13;
        assert Num.computeScale(Double.MAX_VALUE) == 14;
    }

    @Test
    void decuple() {
        assert Num.of(10).decuple(2).doubleValue() == 1000;
        assert Num.of(10).decuple(-2).doubleValue() == 0.1;
    }

    @Test
    void negate() {
        assert Num.of(100).negate().doubleValue() == -100;
        assert Num.of(-100).negate().doubleValue() == 100;
    }

    @Test
    void promotePlus() {
        assert Num.of(Long.MAX_VALUE).plus(Long.MAX_VALUE).toString().equals("18446744073709551614");
    }

    @Test
    void promoteMinus() {
        assert Num.of(Long.MIN_VALUE).minus(Long.MAX_VALUE).toString().equals("-18446744073709551615");
    }

    @Test
    void promoteMultiply() {
        assert Num.of(1234567898765L).multiply(1234567898765L).toString().equals("1524157896661027288525225");
    }

    @Test
    @Disabled
    void promoteDivide() {
        assert Num.of(Long.MAX_VALUE).divide(Double.MIN_VALUE).toString().equals("-18446744073709551615");
    }

    @Test
    void promotePow() {
        assert Num.of(Long.MAX_VALUE).pow(2).toString().equals("85070591730234615847396907784232501249");
    }

    @Test
    void promotePow10() {
        assert Num.of(Long.MAX_VALUE).decuple(5).toString().equals("9.223372036854774E23");
    }

    @Test
    void equals() {
        assert Num.of(12345).equals(Num.of(12345));
        assert Num.of("12345").equals(Num.of("12345"));
        assert Num.of(12345).equals(Num.of("12345"));
        assert Num.of("12345").equals(Num.of(12345));
    }

    @Test
    void stringlize() {
        assert Num.of(12345).toString().equals("12345");
    }
}