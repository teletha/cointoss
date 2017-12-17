/*
 * Copyright (C) 2017 Nameless Production Committee
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          http://opensource.org/licenses/mit-license.php
 */
package trademate.chart3;

import javafx.scene.chart.XYChart;
import javafx.scene.layout.StackPane;

/**
 * @version 2017/12/17 23:12:01
 */
public class LayeredChart extends StackPane {

    /**
     * @param charts
     * @return
     */
    public LayeredChart addChart(XYChart... charts) {
        for (XYChart chart : charts) {
            chart.setAlternativeRowFillVisible(false);
            chart.setAlternativeColumnFillVisible(false);
            chart.setHorizontalGridLinesVisible(false);
            chart.setVerticalGridLinesVisible(false);
            chart.getXAxis().setVisible(false);
            chart.getYAxis().setVisible(false);
            chart.setLegendVisible(false);
            chart.setAnimated(false);
            chart.getStylesheets().addAll(ClassLoader.getSystemResource("chart.css").toExternalForm());
        }
        getChildren().addAll(charts);

        return this;
    }
}
