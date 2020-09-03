/*
 * Copyright (C) 2020 Nameless Production Committee
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          http://opensource.org/licenses/mit-license.php
 */
package cointoss.util.decimal;

import static cointoss.util.decimal.Decimal.*;

import org.junit.jupiter.api.Test;

import cointoss.Direction;

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
    }

    @Test
    void plus() {
        assert Decimal.of(10).plus(Decimal.of(20)).doubleValue() == 30;
        assert Decimal.of(10).plus(Decimal.of(20000)).doubleValue() == 20010;
        assert Decimal.of(10).plus(Decimal.of(0.1)).doubleValue() == 10.1;
        assert Decimal.of(10).plus(Decimal.of(0.123456)).doubleValue() == 10.123456;
    }

    @Test
    void minus() {
        assert Decimal.of(10).minus(Decimal.of(20)).doubleValue() == -10;
        assert Decimal.of(10).minus(Decimal.of(20000)).doubleValue() == -19990;
        assert Decimal.of(10).minus(Decimal.of(0.1)).doubleValue() == 9.9;
        assert Decimal.of(10).minus(Decimal.of(0.123456)).doubleValue() == 9.876544;
    }

    @Test
    void multiply() {
        assert Decimal.of(10).multiply(Decimal.of(20)).doubleValue() == 200;
        assert Decimal.of(10).multiply(Decimal.of(12345678)).doubleValue() == 123456780;
        assert Decimal.of(10).multiply(Decimal.of(0.1)).doubleValue() == 1;
        assert Decimal.of(10).multiply(Decimal.of(0.00002)).doubleValue() == 0.0002;
        assert Decimal.of(10).multiply(Decimal.of(0.1234567)).doubleValue() == 1.234567;
    }

    @Test
    void divide() {
        assert Decimal.of(10).divide(Decimal.of(20)).doubleValue() == 0.5;
        assert Decimal.of(10).divide(Decimal.of(12345678)).doubleValue() == 8.100000664200054E-7;
        assert Decimal.of(10).divide(Decimal.of(0.1)).doubleValue() == 100;
        assert Decimal.of(10).divide(Decimal.of(0.00002)).intValue() == 500000;
        assert Decimal.of(10).divide(Decimal.of(0.1234567)).doubleValue() == 81.00005913004316;
    }

    @Test
    void remainder() {
        assert Decimal.of(10).remainder(Decimal.of(3)).doubleValue() == 1;
        assert Decimal.of(10).remainder(Decimal.of(345)).doubleValue() == 0;
        assert Decimal.of(10).remainder(Decimal.of(0.1)).doubleValue() == 0.1;
        assert Decimal.of(10).remainder(Decimal.of(0.00002)).intValue() == 2.0e-5;
    }

    @Test
    void pow() {
        assert Decimal.of(10).pow(2).doubleValue() == 100;
        assert Decimal.of(10).pow(-2).doubleValue() == 0.01;
        assert Decimal.of(10).pow(2.5).doubleValue() == 316.22776601683796;
        assert Decimal.of(10).pow(-2.5).doubleValue() == 0.003162277660168379;
    }

    @Test
    void decuple() {
        assert Decimal.of(10).decuple(2).doubleValue() == 1000;
        assert Decimal.of(10).decuple(-2).doubleValue() == 0.1;
    }

    @Test
    void sqrt() {
        assert Decimal.of(100).sqrt().doubleValue() == 10;
        assert Decimal.of(3).sqrt().doubleValue() == 1.73205080756887729352;
    }

    @Test
    void abs() {
        assert Decimal.of(100).abs().doubleValue() == 100;
        assert Decimal.of(-100).abs().doubleValue() == 100;
    }

    @Test
    void negate() {
        assert Decimal.of(100).negate().doubleValue() == -100;
        assert Decimal.of(-100).negate().doubleValue() == 100;
    }

    @Test
    void scale() {
        assert Decimal.of("10.1").scale(1).doubleValue() == 10.1;
        assert Decimal.of(1).scale(2).doubleValue() == 1;

        assert Decimal.of("1.234").scale(1).doubleValue() == 1.2;
        assert Decimal.of("1.234").scale(2).doubleValue() == 1.23;
        assert Decimal.of("0.05").scale(1).doubleValue() == 0.1;

        assert Decimal.of(21.234).scale(-1).doubleValue() == 20;
        assert Decimal.of(321.234).scale(-2).doubleValue() == 300;
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
        assert Decimal.of(1).isZero() == false;
        assert Decimal.of(0).isZero();
    }

    @Test
    void isNotZero() {
        assert Decimal.of(1).isNotZero();
        assert Decimal.of(0).isNotZero() == false;
    }

    @Test
    void isPositive() {
        assert Decimal.of(1).isPositive() == true;
        assert Decimal.of(0).isPositive() == false;
        assert Decimal.of(-1).isPositive() == false;
    }

    @Test
    void isPositiveOrZero() {
        assert Decimal.of(1).isPositiveOrZero() == true;
        assert Decimal.of(0).isPositiveOrZero() == true;
        assert Decimal.of(-1).isPositiveOrZero() == false;
    }

    @Test
    void isNegative() {
        assert Decimal.of(1).isNegative() == false;
        assert Decimal.of(0).isNegative() == false;
        assert Decimal.of(-1).isNegative() == true;
    }

    @Test
    void isNegativeOrZero() {
        assert Decimal.of(1).isNegativeOrZero() == false;
        assert Decimal.of(0).isNegativeOrZero() == true;
        assert Decimal.of(-1).isNegativeOrZero() == true;
    }
}
