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

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.layout.StackPane;

import cointoss.Market;
import cointoss.market.bitflyer.BitFlyer;
import viewtify.View;
import viewtify.Viewtify;

/**
 * @version 2017/09/25 21:39:19
 */
public class ChartDemo extends View {

    private @FXML StackPane chart;

    /**
     * Launch application.
     * 
     * @param args
     */
    public static void main(final String[] args) {
        Viewtify.activate(ChartDemo.class);
    }

    // Chart serise = market.minute1;
    //
    // // Num max = Num.MIN;
    // // Num min = Num.MAX;
    // //
    // // for (Tick tick : serise.ticks) {
    // // max = Num.max(max, tick.maxPrice);
    // // min = Num.min(min, tick.minPrice);
    // // }
    // // Num diff = max.minus(min);
    //
    // CandleChart candleChart = new CandleChart()//
    // .graph(plot -> {
    // })
    // // .axisX(axis -> {
    // // long minute = 60000;
    // // axis.nameLabel.setText("日時");
    // // axis.tickLabelFormatter.set(v -> Instant.ofEpochMilli((long) v).plus(9,
    // // HOURS).atZone(UTC).format(formatter));
    // // axis.visibleRange.set(100D / serise.ticks.size());
    // // axis.units.set(new double[] {minute, 10 * minute, 30 * minute, 60 *
    // // minute, 2
    // // *
    // // 60 * minute, 4 * 60 * minute,
    // // 6 * 60 * minute, 12 * 60 * minute, 24 * 60 * minute});
    // // })
    // // .axisY(axis -> {
    // // axis.nameLabel.setText("JPY");
    // // axis.tickLabelFormatter.set(v -> Num.of(v).asJPY());
    // // axis.visibleRange.set(Num.of(50000).divide(diff).toDouble());
    // // axis.scrollBarVisibility.set(false);
    // // })
    // .candleDate(serise);
    //
    /**
     * {@inheritDoc}
     */
    @Override
    protected void initialize() {
        Viewtify.inWorker(() -> {
            Market market = new Market(BitFlyer.FX_BTC_JPY.service(), BitFlyer.FX_BTC_JPY.log().rangeRandom(1));

            Viewtify.inUI(() -> {
                chart.getChildren().add(new LayeredChart().addChart(createBarChart(), createLineChart()));
            });

            return market;
        });
    }

    private BarChart<String, Number> createBarChart() {
        final BarChart<String, Number> chart = new BarChart<>(new CategoryAxis(), createYaxis());
        chart.getData().addAll(new XYChart.Series(FXCollections
                .observableArrayList(new XYChart.Data("Jan", 2), new XYChart.Data("Feb", 10), new XYChart.Data("March", 8), new XYChart.Data("April", 4), new XYChart.Data("May", 7), new XYChart.Data("Jun", 5), new XYChart.Data("Jul", 4), new XYChart.Data("Aug", 8), new XYChart.Data("Sep", 16.5), new XYChart.Data("Oct", 13.9), new XYChart.Data("Nov", 17), new XYChart.Data("Dec", 10))));
        return chart;
    }

    private LineChart<String, Number> createLineChart() {
        final LineChart<String, Number> chart = new LineChart<>(new CategoryAxis(), createYaxis());
        chart.setCreateSymbols(false);
        chart.getData().addAll(new XYChart.Series(FXCollections
                .observableArrayList(new XYChart.Data("January", 1), new XYChart.Data("Feburary", 2), new XYChart.Data("Mar", 1.5), new XYChart.Data("Apr", 3), new XYChart.Data("May", 2.5), new XYChart.Data("Jun", 5), new XYChart.Data("Jul", 4), new XYChart.Data("Aug", 8), new XYChart.Data("Sep", 6.5), new XYChart.Data("Oct", 13), new XYChart.Data("Nov", 10), new XYChart.Data("Dec", 20))));
        return chart;
    }

    private NumberAxis createYaxis() {
        final NumberAxis axis = new NumberAxis(0, 21, 1);
        axis.setPrefWidth(35);
        axis.setMinorTickCount(10);

        axis.setTickLabelFormatter(new NumberAxis.DefaultFormatter(axis) {
            @Override
            public String toString(Number object) {
                return String.format("%7.2f", object.floatValue());
            }
        });

        return axis;
    }

}
