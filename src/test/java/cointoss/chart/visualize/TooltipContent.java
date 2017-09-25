/*
 * Copyright (C) 2017 Nameless Production Committee
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          http://opensource.org/licenses/mit-license.php
 */
package cointoss.chart.visualize;

import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;

/**
 * @version 2017/09/25 17:32:28
 */
public class TooltipContent extends GridPane {
    private final Label openValue = new Label();

    private final Label closeValue = new Label();

    private final Label highValue = new Label();

    private final Label lowValue = new Label();

    TooltipContent() {
        Label open = new Label("始値：");
        Label close = new Label("終値：");
        Label high = new Label("高値：");
        Label low = new Label("安値：");
        open.getStyleClass().add("candlestick-tooltip-label");
        close.getStyleClass().add("candlestick-tooltip-label");
        high.getStyleClass().add("candlestick-tooltip-label");
        low.getStyleClass().add("candlestick-tooltip-label");
        setConstraints(open, 0, 0);
        setConstraints(openValue, 1, 0);
        setConstraints(close, 0, 1);
        setConstraints(closeValue, 1, 1);
        setConstraints(high, 0, 2);
        setConstraints(highValue, 1, 2);
        setConstraints(low, 0, 3);
        setConstraints(lowValue, 1, 3);
        getChildren().addAll(open, openValue, close, closeValue, high, highValue, low, lowValue);
    }

    public void update(double open, double close, double high, double low) {
        openValue.setText(Double.toString(open));
        closeValue.setText(Double.toString(close));
        highValue.setText(Double.toString(high));
        lowValue.setText(Double.toString(low));
    }
}
