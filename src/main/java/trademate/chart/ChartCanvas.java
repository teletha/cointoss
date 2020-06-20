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

import static transcript.Transcript.*;

import java.time.Duration;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.TimeUnit;
import java.util.function.Predicate;
import java.util.function.Supplier;

import javafx.collections.ObservableList;
import javafx.geometry.VPos;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Region;
import javafx.scene.paint.Color;
import javafx.scene.shape.LineTo;
import javafx.scene.shape.MoveTo;
import javafx.scene.shape.Path;
import javafx.scene.shape.PathElement;
import javafx.scene.text.Font;

import org.eclipse.collections.api.list.primitive.MutableDoubleList;
import org.eclipse.collections.impl.list.mutable.FastList;
import org.eclipse.collections.impl.list.mutable.primitive.DoubleArrayList;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;

import cointoss.Market;
import cointoss.MarketService;
import cointoss.market.bitflyer.BitFlyer;
import cointoss.market.bitflyer.SFD;
import cointoss.order.OrderBookPage;
import cointoss.ticker.AbstractIndicator;
import cointoss.ticker.Indicator;
import cointoss.ticker.Tick;
import cointoss.ticker.Ticker;
import cointoss.util.Chrono;
import cointoss.util.Num;
import cointoss.util.Primitives;
import kiss.Disposable;
import kiss.I;
import kiss.Signal;
import kiss.Variable;
import kiss.Ⅲ;
import stylist.Style;
import trademate.CommonText;
import trademate.TradeMateStyle;
import trademate.chart.Axis.TickLable;
import trademate.chart.PlotScript.Plotter;
import trademate.setting.Notificator;
import viewtify.Viewtify;
import viewtify.ui.canvas.EnhancedCanvas;
import viewtify.ui.helper.LayoutAssistant;
import viewtify.ui.helper.StyleHelper;
import viewtify.ui.helper.User;
import viewtify.ui.helper.UserActionHelper;
import viewtify.util.FXUtils;

public class ChartCanvas extends Region implements UserActionHelper<ChartCanvas> {

    /** Infomation Font */
    private static final Font InfoFont = Font.font(10.5);

    /** Infomation Color */
    private static final Color InfoColor = Color.rgb(247, 239, 227);

    /** Chart Color */
    private static final Color BuyerColor = FXUtils.color(TradeMateStyle.BUY.opacify(-0.2));

    /** Chart Color */
    private static final Color SellerColor = FXUtils.color(TradeMateStyle.SELL.opacify(-0.2));

    /** The width orderbook bar graph. */
    private static final double OrderbookBarWidth = 40;

    /** The width of orderbook size figure. */
    private static final double OrderbookDigitWidth = 20;

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
    private final EnhancedCanvas candles = new EnhancedCanvas().bindSizeTo(this);

    /** Chart UI */
    private final EnhancedCanvas candleLatest = new EnhancedCanvas().bindSizeTo(this);

    /** Chart UI */
    private final EnhancedCanvas orderbook = new EnhancedCanvas().bindSizeTo(OrderbookDigitWidth + OrderbookBarWidth, this)
            .fontSize(8)
            .textBaseLine(VPos.CENTER);

    /** Chart UI */
    private final EnhancedCanvas orderbookDigit = new EnhancedCanvas().bindSizeTo(OrderbookDigitWidth + OrderbookBarWidth, this)
            .fontSize(8)
            .textBaseLine(VPos.CENTER);

    /** Chart UI */
    private final EnhancedCanvas marketName = new EnhancedCanvas().size(230, 30).fontSize(24).fillColor(50, 50, 50);

    /** Chart UI */
    private final EnhancedCanvas chartInfo = new EnhancedCanvas().bindSizeTo(this);

    /** Chart UI */
    private final EnhancedCanvas supporter = new EnhancedCanvas().bindSizeTo(this);

    /** Flag whether candle chart shoud layout on the next rendering phase or not. */
    final LayoutAssistant layoutCandle = new LayoutAssistant(this);

