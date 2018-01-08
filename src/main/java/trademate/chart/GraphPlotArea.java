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

import java.util.BitSet;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.IntToDoubleFunction;

import javafx.beans.InvalidationListener;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.value.ChangeListener;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.layout.Region;
import javafx.scene.shape.LineTo;
import javafx.scene.shape.MoveTo;
import javafx.scene.shape.Path;
import javafx.scene.shape.PathElement;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.StrokeLineJoin;

import cointoss.Order.State;
import cointoss.chart.Tick;
import cointoss.util.Num;
import kiss.I;
import trademate.Notificator;
import trademate.TradingView;
import trademate.chart.Axis.TickLable;
import trademate.chart.shape.Candle;
import trademate.chart.shape.GraphShape;

/**
 * @version 2018/01/05 20:35:03
 */
public class GraphPlotArea extends Region {

    /** The visibility of horizontal grid line. */
    public final BooleanProperty horizontalGridLineVisibility = new SimpleBooleanProperty(this, "horizontalGridLinesVisible", true);

    /** The visibility of vertical grid line. */
    public final BooleanProperty verticalGridLineVisibility = new SimpleBooleanProperty(this, "verticalGridLineVisibility", true);

    final Axis axisX;

    /** The vertical axis. */
    final Axis axisY;

    /** The current market. */
    private final TradingView trade;

    /** The notificator. */
    private final Notificator notificator = I.make(Notificator.class);

    /** The validity flag of plotting. */
    private boolean plotValidate = false;

    /** The validator. */
    private final InvalidationListener plotValidateListener = observable -> invalidate();

    /** The validator. */
    private final ChangeListener<Axis> axisListener = (observable, oldValue, newValue) -> {
        if (oldValue != null) {
            oldValue.scroll.valueProperty().removeListener(plotValidateListener);
            oldValue.scroll.visibleAmountProperty().removeListener(plotValidateListener);
        }

        if (newValue != null) {
            newValue.scroll.valueProperty().addListener(plotValidateListener);
            newValue.scroll.visibleAmountProperty().addListener(plotValidateListener);
        }
        if (plotValidate) {
            plotValidate = false;
            requestLayout();
        }
    };

    private final Rectangle clip = new Rectangle();

    private final Group background = new LocalGroup();

    /** The line chart manager */
    private final Group lines = new LocalGroup();

    /** The candle chart manager */
    private final Group candles = new LocalGroup();

    private final Group foreground = new LocalGroup();

    private final Group userBackround = new LocalGroup();

    private final Group userForeground = new LocalGroup();

    private final Path verticalGridLines = new Path();

    private final HorizontalMark horizontalGridLines = new HorizontalMark(ChartClass.GridLine);

    /** The line chart color manager. */
    private final BitSet lineColorManager = new BitSet(8);

    /** The price signal line. */
    private final HorizontalMark priceSignal = new HorizontalMark(ChartClass.PriceSignal);

    /** The price signal line. */
    private final HorizontalMark orders = new HorizontalMark(ChartClass.OrderSupport);

    /** The line chart data list. */
    private ObservableList<Tick> candleChartData;

    /** The line chart data list. */
    private ObservableList<CandleChartData> lineChartData;

    /** The line chart data list observer. */
    private final ListChangeListener<CandleChartData> lineDataListObserver = change -> {
        change.next();
        for (CandleChartData d : change.getRemoved()) {
            lineColorManager.clear(d.defaultColorIndex);
        }
        for (CandleChartData d : change.getAddedSubList()) {
            d.defaultColorIndex = lineColorManager.nextClearBit(0);
            lineColorManager.set(d.defaultColorIndex, true);
            d.defaultColor = "default-color" + (d.defaultColorIndex % 8);
        }
    };

    /**
     * 
     */
    public GraphPlotArea(TradingView trade, Axis axisX, Axis axisY) {
        this.trade = trade;
        this.axisX = axisX;
        this.axisY = axisY;

        axisX.scroll.valueProperty().addListener(plotValidateListener);
        axisX.scroll.visibleAmountProperty().addListener(plotValidateListener);
        axisY.scroll.valueProperty().addListener(plotValidateListener);
        axisY.scroll.visibleAmountProperty().addListener(plotValidateListener);

        getStyleClass().setAll("chart-plot-background");
        widthProperty().addListener(plotValidateListener);
        heightProperty().addListener(plotValidateListener);
        clip.widthProperty().bind(widthProperty());
        clip.heightProperty().bind(heightProperty());
        setClip(clip);

        provideMouseTracker();
        providePriceSignal();
        provideOrderSupport();

        verticalGridLines.getStyleClass().setAll("chart-vertical-grid-line");
        getChildren()
                .addAll(verticalGridLines, horizontalGridLines.path, priceSignal.path, orders.path, background, userBackround, candles, lines, foreground, userForeground);
    }

