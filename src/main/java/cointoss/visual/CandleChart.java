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

import java.util.function.Consumer;

import javafx.beans.InvalidationListener;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.event.EventType;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.geometry.Point2D;
import javafx.geometry.Rectangle2D;
import javafx.geometry.Side;
import javafx.scene.control.Label;
import javafx.scene.layout.Region;

import cointoss.chart.Tick;
import cointoss.util.Num;
import cointoss.visual.shape.GraphShape;

/**
 * @version 2017/09/27 18:13:28
 */
public class CandleChart extends Region {

    /** The list of plottable line date. */
    public final ObservableList<CandleChartData> lines;

    /** The list of plottable cnadle date. */
    public final ObservableList<Tick> candles;

    /** The x-axis UI. */
    public final LinearAxis axisX = new LinearAxis();

    /** The y-axis UI. */
    public final LinearAxis axisY = new LinearAxis();

    /** The actual graph drawer. */
    public final GraphPlotArea graph = new GraphPlotArea();

    private final InvalidationListener dataValidateListener = observable -> {
        if (isDataValidate()) {
            setDataValidate(false);
            requestLayout();
        }
    };

    private boolean prelayout = false;

    /**
     * 
     */
    public CandleChart() {
        getStylesheets().add(getClass().getResource("CandleStickChart.css").toExternalForm());

        zoom.install(axisX);
        zoom.install(axisY);
        axisX.visualMinValue.addListener(dataValidateListener);
        axisX.visibleRange.addListener(dataValidateListener);
        axisY.visualMinValue.addListener(dataValidateListener);
        axisY.visibleRange.addListener(dataValidateListener);

        // create plotting data collection
        lines = FXCollections.observableArrayList();
        lines.addListener(dataValidateListener);
        lines.addListener((ListChangeListener<CandleChartData>) c -> {
            InvalidationListener listener = getLineChartDataListener();

            while (c.next()) {
                c.getRemoved().stream().map(CandleChartData::validateProperty).forEach(p -> p.removeListener(listener));
                c.getAddedSubList().stream().map(CandleChartData::validateProperty).forEach(p -> p.addListener(listener));

                if (isDataValidate()) {
                    setDataValidate(false);
                    setNeedsLayout(true);
                }
            }
        });

        candles = FXCollections.observableArrayList();
        candles.addListener(dataValidateListener);

        getStyleClass().setAll("chart");
        graph.setLineChartDataList(lines);
        graph.setCandleChartDataList(candles);
        graph.axisX.set(axisX);
        graph.axisY.set(axisY);
        graph.orientationProperty().bind(orientationProperty());
        graph.showHorizontalZeroLine();
        graph.showVerticalZeroLine();
        getChildren().addAll(graph, axisX, axisY);
    }

    /** Zoon functionality. */
    private final AxisZoomHandler zoom = new AxisZoomHandler();

    /**
     * Configure x-axis.
     * 
     * @param axis
     * @return Chainable API.
     */
    public final CandleChart axisX(Consumer<LinearAxis> axis) {
        axis.accept(this.axisX);

        return this;
    }

    /**
     * Configure y-axis.
     * 
     * @param axis
     * @return Chainable API.
     */
    public final CandleChart axisY(Consumer<LinearAxis> axis) {
        axis.accept(this.axisY);

        return this;
    }

    /**
     * Configure graph plot area.
     * 
     * @param graph
     * @return Chainable API.
     */
    public final CandleChart graph(Consumer<GraphPlotArea> graph) {
        graph.accept(this.graph);

        return this;
    }

    /**
     * @param x グラフの中での座標（グラフの値ではない）
     * @param y グラフの中での座標（グラフの値ではない）
     * @return
     */
    public Point2D getLocationOfGraph(final double x, final double y) {
        return graph.localToParent(x, y);
    }

    public Point2D getValueOfLocalLocation(final double x, final double y) {

        final Axis xAxis = axisX;
        final Axis yAxis = axisY;
        if (xAxis == null || yAxis == null) {
            return null;
        }
        final Point2D p = graph.parentToLocal(x, y);

        final double vx = xAxis.getValueForPosition(p.getX());
        final double vy = yAxis.getValueForPosition(p.getY());

        return new Point2D(vx, vy);
    }

    public void preLayout(final double x, final double y, final double width, final double height) {
        prelayout = true;
        resizeRelocate(x, y, width, height);
        layout();
        prelayout = false;
    }

    @Override
    protected final void layoutChildren() {
        final double w = getWidth();
        final double h = getHeight();
        if (w == -1 || h == -1) {
            return;
        }
        layoutChildren(w, h);
        setLayoutedSize(new Point2D(w, h));
    }

