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
import static java.lang.Math.min;
import static java.util.Collections.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import javafx.beans.property.DoubleProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.collections.ObservableList;
import javafx.scene.text.Text;

import org.eclipse.collections.api.block.function.primitive.DoubleToObjectFunction;

/**
 * @version 2017/09/26 1:03:05
 */
public class LinearAxis extends Axis {

    private final DoubleToObjectFunction<String> labelFormatter;

    /**
     * 
     */
    public LinearAxis(String name, DoubleToObjectFunction<String> labelFormatter) {
        this.labelFormatter = Objects.requireNonNull(labelFormatter);
        nameProperty.set(name);
    }

    private double lowVal = 0;

    private double m;

    @Override
    public double getDisplayPosition(final double v) {
        final double d = m * (v - lowVal);
        return isHorizontal() ? d : getHeight() - d;
    }

    @Override
    public double getValueForDisplay(double position) {
        if (!isHorizontal()) {
            position = getHeight() - position;
        }
        return position / m + lowVal;
    }

    private int unitIndex = -1;

    private double lastPUnitSize = Double.NaN;

    private List<Double> majours = new ArrayList<>(10), majoursU = unmodifiableList(majours), minors = new ArrayList<>(100),
            minorsU = unmodifiableList(minors);

    private List<Boolean> majoursFill = new ArrayList<>(10), majoursFillU = unmodifiableList(majoursFill);

    @Override
    public List<Double> getMajorTicks() {
        return majoursU;
    }

    @Override
    public List<Double> getMinorTicks() {
        return minorsU;
    }

    @Override
    public List<Boolean> getMajorTicksFill() {
        return majoursFillU;
    }

    @Override
    public void adjustLowerValue() {
        final double max = getMaxValue();
        final double a = getVisibleAmount();
        final double min = getMinValue();
        final double ll = max - min;
        double low = computeLowerValue(max);
        final double up = low + ll * a;
        if (up > max) {
            low = max - ll * a;
            lowerValue(low);
        }
    }

    private double computeUpperValue(final double low) {
        final double max = getMaxValue();
        final double a = getVisibleAmount();
        final double min = getMinValue();
        final double ll = max - min;
        return min(low + ll * a, max);
    }

