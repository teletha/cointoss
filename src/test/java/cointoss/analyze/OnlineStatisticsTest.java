/*
 * Copyright (C) 2020 cointoss Development Team
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          https://opensource.org/licenses/MIT
 */
package cointoss.analyze;

import org.junit.jupiter.api.Test;

public class OnlineStatisticsTest {

    @Test
    public void add1() {
        final OnlineStatistics updater = new OnlineStatistics();
        updater.add(1);
        updater.add(2);
        updater.add(3);
        updater.add(6);

        assert updater.getCount() == 4;
        assert updater.getSum() == 12;
        assert updater.getMean() == 3;
        assert updater.getVariance() == 3.5;
        assert updater.getVarianceUnbiased() == 4.666666666666667;
        assert updater.getStdDev() == 1.8708286933869707;
        assert updater.getStdDevUnbiased() == 2.160246899469287;
    }

    @Test
    public void add2() {
        final OnlineStatistics updater = new OnlineStatistics();
        updater.add(1);
        updater.add(2);
        updater.add(3);
        updater.add(4);
        updater.add(5);
        updater.add(6);

        assert updater.getCount() == 6;
        assert updater.getSum() == 21;
        assert updater.getMean() == 3.5;
        assert updater.getVariance() == 2.9166666666666665;
        assert updater.getVarianceUnbiased() == 3.5;
        assert updater.getStdDev() == 1.707825127659933;
        assert updater.getStdDevUnbiased() == 1.8708286933869707;
    }

    @Test
    public void remove() {
        final OnlineStatistics updater = new OnlineStatistics();
        updater.add(1);
        updater.add(2);
        updater.add(3);
        updater.add(4);
        updater.add(5);
        updater.add(6);
        updater.remove(4);
        updater.remove(5);

        assert updater.getCount() == 4;
        assert updater.getSum() == 12;
        assert updater.getMean() == 3;
        assert updater.getVariance() == 3.5;
        assert updater.getVarianceUnbiased() == 4.666666666666667;
        assert updater.getStdDev() == 1.8708286933869707;
        assert updater.getStdDevUnbiased() == 2.160246899469287;
    }

    @Test
    public void replace() {
        final OnlineStatistics updater = new OnlineStatistics();
        updater.add(1);
        updater.add(2);
        updater.add(3);
        updater.add(4);
        updater.replace(4, 6);

        assert updater.getCount() == 4;
        assert updater.getSum() == 12;
        assert updater.getMean() == 3;
        assert updater.getVariance() == 3.5;
        assert updater.getVarianceUnbiased() == 4.666666666666667;
        assert updater.getStdDev() == 1.8708286933869707;
        assert updater.getStdDevUnbiased() == 2.160246899469287;
    }

    @Test
    public void reset() {
        final OnlineStatistics updater = new OnlineStatistics();
        updater.add(1);
        updater.add(2);
        updater.reset();

        assert updater.getCount() == 0;
        assert updater.getMean() == 0;
        assert updater.getSum() == 0;
    }
}