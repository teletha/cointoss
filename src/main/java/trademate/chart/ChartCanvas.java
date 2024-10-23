/*
 * Copyright (C) 2024 The COINTOSS Development Team
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          https://opensource.org/licenses/MIT
 */
package trademate.chart;

import static java.lang.Boolean.*;
import static java.util.concurrent.TimeUnit.*;

import java.time.Duration;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.function.Predicate;
import java.util.function.Supplier;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;

import cointoss.Market;
import cointoss.execution.Execution;
import cointoss.ticker.AbstractIndicator;
import cointoss.ticker.Indicator;
import cointoss.ticker.Tick;
import cointoss.ticker.Ticker;
import cointoss.util.Chrono;
import cointoss.util.DateRange;
import hypatia.Num;
import hypatia.Primitives;
import javafx.beans.Observable;
import javafx.beans.property.DoubleProperty;
import javafx.collections.ObservableList;
import javafx.scene.Node;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Region;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import kiss.Disposable;
import kiss.I;
import kiss.Signal;
import kiss.Variable;
import kiss.Ⅲ;
import primavera.array.DoubleList;
import stylist.Style;
import trademate.ChartTheme;
import trademate.CommonText;
import trademate.TradingView;
import trademate.chart.Axis.TickLable;
import trademate.chart.PlotScript.Plotter;
import trademate.chart.line.LineMark;
import trademate.chart.line.PriceNotify;
import trademate.chart.part.ChartPart;
import trademate.chart.part.MarketInfoPart;
import trademate.chart.part.OrderBookPart;
import trademate.chart.part.PriceRangedVolumePart;
import trademate.order.OrderView;
import trademate.setting.PerformanceSetting;
import viewtify.Viewtify;
import viewtify.preference.Preferences;
import viewtify.ui.canvas.EnhancedCanvas;
import viewtify.ui.helper.LayoutAssistant;
import viewtify.ui.helper.User;
import viewtify.ui.helper.UserActionHelper;
import viewtify.util.FXUtils;

public class ChartCanvas extends Region implements UserActionHelper<ChartCanvas> {

    private static final PerformanceSetting performance = Preferences.of(PerformanceSetting.class);

    /** Infomation Font */
    private static final Font InfoFont = Font.font(10.5);

    /** Infomation Color */
    private static final Color InfoColor = Color.rgb(247, 239, 227);

    /** The candle width. */
    private static final int BarWidth = 3;

    /** The size of chart infomation area. */
    private static final int chartInfoTitle = 105;

    /** The size of chart infomation area. */
    private static final int chartInfoWidth = 75;

    /** The size of chart infomation area. */
    private static final int chartInfoHeight = 16;

    /** The size of chart infomation area. */
    public static final int chartInfoLeftPadding = 4;

    /** The size of chart infomation area. */
    private static final int chartInfoTopPadding = 35;

    /** The size of chart infomation area. */
    private static final int chartInfoHorizontalGap = 3;

    /** The chart node. */
    public final ChartView chart;

    /** The horizontal axis. (shortcut) */
    public final Axis axisX;

    /** The vertical axis. (shortcut) */
    public final Axis axisY;

    /** Chart UI */
    private final LineMark backGridVertical;

    /** Chart UI */
    private final LineMark backGridHorizontal;

    /** Chart UI */
    private final LineMark mouseTrackVertical;

    /** Chart UI */
    private final LineMark mouseTrackHorizontal;

    /** Chart UI */
    private final PriceNotify notifyPrice;

    /** Chart UI */
    private final LineMark latestPrice;

    /** Chart UI */
    private final LineMark orderBuyPrice;

    /** Chart UI */
    private final LineMark orderSellPrice;

    /** Flag whether candle chart should layout on the next rendering phase or not. */
    public final LayoutAssistant layoutCandle = new LayoutAssistant(this);

    /** Flag whether candle chart should layout on the next rendering phase or not. */
    private final LayoutAssistant layoutCandleLatest = new LayoutAssistant(this);