    @Override
    protected void computeAxisProperties(final double width, final double height) {
        majours.clear();
        minors.clear();
        majoursFill.clear();

        final double low = computeLowerValue(getMaxValue());
        final double up = computeUpperValue(low);
        final double len = getAxisLength(width, height);
        if (low == up || low != low || up != up || len <= 0) {
            noData(width, height);
            return;
        }

        lowVal = low;
        setUpperValue(up);
        {// scroll bar
            final double max = getMaxValue();
            final double min = getMinValue();
            if (low == min && max == up) {
                setScrollBarValue(-1);
                setScrollVisibleAmount(1);
            } else {
                final double ll = max - min;
                final double l = up - low;
                setScrollBarValue((low - min) / (ll - l));
                setScrollVisibleAmount(l / ll);
            }
        }

        // 適当な単位を見つける
        ConstantDArray units = TICK_UNIT_DEFAULTS_ARRAY;
        final int unitsLength = units.length();

        final int mtn = getPrefferedMajorTickNumber();
        final double minu = getMinUnitLength();
        double pUnitLength = len / mtn;
        if (minu > 0 && pUnitLength < minu) {
            pUnitLength = minu;
        }
        final double pUnitSize = (up - low) / (len / pUnitLength);
        int uindex = unitIndex;// 前回の探索結果の再利用
        boolean useBefore = true;
        if (lastPUnitSize != pUnitSize) {
            if (pUnitSize <= units.get(0)) {
                uindex = 0;
            } else if (pUnitSize >= units.get(unitsLength - 1)) {
                uindex = unitsLength - 1;
            } else {
                BLOCK: {
                    int l = 1, r = unitsLength - 2;
                    int m = (l + r >> 1);

                    while (r - l > 1) {
                        final double d = units.get(m);
                        if (d == pUnitSize) {
                            uindex = m;
                            break BLOCK;
                        }
                        if (d < pUnitSize) {
                            l = m;
                        } else {
                            r = m;
                        }
                        m = (l + r >> 1);
                    }

                    if (units.get(r) < pUnitSize) {
                        uindex = r + 1;
                    } else if (units.get(l) > pUnitSize) {
                        uindex = l;
                    } else {
                        uindex = r;
                    }
                }
            }
            lastPUnitSize = pUnitSize;
            useBefore = uindex == unitIndex;
            unitIndex = uindex;
        }
        final double usize = units.get(uindex);

        final double l = up - low;
        boolean fill = ((int) floor(low / usize) & 1) != 0;
        final double basel = floor(low / usize) * usize;
        final double m = len / l;

        final double majorLength = m * usize;
        final int k = (int) (ceil((up - basel) / usize));
        if (k > 2000 || k <= 0) {
            noData(width, height);
            return;
        }
        this.m = m;

        double minorLength;
        int mcount = getPrefferedMinorCount();
        if (!isMinorTickVisible() || mcount <= 1) {
            minorLength = -1;
        } else {
            minorLength = majorLength / mcount;
            final double mins = getMinorUnitMinLength();
            if (mins > 0 && mins >= majorLength) {
                minorLength = -1;
            } else if (mins > 0 && minorLength < mins) {
                mcount = (int) floor(majorLength / mins);
                minorLength = majorLength / mcount;
            }
        }
        final boolean visibleMinor = minorLength != -1;
        // 単位の検索終わり
        final boolean isH = isHorizontal();

        final ObservableList<AxisLabel> labels = getLabels();
        if (!useBefore) {
            labels.clear();
        }

        ArrayList<AxisLabel> notUse = new ArrayList<>(labels);
        ArrayList<AxisLabel> labelList = new ArrayList<>(k + 1);

        for (int i = 0; i <= k + 1; i++) {
            double value = basel + usize * i;
            fill = !fill;
            if (value > up) {
                break;// i==k
            }
            double majorpos = m * (value - low);
            if (value >= low) {
                majours.add(floor(isH ? majorpos : height - majorpos));
                majoursFill.add(fill);
                boolean find = false;
                for (int t = 0, lsize = notUse.size(); t < lsize; t++) {
                    AxisLabel a = notUse.get(t);
                    if (a.match(value)) {
                        labelList.add(a);
                        notUse.remove(t);
                        find = true;
                        break;
                    }
                }
                if (!find) {
                    AxisLabel a = new AxisLabel();
                    a.setID(value);
                    a.setNode(new Text(labelFormatter.apply(value)));
                    labelList.add(a);
                }
            }
            if (visibleMinor) {
                for (int count = 1; count < mcount; count++) {
                    double minorpos = majorpos + count * minorLength;
                    if (minorpos < 0) {
                        continue;
                    }
                    if (minorpos >= len) {
                        break;
                    }
                    minors.add(floor(isH ? minorpos : height - minorpos));
                }
            }
        } // end for

        // これで大丈夫か？
        labels.removeAll(notUse);
        for (int i = 0, e = labelList.size(); i < e; i++) {
            AxisLabel axisLabel = labelList.get(i);

            if (!labels.contains(axisLabel)) {
                labels.add(i, axisLabel);
            }
        }
    }

    protected void noData(final double width, final double height) {
        lastPUnitSize = Double.NaN;
        unitIndex = -1;
        lowVal = 0;
        setUpperValue(1);
        final double len = getAxisLength(width, height);
        m = len;
        majours.add(0d);
        majours.add(getAxisLength(width, height));
        majoursFill.add(true);
        majoursFill.add(false);
        final ObservableList<AxisLabel> labels = getLabels();
        labels.clear();
        AxisLabel l = new AxisLabel();
        l.setID(Double.NaN);
        l.setNode(new Text("1"));
        labels.add(l);
        l = new AxisLabel();
        l.setID(Double.NaN);
        l.setNode(new Text("0"));
        labels.add(l);
    }

    // ----------------------------------------------------------------------
    // data
    // ----------------------------------------------------------------------

    /**
     * major tickが画面に表示される理想個数
     * 
     * @return
     */
    public IntegerProperty prefferedMajorTickNumberProperty() {
        if (prefferedMajorTickNumberProperty == null) {
            prefferedMajorTickNumberProperty = new SimpleIntegerProperty(this, "prefferedMajorTickNumber", 10);
            prefferedMajorTickNumberProperty.addListener(getDataValidateListener());
        }
        return prefferedMajorTickNumberProperty;
    }

