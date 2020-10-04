/*
 * Copyright (C) 2020 Nameless Production Committee
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          http://opensource.org/licenses/mit-license.php
 */
package trademate.chart;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.function.DoubleUnaryOperator;

import org.apache.commons.math3.stat.regression.SimpleRegression;

import cointoss.util.ring.RingBuffer;

public class TrendLine {

    private final boolean up;

    private final int size;

    private final RingBuffer<double[]> data;

    private int currentInterval = 0;

    /**
     * @param up
     * @param size
     */
    public TrendLine(boolean up, int size) {
        this.up = up;
        this.size = size;
        this.data = new RingBuffer(size);
    }

    public DoubleUnaryOperator add(double x, double y) {
        data.add(new double[] {x, y});

        if (++currentInterval != size) {
            return null;
        }
        currentInterval = 0;

        SimpleRegression regression = new SimpleRegression(true);
        List<double[]> items = new ArrayList();
        data.forEach(values -> {
            regression.addData(values[0], values[1]);
            items.add(values);
        });
        int last = 0;
        while (3 < regression.getN() && last != items.size()) {
            last = items.size();
            Iterator<double[]> iterator = items.iterator();
            while (iterator.hasNext()) {
                double[] item = iterator.next();
                double predicated = regression.predict(item[0]);
                if (up ? predicated < item[1] : item[1] < predicated) {
                    iterator.remove();
                    regression.removeData(item[0], item[1]);
                }
            }
        }
        return regression::predict;
    }
}
