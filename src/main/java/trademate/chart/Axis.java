/*
 * Copyright (C) 2019 CoinToss Development Team
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          https://opensource.org/licenses/MIT
 */
package trademate.chart;

import java.util.ArrayList;
import java.util.List;
import java.util.function.DoubleFunction;

import javafx.beans.property.DoubleProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.ObservableList;
import javafx.geometry.Bounds;
import javafx.geometry.Orientation;
import javafx.geometry.Side;
import javafx.scene.Group;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollBar;
import javafx.scene.control.Tooltip;
import javafx.scene.input.ScrollEvent;
import javafx.scene.layout.Region;
import javafx.scene.shape.Line;
import javafx.scene.shape.LineTo;
import javafx.scene.shape.MoveTo;
import javafx.scene.shape.Path;
import javafx.scene.shape.PathElement;
import javafx.util.Duration;
import kiss.Disposable;
import stylist.Style;
import viewtify.Viewtify;
import viewtify.ui.helper.LayoutAssistant;
import viewtify.ui.helper.StyleHelper;

public class Axis extends Region {

    /** The default zoom size. */
    private static final int ZoomSize = 200;

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

    /** The visual placement position. */
    public final Side side;

    public final ObjectProperty<DoubleFunction<String>> tickLabelFormatter = new SimpleObjectProperty<>(this, "tickLabelFormatter", String::valueOf);

    /** The preferred visible number of ticks. */
    public final int tickNumber = 10;

    /** The visual length of tick. */
    public final int tickLength;

    /** The visual distance between tick and label. */
    public final int tickLabelDistance;

    /** The logical maximum value. */
    public final DoubleProperty logicalMaxValue = new SimpleDoubleProperty(this, "logicalMaxValue", 10000);

    /** The logical minimum value. */
    public final DoubleProperty logicalMinValue = new SimpleDoubleProperty(this, "logicalMinValue", 0);

    /** The visible maximum range. */
    public final DoubleProperty visibleMaxRange = new SimpleDoubleProperty(this, "visibleMaxRange", Double.MAX_VALUE);

    /** The visible minimum range. */
    public final DoubleProperty visibleMinRange = new SimpleDoubleProperty(this, "visibleMinRange", 0);

    /** The right padding. */
    public final DoubleProperty paddingRight = new SimpleDoubleProperty(this, "paddingRight", 0.1);

    /** The tick unit. */
    public final ObjectProperty<double[]> units = new SimpleObjectProperty(DefaultTickUnit);

    public final List<TickLable> forGrid = new ArrayList();

    public final ScrollBar scroll = new ScrollBar();

    /** UI widget. */
    private final Group lines = new Group();

    /** UI widget. */
    private final Path tickPath = new Path();

    /** UI widget. */
    private final Group tickLabels = new Group();

    /** UI widget. */
    private final Line baseLine = new Line();

    /** The current. (axisLength / visibleValueDistance) */
    private double uiRatio;

    /** Flag whether axis shoud layout on the next rendering phase or not. */
    private LayoutAssistant layoutAxis = new LayoutAssistant(this) //
            .layoutBy(widthProperty(), heightProperty())
            .layoutBy(logicalMaxValue, logicalMinValue)
            .layoutBy(scroll.visibleAmountProperty(), scroll.valueProperty());

    /**
     * 
     */
    public Axis(int tickLength, int tickLabelDistance, Side side) {
        this.tickLength = tickLength;
        this.tickLabelDistance = tickLabelDistance;
        this.side = side;

        for (int i = 0; i < tickNumber; i++) {
            forGrid.add(new TickLable(ChartStyles.BackGrid));
        }

        Viewtify.clip(tickPath, this);
        Viewtify.clip(tickLabels, this);

        // ====================================================
        // Initialize UI widget
        // ====================================================
        StyleHelper.of(tickPath).style(ChartStyles.BackGrid);
        StyleHelper.of(baseLine).style(ChartStyles.BackGrid);

        lines.getChildren().addAll(tickPath, baseLine);

        scroll.setOrientation(isHorizontal() ? Orientation.HORIZONTAL : Orientation.VERTICAL);
        scroll.setMin(0);
        scroll.setMax(1);
        scroll.setVisibleAmount(1);
        scroll.setUnitIncrement(1d / ZoomSize);

        getChildren().addAll(lines, tickLabels, scroll);

        // zoom function
        addEventHandler(ScrollEvent.SCROLL, e -> {
            zoom(scroll.getVisibleAmount() + e.getDeltaY() / e.getMultiplierY() / ZoomSize);
        });
    }

    /**
     * Fit zooming.
     */
    public void zoom() {
        double currentAmount = scroll.getVisibleAmount();
        double range = computeVisibleMaxValue() - computeVisibleMinValue();

        if (visibleMaxRange.get() <= range) {
            zoom(currentAmount * (visibleMaxRange.get() / range));
        } else if (range <= visibleMinRange.get()) {
            zoom(currentAmount * (visibleMinRange.get() / range));
        }
    }

