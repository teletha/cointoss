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

import java.util.Arrays;

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
     * 
     */
    public LineChartData() {
    }

    /**
     * @param capacity
     */
    public LineChartData(int capacity) {
        x = new double[capacity];
        y = new double[capacity];
    }

    public void clear() {
        length = 0;
        setValidate(false);
    }

    public void shurink() {
        if (x.length != length) {
            if (length == 0) {
                x = EMPTY_ARRAY;
                y = EMPTY_ARRAY;
                return;
            }
            x = Arrays.copyOf(x, length);
            y = Arrays.copyOf(y, length);
        }
    }

    public void setCapacity(final int length) {
        if (x.length < length) {
            final double[] a = new double[length], b = new double[length];
            System.arraycopy(x, 0, a, 0, this.length);
            System.arraycopy(y, 0, b, 0, this.length);
            x = a;
            y = b;
        }
    }

    public void setData(final double[] x, final double[] y, final int offset, final int length) {
        final int min = Math.min(length, Math.min(x.length, y.length) - offset);
        if (this.x.length < min) {
            this.x = new double[min];
            this.y = new double[min];
        }

        System.arraycopy(x, offset, this.x, 0, min);
        System.arraycopy(y, offset, this.y, 0, min);
        this.length = min;
        setValidate(false);
    }

    public void setData(final double[] x, final double[] y) {
        setData(x, y, 0, Integer.MAX_VALUE);
    }

    public void setData(final double[] x, final double[] y, final int length) {
        setData(x, y, 0, length);
    }

    public void addData(final double[] x, final double[] y, final int offset, final int length) {
        final int min = Math.min(Math.min(x.length, y.length) - offset, length);
        final int oldLen = length;
        final int newlen = oldLen + min;
        if (this.x.length < newlen) {
            final double[] newx = new double[newlen];
            final double[] newy = new double[newlen];
            System.arraycopy(this.x, 0, newx, 0, oldLen);
            System.arraycopy(this.y, 0, newy, 0, oldLen);
            this.x = newx;
            this.y = newy;
        }
        System.arraycopy(x, offset, this.x, oldLen, min);
        System.arraycopy(y, offset, this.y, oldLen, min);
        this.length = newlen;
        setValidate(false);
    }

    public void addData(final double[] x, final double[] y) {
        addData(x, y, 0, Integer.MAX_VALUE);
    }

    public void addData(final double[] x, final double[] y, final int length) {
        addData(x, y, 0, length);
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

    public void addData(final double x, final double y, final int index) {
        if (index < 0 || index > length) {
            throw new ArrayIndexOutOfBoundsException(index);
        }
        if (index == length) {
            addData(x, y);
            return;
        }

        final int oldLen = length;
        length++;
        if (this.x.length < length) {
            final int size = this.x.length + 16;
            final double[] newx = new double[size];
            final double[] newy = new double[size];
            System.arraycopy(this.x, 0, newx, 0, index);
            System.arraycopy(this.y, 0, newy, 0, index);
            System.arraycopy(this.x, index, newx, index + 1, oldLen - index);
            System.arraycopy(this.y, index, newy, index + 1, oldLen - index);
            this.x = newx;
            this.y = newy;
        } else {
            System.arraycopy(this.x, index, this.x, index + 1, oldLen - index);
            System.arraycopy(this.y, index, this.y, index + 1, oldLen - index);
        }
        this.x[index] = x;
        this.y[index] = y;
        setValidate(false);
    }

    public void setData(final double x, final double y, final int index) {
        if (index < 0 || index >= length) {
            throw new ArrayIndexOutOfBoundsException(index);
        }
        if (this.x[index] == x && this.y[index] == y) {
            return;
        }
        this.x[index] = x;
        this.y[index] = y;
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

    public double[] toArrayX(final double[] arr) {
        setValidate(true);
        if (arr == null || arr.length < length) {
            return Arrays.copyOf(x, length);
        }

        System.arraycopy(x, 0, arr, 0, length);
        return arr;
    }

    public double[] toArrayY(final double[] arr) {
        setValidate(true);
        if (arr == null || arr.length < length) {
            return Arrays.copyOf(y, length);
        }

        System.arraycopy(y, 0, arr, 0, length);
        return arr;
    }

    /**
     * x座標のデータを基準にしてソートします。 データにNaNがないことが前提条件です。
     */
    public void sortByX() {
        if (length < 2) {
            return;
        }
        qsort(x, y, length);
        setValidate(false);
    }

    /**
     * y座標のデータを基準にしてソートします。 データにNaNがないことが前提条件です。
     */
    public void sortByY() {
        if (length < 2) {
            return;
        }
        qsort(y, x, length);
        setValidate(false);
    }

    private static void qsort(final double[] data, final double[] subarray, final int length) {
        qsort(data, 0, length - 1, subarray);
    }

    private static void qsort(final double[] data, final int left, final int right, final double[] subarray) {
        int j = left;
        double temp;
        for (int i = left + 1; i <= right; i++) {
            if (data[i] < data[left]) {
                j++;
                temp = data[j];
                data[j] = data[i];
                data[i] = temp;
                temp = subarray[j];
                subarray[j] = subarray[i];
                subarray[i] = temp;
            }
        }

        temp = data[left];
        data[left] = data[j];
        data[j] = temp;
        temp = subarray[left];
        subarray[left] = subarray[j];
        subarray[j] = temp;

        if (left < j - 1) {
            qsort(data, left, j - 1, subarray);
        }

        if (j + 1 < right) {
            qsort(data, j + 1, right, subarray);
        }

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
     * xの最大値を探します
     * 
     * @param ignoreInfinit 無限を無視するかどうか
     * @return
     */
    public double getMaxX(final boolean ignoreInfinit) {
        return getMaxX(0, length - 1, ignoreInfinit);
    }

    /**
     * startIndex ～endIndexの範囲（両端含む）の中で最大のxを探します
     * 
     * @param startIndex
     * @param endIndex endIndexも検索に含みます
     * @param ignoreInfinit 無限を無視するかどうか
     * @return
     */
    public double getMaxX(int startIndex, int endIndex, final boolean ignoreInfinit) {
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
        return findMaxValue(x, startIndex, endIndex, ignoreInfinit);
    }

    /**
     * xの最小値を探します
     * 
     * @param ignoreInfinit 無限を無視するかどうか
     * @return
     */
    public double getMinX(final boolean ignoreInfinit) {
        return getMinX(0, length - 1, ignoreInfinit);
    }

    /**
     * startIndex ～endIndexの範囲（両端含む）の中で最小のxを探します
     * 
     * @param startIndex
     * @param endIndex endIndexも検索に含みます
     * @param ignoreInfinit 無限を無視するかどうか
     * @return
     */
    public double getMinX(int startIndex, int endIndex, final boolean ignoreInfinit) {
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
        return findMinValue(x, startIndex, endIndex, ignoreInfinit);
    }

    /**
     * xの最小値、最大値を探します
     * 
     * @param ignoreInfinit 無限を無視するかどうか
     * @return {最小値,最大値}の配列
     */
    public double[] getMinMaxX(final boolean ignoreInfinit) {
        return getMinMaxX(0, length - 1, ignoreInfinit);
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

    /**
     * yの最大値を探します
     * 
     * @param ignoreInfinit 無限を無視するかどうか
     * @return
     */
    public double getMaxY(final boolean ignoreInfinit) {
        return getMaxY(0, length - 1, ignoreInfinit);
    }

    /**
     * startIndex ～endIndexの範囲（両端含む）の中で最大のyを探します
     * 
     * @param startIndex
     * @param endIndex endIndexも検索に含みます
     * @param ignoreInfinit 無限を無視するかどうか
     * @return
     */
    public double getMaxY(int startIndex, int endIndex, final boolean ignoreInfinit) {
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
        return findMaxValue(y, startIndex, endIndex, ignoreInfinit);
    }

    /**
     * yの最小値を探します
     * 
     * @param ignoreInfinit 無限を無視するかどうか
     * @return
     */
    public double getMinY(final boolean ignoreInfinit) {
        return getMinY(0, length - 1, ignoreInfinit);
    }

    /**
     * startIndex ～endIndexの範囲（両端含む）の中で最小のyを探します
     * 
     * @param startIndex
     * @param endIndex endIndexも検索に含みます
     * @param ignoreInfinit 無限を無視するかどうか
     * @return
     */
    public double getMinY(int startIndex, int endIndex, final boolean ignoreInfinit) {
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
        return findMinValue(y, startIndex, endIndex, ignoreInfinit);
    }

    /**
     * yの最小値、最大値を探します
     * 
     * @param ignoreInfinit 無限を無視するかどうか
     * @return {最小値,最大値}の配列
     */
    public double[] getMinMaxY(final boolean ignoreInfinit) {
        return getMinMaxY(0, length - 1, ignoreInfinit);
    }

    /**
     * startIndex ～endIndexの範囲（両端含む）の中で最小、最大のyを探します
     * 
     * @param startIndex
     * @param endIndex endIndexも検索に含みます
     * @param ignoreInfinit 無限を無視するかどうか
     * @return {最小値,最大値}の配列
     */
    public double[] getMinMaxY(int startIndex, int endIndex, final boolean ignoreInfinit) {
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
        return findMinMaxValue(y, startIndex, endIndex, ignoreInfinit);
    }

    private static double findMaxValue(final double[] a, int l, final int r, final boolean ignoreInfinit) {
        double d = a[l];
        if (l == r) {
            if (ignoreInfinit && Double.isInfinite(d)) {
                return Double.NaN;
            } else {
                return d;
            }
        }

        while (d != d) {
            d = a[l++];
            if (l > r) {
                return Double.NaN;
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
            d = max(d, dd);
        }
        return d;
    }

    private static double findMinValue(final double[] a, int l, final int r, final boolean ignoreInfinit) {
        double d = a[l];
        if (l == r) {
            if (ignoreInfinit && Double.isInfinite(d)) {
                return Double.NaN;
            } else {
                return d;
            }
        }

        while (d != d) {
            d = a[l++];
            if (l > r) {
                return Double.NaN;
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
            d = min(d, dd);
        }
        return d;
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

    public boolean isValidate() {
        return validateWrapper == null ? false : validateWrapper.get();
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
