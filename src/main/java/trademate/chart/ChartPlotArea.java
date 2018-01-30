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
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.ToDoubleFunction;

import javafx.collections.ObservableList;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.input.ContextMenuEvent;
import javafx.scene.layout.Region;
import javafx.scene.shape.LineTo;
import javafx.scene.shape.MoveTo;
import javafx.scene.shape.Path;
import javafx.scene.shape.PathElement;
import javafx.scene.shape.Polyline;

import cointoss.Side;
import cointoss.order.Order.State;
import cointoss.ticker.Tick;
import cointoss.util.Num;
import kiss.I;
import trademate.Notificator;
import trademate.TradingView;
import trademate.chart.Axis.TickLable;
import trademate.chart.shape.Candle;
import viewtify.Viewtify;
import viewtify.ui.helper.LayoutAssistant;
import viewtify.ui.helper.StyleHelper;

/**
 * @version 2018/01/12 16:40:07
 */
public class ChartPlotArea extends Region {

    /** The chart node. */
    private final CandleChart chart;

    /** The horizontal axis. */
    final Axis axisX;

    /** The vertical axis. */
    final Axis axisY;

    /** The current market. */
    private final TradingView trade;

    /** The notificator. */
    private final Notificator notificator = I.make(Notificator.class);

    /** Chart UI */
    private final Group candles = new Group();

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
    private final LineMark latestPrice;

    /** Chart UI */
    private final LineMark orderBuyPrice;

    /** Chart UI */
    private final LineMark orderSellPrice;

    /** Chart UI */
    private final LineChart chartBottom = new LineChart();

    /** Flag whether candle chart shoud layout on the next rendering phase or not. */
    public final LayoutAssistant layoutCandle = new LayoutAssistant(this);

