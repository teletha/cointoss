/*
 * Copyright (C) 2017 Nameless Production Committee
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          http://opensource.org/licenses/mit-license.php
 */
package trademate.chart.shape;

import javafx.scene.Group;
import javafx.scene.layout.Region;
import javafx.scene.shape.Line;

import cointoss.Side;
import trademate.chart.ChartClass;

/**
 * @version 2018/02/02 17:40:38
 */
public class Candle extends Group {

    /** The candle width. */
    private static final int width = 4;

    /** The line part. */
    private final Line line = new Line();

    /** The bar part. */
    private final Region bar = new Region();

    /** The direction */
    private Side side = Side.BUY;

    /**
     * 
     */
    public Candle() {
        updateStyle();
        setAutoSizeChildren(false);
        getChildren().addAll(line, bar);
    }

    /**
     * Update value.
     * 
     * @param closeOffset
     * @param highOffset
     * @param lowOffset
     */
    public void update(double closeOffset, double highOffset, double lowOffset, String top) {
        this.side = closeOffset > 0 ? Side.SELL : Side.BUY;

        line.setStartY(highOffset);
        line.setEndY(lowOffset);

        if (side.isSell()) {
            bar.resizeRelocate(-width / 2, 0, width, closeOffset);
        } else {
            bar.resizeRelocate(-width / 2, closeOffset, width, closeOffset * -1);
        }
        updateStyle();
    }

    /**
     * Update style.
     */
    private void updateStyle() {
        line.getStyleClass().setAll(ChartClass.CandleLine.name(), side.name());
        bar.getStyleClass().setAll(ChartClass.CandleBar.name(), side.name());
    }
}
