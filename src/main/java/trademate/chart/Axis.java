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
import java.util.concurrent.atomic.AtomicBoolean;

import javafx.beans.InvalidationListener;
import javafx.beans.Observable;
import javafx.beans.binding.Bindings;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ReadOnlyDoubleWrapper;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.geometry.Bounds;
import javafx.geometry.Orientation;
import javafx.geometry.Side;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.ScrollBar;
import javafx.scene.input.ScrollEvent;
import javafx.scene.layout.Region;
import javafx.scene.shape.Line;
import javafx.scene.shape.LineTo;
import javafx.scene.shape.MoveTo;
import javafx.scene.shape.Path;
import javafx.scene.shape.PathElement;
import javafx.scene.text.Text;

import org.eclipse.collections.api.block.function.primitive.DoubleToObjectFunction;
import org.eclipse.collections.api.list.primitive.MutableDoubleList;
import org.eclipse.collections.impl.factory.primitive.DoubleLists;

import viewtify.ui.UILine;

/**
 * @version 2017/09/27 13:44:10
 */
public class Axis extends Region {

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

    /** The layouting flag. */
    private final AtomicBoolean whileLayout = new AtomicBoolean();

    /** 状態の正当性を示すプロパティ */
    private boolean layoutValidate = false;

    /** 状態の正当性を示すプロパティ */
    private boolean dateIsValid = false;

    private double lastLayoutWidth = -1, lastLayoutHeight = -1;

    /** The visual placement position. */
    public final Side side;

    public final ObjectProperty<DoubleToObjectFunction<String>> tickLabelFormatter = new SimpleObjectProperty<>(this, "tickLabelFormatter", String::valueOf);

    /** レイアウトを構成すべき情報に対して付加すべきリスナ */
    private final InvalidationListener layoutValidator = observable -> {
        if (layoutValidate) {
            layoutValidate = false;
            requestLayout();
        }
    };

    /** Axisの構成情報を書き換えるべきデータに対して付加するリスナ */
    private final InvalidationListener dataValidateListener = observable -> {
        if (widthProperty() == observable) {
            if (isVertical() || getWidth() == lastLayoutWidth) {
                return;
            }
        }
        if (heightProperty() == observable) {
            if (isHorizontal() || getHeight() == lastLayoutHeight) {
                return;
            }
        }
        if (dateIsValid) {
            dateIsValid = false;
            requestLayout();
        }
    };

    private final InvalidationListener scrollValueValidator = new InvalidationListener() {

        private boolean doflag = true;

        /**
         * {@inheritDoc}
         */
        @Override
        public void invalidated(final Observable observable) {
            if (whileLayout.get() == false) {
                if (scroll != null && observable == scroll.valueProperty()) {
                    final double position = scroll.getValue();
                    final double size = scrollBarSize.get();
                    if (position == -1 || size == 1) {
                        visualMinValue.set(Double.NaN);
                        visibleRange.set(1);
                    } else {
                        final double p = isHorizontal() ? position : 1 - position;
                        final double d = calcLowValue(p, size);
                        visualMinValue.set(d);
                    }
                } else if (scroll != null) {
                    final double d = isHorizontal() ? scrollBarValue.get() : 1 - scrollBarValue.get();
                    scroll.setValue(d);
                }
            } else if (doflag && scroll != null && observable != scroll.valueProperty()) {
                doflag = false;
                final double d = isHorizontal() ? scrollBarValue.get() : 1 - scrollBarValue.get();
                scroll.setValue(d);
                doflag = true;
            }
        }
    };

    public final BooleanProperty scrollBarVisibility = new SimpleBooleanProperty(this, "scrollBarVisible", true);

    /** スクロールバーのvisibleAmountを0～1で表現する。 */
    public final ReadOnlyDoubleWrapper scrollBarSize = new ReadOnlyDoubleWrapper(this, "scrollBarSize", 1);