    /**
     * Make this graph invalidate.
     */
    private void invalidate() {
        if (plotValidate) {
            plotValidate = false;
            graphshapeValidate = false;
            setNeedsLayout(true);
        }
    }

    /**
     * Provide mouse tracker.
     */
    private void provideMouseTracker() {
        TickLable labelX = axisX.createLabel(ChartClass.MouseTrack);
        TickLable labelY = axisY.createLabel(ChartClass.MouseTrack);

        // track on move
        setOnMouseMoved(e -> {
            labelX.value.set(axisX.getValueForPosition(e.getX()));
            labelY.value.set(axisY.getValueForPosition(e.getY()));

            invalidate();
        });

        // remove on exit
        setOnMouseExited(e -> {
            labelX.value.set(-1);
            labelY.value.set(-1);

            invalidate();
        });
    }

    /**
     * Provide price signal.
     */
    private void providePriceSignal() {
        setOnContextMenuRequested(e -> {
            Num price = Num.of(Math.floor(axisY.getValueForPosition(e.getY())));

            // check price range to add or remove
            for (TickLable mark : priceSignal.labels) {
                if (Num.of(mark.value.get()).isNear(price, 500)) {
                    priceSignal.remove(mark);
                    return;
                }
            }

            // create new mark
            TickLable label = axisY.createLabel(ChartClass.PriceSignal);
            label.value.set(price.toDouble());
            priceSignal.add(label);

            label.add(trade.market().signalByPrice(price).to(exe -> {
                notificator.priceSignal.notify("Rearch to " + price);
                priceSignal.remove(label);
            }));
        });
    }

    /**
     * Provide order support.
     */
    private void provideOrderSupport() {
        trade.market().yourOrder.to(o -> {

            TickLable label = axisY.createLabel(ChartClass.OrderSupport);
            label.value.set(o.price.toDouble());

            orders.add(label);

            o.state.observe().take(State.CANCELED, State.COMPLETED).take(1).to(() -> {
                orders.remove(label);
            });
        });
    }

    /**
     * ユーザが任意に使える背景領域
     * 
     * @return
     */
    public final ObservableList<Node> getBackgroundChildren() {
        return userBackround.getChildren();
    }

    /**
     * ユーザが任意に使える前景領域
     * 
     * @return
     */
    public final ObservableList<Node> getForegroundChildren() {
        return userForeground.getChildren();
    }

    @Override
    protected void layoutChildren() {
        if (!plotValidate) {
            plotData();
        }
        if (!graphshapeValidate) {
            drawGraphShapes();
        }
    }

    /**
     * Draw plot data.
     */
    public void plotData() {
        final Axis xaxis = axisX;
        final Axis yaxis = axisY;

        if (xaxis == null || yaxis == null) {
            plotValidate = true;
            graphshapeValidate = true;
            return;
        }

        drawGraphShapes();

        if (!plotValidate) {
            drawBackGroundLine();
            plotLineChartDatas();
            plotCandleChartDatas();
            plotValidate = true;
        }
    }

    public void drawGraphShapes() {
        if (plotValidate && graphshapeValidate) {
            return;
        }
        final Axis xaxis = axisX;
        final Axis yaxis = axisY;

        if (xaxis == null || yaxis == null) {
            graphshapeValidate = true;
            return;
        }

        final double w = getWidth(), h = getHeight();
        if (w != xaxis.getWidth() || h != yaxis.getHeight()) {
            return;
        }
        List<GraphShape> lines = backGroundShapes;
        if (lines != null) {

            for (final GraphShape gl : lines) {
                gl.setNodeProperty(xaxis, yaxis, w, h);
            }
        }
        lines = foreGroundShapes;
        if (lines != null) {
            for (final GraphShape gl : lines) {
                gl.setNodeProperty(xaxis, yaxis, w, h);
            }
        }
        graphshapeValidate = true;
    }

