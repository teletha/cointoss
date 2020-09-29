/*
 * Copyright (C) 2020 Nameless Production Committee
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          http://opensource.org/licenses/mit-license.php
 */
package trademate;

import static trademate.FXColorPalettes.*;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.TimeUnit;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.css.Styleable;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.layout.Region;
import javafx.scene.paint.Color;

import cointoss.Currency;
import cointoss.MarketService;
import cointoss.market.MarketServiceProvider;
import cointoss.market.binance.Binance;
import cointoss.market.bitfinex.Bitfinex;
import cointoss.market.bitflyer.BitFlyer;
import cointoss.market.bitmex.BitMex;
import cointoss.market.ftx.FTX;
import cointoss.util.Primitives;
import cointoss.util.ring.RingBuffer;
import cointoss.volume.GlobalVolume;
import kiss.I;
import stylist.Style;
import stylist.StyleDSL;
import viewtify.Viewtify;
import viewtify.ui.UILabel;
import viewtify.ui.UserInterfaceProvider;
import viewtify.ui.View;
import viewtify.ui.ViewDSL;
import viewtify.ui.canvas.EnhancedCanvas;

/**
 * 
 */
public class GlobalVolumeView extends View {

    /** The maximum store size. */
    private static final int MaxSpan = 20;

    /** The interval time(second) for each span. */
    private static final int SpanInterval = 10;

    /** The interval time(second) for each update. */
    private static final int UpdateInterval = 1;

    private static final Map<String, Color> Colors = Map
            .of(BitFlyer.BTC_JPY.exchangeName, Pastel10[0], BitMex.XBT_USD.exchangeName, Pastel10[1], Binance.BTC_USDT.exchangeName, Pastel10[2], Binance.FUTURE_BTC_USDT.exchangeName, Pastel10[2], Bitfinex.BTC_USD.exchangeName, Pastel10[3], FTX.BTC_PERP.exchangeName, Pastel10[4]);

    private final ObservableList<CurrencyView> charts = FXCollections.observableArrayList();

    class view extends ViewDSL {
        {
            $(vbox, charts);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void initialize() {
        List<Currency> currencies = List.of(Currency.BTC, Currency.ETH, Currency.XRP, Currency.EOS);
        for (Currency currency : currencies) {
            charts.add(new CurrencyView(currency));
        }
        I.schedule(0, SpanInterval, TimeUnit.SECONDS, true).flatIterable(x -> charts).to(c -> c.chart.volumes.add(new GlobalVolume()));
        I.schedule(0, UpdateInterval, TimeUnit.SECONDS, true).on(Viewtify.UIThread).flatIterable(x -> charts).to(c -> c.update());
    }

    static class CurrencyView extends View {

        private Currency target;

        private UILabel name;

        private UILabel volumeLong;

        private UILabel volumeShort;

        private UILabel volumeRatio;

        private final CurrencyVolume chart;

        class view extends ViewDSL {
            {
                $(vbox, () -> {
                    $(hbox, () -> {
                        $(name, style.info);
                        $(volumeLong, style.info, TradeMateStyle.Long);
                        $(volumeShort, style.info, TradeMateStyle.Short);
                        $(volumeRatio, style.info);
                    });
                    $(() -> chart, style.chart);
                });
            }
        }

        interface style extends StyleDSL {

            Style info = () -> {
                display.width(45, px);
            };

            Style chart = () -> {
                display.width(300, px).height(110, px);
            };
        }

        /**
         * Currency info on global markets.
         * 
         * @param target
         */
        CurrencyView(Currency target) {
            this.target = target;
            this.chart = new CurrencyVolume(target);
        }

        /**
         * {@inheritDoc}
         */
        @Override
        protected void initialize() {
            name.text(target.code);
        }

