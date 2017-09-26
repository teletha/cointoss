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
import java.time.Instant;
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
        Chart serise = chart(BitFlyer.FX_BTC_JPY, "2017-09-05T13:00:00", "2017-09-05T23:59:59", Duration.ofMinutes(1));

        Num max = Num.MIN;
        Num min = Num.MAX;

        for (Tick tick : serise.ticks) {
            max = Num.max(max, tick.maxPrice);
            min = Num.min(min, tick.minPrice);
        }
        Num diff = max.minus(min);

        LinearAxis axis = new LinearAxis("日時", v -> {
            return Instant.ofEpochMilli((long) v).atZone(Execution.UTC).format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);
        });
        axis.visibleAmount(100D / serise.ticks.size());

        LinearAxis yaxis = new LinearAxis("JPY", v -> Num.of(v).asJPY());
        yaxis.visibleAmount(Num.of(24000).divide(diff).toDouble());

        AxisZoomHandler zoom = new AxisZoomHandler();
        zoom.install(axis);
        zoom.install(yaxis);

        LineChartData weightMedian = new LineChartData(serise.ticks.size()).name("Weight Median");

        for (int i = 0; i < serise.ticks.size(); i++) {
            weightMedian.addData(serise.ticks.get(i).start.toInstant().toEpochMilli(), serise.ticks.get(i).getWeightMedian().toDouble());
        }

        LineChart line = new LineChart()//
                .xAxis(axis)
                .yAxis(yaxis)
                .orientation(Orientation.HORIZONTAL)
                .rangeMarginY(1.06)
                .graphTracker(new GraphTracker())
                .lineData(weightMedian)
                .candleDate(serise.ticks);

        Legend legend = new Legend();
        legend.setDataList(line.lines);

        BorderPane p = new BorderPane();
        p.setTop(legend);
        p.setPrefWidth(800);
        p.setPrefHeight(500);
        p.setCenter(line);
        p.setStyle("-fx-padding:50");

        Scene s = new Scene(p);
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
