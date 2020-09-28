/*
 * Copyright (C) 2020 Nameless Production Committee
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          http://opensource.org/licenses/mit-license.php
 */
package trademate.locust;

import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

import cointoss.Currency;
import cointoss.MarketService;
import cointoss.market.MarketServiceProvider;
import cointoss.util.ring.RingBuffer;
import kiss.I;
import viewtify.ui.View;
import viewtify.ui.ViewDSL;
import viewtify.ui.canvas.EnhancedCanvas;

/**
 * 
 */
public class LocustView extends View {

    /** The maximum store size. */
    private static final int MaxSpan = 30;

    /** The interval time(second) for each span. */
    private static final int SpanInterval = 5;

    /** The interval time(second) for each update. */
    private static final int UpdateInterval = 1;

    /** The volumes on various services. */
    private final RingBuffer<Volume> volumes = new RingBuffer(MaxSpan);

    /** The maximum volume tracker. */
    private double maximumVolume = 0;

    private EnhancedCanvas canvas = new EnhancedCanvas().size(200, 200);

    class view extends ViewDSL {
        {
            $(() -> canvas);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void initialize() {
        volumes.add(new Volume());
        I.schedule(0, SpanInterval, TimeUnit.SECONDS, true).to(() -> volumes.add(new Volume()));
        I.schedule(0, UpdateInterval, TimeUnit.SECONDS, true).to(this::drawVolume);

        MarketServiceProvider.availableMarketServices().take(service -> service.setting.target.currency == Currency.BTC).to(service -> {
            service.executionsRealtimely().to(e -> {
                Volume volume = volumes.latest();
                double[] sizes = volume.computeIfAbsent(service, key -> new double[2]);
                if (e.isBuy()) {
                    double size = e.size.doubleValue();
                    sizes[0] += size;
                    volume.buys += size;
                } else {
                    double size = e.size.doubleValue();
                    sizes[1] += size;
                    volume.sells += size;
                }
            });
        });
    }

    /**
     * Draw volumes.
     */
    private void drawVolume() {
        canvas.clear();

        // compute maximum volume
        volumes.forEach(v -> {

        });

        final double max = 200;
        final double ratio = Math.min(1, 20);
        final double width = 30;

        double buyerY = max;
        double sellerY = max;
        GraphicsContext context = canvas.getGraphicsContext2D();

        for (Entry<MarketService, double[]> entry : volumes.latest().entrySet()) {
            double[] volumes = entry.getValue();

            // buyer
            double buyerFixedVolume = volumes[0] * ratio;
            context.setFill(Color.GREEN);
            context.fillRect(0, buyerY - buyerFixedVolume, width, buyerFixedVolume);
            buyerY = buyerY - buyerFixedVolume;

            // seller
            double sellerFixedVolume = volumes[1] * ratio;
            context.setFill(Color.RED);
            context.fillRect(0, sellerY, width, sellerFixedVolume);
            sellerY = sellerY + sellerFixedVolume;
        }
    }

    /**
     * Time based volume.
     */
    @SuppressWarnings("serial")
    private static class Volume extends ConcurrentHashMap<MarketService, double[]> {

        /** The total volume. */
        private double buys = 0;

        /** The total volume. */
        private double sells = 0;

        /**
         * Compute maximum volume side.
         * 
         * @return
         */
        private double maximumVolume() {
            return buys < sells ? sells : buys;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public String toString() {
            StringBuilder builder = new StringBuilder();
            forEach((service, size) -> {
                builder.append(service.marketReadableName).append(":B").append(size[0]).append("S").append(size[1]).append(" ");
            });
            return builder.toString();
        }
    }
}
