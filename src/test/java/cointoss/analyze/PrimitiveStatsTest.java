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

class PrimitiveStatsTest {

    @Test
    void mean() {
        PrimitiveStats statistics = new PrimitiveStats();
        statistics.add(10d);
        assert statistics.mean() == 10d;

        statistics.add(20d);
        assert statistics.mean() == 15d;

        statistics.add(30d);
        assert statistics.mean() == 20d;
    }

    @Test
    void total() {
        PrimitiveStats statistics = new PrimitiveStats();
        statistics.add(10d);
        assert statistics.total() == 10d;

        statistics.add(20d);
        assert statistics.total() == 30d;

        statistics.add(30d);
        assert statistics.total() == 60d;
    }

    @Test
    void size() {
        PrimitiveStats statistics = new PrimitiveStats();
        statistics.add(10d);
        assert statistics.size() == 1;

        statistics.add(20d);
        assert statistics.size() == 2;

        statistics.add(30d);
        assert statistics.size() == 3;
    }

    @Test
    void min() {
        PrimitiveStats statistics = new PrimitiveStats();
        statistics.add(10d);
        assert statistics.min() == 10d;

        statistics.add(20d);
        assert statistics.min() == 10d;

        statistics.add(5d);
        assert statistics.min() == 5d;
    }

    @Test
    void max() {
        PrimitiveStats statistics = new PrimitiveStats();
        statistics.add(10d);
        assert statistics.max() == 10d;

        statistics.add(20d);
        assert statistics.max() == 20d;

        statistics.add(5d);
        assert statistics.max() == 20d;
    }

    @Test
    void variance() {
        PrimitiveStats statistics = new PrimitiveStats();
        statistics.add(10d);
        assert statistics.variance() == 0d;

        statistics.add(20d);
        assert statistics.variance() == 25d;

        statistics.add(30d);
        assert statistics.variance() == 66.66666666666667d;
    }

    @Test
    void standardDeviation() {
        PrimitiveStats statistics = new PrimitiveStats();
        statistics.add(10d);
        assert statistics.standardDeviation() == 0d;

        statistics.add(20d);
        assert statistics.standardDeviation() == 5d;

        statistics.add(30d);
        assert statistics.standardDeviation() == 8.16496580927726d;
    }

    @Test
    void skewness() {
        PrimitiveStats statistics = new PrimitiveStats();
        statistics.add(10d);
        assert statistics.skewness() == 0d;

        statistics.add(20d);
        assert statistics.skewness() == 0d;

        statistics.add(10d);
        assert statistics.skewness() == 0.7071067811865475d;
    }

    @Test
    void kurtosis() {
        PrimitiveStats statistics = new PrimitiveStats();
        statistics.add(10d);
        assert statistics.kurtosis() == 0d;

        statistics.add(20d);
        assert statistics.kurtosis() == -2d;

        statistics.add(30d);
        assert statistics.kurtosis() == -1.5d;

        statistics.add(20d);
        statistics.add(20d);
        statistics.add(20d);
        assert statistics.kurtosis() == 0d;

        statistics.add(20d);
        statistics.add(20d);
        statistics.add(20d);
        assert statistics.kurtosis() == 1.5d;
    }
}