    /**
     * Draw background lines.
     */
    private void drawBackGroundLine() {
        Axis axisX = this.axisX;
        Axis axisY = this.axisY;
        double width = getWidth();
        double height = getHeight();

        // vertical lines
        vertical: {
            boolean visible = verticalGridLineVisibility.get();
            ObservableList<PathElement> paths = verticalGridLines.getElements();
            int pathSize = paths.size();
            int tickSize = axisX.tickSize();

            // update visibility
            verticalGridLines.setVisible(visible);

            if (!visible) {
                paths.clear();
                break vertical;
            } else if (pathSize > tickSize * 2) {
                paths.remove(tickSize * 2, pathSize);
                pathSize = tickSize * 2;
            }

            for (int i = 0; i < tickSize; i++) {
                double d = axisX.labelAt(i).position();
                MoveTo mt;
                LineTo lt;
                if (i * 2 < pathSize) {
                    mt = (MoveTo) paths.get(i * 2);
                    lt = (LineTo) paths.get(i * 2 + 1);
                } else {
                    mt = new MoveTo();
                    lt = new LineTo();
                    paths.addAll(mt, lt);
                }
                mt.setX(d);
                mt.setY(0);
                lt.setX(d);
                lt.setY(height);
            }
        }

        // horizontal lines
        horizontalGridLines
                .draw(axisY.forGrid.size(), index -> axisY.forGrid.get(index).position(), horizontalGridLineVisibility.get(), width);

        // horizontal marks
        priceSignal.draw(width);
        orders.draw(width);
    }

    /**
     * Draw line chart.
     */
    protected void plotLineChartDatas() {
        ObservableList<Node> paths = lines.getChildren();
        List<CandleChartData> datas = lineChartData;

        if (datas == null) {
            paths.clear();
        } else {
            int sizeData = datas.size();
            int sizePath = paths.size();

            if (sizeData < sizePath) {
                paths.remove(sizeData, sizePath);
                sizePath = sizeData;
            }

            for (int i = 0; i < sizeData; i++) {
                int defaultColorIndex = 2;
                CandleChartData data = datas.get(i);

                Path path;

                if (i < sizePath) {
                    path = (Path) paths.get(i);
                } else {
                    path = new Path();
                    path.setStrokeLineJoin(StrokeLineJoin.BEVEL);
                    path.setStrokeWidth(0.6);
                    path.getStyleClass().setAll("chart-series-line", "series" + i, data.defaultColor);
                    paths.add(path);
                }

                ObservableList<String> className = path.getStyleClass();

                if (!className.get(defaultColorIndex).equals(data.defaultColor)) {
                    className.set(defaultColorIndex, data.defaultColor);
                }
                plotLineChartData(data, path);
            }
        }
    }

    /**
     * Draw line chart.
     * 
     * @param width
     * @param height
     */
    protected void plotCandleChartDatas() {
        ObservableList<Node> nodes = candles.getChildren();
        List<Tick> datas = candleChartData;

        if (datas == null) {
            nodes.clear();
        } else {
            int sizeData = datas.size();
            int sizePath = nodes.size();

            if (sizeData < sizePath) {
                nodes.remove(sizeData, sizePath);
                sizePath = sizeData;
            }

            Num min = Num.MAX;
            Axis xAxis = axisX;
            long start = (long) xAxis.computeVisibleMinValue();
            long end = (long) xAxis.computeVisibleMaxValue();

            for (int i = 0; i < sizeData; i++) {
                Tick data = datas.get(i);
                long time = data.start.toInstant().toEpochMilli();

                if (start <= time && time <= end) {
                    min = Num.min(min, data.minPrice);

                    Candle candle;

                    if (i < sizePath) {
                        candle = (Candle) nodes.get(i);
                    } else {
                        candle = new Candle("series" + i, "data" + i);
                        nodes.add(candle);
                    }
                    plotCandleChartData(data.start.toInstant().toEpochMilli(), data, candle);
                } else {
                    if (i < sizePath) {
                        Candle candle = (Candle) nodes.get(i);
                        candle.setLayoutX(-10);
                        candle.setLayoutY(-10);
                    }
                }
            }
        }
    }

    private final double DISTANCE_THRESHOLD = 0.5;

    /**
     * Draw chart data.
     * 
     * @param data
     * @param path
     */
    protected void plotLineChartData(CandleChartData data, Path path) {
        Axis axis = axisX;
        double min = axis.computeVisibleMinValue();
        double max = axis.computeVisibleMaxValue();
        int start = data.searchXIndex(min, false);
        int end = data.searchXIndex(max, true);

        start = Math.max(0, start - 2);

        plotLineChartData(data, path.getElements(), start, end);
    }

