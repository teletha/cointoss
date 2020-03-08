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

import static transcript.Transcript.en;

import java.time.Duration;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;
import java.util.function.ToDoubleFunction;

import javafx.collections.ObservableList;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Region;
import javafx.scene.paint.Color;
import javafx.scene.shape.LineTo;
import javafx.scene.shape.MoveTo;
import javafx.scene.shape.Path;
import javafx.scene.shape.PathElement;
import javafx.scene.text.Font;

import org.eclipse.collections.api.list.primitive.MutableDoubleList;
import org.eclipse.collections.impl.list.mutable.primitive.DoubleArrayList;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;

import cointoss.Market;
import cointoss.MarketService;
import cointoss.market.bitflyer.BitFlyer;
import cointoss.market.bitflyer.SFD;
import cointoss.ticker.AbstractIndicator;
import cointoss.ticker.Indicator;
import cointoss.ticker.Tick;
import cointoss.ticker.Ticker;
import cointoss.util.Chrono;
import cointoss.util.Num;
import cointoss.util.Primitives;
import kiss.I;
import kiss.Variable;
import kiss.Ⅲ;
import stylist.Style;
import trademate.chart.Axis.TickLable;
import trademate.chart.PlotScript.Plotter;
import trademate.setting.Notificator;
import viewtify.Viewtify;
import viewtify.ui.helper.LayoutAssistant;
import viewtify.ui.helper.StyleHelper;
import viewtify.ui.helper.User;
import viewtify.ui.helper.UserActionHelper;
import viewtify.util.FXUtils;

public class ChartCanvas extends Region implements UserActionHelper<ChartCanvas> {

    /** Name Font */
    private static final Font NameFont = Font.font(Font.getDefault().getName(), 24);

    /** Name Color */
    private static final Color NameColor = Color.rgb(50, 50, 50);

    /** Infomation Font */
    private static final Font InfoFont = Font.font(Font.getDefault().getName(), 10.5);

    /** Infomation Color */
    private static final Color InfoColor = Color.rgb(247, 239, 227);

    /** The candle width. */
    private static final int BarWidth = 3;

    /** The chart node. */
    private final ChartView chart;

    /** The horizontal axis. (shortcut) */
    private final Axis axisX;

    /** The vertical axis. (shortcut) */
    private final Axis axisY;

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
    private final Canvas name = new Canvas();

    /** Chart UI */
    private final Canvas chartInfo = new Canvas();

    /** Flag whether candle chart shoud layout on the next rendering phase or not. */
    private final LayoutAssistant layoutCandle = new LayoutAssistant(this);

    /** Flag whether candle chart shoud layout on the next rendering phase or not. */
    private final LayoutAssistant layoutCandleLatest = new LayoutAssistant(this);

    /** The script registry. */
    private final PlotScriptRegistry registry = I.make(PlotScriptRegistry.class);

    /** The associated plot scripts. */
    private Plotter[] plotters = new Plotter[0];

    /** The number of plot scripts. */
    private List<PlotScript> scripts;

    /** The cache by span. */
    private LoadingCache<Ⅲ<Market, Ticker, ObservableList<Supplier<PlotScript>>>, Plotter[]> plottersCache = CacheBuilder.newBuilder()
            .maximumSize(7)
            .expireAfterAccess(Duration.ofHours(1))
            .build(new CacheLoader<>() {

                @Override
                public Plotter[] load(Ⅲ<Market, Ticker, ObservableList<Supplier<PlotScript>>> v) throws Exception {
                    List<PlotScript> registered = registry.findPlottersBy(v.ⅰ, v.ⅱ);
                    List<PlotScript> additional = I.signal(v.ⅲ).map(Supplier::get).toList();

                    List<Plotter> combined = I.signal(registered, additional)
                            .flatIterable(list -> list)
                            .effect(script -> script.initialize(v.ⅰ, v.ⅱ))
                            .flatIterable(script -> script.plotters.values())
                            .skip(plotter -> plotter.lines.isEmpty() && plotter.horizons.isEmpty() && plotter.candles.isEmpty())
                            .toList();

                    return combined.toArray(new Plotter[combined.size()]);
                }
            });

    /** The size of chart infomation area. */
    private final int chartInfoWidth = 60;

    /** The size of chart infomation area. */
    private final int chartInfoHeight = 16;

    /** The size of chart infomation area. */
    private final int chartInfoLeftPadding = 10;

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
        this.name.widthProperty().bind(widthProperty());
        this.name.heightProperty().bind(heightProperty());

        chart.market.observe().to(this::drawMarketName);
        chart.market.observe().combineLatest(chart.ticker.observe(), Viewtify.observing(chart.scripts)).to(v -> {
            plotters = plottersCache.getUnchecked(v);
            scripts = I.signal(plotters).map(p -> p.origin).distinct().toList();
        });

