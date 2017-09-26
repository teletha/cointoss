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

import static java.lang.Math.*;

import java.util.function.ToDoubleFunction;

import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.beans.property.ReadOnlyBooleanWrapper;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

import cointoss.chart.Tick;

/**
 * @version 2017/09/26 1:03:28
 */
public class LineChartData {

    public final StringProperty nameProperty = new SimpleStringProperty(this, "name", "");

    int defaultColorIndex;

    String defaultColor;

    private Tick[] ticks = {};

    private int length = 0;

    /**
     * @param capacity
     */
    public LineChartData(int capacity) {
        ticks = new Tick[capacity];
    }

    public void addData(final double x, final Tick y) {
        final int t = length;
        length++;
        if (this.ticks.length < length) {
            final int size = this.ticks.length + 16;
            final Tick[] newy = new Tick[size];
            System.arraycopy(this.ticks, 0, newy, 0, t);
            this.ticks = newy;
        }

        this.ticks[t] = y;
        setValidate(false);
    }

    public int size() {
        setValidate(true);
        return length;
    }

    public double getX(final int index) throws ArrayIndexOutOfBoundsException {
        if (index < 0 || index >= length) {
            throw new ArrayIndexOutOfBoundsException(index);
        }
        setValidate(true);
        return ticks[index].start.toInstant().toEpochMilli();
    }

    public double getY(final int index) throws ArrayIndexOutOfBoundsException {
        if (index < 0 || index >= length) {
            throw new ArrayIndexOutOfBoundsException(index);
        }
        setValidate(true);
        return ticks[index].getWeightMedian().toDouble();
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
        setValidate(true);
        if (minMode) {
            return findMinIndex(ticks, length, value, i -> i.start.toInstant().toEpochMilli());
        } else {
            return findMaxIndex(ticks, length, value, i -> i.start.toInstant().toEpochMilli());
        }
    }

    /**
     * <b>このデータがyでソートされているときに限り</b>valueが出てくるインデックスを検索します。
     * minModeにより、valueを越えない最大インデックスか、valueより大きな最小インデックスを探すかを変えられます。<br>
     * このデータがソートされていないときの挙動は保証しません
     * 
     * @param value
     * @param minMode trueの時、value「より小さくならない最小の」インデックスを検索する。<br>
     *            falseの時、value「を越えない最大の」インデックスを検索する
     * @return
     */
    public int searchYIndex(final double value, final boolean minMode) {
        setValidate(true);
        if (minMode) {
            return findMinIndex(ticks, length, value, t -> t.getWeightMedian().toDouble());
        } else {
            return findMaxIndex(ticks, length, value, t -> t.getWeightMedian().toDouble());
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
    private static int findMaxIndex(final Tick[] a, final int size, final double v, ToDoubleFunction<Tick> converter) {
        if (size < 2) {
            return 0;
        }
        if (size == 2) {
            return 0;
        }

        int l = 1, r = size - 2, m = (l + r) >> 1;

        while (r - l > 1) {
            final double d = converter.applyAsDouble(a[m]);
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

        if (converter.applyAsDouble(a[l]) > v) {
            return l - 1;
        }
        if (converter.applyAsDouble(a[r]) <= v) {
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
    private static int findMinIndex(final Tick[] a, final int size, final double v, ToDoubleFunction<Tick> converter) {
        if (size < 2) {
            return 0;
        }
        if (size == 2) {
            return 1;
        }
        int l = 1, r = size - 2, m = (l + r) >> 1;

        while (r - l > 1) {
            final double d = converter.applyAsDouble(a[m]);
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
        if (converter.applyAsDouble(a[l]) >= v) {
            return l;
        }
        if (converter.applyAsDouble(a[r]) < v) {
            return r + 1;
        }
        return r;
    }

    /**
     * startIndex ～endIndexの範囲（両端含む）の中で最小、最大のxを探します
     * 
     * @param startIndex
     * @param endIndex endIndexも検索に含みます
     * @param ignoreInfinit 無限を無視するかどうか
     * @return {最小値,最大値}の配列
     */
    public double[] getMinMaxX(int startIndex, int endIndex, final boolean ignoreInfinit) {
        if (startIndex > endIndex) {
            final int t = startIndex;
            startIndex = endIndex;
            endIndex = t;
        }
        if (startIndex < 0) {
            startIndex = 0;
        }
        if (endIndex >= length) {
            endIndex = length - 1;
        }
        setValidate(true);
        return findMinMaxValue(ticks, startIndex, endIndex, ignoreInfinit, t -> t.start.toInstant().toEpochMilli());
    }

    private static double[] findMinMaxValue(final Tick[] a, int l, final int r, final boolean ignoreInfinit, ToDoubleFunction<Tick> converter) {
        double min = converter.applyAsDouble(a[l]);
        double max = converter.applyAsDouble(a[l]);

        if (l == r) {
            if (ignoreInfinit && Double.isInfinite(min)) {
                return new double[] {Double.NaN, Double.NaN};
            } else {
                return new double[] {min, max};
            }
        }

        while (min != min || (ignoreInfinit && Double.isInfinite(min))) {
            min = max = converter.applyAsDouble(a[l++]);
            if (l > r) {
                return new double[] {Double.NaN, Double.NaN};
            }
        }

        for (; l <= r; l++) {
            final double dd = converter.applyAsDouble(a[l]);
            if (dd != dd) {
                continue;
            }
            if (ignoreInfinit && Double.isInfinite(dd)) {
                continue;
            }
            min = min(min, dd);
            max = max(max, dd);
        }
        return new double[] {min, max};
    }

    public String getName() {
        return nameProperty == null ? "" : nameProperty.get();
    }

    /**
     * Set plot name.
     * 
     * @param name
     */
    public LineChartData name(String name) {
        nameProperty.set(name);

        return this;
    }

    /**
     * データが正当かどうか。 get～や、clearなどを呼び出すとtrueになります。
     * 
     * @return
     */
    public ReadOnlyBooleanProperty validateProperty() {
        return validateWrapper().getReadOnlyProperty();
    }

    protected void setValidate(final boolean value) {
        validateWrapper().set(value);
    }

    protected ReadOnlyBooleanWrapper validateWrapper() {
        if (validateWrapper == null) {
            validateWrapper = new ReadOnlyBooleanWrapper(this, "validate", false);
        }
        return validateWrapper;
    }

    private ReadOnlyBooleanWrapper validateWrapper;

}