    /** Flag whether candle chart shoud layout on the next rendering phase or not. */
    private final LayoutAssistant layoutCandleLatest = new LayoutAssistant(this);

    /** Flag whether orderbook shoud layout on the next rendering phase or not. */
    private final LayoutAssistant layoutOrderbook = new LayoutAssistant(this);

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
    private final int chartInfoTitle = 105;

    /** The size of chart infomation area. */
    private final int chartInfoWidth = 75;

    /** The size of chart infomation area. */
    private final int chartInfoHeight = 16;

    /** The size of chart infomation area. */
    private final int chartInfoLeftPadding = 10;

    /** The size of chart infomation area. */
    private final int chartInfoHorizontalGap = 3;

    /** The latest orderbook layer. */
    private OrderbookBar orderbookBar;

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

        chart.market.observe().to(m -> marketName.clear().fillText(m.service.marketReadableName(), 4, 28));
        chart.market.observe().combineLatest(chart.ticker.observe(), Viewtify.observing(chart.scripts)).to(v -> {
            plotters = plottersCache.getUnchecked(v);
            scripts = I.signal(plotters).map(p -> p.origin).distinct().toList();
        });

        layoutCandle.layoutBy(widthProperty(), heightProperty())
                .layoutBy(axisX.scroll.valueProperty(), axisX.scroll.visibleAmountProperty())
                .layoutBy(axisY.scroll.valueProperty(), axisY.scroll.visibleAmountProperty())
                .layoutBy(chart.candleType.observe())
                .layoutBy(chart.ticker.observe()
                        .switchMap(ticker -> ticker.open.startWithNull().throttle(Chart.RefreshTime, TimeUnit.MILLISECONDS)));
        layoutCandleLatest.layoutBy(widthProperty(), heightProperty())
                .layoutBy(axisX.scroll.valueProperty(), axisX.scroll.visibleAmountProperty())
                .layoutBy(axisY.scroll.valueProperty(), axisY.scroll.visibleAmountProperty())
                .layoutBy(chart.candleType.observe())
                .layoutBy(chart.ticker.observe()
                        .switchMap(ticker -> ticker.update.startWithNull().throttle(Chart.RefreshTime, TimeUnit.MILLISECONDS)))
                .layoutWhile(chart.showRealtimeUpdate.observing());
        layoutOrderbook.layoutBy(widthProperty(), heightProperty())
                .layoutBy(axisX.scroll.valueProperty(), axisX.scroll.visibleAmountProperty())
                .layoutBy(axisY.scroll.valueProperty(), axisY.scroll.visibleAmountProperty())
                .layoutBy(chart.ticker.observe(), chart.showOrderbook.observe())
                .layoutBy(chart.market.observe()
                        .map(m -> m.orderBook)
                        .flatMap(b -> b.longs.update.merge(b.shorts.update).throttle(1, TimeUnit.SECONDS)))
                .layoutWhile(chart.showRealtimeUpdate.observing());

        configIndicator();
        visualizeOrderPrice();
        visualizeLatestPrice();
        visualizeMouseTrack();
        visualizeSFDPrice();
        visualizePriceSupporter();

        getChildren()
                .addAll(marketName, backGridVertical, backGridHorizontal, notifyPrice, orderBuyPrice, orderSellPrice, latestPrice, sfdPrice, orderbook, orderbookDigit, candles, candleLatest, chartInfo, supporter, mouseTrackHorizontal, mouseTrackVertical);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Object ui() {
        return this;
    }

