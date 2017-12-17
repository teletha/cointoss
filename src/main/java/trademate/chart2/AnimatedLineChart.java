/*
 * Copyright (C) 2017 Nameless Production Committee
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          http://opensource.org/licenses/mit-license.php
 */
package trademate.chart2;

import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;

import javafx.animation.AnimationTimer;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.stage.Stage;

public class AnimatedLineChart extends Application {

    private static final int MAX_DATA_POINTS = 50;

    private int xSeriesData = 0;

    private XYChart.Series<Number, Number> series1 = new XYChart.Series<>();

    private XYChart.Series<Number, Number> series2 = new XYChart.Series<>();

    private XYChart.Series<Number, Number> series3 = new XYChart.Series<>();

    private ExecutorService executor;

    private ConcurrentLinkedQueue<Number> dataQ1 = new ConcurrentLinkedQueue<>();

    private ConcurrentLinkedQueue<Number> dataQ2 = new ConcurrentLinkedQueue<>();

    private ConcurrentLinkedQueue<Number> dataQ3 = new ConcurrentLinkedQueue<>();

    private NumberAxis xAxis;

    private void init(Stage primaryStage) {

        xAxis = new NumberAxis(0, MAX_DATA_POINTS, MAX_DATA_POINTS / 10);
        xAxis.setForceZeroInRange(true);
        xAxis.setAutoRanging(false);
        xAxis.setTickLabelsVisible(true);
        xAxis.setTickMarkVisible(true);
        xAxis.setMinorTickVisible(true);

        NumberAxis yAxis = new NumberAxis();

        // Create a LineChart
        final LineChart<Number, Number> lineChart = new LineChart<Number, Number>(xAxis, yAxis) {
            // Override to remove symbols on each data point
            @Override
            protected void dataItemAdded(Series<Number, Number> series, int itemIndex, Data<Number, Number> item) {
            }
        };

        lineChart.setAnimated(false);
        lineChart.setTitle("Animated Line Chart");
        lineChart.setHorizontalGridLinesVisible(true);

        // Set Name for Series
        series1.setName("Series 1");
        series2.setName("Series 2");
        series3.setName("Series 3");

        // Add Chart Series
        lineChart.getData().addAll(series1, series2, series3);

        primaryStage.setScene(new Scene(lineChart));
    }

    @Override
    public void start(Stage stage) {
        stage.setTitle("Animated Line Chart Sample");
        init(stage);
        stage.show();

        executor = Executors.newCachedThreadPool(new ThreadFactory() {
            @Override
            public Thread newThread(Runnable r) {
                Thread thread = new Thread(r);
                thread.setDaemon(true);
                return thread;
            }
        });

        AddToQueue addToQueue = new AddToQueue();
        executor.execute(addToQueue);
        // -- Prepare Timeline
        prepareTimeline();
    }

    private class AddToQueue implements Runnable {
        @Override
        public void run() {
            try {
                // add a item of random data to queue
                dataQ1.add(Math.random());
                dataQ2.add(Math.random());
                dataQ3.add(Math.random());

                Thread.sleep(500);
                executor.execute(this);
            } catch (InterruptedException ex) {
                ex.printStackTrace();
            }
        }
    }

    // -- Timeline gets called in the JavaFX Main thread
    private void prepareTimeline() {
        // Every frame to take any data from queue and add to chart
        new AnimationTimer() {
            @Override
            public void handle(long now) {
                addDataToSeries();
            }
        }.start();
    }

    private void addDataToSeries() {
        for (int i = 0; i < 20; i++) { // -- add 20 numbers to the plot+
            if (dataQ1.isEmpty()) break;
            series1.getData().add(new XYChart.Data<>(xSeriesData++, dataQ1.remove()));
            series2.getData().add(new XYChart.Data<>(xSeriesData++, dataQ2.remove()));
            series3.getData().add(new XYChart.Data<>(xSeriesData++, dataQ3.remove()));
        }
        // remove points to keep us at no more than MAX_DATA_POINTS
        if (series1.getData().size() > MAX_DATA_POINTS) {
            series1.getData().remove(0, series1.getData().size() - MAX_DATA_POINTS);
        }
        if (series2.getData().size() > MAX_DATA_POINTS) {
            series2.getData().remove(0, series2.getData().size() - MAX_DATA_POINTS);
        }
        if (series3.getData().size() > MAX_DATA_POINTS) {
            series3.getData().remove(0, series3.getData().size() - MAX_DATA_POINTS);
        }
        // update
        xAxis.setLowerBound(xSeriesData - MAX_DATA_POINTS);
        xAxis.setUpperBound(xSeriesData - 1);
    }

    public static void main(String[] args) {
        launch(args);
    }
}