/*
 * Copyright (C) 2024 Nameless Production Committee
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          http://opensource.org/licenses/mit-license.php
 */
package trademate.chart2;

import io.fair_acc.chartfx.Chart;
import io.fair_acc.chartfx.XYChart;
import io.fair_acc.chartfx.axes.spi.DefaultNumericAxis;
import io.fair_acc.chartfx.renderer.Renderer;
import io.fair_acc.chartfx.renderer.spi.financial.CandleStickRenderer;
import io.fair_acc.chartfx.renderer.spi.financial.FinancialTheme;
import io.fair_acc.dataset.spi.DefaultDataSet;
import io.fair_acc.dataset.spi.DoubleDataSet;
import io.fair_acc.dataset.spi.financial.OhlcvDataSet;
import viewtify.ui.View;
import viewtify.ui.ViewDSL;

public class ChartView2 extends View {

    private Chart chart = getDefaultFinancialTestChart(FinancialTheme.Dark);

    /**
     * {@inheritDoc}
     */
    @Override
    protected ViewDSL declareUI() {
        return new ViewDSL() {
            {
                $(() -> chart);
            }
        };
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void initialize() {

    }

    /**
     * Default financial chart configuration
     *
     * @param theme defines theme which has to be used for sample app
     */
    protected Chart getDefaultFinancialTestChart(final FinancialTheme theme) {
        int N_SAMPLES = 2000;

        final XYChart chart = new XYChart(new DefaultNumericAxis(), new DefaultNumericAxis());

        final DoubleDataSet dataSet1 = new DoubleDataSet("data set #1");
        final DoubleDataSet dataSet2 = new DoubleDataSet("data set #2");
        // lineChartPlot.getDatasets().add(dataSet1); // for single data set
        chart.getDatasets().addAll(dataSet1, dataSet2); // two data sets

        final double[] xValues = new double[N_SAMPLES];
        final double[] yValues1 = new double[N_SAMPLES];
        final double[] yValues2 = new double[N_SAMPLES];
        for (int n = 0; n < N_SAMPLES; n++) {
            xValues[n] = n;
            yValues1[n] = Math.cos(Math.toRadians(10.0 * n));
            yValues2[n] = Math.sin(Math.toRadians(10.0 * n));
        }
        dataSet1.set(xValues, yValues1);
        dataSet2.set(xValues, yValues2);

        return chart;
    }

    protected void prepareRenderers(XYChart chart, OhlcvDataSet ohlcvDataSet, DefaultDataSet indiSet) {
        // create and apply renderers
        Renderer renderer = new CandleStickRenderer(true);
        renderer.getDatasets().addAll(ohlcvDataSet);

        chart.getRenderers().clear();
        chart.getRenderers().add(renderer);
    }
}
