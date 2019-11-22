/*
 * Copyright (C) 2019 CoinToss Development Team
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          https://opensource.org/licenses/MIT
 */
package trademate.chart;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import javafx.collections.ObservableList;
import javafx.scene.Node;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.input.ContextMenuEvent;
import javafx.scene.layout.Region;
import javafx.scene.paint.Color;
import javafx.scene.shape.LineTo;
import javafx.scene.shape.MoveTo;
import javafx.scene.shape.Path;
import javafx.scene.shape.PathElement;

import org.eclipse.collections.api.list.primitive.MutableDoubleList;
import org.eclipse.collections.impl.list.mutable.primitive.DoubleArrayList;

import cointoss.market.bitflyer.BitFlyer;
import cointoss.market.bitflyer.SFD;
import cointoss.ticker.Indicator;
import cointoss.ticker.Tick;
import cointoss.util.Chrono;
import cointoss.util.Num;
import kiss.I;
import stylist.Style;
import trademate.chart.Axis.TickLable;
import trademate.chart.PlotScript.PlotDSL;
import trademate.setting.Notificator;
import viewtify.Viewtify;
import viewtify.ui.helper.LayoutAssistant;
import viewtify.ui.helper.StyleHelper;
import viewtify.ui.helper.User;
import viewtify.ui.helper.UserActionHelper;
import viewtify.util.FXUtils;

/**
 * @version 2018/07/13 23:47:28
 */
public class ChartCanvas extends Region implements UserActionHelper<ChartCanvas> {

    /** @FIXME Read from css file. */
    private static final Color Buy = Color.rgb(32, 151, 77);

    /** @FIXME Read from css file. */
    private static final Color Sell = Color.rgb(247, 105, 77);

    /** The candle width. */
    private static final int BarWidth = 3;

    /** The chart node. */
    private final ChartView chart;

    /** The horizontal axis. (shortcut) */
    private final Axis axisX;

    /** The vertical axis. (shortcut) */
    private final Axis axisY;

    /** The notificator. */
    private final Notificator notificator = I.make(Notificator.class);

    /** Chart UI */
    private final LineMark backGridVertical;

    /** Chart UI */
    private final LineMark backGridHorizontal;

    /** Chart UI */
    private final LineMark mouseTrackVertical;

    /** Chart UI */
    private final LineMark mouseTrackHorizontal;

    /** Chart UI */
    private final LineMark notifyPrice;

    /** Chart UI */
    private final LineMark latestPrice;

    /** Chart UI */
    private final LineMark orderBuyPrice;

    /** Chart UI */
    private final LineMark orderSellPrice;

    /** Chart UI */
    private final LineMark sfdPrice;

    /** Chart UI */
    private final Canvas candles = new Canvas();

    /** Chart UI */
    private final Canvas candleLatest = new Canvas();

    /** Chart UI */
    private final Canvas chartInfo = new Canvas();

    /** Flag whether candle chart shoud layout on the next rendering phase or not. */
    private final LayoutAssistant layoutCandle = new LayoutAssistant(this);

    /** Flag whether candle chart shoud layout on the next rendering phase or not. */
    private final LayoutAssistant layoutCandleLatest = new LayoutAssistant(this);

    /** The associated plot scripts. */
    private PlotDSL[] plotters = new PlotDSL[0];

