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

import java.util.ArrayList;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.ObservableList;
import javafx.scene.text.Text;

import org.eclipse.collections.api.block.function.primitive.DoubleToObjectFunction;

/**
 * @version 2017/09/27 9:11:27
 */
public class LinearAxis extends Axis {

    public final ObjectProperty<DoubleToObjectFunction<String>> tickLabelFormatter = new SimpleObjectProperty<>(this, "tickLabelFormatter", String::valueOf);

    private double lowVal = 0;

    private double m;

    @Override
    public double getPositionForValue(final double v) {
        final double d = m * (v - lowVal);
        return isHorizontal() ? d : getHeight() - d;
    }

    @Override
    public double getValueForPosition(double position) {
        if (!isHorizontal()) {
            position = getHeight() - position;
        }
        return position / m + lowVal;
    }

    private int unitIndex = -1;

    private double lastPUnitSize = Double.NaN;

    @Override
    public void adjustLowerValue() {
        double max = logicalMaxValue.get();
        double min = logicalMinValue.get();
        double diff = max - min;
        double range = visibleRange.get();
        double low = computeLowerValue(max);
        double up = low + diff * range;
        if (up > max) {
            low = max - diff * range;
            visualMinValue.set(low);
        }
    }

    private double computeUpperValue(final double low) {
        final double max = logicalMaxValue.get();
        final double a = visibleRange.get();
        final double min = logicalMinValue.get();
        final double ll = max - min;
        return min(low + ll * a, max);
    }

    @Override
    protected void computeAxisProperties(final double width, final double height) {
        majors.clear();
        minors.clear();
        majorsFill.clear();

        final double low = computeLowerValue(logicalMaxValue.get());
        final double up = computeUpperValue(low);
        final double len = getAxisLength(width, height);
        if (low == up || low != low || up != up || len <= 0) {
            noData(width, height);
            return;
        }

        lowVal = low;
        visualMaxValue.set(up);
        {// scroll bar
            final double max = logicalMaxValue.get();
            final double min = logicalMinValue.get();
            if (low == min && max == up) {
                scrollBarValue.set(-1);
                scrollBarSize.set(1);
            } else {
                final double ll = max - min;
                final double l = up - low;
                scrollBarValue.set((low - min) / (ll - l));
                scrollBarSize.set(l / ll);
            }
        }

        // 適当な単位を見つける
        final int mtn = 10; // getPrefferedMajorTickNumber();
        final double minu = 20;// getMinUnitLength();
        double pUnitLength = len / mtn;
        if (minu > 0 && pUnitLength < minu) {
            pUnitLength = minu;
        }
        final double pUnitSize = (up - low) / (len / pUnitLength);
        int uindex = unitIndex;// 前回の探索結果の再利用
        boolean useBefore = true;
        if (lastPUnitSize != pUnitSize) {
            if (pUnitSize <= TickUnits[0]) {
                uindex = 0;
            } else if (pUnitSize >= TickUnits[TickUnits.length - 1]) {
                uindex = TickUnits.length - 1;
            } else {
                BLOCK: {
                    int l = 1, r = TickUnits.length - 2;
                    int m = (l + r >> 1);

                    while (r - l > 1) {
                        final double d = TickUnits[m];
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

                    if (TickUnits[r] < pUnitSize) {
                        uindex = r + 1;
                    } else if (TickUnits[l] > pUnitSize) {
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
        final double usize = TickUnits[uindex];

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
        int mcount = 10; // getPrefferedMinorCount();
        if (minorTickLength.get() <= 0 || mcount <= 1) {
            minorLength = -1;
        } else {
            minorLength = majorLength / mcount;
            final double mins = 4; // minor unit min length
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
                majors.add(floor(isH ? majorpos : height - majorpos));
                majorsFill.add(fill);
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
                    a.setNode(new Text(tickLabelFormatter.get().apply(value)));
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
        visualMaxValue.set(1);
        final double len = getAxisLength(width, height);
        m = len;
        majors.add(0d);
        majors.add(getAxisLength(width, height));
        majorsFill.add(true);
        majorsFill.add(false);
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
     * We use these for auto ranging to pick a user friendly tick unit. We handle tick units in the
     * range of 1e-10 to 1e+12
     */
    private static final double[] TickUnits = {1.0E-10d, 2.5E-10d, 5.0E-10d, 1.0E-9d, 2.5E-9d, 5.0E-9d, 1.0E-8d, 2.5E-8d, 5.0E-8d, 1.0E-7d,
            2.5E-7d, 5.0E-7d, 1.0E-6d, 2.5E-6d, 5.0E-6d, 1.0E-5d, 2.5E-5d, 5.0E-5d, 1.0E-4d, 2.5E-4d, 5.0E-4d, 0.0010d, 0.0025d, 0.0050d,
            0.01d, 0.025d, 0.05d, 0.1d, 0.25d, 0.5d, 1.0d, 2.5d, 5.0d, 10.0d, 25.0d, 50.0d, 100.0d, 250.0d, 500.0d, 1000.0d, 2500.0d,
            5000.0d, 10000.0d, 25000.0d, 50000.0d, 100000.0d, 250000.0d, 500000.0d, 1000000.0d, 2500000.0d, 5000000.0d, 1.0E7d, 2.5E7d,
            5.0E7d, 1.0E8d, 2.5E8d, 5.0E8d, 1.0E9d, 2.5E9d, 5.0E9d, 1.0E10d, 2.5E10d, 5.0E10d, 1.0E11d, 2.5E11d, 5.0E11d, 1.0E12d, 2.5E12d,
            5.0E12d};
}
