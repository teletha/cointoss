/*
 * Copyright (C) 2019 CoinToss Development Team
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          https://opensource.org/licenses/MIT
 */
package trademate;

import java.util.concurrent.TimeUnit;
import java.util.stream.IntStream;

import cointoss.execution.Execution;
import cointoss.ticker.RealtimeTicker;
import cointoss.util.Chrono;
import cointoss.util.Primitives;
import stylist.Style;
import stylist.StyleDSL;
import viewtify.Viewtify;
import viewtify.style.FormStyles;
import viewtify.ui.UILabel;
import viewtify.ui.UIListView;
import viewtify.ui.UISpinner;
import viewtify.ui.View;
import viewtify.ui.ViewDSL;
import viewtify.util.Icon;

public class ExecutionView extends View {

    private UILabel delay;

    private UILabel countLong;

    private UILabel countShort;

    private UILabel countRatio;

    private UILabel volumeLong;

    private UILabel volumeShort;

    private UILabel volumeRatio;

    /** The execution list. */
    private UIListView<Execution> executionList;

    /** The execution list. */
    private UIListView<Execution> executionCumulativeList;

    /** UI for interval configuration. */
    private UISpinner<Integer> takerSize;

    /** Parent View */
    private TradingView tradingView;

    class view extends ViewDSL implements TradeMateStyle, FormStyles {

        {
            $(vbox, style.root, FormLabelMin, () -> {
                form(en("Delay"), delay);
                form(en("Count"), FormInputMin, countLong.style(Long), countShort.style(Short), countRatio);
                form(en("Volume"), FormInputMin, volumeLong.style(Long), volumeShort.style(Short), volumeRatio);
                $(executionList, style.fill);
                $(hbox, () -> {
                    $(takerSize, style.takerSize);
                });
                $(executionCumulativeList, style.fill);
            });
        }
    }

    interface style extends StyleDSL {
        Style root = () -> {
            display.minWidth(170, px).maxWidth(170, px).height.fill();

            $.descendant(() -> {
                text.unselectable();
            });
        };

        Style takerSize = () -> {
            display.width(70, px);
        };

        Style fill = () -> {
            display.height.fill();
        };
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void initialize() {
        tradingView.market.tickers.latest.observe().throttle(1000, TimeUnit.MILLISECONDS).on(Viewtify.UIThread).to(e -> {
            long diff = System.currentTimeMillis() - e.mills;
            if (diff < 0) {
                delay.tooltip("The time on your computer may not be accurate.\r\nPlease synchronize the time with public NTP server.");
                delay.ui.setGraphic(Icon.Error.image());
            } else if (1000 < diff) {
                delay.tooltip("You are experiencing significant delays and may be referring to outdated data.\r\nWe recommend that you stop trading.");
                delay.ui.setGraphic(Icon.Error.image());
            } else {
                delay.untooltip();
                delay.ui.setGraphic(null);
            }
            delay.text(diff + "ms");
        });

        RealtimeTicker realtime = tradingView.market.tickers.realtime(60);
        Chrono.seconds().on(Viewtify.UIThread).to(() -> {
            int longCount = realtime.longCount();
            int shortCount = realtime.shortCount();
            double longVolume = realtime.longVolume();
            double shortVolume = realtime.shortVolume();

            countLong.text(longCount);
            countShort.text(shortCount);
            countRatio.text(Primitives.roundString(longCount / (shortCount + 0.000000001), 2));

            volumeLong.text(Primitives.roundString(longVolume, 1));
            volumeShort.text(Primitives.roundString(shortVolume, 1));
            volumeRatio.text(Primitives.roundString(longVolume / (shortVolume + 0.0000000001), 2));
        });

        int scale = tradingView.market.service.setting.targetCurrencyScaleSize;

        // configure UI
        takerSize.initialize(IntStream.range(1, 51).boxed());
        executionList.render((label, e) -> update(label, e, false, scale));
        executionCumulativeList.render((label, e) -> update(label, e, true, scale)).take(takerSize, (e, size) -> size <= e.accumulative);

        // load execution log
        tradingView.service
                .add(tradingView.market.timeline.take(tradingView.chart.showRealtimeUpdate.observing()).on(Viewtify.UIThread).to(e -> {
                    executionList.addItemAtFirst(e);

                    if (15 < executionList.size()) {
                        executionList.removeItemAtLast();
                    }
                }));

        // load big taker log
        tradingView.service.add(tradingView.market.timelineByTaker.take(tradingView.chart.showRealtimeUpdate.observing())
                .on(Viewtify.UIThread)
                .to(e -> {
                    if (1 <= e.accumulative) {
                        executionCumulativeList.addItemAtFirst(e);

                        if (1000 < executionCumulativeList.size()) {
                            executionCumulativeList.removeItemAtLast();
                        }
                    }
                }));
    }

    private void update(UILabel label, Execution e, boolean accum, int scale) {
        String text = Chrono.system(e.date).format(Chrono.Time) + "  " + e.price + " " + (accum
                ? Primitives.roundDecimal(e.accumulative, scale)
                : e.size.scale(scale)) + "  " + e.delay;

        label.text(text).styleOnly(TradeMateStyle.Side.of(e.direction));
    }
}
