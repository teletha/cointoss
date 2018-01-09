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
import javafx.scene.input.ContextMenuEvent;
import javafx.scene.layout.Region;
import javafx.scene.shape.LineTo;
import javafx.scene.shape.MoveTo;
import javafx.scene.shape.Path;
import javafx.scene.shape.PathElement;
import javafx.scene.shape.StrokeLineJoin;

import org.eclipse.collections.impl.map.mutable.primitive.IntObjectHashMap;

import cointoss.Order.State;
import cointoss.Side;
import cointoss.chart.Tick;
import cointoss.util.Num;
import kiss.I;
import trademate.Notificator;
import trademate.TradingView;
import trademate.chart.Axis.TickLable;
import trademate.chart.shape.Candle;
import viewtify.Viewtify;
import viewtify.ui.helper.StyleHelper;

/**
 * @version 2018/01/09 0:28:48
 */
public class ChartPlotArea extends Region {

    final Axis axisX;

    /** The vertical axis. */
    final Axis axisY;

    /** The current market. */
    private final TradingView trade;

    /** The notificator. */
    private final Notificator notificator = I.make(Notificator.class);

    /** The validity flag of plotting. */
    private boolean plotIsValid = false;

    /** The validator. */
    private final InvalidationListener plotValidateListener = observable -> invalidate();

    /** The line chart manager */
    private final Group lines = new LocalGroup();

    /** The candle chart manager */
    private final IntObjectHashMap<Candle> candles = new IntObjectHashMap<>();

    /** Chart UI */
    private final Group candleGraph = new LocalGroup();

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
    private final LineMark orderBuyPrice;

    /** Chart UI */
    private final LineMark orderSellPrice;

    /** The line chart data list. */
    private ObservableList<Tick> candleChartData;

    /** The line chart data list. */
    private ObservableList<CandleChartData> lineChartData;

    /**
     * @param trade
     * @param axisX
     * @param axisY
     */
    public ChartPlotArea(TradingView trade, Axis axisX, Axis axisY) {
        this.trade = trade;
        this.axisX = axisX;
        this.axisY = axisY;
        this.backGridVertical = new LineMark(axisX.forGrid, axisX, ChartClass.BackGrid);
        this.backGridHorizontal = new LineMark(axisY.forGrid, axisY, ChartClass.BackGrid);
        this.mouseTrackVertical = new LineMark(axisX, ChartClass.MouseTrack);
        this.mouseTrackHorizontal = new LineMark(axisY, ChartClass.MouseTrack);
        this.notifyPrice = new LineMark(axisY, ChartClass.PriceSignal);
        this.orderBuyPrice = new LineMark(axisY, ChartClass.OrderSupport, Side.BUY);
        this.orderSellPrice = new LineMark(axisY, ChartClass.OrderSupport, Side.SELL);

        widthProperty().addListener(plotValidateListener);
        heightProperty().addListener(plotValidateListener);
        axisX.scroll.valueProperty().addListener(plotValidateListener);
        axisX.scroll.visibleAmountProperty().addListener(plotValidateListener);
        axisY.scroll.valueProperty().addListener(plotValidateListener);
        axisY.scroll.visibleAmountProperty().addListener(plotValidateListener);

        Viewtify.clip(this);
        Viewtify.clip(backGridVertical);
        Viewtify.clip(backGridHorizontal);

        visualizeMouseTrack();
        visualizeNotifyPrice();
        visualizeOrderPrice();

        getChildren()
                .addAll(backGridVertical, backGridHorizontal, notifyPrice, orderBuyPrice, orderSellPrice, mouseTrackHorizontal, mouseTrackVertical, candleGraph, lines);
    }

    /**
     * Make this graph invalidate.
     */
    private void invalidate() {
        if (plotIsValid) {
            plotIsValid = false;
            setNeedsLayout(true);
        }
    }