    /** Chart UI */
    private final EnhancedCanvas candles = new EnhancedCanvas().visibleWhen(layoutCandle.canLayout);

    /** Chart UI */
    private final EnhancedCanvas candleLatest = new EnhancedCanvas().visibleWhen(layoutCandleLatest.canLayout);

    /** Chart UI */
    private final EnhancedCanvas chartInfo = new EnhancedCanvas();

    /** Chart UI */
    private final EnhancedCanvas supporter = new EnhancedCanvas();

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

    /** The managed parts. */
    private final List<ChartPart> parts;

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
        this.backGridVertical = new LineMark(this, axisX.forGrid, axisX, ChartStyles.BackGrid);
        this.backGridHorizontal = new LineMark(this, axisY.forGrid, axisY, ChartStyles.BackGrid);
        this.mouseTrackVertical = new LineMark(this, axisX, ChartStyles.MouseTrack);
        this.mouseTrackHorizontal = new LineMark(this, axisY, ChartStyles.MouseTrack);
        this.notifyPrice = new PriceNotify(this);
        this.latestPrice = new LineMark(this, axisY, ChartStyles.PriceLatest);
        this.orderBuyPrice = new LineMark(this, axisY, ChartStyles.OrderSupportBuy);
        this.orderSellPrice = new LineMark(this, axisY, ChartStyles.OrderSupportSell);
        parts = List.of(new MarketInfoPart(this), new OrderBookPart(this, chart), new PriceRangedVolumePart(this, chart));

        layoutCandle.layoutBy(chartAxisModification())
                .layoutBy(userInterfaceModification())
                .layoutBy(chart.candleType.observe(), chart.ticker.observe(), chart.showCandle.observe())
                .layoutBy(chart.ticker.observe().switchMap(ticker -> ticker.open.throttle(performance.refreshRate, TimeUnit.MILLISECONDS)))
                .layoutBy(ChartTheme.$.buy.observe(), ChartTheme.$.sell.observe())
                .layoutWhile(chart.showRealtimeUpdate.observing());

        layoutCandleLatest.layoutBy(chartAxisModification())
                .layoutBy(userInterfaceModification())
                .layoutBy(chart.candleType.observe(), chart.ticker.observe(), chart.showCandle.observe())
                .layoutBy(chart.market.observe()
                        .switchMap(market -> market.timeline.throttle(performance.refreshRate, TimeUnit.MILLISECONDS)))
                .layoutBy(ChartTheme.$.buy.observe(), ChartTheme.$.sell.observe())
                .layoutWhile(chart.showRealtimeUpdate.observing());

        chart.showRealtimeUpdate.observing().on(Viewtify.UIThread).to(show -> {
            if (show) {
                onShow();
            } else {
                onHide();
            }
        });

        configIndicator();
        configScript();
        makeChartScrollable();
        visualizeLatestPrice();
        visualizeMouseTrack();
        visualizePriceSupporter();

