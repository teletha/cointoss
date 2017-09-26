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

import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.beans.property.ReadOnlyBooleanWrapper;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

/**
 * @version 2017/09/26 1:03:28
 */
public class LineChartData {

    public final StringProperty nameProperty = new SimpleStringProperty(this, "name", "");

    private static final double[] EMPTY_ARRAY = {};

    int defaultColorIndex;

    String defaultColor;

    private double[] x = EMPTY_ARRAY, y = EMPTY_ARRAY;

    private int length = 0;

    /**
     * @param capacity
     */
    public LineChartData(int capacity) {
        x = new double[capacity];
        y = new double[capacity];
    }

    public void addData(final double x, final double y) {
        final int t = length;
        length++;
        if (this.x.length < length) {
            final int size = this.x.length + 16;
            final double[] newx = new double[size];
            final double[] newy = new double[size];
            System.arraycopy(this.x, 0, newx, 0, t);
            System.arraycopy(this.y, 0, newy, 0, t);
            this.x = newx;
            this.y = newy;
        }

        this.x[t] = x;
        this.y[t] = y;
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
        return x[index];
    }

    public double getY(final int index) throws ArrayIndexOutOfBoundsException {
        if (index < 0 || index >= length) {
            throw new ArrayIndexOutOfBoundsException(index);
        }
        setValidate(true);
        return y[index];
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
            return findMinIndex(x, length, value);
        } else {
            return findMaxIndex(x, length, value);
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
            return findMinIndex(y, length, value);
        } else {
            return findMaxIndex(y, length, value);
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
    private static int findMaxIndex(final double[] a, final int size, final double v) {
        if (size < 2 || a[0] >= v) {
            return 0;
        }
        if (a[size - 1] <= v) {
            return size - 1;
        }
        if (size == 2) {
            return 0;
        }

        int l = 1, r = size - 2, m = (l + r) >> 1;

        while (r - l > 1) {
            final double d = a[m];
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

        if (a[l] > v) {
            return l - 1;
        }
        if (a[r] <= v) {
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
    private static int findMinIndex(final double[] a, final int size, final double v) {
        if (size < 2 || a[0] >= v) {
            return 0;
        }
        if (a[size - 1] <= v) {
            return size - 1;
        }
        if (size == 2) {
            return 1;
        }
        int l = 1, r = size - 2, m = (l + r) >> 1;

        while (r - l > 1) {
            final double d = a[m];
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
        if (a[l] >= v) {
            return l;
        }
        if (a[r] < v) {
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
        return findMinMaxValue(x, startIndex, endIndex, ignoreInfinit);
    }

    private static double[] findMinMaxValue(final double[] a, int l, final int r, final boolean ignoreInfinit) {
        double min = a[l];
        double max = a[l];

        if (l == r) {
            if (ignoreInfinit && Double.isInfinite(min)) {
                return new double[] {Double.NaN, Double.NaN};
            } else {
                return new double[] {min, max};
            }
        }

        while (min != min || (ignoreInfinit && Double.isInfinite(min))) {
            min = max = a[l++];
            if (l > r) {
                return new double[] {Double.NaN, Double.NaN};
            }
        }

        for (; l <= r; l++) {
            final double dd = a[l];
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
