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

import java.util.concurrent.atomic.AtomicBoolean;

import javafx.beans.InvalidationListener;
import javafx.beans.Observable;
import javafx.beans.binding.Bindings;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ReadOnlyDoubleWrapper;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.geometry.Bounds;
import javafx.geometry.Orientation;
import javafx.geometry.Side;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollBar;
import javafx.scene.layout.Region;
import javafx.scene.shape.Line;
import javafx.scene.shape.LineTo;
import javafx.scene.shape.MoveTo;
import javafx.scene.shape.Path;
import javafx.scene.shape.PathElement;

import org.eclipse.collections.api.list.primitive.MutableBooleanList;
import org.eclipse.collections.api.list.primitive.MutableDoubleList;
import org.eclipse.collections.impl.factory.primitive.BooleanLists;
import org.eclipse.collections.impl.factory.primitive.DoubleLists;

/**
 * @version 2017/09/27 13:44:10
 */
public abstract class Axis extends Region {

    /** The layouting flag. */
    private final AtomicBoolean whileLayout = new AtomicBoolean();

    /** 状態の正当性を示すプロパティ */
    private boolean layoutValidate = false;

    /** 状態の正当性を示すプロパティ */
    private boolean dataValidate = false;

    private double lastLayoutWidth = -1, lastLayoutHeight = -1;

    public final StringProperty name = new SimpleStringProperty(this, "name", null);

    /** The visual placement direction. */
    public final ObjectProperty<Orientation> orientation = new SimpleObjectProperty<Orientation>(this, "orientation", Orientation.HORIZONTAL) {
        /**
         * {@inheritDoc}
         */
        @Override
        public void set(final Orientation newValue) {
            if (newValue == null) {
                return;
            }
            super.set(newValue);
        }
    };

