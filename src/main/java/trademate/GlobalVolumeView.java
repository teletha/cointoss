/*
 * Copyright (C) 2024 The COINTOSS Development Team
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          https://opensource.org/licenses/MIT
 */
package trademate;

import static cointoss.Currency.*;
import static trademate.FXColorPalettes.*;

import java.text.Normalizer.Form;
import java.util.HashMap;
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
import javafx.scene.paint.Paint;

import cointoss.Currency;
import cointoss.MarketService;
import cointoss.market.Exchange;
import cointoss.market.MarketServiceProvider;
import cointoss.volume.GlobalVolume;
import hypatia.Primitives;
import kiss.I;
import kiss.Variable;
import primavera.ring.RingBuffer;
import stylist.Style;
import stylist.StyleDSL;
import trademate.setting.Notificator;
import viewtify.Viewtify;
import viewtify.style.FormStyles;
import viewtify.ui.UILabel;
import viewtify.ui.UIScrollPane;
import viewtify.ui.UIText;
import viewtify.ui.UserInterfaceProvider;
import viewtify.ui.View;
import viewtify.ui.ViewDSL;
import viewtify.ui.canvas.EnhancedCanvas;

/**
 * 
 */
public class GlobalVolumeView extends View {

    /** The maximum store size. */
    private static final int MaxSpan = 25;

    /** The width of volume chart. */
    private static final int BarWidth = 10;

    /** The interval time(ms) for each span. */
    private static final int SpanInterval = 10000;

    /** The interval time(ms) for each update. */
    private static final int UpdateInterval = 1000;

    private static final Map<Exchange, Color> colors = new HashMap();

    private final ObservableList<CurrencyView> charts = FXCollections.observableArrayList();

    private UIScrollPane scroll;

    private UILabel binance;

    private UILabel bitbank;

    private UILabel bitfinex;

    private UILabel bitfyler;

    private UILabel bitmex;

    private UILabel bybit;

    private UILabel coinbase;

    private UILabel gmo;

    class view extends ViewDSL {
        {
            $(scroll, () -> {
                $(vbox, () -> {
                    $(hbox, () -> {
                        $(binance, style.name);
                        $(bitbank, style.name);
                        $(bitfinex, style.name);
                        $(bitfyler, style.name);
                        $(bitmex, style.name);
                    });
                    $(hbox, () -> {
                        $(bybit, style.name);
                        $(coinbase, style.name);
                        $(gmo, style.name);
                    });
                    $(vbox, charts);
                });
            });
        }
    }

    interface style extends StyleDSL {
        Style name = () -> {
            font.size(10.5, px).weight.bold();
            display.width(48, px);
            padding.top(1, px);
        };
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void initialize() {
        drawSample(binance, Exchange.Binance, Pastel10[0], Exchange.BinanceF);
        drawSample(bitbank, Exchange.BitBank, Pastel10[1]);
        drawSample(bitfinex, Exchange.Bitfinex, Pastel10[2]);
        drawSample(bitfyler, Exchange.BitFlyer, Pastel10[3]);
        drawSample(bitmex, Exchange.BitMEX, Pastel10[4]);
        drawSample(bybit, Exchange.Bybit, Pastel10[5]);
        drawSample(coinbase, Exchange.Coinbase, Pastel10[6]);
        drawSample(gmo, Exchange.GMO, Pastel10[8]);

        List<Currency> currencies = List.of(BTC, ETH, XRP, EOS, COMP, SRM, DOGE);
        for (Currency currency : currencies) {
            charts.add(new CurrencyView(currency));
        }
        I.schedule(0, SpanInterval, TimeUnit.MILLISECONDS, true).flatIterable(x -> charts).to(c -> {
            c.chart.volumes.add(new GlobalVolume());
            c.longCount = 1;
            c.shortCount = 1;
        });
        I.schedule(0, UpdateInterval, TimeUnit.MILLISECONDS, true).on(Viewtify.UIThread).flatIterable(x -> charts).to(c -> c.update());
    }

    /**
     * Display the target exchange.
     * 
     * @param label An actual UI.
     * @param exchange A target.
     * @param color An associated color.
     * @param additions A list of friend exchanges.
     */
    private void drawSample(UILabel label, Exchange exchange, Color color, Exchange... additions) {
        colors.put(exchange, color);
        for (Exchange addition : additions) {
            colors.put(addition, color);
        }

        // FIXME : Probably JavaFX's Bug
        // When the CSS recalculation occurs, somehow the current value is ignored
        // and overwritten by the CSS value (the root's value is used because color is
        // not specified for this ui).
        // We have no choice but to disable external recalculation by binding colors
        // instead of specifying it.
        label.text(exchange.name()).color(Variable.of(color));
    }

    static class CurrencyView extends View {

        private Currency target;

        private UILabel name;

        private UILabel volumeLong;

        private UILabel volumeShort;

        private UILabel volumeRatio;

        private UIText<String> threshold;

        private final CurrencyVolume chart;

        private final Notificator notificator = I.make(Notificator.class);

        private int longCount = 1;

        private int shortCount = 1;

