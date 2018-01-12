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

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import javafx.beans.Observable;
import javafx.collections.ObservableList;
import javafx.scene.Group;
import javafx.scene.input.ContextMenuEvent;
import javafx.scene.layout.Region;
import javafx.scene.shape.LineTo;
import javafx.scene.shape.MoveTo;
import javafx.scene.shape.Path;
import javafx.scene.shape.PathElement;

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
 * @version 2018/01/12 16:40:07
 */
public class ChartPlotArea extends Region {

    final Axis axisX;

    /** The vertical axis. */
    final Axis axisY;

    /** The current market. */
    private final TradingView trade;

    /** The notificator. */
    private final Notificator notificator = I.make(Notificator.class);

    /** The candle chart manager */
    private final ArrayList<Candle> candles = new ArrayList<>();

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

    /** Flag whether candle chart shoud layout on the next rendering phase or not. */
    private boolean shoudLayoutCandle = true;

    /** The line chart data list. */
    private ObservableList<Tick> candleChartData;

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

        widthProperty().addListener(this::shoudLayoutCandle);
        heightProperty().addListener(this::shoudLayoutCandle);
        axisX.scroll.valueProperty().addListener(this::shoudLayoutCandle);
        axisX.scroll.visibleAmountProperty().addListener(this::shoudLayoutCandle);
        axisY.scroll.valueProperty().addListener(this::shoudLayoutCandle);
        axisY.scroll.visibleAmountProperty().addListener(this::shoudLayoutCandle);

        Viewtify.clip(this);
        Viewtify.clip(backGridVertical);
        Viewtify.clip(backGridHorizontal);

        visualizeMouseTrack();
        visualizeNotifyPrice();
        visualizeOrderPrice();

        getChildren()
                .addAll(backGridVertical, backGridHorizontal, notifyPrice, orderBuyPrice, orderSellPrice, mouseTrackHorizontal, mouseTrackVertical, candleGraph);
    }

    /**
     * This chart should draw on the next rendering phase.
     */
    private void shoudLayoutCandle(Observable source) {
        if (shoudLayoutCandle == false) {
            shoudLayoutCandle = true;
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

            mouseTrackVertical.shouldLayout();
            mouseTrackHorizontal.shouldLayout();
        });

        // remove on exit
        setOnMouseExited(e -> {
            labelX.value.set(-1);
            labelY.value.set(-1);

            mouseTrackVertical.shouldLayout();
            mouseTrackHorizontal.shouldLayout();
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
        // draw back lines
        backGridVertical.draw();
        backGridHorizontal.draw();
        mouseTrackVertical.draw();
        mouseTrackHorizontal.draw();
        notifyPrice.draw();
        orderBuyPrice.draw();
        orderSellPrice.draw();

        // plotLineChartDatas();
        drawCandleChart();
    }

    /**
     * Draw candle chart.
     */
    private void drawCandleChart() {
        if (shoudLayoutCandle) {
            int candleSize = candles.size();
            int dataSize = candleChartData.size();

            // size matching
            int difference = dataSize - candleSize;

            if (0 < difference) {
                // ensure size with null
                candles.addAll(Arrays.asList((Candle[]) Array.newInstance(Candle.class, difference)));
            } else if (difference < 0) {
                Iterator<Candle> remover = candles.subList(candleSize + difference, candleSize).iterator();

                while (remover.hasNext()) {
                    Candle candle = remover.next();
                    candleGraph.getChildren().remove(candle);
                    remover.remove();
                }
            }

            // draw chart
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
                        candles.set(i, candle);
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
            shoudLayoutCandle = false;
        }
    }

    /**
     * Set data list for line chart.
     * 
     * @param datalist
     */
    public final void setCandleChartDataList(ObservableList<Tick> datalist) {
        // clear old list configuration
        if (candleChartData != null) {
            candleChartData.removeListener(this::shoudLayoutCandle);
        }

        // add new list configuration
        if (datalist != null) {
            datalist.addListener(this::shoudLayoutCandle);
        }

        // update
        candleChartData = datalist;

        shoudLayoutCandle(null);
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

        /** Flag whether this mark should draw on the next rendering phase. */
        private boolean shouldLayout = true;

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

            shouldLayout();

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
            shouldLayout();
        }

        /**
         * Draw mark.
         */
        private void draw() {
            if (shouldLayout || shoudLayoutCandle) {
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
                shouldLayout = false;
            }
        }

        /**
         * This mark should draw on the next rendering phase.
         */
        private void shouldLayout() {
            shouldLayout(null);
        }

        /**
         * This mark should draw on the next rendering phase.
         */
        private void shouldLayout(Observable source) {
            shouldLayout = true;
            setNeedsLayout(true);
        }
    }
}
