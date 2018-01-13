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
import javafx.scene.control.Tooltip;
import javafx.scene.layout.Region;
import javafx.scene.shape.Line;
import javafx.scene.shape.Path;
import javafx.scene.text.Text;

import cointoss.Side;
import trademate.chart.ChartClass;

/**
 * @version 2018/01/12 21:50:12
 */
public class Candle extends Group {

    /** The candle width. */
    private static final int width = 4;

    /** The line part. */
    private final Line line = new Line();

    /** The bar part. */
    private final Region bar = new Region();

    /** The top mark. */
    private Text textTop;

    /** The bottom mark. */
    private Path bottom;

    /** The direction */
    private Side side = Side.BUY;

    private final Tooltip tooltip = new Tooltip();

    /**
     * 
     */
    public Candle() {
        updateStyle();
        setAutoSizeChildren(false);
        getChildren().addAll(line, bar);

        tooltip.setGraphic(new TooltipContent());
        Tooltip.install(bar, tooltip);
        updateStyle();
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

        // top text
        if (top == null) {
            if (textTop != null) {
                textTop.setVisible(false);
            }
        } else {
            if (textTop == null) {
                textTop = new Text();
                textTop.getStyleClass().add(ChartClass.CandleTopText.name());
                getChildren().add(textTop);
            }
            textTop.setVisible(true);
            textTop.setText(top);
            textTop.setLayoutX(0);
            textTop.setLayoutY(highOffset - 10);
        }
    }

    /**
     * Update tooltip.
     * 
     * @param open
     * @param close
     * @param high
     * @param low
     */
    public void updateTooltip(double open, double close, double high, double low) {
        TooltipContent tooltipContent = (TooltipContent) tooltip.getGraphic();
        tooltipContent.update(open, close, high, low);
    }

    /**
     * Update style.
     */
    private void updateStyle() {
        line.getStyleClass().setAll(ChartClass.CandleLine.name(), side.name());
        bar.getStyleClass().setAll(ChartClass.CandleBar.name(), side.name());
    }

    /**
     * Draw top text.
     * 
     * @param text
     */
    private void drawTopText(String text) {

    }
}