        class view extends ViewDSL {
            {
                $(vbox, () -> {
                    $(hbox, FormStyles.Row, () -> {
                        $(name, style.info);
                        $(volumeLong, style.info);
                        $(volumeShort, style.info);
                        $(volumeRatio, style.info);
                        $(threshold, style.form);
                    });
                    $(() -> chart, style.chart);
                });
            }
        }

        interface style extends StyleDSL {

            Style info = () -> {
                display.width(45, px);
                font.weight.bold();
            };

            Style form = () -> {
                display.width(60, px);
            };

            Style chart = () -> {
                display.width(BarWidth * MaxSpan + 6, px).height(85, px);
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
            volumeLong.color(ChartTheme.$.buy);
            volumeShort.color(ChartTheme.$.sell);
            threshold.acceptPositiveDecimalInput().normalizeInput(Form.NFKD).initialize("0");
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

            double size = Double.parseDouble(threshold.value());
            GlobalVolume latest = chart.volumes.latest();
            if (0 < size) {
                if (size * longCount <= latest.longVolume()) {
                    notificator.longTrend.notify(target.code, "Long!!!!!!!!!");
                    longCount++;
                }
                if (size * shortCount <= latest.shortVolume()) {
                    notificator.shortTrend.notify(target.code, "Short!!!!!!");
                    shortCount++;
                }
            }

            // chart
            chart.drawVolume();
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public String id() {
            return target.code;
        }
    }

    private static class CurrencyVolume extends Region implements UserInterfaceProvider {

        /** The volumes on various markets. */
        private final RingBuffer<GlobalVolume> volumes = new RingBuffer(MaxSpan);

        /** The chart pane. */
        private final EnhancedCanvas canvas = new EnhancedCanvas().bindSizeTo(this).strokeColor(160, 160, 160).font(8).lineWidth(0.4);

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

                service.liquidationRealtimely().to(e -> {
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
            final float[] maxs = new float[2];
            volumes.forEach(volume -> {
                if (volume != null) {
                    maxs[0] = Math.max(maxs[0], volume.longVolume());
                    maxs[1] = Math.max(maxs[1], volume.shortVolume());
                }
            });
            final float maxVolume = Math.max(maxs[0], maxs[1]);
            final double padding = 12;
            final float maxHeight = 28;
            final float ratio = Math.min(1, maxHeight / maxVolume);

            GraphicsContext context = canvas.getGraphicsContext2D();
            Paint textColor = context.getStroke();
            Paint liquidatedColor = Color.web("#dfdfdf");
            context.strokeLine(0, maxHeight + padding, BarWidth * MaxSpan, maxHeight + padding);

            double[] x = {BarWidth * MaxSpan};
            boolean[] canDisplayVolume = {true, true};

            volumes.forEachFromLatest(volume -> {
                if (volume != null) {
                    double buyerY = maxHeight + padding - 1;
                    double sellerY = maxHeight + padding + 1;
                    x[0] -= BarWidth;

                    for (Entry<MarketService, float[]> entry : volume.volumes()) {
                        float[] volumes = entry.getValue();

                        // buyer
                        float buyerFixedVolume = volumes[0] * ratio;
                        context.setFill(colors.get(entry.getKey().exchange));
                        context.fillRect(x[0], buyerY - buyerFixedVolume, BarWidth, buyerFixedVolume);
                        buyerY = buyerY - buyerFixedVolume;

                        // seller
                        float sellerFixedVolume = volumes[1] * ratio;
                        context.setFill(colors.get(entry.getKey().exchange));
                        context.fillRect(x[0], sellerY, BarWidth, sellerFixedVolume);
                        sellerY = sellerY + sellerFixedVolume;
                    }

                    if (canDisplayVolume[0] && 0 < maxs[0] && maxs[0] * 0.6 <= volume.longVolume()) {
                        String text = Primitives.roundString(volume.longVolume(), 0);
                        context.strokeText(text, x[0] + coordinate(text), buyerY - 3);
                        canDisplayVolume[0] = false;
                    } else {
                        canDisplayVolume[0] = true;
                    }

                    if (1 <= volume.liquidatedLongVolume()) {
                        String text = Primitives.roundString(volume.liquidatedLongVolume(), 0);
                        context.setStroke(liquidatedColor);
                        context.strokeText(text, x[0] + coordinate(text), 10);
                        context.setStroke(textColor);
                    }

                    if (canDisplayVolume[1] && 0 < maxs[1] && maxs[1] * 0.6 <= volume.shortVolume()) {
                        String text = Primitives.roundString(volume.shortVolume(), 0);
                        context.strokeText(text, x[0] + coordinate(text), sellerY + 3 + 6);
                        canDisplayVolume[1] = false;
                    } else {
                        canDisplayVolume[1] = true;
                    }

                    if (1 <= volume.liquidatedShortVolume()) {
                        String text = Primitives.roundString(volume.liquidatedShortVolume(), 0);
                        context.setStroke(liquidatedColor);
                        context.strokeText(text, x[0] + coordinate(text), padding + maxHeight * 2);
                        context.setStroke(textColor);
                    }
                }
            });
        }

        private double coordinate(String text) {
            return 4.5 - text.length() * 2.1;
        }
    }
}