    /**
     * Draw chart data.
     * 
     * @param data
     * @param elements
     * @param start
     * @param end
     */
    private void plotLineChartData(CandleChartData data, ObservableList<PathElement> elements, int start, int end) {
        int elementSize = elements.size();
        Axis xaxis = axisX;
        Axis yaxis = axisY;

        boolean moveTo = true;
        double beforeX = 0, beforeY = 0;
        int elementIndex = 0;
        for (int i = start; i <= end; i++) {
            double x = data.getX(i);
            double y = data.getY(i);

            // 座標変換
            x = xaxis.getPositionForValue(x);
            y = yaxis.getPositionForValue(y);

            if (moveTo) {// 線が途切れている場合
                if (elementIndex < elementSize) {
                    PathElement pathElement = elements.get(elementIndex);
                    if (pathElement.getClass() == MoveTo.class) {// 再利用
                        MoveTo m = ((MoveTo) pathElement);
                        m.setX(x);
                        m.setY(y);
                    } else {
                        MoveTo m = new MoveTo(x, y);
                        elements.set(elementIndex, m);// 置換
                    }
                    elementIndex++;
                } else {
                    MoveTo m = new MoveTo(x, y);
                    elements.add(m);
                }
                moveTo = false;
                beforeX = x;
                beforeY = y;
            } else {// 線が続いている場合
                double l = Math.hypot(x - beforeX, y - beforeY);
                // 距離が小さすぎる場合は無視
                if (l < DISTANCE_THRESHOLD) {
                    continue;
                }
                if (elementIndex < elementSize) {
                    final PathElement pathElement = elements.get(elementIndex);
                    if (pathElement.getClass() == LineTo.class) {
                        LineTo m = ((LineTo) pathElement);
                        m.setX(x);
                        m.setY(y);
                    } else {
                        LineTo m = new LineTo(x, y);
                        elements.set(elementIndex, m);
                    }
                    elementIndex++;
                } else {
                    LineTo m = new LineTo(x, y);
                    elements.add(m);
                }
                beforeX = x;
                beforeY = y;
            }
        } // end for

        if (elementIndex < elementSize) {
            elements.remove(elementIndex, elementSize);
        }

    }

    /**
     * Draw chart data.
     * 
     * @param data
     * @param candle
     * @param width
     * @param height
     */
    protected void plotCandleChartData(long index, Tick data, Candle candle) {
        double x = axisX.getPositionForValue(index);
        double open = axisY.getPositionForValue(data.openPrice.toDouble());
        double close = axisY.getPositionForValue(data.closePrice.toDouble());
        double high = axisY.getPositionForValue(data.maxPrice.toDouble());
        double low = axisY.getPositionForValue(data.minPrice.toDouble());

        // calculate candle width
        double candleWidth = 3;

        // update candle
        candle.update(close - open, high - open, low - open, candleWidth);
        // candle.updateTooltip(item.getYValue().doubleValue(), extra.getClose(), extra.getHigh(),
        // extra.getLow());

        // position the candle
        candle.setLayoutX(x);
        candle.setLayoutY(open);
    }

    boolean graphshapeValidate = true;

    private InvalidationListener graphShapeValidateListener = o -> {
        final ReadOnlyBooleanProperty p = (ReadOnlyBooleanProperty) o;
        final boolean b = p.get();
        if (!b && graphshapeValidate) {
            graphshapeValidate = false;
            setNeedsLayout(true);

        }
    };

    private ObservableList<GraphShape> backGroundShapes, foreGroundShapes;

    public final ObservableList<GraphShape> getBackGroundShapes() {
        if (backGroundShapes == null) {
            backGroundShapes = FXCollections.observableArrayList();
            final ListChangeListener<GraphShape> l = (c) -> {
                c.next();
                final Group g = background;
                final ObservableList<Node> ch = g.getChildren();
                for (final GraphShape gl : c.getRemoved()) {
                    final Node node = gl.getNode();
                    if (node != null) {
                        ch.remove(node);
                    }
                    gl.validateProperty().removeListener(graphShapeValidateListener);
                }
                for (final GraphShape gl : c.getAddedSubList()) {
                    final Node node = gl.getNode();
                    if (node != null) {
                        ch.add(gl.getNode());
                    }
                    gl.validateProperty().addListener(graphShapeValidateListener);
                }
                if (plotValidate) {
                    graphshapeValidate = false;
                    plotValidate = false;
                    setNeedsLayout(true);
                }
            };
            backGroundShapes.addListener(l);
        }
        return backGroundShapes;
    }

    public final ObservableList<GraphShape> getForeGroundShapes() {
        if (foreGroundShapes == null) {
            foreGroundShapes = FXCollections.observableArrayList();
            final ListChangeListener<GraphShape> l = c -> {
                c.next();
                final Group g = foreground;
                final ObservableList<Node> ch = g.getChildren();
                for (final GraphShape gl : c.getRemoved()) {
                    final Node node = gl.getNode();
                    if (node != null) {
                        ch.remove(node);
                    }
                    gl.validateProperty().removeListener(graphShapeValidateListener);
                }
                for (final GraphShape gl : c.getAddedSubList()) {
                    final Node node = gl.getNode();
                    if (node != null) {
                        ch.add(gl.getNode());
                    }
                    gl.validateProperty().addListener(graphShapeValidateListener);
                }
                if (plotValidate) {
                    graphshapeValidate = false;
                    requestLayout();
                }
            };
            foreGroundShapes.addListener(l);
        }
        return foreGroundShapes;
    }

