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
import java.util.concurrent.TimeUnit;

import javafx.scene.canvas.GraphicsContext;

import cointoss.Currency;
import cointoss.MarketService;
import cointoss.market.MarketServiceProvider;
import cointoss.util.ring.RingBuffer;
import cointoss.volume.GlobalVolume;
import kiss.I;
import trademate.TradeMateStyle;
import viewtify.ui.View;
import viewtify.ui.ViewDSL;
import viewtify.ui.canvas.EnhancedCanvas;

/**
 * 
 */
public class LocustView extends View {

    /** The maximum store size. */
    private static final int MaxSpan = 20;

    /** The interval time(second) for each span. */
    private static final int SpanInterval = 10;

    /** The interval time(second) for each update. */
    private static final int UpdateInterval = 1;

    /** The volumes on various services. */
    private final RingBuffer<GlobalVolume> volumes = new RingBuffer(MaxSpan);

    private EnhancedCanvas canvas = new EnhancedCanvas().size(300, 300);

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
        volumes.add(new GlobalVolume());
        I.schedule(0, SpanInterval, TimeUnit.SECONDS, true).to(() -> volumes.add(new GlobalVolume()));
        I.schedule(0, UpdateInterval, TimeUnit.SECONDS, true).to(this::drawVolume);

        MarketServiceProvider.availableMarketServices().take(service -> service.setting.target.currency == Currency.BTC).to(service -> {
            service.executionsRealtimely().to(e -> {
                volumes.latest().add(service, e);
            });
        });
    }

    /**
     * Draw volumes.
     */
    private void drawVolume() {
        canvas.clear();

        // compute maximum volume
        double maxVolume = volumes.reduce((a, b) -> a.maximumVolume() > b.maximumVolume() ? a : b).maximumVolume();

        final double maxHeight = 100;
        final double ratio = Math.min(1, maxHeight / maxVolume);
        final double width = 12;

        GraphicsContext context = canvas.getGraphicsContext2D();
        double[] x = {0};

        volumes.forEach(volume -> {
            if (volume != null) {
                double buyerY = maxHeight;
                double sellerY = maxHeight;
                x[0] += width;

                for (Entry<MarketService, double[]> entry : volume.volumes()) {
                    double[] volumes = entry.getValue();

                    // buyer
                    double buyerFixedVolume = volumes[0] * ratio;
                    context.setFill(TradeMateStyle.BUY_FX);
                    context.fillRect(x[0], buyerY - buyerFixedVolume, width, buyerFixedVolume);
                    buyerY = buyerY - buyerFixedVolume;

                    // seller
                    double sellerFixedVolume = volumes[1] * ratio;
                    context.setFill(TradeMateStyle.SELL_FX);
                    context.fillRect(x[0], sellerY, width, sellerFixedVolume);
                    sellerY = sellerY + sellerFixedVolume;
                }
            }
        });
    }
}