    /**
     * Increment or decrement zooming.
     * 
     * @param newAmount
     */
    public void zoom(double newAmount) {
        double currentAmount = scroll.getVisibleAmount();
        double range = computeVisibleMaxValue() - computeVisibleMinValue();

        if (currentAmount < newAmount) {
            if (visibleMaxRange.get() <= range) {
                return;
            }
        }

        if (currentAmount > newAmount) {
            if (range <= visibleMinRange.get()) {
                return;
            }
        }
        scroll.setVisibleAmount(newAmount);
    }

    /**
     * Detect orientation of this axis.
     * 
     * @return
     */
    public final boolean isHorizontal() {
        return side.isHorizontal();
    }

    /**
     * Detect orientation of this axis.
     * 
     * @return
     */
    public final boolean isVertical() {
        return side.isVertical();
    }

    /**
     * Configure tick interval units.
     * 
     * @param units A tick interval unit list.
     * @return Chainable API.
     */
    public final Axis units(double... units) {
        this.units.set(units);
        return this;
    }

    /**
     * Configure {@link ScrollBar}'s visibility.
     * 
     * @param visible A visibility of scroll bar.
     * @return Chainable API.
     */
    public final Axis visibleScroll(boolean visible) {
        scroll.setVisible(visible);
        return this;
    }

    public int tickSize() {
        return tickLabels.getChildren().size();
    }

    public TickLable labelAt(int index) {
        return (TickLable) tickLabels.getChildren().get(index);
    }

    /**
     * Compute the visual position for the specified value.
     * 
     * @param value A value to search position.
     * @return A corresponding visual position.
     */
    public double getPositionForValue(double value) {
        double position = uiRatio * (value - computeVisibleMinValue());
        return isHorizontal() ? position : getHeight() - position;
    }

    /**
     * Compute the value for the specified visual position.
     * 
     * @param position
     * @return
     */
    public final double getValueForPosition(double position) {
        if (isVertical()) {
            position = getHeight() - position;
        }
        return position / uiRatio + computeVisibleMinValue();
    }

    /**
     * Compute visible max value.
     * 
     * @return
     */
    public final double computeVisibleMaxValue() {
        double max = logicalMaxValue.get();
        double min = logicalMinValue.get();
        double amount = scroll.getVisibleAmount();
        return Math.min(computeVisibleMinValue() + (max - min) * amount, max);
    }

    /**
     * Compute visible max value.
     * 
     * @return
     */
    public final double computeVisibleMinValue() {
        double position = isHorizontal() ? scroll.getValue() : 1 - scroll.getValue();
        double max = logicalMaxValue.get();
        double min = logicalMinValue.get();
        double logicalDiff = max - min;
        double bar = logicalDiff * scroll.getVisibleAmount();
        return Math.max(min, min + (logicalDiff - bar) * position);
    }

    /**
     * Compute axis properties to layout items.
     * 
     * @param width A current visual width, may be -1.
     * @param height A curretn visual height, may be -1.
     */
    private void computeAxisProperties(double width, double height) {
        double low = computeVisibleMinValue();
        double up = computeVisibleMaxValue();
        double visualDiff = up - low;
        this.uiRatio = computeAxisLength() / visualDiff;

        // layout scroll bar
        double max = logicalMaxValue.get();
        double min = logicalMinValue.get();
        double logicalDiff = max - min;

        if (low == min && up == max) {
            scroll.setValue(1);
            scroll.setVisibleAmount(1);
        } else {
            double value = (low - min) / (logicalDiff - visualDiff);
            scroll.setValue(isHorizontal() ? value : 1 - value);
            scroll.setVisibleAmount(visualDiff / logicalDiff);
        }

        // search nearest unit index
        int nextUnitIndex = findNearestUnitIndex(visualDiff / tickNumber);
        double nextUnitSize = units.get()[nextUnitIndex];
        double visibleTickBaseValue = Math.floor(low / nextUnitSize) * nextUnitSize;

        // reset value for grid lines
        for (int i = 0; i < forGrid.size(); i++) {
            forGrid.get(i).value.set(visibleTickBaseValue + nextUnitSize * (i + 1));
        }
    }

    /**
     * Find nearest unit index from the unit list.
     * 
     * @param tickInterval
     * @return
     */
    private int findNearestUnitIndex(double tickInterval) {
        // serach from unit list
        for (int i = 0; i < units.get().length; i++) {
            if (tickInterval < units.get()[i]) {
                return i;
            }
        }
        return units.get().length - 1;
    }

