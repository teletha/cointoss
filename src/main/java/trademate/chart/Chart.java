/*
 * Copyright (C) 2018 CoinToss Development Team
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          https://opensource.org/licenses/MIT
 */
package trademate.chart;

import javafx.geometry.Insets;
import javafx.geometry.Side;
import javafx.scene.layout.Region;

import cointoss.util.Chrono;
import cointoss.util.Num;
import kiss.Variable;
import viewtify.ui.helper.LayoutAssistant;

/**
 * @version 2018/06/26 18:55:05
 */
public class Chart extends Region {

    /** The time unit interval. */
    private static long M = 60;

    /** The x-axis UI. */
    public final Axis axisX = new Axis(5, 4, Side.BOTTOM)
            .units(M, 5 * M, 10 * M, 30 * M, 60 * M, 2 * 60 * M, 4 * 60 * M, 6 * 60 * M, 12 * 60 * M, 24 * 60 * M);

    /** The y-axis UI. */
    public final Axis axisY = new Axis(5, 4, Side.RIGHT).visibleScroll(false);

    /** The chart view. */
    private final ChartView chart;

    /** The actual graph drawer. */
    private final ChartCanvas canvas;

    /** The layout manager. */
    private final LayoutAssistant layoutChart = new LayoutAssistant(this);

    /**
     * 
     */
    public Chart(ChartView chart) {
        this.chart = chart;
        this.canvas = new ChartCanvas(chart, axisX, axisY);

        layoutChart.layoutBy(widthProperty(), heightProperty())
                .layoutBy(axisX.scroll.valueProperty(), axisX.scroll.visibleAmountProperty())
                .layoutBy(axisY.scroll.valueProperty(), axisY.scroll.visibleAmountProperty())
                .layoutBy(chart.ticker.observe().switchMap(ticker -> ticker.add.startWithNull()));

        chart.market.observe().to(m -> {
            // configure axis label
            axisX.tickLabelFormatter.set(time -> Chrono.systemBySeconds(time).format(Chrono.TimeWithoutSec));
            axisY.tickLabelFormatter.set(m.service::calculateReadablePrice);
        });

        getChildren().addAll(canvas, axisX, axisY);
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
            canvas.resizeRelocate(x, y, mainWidth, mainHeight);
            canvas.layoutChildren();
        });
    }

    /**
     * Set x-axis range.
     */
    private void setAxisXRange() {
        axisX.logicalMinValue.set(chart.ticker.v.first().start.toEpochSecond());
        axisX.logicalMaxValue.set(chart.ticker.v.last().start.toEpochSecond() + 3 * 60);
    }

    /**
     * Set y-axis range.
     */
    private void setAxisYRange() {
        Variable<Num> max = Variable.of(Num.MIN);
        Variable<Num> min = Variable.of(Num.MAX);

        double start = axisX.computeVisibleMinValue();
        double end = axisX.computeVisibleMaxValue();

        chart.ticker.v.each(data -> {
            long time = data.start.toEpochSecond();

            if (start <= time && time <= end) {
                max.set(Num.max(max.v, data.highPrice));
                min.set(Num.min(min.v, data.lowPrice));
            }
        });

        Num margin = max.v.minus(min).multiply(Num.of(0.5));
        axisY.logicalMaxValue.set(max.v.plus(margin).toLong());
        axisY.logicalMinValue.set(min.v.minus(margin).toLong());
    }
}
