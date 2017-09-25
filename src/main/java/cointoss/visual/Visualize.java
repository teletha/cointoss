/*
 * Copyright (C) 2017 Nameless Production Committee
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          http://opensource.org/licenses/mit-license.php
 */
package cointoss.visual;

import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

import javafx.application.Application;
import javafx.geometry.Orientation;
import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;

import cointoss.Execution;
import cointoss.chart.Chart;
import cointoss.chart.Tick;
import cointoss.market.bitflyer.BitFlyer;
import cointoss.util.Num;
import filer.Filer;

/**
 * @version 2017/09/25 21:39:19
 */
public class Visualize extends Application {

    public static void main(final String[] args) {
        launch(args);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void start(final Stage stage) throws Exception {
        Chart cc = chart(BitFlyer.FX_BTC_JPY, "2017-09-05T13:00:00", "2017-09-07T00:59:59", Duration.ofMinutes(1));

        Num max = Num.MIN;
        Num min = Num.MAX;

        for (Tick tick : cc.ticks) {
            max = Num.max(max, tick.maxPrice);
            min = Num.min(min, tick.minPrice);
        }
        int size = cc.ticks.size();

        final LinearAxis axis = new LinearAxis();
        final LinearAxis yaxis = new LinearAxis();
        axis.setName("ももも");
        axis.setLowerValue(0);
        axis.setVisibleAmount(0.1);
        yaxis.setName("ままま");

        final AxisZoomHandler zoom = new AxisZoomHandler();
        zoom.install(axis);
        zoom.install(yaxis);

        final LineChart c = new LineChart();
        c.setXAxis(axis);
        c.setYAxis(yaxis);

        LineChartData close = new LineChartData(size);
        close.setName("Close");

        LineChartData maxPrice = new LineChartData(size);
        maxPrice.setName("Max");

        LineChartData minPrice = new LineChartData(size);
        minPrice.setName("Min");

        for (int i = 0; i < size; i++) {
            close.addData(i, cc.ticks.get(i).closePrice.toDouble());
            maxPrice.addData(i, cc.ticks.get(i).maxPrice.toDouble());
            minPrice.addData(i, cc.ticks.get(i).minPrice.toDouble());
        }

        c.getDataList().addAll(close, maxPrice, minPrice);

        c.setOrientation(Orientation.HORIZONTAL);
        c.setRangeMarginX(1);

        final GraphTracker traker = new GraphTracker();
        traker.install(c);

        final Legend legend = new Legend();
        legend.setDataList(c.getDataList());

        final BorderPane p = new BorderPane();
        p.setTop(legend);
        p.setPrefWidth(800);
        p.setPrefHeight(500);
        p.setCenter(c);
        p.setStyle("-fx-padding:50");
        final Scene s = new Scene(p);
        stage.setScene(s);
        stage.show();
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
}