        layoutCandle.layoutBy(widthProperty(), heightProperty())
                .layoutBy(axisX.scroll.valueProperty(), axisX.scroll.visibleAmountProperty())
                .layoutBy(axisY.scroll.valueProperty(), axisY.scroll.visibleAmountProperty())
                .layoutBy(chart.candleType.observe())
                .layoutBy(chart.ticker.observe().switchMap(ticker -> ticker.open.startWithNull().throttle(50, TimeUnit.MILLISECONDS)));
        layoutCandleLatest.layoutBy(widthProperty(), heightProperty())
                .layoutBy(axisX.scroll.valueProperty(), axisX.scroll.visibleAmountProperty())
                .layoutBy(axisY.scroll.valueProperty(), axisY.scroll.visibleAmountProperty())
                .layoutBy(chart.candleType.observe())
                .layoutBy(chart.ticker.observe().switchMap(ticker -> ticker.update.startWithNull().throttle(50, TimeUnit.MILLISECONDS)))
                .layoutWhile(chart.showRealtimeUpdate.observing());

        configIndicator();
        visualizeNotifyPrice();
        visualizeOrderPrice();
        visualizeLatestPrice();
        visualizeMouseTrack();
        visualizeSFDPrice();

        getChildren()
                .addAll(name, backGridVertical, backGridHorizontal, notifyPrice, orderBuyPrice, orderSellPrice, latestPrice, sfdPrice, candles, candleLatest, chartInfo, mouseTrackHorizontal, mouseTrackVertical);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Object ui() {
        return this;
    }

    /**
     * Configure indicator setting.
     */
    private void configIndicator() {
        when(User.MouseClick).to(e -> {
            findScriptByPosition(e).to(script -> {
                registry.globalSetting(script).toggleVisible();

                // redraw
                layoutCandle.requestLayout();
                layoutCandleLatest.requestLayout();
                Tick tick = findTickByPostion(e);
                if (tick != null) {
                    drawChartInfo(tick);
                }
            });
        });
    }

    /**
     * Find the {@link PlotScript} by the mouse position.
     * 
     * @param e
     * @return
     */
    private Variable<PlotScript> findScriptByPosition(MouseEvent e) {
        double x = e.getX();
        double y = e.getY();

        if (scripts != null && x < chartInfoLeftPadding + chartInfoWidth && y < (scripts.size() + 1) * chartInfoHeight) {
            int index = (int) (y / chartInfoHeight) - 1;

            if (0 <= index) {
                return Variable.of(scripts.get(index));
            }
        }
        return Variable.empty();
    }

