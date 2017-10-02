/*
 * Copyright (C) 2017 Nameless Production Committee
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          http://opensource.org/licenses/mit-license.php
 */
package cointoss.visual;

import java.util.Objects;
import java.util.function.ToDoubleFunction;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

import cointoss.chart.Chart;
import cointoss.chart.Tick;
import cointoss.util.RingBuffer;

/**
 * @version 2017/09/27 21:41:30
 */
public class CandleChartData {

    public final StringProperty name = new SimpleStringProperty(this, "name", "");

    int defaultColorIndex;

    String defaultColor;

    private Chart chart;

    /**
     * @param capacity
     */
    public CandleChartData(Chart chart) {
        this.chart = Objects.requireNonNull(chart);
    }

    public int size() {
        return chart.ticks.size();
    }

    public double getX(final int index) throws ArrayIndexOutOfBoundsException {
        if (index < 0 || index >= chart.ticks.size()) {
            throw new ArrayIndexOutOfBoundsException(index);
        }
        return chart.getTick(index).start.toInstant().toEpochMilli();
    }

    public double getY(final int index) throws ArrayIndexOutOfBoundsException {
        if (index < 0 || index >= chart.ticks.size()) {
            throw new ArrayIndexOutOfBoundsException(index);
        }
        return chart.getTick(index).getWeightMedian().toDouble();
    }

    /**
     * <b>このデータがxでソートされているときに限り</b>valueが出てくるインデックスを検索します。
     * minModeにより、valueを越えない最大インデックスか、valueより大きな最小インデックスを探すかを変えられます。<br>
     * このデータがソートされていないときの挙動は保証しません
     * 
     * @param value
     * @param minMode trueの時、value「より小さくならない最小の」インデックスを検索する。<br>
     *            falseの時、value「を越えない最大の」インデックスを検索する
     * @return
     */
    public int searchXIndex(final double value, final boolean minMode) {
        if (minMode) {
            return findMinIndex(chart.ticks, chart.ticks.size(), value, i -> i.start.toInstant().toEpochMilli());
        } else {
            return findMaxIndex(chart.ticks, chart.ticks.size(), value, i -> i.start.toInstant().toEpochMilli());
        }
    }

    /**
     * ※aが昇順に整列されているときに限る。 vを越えない最大のaの場所を探索する
     * 
     * @param a
     * @param size
     * @param v
     * @return
     */
    private static int findMaxIndex(final RingBuffer<Tick> a, final int size, final double v, ToDoubleFunction<Tick> converter) {
        if (size < 2) {
            return 0;
        }
        if (size == 2) {
            return 0;
        }

        int l = 1, r = size - 2, m = (l + r) >> 1;

        while (r - l > 1) {
            System.out.println(m + "  " + a.start() + "  " + a.end() + " " + a.size());
            final double d = converter.applyAsDouble(a.get(m));
            if (d == v) {
                return m;
            }
            if (d < v) {
                l = m;
            } else {
                r = m;
            }
            m = (l + r) >> 1;
        }

        if (converter.applyAsDouble(a.get(l)) > v) {
            return l - 1;
        }
        if (converter.applyAsDouble(a.get(r)) <= v) {
            return r;
        }
        return l;
    }

    /**
     * ※aが昇順に整列されているときに限る。 vより小さくならない最小のaの場所を探索する
     * 
     * @param a
     * @param size
     * @param v
     * @return
     */
    private static int findMinIndex(final RingBuffer<Tick> a, final int size, final double v, ToDoubleFunction<Tick> converter) {
        if (size < 2) {
            return 0;
        }
        if (size == 2) {
            return 1;
        }
        int l = 1, r = size - 2, m = (l + r) >> 1;

        while (r - l > 1) {
            final double d = converter.applyAsDouble(a.get(m));
            if (d == v) {
                return m;
            }
            if (d < v) {
                l = m;
            } else {
                r = m;
            }
            m = (l + r) >> 1;
        }
        if (converter.applyAsDouble(a.get(l)) >= v) {
            return l;
        }
        if (converter.applyAsDouble(a.get(r)) < v) {
            return r + 1;
        }
        return r;
    }
}
