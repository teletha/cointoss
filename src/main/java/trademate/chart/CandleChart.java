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

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;

import javafx.beans.InvalidationListener;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Side;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Region;

import cointoss.chart.Chart;
import cointoss.chart.Tick;
import cointoss.util.Num;
import trademate.TradingView;

/**
 * @version 2018/01/09 22:15:56
 */
public class CandleChart extends Region {

    /** The target chart. */
    public final ObjectProperty<Chart> chart = new SimpleObjectProperty();

    /** The list of plottable cnadle date. */
    public final ObservableList<Tick> candles;

    /** The x-axis UI. */
    public final Axis axisX = new Axis(5, 8, Side.BOTTOM);

    /** The y-axis UI. */
    public final Axis axisY = new Axis(5, 4, Side.RIGHT);

    /** The actual graph drawer. */
    public final ChartPlotArea graph;

    /** The validity of data. */
    private AtomicBoolean dataIsValid = new AtomicBoolean();

    private final InvalidationListener dataValidateListener = observable -> {
        if (dataIsValid.compareAndSet(true, false)) {
            requestLayout();
        }
    };

    /**
     * 
     */
    public CandleChart(AnchorPane parent, TradingView trade) {
        this.graph = new ChartPlotArea(trade, axisX, axisY);

        parent.getChildren().add(this);

        AnchorPane.setTopAnchor(this, 10d);
        AnchorPane.setBottomAnchor(this, 15d);
        AnchorPane.setRightAnchor(this, 15d);
        AnchorPane.setLeftAnchor(this, 0d);

        chart.addListener(dataValidateListener);
        axisX.scroll.valueProperty().addListener(dataValidateListener);
        axisX.scroll.visibleAmountProperty().addListener(dataValidateListener);
        axisY.scroll.valueProperty().addListener(dataValidateListener);
        axisY.scroll.visibleAmountProperty().addListener(dataValidateListener);

        candles = FXCollections.observableArrayList();
        candles.addListener(dataValidateListener);

        getChildren().addAll(graph, axisX, axisY);
    }

    /**
     * Configure x-axis.
     * 
     * @param axis
     * @return Chainable API.
     */
    public final CandleChart axisX(Consumer<Axis> axis) {
        axis.accept(this.axisX);

        return this;
    }

    /**
     * Configure y-axis.
     * 
     * @param axis
     * @return Chainable API.
     */
    public final CandleChart axisY(Consumer<Axis> axis) {
        axis.accept(this.axisY);

        return this;
    }

