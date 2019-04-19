/*
 * Copyright (C) 2019 Nameless Production Committee
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          http://opensource.org/licenses/mit-license.php
 */
package cointoss.verify;

import org.junit.jupiter.api.Test;

import cointoss.util.Num;

class StatisticsTest {

    @Test
    void mean() {
        Statistics statistics = new Statistics();
        statistics.add(Num.of(10));
        assert statistics.mean().is(10);

        statistics.add(Num.of(20));
        assert statistics.mean().is(15);

        statistics.add(Num.of(30));
        assert statistics.mean().is(20);
    }

    @Test
    void total() {
        Statistics statistics = new Statistics();
        statistics.add(Num.of(10));
        assert statistics.total().is(10);

        statistics.add(Num.of(20));
        assert statistics.total().is(30);

        statistics.add(Num.of(30));
        assert statistics.total().is(60);
    }

    @Test
    void size() {
        Statistics statistics = new Statistics();
        statistics.add(Num.of(10));
        assert statistics.size() == 1;

        statistics.add(Num.of(20));
        assert statistics.size() == 2;

        statistics.add(Num.of(30));
        assert statistics.size() == 3;
    }

    @Test
    void min() {
        Statistics statistics = new Statistics();
        statistics.add(Num.of(10));
        assert statistics.min().is(10);

        statistics.add(Num.of(20));
        assert statistics.min().is(10);

        statistics.add(Num.of(5));
        assert statistics.min().is(5);
    }

    @Test
    void max() {
        Statistics statistics = new Statistics();
        statistics.add(Num.of(10));
        assert statistics.max().is(10);

        statistics.add(Num.of(20));
        assert statistics.max().is(20);

        statistics.add(Num.of(5));
        assert statistics.max().is(20);
    }

    @Test
    void variance() {
        Statistics statistics = new Statistics();
        statistics.add(Num.of(10));
        assert statistics.variance().is(0);

        statistics.add(Num.of(20));
        assert statistics.variance().is(25);

        statistics.add(Num.of(30));
        assert statistics.variance().is(66.6666666666667);
    }

    @Test
    void standardDeviation() {
        Statistics statistics = new Statistics();
        statistics.add(Num.of(10));
        assert statistics.standardDeviation().is(0);

        statistics.add(Num.of(20));
        assert statistics.standardDeviation().is(5);

        statistics.add(Num.of(30));
        assert statistics.standardDeviation().is(8.16496580927726);
    }

    @Test
    void skewness() {
        Statistics statistics = new Statistics();
        statistics.add(Num.of(10));
        assert statistics.skewness().is(0);

        statistics.add(Num.of(20));
        assert statistics.skewness().is(0);

        statistics.add(Num.of(10));
        assert statistics.skewness().is(0.707106782);
    }

    @Test
    void kurtosis() {
        Statistics statistics = new Statistics();
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