    /**
     * @param chart
     * @param trade
     * @param axisX
     * @param axisY
     */
    public ChartPlotArea(CandleChart chart, TradingView trade, Axis axisX, Axis axisY) {
        this.chart = chart;
        this.trade = trade;
        this.axisX = axisX;
        this.axisY = axisY;
        this.backGridVertical = new LineMark(axisX.forGrid, axisX, ChartClass.BackGrid);
        this.backGridHorizontal = new LineMark(axisY.forGrid, axisY, ChartClass.BackGrid);
        this.mouseTrackVertical = new LineMark(axisX, ChartClass.MouseTrack);
        this.mouseTrackHorizontal = new LineMark(axisY, ChartClass.MouseTrack);
        this.notifyPrice = new LineMark(axisY, ChartClass.PriceSignal);
        this.latestPrice = new LineMark(axisY, ChartClass.PriceLatest);
        this.orderBuyPrice = new LineMark(axisY, ChartClass.OrderSupport, Side.BUY);
        this.orderSellPrice = new LineMark(axisY, ChartClass.OrderSupport, Side.SELL);

        this.chartBottom.create(tick -> tick.longVolume.toDouble() * 2, ChartClass.ChartVolume, Side.BUY);
        this.chartBottom.create(tick -> tick.shortVolume.toDouble() * 2, ChartClass.ChartVolume, Side.SELL);
        this.chartBottom.create(tick -> tick.volume.toDouble() * 2, ChartClass.ChartTotalVolume);

        Viewtify.clip(this);

        layoutCandle.layoutBy(widthProperty(), heightProperty())
                .layoutBy(axisX.scroll.valueProperty(), axisX.scroll.visibleAmountProperty())
                .layoutBy(axisY.scroll.valueProperty(), axisY.scroll.visibleAmountProperty());

        visualizeNotifyPrice();
        visualizeOrderPrice();
        visualizeLatestPrice();
        visualizeMouseTrack();

        getChildren()
                .addAll(backGridVertical, backGridHorizontal, notifyPrice, orderBuyPrice, orderSellPrice, latestPrice, mouseTrackHorizontal, mouseTrackVertical, candles, chartBottom);
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

            mouseTrackVertical.layoutLine.requestLayout();
            mouseTrackHorizontal.layoutLine.requestLayout();
        });

        // remove on exit
        setOnMouseExited(e -> {
            labelX.value.set(-1);
            labelY.value.set(-1);

            mouseTrackVertical.layoutLine.requestLayout();
            mouseTrackHorizontal.layoutLine.requestLayout();
        });
    }

    /**
     * Visualize notifiable price in chart.
     */
    private void visualizeNotifyPrice() {
        addEventHandler(ContextMenuEvent.CONTEXT_MENU_REQUESTED, e -> {
            double clickedPosition = e.getY();

            // check price range to add or remove
            for (TickLable mark : notifyPrice.labels) {
                double markedPosition = axisY.getPositionForValue(mark.value.get());

                if (Math.abs(markedPosition - clickedPosition) < 5) {
                    notifyPrice.remove(mark);
                    return;
                }
            }

            Num price = Num.of(Math.floor(axisY.getValueForPosition(clickedPosition)));
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
     * Visualize latest price in chart.
     */
    private void visualizeLatestPrice() {
        trade.market().timeline.map(e -> e.price).diff().on(Viewtify.UIThread).to(price -> {
            latestPrice.removeAll();
            latestPrice.createLabel(price);
        });
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void layoutChildren() {
        // draw back lines
        backGridVertical.draw();
        backGridHorizontal.draw();
        mouseTrackVertical.draw();
        mouseTrackHorizontal.draw();
        notifyPrice.draw();
        latestPrice.draw();
        orderBuyPrice.draw();
        orderSellPrice.draw();

        // plotLineChartDatas();
        drawCandleChart();
    }

    /**
     * Draw candle chart.
     */
    private void drawCandleChart() {
        layoutCandle.layout(() -> {
            ObservableList<Node> candles = this.candles.getChildren();
            int candleSize = candles.size();
            int dataSize = chart.ticker.size();

            // ensure size
            int difference = dataSize - candleSize;

            if (0 < difference) {
                for (int i = 0; i < difference; i++) {
                    candles.add(new Candle());
                }
            }

            // estimate visible range
            long start = (long) axisX.computeVisibleMinValue();
            long end = (long) axisX.computeVisibleMaxValue();
            int visible = 0;

            // draw chart
            for (int i = 0; i < candleSize; i++) {
                Candle candle = (Candle) candles.get(i);

                if (i < dataSize) {
                    Tick tick = chart.ticker.get(i);
                    long time = tick.start.toInstant().toEpochMilli();

                    if (start <= time && time <= end) {
                        // in visible range
                        double x = axisX.getPositionForValue(time);
                        double open = axisY.getPositionForValue(tick.openPrice.toDouble());
                        double close = axisY.getPositionForValue(tick.closePrice.toDouble());
                        double high = axisY.getPositionForValue(tick.maxPrice.toDouble());
                        double low = axisY.getPositionForValue(tick.minPrice.toDouble());
                        candle.update(close - open, high - open, low - open, null);
                        candle.setLayoutX(x);
                        candle.setLayoutY(open);

                        chartBottom.calculate(tick, visible++, x);
                    } else {
                        // out of visible range
                        candle.setLayoutX(-50);
                        candle.setLayoutY(-50);
                    }
                } else {
                    // unused
                    candle.setLayoutX(-50);
                    candle.setLayoutY(-50);
                }
            }
            chartBottom.draw(visible);
        });
    }

    /**
     * @version 2018/01/15 20:52:31
     */
    private class LineChart extends Group {

        /** The poly line. */
        private final List<Line> lines = new ArrayList();

        /**
         * 
         */
        private LineChart() {
        }

        /**
         * Create new line chart.
         * 
         * @param className
         * @param converter
         */
        private void create(ToDoubleFunction<Tick> converter, Enum... classNames) {
            lines.add(new Line(converter, classNames));
        }

        /**
         * Calculate each value
         */
        private void calculate(Tick tick, int index, double x) {
            for (Line line : lines) {
                line.calculate(tick, x);
            }
        }

        /**
         * Finish drawing chart line.
         */
        private void draw(int index) {
            double height = getHeight();
            double scale = lines.stream().map(Line::scale).min(Comparator.naturalOrder()).get();

            for (Line line : lines) {
                // draw
                for (int i = 1; i < line.values.size(); i += 2) {
                    line.values.set(i, height - line.values.get(i) * scale);
                }
                line.getPoints().setAll(line.values);

                // clear
                line.values.clear();
                line.valueMax = 0;
            }
        }

        /**
         * @version 2018/01/15 20:38:38
         */
        private class Line extends Polyline {

            /** The maximum height. */
            private final double heightMax = 40;

            /** The value converter. */
            private final ToDoubleFunction<Tick> converter;

            /** The values. */
            private final List<Double> values = new ArrayList();

            /** The max value. */
            private double valueMax = 0;

            /**
             * @param converter
             * @param valueMax
             */
            private Line(ToDoubleFunction<Tick> converter, Enum... classNames) {
                this.converter = converter;

                getChildren().add(this);
                StyleHelper.of(this).style(classNames);
            }

            /**
             * Calculate value.
             * 
             * @param tick
             * @return
             */
            private void calculate(Tick tick, double x) {
                double calculated = converter.applyAsDouble(tick);

                valueMax = Math.max(valueMax, calculated);
                values.add(x);
                values.add(calculated);
            }

            /**
             * Calculate scale.
             * 
             * @return
             */
            private double scale() {
                return heightMax < valueMax ? heightMax / valueMax : 1;
            }
        }
    }

    /**
     * @version 2018/01/12 21:54:07
     */
    private class TopMark extends Path {

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

        /** The layout manager. */
        private final LayoutAssistant layoutLine = layoutCandle.sub();

        /**
         * @param classNames
         */
        private LineMark(Axis axis, Enum... classNames) {
            this(new CopyOnWriteArrayList(), axis, classNames);
        }

        /**
         * @param className
         * @param labels
         */
        private LineMark(List<TickLable> labels, Axis axis, Enum... classNames) {
            this.classNames = classNames;
            this.labels = labels;
            this.axis = axis;

            Viewtify.clip(this);
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

            layoutLine.requestLayout();

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
            layoutLine.requestLayout();
        }

        /**
         * Dsipose all mark.
         */
        private void removeAll() {
            labels.forEach(this::remove);
        }

        /**
         * Draw mark.
         */
        private void draw() {
            layoutLine.layout(() -> {
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
            });
        }
    }
}