    /**
     * Visualize mouse tracker in chart.
     */
    private void visualizeMouseTrack() {
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
     * Visualize notifiable price in chart.
     */
    private void visualizeNotifyPrice() {
        addEventHandler(ContextMenuEvent.CONTEXT_MENU_REQUESTED, e -> {
            Num price = Num.of(Math.floor(axisY.getValueForPosition(e.getY())));

            // check price range to add or remove
            for (TickLable mark : notifyPrice.labels) {
                if (Num.of(mark.value.get()).isNear(price, 500)) {
                    notifyPrice.remove(mark);
                    return;
                }
            }

            TickLable label = notifyPrice.createLabel(price);

            label.add(trade.market().signalByPrice(price).on(Viewtify.UIThread).to(exe -> {
                notificator.priceSignal.notify("Rearch to " + price);
                notifyPrice.remove(label);
            }));
        });
    }

    /**
     * Visualize order price in chart.
     */
    private void visualizeOrderPrice() {
        trade.market().yourOrder.on(Viewtify.UIThread).to(o -> {
            LineMark mark = o.isBuy() ? orderBuyPrice : orderSellPrice;
            TickLable label = mark.createLabel(o.price);

            o.state.observe().take(State.CANCELED, State.COMPLETED).take(1).on(Viewtify.UIThread).to(() -> {
                mark.remove(label);
            });
        });
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void layoutChildren() {
        if (!plotIsValid) {
            // draw back lines
            backGridVertical.draw();
            backGridHorizontal.draw();
            mouseTrackVertical.draw();
            mouseTrackHorizontal.draw();
            notifyPrice.draw();
            orderBuyPrice.draw();
            orderSellPrice.draw();

            plotLineChartDatas();
            drawCandleChart();
            plotIsValid = true;
        }
    }

    /**
     * Draw candle chart.
     */
    private void drawCandleChart() {
        int dataSize = candleChartData.size();
        long start = (long) axisX.computeVisibleMinValue();
        long end = (long) axisX.computeVisibleMaxValue();

        for (int i = 0; i < dataSize; i++) {
            Candle candle = candles.get(i);
            Tick tick = candleChartData.get(i);
            long time = tick.start.toInstant().toEpochMilli();

            if (start <= time && time <= end) {
                // in visible range
                if (candle == null) {
                    candle = new Candle();
                    candleGraph.getChildren().add(candle);
                    candles.put(i, candle);
                }

                // update candle layout
                double x = axisX.getPositionForValue(time);
                double open = axisY.getPositionForValue(tick.openPrice.toDouble());
                double close = axisY.getPositionForValue(tick.closePrice.toDouble());
                double high = axisY.getPositionForValue(tick.maxPrice.toDouble());
                double low = axisY.getPositionForValue(tick.minPrice.toDouble());
                candle.update(close - open, high - open, low - open);
                candle.setLayoutX(x);
                candle.setLayoutY(open);
            } else {
                // out of visible range
                if (candle != null) {
                    candle.setLayoutX(-10);
                    candle.setLayoutY(-10);
                }
            }
        }
    }

    private final double DISTANCE_THRESHOLD = 0.5;

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
     * Set data list for line chart.
     * 
     * @param datalist
     */
    public final void setLineChartDataList(ObservableList<CandleChartData> datalist) {
        // update
        lineChartData = datalist;

        if (plotIsValid) {
            plotIsValid = false;
            setNeedsLayout(true);
        }
    }

    private final InvalidationListener dataChangeObserver = observalbe -> {
        if (plotIsValid) {
            plotIsValid = false;
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

        if (plotIsValid) {
            plotIsValid = false;
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
        private final Enum[] classNames;

        /** The model. */
        private final List<TickLable> labels;

        /** The associated axis. */
        private final Axis axis;

        /**
         * @param classNames
         */
        private LineMark(Axis axis, Enum... classNames) {
            this(new ArrayList(), axis, classNames);
        }

        /**
         * @param className
         * @param labels
         */
        private LineMark(List<TickLable> labels, Axis axis, Enum... classNames) {
            this.classNames = classNames;
            this.labels = labels;
            this.axis = axis;

            StyleHelper.of(this).style(ChartClass.Line).style(classNames);
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
            TickLable label = axis.createLabel(classNames);
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