    public int getPrefferedMajorTickNumber() {
        return prefferedMajorTickNumberProperty == null ? 10 : prefferedMajorTickNumberProperty.get();
    }

    public void setPrefferedMajorTickNumber(final int value) {
        prefferedMajorTickNumberProperty().set(value);
    }

    private IntegerProperty prefferedMajorTickNumberProperty;

    /**
     * major tick間の画面距離（ピクセルの事）の最小値の理想。
     * 
     * @return
     */
    public DoubleProperty minUnitLengthProperty() {
        if (minUnitLengthProperty == null) {
            minUnitLengthProperty = new SimpleDoubleProperty(this, "minUnitLength", 20);
            minUnitLengthProperty.addListener(getDataValidateListener());
        }
        return minUnitLengthProperty;
    }

    public double getMinUnitLength() {
        return minUnitLengthProperty == null ? 20 : minUnitLengthProperty.get();
    }

    public void setMinUnitLength(final double value) {
        minUnitLengthProperty().set(value);
    }

    private DoubleProperty minUnitLengthProperty;

    /**
     * major tick間を何分割するか。表示されるMinor tickはこの数より1少ない。 これより大きくなることはない。
     * 
     * @return
     */
    public IntegerProperty prefferedMinorCountProperty() {
        if (prefferedMinorCountProperty == null) {
            prefferedMinorCountProperty = new SimpleIntegerProperty(this, "prefferedMinorCount", 10);
        }
        return prefferedMinorCountProperty;
    }

    public int getPrefferedMinorCount() {
        return prefferedMinorCountProperty == null ? 10 : prefferedMinorCountProperty.get();
    }

    public void setPrefferedMinorCount(final int value) {
        prefferedMinorCountProperty().set(value);
    }

    private IntegerProperty prefferedMinorCountProperty;

    /**
     * minor tick間の最小画面距離
     * 
     * @return
     */
    public DoubleProperty minorUnitMinLengthProperty() {
        if (minorUnitMinLengthProperty == null) {
            minorUnitMinLengthProperty = new SimpleDoubleProperty(this, "minorUnitMinLength", 4);
        }
        return minorUnitMinLengthProperty;
    }

    public double getMinorUnitMinLength() {
        return minorUnitMinLengthProperty == null ? 4 : minorUnitMinLengthProperty.get();
    }

    public void setMinorUnitMinLength(final double value) {
        minorUnitMinLengthProperty().set(value);
    }

    private DoubleProperty minorUnitMinLengthProperty;

    /**
     * We use these for auto ranging to pick a user friendly tick unit. We handle tick units in the
     * range of 1e-10 to 1e+12
     */
    private static final double[] TICK_UNIT_DEFAULTS = {1.0E-10d, 2.5E-10d, 5.0E-10d, 1.0E-9d, 2.5E-9d, 5.0E-9d, 1.0E-8d, 2.5E-8d, 5.0E-8d,
            1.0E-7d, 2.5E-7d, 5.0E-7d, 1.0E-6d, 2.5E-6d, 5.0E-6d, 1.0E-5d, 2.5E-5d, 5.0E-5d, 1.0E-4d, 2.5E-4d, 5.0E-4d, 0.0010d, 0.0025d,
            0.0050d, 0.01d, 0.025d, 0.05d, 0.1d, 0.25d, 0.5d, 1.0d, 2.5d, 5.0d, 10.0d, 25.0d, 50.0d, 100.0d, 250.0d, 500.0d, 1000.0d,
            2500.0d, 5000.0d, 10000.0d, 25000.0d, 50000.0d, 100000.0d, 250000.0d, 500000.0d, 1000000.0d, 2500000.0d, 5000000.0d, 1.0E7d,
            2.5E7d, 5.0E7d, 1.0E8d, 2.5E8d, 5.0E8d, 1.0E9d, 2.5E9d, 5.0E9d, 1.0E10d, 2.5E10d, 5.0E10d, 1.0E11d, 2.5E11d, 5.0E11d, 1.0E12d,
            2.5E12d, 5.0E12d};

    protected static final ConstantDArray TICK_UNIT_DEFAULTS_ARRAY = new ConstantDArray(false, TICK_UNIT_DEFAULTS);
}
