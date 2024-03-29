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

import static hypatia.Num.*;

import java.math.RoundingMode;

import org.junit.jupiter.api.Test;

class NumTest {

    @Test
    void parseString() {
        for (int i = 1; i <= Num.CONTEXT.getPrecision(); i++) {
            String value = "1".repeat(i);
            assert Num.of(value).toString().equals(value);
        }
    }

    @Test
    void parseStringDecimal() {
        assert Num.of("0.1234").is(0.1234);
        assert Num.of("0.00123").is(0.00123);
        assert Num.of("1.234").is(1.234);
        assert Num.of("12.34").is(12.34);
        assert Num.of("100.004").is(100.004);
        assert Num.of("1.000").is(1);
        assert Num.of("100.000").is(100);
        assert Num.of("-0.00100").is(-0.001);
        assert Num.of("-12.0120").is(-12.012);
    }

    @Test
    void parseStringExponential() {
        assert Num.of("1.7E-7").is(0.00000017);
        assert Num.of("1.0E-4").is(0.0001);
        assert Num.of("0.12E-2").is(0.0012);
        assert Num.of("12.34e-5").is(0.0001234);
        assert Num.of("-12.34e-5").is(-0.0001234);
        assert Num.of("2e-5").is(0.00002);
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
        assert Num.max(Orientational.NEGATIVE, ONE).is(1);
        assert Num.max(Orientational.NEGATIVE, ONE, TWO, TEN).is(1);
        assert Num.max(Orientational.NEGATIVE, ONE, TWO, of(-10)).is(-10);
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
        assert Num.min(Orientational.NEGATIVE, ONE).is(1);
        assert Num.min(Orientational.NEGATIVE, ONE, TWO, TEN).is(10);
        assert Num.min(Orientational.NEGATIVE, ONE, TWO, of(-10)).is(2);
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