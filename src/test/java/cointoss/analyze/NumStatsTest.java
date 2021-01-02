/*
 * Copyright (C) 2021 cointoss Development Team
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          https://opensource.org/licenses/MIT
 */
package cointoss.analyze;

import org.junit.jupiter.api.Test;

import com.google.common.math.DoubleMath;

import cointoss.util.arithmetic.Num;

class NumStatsTest {

    @Test
    void mean() {
        NumStats statistics = new NumStats();
        statistics.add(Num.of(10));
        assert statistics.mean().is(10);

        statistics.add(Num.of(20));
        assert statistics.mean().is(15);

        statistics.add(Num.of(30));
        assert statistics.mean().is(20);
    }

    @Test
    void total() {
        NumStats statistics = new NumStats();
        statistics.add(Num.of(10));
        assert statistics.total().is(10);

        statistics.add(Num.of(20));
        assert statistics.total().is(30);

        statistics.add(Num.of(30));
        assert statistics.total().is(60);
    }

    @Test
    void size() {
        NumStats statistics = new NumStats();
        statistics.add(Num.of(10));
        assert statistics.size() == 1;

        statistics.add(Num.of(20));
        assert statistics.size() == 2;

        statistics.add(Num.of(30));
        assert statistics.size() == 3;
    }

    @Test
    void min() {
        NumStats statistics = new NumStats();
        statistics.add(Num.of(10));
        assert statistics.min().is(10);

        statistics.add(Num.of(20));
        assert statistics.min().is(10);

        statistics.add(Num.of(5));
        assert statistics.min().is(5);
    }

    @Test
    void max() {
        NumStats statistics = new NumStats();
        statistics.add(Num.of(10));
        assert statistics.max().is(10);

        statistics.add(Num.of(20));
        assert statistics.max().is(20);

        statistics.add(Num.of(5));
        assert statistics.max().is(20);
    }

    @Test
    void variance() {
        NumStats statistics = new NumStats();
        statistics.add(Num.of(10));
        assert statistics.variance().is(0);

        statistics.add(Num.of(20));
        assert statistics.variance().is(25);

        statistics.add(Num.of(30));
        assert statistics.variance().is(66.66666666666667);
    }

    @Test
    void standardDeviation() {
        NumStats statistics = new NumStats();
        statistics.add(Num.of(10));
        assert statistics.standardDeviation().is(0);

        statistics.add(Num.of(20));
        assert statistics.standardDeviation().is(5);

        statistics.add(Num.of(30));
        assert DoubleMath.fuzzyEquals(statistics.standardDeviation().doubleValue(), 8.16496580927, 0.0000000001);
    }

    @Test
    void skewness() {
        NumStats statistics = new NumStats();
        statistics.add(Num.of(10));
        assert statistics.skewness().is(0);

        statistics.add(Num.of(20));
        assert statistics.skewness().is(0);

        statistics.add(Num.of(10));
        assert DoubleMath.fuzzyEquals(statistics.skewness().doubleValue(), 0.70710678, 0.0000001);
    }

    @Test
    void kurtosis() {
        NumStats statistics = new NumStats();
        statistics.add(Num.of(10));
        assert statistics.kurtosis().is(0);

        statistics.add(Num.of(20));
        assert statistics.kurtosis().is(-2);

        statistics.add(Num.of(30));
        assert statistics.kurtosis().is(-1.5);

        statistics.add(Num.of(20));
        statistics.add(Num.of(20));
        statistics.add(Num.of(20));
        assert statistics.kurtosis().is(0);

        statistics.add(Num.of(20));
        statistics.add(Num.of(20));
        statistics.add(Num.of(20));
        assert statistics.kurtosis().is(1.5);
    }
}