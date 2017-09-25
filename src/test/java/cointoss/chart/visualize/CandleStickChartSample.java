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

import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.stage.Stage;

import cointoss.Execution;
import cointoss.chart.Chart;
import cointoss.chart.Tick;
import cointoss.market.bitflyer.BitFlyer;
import cointoss.util.Num;
import filer.Filer;

/**
 * @version 2017/09/25 17:33:38
 */
public class CandleStickChartSample extends Application {

    private CandleStickChart chart;

    private NumberAxis xAxis;

    private NumberAxis yAxis;

    public Parent createContent() {
        Chart cc = chart(BitFlyer.FX_BTC_JPY, "2017-09-05T13:00:00", "2017-09-07T00:59:59", Duration.ofMinutes(1));

        Num max = Num.MIN;
        Num min = Num.MAX;

        for (Tick tick : cc.ticks) {
            max = Num.max(max, tick.maxPrice);
            min = Num.min(min, tick.minPrice);
        }

        xAxis = new NumberAxis(0, cc.ticks.size(), 1);
        xAxis.setMinorTickCount(0);
        yAxis = new NumberAxis(min.toDouble(), max.toDouble(), 50);
        chart = new CandleStickChart(xAxis, yAxis);
        chart.setTitle("ローソク足チャートのサンプル");

        // setup chart
        xAxis.setLabel("日付（順序）");
        yAxis.setLabel("株価 (USD)");

        // add starting data
        XYChart.Series<Number, Number> series = new XYChart.Series<>();
        int day = 0;

        for (Tick tick : cc.ticks) {
            series.getData().add(new XYChart.Data<>(day++, tick.getWeightMedian().toDouble(), new CandleStickExtraValues(tick.openPrice
                    .toDouble(), tick.closePrice.toDouble(), tick.maxPrice.toDouble(), tick.minPrice.toDouble())));
        }

        ObservableList<XYChart.Series<Number, Number>> stockData = chart.getData();
        if (stockData == null) {
            stockData = FXCollections.observableArrayList(series);
            chart.setData(stockData);
        } else {
            chart.getData().add(series);
        }
        return chart;
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        Scene scene = new Scene(createContent(), 600, 400);
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    private Chart chart(BitFlyer type, String start, String end, Duration duration) {
        // convert Asia/Tokyo to UTC
        ZonedDateTime startTime = LocalDateTime.parse(start).minusHours(9).atZone(Execution.UTC);
        ZonedDateTime endTime = LocalDateTime.parse(end).minusHours(9).atZone(Execution.UTC);

        // search tick log
        DateTimeFormatter format = DateTimeFormatter.ofPattern("yyyyMMddhhmmss");
        Path file = Filer.locate("src/test/resources/trend")
                .resolve(format.format(startTime) + "～" + format.format(endTime) + " " + duration + ".txt");

        Chart chart = new Chart(duration);

        if (Files.notExists(file)) {
            // crate new tick log from execution log
            type.log() //
                    .range(startTime, endTime)
                    .skipWhile(e -> e.isBefore(startTime))
                    .takeWhile(e -> e.isBefore(endTime))
                    .to(chart::tick);

            chart.writeTo(file);
        } else {
            chart.readFrom(file);
        }
        return chart;
    }

    /**
     * Java main for when running without JavaFX launcher
     *
     * @param args command line arguments
     */
    public static void main(String[] args) {
        launch(args);
    }
}
