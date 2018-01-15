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

import java.util.function.Consumer;

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
import kiss.Disposable;
import trademate.TradingView;
import viewtify.ui.helper.LayoutAssistant;

/**
 * @version 2018/01/09 22:15:56
 */
public class CandleChart extends Region {

    /** The target chart. */
    public final ObjectProperty<Chart> chart = new SimpleObjectProperty();

    /** The x-axis UI. */
    public final Axis axisX = new Axis(5, 8, Side.BOTTOM);

    /** The y-axis UI. */
    public final Axis axisY = new Axis(5, 4, Side.RIGHT);

    /** The actual graph drawer. */
    public final ChartPlotArea main;

    /** The list of plottable cnadle date. */
    private final ObservableList<Tick> candles = FXCollections.observableArrayList();

    /** The layout manager. */
    private final LayoutAssistant layoutChart = new LayoutAssistant(this)//
            .layoutBy(widthProperty(), heightProperty())
            .layoutBy(chart, candles)
            .layoutBy(axisX.scroll.valueProperty(), axisX.scroll.visibleAmountProperty())
            .layoutBy(axisY.scroll.valueProperty(), axisY.scroll.visibleAmountProperty());

    /**
     * 
     */
    public CandleChart(AnchorPane parent, TradingView trade) {
        this.main = new ChartPlotArea(trade, axisX, axisY);

        parent.getChildren().add(this);

        AnchorPane.setTopAnchor(this, 10d);
        AnchorPane.setBottomAnchor(this, 15d);
        AnchorPane.setRightAnchor(this, 15d);
        AnchorPane.setLeftAnchor(this, 0d);

        getChildren().addAll(main, axisX, axisY);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected final void layoutChildren() {
        layoutChart.layout(() -> {
            setAxisXRange();
            setAxisYRange();

            Insets insets = getInsets();
            double x = insets.getLeft();
            double y = insets.getTop();
            double width = getWidth() - x - insets.getRight();
            double height = getHeight() - y - insets.getBottom();
            double axisXHeight = axisX.prefHeight(width);
            double axisYWidth = axisY.prefWidth(height);
            double mainHeight = Math.max(0, height - axisXHeight);
            double mainWidth = Math.max(0, width - axisYWidth);

            // layout axis
            axisX.resizeRelocate(x, y + mainHeight, mainWidth, axisXHeight);
            axisY.resizeRelocate(x + mainWidth, y, axisYWidth, mainHeight);
            axisX.layout();
            axisY.layout();

            // layout chart
            main.resizeRelocate(x, y, mainWidth, mainHeight);
            main.layoutChildren();
        });
    }

    /**
     * Set x-axis range.
     */
    private void setAxisXRange() {
        axisX.logicalMaxValue.set(candles.get(candles.size() - 1).start.toInstant().toEpochMilli() + 3 * 60 * 1000);
        axisX.logicalMinValue.set(candles.get(0).start.toInstant().toEpochMilli());
    }

    /**
     * Set y-axis range.
     */
    private void setAxisYRange() {
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
        graph.accept(this.main);

        return this;
    }

    private Disposable disposable;

    /**
     * @param closePrice
     * @param maxPrice
     * @param minPrice
     * @return
     */
    public CandleChart candleDate(Chart data) {
        this.candles.clear();

        if (disposable != null) {
            disposable.dispose();
        }

        for (Tick tick : data.ticks) {
            this.candles.add(tick);
        }

        disposable = data.add.to(tick -> {
            this.candles.add(tick);

            if (0.99 <= axisX.scroll.getValue()) {
                axisX.scroll.setValue(1);
            }
        });

        main.setCandleChartDataList(candles);

        layoutChart.requestLayout();

        return this;
    }
}