    /** スクロールバーの表示位置のプロパティ。縦方向の場合、1からこの値を引いた値を利用する。 -1の時、非表示となる。 bindする際にはbindBidirectionalを用いること */
    public final DoubleProperty scrollBarValue = new SimpleDoubleProperty(this, "scrollBarPosition", -1);

    /** The preferred visible number of ticks. */
    public final IntegerProperty tickNumber = new SimpleIntegerProperty(10);

    /** The visual length of tick. */
    public final int tickLength;

    /** The visual distance between tick and label. */
    public final int tickLabelDistance;

    /** The logical maximum value. */
    public final DoubleProperty logicalMaxValue = new SimpleDoubleProperty(this, "logicalMaxValue", 1);

    /** The logical minimum value. */
    public final DoubleProperty logicalMinValue = new SimpleDoubleProperty(this, "logicalMinValue", 0);

    /** The visual maximum value. */
    public final ReadOnlyDoubleWrapper visualMaxValue = new ReadOnlyDoubleWrapper(this, "visualMaxValue", 1);

    /** The visual minimum value. */
    public final DoubleProperty visualMinValue = new SimpleDoubleProperty(this, "visualMinValue", Double.NaN);

    /** The visible range (between 0 and 1) of scrollable area. "1" will hide scroll bar. */
    public final DoubleProperty visibleRange = new SimpleDoubleProperty(this, "visibleAmount", 1) {
        /**
         * {@inheritDoc}
         */
        @Override
        public void set(double newValue) {
            if (newValue > 1) {
                newValue = 1;
            } else if (newValue <= 0) {
                return;
            }
            super.set(newValue);
        }
    };

    /** The tick unit. */
    public final ObjectProperty<double[]> units = new SimpleObjectProperty(DefaultTickUnit);

    protected final MutableDoubleList ticks = DoubleLists.mutable.empty();

    /** UI widget. */
    private final Group lines = new Group();

    /** UI widget. */
    private final Path tickPath = new Path();

    /** UI widget. */
    private final Group tickLabels = new Group();

    /** UI widget. */
    private final UILine indicatorPath = new UILine().style(ChartClass.AxisTick).visible(false).startX(0).startY(0);

    /** UI widget. */
    private final Text indicatorLabel = new Text();

    /** UI widget. */
    private final Line baseLine = new Line();

    /** UI widget. */
    protected final ScrollBar scroll = new ScrollBar();

    private double lowVal = 0;

    /** The current. (axisLength / visibleValueDistance) */
    private double uiRatio;

    /** The current unit index. */
    private int currentUnitIndex = -1;

    /**
     * 
     */
    public Axis(int tickLength, int tickLabelDistance, Side side) {
        this.tickLength = tickLength;
        this.tickLabelDistance = tickLabelDistance;
        this.side = side;

        if (isHorizontal()) {
            indicatorPath.endX(0).endY(tickLength);
        } else {
            indicatorPath.endX(tickLength).endY(0);
        }
        indicatorLabel.getStyleClass().add(ChartClass.AxisTickLabel.name());
        tickLabels.getChildren().add(indicatorLabel);

        // ====================================================
        // Initialize Property
        // ====================================================
        widthProperty().addListener(dataValidateListener);
        heightProperty().addListener(dataValidateListener);
        tickNumber.addListener(dataValidateListener);
        logicalMaxValue.addListener(dataValidateListener);
        logicalMinValue.addListener(dataValidateListener);
        visualMinValue.addListener(dataValidateListener);
        visibleRange.addListener(dataValidateListener);
        scrollBarSize.addListener(scrollValueValidator);
        scrollBarValue.addListener(scrollValueValidator);

        // ====================================================
        // Initialize UI widget
        // ====================================================

        tickPath.getStyleClass().setAll(ChartClass.AxisTick.name());
        baseLine.getStyleClass().setAll(ChartClass.AxisLine.name());

        lines.setAutoSizeChildren(false);
        lines.getChildren().addAll(tickPath, indicatorPath.ui, baseLine);

        tickLabels.setAutoSizeChildren(false);

        scroll.setOrientation(isHorizontal() ? Orientation.HORIZONTAL : Orientation.VERTICAL);
        scroll.visibleProperty().bind(Bindings.createBooleanBinding(() -> scrollBarVisibility
                .get() && scrollBarValue.get() != -1 && scrollBarSize.get() != 1, scrollBarValue, scrollBarVisibility, scrollBarSize));
        scroll.visibleProperty().addListener(layoutValidator);
        scroll.valueProperty().addListener(scrollValueValidator);
        scroll.setMin(0);
        scroll.setMax(1);
        scroll.visibleAmountProperty().bind(scrollBarSize);

        if (scrollBarValue.get() != -1 && scrollBarSize.get() != 1) {
            scroll.setValue(isHorizontal() ? scrollBarValue.get() : 1 - scrollBarValue.get());
        }
        getChildren().addAll(lines, tickLabels, scroll);

        addEventHandler(ScrollEvent.SCROLL, this::zoom);
    }

