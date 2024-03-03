/*
 * Copyright (C) 2023 Nameless Production Committee
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          http://opensource.org/licenses/mit-license.php
 */
package trademate.chart.part;

import static cointoss.Direction.*;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import javafx.geometry.VPos;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

import cointoss.Direction;
import cointoss.orderbook.OrderBookPage;
import trademate.ChartTheme;
import trademate.chart.ChartCanvas;
import trademate.chart.ChartView;
import viewtify.ui.canvas.EnhancedCanvas;

public class OrderBookPart extends ChartPart {

    /** The width orderbook bar graph. */
    private static final double OrderbookBarWidth = 40;

    /** The width of orderbook size figure. */
    private static final double OrderbookDigitWidth = 20;

    /** Chart UI */
    public final EnhancedCanvas digit = createCanvas().font(8).textBaseLine(VPos.CENTER);

    private final ChartView chart;

    /** The current diminishing scale. */
    private final double scale;

    /** The current orderbook for buyer. */
    private final List<OrderBookPage> buyers = new ArrayList();

    /** The maximum size on buyers. */
    private double buyerMaxSize = OrderbookBarWidth;

    /** The current orderbook for seller. */
    private final List<OrderBookPage> sellers = new ArrayList();

    /** The maximum size on sellers. */
    private double sellerMaxSize = OrderbookBarWidth;

    /**
     * @param parent
     */
    public OrderBookPart(ChartCanvas parent, ChartView chart) {
        super(parent);

        this.chart = chart;

        canvas.font(8).textBaseLine(VPos.CENTER);

        layout.layoutBy(chart.chartAxisModification())
                .layoutBy(chart.userInterfaceModification())
                .layoutBy(chart.ticker.observe(), chart.showOrderbook.observe())
                .layoutBy(chart.market.observe()
                        .switchMap(b -> b.orderBook.longs.update.merge(b.orderBook.shorts.update).throttle(1, TimeUnit.SECONDS)))
                .layoutBy(ChartTheme.$.buy.observe(), ChartTheme.$.sell.observe())
                .layoutWhile(chart.showRealtimeUpdate.observing(), chart.showOrderbook.observing());

        final double visibleMax = chart.chart.axisY.computeVisibleMaxValue();
        final double visibleMin = chart.chart.axisY.computeVisibleMinValue();

        // collect buyer pages
        for (OrderBookPage page : chart.market.v.orderBook.longs.ascendingPages()) {
            if (visibleMin < page.price) {
                buyerMaxSize = Math.max(buyerMaxSize, page.size);
                buyers.add(page);
            } else {
                break;
            }
        }

        // collect seller pages
        for (OrderBookPage page : chart.market.v.orderBook.shorts.ascendingPages()) {
            if (page.price < visibleMax) {
                sellerMaxSize = Math.max(sellerMaxSize, page.size);
                sellers.add(page);
            } else {
                break;
            }
        }

        scale = OrderbookBarWidth / Math.max(buyerMaxSize, sellerMaxSize);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onShown() {
        canvas.bindSizeTo(OrderbookDigitWidth + OrderbookBarWidth, parent);
        digit.bindSizeTo(OrderbookDigitWidth + OrderbookBarWidth, parent);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onMouseMove(double x, double y) {
        // search the nearest and largest order size
        chart.market.to(m -> {
            OrderBookPage largest = m.orderBook
                    .findLargestOrder(chart.chart.axisY.getValueForPosition(y + 2), chart.chart.axisY.getValueForPosition(y - 2));

            if (largest != null) {
                double price = largest.price + m.orderBook.ranged();
                double position = chart.chart.axisX.getPositionForValue(price);
                digit.clear()
                        .strokeColor(ChartTheme.colorBy(price <= m.tickers.latest.v.price.doubleValue() ? BUY : SELL))
                        .strokeText((int) largest.size, digit.getWidth() - largest.size * scale - 15, position);
            }
        });
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onMouseExit() {
        digit.clear();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void draw() {
        layout.layout(() -> {
            double x = parent.getWidth() - OrderbookBarWidth - OrderbookDigitWidth;
            canvas.setLayoutX(x);
            digit.setLayoutX(x);

            canvas.clear();
        });
    }

    /**
     * Draw orderbooks on chart' side.
     */
    private void draw2() {
        draw(buyers, buyerMaxSize, ChartTheme.colorBy(Direction.BUY));
        draw(sellers, sellerMaxSize, ChartTheme.colorBy(Direction.SELL));
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
        double start = canvas.getWidth();
        double lastPosition = 0;
        double range = chart.market.v.orderBook.ranged();
        int hideSize = chart.orderbookHideSize.value();

        GraphicsContext gc = canvas.getGraphicsContext2D();
        gc.setStroke(color);

        for (int i = 0, size = pages.size(); i < size; i++) {
            OrderBookPage page = pages.get(i);
            if (page.size < hideSize) {
                continue; // hiding
            }

            double position = chart.chart.axisY.getPositionForValue(page.price + range);
            double width = start - page.size * scale;
            gc.strokeLine(start, position, width, position);
            if (page.size > upper && Math.abs(lastPosition - position) > 8) {
                gc.strokeText(String.valueOf((int) page.size), width - 15, position, OrderbookBarWidth);
                lastPosition = position;
            }
        }
    }
}
