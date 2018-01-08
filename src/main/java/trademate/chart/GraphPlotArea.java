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

import java.util.ArrayList;
import java.util.List;

import javafx.beans.InvalidationListener;
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
import viewtify.Viewtify;

/**
 * @version 2018/01/09 0:28:48
 */
public class GraphPlotArea extends Region {

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

    private final Rectangle clip = new Rectangle();

    private final Group background = new LocalGroup();

    /** The line chart manager */
    private final Group lines = new LocalGroup();

    /** The candle chart manager */
    private final Group candles = new LocalGroup();

    /** Chart UI */
    private final LineMark backGridVertical;

    /** Chart UI */
    private final LineMark backGridHorizontal;

    /** Chart UI */
    private final LineMark mouseTrackVertical;

    /** Chart UI */
    private final LineMark mouseTrackHorizontal;

    /** Chart UI */
    private final LineMark notifyPrice;

    /** Chart UI */
    private final LineMark orderPrice;

    /** The line chart data list. */
    private ObservableList<Tick> candleChartData;

    /** The line chart data list. */
    private ObservableList<CandleChartData> lineChartData;

    /**
     * 
     */
    public GraphPlotArea(TradingView trade, Axis axisX, Axis axisY) {
        this.trade = trade;
        this.axisX = axisX;
        this.axisY = axisY;
        this.backGridVertical = new LineMark(ChartClass.BackGrid, axisX.forGrid, axisX);
        this.backGridHorizontal = new LineMark(ChartClass.BackGrid, axisY.forGrid, axisY);
        this.mouseTrackVertical = new LineMark(ChartClass.MouseTrack, axisX);
        this.mouseTrackHorizontal = new LineMark(ChartClass.MouseTrack, axisY);
        this.notifyPrice = new LineMark(ChartClass.PriceSignal, axisY);
        this.orderPrice = new LineMark(ChartClass.OrderSupport, axisY);

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

        getChildren()
                .addAll(backGridVertical, backGridHorizontal, notifyPrice, orderPrice, mouseTrackHorizontal, mouseTrackVertical, background, candles, lines);
    }

    /**
     * Make this graph invalidate.
     */
    private void invalidate() {
        if (plotValidate) {
            plotValidate = false;
            setNeedsLayout(true);
        }
    }

    /**
     * Provide mouse tracker.
     */
    private void provideMouseTracker() {
        TickLable labelX = mouseTrackVertical.createLabel();
        TickLable labelY = mouseTrackHorizontal.createLabel();

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
            for (TickLable mark : notifyPrice.labels) {
                if (Num.of(mark.value.get()).isNear(price, 500)) {
                    notifyPrice.remove(mark);
                    return;
                }
            }

            TickLable label = notifyPrice.createLabel(price);

            label.add(trade.market().signalByPrice(price).to(exe -> {
                notificator.priceSignal.notify("Rearch to " + price);
                notifyPrice.remove(label);
            }));
        });
    }

    /**
     * Provide order support.
     */
    private void provideOrderSupport() {
        trade.market().yourOrder.on(Viewtify.UIThread).to(o -> {
            TickLable label = orderPrice.createLabel(o.price);

            o.state.observe().take(State.CANCELED, State.COMPLETED).take(1).on(Viewtify.UIThread).to(() -> {
                orderPrice.remove(label);
            });
        });
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void layoutChildren() {
        if (!plotValidate) {
            plotData();
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
            return;
        }

        if (!plotValidate) {
            // draw back lines
            backGridVertical.draw();
            backGridHorizontal.draw();
            mouseTrackVertical.draw();
            mouseTrackHorizontal.draw();
            notifyPrice.draw();
            orderPrice.draw();

            plotLineChartDatas();
            plotCandleChartDatas();
            plotValidate = true;
        }
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

    /**
     * Set data list for line chart.
     * 
     * @param datalist
     */
    public final void setLineChartDataList(ObservableList<CandleChartData> datalist) {
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
     * @version 2018/01/09 0:19:26
     */
    private class LineMark extends Path {

        /** The class name. */
        private final ChartClass className;

        /** The model. */
        private final List<TickLable> labels;

        /** The associated axis. */
        private final Axis axis;

        /**
         * @param className
         */
        private LineMark(ChartClass className, Axis axis) {
            this(className, new ArrayList(), axis);
        }

        /**
         * @param className
         * @param labels
         */
        private LineMark(ChartClass className, List<TickLable> labels, Axis axis) {
            this.className = className;
            this.labels = labels;
            this.axis = axis;

            getStyleClass().addAll(className.name(), ChartClass.Line.name());
        }

        /**
         * Create new mark.
         * 
         * @return
         */
        private TickLable createLabel() {
            return createLabel(null);
        }

        /**
         * Create new mark.
         * 
         * @return
         */
        private TickLable createLabel(Num price) {
            TickLable label = axis.createLabel(className);
            if (price != null) label.value.set(price.toDouble());
            labels.add(label);

            invalidate();

            return label;
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

        /**
         * Draw mark.
         */
        private void draw() {
            ObservableList<PathElement> paths = getElements();
            int pathSize = paths.size();
            int labelSize = labels.size();

            if (pathSize > labelSize * 2) {
                paths.remove(labelSize * 2, pathSize);
                pathSize = labelSize * 2;
            }

            for (int i = 0; i < labelSize; i++) {
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

                double value = labels.get(i).position();

                if (axis.isHorizontal()) {
                    move.setX(value);
                    move.setY(0);
                    line.setX(value);
                    line.setY(getHeight());
                } else {
                    move.setX(0);
                    move.setY(value);
                    line.setX(getWidth());
                    line.setY(value);
                }
            }
        }
    }
}
