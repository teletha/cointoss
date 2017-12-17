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

import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javafx.fxml.FXML;
import javafx.scene.layout.Pane;

import cointoss.Market;
import cointoss.chart.Chart;
import cointoss.market.bitflyer.BitFlyer;
import viewtify.View;
import viewtify.Viewtify;

/**
 * @version 2017/09/25 21:39:19
 */
public class ChartDemo extends View {

    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm");

    /** The system thread. */
    private static ExecutorService system = Executors.newFixedThreadPool(1);

    private @FXML Pane root;

    /**
     * Launch application.
     * 
     * @param args
     */
    public static void main(final String[] args) {
        Viewtify.activate(ChartDemo.class);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void initialize() {
        Viewtify.inWorker(() -> {
            Market market = new Market(BitFlyer.FX_BTC_JPY.service(), BitFlyer.FX_BTC_JPY.log().fromLast(10, ChronoUnit.HOURS));

            Viewtify.inUI(() -> {
                Chart serise = market.minute1;

                // Num max = Num.MIN;
                // Num min = Num.MAX;
                //
                // for (Tick tick : serise.ticks) {
                // max = Num.max(max, tick.maxPrice);
                // min = Num.min(min, tick.minPrice);
                // }
                // Num diff = max.minus(min);

                CandleChart candleChart = new CandleChart()//
                        .graph(plot -> {
                        })
                        // .axisX(axis -> {
                        // long minute = 60000;
                        // axis.nameLabel.setText("日時");
                        // axis.tickLabelFormatter.set(v -> Instant.ofEpochMilli((long) v).plus(9,
                        // HOURS).atZone(UTC).format(formatter));
                        // axis.visibleRange.set(100D / serise.ticks.size());
                        // axis.units.set(new double[] {minute, 10 * minute, 30 * minute, 60 *
                        // minute, 2
                        // *
                        // 60 * minute, 4 * 60 * minute,
                        // 6 * 60 * minute, 12 * 60 * minute, 24 * 60 * minute});
                        // })
                        // .axisY(axis -> {
                        // axis.nameLabel.setText("JPY");
                        // axis.tickLabelFormatter.set(v -> Num.of(v).asJPY());
                        // axis.visibleRange.set(Num.of(50000).divide(diff).toDouble());
                        // axis.scrollBarVisibility.set(false);
                        // })
                        .candleDate(serise);

                root.getChildren().add(candleChart);
            });

            return market;
        });
    }
}
