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

import org.junit.jupiter.api.Test;

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
    void multiply() {
        assert Decimal.of(10).multiply(Decimal.of(20)).doubleValue() == 200;
        assert Decimal.of(10).multiply(Decimal.of(12345678)).doubleValue() == 123456780;
        assert Decimal.of(10).multiply(Decimal.of(0.1)).doubleValue() == 1;
        assert Decimal.of(10).multiply(Decimal.of("0.00002")).doubleValue() == 0.0002;
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
}
