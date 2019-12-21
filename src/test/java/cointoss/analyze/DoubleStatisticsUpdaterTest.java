/*
 * Copyright (C) 2019 Nameless Production Committee
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          http://opensource.org/licenses/mit-license.php
 */
package cointoss.analyze;

import org.junit.jupiter.api.Test;

public class DoubleStatisticsUpdaterTest {

    @Test
    public void add() {
        final DoubleStatisticsUpdater updater = new DoubleStatisticsUpdater();
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
    public void remove() {
        final DoubleStatisticsUpdater updater = new DoubleStatisticsUpdater();
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
        final DoubleStatisticsUpdater updater = new DoubleStatisticsUpdater();
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
        final DoubleStatisticsUpdater updater = new DoubleStatisticsUpdater();
        updater.add(1);
        updater.add(2);
        updater.reset();
    
        assert updater.getCount() == 0;
        assert updater.getMean() == 0;
        assert updater.getSum() == 0;
    }
}