    /**
     * Compute axis visual length.
     * 
     * @return
     */
    private double computeAxisLength() {
        if (isVertical()) {
            return getHeight();
        } else {
            return getWidth();
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected final void layoutChildren() {
        layoutAxis.layout(() -> {
            double width = getWidth();
            double height = getHeight();

            computeAxisProperties(width, height);
            layoutLines(width, height);
            layoutLabels(width, height);
            layoutGroups(width, height);
        });
    }

    /**
     * Layout lines.
     * 
     * @param width
     * @param height
     */
    private void layoutLines(double width, double height) {
        boolean horizontal = isHorizontal();
        double axisLength = computeAxisLength();
        baseLine.setEndX(horizontal ? axisLength : 0);
        baseLine.setEndY(horizontal ? 0 : axisLength);

        final int k = horizontal ? side != Side.TOP ? 1 : -1 : side != Side.RIGHT ? -1 : 1;

        final ObservableList<PathElement> elements = tickPath.getElements();
        if (elements.size() > tickSize() * 2) {
            elements.remove(tickSize() * 2, elements.size());
        }

        final int eles = elements.size();
        final int ls = tickSize();
        for (int i = 0; i < ls; i++) {
            final double d = labelAt(i).position();
            MoveTo mt;
            LineTo lt;
            if (i * 2 < eles) {
                mt = (MoveTo) elements.get(i * 2);
                lt = (LineTo) elements.get(i * 2 + 1);
            } else {
                mt = new MoveTo();
                lt = new LineTo();
                elements.addAll(mt, lt);
            }
            double x1, x2, y1, y2;
            if (horizontal) {
                x1 = x2 = d;
                y1 = 0;
                y2 = tickLength * k;
            } else {
                x1 = 0;
                x2 = tickLength * k;
                y1 = y2 = d;
            }
            mt.setX(x1);
            mt.setY(y1);
            lt.setX(x2);
            lt.setY(y2);
        }
    }

    /**
     * Layout labels.
     * 
     * @param width
     * @param height
     */
    private void layoutLabels(double width, double height) {
        for (int i = 0, e = tickSize(); i < e; i++) {
            TickLable label = labelAt(i);
            label.setLayoutX(0);
            label.setLayoutY(0);

            double position = label.position();
            Bounds bounds = label.getBoundsInParent();

            if (isHorizontal()) {
                double cx = (bounds.getMinX() + bounds.getMaxX()) * 0.5;
                label.setLayoutX(label.position() - cx);
                if (side == Side.BOTTOM) {
                    label.setLayoutY(tickLabelDistance);
                } else {
                    label.setLayoutY(-tickLabelDistance);
                }
            } else {
                double cy = (bounds.getMinY() + bounds.getMaxY()) * 0.5;
                label.setLayoutY(position - cy);

                if (side == Side.LEFT) {
                    label.setLayoutX(-tickLabelDistance);
                } else {
                    label.setLayoutX(tickLabelDistance);
                }
            }
        }
    }

    /**
     * Layout.
     * 
     * @param width
     * @param height
     */
    private void layoutGroups(double width, double height) {
        if (isHorizontal()) {
            double distanceFromTop = 0;

            // scroll bar
            if (scroll.isVisible()) {
                distanceFromTop = scroll.prefHeight(-1);
                scroll.resizeRelocate(0, 0, width, distanceFromTop);
            }

            // lines
            lines.setLayoutX(0);
            lines.setLayoutY(Math.floor(distanceFromTop));

            // labels
            tickLabels.setLayoutX(0);
            tickLabels.setLayoutY(Math.floor(distanceFromTop + lines.prefHeight(-1) + tickLabelDistance));

        } else {
            // lines
            lines.setLayoutX(0);
            lines.setLayoutY(0);

            // labels
            tickLabels.setLayoutX(Math.floor(tickLabelDistance + lines.prefWidth(-1)));
            tickLabels.setLayoutY(0);
        }
    }

    /**
     * Create new label.
     * 
     * @return
     */
    public TickLable createLabel(String description, Style... styles) {
        return new TickLable(description, styles);
    }

    /**
     * @version 2018/07/31 6:58:28
     */
    public class TickLable extends Label implements Disposable {

        /** The associated value. */
        public final DoubleProperty value = new SimpleDoubleProperty();

        /** The label formatter. */
        private DoubleFunction<String> formatter;

        /**
         * 
         */
        private TickLable(Style... classNames) {
            this(null, classNames);
        }

        /**
         * 
         */
        private TickLable(String description, Style... classNames) {
            tickLabels.getChildren().add(this);
            textProperty().bind(Viewtify.calculate(value, () -> formatter != null ? formatter.apply(value.get())
                    : tickLabelFormatter.get().apply(value.get())));
            value.addListener(layoutAxis);

            if (description != null && !description.isEmpty()) {
                Tooltip tooltip = new Tooltip(description);
                tooltip.setShowDuration(Duration.INDEFINITE);
                tooltip.setShowDelay(Duration.ZERO);
                setTooltip(tooltip);
            }

            StyleHelper.of(this).style(ChartStyles.Label).style(classNames);
        }

        /**
         * Set label formatter.
         * 
         * @param formatter
         * @return
         */
        public TickLable formatter(DoubleFunction<String> formatter) {
            this.formatter = formatter;
            return this;
        }

        /**
         * Compute the current position in axis.
         * 
         * @return
         */
        public double position() {
            return getPositionForValue(value.get());
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public void vandalize() {
            Viewtify.inUI(() -> {
                tickLabels.getChildren().remove(this);
                textProperty().unbind();
                value.removeListener(layoutAxis);
            });
        }
    }
}