    /**
     * Configure graph plot area.
     * 
     * @param graph
     * @return Chainable API.
     */
    public final CandleChart graph(Consumer<ChartPlotArea> graph) {
        graph.accept(this.graph);

        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected final void layoutChildren() {
        double width = getWidth();
        double height = getHeight();
        if (width == -1 || height == -1) {
            return;
        }

        if (dataIsValid.get() == false) {
            setXAxisRange();
            setYAxisRange();
        }

        Insets insets = getInsets();
        width -= insets.getLeft() + insets.getRight();
        height -= insets.getTop() + insets.getBottom();

        layoutChartArea(width, height, insets.getLeft(), insets.getTop());
    }

    private void layoutChartArea(double width, double height, double x0, double y0) {
        double graphWidth, graphHeight;
        double xAxisHeight, yAxisWidth;

        if (axisX.isVisible() && axisY.isVisible()) {
            double gap = 10;
            int loop = 0;
            double xH = 0, yW = 0;
            while (gap > 5) {
                double xaxisH = axisX.prefHeight(width);
                double yaxisW = axisY.prefWidth(height - xaxisH);
                final double xaxisH2 = axisX.prefHeight(width - yaxisW);
                gap = abs(xaxisH2 - xaxisH);
                xaxisH = xaxisH2;
                if (gap > 5) {
                    final double yaxisW2 = axisY.prefWidth(height - xaxisH);
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
            graphWidth = width - yW;
            graphHeight = height - xH;
            xAxisHeight = xH;
            yAxisWidth = yW;
        } else {
            if (axisX.isVisible()) {
                xAxisHeight = axisX.prefHeight(width);
                graphHeight = height - xAxisHeight;
                graphWidth = width;
                yAxisWidth = axisY.prefWidth(graphHeight);
            } else if (axisY.isVisible()) {
                yAxisWidth = axisY.prefWidth(height);
                graphWidth = width - yAxisWidth;
                graphHeight = height;
                xAxisHeight = axisX.prefHeight(graphWidth);
            } else {
                xAxisHeight = axisX.prefHeight(width);
                yAxisWidth = axisY.prefWidth(height);
                graphWidth = width;
                graphHeight = height;
            }
        }
        graphHeight = max(0, graphHeight);
        graphWidth = max(0, graphWidth);
        axisX.resize(graphWidth, xAxisHeight);
        axisY.resize(yAxisWidth, graphHeight);
        axisX.layout();
        axisY.layout();
        final boolean isLeft = axisY.side != Side.RIGHT;
        final boolean isBottom = axisX.side != Side.TOP;
        final double x = axisY.isVisible() && isLeft ? yAxisWidth : 0, y = !axisX.isVisible() || isBottom ? 0 : xAxisHeight;
        if (axisX.isVisible()) {
            if (isBottom) {
                axisX.relocate(x + x0, graphHeight + y0);
            } else {
                axisX.relocate(x + x0, y0);
            }
        }
        if (axisY.isVisible()) {
            if (isLeft) {
                axisY.relocate(x0, y + y0);
            } else {
                axisY.relocate(graphWidth + x0, y + y0);
            }
        }
        graph.relocate(x + x0, y + y0);

        final double oldgW = graph.getWidth();
        final double oldgH = graph.getHeight();
        final boolean resize = oldgW != graphWidth || oldgH != graphHeight;

        if (resize) {
            graph.resize(graphWidth, graphHeight);
        }
        if (resize || dataIsValid.get() == false) {
            graph.layoutChildren();
            dataIsValid.set(true);
        }
    }

    private static double max(double max, double v) {
        return v != v || max >= v ? max : v;
    }

    /**
     * Set x-axis range.
     */
    private void setXAxisRange() {
        axisX.logicalMaxValue.set(candles.get(candles.size() - 1).start.toInstant().toEpochMilli() + 3 * 60 * 1000);
        axisX.logicalMinValue.set(candles.get(0).start.toInstant().toEpochMilli());
    }

    /**
     * Set y-axis range.
     */
    private void setYAxisRange() {
        Num max = Num.MIN;
        Num min = Num.MAX;

        long start = (long) axisX.computeVisibleMinValue();
        long end = (long) axisX.computeVisibleMaxValue();

        for (int i = 0; i < candles.size(); i++) {
            Tick data = candles.get(i);
            long time = data.start.toInstant().toEpochMilli();

            if (start <= time && time <= end) {
                max = Num.max(max, data.maxPrice);
                min = Num.min(min, data.minPrice);
            }
        }

        Num margin = max.minus(min).multiply(Num.of(0.5));
        axisY.logicalMaxValue.set(max.plus(margin).toDouble());
        axisY.logicalMinValue.set(min.minus(margin).toDouble());
    }

    /**
     * @param closePrice
     * @param maxPrice
     * @param minPrice
     * @return
     */
    public CandleChart candleDate(Chart data) {
        if (data == null) {
            graph.setCandleChartDataList(FXCollections.emptyObservableList());
            return this;
        }

        dataIsValid.set(false);
        this.candles.clear();

        for (Tick tick : data.ticks) {
            this.candles.add(tick);
        }

        data.to(tick -> {
            this.candles.add(tick);

            if (0.99 <= axisX.scroll.getValue()) {
                axisX.scroll.setValue(1);
            }
        });

        graph.setCandleChartDataList(candles);

        return this;
    }
}