    /**
     * Chart canvas.
     * 
     * @param chart
     * @param axisX
     * @param axisY
     */
    public ChartCanvas(ChartView chart, Axis axisX, Axis axisY) {
        this.chart = chart;
        this.axisX = axisX;
        this.axisY = axisY;
        this.backGridVertical = new LineMark(axisX.forGrid, axisX, ChartStyles.BackGrid);
        this.backGridHorizontal = new LineMark(axisY.forGrid, axisY, ChartStyles.BackGrid);
        this.mouseTrackVertical = new LineMark(axisX, ChartStyles.MouseTrack);
        this.mouseTrackHorizontal = new LineMark(axisY, ChartStyles.MouseTrack);
        this.notifyPrice = new LineMark(axisY, ChartStyles.PriceSignal);
        this.latestPrice = new LineMark(axisY, ChartStyles.PriceLatest);
        this.orderBuyPrice = new LineMark(axisY, ChartStyles.OrderSupportBuy);
        this.orderSellPrice = new LineMark(axisY, ChartStyles.OrderSupportSell);
        this.sfdPrice = new LineMark(axisY, ChartStyles.PriceSFD);
        this.candles.widthProperty().bind(widthProperty());
        this.candles.heightProperty().bind(heightProperty());
        this.candleLatest.widthProperty().bind(widthProperty());
        this.candleLatest.heightProperty().bind(heightProperty());
        this.chartInfo.widthProperty().bind(widthProperty());
        this.chartInfo.heightProperty().bind(heightProperty());

        chart.market.observe()
                .combineLatest(chart.ticker.observe())
                .map(v -> I.signal(I.make(PlotScriptRegistry.class).findScriptsOn(v.ⅰ.service))
                        .flatMap(script -> script.plot(chart.market.v, chart.ticker.v, chart))
                        .toList()
                        .toArray(PlotDSL[]::new))
                .to(v -> plotters = v);

        Viewtify.clip(this);

        layoutCandle.layoutBy(widthProperty(), heightProperty())
                .layoutBy(axisX.scroll.valueProperty(), axisX.scroll.visibleAmountProperty())
                .layoutBy(axisY.scroll.valueProperty(), axisY.scroll.visibleAmountProperty())
                .layoutBy(chart.ticker.observe().switchMap(ticker -> ticker.add.startWithNull()));
        layoutCandleLatest.layoutBy(widthProperty(), heightProperty())
                .layoutBy(axisX.scroll.valueProperty(), axisX.scroll.visibleAmountProperty())
                .layoutBy(axisY.scroll.valueProperty(), axisY.scroll.visibleAmountProperty())
                .layoutBy(chart.ticker.observe().switchMap(ticker -> ticker.update.startWithNull()))
                .layoutWhile(chart.showRealtimeUpdate.observeNow());

        visualizeNotifyPrice();
        visualizeOrderPrice();
        visualizeLatestPrice();
        visualizeMouseTrack();
        visualizeSFDPrice();

        getChildren()
                .addAll(backGridVertical, backGridHorizontal, notifyPrice, orderBuyPrice, orderSellPrice, latestPrice, sfdPrice, candles, candleLatest, chartInfo, mouseTrackHorizontal, mouseTrackVertical);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Node ui() {
        return this;
    }

    /**
     * Visualize mouse tracker in chart.
     */
    private void visualizeMouseTrack() {
        TickLable labelX = mouseTrackVertical.createLabel();
        TickLable labelY = mouseTrackHorizontal.createLabel();

        // track on move
        when(User.MouseMove, User.MouseDrag).to(e -> {
            double x = axisX.getValueForPosition(e.getX());
            labelX.value.set(x);
            labelY.value.set(axisY.getValueForPosition(e.getY()));

            mouseTrackVertical.layoutLine.requestLayout();
            mouseTrackHorizontal.layoutLine.requestLayout();

            // move the start position forward for visual consistency
            long sec = (long) x + chart.ticker.v.span.duration.toSeconds() / 2;

            // update upper info
            chart.ticker.v.findByEpochSecond(sec).to(this::drawChartInfo);
        });

        // remove on exit
        when(User.MouseExit).to(e -> {
            labelX.value.set(-1);
            labelY.value.set(-1);

            mouseTrackVertical.layoutLine.requestLayout();
            mouseTrackHorizontal.layoutLine.requestLayout();

            // clear upper info
            GraphicsContext gc = chartInfo.getGraphicsContext2D();
            gc.clearRect(0, 0, chartInfo.getWidth(), chartInfo.getHeight());
        });
    }

    /**
     * Visualize notifiable price in chart.
     */
    private void visualizeNotifyPrice() {
        addEventHandler(ContextMenuEvent.CONTEXT_MENU_REQUESTED, e -> {
            double clickedPosition = e.getY();

            // check price range to add or remove
            for (TickLable mark : notifyPrice.labels) {
                double markedPosition = axisY.getPositionForValue(mark.value.get());

                if (Math.abs(markedPosition - clickedPosition) < 5) {
                    notifyPrice.remove(mark);
                    return;
                }
            }

            Num price = Num.of(Math.floor(axisY.getValueForPosition(clickedPosition)));
            TickLable label = notifyPrice.createLabel(price);

            label.add(chart.market.v.signalByPrice(price).on(Viewtify.UIThread).to(exe -> {
                notifyPrice.remove(label);
            }));
        });
    }

    /**
     * Visualize order price in chart.
     */
    private void visualizeOrderPrice() {
        chart.market.observe()
                .switchMap(m -> m.orders.manages())
                .switchOn(chart.showOrderSupport.observeNow())
                .on(Viewtify.UIThread)
                .to(o -> {
                    LineMark mark = o.isBuy() ? orderBuyPrice : orderSellPrice;
                    TickLable label = mark.createLabel(o.price);

                    o.observeTerminating().on(Viewtify.UIThread).to(e -> {
                        System.out.println("remove order line " + e);
                        mark.remove(label);
                    });
                });
    }

    /**
     * Visualize latest price in chart.
     */
    private void visualizeLatestPrice() {
        chart.market.observeNow() //
                .skipNull()
                .switchMap(m -> m.tickers.latestPrice.observeNow())
                .switchOn(chart.showLatestPrice.observeNow())
                .on(Viewtify.UIThread)
                .effectOnLifecycle(disposer -> {
                    TickLable latest = latestPrice.createLabel("最新値");

                    disposer.add(() -> latestPrice.remove(latest));

                    return price -> latest.value.set(price.doubleValue());
                })
                .to(latestPrice.layoutLine::requestLayout);
    }

    /**
     * Visualize SFD price in chart.
     */
    private void visualizeSFDPrice() {
        chart.market.observe().to(market -> {
            if (market.service == BitFlyer.FX_BTC_JPY) {
                for (SFD sfd : SFD.values()) {
                    TickLable label = sfdPrice.createLabel("乖離" + sfd.percentage + "%");
                    market.service.add(sfd.boundary().on(Viewtify.UIThread).to(price -> {
                        label.value.set(price.doubleValue());
                        sfdPrice.layoutLine.requestLayout();
                    }));
                }
            }
        });
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void layoutChildren() {
        // draw back lines
        backGridVertical.draw();
        backGridHorizontal.draw();
        mouseTrackVertical.draw();
        mouseTrackHorizontal.draw();
        notifyPrice.draw();
        latestPrice.draw();
        sfdPrice.draw();
        orderBuyPrice.draw();
        orderSellPrice.draw();

        drawCandle();
    }

    /**
     * Draw candle chart.
     */
    private void drawCandle() {
        layoutCandle.layout(() -> {
            if (chart.ticker.v.size() == 0) {
                return;
            }

            // estimate visible range
            long start = (long) axisX.computeVisibleMinValue();
            long end = Math.min((long) axisX.computeVisibleMaxValue(), chart.ticker.v.last().start.toEpochSecond());
            long span = chart.ticker.v.span.duration.getSeconds();
            int visibleSize = (int) ((end - start) / span) + 1;
            int visibleStartIndex = (int) ((start - chart.ticker.v.first().start.toEpochSecond()) / span);
            if (visibleStartIndex + visibleSize == chart.ticker.v.size()) visibleSize--;

            // redraw all candles.
            GraphicsContext gc = candles.getGraphicsContext2D();
            gc.clearRect(0, 0, candles.getWidth(), candles.getHeight());

            // draw chart in visible range
            for (PlotDSL plotter : plotters) {
                plotter.lineMaxY = 0;

                // ensure size
                for (LineChart chart : plotter.lines) {
                    chart.valueY.clear();
                }
            }
            MutableDoubleList valueX = new NoCopyDoubleList();

            chart.ticker.v.each(visibleStartIndex, visibleSize, tick -> {
                double x = axisX.getPositionForValue(tick.start.toEpochSecond());
                double open = axisY.getPositionForValue(tick.openPrice.doubleValue());
                double close = axisY.getPositionForValue(tick.closePrice().doubleValue());
                double high = axisY.getPositionForValue(tick.highPrice().doubleValue());
                double low = axisY.getPositionForValue(tick.lowPrice().doubleValue());

                gc.setStroke(open < close ? Sell : Buy);
                gc.setLineWidth(1);
                gc.strokeLine(x, high, x, low);
                gc.setLineWidth(BarWidth);
                gc.strokeLine(x, open, x, close);

                for (PlotDSL plotter : plotters) {
                    for (LineChart chart : plotter.lines) {
                        double calculated = chart.indicator.valueAt(tick).doubleValue();

                        if (plotter.area == PlotArea.Main) {
                            calculated = axisY.getPositionForValue(calculated);
                        } else {
                            double max = 0 <= calculated ? calculated : -calculated;
                            if (plotter.lineMaxY < max) {
                                plotter.lineMaxY = max;
                            }
                        }
                        chart.valueY.add(calculated);
                    }
                }
                valueX.add(x);
            });

            double[] arrayX = valueX.toArray();
            double width = candles.getWidth();
            double height = candles.getHeight();

            for (PlotDSL plotter : plotters) {
                double scale = plotter.scale();

                // draw horizontal line
                for (Horizon horizon : plotter.horizons) {
                    double y = plotter.area != PlotArea.Main ? height - plotter.area.offset - horizon.value * scale : horizon.value;

                    gc.setLineWidth(horizon.width);
                    gc.setStroke(horizon.color);
                    gc.setLineDashes(horizon.dashArray);
                    gc.strokeLine(0, y, width, y);
                }

                // draw line chart
                for (LineChart chart : plotter.lines) {
                    if (plotter.area != PlotArea.Main) {
                        for (int i = 0; i < chart.valueY.size(); i++) {
                            chart.valueY.set(i, height - plotter.area.offset - chart.valueY.get(i) * scale);
                        }
                    }

                    gc.setLineWidth(chart.width);
                    gc.setStroke(chart.color);
                    gc.setLineDashes(chart.dashArray);
                    gc.strokePolyline(arrayX, chart.valueY.toArray(), valueX.size());
                }
            }
        });

        layoutCandleLatest.layout(() -> {
            if (chart.ticker.v.size() == 0) {
                return;
            }

            GraphicsContext gc = candleLatest.getGraphicsContext2D();
            gc.clearRect(0, 0, candleLatest.getWidth(), candleLatest.getHeight());

            Tick tick = chart.ticker.v.last();

            double x = axisX.getPositionForValue(tick.start.toEpochSecond());
            double open = axisY.getPositionForValue(tick.openPrice.doubleValue());
            double close = axisY.getPositionForValue(tick.closePrice().doubleValue());
            double high = axisY.getPositionForValue(tick.highPrice().doubleValue());
            double low = axisY.getPositionForValue(tick.lowPrice().doubleValue());

            gc.setStroke(open < close ? Sell : Buy);
            gc.setLineWidth(1);
            gc.strokeLine(x, high, x, low);
            gc.setLineWidth(BarWidth);
            gc.strokeLine(x, open, x, close);

            if (tick.previous != null) {
                double lastX = axisX.getPositionForValue(tick.previous.start.toEpochSecond());

                for (PlotDSL plotter : plotters) {
                    double height = getHeight();
                    double scale = plotter.scale();

                    for (LineChart chart : plotter.lines) {
                        if (!chart.valueY.isEmpty()) {
                            gc.setLineWidth(chart.width);
                            gc.setStroke(chart.color);
                            gc.setLineDashes(chart.dashArray);
                            gc.strokeLine(lastX, chart.valueY.getLast(), x, plotter.area == PlotArea.Main
                                    ? axisY.getPositionForValue(chart.indicator.valueAt(tick).doubleValue())
                                    : height - plotter.area.offset - chart.indicator.valueAt(tick).doubleValue() * scale);
                        }
                    }
                }
            }
        });
    }

    /**
     * Draw chart info.
     */
    private void drawChartInfo(Tick tick) {
        GraphicsContext gc = chartInfo.getGraphicsContext2D();
        gc.clearRect(0, 0, chartInfo.getWidth(), chartInfo.getHeight());

        int base = chart.market.v.service.setting.baseCurrencyScaleSize;
        String date = Chrono.system(tick.start).format(Chrono.DateTime);
        String open = "O" + tick.openPrice.scale(base);
        String high = "H" + tick.highPrice().scale(base);
        String low = "L" + tick.lowPrice().scale(base);
        String close = "C" + tick.closePrice().scale(base);

        int gap = 5;
        int width = 43;
        int largeWidth = width * 2 + gap;
        int y = 18;
        int offsetX = 10;
        gc.setFill(Color.WHITE);
        gc.fillText(date, offsetX, y, largeWidth);
        gc.fillText(open, offsetX + largeWidth + gap, y, width);
        gc.fillText(high, offsetX + largeWidth + width + gap * 2, y, width);
        gc.fillText(low, offsetX + largeWidth + width * 2 + gap * 3, y, width);
        gc.fillText(close, offsetX + largeWidth + width * 3 + gap * 4, y, width);

        // indicator values drawn from the same plot script are displayed on the same line
        int x = 0;
        Object origin = null;
        for (PlotDSL plotter : plotters) {
            if (origin != plotter.origin) {
                y += 15;
                x = offsetX;
                origin = plotter.origin;
            }
            for (LineChart chart : plotter.lines) {
                gc.setFill(chart.color);
                gc.fillText(chart.indicator.valueAt(tick).toString(), x, y, width);
                x += width + gap;
            }
        }
    }

    /**
     * 
     */
    static class Horizon {

        /** The constant value. */
        private final double value;

        /** The indicator color. */
        private final Color color;

        /** The indicator line width. */
        private final double width;

        /** The indicator line style. */
        private final double[] dashArray;

        /**
         * @param indicator
         * @param style
         */
        Horizon(double value, Style style) {
            this.value = value;
            this.color = FXUtils.color(style, "stroke");
            this.width = FXUtils.length(style, "stroke-width", 1);
            this.dashArray = FXUtils.lengths(style, "stroke-dasharray");
        }
    }

    /**
     * 
     */
    static class LineChart {

        /** The indicator. */
        private final Indicator<? extends Number> indicator;

        /** The indicator color. */
        private final Color color;

        /** The indicator line width. */
        private final double width;

        /** The indicator line style. */
        private final double[] dashArray;

        /** The y-axis values. */
        private final MutableDoubleList valueY = new NoCopyDoubleList();

        /**
         * @param indicator
         * @param style
         */
        LineChart(Indicator<? extends Number> indicator, Style style) {
            this.indicator = indicator;
            this.color = FXUtils.color(style, "stroke");
            this.width = FXUtils.length(style, "stroke-width", 1);
            this.dashArray = FXUtils.lengths(style, "stroke-dasharray");
        }
    }

    /**
     * 
     */
    static class CandleChart {

        /** The indicator. */
        private final Indicator<Tick> indicator;

        /**
         * @param indicator
         */
        CandleChart(Indicator<Tick> indicator, Style style) {
            this.indicator = indicator;
        }
    }

    /**
     * 
     */
    private class LineMark extends Path {

        /** The styles. */
        private final Style[] styles;

        /** The model. */
        private final List<TickLable> labels;

        /** The associated axis. */
        private final Axis axis;

        /** The layout manager. */
        private final LayoutAssistant layoutLine = layoutCandle.sub();

        /**
         * @param classNames
         */
        private LineMark(Axis axis, Style... styles) {
            this(new CopyOnWriteArrayList(), axis, styles);
        }

        /**
         * @param className
         * @param labels
         */
        private LineMark(List<TickLable> labels, Axis axis, Style... styles) {
            this.styles = styles;
            this.labels = labels;
            this.axis = axis;

            Viewtify.clip(this);
            StyleHelper.of(this).style(styles);
        }

        /**
         * Create new mark.
         * 
         * @return
         */
        private TickLable createLabel() {
            return createLabel(null, null);
        }

        /**
         * Create new mark.
         * 
         * @return
         */
        private TickLable createLabel(String description) {
            return createLabel(null, description);
        }

        /**
         * Create new mark.
         * 
         * @return
         */
        private TickLable createLabel(Num price) {
            return createLabel(price, null);
        }

        /**
         * Create new mark.
         * 
         * @return
         */
        private TickLable createLabel(Num price, String description) {
            TickLable label = axis.createLabel(description, styles);
            if (price != null) label.value.set(price.doubleValue());
            labels.add(label);

            layoutLine.requestLayout();

            return label;
        }

        /**
         * Dispose mark.
         * 
         * @param mark
         */
        private void remove(TickLable mark) {
            labels.remove(mark);
            mark.dispose();
            layoutLine.requestLayout();
        }

        /**
         * Hide all marks.
         */
        @SuppressWarnings("unused")
        private void hide() {
            layoutLine.requestLayout();
        }

        /**
         * Show all marks.
         */
        @SuppressWarnings("unused")
        private void show() {
            layoutLine.requestLayout();
        }

        /**
         * Draw mark.
         */
        private void draw() {
            layoutLine.layout(() -> {
                ObservableList<PathElement> paths = getElements();
                int pathSize = paths.size();
                int labelSize = labels.size();

                if (pathSize > labelSize * 2) {
                    paths.remove(labelSize * 2, pathSize);
                    pathSize = labelSize * 2;
                }

                for (int i = 0; i < labelSize; i++) {
                    MoveTo move;
                    LineTo line;

                    if (i * 2 < pathSize) {
                        move = (MoveTo) paths.get(i * 2);
                        line = (LineTo) paths.get(i * 2 + 1);
                    } else {
                        move = new MoveTo();
                        line = new LineTo();
                        paths.addAll(move, line);
                    }

                    double value = labels.get(i).position();

                    if (axis.isHorizontal()) {
                        move.setX(value);
                        move.setY(0);
                        line.setX(value);
                        line.setY(getHeight());
                    } else {
                        move.setX(0);
                        move.setY(value);
                        line.setX(getWidth());
                        line.setY(value);
                    }
                }
            });
        }
    }

    /**
     * 
     */
    private static class NoCopyDoubleList extends DoubleArrayList {

        /**
         * {@inheritDoc}
         */
        @Override
        public double[] toArray() {
            return items;
        }
    }
}