        ObservableList<Node> children = getChildren();
        children.addAll(backGridVertical, backGridHorizontal, notifyPrice, orderBuyPrice, orderSellPrice, latestPrice);
        I.signal(parts).flatIterable(x -> x.managed).to(children::add);
        children.addAll(candles, candleLatest, chartInfo, supporter, mouseTrackHorizontal, mouseTrackVertical);
    }

    private Signal userInterfaceModification() {
        return Viewtify.observe(widthProperty())
                .merge(Viewtify.observe(heightProperty()))
                .debounce(performance.refreshRate, TimeUnit.MILLISECONDS, false);
    }

    private Observable[] chartAxisModification() {
        return new DoubleProperty[] {axisX.scroll.valueProperty(), axisX.scroll.visibleAmountProperty()};
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Object ui() {
        return this;
    }

    /**
     * Invoke when the canvas is shown.
     */
    private void onShow() {
        candles.bindSizeTo(this);
        candleLatest.bindSizeTo(this);
        chartInfo.bindSizeTo(this);
        supporter.bindSizeTo(this);

        chart.chart.layoutForcely();
    }

    /**
     * Invoke when the canvas is hidden.
     */
    private void onHide() {
        candles.clear().size(0, 0);
        candleLatest.clear().size(0, 0);
        chartInfo.clear().size(0, 0);
        supporter.clear().size(0, 0);
    }

    /**
     * Find the {@link PlotScript} by info text position.
     * 
     * @param e
     * @return
     */
    private Variable<PlotScript> findScriptByInfoText(MouseEvent e) {
        double x = e.getX();
        double y = e.getY() - chartInfoTopPadding;

        if (scripts != null && x < chartInfoLeftPadding + chartInfoTitle && y < (scripts.size() + 1) * chartInfoHeight) {
            int index = (int) (y / chartInfoHeight) - 1;

            if (0 <= index) {
                return Variable.of(scripts.get(index));
            }
        }
        return Variable.empty();
    }

    /**
     * Find the {@link LineChart} by info text position.
     * 
     * @param e
     * @return
     */
    private Variable<LineChart> findLineChartByInfoText(MouseEvent e) {
        double x = e.getX();
        double y = e.getY() - chartInfoTopPadding;

        if (scripts != null && chartInfoLeftPadding + chartInfoTitle < x && y < (scripts.size() + 1) * chartInfoHeight) {
            int indexY = (int) (y / chartInfoHeight) - 1;

            if (0 <= indexY) {
                int indexX = (int) ((x - chartInfoLeftPadding - chartInfoTitle) / (chartInfoWidth + chartInfoHorizontalGap));
                return I.signal(scripts.get(indexY))
                        .flatIterable(s -> s.plotters.values())
                        .flatIterable(p -> p.lines)
                        .takeAt(i -> i == indexX)
                        .first()
                        .to();
            }
        }
        return Variable.empty();
    }

    /**
     * Find the {@link LineChart} by chart position.
     * 
     * @return
     */
    private Signal<LineChart> findLineChartByChart(MouseEvent e) {
        double x = axisX.getValueForPosition(e.getX());
        double y = e.getY();
        double min = y - 4;
        double max = y + 4;

        // move the start position forward for visual consistency
        long sec = (long) x + chart.ticker.v.span.duration.toSeconds() / 2;

        // estimate visible range
        long start = (long) axisX.computeVisibleMinValue();
        int index = (int) ((sec - start) / chart.ticker.v.span.seconds);

        return I.signal(plotters).flatIterable(p -> p.lines).take(chart -> {
            double value = chart.valueY.get(index);
            return min <= value && value <= max;
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

        return chart.ticker.v.ticks.at(sec);
    }

    /**
     * Configure indicator setting.
     */
    private void configIndicator() {
        when(User.LeftClick).to(e -> {
            findScriptByInfoText(e).to(script -> {
                registry.globalSetting(script).toggleVisible();

                // redraw
                layoutCandle.requestLayout();
                layoutCandleLatest.requestLayout();
                Tick tick = findTickByPostion(e);
                if (tick != null) {
                    drawChartInfo(tick);
                }
            });

            findLineChartByInfoText(e).to(chart -> {
                chart.toggleVisible();

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
     * Config script setting.
     */
    private void configScript() {
        chart.market.observe().combineLatest(chart.ticker.observe(), Viewtify.observing(chart.scripts)).to(v -> {
            plotters = plottersCache.getUnchecked(v);
            scripts = I.signal(plotters).map(p -> p.origin).distinct().toList();
        });
    }

    /**
     * Make chart scrollable by mouse drag.
     */
    private void makeChartScrollable() {
        when(User.Scroll).to(e -> {
            axisX.zoom(e);
        });
        when(User.MouseDrag).take(MouseEvent::isPrimaryButtonDown)
                .buffer(2, 1)
                .takeUntil(when(User.MouseRelease).take(e -> e.getButton() == MouseButton.PRIMARY))
                .repeat()
                .to(e -> {
                    double prev = e.get(0).getX();
                    double now = e.get(1).getX();

                    if (prev != now) {
                        double visibleDuration = axisX.computeVisibleMaxValue() - axisX.computeVisibleMinValue();
                        double logicalDuration = axisX.logicalMaxValue.get() - axisX.logicalMinValue.get();
                        double movedDuration = visibleDuration / candles.widthProperty().get() * (now - prev);
                        double ratio = movedDuration / (logicalDuration * (1 - axisX.scroll.getVisibleAmount()));
                        if (ratio != 0 && Double.isFinite(ratio)) {
                            axisX.scroll.setValue(Primitives.between(0, axisX.scroll.getValue() - ratio, 1));
                        }
                    }
                });

        // reduce drawing chart on scroll
        Viewtify.observe(axisX.scroll.valueProperty())
                .merge(Viewtify.observe(axisX.scroll.visibleAmountProperty()))
                .debounce(150, MILLISECONDS, true)
                .toggle(Boolean.FALSE, Boolean.TRUE)
                .to(show -> {
                    chart.showIndicator.set(show);
                    if (show) layoutCandle.layoutForcely();
                });
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
            chart.ticker.to(ticker -> {
                double x = axisX.getValueForPosition(e.getX());
                double y = e.getY();
                labelX.value.set(x);
                labelY.value.set(axisY.getValueForPosition(y));

                mouseTrackVertical.layoutLine.requestLayout();
                mouseTrackHorizontal.layoutLine.requestLayout();

                // move the start position forward for visual consistency
                long sec = (long) x + ticker.span.duration.toSeconds() / 2;

                // update upper info
                Tick tick = chart.ticker.v.ticks.at(sec);

                if (tick != null) {
                    drawChartInfo(tick);
                }
            });
        });

        // remove on exit
        when(User.MouseExit).to(e -> {
            labelX.value.set(-1);
            labelY.value.set(-1);

            mouseTrackVertical.layoutLine.requestLayout();
            mouseTrackHorizontal.layoutLine.requestLayout();

            // clear mouse related info
            chartInfo.clear();
        });
    }

    /**
     * Visualize entry supporter or notifiable price in chart.
     */
    private void visualizePriceSupporter() {
        Predicate<MouseEvent> RightButton = e -> e.getButton() == MouseButton.SECONDARY;

        when(User.click(MouseButton.PRIMARY)).to(e -> {
            System.out.println("Click");
        });

        when(User.MousePress).take(RightButton).to(pressed -> {

            Disposable dispose = Disposable.empty();
            when(User.MouseDrag).take(RightButton).to(dragged -> {
                drawPriceSupporter(pressed, dragged);
            }, dispose);

            when(User.MouseRelease).take(RightButton).take(1).to(released -> {
                if (pressed.getX() != released.getX() || pressed.getY() != released.getY()) {
                    supporter.clear();

                    if (released.isControlDown() && released.isShiftDown()) {
                        ZonedDateTime start = Chrono.utcBySeconds((long) axisX.getValueForPosition(pressed.getX()));
                        ZonedDateTime end = Chrono.utcBySeconds((long) axisX.getValueForPosition(released.getX()));
                        DateRange range = DateRange.between(start, end);

                        chart.findAncestorView(TradingView.class).to(view -> {
                            view.buildTicker(range, true, released.isAltDown());
                        });
                    }
                } else {
                    notifyPrice.notifyByPrice(released);
                }
            }, dispose);
        });
    }

    private void drawPriceSupporter(MouseEvent start, MouseEvent end) {
        double startX = start.getX();
        double startY = start.getY();
        double endX = end.getX();
        double endY = end.getY();

        GraphicsContext gc = supporter.getGraphicsContext2D();
        gc.setFill(Color.WHITE.deriveColor(0, 1, 1, 0.15));
        gc.setStroke(Color.WHITE.deriveColor(0, 1, 1, 0.7));

        gc.clearRect(0, 0, supporter.getWidth(), supporter.getHeight());
        gc.strokeLine(startX, startY, endX, endY);

        long startTime = (long) axisX.getValueForPosition(startX);
        long endTime = (long) axisX.getValueForPosition(endX);
        double upperPrice = axisY.getValueForPosition(startY);
        double lowerPrice = axisY.getValueForPosition(endY);

        int scale = chart.market.v.service.setting.base.scale;
        double textX = endX - 40;
        double lineY = -12;
        gc.setStroke(Color.WHITESMOKE);
        gc.setFont(Font.font(10));
        gc.strokeText(I.translate("Duration") + "\t" + Chrono.formatAsDuration(Math.abs(endTime - startTime) * 1000), textX, endY + lineY);
        gc.strokeText(I.translate("Spread") + "\t" + Primitives
                .roundString(Math.abs(upperPrice - lowerPrice), scale), textX, endY + lineY * 2);

        if (OrderView.ActiveMarket.equals(chart.market)) {
            I.make(OrderView.class).setPriceRange(Num.of(upperPrice).scale(scale), Num.of(lowerPrice).scale(scale));
        }
    }

    /**
     * Visualize latest price in chart.
     */
    private void visualizeLatestPrice() {
        chart.market.observing()
                .skipNull()
                .switchMap(m -> m.tickers.latest.observing().map(Execution::price))
                .plug(PerformanceSetting.applyUIRefreshRate())
                .switchOn(chart.showRealtimeUpdate.observing())
                .on(Viewtify.UIThread)
                .effectOnLifecycle(disposer -> {
                    TickLable latest = latestPrice.createLabel("最新値");

                    disposer.add(() -> {
                        latestPrice.removeLabel(latest);
                    });

                    return price -> latest.value.set(price.doubleValue());
                })
                .subscribeOn(Viewtify.UIThread)
                .switchOn(chart.showLatestPrice.observing())
                .to(latestPrice.layoutLine::requestLayout);
    }

    /**
     * 
     */
    void layoutForcely() {
        layoutCandle.layoutForcely();
        layoutCandleLatest.layoutForcely();
        for (ChartPart part : parts) {
            part.layout.layoutForcely();
        }
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
        orderBuyPrice.draw();
        orderSellPrice.draw();

        drawCandle();
        for (ChartPart part : parts) {
            part.draw();
        }
    }

    /**
     * Draw candle chart.
     */
    private void drawCandle() {
        layoutCandle.layout(() -> {
            chart.ticker.to(ticker -> {
                // estimate visible range
                chart.ticker.map(t -> t.latest().openTime() - ticker.span.seconds).to(end -> {
                    long startVisible = (long) axisX.computeVisibleMinValue();
                    long endVisible = Math.min((long) axisX.computeVisibleMaxValue(true), end);

                    // Estimate capacity, but a little larger as insurance (+2) to avoid re-copying
                    // the array of capacity increase.
                    double tickSize = ((endVisible - startVisible) / ticker.span.seconds) + 2;
                    boolean needDrawingOpenAndClose = tickSize * 0.4 <= candles.getWidth();

                    // redraw all candles.
                    GraphicsContext gc = candles.getGraphicsContext2D();
                    gc.clearRect(0, 0, candles.getWidth(), candles.getHeight());

                    // draw chart in visible range
                    if (chart.showIndicator.is(TRUE)) {
                        for (Plotter plotter : plotters) {
                            plotter.lineMaxY = 0;

                            // ensure size
                            for (LineChart chart : plotter.lines) {
                                chart.valueY.clear();
                            }
                        }
                    }

                    CandleType candleType = chart.candleType.value();
                    double width = candles.getWidth();
                    double height = candles.getHeight();
                    DoubleList valueX = new DoubleList((int) tickSize);
                    Indicator<double[]> candle = candleType.candles.apply(ticker);

                    ticker.ticks.query(startVisible, endVisible).to(tick -> {
                        double[] values = candle.valueAt(tick);
                        double x = axisX.getPositionForValue(tick.openTime());
                        double high = axisY.getPositionForValue(values[1]);
                        double low = axisY.getPositionForValue(values[2]);

                        if (chart.showCandle.is(true)) {
                            gc.setLineWidth(1);
                            gc.setStroke(candleType.coordinator.apply(tick));
                            gc.strokeLine(x, high, x, low);
                            if (needDrawingOpenAndClose) {
                                double open = axisY.getPositionForValue(values[0]);
                                double close = axisY.getPositionForValue(values[3]);
                                gc.setLineWidth(BarWidth);
                                gc.strokeLine(x, open, x, close);
                            }
                        }

                        // reduce drawing cost at initialization phase
                        if (chart.showRealtimeUpdate.is(TRUE) && chart.showIndicator.is(TRUE)) {
                            for (Plotter plotter : plotters) {
                                if (registry.globalSetting(plotter.origin).visible.is(false)) {
                                    continue;
                                }

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

                                // draw candle mark
                                for (CandleMark mark : plotter.candles) {
                                    if (mark.indicator.valueAt(tick)) {
                                        gc.setFill(mark.color);
                                        gc.fillOval(x - (BarWidth / 2), high - BarWidth - 2, BarWidth, BarWidth);
                                    }
                                }
                            }
                            valueX.add(x);
                        }
                    });

                    // reduce drawing cost at initialization phase
                    if (chart.showRealtimeUpdate.is(TRUE) && chart.showIndicator.is(TRUE)) {
                        for (Plotter plotter : plotters) {
                            if (registry.globalSetting(plotter.origin).visible.is(false)) {
                                continue;
                            }

                            double scale = plotter.scale();

                            // draw horizontal line
                            for (Horizon horizon : plotter.horizons) {
                                double y = plotter.area != PlotArea.Main ? height - plotter.area.offset - horizon.value * scale
                                        : horizon.value;

                                gc.setLineWidth(horizon.width);
                                gc.setStroke(horizon.color);
                                gc.setLineDashes(horizon.dashArray);
                                gc.strokeLine(0, y, width, y);
                            }

                            // draw line chart
                            for (LineChart chart : plotter.lines) {
                                if (chart.visible == false) {
                                    continue;
                                }

                                if (plotter.area != PlotArea.Main) {
                                    for (int i = 0; i < chart.valueY.size(); i++) {
                                        chart.valueY.set(i, height - plotter.area.offset - chart.valueY.get(i) * scale);
                                    }
                                }

                                gc.setLineWidth(chart.width);
                                gc.setStroke(chart.color);
                                gc.setLineDashes(chart.dashArray);
                                gc.strokePolyline(valueX.asArray(), chart.valueY.asArray(), valueX.size());
                            }
                        }
                    }
                });
            });
        });

        layoutCandleLatest.layout(() -> {
            chart.ticker.to(ticker -> {
                if (ticker.ticks.isEmpty() || chart.showRealtimeUpdate.is(FALSE)) {
                    return;
                }

                GraphicsContext gc = candleLatest.getGraphicsContext2D();
                gc.clearRect(0, 0, candleLatest.getWidth(), candleLatest.getHeight());

                Tick tick = ticker.latest();
                if (tick == null) {
                    return;
                }

                double x = axisX.getPositionForValue(tick.openTime());
                double open = axisY.getPositionForValue(tick.openPrice());
                double close = axisY.getPositionForValue(tick.closePrice());
                double high = axisY.getPositionForValue(tick.highPrice());
                double low = axisY.getPositionForValue(tick.lowPrice());

                if (chart.showCandle.is(true)) {
                    gc.setStroke(chart.candleType.value().coordinator.apply(tick));
                    gc.setLineWidth(1);
                    gc.strokeLine(x, high, x, low);
                    gc.setLineWidth(BarWidth);
                    gc.strokeLine(x, open, x, close);
                }

                Tick previous = ticker.ticks.before(tick);
                if (previous != null && chart.showIndicator.is(TRUE)) {
                    double lastX = axisX.getPositionForValue(previous.openTime());

                    for (Plotter plotter : plotters) {
                        if (registry.globalSetting(plotter.origin).visible.is(false)) {
                            continue;
                        }

                        double height = getHeight();
                        double scale = plotter.scale();

                        for (LineChart chart : plotter.lines) {
                            if (!chart.valueY.isEmpty() && chart.visible) {
                                gc.setLineWidth(chart.width);
                                gc.setStroke(chart.color);
                                gc.setLineDashes(chart.dashArray);
                                gc.strokeLine(lastX, chart.valueY.last(), x, plotter.area == PlotArea.Main
                                        ? axisY.getPositionForValue(chart.indicator.valueAt(tick).doubleValue())
                                        : height - plotter.area.offset - chart.indicator.valueAt(tick).doubleValue() * scale);
                            }
                        }
                    }
                }
            });
        });
    }

    /**
     * Draw chart info.
     */
    private void drawChartInfo(Tick tick) {
        GraphicsContext gc = chartInfo.clear().getGraphicsContext2D();
        gc.setFont(InfoFont);

        int base = chart.market.v.service.setting.base.scale;
        String date = Chrono.systemByMills(tick.openTime() * 1000).format(Chrono.DateTime);
        String open = CommonText.OpenPrice + " " + Primitives.roundString(tick.openPrice(), base);
        String high = CommonText.HighPrice + " " + Primitives.roundString(tick.highPrice(), base);
        String low = CommonText.LowPrice + " " + Primitives.roundString(tick.lowPrice(), base);
        String close = CommonText.ClosePrice + " " + Primitives.roundString(tick.closePrice(), base);

        int y = chartInfoHeight + chartInfoTopPadding;
        gc.setFill(InfoColor);
        gc.fillText(date, chartInfoLeftPadding, y, chartInfoTitle);
        gc.fillText(open, chartInfoLeftPadding + chartInfoTitle + chartInfoHorizontalGap, y, chartInfoWidth);
        gc.fillText(high, chartInfoLeftPadding + chartInfoTitle + chartInfoWidth + chartInfoHorizontalGap * 2, y, chartInfoWidth);
        gc.fillText(low, chartInfoLeftPadding + chartInfoTitle + chartInfoWidth * 2 + chartInfoHorizontalGap * 3, y, chartInfoWidth);
        gc.fillText(close, chartInfoLeftPadding + chartInfoTitle + chartInfoWidth * 3 + chartInfoHorizontalGap * 4, y, chartInfoWidth);

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
                x += chartInfoTitle + chartInfoHorizontalGap;
            }
            for (LineChart chart : plotter.lines) {
                String name = chart.indicator.name.v;
                if (!name.isEmpty()) name = name.concat(" ");

                gc.setFill(visible && chart.visible ? chart.color : chart.color.deriveColor(0, 1, 1, 0.4));
                gc.fillText(name + chart.info.valueAt(tick), x, y, chartInfoWidth);
                x += chartInfoWidth + chartInfoHorizontalGap;
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
         * @param value
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

        /** THe actual indicator. */
        private final AbstractIndicator<? extends Number, ?> indicator;

        /** The infomation writer. */
        private final Indicator<String> info;

        /** The indicator color. */
        private final Color color;

        /** The indicator line width. */
        private final double width;

        /** The indicator line style. */
        private final double[] dashArray;

        /** The y-axis values. */
        private final DoubleList valueY = new DoubleList(64);

        /** The visibility state. */
        private boolean visible = true;

        /**
         * @param indicator
         * @param style
         * @param info
         */
        LineChart(AbstractIndicator<? extends Number, ?> indicator, Style style, Indicator<String> info) {
            this.indicator = indicator;
            this.color = FXUtils.color(style, "stroke");
            this.width = FXUtils.length(style, "stroke-width", 1);
            this.dashArray = FXUtils.lengths(style, "stroke-dasharray");
            this.info = info == null ? indicator.map(v -> v.toString()) : info;
        }

        /**
         * Toggle visibility.
         */
        private void toggleVisible() {
            visible = !visible;
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
}