        private void update() {
            // realtime volume
            double[] longVolume = {0};
            double[] shortVolume = {0};
            chart.volumes.forEachFromLatest(6, v -> {
                if (v != null) {
                    longVolume[0] += v.longVolume();
                    shortVolume[0] += v.shortVolume();
                }
            });

            volumeLong.text(Primitives.roundString(longVolume[0], 1));
            volumeShort.text(Primitives.roundString(shortVolume[0], 1));
            volumeRatio.text(Primitives.percent(longVolume[0], longVolume[0] + shortVolume[0]));

            // chart
            chart.drawVolume();
        }
    }

    private static class CurrencyVolume extends Region implements UserInterfaceProvider {

        /** The volumes on various markets. */
        private final RingBuffer<GlobalVolume> volumes = new RingBuffer(MaxSpan);

        /** The chart pane. */
        private final EnhancedCanvas canvas = new EnhancedCanvas().bindSizeTo(this).strokeColor(80, 80, 80).fontSize(8);

        /**
         * @param target
         */
        private CurrencyVolume(Currency target) {
            getChildren().add(canvas);

            volumes.add(new GlobalVolume());
            MarketServiceProvider.availableMarketServices().take(service -> service.setting.target.currency == target).to(service -> {
                service.executionsRealtimely().to(e -> {
                    volumes.latest().add(service, e);
                });
            });
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public Styleable ui() {
            return this;
        }

        /**
         * Draw volumes.
         */
        private void drawVolume() {
            canvas.clear();

            // compute maximum volume
            final double[] maxs = new double[2];
            volumes.forEach(volume -> {
                if (volume != null) {
                    maxs[0] = Math.max(maxs[0], volume.longVolume());
                    maxs[1] = Math.max(maxs[1], volume.shortVolume());
                }
            });
            final double maxVolume = Math.max(maxs[0], maxs[1]);
            final double padding = 15;
            final double maxHeight = 35;
            final double ratio = Math.min(1, maxHeight / maxVolume);
            final double width = 12;

            GraphicsContext context = canvas.getGraphicsContext2D();
            context.setStroke(Color.GRAY);
            context.setLineWidth(0.5);
            context.strokeLine(0, maxHeight + padding, canvas.getWidth(), maxHeight + padding);

            double[] x = {width * (MaxSpan - 1)};
            boolean[] canDisplayVolume = {true, true};

            volumes.forEachFromLatest(volume -> {
                if (volume != null) {
                    double buyerY = maxHeight + padding;
                    double sellerY = maxHeight + padding;
                    x[0] -= width;

                    for (Entry<MarketService, double[]> entry : volume.volumes()) {
                        String name = entry.getKey().exchangeName;
                        double[] volumes = entry.getValue();

                        // buyer
                        double buyerFixedVolume = volumes[0] * ratio;
                        context.setFill(Colors.getOrDefault(name, TradeMateStyle.BUY_FX));
                        context.fillRect(x[0], buyerY - buyerFixedVolume, width, buyerFixedVolume);
                        buyerY = buyerY - buyerFixedVolume;

                        // seller
                        double sellerFixedVolume = volumes[1] * ratio;
                        context.setFill(Colors.getOrDefault(name, TradeMateStyle.SELL_FX));
                        context.fillRect(x[0], sellerY, width, sellerFixedVolume);
                        sellerY = sellerY + sellerFixedVolume;
                    }

                    if (canDisplayVolume[0] && maxs[0] * 0.6 <= volume.longVolume()) {
                        context.strokeText(Primitives.roundString(volume.longVolume(), 1), x[0], buyerY - 3);
                        canDisplayVolume[0] = false;
                    } else {
                        canDisplayVolume[0] = true;
                    }

                    if (canDisplayVolume[1] && maxs[1] * 0.6 <= volume.shortVolume()) {
                        context.strokeText(Primitives.roundString(volume.shortVolume(), 1), x[0], sellerY + 3 + 6);
                        canDisplayVolume[1] = false;
                    } else {
                        canDisplayVolume[1] = true;
                    }
                }
            });
        }
    }
}