    protected void layoutChildren(double width, double height) {
        if (!isDataValidate()) {
            dealWithData();
        }
        final Insets in = getInsets();
        width -= in.getLeft() + in.getRight();
        height -= in.getTop() + in.getBottom();
        layoutChart(width, height, in.getLeft(), in.getTop());

    }

    protected void dealWithData() {
        setXAxisRange();
        setYAxisRange();
    }

    @Override
    protected double computePrefHeight(final double width) {
        return 150;
    }

    @Override
    protected double computePrefWidth(final double height) {
        return 150;
    }

    public final <T extends Event> void addEventHandlerToGraphArea(final EventType<T> eventType, final EventHandler<? super T> eventHandler) {
        graph.addEventHandler(eventType, eventHandler);
    }

    public final <T extends Event> void addEventFilterToGraphArea(final EventType<T> eventType, final EventHandler<? super T> eventFilter) {
        graph.addEventFilter(eventType, eventFilter);
    }

    public final <T extends Event> void removeEventHandlerFromGraphArea(final EventType<T> eventType, final EventHandler<? super T> eventHandler) {
        graph.removeEventHandler(eventType, eventHandler);
    }

    public final <T extends Event> void removeEventFilterFromGraphArea(final EventType<T> eventType, final EventHandler<? super T> eventFilter) {
        graph.removeEventFilter(eventType, eventFilter);
    }

    public final ObservableList<GraphShape> getForeGroundShapes() {
        return graph.getForeGroundShapes();
    }

    public final ObservableList<GraphShape> getBackGroundShapes() {
        return graph.getBackGroundShapes();
    }

    private static boolean isLeft(final Side s) {
        return s != Side.RIGHT;
    }

    private static boolean isBottom(final Side s) {
        return s != Side.TOP;
    }

    protected void layoutChart(final double w, final double h, final double x0, final double y0) {
        final Rectangle2D bounds = getPlotAreaPrefferedBounds();
        if (bounds == null) {
            layoutChartArea(w, h, x0, y0);
        } else {
            final double ww = bounds.getWidth();
            final double hh = bounds.getHeight();
            final boolean resized = ww != graph.getWidth() || hh != graph.getHeight();
            if (!prelayout) {
                graph.resizeRelocate(bounds.getMinX(), bounds.getMinY(), ww, hh);
            }
            setPlotAreaBounds(bounds);
            final Axis xAxis = axisX;
            final Axis yAxis = axisY;
            if (xAxis != null) {
                final double xh = xAxis.prefHeight(ww);
                xAxis.resize(ww, xh);
                if (isBottom(xAxis.side.get())) {
                    xAxis.relocate(bounds.getMinX(), bounds.getMaxY());
                } else {
                    xAxis.relocate(bounds.getMinX(), bounds.getMinY() - xh);
                }
            }
            if (yAxis != null) {
                final double yw = yAxis.prefWidth(hh);
                yAxis.resize(yw, hh);
                if (isLeft(yAxis.side.get())) {
                    yAxis.relocate(bounds.getMinX() - yw, bounds.getMinY());
                } else {
                    yAxis.relocate(bounds.getMaxX(), bounds.getMinY());
                }
            }
            if (resized || !isDataValidate()) {
                if (!prelayout) {
                    graph.plotData();
                    setDataValidate(true);
                }
            }
            if (!prelayout && !graph.isGraphShapeValidate()) {
                graph.drawGraphShapes();
            }
        }
    }