    /** The visual placement position. */
    public final ObjectProperty<Side> side = new SimpleObjectProperty<>(this, "side", orientation.get() != Orientation.VERTICAL
            ? Side.BOTTOM
            : Side.LEFT);

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
            if (orientation.get() != Orientation.HORIZONTAL || getWidth() == lastLayoutWidth) {
                return;
            }
        }
        if (heightProperty() == observable) {
            if (orientation.get() != Orientation.VERTICAL || getHeight() == lastLayoutHeight) {
                return;
            }
        }
        if (dataValidate) {
            dataValidate = false;
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

    public final BooleanProperty scrollBarVisible = new SimpleBooleanProperty(this, "scrollBarVisible", true);

    /** スクロールバーのvisibleAmountを0～1で表現する。 */
    public final ReadOnlyDoubleWrapper scrollBarSize = new ReadOnlyDoubleWrapper(this, "scrollBarSize", 1);

    /** スクロールバーの表示位置のプロパティ。縦方向の場合、1からこの値を引いた値を利用する。 -1の時、非表示となる。 bindする際にはbindBidirectionalを用いること */
    public final DoubleProperty scrollBarValue = new SimpleDoubleProperty(this, "scrollBarPosition", -1);

    /** The visual length of major tick. */
    public final DoubleProperty majorTickLength = new SimpleDoubleProperty(this, "MajorTickLength", 12);

    /** The visual length of minor tick. */
    public final DoubleProperty minorTickLength = new SimpleDoubleProperty(this, "MinorTickLength", 8);

    /** The visibility of minor tick. */
    public final BooleanProperty minorTickVisibility = new SimpleBooleanProperty(this, "MinorTickVisibility", true);

    /** The visual distance between tick and label. */
    public final DoubleProperty tickLabelDistance = new SimpleDoubleProperty(this, "tickLabelGap", 10);

    /** The rotation angle of label. */
    public final DoubleProperty tickLabelRotate = new SimpleDoubleProperty(this, "tickLabelRotate", 0);

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

    protected final MutableDoubleList majors = DoubleLists.mutable.empty();

    protected final MutableBooleanList majorsFill = BooleanLists.mutable.empty();

    protected final MutableDoubleList minors = DoubleLists.mutable.empty();

    public Axis() {
        name.addListener(layoutValidator);

        getStyleClass().add("axis");
        widthProperty().addListener(dataValidateListener);
        heightProperty().addListener(dataValidateListener);
        orientation.addListener(dataValidateListener);
        logicalMaxValue.addListener(dataValidateListener);
        logicalMinValue.addListener(dataValidateListener);
        visualMinValue.addListener(dataValidateListener);
        visibleRange.addListener(dataValidateListener);
        scrollBarSize.addListener(scrollValueValidator);
        scrollBarValue.addListener(scrollValueValidator);
        majorTickLength.addListener(layoutValidator);
        minorTickLength.addListener(layoutValidator);
        minorTickVisibility.addListener(layoutValidator);
        tickLabelDistance.addListener(layoutValidator);
        tickLabelRotate.addListener(layoutValidator);
    }

    /**
     * Compute the visual position for the specified value.
     * 
     * @param value
     * @return
     */
    public abstract double getPositionForValue(double value);

    /**
     * Compute the value for the specified visual position.
     * 
     * @param position
     * @return
     */
    public abstract double getValueForPosition(double position);

    /**
     * visibleAmountで設定する範囲が全て見えるような「最大の」最小値を設定する。<br>
     * visibleAmountが全て見えている場合には処理を行わない。
     */
    public abstract void adjustLowerValue();

    /**
     * Axisの描画に必要なプロパティを計算するメソッド width,heightのどちらかは-1である場合がある。
     * 
     * @param width 描画横幅
     * @param height 描画高さ
     */
    protected abstract void computeAxisProperties(double width, double height);

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
     * Force to layout.
     * 
     * @param width
     * @param height
     */
    protected final void layoutChildren(double width, double height) {
        if (whileLayout.compareAndSet(false, true)) {
            try {
                if (!dataValidate || getAxisLength(width, height) != getAxisLength(lastLayoutWidth, lastLayoutHeight)) {
                    computeAxisProperties(width, height);
                    layoutValidate = false;
                    dataValidate = true;
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

    public final boolean isHorizontal() {
        return orientation.get() == Orientation.HORIZONTAL;
    }

    public final boolean isVertical() {
        return orientation.get() == Orientation.VERTICAL;
    }

    /**
     * lowerValueを実際に利用可能な数値に変換して返す
     * 
     * @param up 最大値
     * @return
     */
    protected final double computeLowerValue(double up) {
        double d = visualMinValue.get();
        final double m = logicalMinValue.get();
        if (up != up) {
            up = logicalMaxValue.get();
        }
        if (d != d) {
            d = m;
        }
        if (d > up) {
            d = up;
        }
        if (d < m) {
            return m;
        } else {
            return d;
        }
    }

    /**
     * スクロールバーが変更されたときに呼び出されるメソッド。 表示の最小値を計算する。
     * 
     * @param value scrollBarValueに相当する値
     * @param amount scrollVisibleAmountに相当する値
     * @return
     */
    protected double calcLowValue(double value, double amount) {
        double max = logicalMaxValue.get();
        double min = logicalMinValue.get();
        double diff = max - min;
        double bar = diff * amount;
        return min + (diff - bar) * value;
    }

    // ----------------------------------------------------------------------
    // layout
    // ----------------------------------------------------------------------

    protected static class AxisLabel {
        private Node node;// 複雑な形状のラベルを許可するために単にNode

        private boolean managed = false;

        private boolean beforeVisible = true;

        public Node getNode() {
            return node;
        }

        public void setNode(final Node node) {
            this.node = node;
        }

        private boolean isManaged() {
            return managed;
        }

        private void setManaged(final boolean b) {
            managed = true;
        }

        private boolean isBeforeVisible() {
            return beforeVisible;
        }

        private void setBeforeVisible(final boolean b) {
            beforeVisible = b;
        }

        private double id;

        /** 文字列等の比較以外で同値性を確認するための数値を設定する */
        public void setID(final double id) {
            this.id = id;
        }

        /** 文字列等の比較以外で同値性を確認するための数値を得る */
        public double getID() {
            return id;
        }

        /** 設定されているIDと等しいか調べる */
        public boolean match(final double id) {
            return this.id == id;
        }
    }

    private Group lineGroup, labelGroup = new Group();

    private Path majorTickPath, minorTickPath;

    private Line baseLine;

    private ScrollBar scroll;

    private Label nameLabel;

    private ObservableList<AxisLabel> labels;

    protected final ObservableList<AxisLabel> getLabels() {
        if (labels == null) {
            labels = FXCollections.observableArrayList();
            labels.addListener((ListChangeListener<AxisLabel>) c -> {
                final ObservableList<Node> list = labelGroup.getChildren();
                while (c.next()) {
                    for (final AxisLabel a1 : c.getRemoved()) {
                        list.remove(a1.getNode());
                        a1.setManaged(false);
                        a1.getNode().rotateProperty().unbind();
                    }
                    for (final AxisLabel a2 : c.getAddedSubList()) {
                        list.add(a2.getNode());
                        a2.getNode().setVisible(false);
                        a2.getNode().rotateProperty().bind(tickLabelRotate);
                    }
                }
            });
        }
        return labels;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected double computePrefWidth(final double height) {
        if (isHorizontal()) {
            return 150d;
        } else {
            final double w1 = linesPrefSize();
            layoutChildren(lastLayoutWidth, height);
            final double w2 = scroll.isVisible() ? scroll.getWidth() : 0;

            final double w3 = max(labelGroup.prefWidth(height), 10);
            double w4 = 0;
            if (nameLabel.isVisible()) {
                w4 = nameLabel.getHeight() + 5;
            }

            return w1 + w2 + w3 + w4;
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected double computePrefHeight(final double width) {
        if (isVertical()) {
            return 150d;
        } else {
            final double h1 = linesPrefSize();

            layoutChildren(width, lastLayoutHeight);
            final double h2 = scroll.isVisible() ? scroll.getHeight() : 0;
            final double h3 = max(labelGroup.prefHeight(width), 10);
            double h4 = 0;
            if (nameLabel.isVisible()) {
                h4 = nameLabel.getHeight() + 5;
            }

            return h1 + h2 + h3 + h4;
        }
    }

    /**
     * layoutChildrenから呼び出されます。 このAxisのレイアウトを実際に行うメソッドです。
     */
    protected void layoutAxis(final double width, final double height) {
        if (lineGroup == null) {
            lineGroup = new Group();
            lineGroup.setAutoSizeChildren(false);
            nameLabel = new Label();
            nameLabel.getStyleClass().add("axis-label");
            nameLabel.textProperty().bind(name);
            labelGroup.setAutoSizeChildren(false);
            majorTickPath = new Path();
            minorTickPath = new Path();
            baseLine = new Line();

            scroll = new ScrollBar();
            scroll.orientationProperty().bind(orientation);
            scroll.visibleProperty().bind(Bindings.createBooleanBinding(() -> scrollBarVisible
                    .get() && scrollBarValue.get() != -1 && scrollBarSize.get() != 1, scrollBarValue, scrollBarVisible, scrollBarSize));
            scroll.visibleProperty().addListener(layoutValidator);
            scroll.valueProperty().addListener(scrollValueValidator);
            scroll.setMin(0);
            scroll.setMax(1);
            scroll.visibleAmountProperty().bind(scrollBarSize);
            if (scrollBarValue.get() != -1 && scrollBarSize.get() != 1) {
                scroll.setValue(orientation.get() != Orientation.VERTICAL ? scrollBarValue.get() : 1 - scrollBarValue.get());
            }
            majorTickPath.getStyleClass().setAll("axis-tick-mark");
            minorTickPath.getStyleClass().setAll("axis-minor-tick-mark");
            baseLine.getStyleClass().setAll("axis-line");
            lineGroup.getChildren().addAll(majorTickPath, minorTickPath, baseLine);

            getChildren().addAll(lineGroup, labelGroup, scroll, nameLabel);
        }

        layoutLines(width, height);
        layoutLabels(width, height);
        layoutGroups(width, height);
    }

    private void layoutLabels(final double width, final double height) {
        if (labels == null) {
            return;
        }
        final ObservableList<AxisLabel> labels = this.labels;
        final double lastL = getAxisLength(lastLayoutWidth, lastLayoutHeight);
        final double l = getAxisLength(width, height);
        final boolean isH = isHorizontal();
        int firstIndex = -1;// 重なりを検出する基準位置
        Side s = side.get();
        if (isH) {
            if (s.isVertical()) {
                s = Side.BOTTOM;
            }
        } else {
            if (s.isHorizontal()) {
                s = Side.LEFT;
            }
        }

        for (int i = 0, e = majors.size(); i < e; i++) {
            final AxisLabel a = labels.get(i);
            final double d = majors.get(i);
            if (firstIndex == -1 && a.isManaged() && a.isBeforeVisible()) {
                firstIndex = i;
            }
            a.setManaged(true);
            // 位置を合わせる
            final Node n = a.getNode();
            n.setLayoutX(0);
            n.setLayoutY(0);
            final Bounds bounds = n.getBoundsInParent();
            if (isH) {
                final double cx = (bounds.getMinX() + bounds.getMaxX()) * 0.5;
                n.setLayoutX(d - cx);
                if (s == Side.BOTTOM) {
                    // 上を合わす
                    final double bottom = bounds.getMinY();
                    n.setLayoutY(-bottom);
                } else {
                    // 下を合わす
                    final double top = bounds.getMaxY();
                    n.setLayoutY(-top);
                }
            } else {
                final double cy = (bounds.getMinY() + bounds.getMaxY()) * 0.5;
                n.setLayoutY(d - cy);
                if (s == Side.LEFT) {
                    // 右端を合わす
                    final double right = bounds.getMaxX();
                    n.setLayoutX(-right);
                } else {
                    // 左端を合わす
                    final double left = bounds.getMinX();
                    n.setLayoutX(-left);
                }
            }
        }

        // 大きさが変わったときは前回の履歴を参照しない。
        if (lastL != l) {
            firstIndex = -1;
        }

        // 重なるラベルを不可視にする

        for (int k = 0; k < 2; k++) {
            // TODO 本当に重なっている領域で判定するようにしたい
            Bounds base = null;
            if (firstIndex != -1) {
                base = labels.get(firstIndex).getNode().getBoundsInParent();
            }
            final int end = k == 0 ? -1 : labels.size();
            int i = k == 0 ? firstIndex - 1 : firstIndex + 1;
            final int add = k == 0 ? -1 : 1;
            if (i < 0) {
                i = -1;
            }

            for (; i != end; i += add) {
                final AxisLabel a = labels.get(i);
                final Node n = a.getNode();
                final Bounds bounds = n.getBoundsInParent();
                if (base == null) {
                    n.setVisible(true);
                    a.setBeforeVisible(true);
                    base = bounds;
                } else {
                    final boolean visible = !base.intersects(bounds);
                    a.setBeforeVisible(visible);
                    n.setVisible(visible);
                    if (visible) {
                        base = bounds;
                    }
                }
            }
        }
    }

    private void layoutGroups(final double width, final double height) {
        final boolean ish = isHorizontal();
        final String n = name.get();
        nameLabel.setVisible(n != null && !n.isEmpty());
        if (ish) {
            nameLabel.setRotate(0);
            if (side.get() != Side.TOP) {// BOTTOM
                double y = 0;
                if (scroll.isVisible()) {
                    y = scroll.prefHeight(-1);
                    scroll.resizeRelocate(0, 0, width, y);
                }
                lineGroup.setLayoutX(0);
                lineGroup.setLayoutY(floor(y));
                y += lineGroup.prefHeight(-1) + tickLabelDistance.get();
                labelGroup.setLayoutX(0);
                labelGroup.setLayoutY(floor(y));
                if (nameLabel.isVisible()) {
                    y += max(labelGroup.prefHeight(-1) + 5, 15);
                    final double w = min(nameLabel.prefWidth(-1), width);
                    final double h = nameLabel.prefHeight(w);
                    nameLabel.resize(w, h);
                    nameLabel.relocate(floor((width - nameLabel.getWidth()) * 0.5), floor(y));
                }
            } else {
                double y = height;
                if (scroll.isVisible()) {
                    final double h = scroll.prefHeight(-1);
                    y -= h;
                    scroll.resizeRelocate(0, floor(y), width, h);
                }
                lineGroup.setLayoutX(0);
                lineGroup.setLayoutY(floor(y));
                y -= lineGroup.prefHeight(-1) + tickLabelDistance.get();
                labelGroup.setLayoutX(0);
                labelGroup.setLayoutY(floor(y));
                if (nameLabel.isVisible()) {
                    y -= max(labelGroup.prefHeight(-1) + 5, 15);
                    final double w = min(nameLabel.prefWidth(-1), width);
                    final double h = nameLabel.prefHeight(w);
                    nameLabel.resize(w, h);
                    nameLabel.relocate(floor((width - nameLabel.getWidth()) * 0.5), floor(y));
                }
            }
        } else {
            if (side.get() != Side.RIGHT) {// LEFT
                nameLabel.setRotate(-90);
                double x = width;
                if (scroll.isVisible()) {
                    final double w = scroll.prefWidth(-1);
                    x = x - w;
                    scroll.resizeRelocate(floor(x), 0, w, height);
                }

                lineGroup.setLayoutX(floor(x));
                lineGroup.setLayoutY(0);
                x -= tickLabelDistance.get() + lineGroup.prefWidth(-1);
                labelGroup.setLayoutX(floor(x));
                labelGroup.setLayoutY(0);
                if (nameLabel.isVisible()) {
                    final double w = min(nameLabel.prefWidth(-1), height);
                    final double h = nameLabel.prefHeight(w);
                    nameLabel.resize(w, h);
                    final Bounds b = nameLabel.getBoundsInParent();
                    x -= labelGroup.prefWidth(-1) + 5 + b.getWidth();
                    nameLabel.relocate(floor(x - b.getMinX() + nameLabel
                            .getLayoutX()), floor((height - b.getHeight()) * 0.5 - b.getMinY() + nameLabel.getLayoutY()));
                }
            } else {
                nameLabel.setRotate(90);
                double x = 0;
                if (scroll.isVisible()) {
                    final double w = scroll.prefWidth(-1);
                    x = w;
                    scroll.resizeRelocate(0, 0, w, height);
                }

                lineGroup.setLayoutX(floor(x));
                lineGroup.setLayoutY(0);
                x = tickLabelDistance.get() + lineGroup.prefWidth(-1);
                labelGroup.setLayoutX(floor(x));
                labelGroup.setLayoutY(0);
                if (nameLabel.isVisible()) {
                    final double w = min(nameLabel.prefWidth(-1), height);
                    final double h = nameLabel.prefHeight(w);
                    nameLabel.resize(w, h);
                    final Bounds b = nameLabel.getBoundsInParent();
                    x += max(labelGroup.prefWidth(-1) + 5, 15);
                    nameLabel.relocate(floor(x - b.getMinX() + nameLabel
                            .getLayoutX()), floor((height - b.getHeight()) * 0.5 - b.getMinY() + nameLabel.getLayoutY()));
                }
            }
        }

    }

    private double linesPrefSize() {
        return max(majorTickLength.get(), minorTickLength.get());
    }

    private void layoutLines(final double width, final double height) {

        final boolean ish = isHorizontal();
        final double l = getAxisLength(width, height);
        baseLine.setEndX(ish ? l : 0);
        baseLine.setEndY(ish ? 0 : l);

        final double al = majorTickLength.get();
        final double il = minorTickLength.get();
        final boolean isIV = il > 0 && minorTickVisibility.get();
        final Side s = side.get();
        final int k = ish ? s != Side.TOP ? 1 : -1 : s != Side.RIGHT ? -1 : 1;

        if (isIV) {
            final ObservableList<PathElement> elements = minorTickPath.getElements();
            if (elements.size() > minors.size() * 2) {
                elements.remove(minors.size() * 2, elements.size());
            }

            final int eles = elements.size();
            final int ls = minors.size();
            for (int i = 0; i < ls; i++) {
                final double d = minors.get(i);
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
                if (ish) {
                    x1 = x2 = d;
                    y1 = 0;
                    y2 = il * k;
                } else {
                    x1 = 0;
                    x2 = il * k;
                    y1 = y2 = d;
                }
                mt.setX(x1);
                mt.setY(y1);
                lt.setX(x2);
                lt.setY(y2);
            }
        } else {
            minorTickPath.setVisible(false);
        }

        final ObservableList<PathElement> elements = majorTickPath.getElements();
        if (elements.size() > majors.size() * 2) {
            elements.remove(majors.size() * 2, elements.size());
        }

        final int eles = elements.size();
        final int ls = majors.size();
        for (int i = 0; i < ls; i++) {
            final double d = majors.get(i);
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
            if (ish) {
                x1 = x2 = d;
                y1 = 0;
                y2 = al * k;
            } else {
                x1 = 0;
                x2 = al * k;
                y1 = y2 = d;
            }
            mt.setX(x1);
            mt.setY(y1);
            lt.setX(x2);
            lt.setY(y2);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void requestLayout() {
        final Parent p = getParent();
        if (p != null) {
            p.requestLayout();
        }
        super.requestLayout();
    }

}
