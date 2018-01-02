/*
 * Copyright (C) 2017 Nameless Production Committee
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          http://opensource.org/licenses/mit-license.php
 */
package trademate.chart;

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

    /**
     * We use these for auto ranging to pick a user friendly tick unit. We handle tick units in the
     * range of 1e-10 to 1e+12
     */
    private static final double[] TickUnit = {0.0010d, 0.0025d, 0.0050d, 0.01d, 0.025d, 0.05d, 0.1d, 0.25d, 0.5d, 1.0d, 2.5d, 5.0d, 10.0d,
            25.0d, 50.0d, 100.0d, 250.0d, 500.0d, 1000.0d, 2500.0d, 5000.0d, 10000.0d, 25000.0d, 50000.0d, 100000.0d, 250000.0d, 500000.0d,
            1000000.0d, 2500000.0d, 5000000.0d};

    public final ObjectProperty<DoubleToObjectFunction<String>> tickLabelFormatter = new SimpleObjectProperty<>(this, "tickLabelFormatter", String::valueOf);

    private double lowVal = 0;

    private double uiRatio;

    public final ObjectProperty<double[]> units = new SimpleObjectProperty(this, "units", TickUnit);

    /**
     * {@inheritDoc}
     */
    @Override
    public double getPositionForValue(final double v) {
        final double d = uiRatio * (v - lowVal);
        return isHorizontal() ? d : getHeight() - d;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public double getValueForPosition(double position) {
        if (!isHorizontal()) {
            position = getHeight() - position;
        }
        return position / uiRatio + lowVal;
    }

    private int unitIndex = -1;

    /**
     * {@inheritDoc}
     */
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

    private int findNearestUnitIndex(double majorTickValueInterval) {
        // serach from unit list
        for (int i = 0; i < units.get().length; i++) {
            if (majorTickValueInterval < units.get()[i]) {
                return i;
            }
        }
        return units.get().length - 1;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void computeAxisProperties(double width, double height) {
        majors.clear();
        minors.clear();
        majorsFill.clear();

        final double lowValue = computeLowerValue(logicalMaxValue.get());
        final double upValue = computeUpperValue(lowValue);
        final double uiFullLength = getAxisLength(width, height);
        if (lowValue == upValue || lowValue != lowValue || upValue != upValue || uiFullLength <= 0) {
            noData(width, height);
            return;
        }

        lowVal = lowValue;
        visualMaxValue.set(upValue);
        {// scroll bar
            final double max = logicalMaxValue.get();
            final double min = logicalMinValue.get();
            if (lowValue == min && max == upValue) {
                scrollBarValue.set(-1);
                scrollBarSize.set(1);
            } else {
                final double ll = max - min;
                final double l = upValue - lowValue;
                scrollBarValue.set((lowValue - min) / (ll - l));
                scrollBarSize.set(l / ll);
            }
        }

        // search sutable unit
        int majorTickCount = 10; // getPrefferedMajorTickNumber();
        double minimumMajorTickVisualInterval = 20;// getMinUnitLength();
        double majorTickVisualInterval = Math.max(uiFullLength / majorTickCount, minimumMajorTickVisualInterval);

        double visibleValueDistance = upValue - lowValue;
        double majorTickValueInterval = visibleValueDistance / (uiFullLength / majorTickVisualInterval);
        int nextUnitIndex = findNearestUnitIndex(majorTickValueInterval);
        boolean usePrevious = nextUnitIndex == unitIndex;

        double nextUnitSize = units.get()[nextUnitIndex];

        double visibleStartUnitBasedValue = floor(lowValue / nextUnitSize) * nextUnitSize;
        double uiRatio = uiFullLength / visibleValueDistance;

        double uiLengthPerMajorTick = uiRatio * nextUnitSize;
        int actualVisibleMajorTickCount = (int) (ceil((upValue - visibleStartUnitBasedValue) / nextUnitSize));

        if (actualVisibleMajorTickCount <= 0 || 2000 < actualVisibleMajorTickCount) {
            noData(width, height);
            return;
        }
        this.uiRatio = uiRatio;

        int minorTickCount = 10; // getPrefferedMinorCount();
        double uiLengthPerMinorTick = minorTickLength.get() <= 0 ? 0 : uiLengthPerMajorTick / minorTickCount;

        boolean isH = isHorizontal();

        ObservableList<AxisLabel> labels = getLabels();
        if (!usePrevious) {
            labels.clear();
        }

        ArrayList<AxisLabel> unused = new ArrayList<>(labels);
        ArrayList<AxisLabel> labelList = new ArrayList<>(actualVisibleMajorTickCount + 1);

        for (int i = 0; i <= actualVisibleMajorTickCount + 1; i++) {
            double value = visibleStartUnitBasedValue + nextUnitSize * i;
            if (value > upValue) {
                break;// i==k
            }
            double majorpos = uiRatio * (value - lowValue);
            if (value >= lowValue) {
                majors.add(floor(isH ? majorpos : height - majorpos));
                boolean find = false;
                for (int t = 0, lsize = unused.size(); t < lsize; t++) {
                    AxisLabel a = unused.get(t);
                    if (a.match(value)) {
                        labelList.add(a);
                        unused.remove(t);
                        find = true;
                        break;
                    }
                }
                if (!find) {
                    AxisLabel a = new AxisLabel();
                    a.setID(value);
                    Text text = new Text(tickLabelFormatter.get().apply(value));
                    text.getStyleClass().add("tick-label");
                    a.setNode(text);
                    labelList.add(a);
                }
            }
            if (0 < uiLengthPerMinorTick) {
                for (int count = 1; count < minorTickCount; count++) {
                    double minorpos = majorpos + count * uiLengthPerMinorTick;
                    if (minorpos < 0) {
                        continue;
                    }
                    if (minorpos >= uiFullLength) {
                        break;
                    }
                    minors.add(floor(isH ? minorpos : height - minorpos));
                }
            }
        } // end for

        // これで大丈夫か？
        labels.removeAll(unused);
        for (int i = 0, e = labelList.size(); i < e; i++) {
            AxisLabel axisLabel = labelList.get(i);

            if (!labels.contains(axisLabel)) {
                labels.add(i, axisLabel);
            }
        }
    }

    protected void noData(final double width, final double height) {
        unitIndex = -1;
        lowVal = 0;
        visualMaxValue.set(1);
        final double len = getAxisLength(width, height);
        uiRatio = len;
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
}