    protected void layoutChartArea(final double w, final double h, final double x0, final double y0) {
        if (axisX == null || axisY == null) {
            graph.setVisible(false);
        } else {
            graph.setVisible(true);
            final Axis xAxis = axisX;
            xAxis.orientation.set(Orientation.HORIZONTAL);
            if (xAxis.side.get().isVertical()) {
                xAxis.side.set(Side.BOTTOM);
            }

            final Axis yAxis = axisY;
            yAxis.orientation.set(Orientation.VERTICAL);
            if (yAxis.side.get().isHorizontal()) {
                yAxis.side.set(Side.LEFT);
            }
            double graphWidth, graphHeight;
            double xAxisHeight, yAxisWidth;
            if (xAxis.isVisible() && yAxis.isVisible()) {
                double gap = 10;
                int loop = 0;
                double xH = 0, yW = 0;
                while (gap > 5) {
                    double xaxisH = xAxis.prefHeight(w);
                    double yaxisW = yAxis.prefWidth(h - xaxisH);
                    final double xaxisH2 = xAxis.prefHeight(w - yaxisW);
                    gap = abs(xaxisH2 - xaxisH);
                    xaxisH = xaxisH2;
                    if (gap > 5) {
                        final double yaxisW2 = yAxis.prefWidth(h - xaxisH);
                        gap = abs(yaxisW - yaxisW2);
                        yaxisW = yaxisW2;
                    }
                    xH = xaxisH;
                    yW = yaxisW;
                    loop++;
                    if (loop == 5) {
                        break;
                    }
                }
                graphWidth = w - yW;
                graphHeight = h - xH;
                xAxisHeight = xH;
                yAxisWidth = yW;
            } else {
                if (xAxis.isVisible()) {
                    xAxisHeight = xAxis.prefHeight(w);
                    graphHeight = h - xAxisHeight;
                    graphWidth = w;
                    yAxisWidth = yAxis.prefWidth(graphHeight);
                } else if (yAxis.isVisible()) {
                    yAxisWidth = yAxis.prefWidth(h);
                    graphWidth = w - yAxisWidth;
                    graphHeight = h;
                    xAxisHeight = xAxis.prefHeight(graphWidth);
                } else {
                    xAxisHeight = xAxis.prefHeight(w);
                    yAxisWidth = yAxis.prefWidth(h);
                    graphWidth = w;
                    graphHeight = h;
                }
            }
            graphHeight = max(0, graphHeight);
            graphWidth = max(0, graphWidth);
            xAxis.resize(graphWidth, xAxisHeight);
            yAxis.resize(yAxisWidth, graphHeight);
            xAxis.layout();
            yAxis.layout();
            final boolean isLeft = yAxis.side.get() != Side.RIGHT;
            final boolean isBottom = xAxis.side.get() != Side.TOP;
            final double x = yAxis.isVisible() && isLeft ? yAxisWidth : 0, y = !xAxis.isVisible() || isBottom ? 0 : xAxisHeight;
            if (xAxis.isVisible()) {
                if (isBottom) {
                    xAxis.relocate(x + x0, graphHeight + y0);
                } else {
                    xAxis.relocate(x + x0, y0);
                }
            }
            if (yAxis.isVisible()) {
                if (isLeft) {
                    yAxis.relocate(x0, y + y0);
                } else {
                    yAxis.relocate(graphWidth + x0, y + y0);
                }
            }
            graph.relocate(x + x0, y + y0);
            final double oldgW = graph.getWidth();
            final double oldgH = graph.getHeight();
            final boolean resize = oldgW != graphWidth || oldgH != graphHeight;
            if (!prelayout) {
                if (resize) {
                    graph.resize(graphWidth, graphHeight);
                }
                if (resize || !isDataValidate()) {
                    graph.plotData();
                    setDataValidate(true);
                }
                if (!graph.isGraphShapeValidate()) {
                    graph.drawGraphShapes();
                }
            }

            setPlotAreaBounds(new Rectangle2D(x + x0, y + y0, graphWidth, graphHeight));
        }

    }

    private static double max(final double max, final double v) {
        return v != v || max >= v ? max : v;
    }

    /**
     * Set x-axis range.
     */
    private void setXAxisRange() {
        axisX.logicalMaxValue.set(candles.get(candles.size() - 1).start.toInstant().toEpochMilli());
        axisX.logicalMinValue.set(candles.get(0).start.toInstant().toEpochMilli());
    }

    /**
     * Set y-axis range.
     */
    private void setYAxisRange() {
        Num max = Num.MIN;
        Num min = Num.MAX;

        for (Tick tick : candles) {
            max = Num.max(max, tick.maxPrice);
            min = Num.min(min, tick.minPrice);
        }

        max = max.plus(200);
        min = min.minus(200);

        axisY.logicalMaxValue.set(max.toDouble());
        axisY.logicalMinValue.set(min.toDouble());
    }

    protected final boolean isDataValidate() {
        return datavalidate;
    }

    protected final void setDataValidate(final boolean bool) {
        datavalidate = bool;
    }

    /** 状態の正当性を示すプロパティ */
    private boolean datavalidate = false;

    private InvalidationListener lineChartDataListener;

    protected final InvalidationListener getLineChartDataListener() {
        if (lineChartDataListener == null) {
            lineChartDataListener = o -> {
                if (!((ReadOnlyBooleanProperty) o).get() && isDataValidate()) {
                    setDataValidate(false);
                    requestLayout();
                }
            };
        }
        return lineChartDataListener;
    }

    /**
     * x軸方向に連続なデータか、y軸方向に連続なデータかを指定するプロパティ
     * 
     * @return
     */
    public final ObjectProperty<Orientation> orientationProperty() {
        if (orientationProperty == null) {
            orientationProperty = new SimpleObjectProperty<>(this, "orientation", Orientation.HORIZONTAL);
        }
        return orientationProperty;
    }

    public final Orientation getOrientation() {
        return orientationProperty == null ? Orientation.HORIZONTAL : orientationProperty.get();
    }