    /**
     * Compute the visual position for the specified value.
     * 
     * @param value A value to search position.
     * @return A corresponding visual position.
     */
    public final double getPositionForValue(double value) {
        double position = uiRatio * (value - lowVal);
        return isHorizontal() ? position : getHeight() - position;
    }

    /**
     * Compute the value for the specified visual position.
     * 
     * @param position
     * @return
     */
    public final double getValueForPosition(double position) {
        if (!isHorizontal()) {
            position = getHeight() - position;
        }
        return position / uiRatio + lowVal;
    }

    /**
     * visibleAmountで設定する範囲が全て見えるような「最大の」最小値を設定する。<br>
     * visibleAmountが全て見えている場合には処理を行わない。
     */
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

    /**
     * Compute axis properties to layout items.
     * 
     * @param width A current visual width, may be -1.
     * @param height A curretn visual height, may be -1.
     */
    private void computeAxisProperties(double width, double height) {
        ticks.clear();

        double low = computeLowerValue(logicalMaxValue.get());
        double up = computeUpperValue(low);
        double axisLength = getAxisLength(width, height);
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
     * {@inheritDoc}
     */
    @Override
    public void requestLayout() {
        Parent parent = getParent();

        if (parent != null) {
            parent.requestLayout();
        }
        super.requestLayout();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected double computePrefWidth(double height) {
        if (isHorizontal()) {
            return 150d;
        } else {
            // force to re-layout
            layoutChildren(lastLayoutWidth, height);

            double scrollBar = scroll.isVisible() ? scroll.getWidth() : 0;
            double labels = max(tickLabels.prefWidth(height), 10);
            return tickLength + scrollBar + labels;
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected double computePrefHeight(double width) {
        if (isVertical()) {
            return 150d;
        } else {
            // force to re-layout
            layoutChildren(width, lastLayoutHeight);

            double scrollBar = scroll.isVisible() ? scroll.getHeight() : 0;
            double labels = max(tickLabels.prefHeight(width), 10);
            return tickLength + scrollBar + labels;
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected final void layoutChildren() {
        if ((isHorizontal() && getWidth() == -1) || (isVertical() && getHeight() == -1)) {
            return;
        }
        layoutChildren(getWidth(), getHeight());
    }

    /**
     * Layout actually.
     * 
     * @param width
     * @param height
     */
    private void layoutAxis(double width, double height) {
        layoutLines(width, height);
        layoutLabels(width, height);
        layoutGroups(width, height);
    }

    /**
     * Layout lines.
     * 
     * @param width
     * @param height
     */
    private void layoutLines(double width, double height) {
        boolean horizontal = isHorizontal();
        double axisLength = getAxisLength(width, height);
        baseLine.setEndX(horizontal ? axisLength : 0);
        baseLine.setEndY(horizontal ? 0 : axisLength);

        final int k = horizontal ? side != Side.TOP ? 1 : -1 : side != Side.RIGHT ? -1 : 1;

        final ObservableList<PathElement> elements = tickPath.getElements();
        if (elements.size() > ticks.size() * 2) {
            elements.remove(ticks.size() * 2, elements.size());
        }

        final int eles = elements.size();
        final int ls = ticks.size();
        for (int i = 0; i < ls; i++) {
            final double d = ticks.get(i);
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
        for (int i = 0, e = ticks.size(); i < e; i++) {
            AxisLabel axisLabel = labels.get(i);
            double d = ticks.get(i);

            // 位置を合わせる
            Node node = axisLabel.node;
            node.setLayoutX(0);
            node.setLayoutY(0);

            Bounds bounds = node.getBoundsInParent();

            if (isHorizontal()) {
                final double cx = (bounds.getMinX() + bounds.getMaxX()) * 0.5;
                node.setLayoutX(d - cx);
                if (side == Side.BOTTOM) {
                    node.setLayoutY(tickLabelDistance);
                } else {
                    node.setLayoutY(-tickLabelDistance);
                }
            } else {
                final double cy = (bounds.getMinY() + bounds.getMaxY()) * 0.5;
                node.setLayoutY(d - cy);
                if (side == Side.LEFT) {
                    node.setLayoutX(-tickLabelDistance);
                } else {
                    node.setLayoutX(tickLabelDistance);
                }
            }
        }

        // 重なるラベルを不可視にする
        Bounds indicatorBounds = indicatorLabel.getBoundsInParent();

        for (

                int i = 0; i != labels.size(); i++) {
            AxisLabel axisLabel = labels.get(i);
            axisLabel.node.setVisible(!indicatorBounds.intersects(axisLabel.node.getBoundsInParent()));
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
            if (side == Side.BOTTOM) {
                double distanceFromTop = 0;

                // scroll bar
                if (scroll.isVisible()) {
                    distanceFromTop = scroll.prefHeight(-1);
                    scroll.resizeRelocate(0, 0, width, distanceFromTop);
                }

                // lines
                lines.setLayoutX(0);
                lines.setLayoutY(floor(distanceFromTop));

                // labels
                tickLabels.setLayoutX(0);
                tickLabels.setLayoutY(floor(distanceFromTop + lines.prefHeight(-1) + tickLabelDistance));
            } else {
                double y = height;
                if (scroll.isVisible()) {
                    final double h = scroll.prefHeight(-1);
                    y -= h;
                    scroll.resizeRelocate(0, floor(y), width, h);
                }
                lines.setLayoutX(0);
                lines.setLayoutY(floor(y));
                y -= lines.prefHeight(-1) + tickLabelDistance;
                tickLabels.setLayoutX(0);
                tickLabels.setLayoutY(floor(y));
            }
        } else {
            if (side == Side.LEFT) {
                double x = width;
                if (scroll.isVisible()) {
                    final double w = scroll.prefWidth(-1);
                    x = x - w;
                    scroll.resizeRelocate(floor(x), 0, w, height);
                }

                lines.setLayoutX(floor(x));
                lines.setLayoutY(0);
                x -= tickLabelDistance + lines.prefWidth(-1);
                tickLabels.setLayoutX(floor(x));
                tickLabels.setLayoutY(0);
            } else {
                double x = 0;
                if (scroll.isVisible()) {
                    final double w = scroll.prefWidth(-1);
                    x = w;
                    scroll.resizeRelocate(0, 0, w, height);
                }

                lines.setLayoutX(floor(x));
                lines.setLayoutY(0);
                x = tickLabelDistance + lines.prefWidth(-1);
                tickLabels.setLayoutX(floor(x));
                tickLabels.setLayoutY(0);
            }
        }

    }

    /**
     * Force to layout child nodes.
     * 
     * @param width
     * @param height
     */
    protected final void layoutChildren(double width, double height) {
        if (whileLayout.compareAndSet(false, true)) {
            try {
                if (!dateIsValid || getAxisLength(width, height) != getAxisLength(lastLayoutWidth, lastLayoutHeight)) {
                    computeAxisProperties(width, height);
                    layoutValidate = false;
                    dateIsValid = true;
                }

                if (!layoutValidate || width != lastLayoutWidth || height != lastLayoutHeight) {
                    layoutAxis(width, height);
                    layoutValidate = true;
                }
                lastLayoutWidth = width;
                lastLayoutHeight = height;
            } finally {
                whileLayout.compareAndSet(true, false);
            }
        }
    }

    /**
     * 軸方向のサイズを返す
     * 
     * @param width
     * @param height
     * @return
     */
    protected final double getAxisLength(final double width, final double height) {
        return isHorizontal() ? width : height;
    }

    /**
     * lowerValueを実際に利用可能な数値に変換して返す
     * 
     * @param up 最大値
     * @return
     */
    protected final double computeLowerValue(double up) {
        double visibleMin = visualMinValue.get();
        double logicalMin = logicalMinValue.get();
        if (up != up) {
            up = logicalMaxValue.get();
        }
        if (visibleMin != visibleMin) {
            visibleMin = logicalMin;
        }
        if (visibleMin > up) {
            visibleMin = up;
        }
        if (visibleMin < logicalMin) {
            return logicalMin;
        } else {
            return visibleMin;
        }
    }

    /**
     * スクロールバーが変更されたときに呼び出されるメソッド。 表示の最小値を計算する。
     * 
     * @param value scrollBarValueに相当する値
     * @param amount scrollVisibleAmountに相当する値
     * @return
     */
    private double calcLowValue(double value, double amount) {
        double max = logicalMaxValue.get();
        double min = logicalMinValue.get();
        double diff = max - min;
        double bar = diff * amount;
        return min + (diff - bar) * value;
    }

    private ObservableList<AxisLabel> labels;

    protected final ObservableList<AxisLabel> getLabels() {
        if (labels == null) {
            labels = FXCollections.observableArrayList();
            labels.addListener((ListChangeListener<AxisLabel>) c -> {
                final ObservableList<Node> list = tickLabels.getChildren();
                while (c.next()) {
                    for (final AxisLabel a1 : c.getRemoved()) {
                        list.remove(a1.node);
                    }
                    for (final AxisLabel a2 : c.getAddedSubList()) {
                        list.add(a2.node);
                        a2.node.setVisible(false);
                    }
                }
            });
        }
        return labels;
    }

    /**
     * @param position
     */
    public void indicateAt(double position) {
        dateIsValid = false;

        if (position < 0) {
            indicatorPath.visible(false);
            indicatorLabel.setVisible(false);
        } else {
            indicatorPath.visible(true);
            indicatorLabel.setVisible(true);
            indicatorLabel.setText(tickLabelFormatter.get().apply(Math.floor(getValueForPosition(position))));

            Bounds bounds = indicatorLabel.getBoundsInParent();

            if (isHorizontal()) {
                indicatorPath.startX(position).endX(position);
                indicatorLabel.setLayoutX(position - bounds.getWidth() / 2);
                indicatorLabel.setLayoutY(tickLabelDistance);
            } else {
                indicatorPath.startY(position).endY(position);
                indicatorLabel.setLayoutY(position + bounds.getHeight() / 4);
                indicatorLabel.setLayoutX(tickLabelDistance);
            }
        }
    }

    private void zoom(ScrollEvent event) {
        Axis axis = (Axis) event.getSource();

        final double d = event.getDeltaY() * 0.01;
        if (d == 0d) {
            event.consume();
            return;
        }
        final double amount = axis.visibleRange.get();
        final double inva = min(1 / amount + d, 20);
        final double newamount = max(min(1 / inva, 1), 0);
        axis.visibleRange.set(newamount);
        if (amount < newamount) {
            axis.adjustLowerValue();
        }
        event.consume();
    }

    /**
     * @version 2017/09/27 14:22:45
     */
    protected static class AxisLabel {

        /** 文字列等の比較以外で同値性を確認するための数値を得る */
        protected final double id;

        protected final Node node;

        /**
         * @param id
         * @param node
         */
        protected AxisLabel(double id, Node node) {
            this.id = id;
            this.node = node;
        }
    }
}
