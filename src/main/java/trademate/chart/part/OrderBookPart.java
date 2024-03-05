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

import cointoss.Direction;
import cointoss.Market;
import cointoss.orderbook.OrderBookPage;
import javafx.geometry.VPos;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import trademate.ChartTheme;
import trademate.chart.ChartCanvas;
import trademate.chart.ChartView;
import viewtify.ui.canvas.EnhancedCanvas;
import viewtify.ui.helper.User;

public class OrderBookPart extends ChartPart {

    /** The width orderbook bar graph. */
    private static final double OrderbookBarWidth = 40;

    /** The width of orderbook size figure. */
    private static final double OrderbookDigitWidth = 20;

    /** Chart UI */
    public final EnhancedCanvas canvasDigit = createCanvas();

    private final ChartView chart;

    /** The current diminishing scale. */
    private double scale;

    /** The current orderbook for buyer. */
    private List<OrderBookPage> buyers = new ArrayList();

    /** The maximum size on buyers. */
    private double buyerMaxSize = OrderbookBarWidth;

    /** The current orderbook for seller. */
    private List<OrderBookPage> sellers = new ArrayList();

    /** The maximum size on sellers. */
    private double sellerMaxSize = OrderbookBarWidth;

    /**
     * @param parent
     */
    public OrderBookPart(ChartCanvas parent, ChartView chart) {
        super(parent);

        this.chart = chart;

        canvas.font(8).textBaseLine(VPos.CENTER);
        canvasDigit.font(8).textBaseLine(VPos.CENTER);

        layout.layoutBy(chartAxisModification())
                .layoutBy(userInterfaceModification())
                .layoutBy(chart.ticker.observe(), chart.showOrderbook.observe())
                .layoutBy(chart.market.observe()
                        .switchMap(b -> b.orderBook.longs.update.merge(b.orderBook.shorts.update).throttle(1, TimeUnit.SECONDS)))
                .layoutBy(ChartTheme.$.buy.observe(), ChartTheme.$.sell.observe())
                .layoutWhile(chart.showRealtimeUpdate.observing(), chart.showOrderbook.observing());

        parent.when(User.MouseMove).to(e -> {
            // search the nearest and largest order size
            chart.market.to(m -> {
                double y = e.getY();

                OrderBookPage largest = m.orderBook
                        .findLargestOrder(parent.axisY.getValueForPosition(y + 2), parent.axisY.getValueForPosition(y - 2));

                if (largest != null) {
                    double price = largest.price + m.orderBook.ranged();
                    double position = parent.axisY.getPositionForValue(price);
                    int size = (int) largest.size;

                    canvasDigit.clear()
                            .strokeColor(ChartTheme.colorBy(price <= m.tickers.latest.v.price.doubleValue() ? BUY : SELL))
                            .strokeText(size, canvasDigit.getWidth() - largest.size * scale - String.valueOf(size).length() * 7, position);
                }
            });
        });
        
        parent.when(User.MouseExit).to(e -> {
          canvasDigit.clear();  
        });
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void configurePreferedCanvasSize(EnhancedCanvas canvas) {
        canvas.bindSizeTo(OrderbookDigitWidth + OrderbookBarWidth, parent);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void draw() {
        layout.layout(() -> {
            double x = parent.getWidth() - OrderbookBarWidth - OrderbookDigitWidth;
            canvas.setLayoutX(x);
            canvasDigit.setLayoutX(x);

            canvas.clear();

            chart.market.to(m -> {
                collect(m);

                draw(buyers, buyerMaxSize, ChartTheme.colorBy(Direction.BUY));
                draw(sellers, sellerMaxSize, ChartTheme.colorBy(Direction.SELL));
            });
        });
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

    private void collect(Market market) {
        final double visibleMax = parent.axisY.computeVisibleMaxValue();
        final double visibleMin = parent.axisY.computeVisibleMinValue();

        buyers = new ArrayList();
        sellers = new ArrayList();

        // collect buyer pages
        for (OrderBookPage page : market.orderBook.longs.ascendingPages()) {
            if (visibleMin < page.price) {
                buyerMaxSize = Math.max(buyerMaxSize, page.size);
                buyers.add(page);
            } else {
                break;
            }
        }

        // collect seller pages
        for (OrderBookPage page : market.orderBook.shorts.ascendingPages()) {
            if (page.price < visibleMax) {
                sellerMaxSize = Math.max(sellerMaxSize, page.size);
                sellers.add(page);
            } else {
                break;
            }
        }

        scale = OrderbookBarWidth / Math.max(buyerMaxSize, sellerMaxSize);
    }
}
