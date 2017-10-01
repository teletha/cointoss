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

import static cointoss.Execution.*;
import static java.time.temporal.ChronoUnit.*;

import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;

import cointoss.Market;
import cointoss.Trading;
import cointoss.chart.Chart;
import cointoss.chart.Tick;
import cointoss.market.bitflyer.BitFlyer;
import cointoss.util.Num;

/**
 * @version 2017/09/25 21:39:19
 */
public class Visualize extends Application {

    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm");

    /** The system thread. */
    private static ExecutorService system = Executors.newFixedThreadPool(1);

    private Market market;

    private CandleChart candleChart;

    /**
     * Launch application.
     * 
     * @param args
     */
    public static void main(final String[] args) {
        launch(args);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void init() throws Exception {
        system.submit(() -> {
            market = new Market(BitFlyer.FX_BTC_JPY.service(), BitFlyer.FX_BTC_JPY.log().fromLast(30, MINUTES), new Trading() {

                /**
                 * 
                 */
                @Override
                protected void initialize() {
                    market.minute1.to(t -> {
                        if (candleChart != null) {
                            Platform.runLater(() -> {
                                candleChart.requestLayout();
                            });
                        }
                    });
                }
            });
        });
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void start(final Stage stage) throws Exception {
        while (market == null) {
            Thread.sleep(100);
        }

        Chart serise = market.second10;

        Num max = Num.MIN;
        Num min = Num.MAX;

        for (Tick tick : serise.ticks) {
            max = Num.max(max, tick.maxPrice);
            min = Num.min(min, tick.minPrice);
        }
        Num diff = max.minus(min);

        CandleChartData weightMedian = new CandleChartData(serise);
        weightMedian.name.set("Weight Median");

        candleChart = new CandleChart()//
                .graph(plot -> {
                })
                .axisX(axis -> {
                    long minute = 60000;
                    axis.nameLabel.setText("日時");
                    axis.tickLabelFormatter.set(v -> Instant.ofEpochMilli((long) v).plus(9, HOURS).atZone(UTC).format(formatter));
                    axis.visibleRange.set(100D / serise.ticks.size());
                    axis.units.set(new double[] {minute, 10 * minute, 30 * minute, 60 * minute, 2 * 60 * minute, 4 * 60 * minute,
                            6 * 60 * minute, 12 * 60 * minute, 24 * 60 * minute});
                })
                .axisY(axis -> {
                    axis.nameLabel.setText("JPY");
                    axis.tickLabelFormatter.set(v -> Num.of(v).asJPY());
                    axis.visibleRange.set(Num.of(24000).divide(diff).toDouble());
                    axis.scrollBarVisibility.set(false);
                })
                .graphTracker(new GraphTracker())
                .lineData(weightMedian)
                .candleDate(serise);

        Legend legend = new Legend();
        legend.setDataList(candleChart.lines);

        BorderPane p = new BorderPane();
        p.setTop(legend);
        p.setPrefWidth(800);
        p.setPrefHeight(500);
        p.setCenter(candleChart);
        p.setStyle("-fx-padding:50");

        Scene s = new Scene(p);
        stage.setScene(s);
        stage.show();
        stage.setOnCloseRequest(e -> {
            market.dispose();
            system.shutdownNow();
        });
    }
}
