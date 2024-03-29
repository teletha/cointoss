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

import org.junit.jupiter.api.Test;

import hypatia.Primitives;

class PrimitivesTest {

    @Test
    void max() {
        assert Primitives.max(0, 1, 2) == 2;
        assert Primitives.max(0) == 0;
        assert Primitives.max(1, 1, 1) == 1;
        assert Primitives.max(-1, -2, -3) == -1;
    }

    @Test
    void maxLong() {
        assert Primitives.max(0L, 1, 2) == 2;
        assert Primitives.max(0L) == 0;
        assert Primitives.max(1L, 1, 1) == 1;
        assert Primitives.max(-1L, -2, -3) == -1;
    }

    @Test
    void min() {
        assert Primitives.min(0, 1, 2) == 0;
        assert Primitives.min(0) == 0;
        assert Primitives.min(1, 1, 1) == 1;
        assert Primitives.min(-1, -2, -3) == -3;
    }

    @Test
    void minLong() {
        assert Primitives.min(0L, 1, 2) == 0;
        assert Primitives.min(0L) == 0;
        assert Primitives.min(1L, 1, 1) == 1;
        assert Primitives.min(-1L, -2, -3) == -3;
    }

    @Test
    void ratio() {
        assert Primitives.ratio(10, 20) == 0.5;
        assert Primitives.ratio(30, 100) == 0.3;
        assert Primitives.ratio(33.34, 100) == 0.333;
        assert Primitives.ratio(33.35, 100) == 0.334;
        assert Primitives.ratio(33.3444444445, 100) == 0.333;

        assert Primitives.ratio(0, 100) == 0;
        assert Primitives.ratio(0, 0) == 0;
        assert Primitives.ratio(100, 100) == 1;
        assert Primitives.ratio(100, 0) == 0;
    }

    @Test
    void percent() {
        assert Primitives.percent(10, 20).equals("50%");
        assert Primitives.percent(30, 100).equals("30%");
        assert Primitives.percent(33.34, 100).equals("33.3%");

        assert Primitives.percent(0, 100).equals("0%");
        assert Primitives.percent(0, 0).equals("0%");
        assert Primitives.percent(100, 100).equals("100%");
        assert Primitives.percent(100, 0).equals("0%");
    }

    @Test
    void round() {
        assert Primitives.round(0, 10, 20) == 10;
        assert Primitives.round(0, -10, 20) == 0;
        assert Primitives.round(0, 30, 20) == 20;
    }

    @Test
    void roundLong() {
        assert Primitives.round(0L, 10, 20) == 10;
        assert Primitives.round(0L, -10, 20) == 0;
        assert Primitives.round(0L, 30, 20) == 20;
    }

    @Test
    void roundDouble() {
        assert Primitives.round(0d, 10, 20) == 10;
        assert Primitives.round(0d, -10, 20) == 0;
        assert Primitives.round(0d, 30, 20) == 20;
    }

    @Test
    void roundString() {
        assert Primitives.roundString(0.0d, 0).equals("0");
        assert Primitives.roundString(0.0d, 1).equals("0");
        assert Primitives.roundString(0.00008, 5).equals("0.00008");
        assert Primitives.roundString(123456789.00008, 15).equals("123456789.00008");
    }

    @Test
    void within() {
        assert Primitives.within(0, 10, 20) == true;
        assert Primitives.within(0, -10, 20) == false;
        assert Primitives.within(0, 30, 20) == false;
    }

    @Test
    void withinLong() {
        assert Primitives.within(0L, 10, 20) == true;
        assert Primitives.within(0L, -10, 20) == false;
        assert Primitives.within(0L, 30, 20) == false;
    }

    @Test
    void withinDouble() {
        assert Primitives.within(0d, 10, 20) == true;
        assert Primitives.within(0d, -10, 20) == false;
        assert Primitives.within(0d, 30, 20) == false;
    }
}