    /**
     * Visualize mouse tracker in chart.
     */
    private void visualizeMouseTrack() {
        TickLable labelX = mouseTrackVertical.createLabel()
                .formatter(v -> Chrono.systemBySeconds((long) v).format(Chrono.DateTimeWithoutSec));
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
            Tick tick = chart.ticker.v.ticks.getByTime(sec);

            if (tick != null) {
                drawChartInfo(tick);
            }
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
     * Find the {@link Tick} by the mouse position.
     * 
     * @param e
     * @return
     */
    private Tick findTickByPostion(MouseEvent e) {
        double x = axisX.getValueForPosition(e.getX());

        // move the start position forward for visual consistency
        long sec = (long) x + chart.ticker.v.span.duration.toSeconds() / 2;

        return chart.ticker.v.ticks.getByTime(sec);
    }

    /**
     * Visualize notifiable price in chart.
     */
    private void visualizeNotifyPrice() {
        when(User.ContextMenu, e -> {
            e.consume();

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

                MarketService service = chart.market.v.service;
                Num p = exe.price.scale(service.setting.targetCurrencyScaleSize);
                String title = "🔊  " + service.marketReadableName() + " " + p;
                CharSequence message = en("The specified price ({0}) has been reached.").with(p);
                System.out.println("remove line");
                I.make(Notificator.class).priceSignal.notify(title, message);
            }));
        });
    }

    /**
     * Visualize order price in chart.
     */
    private void visualizeOrderPrice() {
        chart.market.observe()
                .switchMap(m -> m.orders.manages())
                .switchOn(chart.showOrderSupport.observing())
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
        chart.market.observing() //
                .skipNull()
                .switchMap(m -> m.tickers.latestPrice.observing())
                .on(Viewtify.UIThread)
                .effectOnLifecycle(disposer -> {
                    TickLable latest = latestPrice.createLabel("最新値");

                    disposer.add(() -> {
                        latestPrice.remove(latest);
                    });

                    return price -> latest.value.set(price.doubleValue());
                })
                .subscribeOn(Viewtify.UIThread)
                .switchOn(chart.showLatestPrice.observing())
                .to(latestPrice.layoutLine::requestLayout);
    }

    /**
     * Visualize SFD price in chart.
     */
    private void visualizeSFDPrice() {
        chart.market.observe().on(Viewtify.UIThread).to(market -> {
            if (market.service == BitFlyer.FX_BTC_JPY) {
                for (SFD sfd : SFD.values()) {
                    TickLable label = sfdPrice.createLabel("乖離" + sfd.percentage + "%");
                    market.service.add(sfd.boundary().switchOn(chart.showRealtimeUpdate.observing()).on(Viewtify.UIThread).to(price -> {
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
            // estimate visible range
            long start = (long) axisX.computeVisibleMinValue();
            long end = (long) axisX.computeVisibleMaxValue() - chart.ticker.v.span.seconds;

            // Estimate capacity, but a little larger as insurance (+2) to avoid re-copying the
            // array of capacity increase.
            double tickSize = ((end - start) / chart.ticker.v.span.seconds) + 2;
            boolean needDrawingOpenAndClose = tickSize * 0.3 < candles.getWidth();

            // redraw all candles.
            GraphicsContext gc = candles.getGraphicsContext2D();
            gc.clearRect(0, 0, candles.getWidth(), candles.getHeight());

            // draw chart in visible range
            for (Plotter plotter : plotters) {
                plotter.lineMaxY = 0;

                // ensure size
                for (LineChart chart : plotter.lines) {
                    chart.valueY.clear();
                }
            }

            MutableDoubleList valueX = new NoCopyDoubleList((int) tickSize);

            chart.ticker.v.ticks.each(start, end, tick -> {
                double x = axisX.getPositionForValue(tick.startSeconds);
                double open = axisY.getPositionForValue(tick.openPrice.doubleValue());
                double close = axisY.getPositionForValue(tick.closePrice().doubleValue());
                double high = axisY.getPositionForValue(tick.highPrice().doubleValue());
                double low = axisY.getPositionForValue(tick.lowPrice().doubleValue());

                gc.setStroke(chart.candleType.v.coordinator.apply(tick));
                gc.setLineWidth(1);
                gc.strokeLine(x, high, x, low);
                if (needDrawingOpenAndClose) {
                    gc.setLineWidth(BarWidth);
                    gc.strokeLine(x, open, x, close);
                }

                for (Plotter plotter : plotters) {
                    if (registry.globalSetting(plotter.origin).visible.is(false)) {
                        continue;
                    }

                    for (LineChart chart : plotter.lines) {
                        double calculated = chart.indicator.applyAsDouble(tick);

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

                    // draw candle mark
                    for (CandleMark mark : plotter.candles) {
                        if (mark.indicator.valueAt(tick)) {
                            gc.setFill(mark.color);
                            gc.fillOval(x - (BarWidth / 2), high - BarWidth - 2, BarWidth, BarWidth);
                        }
                    }
                }
                valueX.add(x);
            });

            double[] arrayX = valueX.toArray();
            double width = candles.getWidth();
            double height = candles.getHeight();

            for (Plotter plotter : plotters) {
                if (registry.globalSetting(plotter.origin).visible.is(false)) {
                    continue;
                }

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
            if (chart.ticker.v.ticks.isEmpty()) {
                return;
            }

            GraphicsContext gc = candleLatest.getGraphicsContext2D();
            gc.clearRect(0, 0, candleLatest.getWidth(), candleLatest.getHeight());

            Tick tick = chart.ticker.v.ticks.last();

            double x = axisX.getPositionForValue(tick.startSeconds);
            double open = axisY.getPositionForValue(tick.openPrice.doubleValue());
            double close = axisY.getPositionForValue(tick.closePrice().doubleValue());
            double high = axisY.getPositionForValue(tick.highPrice().doubleValue());
            double low = axisY.getPositionForValue(tick.lowPrice().doubleValue());

            gc.setStroke(chart.candleType.v.coordinator.apply(tick));
            gc.setLineWidth(1);
            gc.strokeLine(x, high, x, low);
            gc.setLineWidth(BarWidth);
            gc.strokeLine(x, open, x, close);

            if (tick.previous() != null) {
                double lastX = axisX.getPositionForValue(tick.previous().startSeconds);

                for (Plotter plotter : plotters) {
                    if (registry.globalSetting(plotter.origin).visible.is(false)) {
                        continue;
                    }

                    double height = getHeight();
                    double scale = plotter.scale();

                    for (LineChart chart : plotter.lines) {
                        if (!chart.valueY.isEmpty()) {
                            gc.setLineWidth(chart.width);
                            gc.setStroke(chart.color);
                            gc.setLineDashes(chart.dashArray);
                            gc.strokeLine(lastX, chart.valueY.getLast(), x, plotter.area == PlotArea.Main
                                    ? axisY.getPositionForValue(chart.indicator.applyAsDouble(tick))
                                    : height - plotter.area.offset - chart.indicator.applyAsDouble(tick) * scale);
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
        gc.setFont(InfoFont);

        int base = chart.market.v.service.setting.baseCurrencyScaleSize;
        String date = Chrono.systemByMills(tick.startSeconds * 1000).format(Chrono.DateTime);
        String open = "O" + tick.openPrice.scale(base);
        String high = "H" + tick.highPrice().scale(base);
        String low = "L" + tick.lowPrice().scale(base);
        String close = "C" + tick.closePrice().scale(base);

        int gap = 3;
        int largeWidth = chartInfoWidth * 2 + gap;
        int y = chartInfoHeight;
        gc.setFill(InfoColor);
        gc.fillText(date, chartInfoLeftPadding, y, largeWidth);
        gc.fillText(open, chartInfoLeftPadding + largeWidth + gap, y, chartInfoWidth);
        gc.fillText(high, chartInfoLeftPadding + largeWidth + chartInfoWidth + gap * 2, y, chartInfoWidth);
        gc.fillText(low, chartInfoLeftPadding + largeWidth + chartInfoWidth * 2 + gap * 3, y, chartInfoWidth);
        gc.fillText(close, chartInfoLeftPadding + largeWidth + chartInfoWidth * 3 + gap * 4, y, chartInfoWidth);

        // indicator values drawn from the same plot script are displayed on the same line
        int x = 0;
        Object origin = null;
        for (Plotter plotter : plotters) {
            boolean visible = registry.globalSetting(plotter.origin).visible.v;

            if (origin != plotter.origin) {
                y += chartInfoHeight;
                x = chartInfoLeftPadding;
                origin = plotter.origin;
                gc.setFill(visible ? InfoColor : InfoColor.deriveColor(0, 1, 1, 0.4));
                gc.fillText(plotter.origin.toString(), x, y, chartInfoWidth);
                x += chartInfoWidth + gap;
            }
            for (LineChart chart : plotter.lines) {
                gc.setFill(visible ? chart.color : chart.color.deriveColor(0, 1, 1, 0.4));
                gc.fillText(chart.info.valueAt(tick), x, y, chartInfoWidth);
                x += chartInfoWidth + gap;
            }
        }
    }

    /**
     * Draw market name in background.
     */
    private void drawMarketName(Market m) {
        GraphicsContext gc = name.getGraphicsContext2D();
        gc.clearRect(0, 0, name.getWidth(), name.getHeight());
        gc.setFont(NameFont);
        gc.setFill(NameColor);
        gc.fillText(m.service.marketReadableName(), 4, NameFont.getSize() * 1.2);
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
        private final ToDoubleFunction<Tick> indicator;

        /** The infomation writer. */
        private final Indicator<String> info;

        /** The indicator color. */
        private final Color color;

        /** The indicator line width. */
        private final double width;

        /** The indicator line style. */
        private final double[] dashArray;

        /** The y-axis values. */
        private final MutableDoubleList valueY = new NoCopyDoubleList(64);

        /**
         * @param indicator
         * @param style
         * @param info
         */
        LineChart(AbstractIndicator<? extends Number, ?> indicator, Style style, Indicator<String> info) {
            this.indicator = tick -> indicator.valueAt(tick).doubleValue();
            this.color = FXUtils.color(style, "stroke");
            this.width = FXUtils.length(style, "stroke-width", 1);
            this.dashArray = FXUtils.lengths(style, "stroke-dasharray");
            this.info = info == null ? indicator.map(v -> v.toString()) : info;
        }
    }

    /**
     * 
     */
    static class CandleMark {

        /** The indicator. */
        private final Indicator<Boolean> indicator;

        /** The indicator color. */
        private final Color color;

        /**
         * @param indicator
         * @param style
         */
        CandleMark(Indicator<Boolean> indicator, Style style) {
            this.indicator = indicator;
            this.color = FXUtils.color(style, "fill");
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
                        if (Primitives.within(0, value, ChartCanvas.this.getWidth())) {
                            move.setX(value);
                            move.setY(0);
                            line.setX(value);
                            line.setY(getHeight());
                        } else {
                            move.setX(0);
                            move.setY(0);
                            line.setX(0);
                            line.setY(0);
                        }
                    } else {
                        if (Primitives.within(0, value, ChartCanvas.this.getHeight())) {
                            move.setX(0);
                            move.setY(value);
                            line.setX(getWidth());
                            line.setY(value);
                        } else {
                            move.setX(0);
                            move.setY(0);
                            line.setX(0);
                            line.setY(0);
                        }
                    }
                }
            });
        }
    }

    /**
     * 
     */
    private static class NoCopyDoubleList extends DoubleArrayList {

        private NoCopyDoubleList(int initialSize) {
            super(initialSize);
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public double[] toArray() {
            return items;
        }
    }
}