    /**
     * Set data list for line chart.
     * 
     * @param datalist
     */
    public final void setLineChartDataList(ObservableList<CandleChartData> datalist) {
        // clear old list configuration
        if (lineChartData != null) {
            lineChartData.removeListener(lineDataListObserver);
            lineColorManager.clear();
        }

        // add new list configuration
        if (datalist != null) {
            datalist.addListener(lineDataListObserver);
            for (CandleChartData data : datalist) {
                data.defaultColorIndex = lineColorManager.nextClearBit(0);
                lineColorManager.set(data.defaultColorIndex, true);
                data.defaultColor = "default-color" + (data.defaultColorIndex % 8);
            }
        }

        // update
        lineChartData = datalist;

        if (plotValidate) {
            plotValidate = false;
            setNeedsLayout(true);
        }
    }

    private final InvalidationListener dataChangeObserver = observalbe -> {
        if (plotValidate) {
            plotValidate = false;
            setNeedsLayout(true);
        }
    };

    /**
     * Set data list for line chart.
     * 
     * @param datalist
     */
    public final void setCandleChartDataList(ObservableList<Tick> datalist) {
        // clear old list configuration
        if (candleChartData != null) {
            candleChartData.removeListener(dataChangeObserver);
        }

        // add new list configuration
        if (datalist != null) {
            datalist.addListener(dataChangeObserver);
        }

        // update
        candleChartData = datalist;

        if (plotValidate) {
            plotValidate = false;
            setNeedsLayout(true);
        }
    }

    /**
     * Get data for line chart.
     * 
     * @return
     */
    public final ObservableList<CandleChartData> getLineDataList() {
        return lineChartData;
    }

    /**
     * Get data for candle chart.
     * 
     * @return
     */
    public final ObservableList<Tick> getCandleDataList() {
        return candleChartData;
    }

    /**
     * @version 2017/09/26 1:20:10
     */
    private final class LocalGroup extends Group {

        /**
         * 
         */
        private LocalGroup() {
            setAutoSizeChildren(false);
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public void requestLayout() {
        }
    }

    /**
     * @version 2018/01/05 21:28:11
     */
    private class HorizontalMark {

        /** The class name. */
        private final ChartClass className;

        /** The user interface. */
        private final Path path = new Path();

        /** The model. */
        private final List<TickLable> labels = new CopyOnWriteArrayList();

        /**
         * @param className
         */
        private HorizontalMark(ChartClass className) {
            this.className = className;
            this.path.getStyleClass().addAll(className.name(), ChartClass.Line.name());
        }

        private void addMark(Num value) {
            addMark(value.toDouble());
        }

        /**
         * Add mark at the specified value.
         * 
         * @param value
         */
        private void addMark(double value) {
            TickLable label = axisY.createLabel(className);
            label.value.set(value);

            labels.add(label);
            invalidate();
        }

        /**
         * Add mark.
         * 
         * @param mark
         */
        private void add(TickLable mark) {
            labels.add(mark);
            invalidate();
        }

        /**
         * Dispose mark.
         * 
         * @param mark
         */
        private void remove(TickLable mark) {
            labels.remove(mark);
            mark.dispose();
            invalidate();
        }

        private void draw(double width) {
            draw(labels.size(), index -> Math.floor(labels.get(index).position()), true, width);
        }

        private void draw(int size, IntToDoubleFunction positionAdviser, boolean visible, double width) {
            ObservableList<PathElement> paths = path.getElements();
            int pathSize = paths.size();

            if (!visible) {
                paths.clear();
                return;
            } else if (pathSize > size * 2) {
                paths.remove(size * 2, pathSize);
                pathSize = size * 2;
            }

            for (int i = 0; i < size; i++) {
                MoveTo move;
                LineTo line;

                if (i * 2 < pathSize) {
                    move = (MoveTo) paths.get(i * 2);
                    line = (LineTo) paths.get(i * 2 + 1);
                } else {
                    move = new MoveTo();
                    line = new LineTo();
                    paths.addAll(move, line);
                }

                double value = positionAdviser.applyAsDouble(i);
                move.setX(0);
                move.setY(value);
                line.setX(width);
                line.setY(value);
            }
        }
    }
}