    /**
     * Find the {@link PlotScript} by info text position.
     * 
     * @param e
     * @return
     */
    private Variable<PlotScript> findScriptByInfoText(MouseEvent e) {
        double x = e.getX();
        double y = e.getY();

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
        double y = e.getY();

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

        return chart.ticker.v.ticks.getByTime(sec);
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
     * Visualize mouse tracker in chart.
     */
    private void visualizeMouseTrack() {
        TickLable labelX = mouseTrackVertical.createLabel()
                .formatter(v -> Chrono.systemBySeconds((long) v).format(Chrono.DateTimeWithoutSec));
        TickLable labelY = mouseTrackHorizontal.createLabel();

        // track on move
        when(User.MouseMove, User.MouseDrag).to(e -> {
            double x = axisX.getValueForPosition(e.getX());
            double y = e.getY();
            labelX.value.set(x);
            labelY.value.set(axisY.getValueForPosition(y));

            mouseTrackVertical.layoutLine.requestLayout();
            mouseTrackHorizontal.layoutLine.requestLayout();

            // move the start position forward for visual consistency
            long sec = (long) x + chart.ticker.v.span.duration.toSeconds() / 2;

            // update upper info
            Tick tick = chart.ticker.v.ticks.getByTime(sec);

            if (tick != null) {
                drawChartInfo(tick);
            }

            // search the nearest and largest order size
            chart.market.to(m -> {
                OrderBookPage largest = m.orderBook.findLargestOrder(axisY.getValueForPosition(y + 2), axisY.getValueForPosition(y - 2));

                if (largest != null && orderbookBar != null) {
                    double position = axisY.getPositionForValue(largest.price.doubleValue());
                    orderbookDigit.clear()
                            .strokeColor(largest.price.isLessThanOrEqual(m.orderBook.longs.best.v.price) ? BuyerColor : SellerColor)
                            .strokeText((int) largest.size, orderbookDigit.getWidth() - largest.size * orderbookBar.scale - 15, position);
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
            orderbookDigit.clear();
        });
    }

    /**
     * Visualize entry supporter or notifiable price in chart.
     */
    private void visualizePriceSupporter() {
        Predicate<MouseEvent> RightButton = e -> e.getButton() == MouseButton.SECONDARY;

        when(User.MousePress).take(RightButton).to(pressed -> {

            Disposable dispose = Disposable.empty();
            when(User.MouseDrag).take(RightButton).to(dragged -> {
                drawSupporterArea(pressed, dragged);
            }, dispose);

            when(User.MouseRelease).take(RightButton).take(1).to(released -> {
                if (pressed.getX() != released.getX() || pressed.getY() != released.getY()) {
                    supporter.clear();
                } else {
                    notifyByPrice(released);
                }
            }, dispose);
        });
    }

    private void drawSupporterArea(MouseEvent start, MouseEvent end) {
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

        int scale = chart.market.v.service.setting.baseCurrencyScaleSize;
        double textX = endX - 40;
        double lineY = -12;
        gc.setStroke(Color.WHITESMOKE);
        gc.setFont(Font.font(10));
        gc.strokeText(en("Duration") + "\t" + Chrono.formatAsDuration(Math.abs(endTime - startTime) * 1000), textX, endY + lineY);
        gc.strokeText(en("Spread") + "\t" + Primitives.roundString(Math.abs(upperPrice - lowerPrice), scale), textX, endY + lineY * 2);
    }

    /**
     * Notify by price.
     * 
     * @param e
     */
    private void notifyByPrice(MouseEvent e) {
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
    }

    /**
     * Notify by price.
     * 
     * @param e
     */
    private void notifyByIndicator(MouseEvent e) {
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
        drawOrderbook();
    }

    /**
     * Draw candle chart.
     */
    private void drawCandle() {
        layoutCandle.layout(() -> {
            // estimate visible range
            chart.ticker.map(v -> v.ticks.last()).map(t -> t.startSeconds - chart.ticker.v.span.seconds).to(end -> {
                long start = (long) axisX.computeVisibleMinValue();

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
                    double high = axisY.getPositionForValue(tick.highPrice().doubleValue());
                    double low = axisY.getPositionForValue(tick.lowPrice().doubleValue());

                    gc.setStroke(chart.candleType.v.coordinator.apply(tick));
                    gc.setLineWidth(1);
                    gc.strokeLine(x, high, x, low);
                    if (needDrawingOpenAndClose) {
                        double open = axisY.getPositionForValue(tick.openPrice.doubleValue());
                        double close = axisY.getPositionForValue(tick.closePrice().doubleValue());
                        gc.setLineWidth(BarWidth);
                        gc.strokeLine(x, open, x, close);
                    }

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
                        gc.strokePolyline(arrayX, chart.valueY.toArray(), valueX.size());
                    }
                }
            });
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
                        if (!chart.valueY.isEmpty() && chart.visible) {
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
        gc.setFont(InfoFont);

        int base = chart.market.v.service.setting.baseCurrencyScaleSize;
        String date = Chrono.systemByMills(tick.startSeconds * 1000).format(Chrono.DateTime);
        String open = CommonText.OpenPrice + " " + tick.openPrice.scale(base);
        String high = CommonText.HighPrice + " " + tick.highPrice().scale(base);
        String low = CommonText.LowPrice + " " + tick.lowPrice().scale(base);
        String close = CommonText.ClosePrice + " " + tick.closePrice().scale(base);

        int y = chartInfoHeight;
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
     * Draw orderbooks on chart.
     */
    private void drawOrderbook() {
        layoutOrderbook.layout(() -> {
            double x = getWidth() - OrderbookBarWidth - OrderbookDigitWidth;
            orderbook.setLayoutX(x);
            orderbookDigit.setLayoutX(x);

            orderbook.clear();

            if (chart.showOrderbook.value()) {
                chart.market.to(m -> {
                    orderbookBar = new OrderbookBar(m);
                    orderbookBar.draw();
                });
            }
        });
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
        private final MutableDoubleList valueY = new NoCopyDoubleList(64);

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

    /**
     * 
     */
    private class OrderbookBar {

        /** The current diminishing scale. */
        private final double scale;

        /** The current orderbook for buyer. */
        private final List<OrderBookPage> buyers = new FastList();

        /** The maximum size on buyers. */
        private double buyerMaxSize = OrderbookBarWidth;

        /** The current orderbook for seller. */
        private final List<OrderBookPage> sellers = new FastList();

        /** The maximum size on sellers. */
        private double sellerMaxSize = OrderbookBarWidth;

        /**
         * Calculate info.
         * 
         * @param market
         */
        private OrderbookBar(Market market) {
            final double visibleMax = axisY.computeVisibleMaxValue();
            final double visibleMin = axisY.computeVisibleMinValue();

            // collect buyer pages
            for (OrderBookPage page : market.orderBook.longs.ascendingPages()) {
                if (visibleMin < page.price.doubleValue()) {
                    buyerMaxSize = Math.max(buyerMaxSize, page.size);
                    buyers.add(page);
                } else {
                    break;
                }
            }

            // collect seller pages
            for (OrderBookPage page : market.orderBook.shorts.descendingPages()) {
                if (page.price.doubleValue() < visibleMax) {
                    sellerMaxSize = Math.max(sellerMaxSize, page.size);
                    sellers.add(page);
                } else {
                    break;
                }
            }

            scale = OrderbookBarWidth / Math.max(buyerMaxSize, sellerMaxSize);
        }

        /**
         * Draw orderbooks on chart' side.
         */
        private void draw() {
            draw(buyers, buyerMaxSize, BuyerColor);
            draw(sellers, sellerMaxSize, SellerColor);
        }

        /**
         * Draw orderbooks on chart' side.
         * 
         * @param pages The page info.
         * @param threshold A range to draw.
         * @param color Visible color.
         */
        private void draw(List<OrderBookPage> pages, double max, Color color) {
            double upper = max * 0.75;
            double start = orderbook.getWidth();
            double lastPosition = 0;
            int hideSize = chart.orderbookHideSize.value();

            GraphicsContext gc = orderbook.getGraphicsContext2D();
            gc.setStroke(color);

            for (OrderBookPage page : pages) {
                if (page.size < hideSize) {
                    continue; // hiding
                }

                double position = axisY.getPositionForValue(page.price.doubleValue());
                double width = start - page.size * scale;
                gc.strokeLine(start, position, width, position);
                if (page.size > upper && Math.abs(lastPosition - position) > 8) {
                    gc.strokeText(String.valueOf((int) page.size), width - 15, position, OrderbookBarWidth);
                    lastPosition = position;
                }
            }
        }
    }
}
