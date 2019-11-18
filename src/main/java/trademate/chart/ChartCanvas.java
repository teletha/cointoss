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

import java.util.ArrayList;
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
import org.eclipse.collections.impl.factory.primitive.DoubleLists;

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

    /** The user plot chart. */
    private final List<PlotScriptChart> plots = new ArrayList();

    /** Chart UI */
    private final Canvas candles = new Canvas();

    /** Chart UI */
    private final Canvas candleLatest = new Canvas();

    /** Chart UI */
    private final Canvas candleInfo = new Canvas();

    /** Flag whether candle chart shoud layout on the next rendering phase or not. */
    public final LayoutAssistant layoutCandle = new LayoutAssistant(this);

    /** Flag whether candle chart shoud layout on the next rendering phase or not. */
    public final LayoutAssistant layoutCandleLatest = new LayoutAssistant(this);

    /** The settings. */
    private final ChartDisplaySetting setting = I.make(ChartDisplaySetting.class);

    /** The script manager. */
    private final PlotScriptRegistry scripts = I.make(PlotScriptRegistry.class);

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
        this.candleInfo.widthProperty().bind(widthProperty());
        this.candleInfo.heightProperty().bind(heightProperty());

        chart.ticker.observe().to(ticker -> {
            plots.clear();

            for (PlotScript script : scripts.findScriptsOn(chart.market.v)) {
                script.plot(chart.market.v, ticker);

                for (PlotDSL plotter : script.plotters) {
                    if (!plotter.lines.isEmpty()) {
                        plots.add(new PlotScriptChart(plotter));
                    }
                }
            }
        });

        Viewtify.clip(this);

        layoutCandle.layoutBy(widthProperty(), heightProperty())
                .layoutBy(axisX.scroll.valueProperty(), axisX.scroll.visibleAmountProperty())
                .layoutBy(axisY.scroll.valueProperty(), axisY.scroll.visibleAmountProperty())
                .layoutBy(chart.ticker.observe().switchMap(ticker -> ticker.add.startWithNull()));
        layoutCandleLatest.layoutBy(widthProperty(), heightProperty())
                .layoutBy(axisX.scroll.valueProperty(), axisX.scroll.visibleAmountProperty())
                .layoutBy(axisY.scroll.valueProperty(), axisY.scroll.visibleAmountProperty())
                .layoutBy(chart.ticker.observe().switchMap(ticker -> ticker.update.startWithNull()));

        visualizeNotifyPrice();
        visualizeOrderPrice();
        visualizeLatestPrice();
        visualizeMouseTrack();
        visualizeSFDPrice();

        getChildren()
                .addAll(backGridVertical, backGridHorizontal, notifyPrice, orderBuyPrice, orderSellPrice, latestPrice, sfdPrice, candles, candleLatest, candleInfo, mouseTrackHorizontal, mouseTrackVertical);
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

            // Move the start position forward for visual consistency
            long sec = (long) x + chart.ticker.v.span.duration.toSeconds() / 2;

            // Upper Info
            chart.ticker.v.findByEpochSecond(sec).to(tick -> {
                chart.selectDate.text(Chrono.system(tick.start).format(Chrono.DateTime));
                chart.selectHigh.text("H " + tick.highPrice().scale(0));
                chart.selectLow.text("L " + tick.lowPrice().scale(0));
                chart.selectVolume.text("V " + tick.volume().scale(3));
                chart.selectLongVolume.text("B " + tick.buyVolume().scale(3));
                chart.selectShortVolume.text("S " + tick.sellVolume().scale(3));

                GraphicsContext gc = candleInfo.getGraphicsContext2D();
                gc.clearRect(0, 0, candleInfo.getWidth(), candleInfo.getHeight());
                int textY = 15;
                for (PlotScriptChart chart : plots) {
                    int textX = 0;
                    for (LineStyle style : chart.plotter.lines) {
                        if (!style.indicator.isConstant()) {
                            gc.setFill(style.color);
                            gc.fillText(style.indicator.valueAt(tick).toString(), textX, textY, 43);
                            textX += 50;
                        }
                    }
                    textY += 15;
                }
            });
        });

        // remove on exit
        when(User.MouseExit).to(e -> {
            labelX.value.set(-1);
            labelY.value.set(-1);

            mouseTrackVertical.layoutLine.requestLayout();
            mouseTrackHorizontal.layoutLine.requestLayout();

            // upper info
            chart.selectDate.text("");
            chart.selectHigh.text("");
            chart.selectLow.text("");
            chart.selectVolume.text("");
            chart.selectLongVolume.text("");
            chart.selectShortVolume.text("");
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
        chart.market.observe().switchMap(m -> m.orders.manages()).on(Viewtify.UIThread).to(o -> {
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
                .on(Viewtify.UIThread)
                .effectOnLifecycle(disposer -> {
                    TickLable latest = latestPrice.createLabel("最新値");

                    disposer.add(() -> latestPrice.remove(latest));

                    return price -> latest.value.set(price.doubleValue());
                })
                .switchOn(setting.showLatestPrice.observeNow())
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

            // Redraw all candles.
            GraphicsContext gc = candles.getGraphicsContext2D();
            gc.clearRect(0, 0, candles.getWidth(), candles.getHeight());

            // draw chart in visible range
            for (PlotScriptChart chart : plots) {
                chart.initialize(visibleSize);
            }
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

                for (PlotScriptChart chart : plots) {
                    chart.calculate(x, tick);
                }
            });
            for (PlotScriptChart chart : plots) {
                chart.draw();
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

            for (PlotScriptChart chart : plots) {
                chart.drawLatest(x, tick);
            }
        });
    }

    /**
     * 
     */
    static class LineStyle {

        /** The indicator. */
        final Indicator<? extends Number> indicator;

        /** The indicator color. */
        final Color color;

        /** The indicator line width. */
        final double width;

        /** The indicator line style. */
        final double[] dashArray;

        /** The y-axis values. */
        final MutableDoubleList valueY = DoubleLists.mutable.empty();

        /**
         * @param indicator
         * @param style
         */
        LineStyle(Indicator<? extends Number> indicator, Style style) {
            this.indicator = indicator;
            this.color = FXUtils.color(style, "stroke");
            this.width = FXUtils.length(style, "stroke-width");
            this.dashArray = FXUtils.lengths(style, "stroke-dasharray");
        }
    }

    /**
     * 
     */
    static class CandleStyle {

        /** The indicator. */
        final Indicator<Tick> indicator;

        /**
         * @param indicator
         */
        CandleStyle(Indicator<Tick> indicator, Style style) {
            this.indicator = indicator;
        }
    }

    /**
     * 
     */
    private class PlotScriptChart {

        /** The associated script. */
        private final PlotDSL plotter;

        private final Canvas canvas = new Canvas();

        /** The bottom base position. */
        private final double bottomUp;

        /** The x-point of values. */
        private final MutableDoubleList valueX = DoubleLists.mutable.empty();

        /** The max y-value. */
        private double valueYMax = 0;

        /**
         * 
         */
        private PlotScriptChart(PlotDSL plotter) {
            this.plotter = plotter;

            switch (plotter.area) {
            case Up:
                this.bottomUp = 100;
                break;

            case Overlay:
                this.bottomUp = 0;
                break;

            default:
                this.bottomUp = 0;
                break;
            }

            this.canvas.widthProperty().bind(widthProperty());
            this.canvas.heightProperty().bind(heightProperty());
        }

        /**
         * Initialize.
         * 
         * @param size
         */
        private void initialize(int size) {
            valueYMax = 0;

            // ensure size
            valueX.clear();
            for (LineStyle style : plotter.lines) {
                style.valueY.clear();
            }
        }

        /**
         * Pre-calculate each values.
         * 
         * @param x
         * @param tick
         */
        private void calculate(double x, Tick tick) {
            for (LineStyle style : plotter.lines) {
                double calculated = style.indicator.valueAt(tick).doubleValue();

                if (plotter.area == PlotArea.Overlay) {
                    calculated = axisY.getPositionForValue(calculated);
                } else {
                    if (valueYMax < calculated) {
                        valueYMax = calculated;
                    }
                }
                style.valueY.add(calculated);
            }
            valueX.add(x);
        }

        /**
         * Finish drawing chart line.
         */
        private void draw() {
            double height = getHeight();
            double scale = scale();
            GraphicsContext gc = candles.getGraphicsContext2D();

            for (LineStyle style : plotter.lines) {
                if (scale != 1) {
                    for (int i = 0; i < style.valueY.size(); i++) {
                        style.valueY.set(i, height - bottomUp - style.valueY.get(i) * scale);
                    }
                }
                gc.setLineWidth(style.width);
                gc.setStroke(style.color);
                gc.setLineDashes(style.dashArray);
                gc.strokePolyline(valueX.toArray(), style.valueY.toArray(), valueX.size());
            }
        }

        /**
         * Drawing latest chart line.
         * 
         * @param x
         * @param tick
         */
        private void drawLatest(double x, Tick tick) {
            double height = getHeight();
            double scale = scale();
            GraphicsContext gc = candleLatest.getGraphicsContext2D();

            for (LineStyle style : plotter.lines) {
                gc.setLineWidth(style.width);
                gc.setStroke(style.color);
                gc.setLineDashes(style.dashArray);

                if (scale != 1) {
                    gc.strokeLine(valueX.getLast(), style.valueY
                            .getLast(), x, height - bottomUp - style.indicator.valueAt(tick).doubleValue() * scale);
                } else {
                    gc.strokeLine(valueX.getLast(), style.valueY.getLast(), x, axisY
                            .getPositionForValue(style.indicator.valueAt(tick).doubleValue()));
                }
            }
        }

        /**
         * Calculate scale.
         * 
         * @return
         */
        private double scale() {
            if (plotter.area != PlotArea.Overlay) {
                return 50 < valueYMax ? 50 / valueYMax : 1;
            } else {
                return 1;
            }
        }
    }

    /**
     * @version 2018/01/12 21:54:07
     */
    @SuppressWarnings("unused")
    private class TopMark extends Path {

    }

    /**
     * @version 2018/01/09 0:19:26
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
}
