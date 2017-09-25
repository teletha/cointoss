/*
 * Copyright (C) 2017 Nameless Production Committee
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          http://opensource.org/licenses/mit-license.php
 */
package cointoss.chart;

import org.knowm.xchart.BitmapEncoder;
import org.knowm.xchart.BitmapEncoder.BitmapFormat;
import org.knowm.xchart.QuickChart;
import org.knowm.xchart.SwingWrapper;
import org.knowm.xchart.XYChart;

/**
 * @version 2017/09/25 12:27:19
 */
public class Plot {

    public static void main(String[] args) throws Exception {
        double[] xData = new double[] {0.0, 1.0, 2.0};
        double[] yData = new double[] {2.0, 1.0, 0.0};

        // Create Chart
        XYChart chart = QuickChart.getChart("Sample Chart", "X", "Y", "y(x)", xData, yData);

        // Show it
        new SwingWrapper(chart).displayChart();

        // Save it
        BitmapEncoder.saveBitmap(chart, "./Sample_Chart", BitmapFormat.PNG);

        // or save it in high-res
        BitmapEncoder.saveBitmapWithDPI(chart, "./Sample_Chart_300_DPI", BitmapFormat.PNG, 300);

        // XYChart chart = new XYChartBuilder().width(600).height(500).title("Gaussian
        // Blobs").xAxisTitle("X").yAxisTitle("Y").build();
        //
        // // Customize Chart
        // chart.getStyler().setDefaultSeriesRenderStyle(XYSeriesRenderStyle.Scatter);
        // chart.getStyler().setChartTitleVisible(false);
        // chart.getStyler().setLegendPosition(LegendPosition.InsideSW);
        // chart.getStyler().setMarkerSize(16);
        //
        // // Series
        // chart.addSeries("Gaussian Blob 1", getGaussian(1000, 1, 10), getGaussian(1000, 1, 10));
        // XYSeries series = chart.addSeries("Gaussian Blob 2", getGaussian(1000, 1, 10),
        // getGaussian(1000, 0, 5));
        // series.setMarker(SeriesMarkers.DIAMOND);
        //
        // new SwingWrapper(chart).displayChart();
    }
}
