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

import static cointoss.util.arithmetic.Num.*;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import cointoss.Direction;
import cointoss.util.arithmetic.Decimal;
import cointoss.util.arithmetic.Num;

/**
 * 
 */
class DecimalTest {

    @Test
    void computeScale() {
        assert Decimal.computeScale(0.1) == 1;
        assert Decimal.computeScale(0.12) == 2;
        assert Decimal.computeScale(0.123) == 3;
        assert Decimal.computeScale(0.1234) == 4;
        assert Decimal.computeScale(0.100) == 1;
        assert Decimal.computeScale(0.02) == 2;
        assert Decimal.computeScale(0.02040) == 4;

        for (int i = 1; i < 10000; i++) {
            if (i % 1000 == 0) {
                assert Decimal.computeScale(i * 0.0001) == 1;
            } else if (i % 100 == 0) {
                assert Decimal.computeScale(i * 0.0001) == 2;
            } else if (i % 10 == 0) {
                assert Decimal.computeScale(i * 0.0001) == 3;
            } else {
                assert Decimal.computeScale(i * 0.0001) == 4;
            }
        }

        assert Decimal.computeScale(1e19) == 18;
        assert Decimal.computeScale(1e100) == 18;
        assert Decimal.computeScale(1e300) == 18;
        assert Decimal.computeScale(Double.MIN_VALUE) == 0;
        assert Decimal.computeScale(Double.MAX_VALUE) == 18;
    }

    @Test
    void plus() {
        assert Num.of(10).plus(Num.of(20)).doubleValue() == 30;
        assert Num.of(10).plus(Num.of(20000)).doubleValue() == 20010;
        assert Num.of(10).plus(Num.of(0.1)).doubleValue() == 10.1;
        assert Num.of(10).plus(Num.of(0.123456)).doubleValue() == 10.123456;
    }

    @Test
    void minus() {
        assert Num.of(10).minus(Num.of(20)).doubleValue() == -10;
        assert Num.of(10).minus(Num.of(20000)).doubleValue() == -19990;
        assert Num.of(10).minus(Num.of(0.1)).doubleValue() == 9.9;
        assert Num.of(10).minus(Num.of(0.123456)).doubleValue() == 9.876544;
    }

    @Test
    void multiply() {
        assert Num.of(10).multiply(Num.of(20)).doubleValue() == 200;
        assert Num.of(10).multiply(Num.of(12345678)).doubleValue() == 123456780;
        assert Num.of(10).multiply(Num.of(0.1)).doubleValue() == 1;
        assert Num.of(10).multiply(Num.of(0.00002)).doubleValue() == 0.0002;
        assert Num.of(10).multiply(Num.of(0.1234567)).doubleValue() == 1.234567;
    }

    @Test
    void divide() {
        assert Num.of(10).divide(Num.of(20)).doubleValue() == 0.5;
        assert Num.of(10).divide(Num.of(12345678)).doubleValue() == 0.00000081;
        assert Num.of(10).divide(Num.of(0.1)).doubleValue() == 100;
        assert Num.of(10).divide(Num.of(0.00002)).intValue() == 500000;
        assert Num.of(10).divide(Num.of(0.1234567)).doubleValue() == 81.00005;
    }

    @Test
    void remainder() {
        assert Num.of(10).remainder(Num.of(3)).doubleValue() == 1;
        assert Num.of(10).remainder(Num.of(345)).doubleValue() == 10;
        assert Num.of(10).remainder(Num.of(0.1)).doubleValue() == 0;
        assert Num.of(10).remainder(Num.of(0.00002)).intValue() == 0;
    }

    @Test
    void pow() {
        assert Num.of(10).pow(2).doubleValue() == 100;
        assert Num.of(10).pow(-2).doubleValue() == 0.01;
        assert Num.of(10).pow(2.5).doubleValue() == 316.227766016837;
        assert Num.of(10).pow(-2.5).doubleValue() == 0.00316227766;
    }

    @Test
    void decuple() {
        assert Num.of(10).decuple(2).doubleValue() == 1000;
        assert Num.of(10).decuple(-2).doubleValue() == 0.1;
    }

    @Test
    void sqrt() {
        assert Num.of(100).sqrt().doubleValue() == 10;
        assert Num.of(3).sqrt().doubleValue() == 1.732050807568;
    }

    @Test
    void abs() {
        assert Num.of(100).abs().doubleValue() == 100;
        assert Num.of(-100).abs().doubleValue() == 100;
    }

    @Test
    void negate() {
        assert Num.of(100).negate().doubleValue() == -100;
        assert Num.of(-100).negate().doubleValue() == 100;
    }

    @Test
    void scale() {
        assert Num.of("10.1").scale(1).doubleValue() == 10.1;
        assert Num.of(1).scale(2).doubleValue() == 1;

        assert Num.of("1.234").scale(1).doubleValue() == 1.2;
        assert Num.of("1.234").scale(2).doubleValue() == 1.23;
        assert Num.of("0.05").scale(1).doubleValue() == 0.1;

        assert Num.of(21.234).scale(-1).doubleValue() == 20;
        assert Num.of(321.234).scale(-2).doubleValue() == 300;
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
        assert ONE.isGreaterThan(1.355) == false;
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
    void promote() {
        assert Num.of("12345678901234567890").toString().equals("12345678901234600000");
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
}
