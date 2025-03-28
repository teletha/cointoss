/*
 * Copyright (C) 2025 The COINTOSS Development Team
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          https://opensource.org/licenses/MIT
 */
package trademate.chart.part;

import java.util.concurrent.TimeUnit;

import cointoss.volume.PriceRangedVolumeManager.GroupedVolumes;
import cointoss.volume.PriceRangedVolumeManager.PriceRangedVolumePeriod;
import hypatia.Num;
import javafx.geometry.VPos;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import trademate.chart.ChartCanvas;
import trademate.chart.ChartView;
import trademate.chart.PriceRangedVolumeType;

public class PriceRangedVolumePart extends ChartPart {

    private final ChartView chart;

    /**
     * @param parent
     */
    public PriceRangedVolumePart(ChartCanvas parent, ChartView chart) {
        super(parent);

        this.chart = chart;

        canvas.strokeColor(Color.WHITESMOKE.deriveColor(0, 1, 1, 0.35)).font(8).textBaseLine(VPos.CENTER);

        layout.layoutBy(chartAxisModification())
                .layoutBy(userInterfaceModification())
                .layoutBy(chart.ticker.observe(), chart.market.observe(), chart.showPricedVolume.observe(), chart.pricedVolumeType
                        .observe(), chart.orderbookPriceRange.observe())
                .layoutBy(chart.market.observe().switchMap(m -> m.timeline.throttle(2, TimeUnit.SECONDS)))
                .layoutWhile(chart.showRealtimeUpdate.observing(), chart.showPricedVolume.observing());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void draw() {
        layout.layout(() -> {
            canvas.clear();

            chart.market.to(m -> {
                PriceRangedVolumePeriod[] volumes = m.priceVolume.latest();
                if (volumes[0] != null) {
                    // update volumes
                    Num range = chart.orderbookPriceRange.value();

                    GroupedVolumes longs = volumes[0].aggregateByPrice(range);
                    GroupedVolumes shorts = volumes[1].aggregateByPrice(range);

                    // Draw price-ranged volumes on chart.
                    PriceRangedVolumeType type = chart.pricedVolumeType.value();

                    double max = type.max(longs.maxVolume, shorts.maxVolume);
                    double widthForPeriod = Math.min(50, parent.axisX.getLengthForValue(60 * 60 * 8));
                    double scale = widthForPeriod / max * type.scale();

                    GraphicsContext gc = canvas.getGraphicsContext2D();
                    double start = 30;

                    for (int i = 0, size = longs.prices.size(); i < size; i++) {
                        double position = parent.axisY.getPositionForValue(longs.prices.get(i));
                        float l = longs.volumes.get(i);
                        float s = shorts.volumes.get(i);

                        if (type == PriceRangedVolumeType.Both) {
                            gc.strokeLine(start, position, start + l * scale, position);
                            gc.strokeLine(start, position, start - s * scale, position);
                        } else {
                            gc.strokeLine(start, position, start + type.width(l, s) * scale, position);
                        }
                    }
                }
            });
        });
    }
}