    /**
     * @param orientation
     * @return
     */
    public final CandleChart orientation(Orientation orientation) {
        orientationProperty().set(orientation);

        return this;
    }

    private ObjectProperty<Orientation> orientationProperty;

    protected final InvalidationListener getLayoutInvalidationListener() {
        if (layoutInvalidationListener == null) {
            layoutInvalidationListener = observable -> requestLayout();
        }
        return layoutInvalidationListener;
    }

    private InvalidationListener layoutInvalidationListener = null;

    /**
     * GraphPlotAreaの最適な大きさと位置。 この値が指定されたときは、このノードの大きさにかかわらず、この値が利用される。
     * 
     * @return
     */
    public final ObjectProperty<Rectangle2D> plotAreaPrefferedBoundsProperty() {
        if (plotAreaPrefferedBoundsProperty == null) {
            plotAreaPrefferedBoundsProperty = new SimpleObjectProperty<>(this, "plotAreaPrefferedBounds", null);
            plotAreaPrefferedBoundsProperty.addListener(getLayoutInvalidationListener());
        }
        return plotAreaPrefferedBoundsProperty;
    }

    public final Rectangle2D getPlotAreaPrefferedBounds() {
        return plotAreaPrefferedBoundsProperty == null ? null : plotAreaPrefferedBoundsProperty.get();
    }

    public final void setPlotAreaPrefferedBounds(final Rectangle2D value) {
        plotAreaPrefferedBoundsProperty().set(value);
    }

    private ObjectProperty<Rectangle2D> plotAreaPrefferedBoundsProperty;

    /**
     * レイアウト結果のGraphPlotAreaの大きさと位置
     * 
     * @return
     */
    public final ReadOnlyObjectProperty<Rectangle2D> plotAreaBoundsProperty() {
        return plotAreaBoundsWrapper().getReadOnlyProperty();
    }

    public final Rectangle2D getPlotAreaBounds() {
        return plotAreaBoundsWrapper.get();
    }

    protected final void setPlotAreaBounds(final Rectangle2D value) {
        plotAreaBoundsWrapper().set(value);
    }

    protected final ReadOnlyObjectWrapper<Rectangle2D> plotAreaBoundsWrapper() {
        return plotAreaBoundsWrapper;
    }

    private final ReadOnlyObjectWrapper<Rectangle2D> plotAreaBoundsWrapper = new ReadOnlyObjectWrapper<>(this, "plotAreaBounds", null);

    /**
     * layoutChildrenが最後に実行されたときのこのノードの大きさ。 xが幅を表し、yが高さを表す。
     * 
     * @return
     */
    public final ReadOnlyObjectProperty<Point2D> layoutedSizeProperty() {
        return layoutedSizeWrapper().getReadOnlyProperty();
    }

    public final Point2D getLayoutedSize() {
        return layoutedSizeWrapper.get();
    }

    protected final void setLayoutedSize(final Point2D value) {
        layoutedSizeWrapper().set(value);
    }

    protected ReadOnlyObjectWrapper<Point2D> layoutedSizeWrapper() {
        return layoutedSizeWrapper;
    }

    private final ReadOnlyObjectWrapper<Point2D> layoutedSizeWrapper = new ReadOnlyObjectWrapper<>(this, "layoutedSize", new Point2D(0, 0));

    /**
     * グラフのタイトル。<br>
     * 単なるデータであり、表示はされない
     * 
     * @return
     */
    public final StringProperty titleProperty() {
        if (titleProperty == null) {
            titleProperty = new SimpleStringProperty(this, "title", null);
        }
        return titleProperty;
    }

    public final String getTitle() {
        return titleProperty == null ? null : titleProperty.get();
    }

    public final void setTitle(final String value) {
        titleProperty().set(value);
    }

    private StringProperty titleProperty;

    private Label titleLabel;

    public Label getTitleLabel() {
        if (titleLabel == null) {
            final Label l = new Label();
            l.getStyleClass().add("chart-title");
            l.textProperty().bind(titleProperty());
            titleLabel = l;
        }
        return titleLabel;
    }

    /**
     * @param graphTracker
     * @return
     */
    public final CandleChart graphTracker(GraphTracker graphTracker) {
        graphTracker.install(this);

        return this;
    }

    /**
     * @param data
     * @return
     */
    public CandleChart lineData(CandleChartData... data) {
        this.lines.addAll(data);

        return this;
    }

    /**
     * @param closePrice
     * @param maxPrice
     * @param minPrice
     * @return
     */
    public CandleChart candleDate(Iterable<Tick> data) {
        for (Tick tick : data) {
            this.candles.add(tick);
        }
        return this;
    }
}
