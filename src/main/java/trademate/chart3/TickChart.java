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

import java.time.ZonedDateTime;
import java.util.List;

import javafx.scene.chart.Axis;
import javafx.scene.chart.XYChart;

import cointoss.chart.Chart;
import cointoss.chart.Tick;
import cointoss.util.Num;

/**
 * @version 2017/12/17 9:12:15
 */
public class TickChart extends XYChart<ZonedDateTime, Num> {

    /**
     * 
     */
    public TickChart() {
        super(new DateAxis(), new NumAxis());
    }

    /**
     * Add new chart.
     * 
     * @param chart
     */
    public void add(Chart chart) {
        Series<ZonedDateTime, Num> series = new Series();

        for (Tick tick : chart.ticks) {
            Data<ZonedDateTime, Num> data = new Data(tick.start, tick.openPrice);
            series.getData().add(data);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void dataItemAdded(Series<ZonedDateTime, Num> series, int itemIndex, Data<ZonedDateTime, Num> item) {
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void dataItemRemoved(Data<ZonedDateTime, Num> item, Series<ZonedDateTime, Num> series) {
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void dataItemChanged(Data<ZonedDateTime, Num> item) {
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void seriesAdded(Series<ZonedDateTime, Num> series, int seriesIndex) {
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void seriesRemoved(Series<ZonedDateTime, Num> series) {
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void layoutPlotChildren() {
    }

    /**
     * @version 2017/12/17 9:14:51
     */
    private static class DateAxis extends Axis<ZonedDateTime> {

        /**
         * {@inheritDoc}
         */
        @Override
        protected Object autoRange(double length) {
            return null;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        protected void setRange(Object range, boolean animate) {
        }

        /**
         * {@inheritDoc}
         */
        @Override
        protected Object getRange() {
            return null;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public double getZeroPosition() {
            return 0;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public double getDisplayPosition(ZonedDateTime value) {
            return 0;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public ZonedDateTime getValueForDisplay(double displayPosition) {
            return null;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public boolean isValueOnAxis(ZonedDateTime value) {
            return false;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public double toNumericValue(ZonedDateTime value) {
            return 0;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public ZonedDateTime toRealValue(double value) {
            return null;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        protected List<ZonedDateTime> calculateTickValues(double length, Object range) {
            return null;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        protected String getTickMarkLabel(ZonedDateTime value) {
            return null;
        }
    }

    /**
     * @version 2017/12/17 9:15:33
     */
    private static class NumAxis extends Axis<Num> {

        /**
         * {@inheritDoc}
         */
        @Override
        protected Object autoRange(double length) {
            return null;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        protected void setRange(Object range, boolean animate) {
        }

        /**
         * {@inheritDoc}
         */
        @Override
        protected Object getRange() {
            return null;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public double getZeroPosition() {
            return 0;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public double getDisplayPosition(Num value) {
            return 0;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public Num getValueForDisplay(double displayPosition) {
            return null;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public boolean isValueOnAxis(Num value) {
            return false;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public double toNumericValue(Num value) {
            return 0;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public Num toRealValue(double value) {
            return null;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        protected List<Num> calculateTickValues(double length, Object range) {
            return null;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        protected String getTickMarkLabel(Num value) {
            return null;
        }
    }
}
