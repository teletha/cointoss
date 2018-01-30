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

import javafx.geometry.Insets;
import javafx.geometry.Side;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Region;

import cointoss.Market;
import cointoss.ticker.Tick;
import cointoss.ticker.TickSpan;
import cointoss.ticker.Ticker;
import cointoss.util.Num;
import kiss.Disposable;
import viewtify.ui.helper.LayoutAssistant;

/**
 * @version 2018/01/09 22:15:56
 */
public class CandleChart extends Region {

    /** The x-axis UI. */
    public final Axis axisX = new Axis(5, 8, Side.BOTTOM);

    /** The y-axis UI. */
    public final Axis axisY = new Axis(5, 4, Side.RIGHT);

    /** The actual graph drawer. */
    public final ChartPlotArea main;

    /** The current market. */
    public final Market market;

    /** The list of plottable cnadle date. */
    Ticker ticker;

    /** The layout manager. */
    private final LayoutAssistant layoutChart = new LayoutAssistant(this)//
            .layoutBy(widthProperty(), heightProperty())
            .layoutBy(axisX.scroll.valueProperty(), axisX.scroll.visibleAmountProperty())
            .layoutBy(axisY.scroll.valueProperty(), axisY.scroll.visibleAmountProperty());

    /** The use flag. */
    private Disposable tickerUsage = Disposable.empty();

    /**
     * 
     */
    public CandleChart(AnchorPane parent, Market market) {
        this.market = market;
        this.main = new ChartPlotArea(this, axisX, axisY);

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
        axisX.logicalMinValue.set(ticker.first().start.toInstant().toEpochMilli());
        axisX.logicalMaxValue.set(ticker.last().start.toInstant().toEpochMilli() + 3 * 60 * 1000);
    }

    /**
     * Set y-axis range.
     */
    private void setAxisYRange() {
        Num max = Num.MIN;
        Num min = Num.MAX;

        long start = (long) axisX.computeVisibleMinValue();
        long end = (long) axisX.computeVisibleMaxValue();

        for (int i = 0; i < ticker.size(); i++) {
            Tick data = ticker.get(i);
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

    /**
     * Specify ticker.
     * 
     * @param ticker
     * @return
     */
    public final CandleChart use(TickSpan span) {
        this.ticker = market.tickerBy(span);

        tickerUsage.dispose();
        tickerUsage = ticker.update.startWith((Tick) null).to(tick -> {
            main.layoutCandle.requestLayout();

            if (0.99 <= axisX.scroll.getValue()) {
                axisX.scroll.setValue(1);
            }
        });
        return this;
    }
}
