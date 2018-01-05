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
import javafx.geometry.Side;
import javafx.scene.text.Text;

/**
 * @version 2017/09/27 9:11:27
 */
public class LinearAxis extends Axis {

    /**
     * We use these for auto ranging to pick a user friendly tick unit. We handle tick units in the
     * range of 1e-10 to 1e+12
     */
    private static final double[] DefaultTickUnit = {1.0E-10d, 2.5E-10d, 5.0E-10d, 1.0E-9d, 2.5E-9d, 5.0E-9d, 1.0E-8d, 2.5E-8d, 5.0E-8d,
            1.0E-7d, 2.5E-7d, 5.0E-7d, 1.0E-6d, 2.5E-6d, 5.0E-6d, 1.0E-5d, 2.5E-5d, 5.0E-5d, 1.0E-4d, 2.5E-4d, 5.0E-4d, 0.0010d, 0.0025d,
            0.0050d, 0.01d, 0.025d, 0.05d, 0.1d, 0.25d, 0.5d, 1.0d, 2.5d, 5.0d, 10.0d, 25.0d, 50.0d, 100.0d, 250.0d, 500.0d, 1000.0d,
            2500.0d, 5000.0d, 10000.0d, 25000.0d, 50000.0d, 100000.0d, 250000.0d, 500000.0d, 1000000.0d, 2500000.0d, 5000000.0d, 1.0E7d,
            2.5E7d, 5.0E7d, 1.0E8d, 2.5E8d, 5.0E8d, 1.0E9d, 2.5E9d, 5.0E9d, 1.0E10d, 2.5E10d, 5.0E10d, 1.0E11d, 2.5E11d, 5.0E11d, 1.0E12d,
            2.5E12d, 5.0E12d};

    private double lowVal = 0;

    private double uiRatio;

    /** The current unit index. */
    private int currentUnitIndex = -1;

    public final ObjectProperty<double[]> units = new SimpleObjectProperty(DefaultTickUnit);

    /**
     * @param tickLength
     * @param tickLabelDistance
     * @param side
     */
    public LinearAxis(int tickLength, int tickLabelDistance, Side side) {
        super(tickLength, tickLabelDistance, side);
    }

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
        ticks.clear();

        final double low = computeLowerValue(logicalMaxValue.get());
        final double up = computeUpperValue(low);
        final double axisLength = getAxisLength(width, height);
        if (low == up || Double.isNaN(low) || Double.isNaN(up) || axisLength <= 0) {
            return;
        }

        lowVal = low;
        visualMaxValue.set(up);

        // layout scroll bar
        double max = logicalMaxValue.get();
        double min = logicalMinValue.get();

        if (low == min && up == max) {
            scrollBarValue.set(-1);
            scrollBarSize.set(1);
        } else {
            double logicalDiff = max - min;
            double visualDiff = up - low;
            scrollBarValue.set((low - min) / (logicalDiff - visualDiff));
            scrollBarSize.set(visualDiff / logicalDiff);
        }

        // search sutable unit
        double visibleValueDistance = up - low;
        int nextUnitIndex = findNearestUnitIndex(visibleValueDistance / tickNumber.get());

        double nextUnitSize = units.get()[nextUnitIndex];
        double visibleStartUnitBasedValue = floor(low / nextUnitSize) * nextUnitSize;
        double uiRatio = axisLength / visibleValueDistance;

        int actualVisibleMajorTickCount = (int) (ceil((up - visibleStartUnitBasedValue) / nextUnitSize));

        if (actualVisibleMajorTickCount <= 0 || 2000 < actualVisibleMajorTickCount) {
            return;
        }

        this.uiRatio = uiRatio;

        ObservableList<AxisLabel> labels = getLabels();

        if (currentUnitIndex != nextUnitIndex) {
            labels.clear();
            currentUnitIndex = nextUnitIndex;
        }

        ArrayList<AxisLabel> unused = new ArrayList<>(labels);
        ArrayList<AxisLabel> labelList = new ArrayList<>(actualVisibleMajorTickCount + 1);

        for (int i = 0; i <= actualVisibleMajorTickCount + 1; i++) {
            double value = visibleStartUnitBasedValue + nextUnitSize * i;
            if (value > up) {
                break;// i==k
            }

            double tickPosition = uiRatio * (value - low);

            if (value >= low) {
                ticks.add(floor(isHorizontal() ? tickPosition : height - tickPosition));
                boolean find = false;
                for (int t = 0, lsize = unused.size(); t < lsize; t++) {
                    AxisLabel axisLabel = unused.get(t);

                    if (axisLabel.id == value) {
                        labelList.add(axisLabel);
                        unused.remove(t);
                        find = true;
                        break;
                    }
                }
                if (!find) {
                    Text text = new Text(tickLabelFormatter.get().apply(value));
                    text.getStyleClass().add(ChartClass.AxisTickLabel.name());
                    labelList.add(new AxisLabel(value, text));
                }
            }
        }

        // これで大丈夫か？
        labels.removeAll(unused);
        for (int i = 0, e = labelList.size(); i < e; i++) {
            AxisLabel axisLabel = labelList.get(i);

            if (!labels.contains(axisLabel)) {
                labels.add(i, axisLabel);
            }
        }
    }
}